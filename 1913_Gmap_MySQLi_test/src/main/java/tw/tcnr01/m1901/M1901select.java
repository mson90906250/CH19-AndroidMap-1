package tw.tcnr01.m1901;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;


public class M1901select extends AppCompatActivity {
    private static final String TAG = "tcnr01=>";
    private Button button_get_record;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selemysql);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
        setupViewComponent();
        setListeners();
    }

    private void setupViewComponent() {
        button_get_record = (Button) findViewById(R.id.btn1);
    }

    // Button元件的事件處理
    public void btn1_on(View view) {
        TableLayout user_list = (TableLayout) findViewById(R.id.user_list);
        user_list.setStretchAllColumns(true);
        TableLayout.LayoutParams row_layout = new
                TableLayout.LayoutParams(ViewGroup.LayoutParams.
                WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        TableRow.LayoutParams view_layout = new
                TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        try {
            String result = DBConnector.executeQuery("SELECT * FROM member");
/*
SQL 結果有多筆資料時使用JSONArray
只有一筆資料時直接建立JSONObject物件
     JSONObject jsonData = new JSONObject(result);
*/
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonData = jsonArray.getJSONObject(i);
                TableRow tr = new TableRow(M1901select.this);
                tr.setLayoutParams(row_layout);
//                tr.setGravity(Gravity.CENTER_HORIZONTAL);
                tr.setGravity(Gravity.LEFT);

                TextView user_name = new TextView(M1901select.this);
                user_name.setText(jsonData.getString("name"));//資料欄位名稱
                user_name.setLayoutParams(view_layout);

                TextView user_grp = new TextView(M1901select.this);
                user_grp.setText("-"+jsonData.getString("grp"));
                user_grp.setLayoutParams(view_layout);

                TextView user_address = new TextView(M1901select.this);
                user_address.setText("-"+jsonData.getString("address"));
                user_address.setLayoutParams(view_layout);

                tr.addView(user_name);
                tr.addView(user_grp);
                tr.addView(user_address);

                user_list.addView(tr);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.main_finish) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setListeners() {
    }


}

