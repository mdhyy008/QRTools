package com.dabai.qrtools;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {


    String wificonfig;

    ConstraintLayout cons;

    private Context context;
    TextView tips;
    //特效 duang
    boolean add = true;
    float alpha = 0;
    private String TAG = "dabai";
    private int REQUEST_CODE_SCAN = 100;
    private boolean clip_monitor, isChrome, screenshot_monitor;
    private Intent screenintent, clipintent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();


        //是否阻止截图
        if (Control.is_sc) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        }


        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        /**
         * 申请权限
         */
        int checkResult = getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //if(!=允许),抛出异常
        if (checkResult != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1); // 动态申请读取权限
            }
        } else {
        }

        /**
         * 申请权限
         */
        int checkResult1 = getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //if(!=允许),抛出异常
        if (checkResult1 != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1); // 动态申请读取权限
            }
        } else {
        }
        cons = findViewById(R.id.cons);


        init();


    }


    /**
     * 初始化各种东西
     */
    private void init() {

        SharedPreferences sp = this.getSharedPreferences("com.dabai.qrtools_preferences", 0);

        //获取 设置 值  来设置是否启动监听

        clip_monitor = sp.getBoolean("clip_monitor", false);
        screenshot_monitor = sp.getBoolean("screenshot_monitor", false);

        //Log.d(TAG, "剪切板服务: " + clip_monitor);
        //Log.d(TAG, "截图服务: " + screenshot_monitor);


        //初始化剪切板监听
        clipintent = new Intent(this, ClipService.class);

        if (clip_monitor) {
            // Android 8.0使用startForegroundService在前台启动新服务
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(clipintent);
            } else {
                context.startService(clipintent);
            }
        }


        //初始化截图监听
        screenintent = new Intent(this, ScreenshotMonitorService.class);

        if (screenshot_monitor) {
            // Android 8.0使用startForegroundService在前台启动新服务
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(screenintent);
            } else {
                context.startService(screenintent);
            }
        }


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.setting:
                startActivity(new Intent(this, SettingActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void QR_create(View view) {
        startActivity(new Intent(this, TextQRActivity.class));
    }

    public void QR_scan(View view) {

        startActivity(new Intent(this, ScanToolActivity.class));

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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        if (requestCode == 6) {
            Uri uri = ContactsContract.Contacts.CONTENT_URI;
            Intent intent = new Intent(Intent.ACTION_PICK, uri);
            startActivityForResult(intent, 5);
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);



        // 扫描二维码/条码回传
        if (requestCode == 5 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String[] contacts = getPhoneContacts(uri);

                Intent intent = new Intent(this, VcfResultActivity.class);
                intent.putExtra("name", contacts[0]);
                intent.putExtra("phoneNumber", contacts[1]);
                startActivity(intent);
            }
        }
    }


    private String[] getPhoneContacts(Uri uri) {
        String[] contact = new String[2];
        //得到ContentResolver对象
        ContentResolver cr = getContentResolver();
        //取得电话本中开始一项的光标
        Cursor cursor = cr.query(uri, null, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            //取得联系人姓名
            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            contact[0] = cursor.getString(nameFieldColumnIndex);
            //取得电话号码
            String ContactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            Cursor phone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + ContactId, null, null);
            if (phone != null) {
                phone.moveToFirst();
                contact[1] = phone.getString(phone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            }
            phone.close();
            cursor.close();
        } else {
            return null;
        }
        return contact;
    }


    public void vcf_create(View view) {

        /**
         * 申请权限
         */
        int checkResult = getApplicationContext().checkCallingOrSelfPermission(Manifest.permission.READ_CONTACTS);
        //if(!=允许),抛出异常
        if (checkResult != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 6); // 动态申请读取权限
            }
        } else {
            Uri uri = ContactsContract.Contacts.CONTENT_URI;
            Intent intent = new Intent(Intent.ACTION_PICK, uri);
            startActivityForResult(intent, 5);
        }
    }


    //退出时的时间
    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (isServiceRunning(getApplicationContext(), "com.dabai.qrtools.ScreenshotMonitorService") || isServiceRunning(getApplicationContext(), "com.dabai.qrtools.ClipService")) {
                new AlertDialog.Builder(this).setTitle("警告").setMessage("你还有监听服务在后台运行,某些手机退出软件会造成服务停止,不推荐强制退出")

                        .setPositiveButton("最小化", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent();
                                // 为Intent设置Action、Category属性
                                intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
                                intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
                                startActivity(intent);


                            }
                        }).show();
            } else {

                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(context, "再按一次退出", Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    finish();
                }


            }

            return false;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 判断服务是否开启
     *
     * @return
     */
    public static boolean isServiceRunning(Context context, String ServiceName) {
        if (("").equals(ServiceName) || ServiceName == null)
            return false;
        ActivityManager myManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        ArrayList<ActivityManager.RunningServiceInfo> runningService = (ArrayList<ActivityManager.RunningServiceInfo>) myManager
                .getRunningServices(30);
        for (int i = 0; i < runningService.size(); i++) {
            if (runningService.get(i).service.getClassName().toString()
                    .equals(ServiceName)) {
                return true;
            }
        }
        return false;
    }

    public void more_create(View view) {
        /**
         * 批量入口
         */

        startActivity(new Intent(this, MoreActivity.class));
    }

    public void gif_create(View view) {
        startActivity(new Intent(this, GifActivity.class));
    }

    boolean isroot;

    public void wifi_config(View view) {
        suthread();
    }


    public void suthread() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                isroot = isRoot();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (!isroot) {
                            AlertDialog ad = new AlertDialog.Builder(MainActivity.this).setTitle("权限提示")
                                    .setMessage("ROOT权限获取失败,点击重新获取。")
                                    .setPositiveButton("尝试获取", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            try {
                                                Runtime.getRuntime().exec("su");
                                            } catch (IOException e) {

                                            }
                                            suthread();

                                        }
                                    }).show();

                            Window window = ad.getWindow();//对话框窗口
                            window.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
                            window.setWindowAnimations(R.style.dialog_style_bottom);//添加动画


                        } else {


                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        new shell().execCommand(new String[]{"cp /data/misc/wifi/WifiConfigStore.xml /sdcard/WifiConfigStore.xml"}, true);
                                        try {
                                            wificonfig = new FileUtils().readText("/sdcard/WifiConfigStore.xml");
                                        } catch (IOException e) {

                                        }
                                    } else {
                                        new shell().execCommand(new String[]{"cp /data/misc/wifi/wpa_supplicant.conf /sdcard/wpa_supplicant.conf"}, true);
                                        try {
                                            wificonfig = new FileUtils().readText("/sdcard/wpa_supplicant.conf");
                                        } catch (IOException e) {

                                        }
                                    }
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            showWifi();
                                        }
                                    });
                                }
                            }).start();
                        }
                    }
                });
            }
        }).start();


    }


    //显示wifi选择弹窗
    private void showWifi() {

        /**
         * 这里需要处理数据
         * 解析下WiFi配置信息
         * 得到 ssid 和 password
         */


        AlertDialog ad = new AlertDialog.Builder(MainActivity.this)
                .setTitle("WIFI选择")
                .setMessage(wificonfig)
                .show();

        Window window = ad.getWindow();//对话框窗口
        window.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
        window.setWindowAnimations(R.style.dialog_style_bottom);//添加动画

    }


    private boolean isRoot() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            process.getOutputStream().write("exit\n".getBytes());
            process.getOutputStream().flush();

            int i = process.waitFor();
            if (0 == i) {
                process = Runtime.getRuntime().exec("su");
                return true;
            }

        } catch (Exception e) {
            return false;
        }
        return false;

    }

    private String exec(String command) {
        try {
            java.lang.Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));
            int read;
            char[] buffer = new char[4096];
            StringBuffer output = new StringBuffer();
            while ((read = reader.read(buffer)) > 0) {
                output.append(buffer, 0, read);
            }
            reader.close();
            process.waitFor();
            return output.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}