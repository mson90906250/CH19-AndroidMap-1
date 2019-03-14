package tw.tcnr01.m1901;


import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;


public class GPSService extends Service implements LocationListener {

    private LocationManager manager;
    private boolean isInArea;
    private double latitude, longitude;
    private static float distance;

    @Override
    public void onCreate() {
        // 取得LocationManager物件
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            // 請求定時更新
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1, this);
        } catch (SecurityException e) {
            Log.e("M1906", "GPS權限失敗..." + e.getMessage());
        }
        isInArea = false;
    }

    @Override
    public void onDestroy() {
        try {
            // 移除更新
            manager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e("M1906", "GPS權限失敗..." + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 取得傳遞的經緯度座標
        latitude = (double) intent.getFloatExtra("LATITUDE:", 24.172127f);
        longitude = (double) intent.getFloatExtra("LONGITUDE:", 120.610313f);
        TimerTask action = new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent("FilterString");
                String t_distance = Float.toString(distance);
                intent.putExtra("Distance", t_distance);
                sendBroadcast(intent);
            }
        };
        Timer timer = new Timer();
        timer.schedule(action, 1000, 1000);

        Log.d("GPServer", "lat/lng:" + latitude + ":" + longitude);
        return super.onStartCommand(intent, flags, startId);
    }

    public static String t_distance() {
        String dis = Float.toString(distance);
        return dis;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) return;
        // 建立目標的經緯度座標
        Location dest = new Location(location);
        dest.setLatitude(latitude);
        dest.setLongitude(longitude);
        // 計算與經緯度座標的距離
        distance = location.distanceTo(dest);
        Log.d("M1906", "距離: " + distance);
//        Intent it = new Intent(this,M1906.class);
//        String aa = Float.toString(distance);
//        it.putExtra("aa",aa);
//        startService(it);
//        startActivity(it);
        // 距離是否小於100公尺
        if (distance < 50.0) {
            Intent intent = new Intent("android.broadcast.LOCATION");
            sendBroadcast(intent);
            isInArea = true;
        } else {
            isInArea = false;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
