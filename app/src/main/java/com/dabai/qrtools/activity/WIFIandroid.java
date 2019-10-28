package com.dabai.qrtools.activity;


import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dabai.qrtools.R;
import com.dabai.qrtools.TextQRActivity;
import com.dabai.qrtools.utils.WifiInfo;
import com.dabai.qrtools.utils.WifiManage;

import java.util.List;

public class WIFIandroid extends AppCompatActivity {

    private WifiManage wifiManage;
    private String TAG = "dabaizzz";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifiandroid);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        wifiManage = new WifiManage();


        try {
            Init();
        } catch (Exception e) {
            Log.d(TAG, "onCreate: " + e);
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                // 处理返回逻辑
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void Init() throws Exception {
        final List<WifiInfo> wifiInfos = WifiManage.Read();
        ListView wifiInfosView = (ListView) findViewById(R.id.WifiInfosView);

        wifiInfosView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private String text;

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                WifiInfo wifiInfo = wifiInfos.get(i);

                String ssid = wifiInfo.getSsid();
                String pass = wifiInfo.getPassword();

                if (pass.equals("无密码")) {
                    text = "WIFI:T:;P:;S:" + ssid + ";";
                } else {
                    text = "WIFI:T:WPA;P:" + pass + ";S:" + ssid + ";";
                }

                ToRes(text);

            }
        });


        wifiInfosView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {



                ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData mclipData = ClipData.newPlainText("Label","WiFi名称 : " + wifiInfos.get(i).ssid + "\n密码 : " + wifiInfos.get(i).password );
                clipboardManager.setPrimaryClip(mclipData);

                Toast.makeText(WIFIandroid.this, "复制完成", Toast.LENGTH_SHORT).show();
                return true;
            }
        });



        WifiAdapter ad = new WifiAdapter(wifiInfos, this);
        wifiInfosView.setAdapter(ad);
    }


    void ToRes(String text) {
        Intent resultIntent = new Intent(this, TextQRActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra("download", text);
        startActivity(resultIntent);

    }


    public class WifiAdapter extends BaseAdapter {

        List<WifiInfo> wifiInfos = null;
        Context con;

        public WifiAdapter(List<WifiInfo> wifiInfos, Context con) {
            this.wifiInfos = wifiInfos;
            this.con = con;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return wifiInfos.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return wifiInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            convertView = LayoutInflater.from(con).inflate(android.R.layout.simple_list_item_1, null);
            TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
            tv.setText("WiFi名称 : " + wifiInfos.get(position).ssid + "\n密码 : " + wifiInfos.get(position).password);
            return convertView;
        }

    }


}


