package tw.tcnr01.m1901;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double[] Lats,Lngs;
    private int max_index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.maps);

        // 取得Intent物件附加的陣列
        Lats = getIntent().getDoubleArrayExtra("GPSLATITUDE");
        Lngs = getIntent().getDoubleArrayExtra("GPSLONGITUDE");

        max_index = getIntent().getIntExtra("MAX_INDEX", 10);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap != null) {
            setUpMap(Lats, Lngs, max_index);
        }

    }

    // 顯示行蹤的標記
    private void setUpMap(double[] Lats, double[] Lngs, int max_index) {
        LatLng first_pos = new LatLng(Lats[0], Lngs[0]); // 建立第1個LatLng物件的座標
        for (int i = 0; i < max_index; i++) {
            // 新增Marker標記
            mMap.addMarker(new MarkerOptions().position(new LatLng(Lats[i], Lngs[i]))
                    .title("第"+i+"筆 "+Lats[i] + "/" + Lngs[i]));
        }
        // 顯示目前位址的附近地圖
        CameraPosition cp = new CameraPosition.Builder()
                .target(first_pos)
                .zoom(16)//1=世界 5=板塊/大陸 10=城市 15=街道 20=建築物
                .bearing(0)//指北針
                .tilt(25)//傾斜度
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp));
    }

}
