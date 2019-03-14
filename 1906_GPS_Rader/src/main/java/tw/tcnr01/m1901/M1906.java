package tw.tcnr01.m1901;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class M1906 extends AppCompatActivity {
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private EditText lat, lon;
    private TextView output, t004;
    private LocationManager manager;
    private Handler handler = new Handler();
    String b_lat="24.172127";
    String b_lon="120.610313";

    private String[] permissionsArray = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
    };
    private List<String> permissionsList = new ArrayList<>();
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1906);
        checkRequiredPermission(this);
        setupViewComponent();
    }

    private void setupViewComponent() {
        // 取得EditText元件
        lat = (EditText) findViewById(R.id.txtLat);
        lon = (EditText) findViewById(R.id.txtLong);
        lat.setText(b_lat);
        lon.setText(b_lon);
        // 取得TextView元件
        output = (TextView) findViewById(R.id.lblOutput);
        t004 = (TextView) findViewById(R.id.t004);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }

    public void btnStart_Click(View view) {//開始警告
        // 取得經緯度座標
        float latitude = Float.parseFloat(lat.getText().toString());
        float longitude = Float.parseFloat(lon.getText().toString());
        // 建立Intent物件
        Intent intent = new Intent(this, GPSService.class);
        // 加上傳遞資料
        intent.putExtra("LATITUDE", latitude);
        intent.putExtra("LONGITUDE", longitude);
        startService(intent); // 啟動服務
        output.setText("服務啟動中...");


        final String Action = "FilterString";
        IntentFilter filter = new IntentFilter(Action);
        // 將 BroadcastReceiver 在 Activity 掛起來。
        registerReceiver(receiver, filter);
    }

    BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String mdistance = intent.getStringExtra("Distance");
            if (mdistance != null) {
                t004.setText("距離:" + mdistance + "公尺");
            } else {
                t004.setText("距離:尚未抓取");
            }
        }
    };

    public void btnStop_Click(View view) {//停止警告
        Intent intent = new Intent(this, GPSService.class);
        stopService(intent); // 停止服務
        handler.removeCallbacks(updatime);//停止序
        output.setText("服務停止中...");
    }
    public void btnFinish_Click(View view) {//結束
//        finish();
        // 取得經緯度座標
        float latitude = Float.parseFloat(lat.getText().toString());
        float longitude = Float.parseFloat(lon.getText().toString());
        // 建立Intent物件
        Intent intent = new Intent(this, GPSService.class);
        // 加上傳遞資料
        intent.putExtra("LATITUDE", latitude);
        intent.putExtra("LONGITUDE", longitude);
        startService(intent); // 啟動服務
        output.setText("服務啟動中...");
        handler.postDelayed(updatime, 1000);
    }
    private Runnable updatime = new Runnable() {
        @Override
        public void run() {
            String dis = GPSService.t_distance();
            t004.setText("距離:" + dis + "公尺");
            handler.postDelayed(this, 1000);
        }
    };



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
}
