package tw.tcnr01.m1901;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


public class M1901 extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMarkerClickListener{



    private GoogleMap map;

    static LatLng VGPS = new LatLng(24.172127, 120.610313);
    int currentZoom = 12;
    private static String[][] locations = {
            { "我的位置", "24.172127,120.610313" },
            { "中區職訓", "24.172127,120.610313" },
            { "東海大學路思義教堂", "24.179051,120.600610" },
            { "台中公園湖心亭", "24.144671,120.683981" },
            { "秋紅谷", "24.1674900,120.6398902" },
            { "台中火車站", "24.136829,120.685011" },
            { "國立科學博物館", "24.1579361,120.6659828" }
    };

    private static String[] mapType = {
            "街道圖",
            "衛星圖",
            "地形圖",
            "混合圖",
            "開啟路況",
            "關閉路況"};

    private Spinner mSpnLocation, mSpnMapType;
    double dLat, dLon;
    double latitude;
    double longitude;
    private BitmapDescriptor image_des;// 圖標顯示
    private int icosel; //圖示旗標
    //----GPS------------
    private TextView txtOutput;
    private TextView tmsg;
    private Marker markerMe;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private LocationManager locationManager;
    private Location currentLocation;
    private String provider; // 提供資料
    long minTime = 5000;// ms
    float minDist = 5.0f;// meter
    //---記錄軌跡-----------------------
    private ArrayList<LatLng> mytrace; // 追蹤我的位置

    private ScrollView controlScroll;
    private CheckBox checkBox;
    private UiSettings mUiSettings;

    //-----自訂義-------
    int infowindow_ico;
    int showMarkerMeON = 0;

    private int resID = 0;
    private int resID1 = 0;


    private final String TAG = "oldpa=>";
    //----------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1901);

        //------------設定MapFragment-----------------------------------
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        //-------------------------------------------------------

        u_checkgps();  //檢查GPS是否開啟
        setupViewComponent();
    }
    //--------------------
    private void setupViewComponent() {
        mSpnLocation = (Spinner) this.findViewById(R.id.spnLocation);
        mSpnMapType = (Spinner) this.findViewById(R.id.spnMapType);
        // -----------------------------------------------------------------
        txtOutput = (TextView) findViewById(R.id.txtOutput);
        tmsg = (TextView) findViewById(R.id.msg);

        // ----設定control 控制鈕----------
        checkBox = (CheckBox) this.findViewById(R.id.checkcontrol);
        controlScroll = (ScrollView) this.findViewById(R.id.Scroll01);
        checkBox.setOnCheckedChangeListener(chklistener);
        controlScroll.setVisibility(View.INVISIBLE);
        // Parameters: 對應的三個常量值：VISIBLE=0 INVISIBLE=4 GONE=8

        // ---------------
        icosel = 1;  //設定圖示初始值
        // ---------------
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item);

        for (int i = 0; i < locations.length; i++)
            adapter.add(locations[i][0]);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnLocation.setAdapter(adapter);
        //==== 指定事件處理物件==========
        mSpnLocation.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                map.clear();
                mytrace = null; //清除軌跡圖
                showloc();
                setMapLocation();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        // ---------------
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);

        for (int i = 0; i < mapType.length; i++)
            adapter.add(mapType[i]);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpnMapType.setAdapter(adapter);
        //-----------設定ARGB 透明度----
//        mSpnMapType.setPopupBackgroundDrawable(new ColorDrawable(0x00FFFFFF)); //全透明
        mSpnMapType.setPopupBackgroundDrawable(new ColorDrawable(0x80FFFFFF)); //50%透明
        mSpnLocation.setPopupBackgroundDrawable(new ColorDrawable(0x80FFFFFF)); //50%透明
//        # ARGB依次代表透明度（alpha）、紅色(red)、綠色(green)、藍色(blue)
//        100% — FF       95% — F2        90% — E6        85% — D9
//        80% — CC        75% — BF        70% — B3        65% — A6
//        60% — 99        55% — 8C        50% — 80        45% — 73
//        40% — 66        35% — 59        30% — 4D        25% — 40
//        20% — 33        15% — 26        10% — 1A         5% — 0D         0% — 00
        //---------------
        mSpnMapType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(map != null){
                    switch (position) {
                        case 0:
                            map.setMapType(GoogleMap.MAP_TYPE_NORMAL); // 道路地圖。
                            break;
                        case 1:
                            map.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // 衛星空照圖
                            break;
                        case 2:
                            map.setMapType(GoogleMap.MAP_TYPE_TERRAIN); // 地形圖
                            break;
                        case 3:
                            map.setMapType(GoogleMap.MAP_TYPE_HYBRID); // 道路地圖混合空照圖
                            break;
                        case 4:
                            map.setTrafficEnabled(true); //開啟路況
                            break;
                        case 5:
                            map.setTrafficEnabled(false); //關閉路況
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }



    // -----顯示 我的位置座標圖示
    public void setMyLocationLayerEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        //----------取得定位許可-----------------------
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //----顯示我的位置ICO-------
            map.setMyLocationEnabled(((CheckBox) v).isChecked());
        } else {
            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        }
    }
    // ---檢查 Google Map 是否正確開啟
    private boolean checkReady() {
        if (map == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //-------------監聽改變控制鈕 ------------
    private CheckBox.OnCheckedChangeListener chklistener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (checkBox.isChecked()) {
                controlScroll.setVisibility(View.VISIBLE);
                // Parameters: 對應的三個常量值：VISIBLE=0 INVISIBLE=4 GONE=8
            } else {
                controlScroll.setVisibility(View.INVISIBLE);
            }
        }
    };

    // ---Control 控制項設定--------------------------------
    private boolean isChecked(int id) {
        return ((CheckBox) findViewById(id)).isChecked();
    }


    // -------- 地圖縮放 -------------------------------------------
    public void setZoomButtonsEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables zoom controls (+/- buttons in the bottom right of
        // the map).
        map.getUiSettings().setZoomControlsEnabled(((CheckBox) v).isChecked());
    }

    // ---------------設定指北針----------------------------------------------
    public void setCompassEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables the compass (icon in the top left that indicates the
        // orientation of the
        // map).
        map.getUiSettings().setCompassEnabled(((CheckBox) v).isChecked());
    }



    //----可用捲動手勢操控,用手指平移或捲動來拖曳地圖
    public void setScrollGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables scroll gestures (i.e. panning the map).
        map.getUiSettings().setScrollGesturesEnabled(((CheckBox) v).isChecked());
    }

    //----縮放手勢按兩下按一下或兩指拉大拉小----
    public void setZoomGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables zoom gestures (i.e., double tap, pinch & stretch).
        map.getUiSettings().setZoomGesturesEnabled(((CheckBox) v).isChecked());
    }
    //----傾斜手勢改變地圖的傾斜角度兩指上下拖曳來增加/減少傾斜角度----
    public void setTiltGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables tilt gestures.
        map.getUiSettings().setTiltGesturesEnabled(((CheckBox) v).isChecked());
    }
    //----旋轉手勢兩指旋轉地圖----
    public void setRotateGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables rotate gestures.
        map.getUiSettings().setRotateGesturesEnabled(((CheckBox) v).isChecked());

    }



    private void setMapLocation() {
        showloc(); //刷新所有景點
        int iSelect = mSpnLocation.getSelectedItemPosition();
        String[] sLocation = locations[iSelect][1].split(",");
        double dLat = Double.parseDouble(sLocation[0]);    // 南北緯
        double dLon = Double.parseDouble(sLocation[1]);    // 東西經
        String vtitle = locations[iSelect][0];
        //--- 設定所選位置之當地圖示 ---//
        image_des = BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN); //使用系統水滴
        VGPS = new LatLng(dLat, dLon);

        // --- 設定自訂義infowindow ---//
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        map.setOnMarkerClickListener(this);
        // map.setOnInfoWindowClickListener(this);
        // map.setOnMarkerDragListener(this);
        // --- 根據所選位置項目顯示地圖/標示文字與圖片 ---//


        map.addMarker(new MarkerOptions()
                .position(VGPS)
                .title(vtitle)
                .snippet("座標:" + dLat + "," + dLon)
                .icon(image_des));// 顯示圖標文字
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, currentZoom));
        onCameraChange(map.getCameraPosition());
        map.setOnMyLocationButtonClickListener(this);
    }
    //-----------onCameraChange--------------------------
    private void onCameraChange(CameraPosition cameraPosition) {
        tmsg.setText("目前Zoom:" + map.getCameraPosition().zoom);
    }
    //-------------------------
    private void showloc() {
        // 將所有景點位置顯示
        for (int i = 0; i < locations.length; i++) {
            String[] sLocation = locations[i][1].split(",");
            dLat = Double.parseDouble(sLocation[0]); // 南北緯
            dLon = Double.parseDouble(sLocation[1]); // 東西經
            String vtitle = locations[i][0];
            resID = 0;
            resID1 = 0;
            // --- 設定所選位置之當地圖片 ---
            // drawable 目錄下 存放 q01.png ~ q06.png  t01.png ~t07.png 超出範圍 用 t99.png & q99.png
            if (i >= 0 && i < 8) {
                String idName = "t" + String.format("%02d", i);
                String imgName = "q" + String.format("%02d", i);
                resID = getResources().getIdentifier(idName, "drawable", getPackageName());
                resID1 = getResources().getIdentifier(imgName, "drawable", getPackageName());
                image_des = BitmapDescriptorFactory.fromResource(resID);// 使用照片
            } else {
                resID = getResources().getIdentifier("t99", "drawable", getPackageName());
                resID1 = getResources().getIdentifier("q99", "drawable", getPackageName());
            }

            // --- 設定所選位置之當地圖片 ---//
            switch (icosel) {
                case 0:
                    image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN); // 使用系統水滴
                    break;
                case 1:
                    // 運用巨集
                    image_des = BitmapDescriptorFactory.fromResource(resID);// 使用照片
                    break;
            }
            //-----------------------------------------
            vtitle = vtitle + "#" + resID1; //存放圖片號碼
            //-----------------------------------------
            VGPS = new LatLng(dLat, dLon);// 更新成欲顯示的地圖座標
            // --- 根據所選位置項目顯示地圖/標示文字與圖片 ---//

            map.addMarker(new MarkerOptions()
                            .position(VGPS)
                            .alpha(0.9f)
                            .title(i + "." + vtitle)
                            .snippet("緯度:" + String.valueOf(dLat) + "\n經度:" + String.valueOf(dLon))
                            .infoWindowAnchor(1.8f, 1.5f)//0.5f水平的中間 0.95f接近marker的底部
                            .icon(image_des)
                    // .draggable(true) //設定maker 可移動
            );
            //--------------------使用自定義式窗-------------------------------------------------------
            map.setInfoWindowAdapter(new CustomInfoWindowAdapter());//外圓內方
        }
    }
    //--------------------------------------------------------------
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
//        mUiSettings = map.getUiSettings();//
//        開啟 Google Map 拖曳功能
        map.getUiSettings().setScrollGesturesEnabled(true);
//        右下角的導覽及開啟 Google Map功能
        map.getUiSettings().setMapToolbarEnabled(true);
//        左上角顯示指北針，要兩指旋轉才會出現
        map.getUiSettings().setCompassEnabled(true);
//        右下角顯示縮放按鈕的放大縮小功能
        map.getUiSettings().setZoomControlsEnabled(true);
        // --------------------------------
        map.addMarker(new MarkerOptions().position(VGPS).title("中區職訓"));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(VGPS, currentZoom));
        //----------取得定位許可-----------------------
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            //----顯示我的位置ICO-------
            map.setMyLocationEnabled(true);
        } else {
            Toast.makeText(getApplicationContext(), "GPS定位權限未允許", Toast.LENGTH_LONG).show();
        }
        //---------------------------------------------
    }
    //-------提示使用者開啟GPS定位------------------------------------
    private void u_checkgps() {
        // 取得系統服務的LocationManager物件
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 檢查是否有啟用GPS
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            // 顯示對話方塊啟用GPS
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("定位管理")
                    .setMessage("GPS目前狀態是尚未啟用.\n"
                            + "請問你是否現在就設定啟用GPS?")
                    .setPositiveButton("啟用", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // 使用Intent物件啟動設定程式來更改GPS設定
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(i);
                        }
                    })
                    .setNegativeButton("不啟用", null).create().show();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }
    // ============ GPS =================
    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(getApplicationContext(), "返回GPS目前位置", Toast.LENGTH_LONG).show();
        return true;
    }
    //*********************************************/
    //----檢查GPS 是否開啟-//
    private boolean initLocationProvider() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            provider = LocationManager.GPS_PROVIDER;
            return true;
        }
        return false;
    }
    /*** 位置變更狀態監視*/
    LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            updateWithNewLocation(location);
            tmsg.setText("目前Zoom:" + map.getCameraPosition().zoom);
        }
        @Override
        public void onProviderDisabled(String provider) {
            updateWithNewLocation(null);
        }
        @Override
        public void onProviderEnabled(String provider) {
            tmsg.setText("onProviderEnabled");
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                case LocationProvider.OUT_OF_SERVICE:
                    tmsg.setText("Out of Service");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    tmsg.setText("Temporarily Unavailable");
                    break;
                case LocationProvider.AVAILABLE:
                    tmsg.setText("Available");
                    break;
            }
        }
    };
    //-------------------
    private void nowaddress() {
// 取得上次已知的位置
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Location location = locationManager.getLastKnownLocation(provider);
            updateWithNewLocation(location);
            return;
        }
// 監聽 GPS Listener----------------------------------
// long minTime = 5000;// ms
// float minDist = 5.0f;// meter
//---網路和GPS來取得定位，因為GPS精準度比網路來的更好，所以先使用網路定位、
// 後續再用GPS定位，如果兩者皆無開啟，則跳無法定位的錯誤訊息
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location = null;
        if (!(isGPSEnabled || isNetworkEnabled))
            tmsg.setText("GPS 未開啟");
        else {
            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                tmsg.setText("使用網路GPS");
            }
            if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDist, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                tmsg.setText("使用精確GPS");
            }
        }
    }
    private void updateWithNewLocation(Location location) {
        String where = "";
        if (location != null) {
            double lng = location.getLongitude();// 經度
            double lat = location.getLatitude();// 緯度
            float speed = location.getSpeed();// 速度
            long time = location.getTime();// 時間
            String timeString = getTimeString(time);
            where = "經度: " + lng + "\n緯度: " + lat + "\n速度: " + speed + "\n時間: " + timeString + "\nProvider: "
                    + provider;
            // 標記"我的位置"
            showMarkerMe(lat, lng);
            cameraFocusOnMe(lat, lng);
            trackMe(lat, lng);// 軌跡圖
        } else {
            where = "*位置訊號消失*";
        }
        // 位置改變顯示
        txtOutput.setText(where);
    }
    //追蹤目前我的位置畫軌跡圖
    private void trackMe(double lat, double lng) {
        if (mytrace == null) {
            mytrace = new ArrayList<LatLng>();
        }
        mytrace.add(new LatLng(lat, lng));

        PolylineOptions polylineOpt = new PolylineOptions();
        for (LatLng latlng : mytrace) {
            polylineOpt.add(latlng);
        }
        polylineOpt.color(Color.RED); // 軌跡顏色
        Polyline line = map.addPolyline(polylineOpt);
        line.setWidth(15); // 軌跡寬度
        line.setPoints(mytrace);
    }

    /*** cameraFocusOnMe*/
    private void cameraFocusOnMe(double lat, double lng) {
        CameraPosition camPosition = new CameraPosition.Builder().target(new LatLng(lat, lng)).zoom(map.getCameraPosition().zoom).build();
        /* 移動地圖鏡頭 */
        map.animateCamera(CameraUpdateFactory.newCameraPosition(camPosition));
        tmsg.setText("目前Zoom:" + map.getCameraPosition().zoom);
    }

    //---顯示目前位置----------------
    private void showMarkerMe(double lat, double lng) {
        if (markerMe != null) {
            markerMe.remove();
        }
//------------------
        if (icosel != 0){
            int resID = getResources().getIdentifier("pin", "drawable", getPackageName());
            image_des = BitmapDescriptorFactory.fromResource(resID);// 使用照片
        }else {
            image_des = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE); // 使用系統水滴
        }
//-------------------------
        dLat = lat; // 南北緯
        dLon = lng; // 東西經
        String vtitle = "GPS位置:" + "#" + resID;
        String vsnippet = "座標:" + String.valueOf(dLat) + "," + String.valueOf(dLon);
        VGPS = new LatLng(lat, lng);// 更新成欲顯示的地圖座標
        MarkerOptions markerOpt = new MarkerOptions();
        markerOpt.position(new LatLng(lat, lng));
        markerOpt.title(vtitle);
        markerOpt.snippet(vsnippet);
        markerOpt.infoWindowAnchor(0.5f, 0.9f);
        markerOpt.draggable(true);
        markerOpt.icon(image_des);
        markerMe = map.addMarker(markerOpt);
//----------------------------
        locations[0][1] = lat + "," + lng;
    }
    /***********************************************
     * timeInMilliseconds
     ***********************************************/
    private String getTimeString(long timeInMilliseconds) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(timeInMilliseconds);
    }

    @Override
    public boolean onMarkerClick(final Marker marker_Animation) {
        Log.d(TAG, "onMarkerClick:" + marker_Animation.getTitle()); //
        if (!marker_Animation.getTitle().substring(0, 4).equals("Move")) {
            // 設定動畫
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final long duration = 1500;//連續時間

            final Interpolator interpolator = new BounceInterpolator();

            handler.post(new Runnable() {
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = Math.max(1 - interpolator.getInterpolation((float) elapsed / duration), 0);
                    marker_Animation.setAnchor(0.5f, 1.0f + 2 * t); //設定標的位置

                    if (t > 0.0) {
                        // Post again 16ms later.
                        handler.postDelayed(this, 16);
                    }
                }
            });
        } else {
            M1901.this.markerMe.hideInfoWindow();
        }
        return false;

    }

    //----生命週期-----
    @Override
    protected void onStart() {
        super.onStart();
        if (initLocationProvider()) {
           // checkRequiredPermission(this);
           nowaddress();
        } else {
            txtOutput.setText("GPS未開啟,請先開啟定位！");
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);
    }
    //====================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.m1901, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.item1:
                map.clear();  //歸零
                if (icosel < 1) {
                    icosel = 1; //用照片顯示
                    showloc();
                } else
                    icosel = 0; //用水滴顯示
                showloc();
                break;
            case R.id.item3:
                //----
                LatLng VGPS_3D = new LatLng(41.8881544, 12.4944637);//羅馬競技場
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(VGPS_3D)//目標
                        .zoom(18)       //縮放 1：世界 5：地塊/大陸 10：城市 15：街道 20：建築物
                        .bearing(0)   //0:北 45:西北 90:西 135:西南 180:南 225:東南 270:東 315:東北
                        .tilt(45)       //傾斜度（檢視角度）0-90 0:正上方(0~15 最佳)
                        .build();       // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                map.setBuildingsEnabled(true);
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE); // 衛星空照圖
                //----
                break;

                case R.id.item4:
                //----
                //LatLng VGPS_3D = new LatLng(34.687404, 135.525763);//大阪城
                CameraPosition cameraPosition01 = new CameraPosition.Builder()
                        .target(VGPS)//目標
                        .zoom(18)       //縮放 1：世界 5：地塊/大陸 10：城市 15：街道 20：建築物
                        .bearing(0)   //0:北 45:西北 90:西 135:西南 180:南 225:東南 270:東 315:東北
                        .tilt(45)       //傾斜度（檢視角度）0-90 0:正上方(0~15 最佳)
                        .build();       // Creates a CameraPosition from the builder
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition01));
                map.setBuildingsEnabled(true);
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL); // 衛星空照圖
                //----
                break;

            case R.id.item5:
                Intent it = new Intent();
                it.setClass(this,M1901select.class);
                startActivity(it);
                break;

            case R.id.action_settings:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);

    }
    //-------------------subclass-----------------------------
///==========自定義的infowindow class==========//

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        @Override
        public View getInfoWindow(Marker marker) {
            // 依指定layout檔，建立地標訊息視窗View物件
            // --------------------------------------------------------------------------------------
            // 單一框
            // View infoWindow=
            // getLayoutInflater().inflate(R.layout.custom_info_window,
            // null);
            // 有指示的外框
            View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_content, null);
            infoWindow.setAlpha(0.5f);
            // ----------------------------------------------
            // 顯示地標title
            TextView title = ((TextView) infoWindow.findViewById(R.id.title));
            String[] ss = marker.getTitle().split("#");
            title.setText(ss[0]);
            // 顯示地標snippet
            TextView snippet = (TextView) infoWindow.findViewById(R.id.snippet);
            snippet.setText(marker.getSnippet());
            // 顯示圖片
            ImageView imageview = ((ImageView) infoWindow.findViewById(R.id.content_ico));
            imageview.setImageResource(Integer.parseInt(ss[1]));

            return infoWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            Toast.makeText(getApplicationContext(), "getInfoContents", Toast.LENGTH_LONG).show();
            return null;
        }
    }



//-----------end class---------------------------------------------
}

