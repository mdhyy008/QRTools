package com.dabai.qrtools;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.app.*;
import android.os.*;
import android.view.*;
import android.content.*;
import android.widget.*;
import android.net.*;

import java.io.*;
import java.util.List;


public class PlugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plug);

        //是否阻止截图
        if (Control.is_sc) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }


        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        try {
            c("qr.apk", "/sdcard/qr.apk");
        } catch (IOException e) {
        }

        handler.post(task);//立即调用


    }

    public void c(String assetsFileName, String OutFileName) throws IOException {
        File f = new File(OutFileName);
        if (f.exists())
            f.delete();
        f = new File(OutFileName);
        f.createNewFile();
        InputStream I = getAssets().open(assetsFileName);
        OutputStream O = new FileOutputStream(OutFileName);
        byte[] b = new byte[1024];
        int l = I.read(b);
        while (l > 0) {
            O.write(b, 0, l);
            l = I.read(b);
        }
        O.flush();
        I.close();
        O.close();
    }

    public void goto1(View v) {
        //调用本程序
        Intent intent = new Intent(this,WebActivity.class);
        intent.putExtra("link", "https://www.coolapk.com/apk/mark.qrcode");
        startActivity(intent);

    }

    public void goto2(View v) {
//root

        Toast.makeText(this, "正在申请权限并安装", Toast.LENGTH_SHORT).show();

        new Thread(new Runnable() {
            @Override
            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setTitle("正在安装中...");
                    }
                });

                new shell().execCommand(new String[]{"pm install /sdcard/qr.apk"}, true);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isAvilible(getApplicationContext(), "mark.qrcode")) {
                            new AlertDialog.Builder(PlugActivity.this).setTitle("提示").setMessage("插件安装成功").setPositiveButton("确定", null).show();
                        } else {
                            new AlertDialog.Builder(PlugActivity.this).setTitle("提示").setMessage("插件安装失败").setPositiveButton("只好两步安装了", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    new AlertDialog.Builder(PlugActivity.this).setTitle("提示").setMessage("去SD卡目录，找到qr.apk安装").setPositiveButton("手动安装", null).show();

                                }
                            }).show();
                        }
                    }
                });
            }
        }).start();

    }





    public void goto3(View v) {

        //调用本程序
        Intent intent = new Intent(this,WebActivity.class);
        intent.putExtra("link", "https://www.lanzous.com/i4goa6f");
        startActivity(intent);

    }


    private boolean isAvilible(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        // 获取所有已安装程序的包信息
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (int i = 0; i < pinfo.size(); i++) {
            if (pinfo.get(i).packageName.equalsIgnoreCase(packageName))
                return true;
        }
        return false;
    }


private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        public void run() {

            handler.postDelayed(this,1000);//设置循环时间，此处是5秒
            if (isAvilible(getApplicationContext(),"mark.qrcode")){
               setTitle("QRT插件 - 已安装成功");
               new File("/sdcard/qr.apk").delete();
               getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

        }
    };



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


}
