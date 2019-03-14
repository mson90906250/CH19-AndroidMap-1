package tw.tcnr01.m1901;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class M1904 extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private static final String TAG = "tcnr01=>";
    private final int MAX_RECORDS = 10;
    private LocationManager manager;
    private Location currentLocation;
    private int index = 0, count = 0;
    private double[] Lats = new double[MAX_RECORDS];
    private double[] Lngs = new double[MAX_RECORDS];

    private String[] permissionsArray = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    private List<String> permissionsList = new ArrayList<>();
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private Button button;
    private TextView output;
    private String bestgps;
    private long minTime = 5000;
    private float minDistance = 15;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1904);
        checkRequiredPermission(this);
        // 取得系統服務的LocationManager物件
        manager = (LocationManager)getSystemService(LOCATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        u_checkgps();
        setupViewComponent();
    }

    //-----------------------------
    private void setupViewComponent() {

    }

    private void checkRequiredPermission(Activity activity) {
        for (String permission : permissionsArray) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(permission);
            }
        }
        if(permissionsList.size()!=0){
            ActivityCompat.requestPermissions(activity,permissionsList.toArray(new
                    String[permissionsList.size()]),REQUEST_CODE_ASK_PERMISSIONS);
        }

    }

    // 建立定位服務的傾聽者物件
    private LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            currentLocation = location;
            updatePosition();
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    // 取得定位資訊
    public String getLocationInfo(Location location) {
        boolean isSave = true;
        double lat, lng;
        lat = location.getLatitude();
        lng = location.getLongitude();
        StringBuffer str = new StringBuffer();
        str.append("定位提供者(Provider): "+location.getProvider());
        str.append("\n緯度(Latitude): " + Double.toString(lat));
        str.append("\n經度(Longitude): " + Double.toString(lng));
        if (count >= 1) { // 檢查是否需要儲存座標
            // 建立目地GPS座標的Location物件
            Location dest = new Location(location);
            // 前一個座標的陣列索引
            int preIndex = index - 1;
            // 檢查前一座標是否是陣列最後一個元素
            if (preIndex < 0 ) preIndex = Lats.length - 1;
            // 指定目的Location物件的座標
            dest.setLatitude(Lats[preIndex]);
            dest.setLongitude(Lngs[preIndex]);
            Log.d("M1907", count + " index/preIndex: " + index + "/" + preIndex);
            Log.d("M1907", "dlat: " + Lats[preIndex]);
            Log.d("M1907", "dlng: " + Lngs[preIndex]);
            // 計算與目地座標的距離
            float distance = location.distanceTo(dest);
            Toast.makeText(this, "距離: " + distance + "公尺",
                    Toast.LENGTH_SHORT).show();
            Log.d("M1907", "distance: " + distance);
            // 檢查距離是否小於20公尺, 小於不用存
            if (distance < 20.0) isSave = false;
        }
        if (isSave) { // 記錄座標
            Lats[index] = lat;
            Lngs[index] = lng;
            count++;
            if (count >= MAX_RECORDS) count = MAX_RECORDS;
            index++; // 陣列索引加一
            // 如果索引大於最大索引, 重設為0
            if (index >= MAX_RECORDS) index = 0;
        }
        return str.toString();
    }


    // 啟動Google地圖
    public void button_Click(View view) {
        // 取得經緯度座標
        float latitude = (float) currentLocation.getLatitude();
        float longitude = (float) currentLocation.getLongitude();
        // 建立URI字串
        String uri = String.format("geo:%f,%f?z=18", latitude, longitude);
        // 建立Intent物件
        Intent geoMap = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(geoMap);  // 啟動活動
    }

    // 在MapView元件顯示個人行蹤
    public void button2_Click(View view) {
        // 建立Intent物件
        Intent mapView = new Intent(this, MapsActivity.class);
        mapView.putExtra("GPSLATITUDE", Lats);
        mapView.putExtra("GPSLONGITUDE", Lngs);
        mapView.putExtra("MAX_INDEX", count);
        startActivity(mapView);  // 啟動活動
    }


    //------------------------------------------------
    private void u_checkgps() {//檢查gps有無開啓
        // 取得系統服務的LocationManager物件
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // 檢查是否有啟用GPS
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
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
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    //------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for(int i=0;i<permissions.length;i++){
                    if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                        Toast.makeText(getApplicationContext(),permissions[i]+"權限申請成功!",Toast.LENGTH_LONG).show();
                    }else{
                        Toast.makeText(getApplicationContext(),"權限被拒絕："+permissions[i],Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {//每resume一次,就重新取得位置
        super.onResume();
        // 取得最佳的定位提供者
        Criteria criteria = new Criteria();
        bestgps = manager.getBestProvider(criteria, true);

        try {
            if (bestgps != null) { // 取得快取的最後位置,如果有的話
                currentLocation = manager.getLastKnownLocation(bestgps);
                manager.requestLocationUpdates(bestgps, minTime, minDistance, listener);
            } else { // 取得快取的最後位置,如果有的話
                currentLocation = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        minTime, minDistance, listener);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "GPS權限失敗..." + e.getMessage());
        }
        updatePosition(); // 更新位置
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            manager.removeUpdates(listener);
        } catch (SecurityException e) {
            Log.e(TAG, "GPS權限失敗..." + e.getMessage());
        }
    }

    // 更新現在的位置
    private void updatePosition() {
        TextView output, list;
        String str = "最近個人行蹤的座標清單:\n";
        output = (TextView) findViewById(R.id.lblOutput);
        list = (TextView) findViewById(R.id.lblList);
        if(currentLocation == null) {
            output.setText("取得定位資訊中...");
        } else {
            // 顯示目前經緯度座標資訊
            output.setText(getLocationInfo(currentLocation));
            // 顯示個人行蹤的座標清單
            for (int i = 0; i < MAX_RECORDS; i++) {
                if (Lats[i] != 0.0)
                    str += Lats[i] + "/" + Lngs[i] +"\n";
            }
            list.setText(str);
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
