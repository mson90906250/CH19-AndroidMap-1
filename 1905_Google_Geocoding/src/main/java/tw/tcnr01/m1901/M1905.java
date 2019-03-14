package tw.tcnr01.m1901;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class M1905 extends AppCompatActivity {
    private String[] permissionsArray = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.INTERNET,
    };
    private List<String> permissionsList = new ArrayList<>();
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private final int maxResult = 3;
    private String addressList[] = new String[maxResult];
    private ArrayAdapter<String> adapter;
    private TextView output;
    private Geocoder geocoder;
    private EditText lat, lon, address;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.m1905);
        checkRequiredPermission(this);
        setupViewComponent();
    }

    private void setupViewComponent() {
        // 取得經緯度EditText元件
        lat = (EditText) findViewById(R.id.txtLat);
        lon = (EditText) findViewById(R.id.txtLong);
        // 取得座標輸出元件
        output = (TextView) findViewById(R.id.lblOutput);
        // 取得住址元件
        address = (EditText) findViewById(R.id.txtAddress);
        // 建立Geocoder物件
        geocoder = new Geocoder(this, Locale.TAIWAN);

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

    // Button元件的事件處理 - 將經緯度座標轉換成地址
    public void button_Click(View view){
        // 取得經緯度座標
        float latitude = Float.parseFloat(lat.getText().toString());
        float longitude = Float.parseFloat(lon.getText().toString());
        try {
            // 取得地址清單的List物件
            List<Address> listAddress = geocoder.getFromLocation(latitude, longitude, maxResult);
            // 是否有取得地址
            if (listAddress != null){
                Spinner spinner=(Spinner)findViewById(R.id.spinAddress);
                // 指定陣列的初值
                for (int i=0; i<maxResult; i++) addressList[i]="N/A";//清空Spinner
                int index=0;
                for (int i=0; i<maxResult; i++){
                    Address findAddress=listAddress.get(i);
                    // 建立StringBuilder物件
                    StringBuilder strAddress=new StringBuilder();
                    // 取得地址的內容
                    for (int j=0; findAddress.getAddressLine(j)!=null; j++){
                        String str=findAddress.getAddressLine(0);
                        strAddress.append(str).append("\n");
                    }
                    if (strAddress.length()>0){// 指定陣列元素值
                        addressList[index++]=strAddress.toString();
                    }
                }
                // 建立結合器物件
                adapter=new ArrayAdapter<String>(this,
                        android.R.layout.simple_spinner_item, addressList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
            }else{
                output.setText("查無地址!");
            }

        }catch (Exception ex){
            output.setText("錯誤:"+ex.toString());
        }

    }

    // 將地址轉換成經緯度座標
    public void button2_Click(View view) {

        String addressName = address.getText().toString();
        try {
            // 取得經緯度座標清單的List物件
            List<Address> listGPSAddress = geocoder.getFromLocationName(addressName, 1);//抓經緯度轉成地名
            // 有找到經緯度座標
            if (listGPSAddress != null) {
                double latitude = listGPSAddress.get(0).getLatitude();
                double longitude = listGPSAddress.get(0).getLongitude();
                output.setText("緯度: " + latitude +
                        "\n經度: " + longitude);
                lat.setText(String.valueOf(latitude)); // 指定值
                lon.setText(String.valueOf(longitude));
            }
        } catch (Exception ex) {
            output.setText("錯誤:" + ex.toString());
        }
    }


    // 啟動Google地圖
    public void button3_Click(View view) {
        // 取得經緯度座標
        float latitude = Float.parseFloat(lat.getText().toString());
        float longitude = Float.parseFloat(lon.getText().toString());
        // 建立URI字串
        String uri = String.format("geo:%f,%f?z=18", latitude, longitude);
        // 建立Intent物件
        Intent geoMap = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(geoMap);  // 啟動活動
    }

}
