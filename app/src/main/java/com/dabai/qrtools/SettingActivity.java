package com.dabai.qrtools;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static android.app.Notification.EXTRA_CHANNEL_ID;
import static android.provider.Settings.EXTRA_APP_PACKAGE;

@SuppressWarnings("deprecation")
public class SettingActivity extends PreferenceActivity {

    AlertDialog dia_pro;

    private Context context;
    Intent clipintent, scintent;
    String TAG = "dabaizzz";
    private ArrayList<String> models;

    //网络组件
    private StringBuffer subtext;
    BufferedReader br;
    private char[] data;
    AlertDialog adddd;
    private List<String> photos_all;

    View dia_pro_view;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pre_setting);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        context = getApplicationContext();
        //version init  版本号初始化
        final Preference ver = getPreferenceManager().findPreference("other_version");

        clipintent = new Intent(this, ClipService.class);
        scintent = new Intent(this, ScreenshotMonitorService.class);


        //三个复选框
        final Preference customtabs = getPreferenceManager().findPreference("isChrome");
        final Preference clip = getPreferenceManager().findPreference("clip_monitor");
        final Preference sc = getPreferenceManager().findPreference("screenshot_monitor");
        final Preference installshort = getPreferenceManager().findPreference("program_shortcuts");
        final Preference appinfo = getPreferenceManager().findPreference("program_info");
        appinfo.setSummary("Android版本:"+Build.VERSION.RELEASE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P + 1) {
            clip.setEnabled(false);
            clip.setSummary("此Android版本不支持");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P || Build.MODEL.toLowerCase().contains("vivo") || Build.MODEL.toLowerCase().contains("oppo")) {
            sc.setEnabled(false);
            sc.setSummary("此Android版本或定制系统不支持");
        }
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            clip.setEnabled(false);
        }


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            installshort.setEnabled(false);
            installshort.setSummary("此Android版本不支持");
        }


        //change preference version name;
        ver.setSummary(new DabaiUtils().getVersionName(getApplicationContext()));


        /**
         * 初始化 drop进度弹窗
         */


        dia_pro_view = LayoutInflater.from(SettingActivity.this).inflate(R.layout.dialog_pro, null);

        dia_pro = new AlertDialog.Builder(SettingActivity.this)
                .setCancelable(false)
                .setView(dia_pro_view)
                .setPositiveButton("终止进程", null).create();

        dia_pro.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                final Button btnPositive = dia_pro.getButton(DialogInterface.BUTTON_POSITIVE);
                btnPositive.setText("终止进程");
                btnPositive.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        is_drop = false;
                        btnPositive.setText("关闭");

                        btnPositive.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                dia_pro.dismiss();
                            }
                        });
                    }
                });
            }
        });


    }

    AlertDialog adpro;

    @Override
    protected void onResume() {
        super.onResume();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!NotificationManagerCompat.from(context).areNotificationsEnabled() || !Settings.canDrawOverlays(this)) {

                adpro = new AlertDialog.Builder(this).setCancelable(false).setTitle("需要申请以下权限").setPositiveButton("去授权", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (!Settings.canDrawOverlays(context)) {

                            try {
                                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                        Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            } catch (Exception e) {
                                toAppInfo();
                            }
                            return;
                        }

                        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                            try {
                                // 根据isOpened结果，判断是否需要提醒用户跳转AppInfo页面，去打开App通知权限
                                Intent intent = new Intent();
                                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);

                                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
                                    //这种方案适用于 API 26, 即8.0（含8.0）以上可以用
                                    intent.putExtra(EXTRA_APP_PACKAGE, getPackageName());
                                    intent.putExtra(EXTRA_CHANNEL_ID, getApplicationInfo().uid);
                                }

                                if (Build.VERSION.SDK_INT > 21 && Build.VERSION.SDK_INT < 25) {
                                    //这种方案适用于 API21——25，即 5.0——7.1 之间的版本可以使用
                                    intent.putExtra("app_package", getPackageName());
                                    intent.putExtra("app_uid", getApplicationInfo().uid);
                                }

                                startActivity(intent);
                            } catch (Exception e) {
                                toAppInfo();
                            }
                        }

                    }
                }).setNeutralButton("不用了", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).create();

                StringBuffer sb = new StringBuffer();
                if (!Settings.canDrawOverlays(context)) {
                    sb.append("\n- 悬浮窗权限");
                }
                if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
                    sb.append("\n- 通知栏权限");
                }
                adpro.setMessage(sb.toString());
                adpro.show();
                Window window = adpro.getWindow();//对话框窗口
                window.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
                window.setWindowAnimations(R.style.dialog_style_bottom);//添加动画
            }
        }

    }

    private void toAppInfo() {
        try {
            /**
             * 跳转程序信息
             */
            Intent mIntent = new Intent();
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            mIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
            context.startActivity(mIntent);
        } catch (Exception e1) {
            Toast.makeText(context, "定制系统限制：自己手动开启吧", Toast.LENGTH_SHORT).show();
        }
    }



    protected String getAuthorityFromPermission() {
        // 先得到默认的Launcher
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        PackageManager mPackageManager = context.getPackageManager();
        ResolveInfo resolveInfo = mPackageManager.resolveActivity(intent, 0);
        if (resolveInfo == null) {
            return null;
        }
        @SuppressLint("WrongConstant")
        List<ProviderInfo> info = mPackageManager.queryContentProviders(resolveInfo.activityInfo.packageName, resolveInfo.activityInfo.applicationInfo.uid, PackageManager.GET_PROVIDERS);
        if (info != null) {
            for (int j = 0; j < info.size(); j++) {
                ProviderInfo provider = info.get(j);
                if (provider.readPermission == null) {
                    continue;
                }
                if (Pattern.matches(".*launcher.*READ_SETTINGS", provider.readPermission)) {
                    return provider.authority;
                }
            }
        }
        return null;
    }


    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        switch (preference.getKey()) {

            case "other_deldir":

                AlertDialog dia_dropdir = new AlertDialog.Builder(SettingActivity.this)
                        .setTitle("清理缓存")
                        .setMessage("确定删除本软件生成的所有缓存嘛?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {


                                try {
                                    File dir = new File("/sdcard/二维码助手/");
                                    File tmpimg = new File("/sdcard/PictureSelector.temp.jpg");

                                    new DabaiUtils().deleteDir(new File("/sdcard/QRTcrash/"));
                                    new DabaiUtils().deleteDir(new File("/sdcard/QRTWifi/"));
                                    for (File file : dir.listFiles()) {
                                        if (file.delete()) {
                                            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
                                        }
                                    }

                                    tmpimg.delete();
                                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(tmpimg)));

                                    Toast.makeText(context, "清理完成!", Toast.LENGTH_SHORT).show();
                                } catch (Exception e) {
                                    Toast.makeText(context, "清理失败!", Toast.LENGTH_SHORT).show();
                                    //e.printStackTrace();
                                }
                            }
                        })
                        .show();

                Window dia_dropdir_win = dia_dropdir.getWindow();//对话框窗口
                dia_dropdir_win.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
                dia_dropdir_win.setWindowAnimations(R.style.dialog_style_bottom);//添加动画


                break;
            case "other_share":

                View view = LayoutInflater.from(context).inflate(R.layout.dialog_share, null);

                ImageView img = view.findViewById(R.id.imageView1);
                img.setImageDrawable(getDrawable(R.drawable.shareqr));

                final AlertDialog addddddd = new AlertDialog.Builder(SettingActivity.this)
                        .setTitle("分享本软件")
                        .setView(view)
                        .show();

                Window window = addddddd.getWindow();//对话框窗口
                window.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
                window.setWindowAnimations(R.style.dialog_style_bottom);//添加动画

                Button but = view.findViewById(R.id.button1);
                but.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new DabaiUtils().sendText(SettingActivity.this, "推荐手机应用 【QRTools - 轻松创建多彩二维码】： \nhttps://www.coolapk.com/apk/com.dabai.qrtools  \n\n电脑版本：https://www.lanzous.com/b808968\n\n分享自【QRTools App】");
                        addddddd.dismiss();
                    }
                });


                break;

            case "program_info":
                Intent mIntent = new Intent();
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                mIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
                context.startActivity(mIntent);
                break;

            case "program_shortcuts":


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    final String items[] = new String[]{"生成二维码", "扫描二维码", "微信扫一扫","WiFi共享"};

                    AlertDialog ad = new AlertDialog.Builder(this).setTitle("选择快捷方式").setItems(items, new DialogInterface.OnClickListener() {
                        @SuppressLint("WrongConstant")
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (items[which]) {
                                case "生成二维码":
                                    //安装了 扫码工具
                                    Intent intent1 = new Intent(context, TextQRActivity.class);
                                    addShortCut(context, "生成二维码", R.mipmap.qr_create3, intent1);
                                    break;

                                case "扫描二维码":
                                    //安装了 扫码工具
                                    Intent intent2 = new Intent();
                                    intent2.setClassName("com.dabai.qrtools", "com.dabai.qrtools.ScanToolActivity");
                                    addShortCut(context, "扫描二维码", R.mipmap.qr_scan4, intent2);
                                    break;
                                case "微信扫一扫":

                                    Intent intent3 = new Intent();
                                    intent3.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
                                    intent3.putExtra("LauncherUI.From.Scaner.Shortcut", true);
                                    intent3.setFlags(335544320);
                                    intent3.setAction("android.intent.action.VIEW");
                                    addShortCut(context, "微信扫一扫", R.mipmap.wx_scan2, intent3);
                                    break;

                                case "WiFi共享":
                                    //安装了 扫码工具
                                    Intent intent4 = new Intent();
                                    intent4.setClassName("com.dabai.qrtools", "com.dabai.qrtools.activity.WIFIandroid");
                                    addShortCut(context, "WiFi共享", R.mipmap.shortwifi, intent4);
                                    break;
                            }
                        }
                    }).show();

                    Window window23 = ad.getWindow();//对话框窗口
                    window23.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
                    window23.setWindowAnimations(R.style.dialog_style_bottom);//添加动画


                } else {
                    Toast.makeText(context, "Android O以下不支持创建", Toast.LENGTH_SHORT).show();
                }
                break;
            case "other_pay":
                try {
                    Intent intent = new Intent();
                    //Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                    intent.setAction("android.intent.action.VIEW");
                    //支付宝二维码解析
                    Uri content_url = Uri.parse("alipayqr://platformapi/startapp?saId=10000007&qrcode=HTTPS://QR.ALIPAY.COM/FKX08574RJXQHHF1SRRFIB2");
                    intent.setData(content_url);
                    startActivity(intent);
                    Toast.makeText(context, "谢谢支持😀", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(context, "调起支付宝失败！", Toast.LENGTH_SHORT).show();
                }
                break;
            case "other_feedback":
                startActivity(new Intent(this, FeedBack.class));
                break;
            case "other_help":
                new DabaiUtils().openLink(this, "https://dabai2017.gitee.io/qrtools-lead");
                break;
            case "other_version":

                if (new DabaiUtils().checkApkExist(context,"com.coolapk.market")){

                    adddd = new AlertDialog.Builder(this).setTitle("更新")
                            .setMessage("当前版本 : " + new DabaiUtils().getVersionName(getApplicationContext())
                                    + "\n酷安最新版本 : " + "正在检查")
                            .setPositiveButton("跳转酷安", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    try {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        intent.setPackage("com.coolapk.market");
                                        intent.setData(Uri.parse("https://www.coolapk.com/apk/com.dabai.qrtools"));
                                        startActivity(intent);
                                    } catch (Exception e) {
                                        Toast.makeText(context, "打开失败!"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }

                                }
                            })
                            .show();

                }else {
                    adddd = new AlertDialog.Builder(this).setTitle("更新")
                            .setMessage("当前版本 : " + new DabaiUtils().getVersionName(getApplicationContext())
                                    + "\n酷安最新版本 : " + "正在检查")
                            .setPositiveButton("跳转更新页面", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(SettingActivity.this, WebActivity.class);
                                    intent.putExtra("link", "https://www.coolapk.com/apk/com.dabai.qrtools");
                                    startActivity(intent);
                                }
                            })
                            .show();
                }

                Window windowver = adddd.getWindow();//对话框窗口
                windowver.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
                windowver.setWindowAnimations(R.style.dialog_style_bottom);//添加动画

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updata_message();
                            }
                        });

                    }
                }).start();

                break;

            case "clip_monitor":
                boolean clip_monitor = preference.getSharedPreferences().getBoolean("clip_monitor", false);
                //即时生效

                if (clip_monitor) {

                    AlertDialog add = new AlertDialog.Builder(this).setTitle("提示").setPositiveButton("知道了", null).setCancelable(false).setMessage("记得把软件后台锁上，避免服务被系统回收").show();

                    Window window1 = add.getWindow();//对话框窗口
                    window1.setGravity(Gravity.TOP);//设置对话框显示在屏幕中间
                    window1.setWindowAnimations(R.style.dialog_style_top);//添加动画

                    try {
                        // Android 8.0使用startForegroundService在前台启动新服务
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(clipintent);
                        } else {
                            context.startService(clipintent);
                        }
                    } catch (Exception e) {
                        context.startService(clipintent);

                    }
                } else {
                    stopService(clipintent);
                }

                break;

            case "exit_0":


                AlertDialog addd = new AlertDialog.Builder(this).setTitle("提示")
                        .setMessage("是否彻底退出程序，这回终止正在运行的后台服务，和全部前台活动!")
                        .setPositiveButton("退出", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                exit0();

                            }
                        })
                        .show();

                Window window2 = addd.getWindow();//对话框窗口
                window2.setGravity(Gravity.TOP);//设置对话框显示在屏幕中间
                window2.setWindowAnimations(R.style.dialog_style_top);//添加动画

                break;

            case "screenshot_monitor":
                boolean screenshot_monitor = preference.getSharedPreferences().getBoolean("screenshot_monitor", false);
                //即时生效

                if (screenshot_monitor) {

                    AlertDialog add = new AlertDialog.Builder(this).setTitle("提示").setPositiveButton("知道了", null).setCancelable(false).setMessage("记得把软件后台锁上，避免服务被系统回收").show();

                    Window window1 = add.getWindow();//对话框窗口
                    window1.setGravity(Gravity.TOP);//设置对话框显示在屏幕中间
                    window1.setWindowAnimations(R.style.dialog_style_top);//添加动画


                    //检查 查看使用权限
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    if (!isSecurityPermissionOpen(context)) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                req_see();
                                            }
                                        });
                                    }
                                }
                            }).start();
                        }
                    } catch (Exception e) {
                    }


                    try {
                        // Android 8.0使用startForegroundService在前台启动新服务
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(scintent);
                        } else {
                            context.startService(scintent);
                        }
                    } catch (Exception e) {
                        context.startService(scintent);
                    }

                } else {
                    stopService(scintent);
                }

                break;


            case "other_delqr":

                /**
                 *  查杀相册中二维码
                 */

                AlertDialog drop = new AlertDialog.Builder(SettingActivity.this)
                        .setTitle("警告")
                        .setMessage("此操作会删除相册里所有附带二维码的图片,请进行确认！")
                        .setPositiveButton("开始验证", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                //P以下
                                KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);

                                if (km.isKeyguardSecure()) {
                                    Intent kmr = km.createConfirmDeviceCredentialIntent("警告", "这个设备的主人是你嘛?");
                                    startActivityForResult(kmr, 888);

                                } else {
                                    Toast to = Toast.makeText(context, "没有设置屏幕锁，跳过验证", Toast.LENGTH_SHORT);
                                    to.setGravity(Gravity.CENTER, 0, 0);
                                    to.show();
                                    two_ok();

                                }

                            }
                        })
                        .show();
                Window dr = drop.getWindow();//对话框窗口
                dr.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
                dr.setWindowAnimations(R.style.dialog_style_bottom);//添加动画

                break;
        }
        return true;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 888) {
            two_ok();

        } else {
            Toast.makeText(context, "验证身份失败!", Toast.LENGTH_SHORT).show();
        }


    }

    private void exit0() {
        Intent intent = new Intent();
        // 为Intent设置Action、Category属性
        intent.setAction(Intent.ACTION_MAIN);// "android.intent.action.MAIN"
        intent.addCategory(Intent.CATEGORY_HOME); //"android.intent.category.HOME"
        startActivity(intent);
        System.exit(0);
        System.exit(0);
    }


    /**
     * 删除 所有二维码
     */

    int total = 0;
    int ph_count = 0;
    int qr_count = 0;
    String up_path;

    boolean is_drop;

    ProgressBar pro_total;
    TextView pro_count, pro_totalpro, pro_totalnum;
    SeekBar pro_seek;

    long speed = 50;


    public void drop_all() {

        total = 0;
        ph_count = 0;
        qr_count = 0;
        is_drop = true;

        photos_all = getSystemPhotoList(context);
        total = photos_all.size();

        dia_pro.show();

        //弹窗控件
        pro_total = dia_pro_view.findViewById(R.id.pro_1);
        pro_totalpro = dia_pro_view.findViewById(R.id.textView11);
        pro_totalnum = dia_pro_view.findViewById(R.id.textView10);
        pro_total.setMax(total);
        pro_totalnum.setText("" + total);
        pro_count = dia_pro_view.findViewById(R.id.pro_2);
        pro_seek = dia_pro_view.findViewById(R.id.pro_3);

        pro_total.setProgress(0);
        pro_count.setText("0");
        pro_seek.setProgress(1);
        speed = 200;

        pro_seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                switch (progress) {
                    case 0:
                        speed = 400;
                        break;
                    case 1:
                        speed = 200;
                        break;
                    case 2:
                        speed = 50;
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        Window window = dia_pro.getWindow();//对话框窗口
        window.setGravity(Gravity.CENTER);//设置对话框显示在屏幕中间
        window.setWindowAnimations(R.style.dialog_style_bottom);//添加动画


        new Thread(new Runnable() {
            @Override
            public void run() {

                for (String photo : photos_all) {

                    if (is_drop) {

                        //文件 转 bitmap
                        Bitmap obmp = BitmapFactory.decodeFile(photo);
                        int width = obmp.getWidth();
                        int height = obmp.getHeight();
                        int[] data = new int[width * height];
                        obmp.getPixels(data, 0, width, 0, 0, width, height);
                        RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                        up_path = photo;
                        new QrCodeAsyncTask().execute(bitmap);

                        if (total < 30) {
                            //休息一秒
                            try {
                                Thread.sleep(400);
                            } catch (InterruptedException e) {
                            }

                        } else {
                            //休息一秒
                            try {
                                Thread.sleep(speed);
                            } catch (InterruptedException e) {
                            }

                        }


                        ph_count++;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pro_total.setProgress(ph_count);
                                pro_totalpro.setText(Math.ceil(((double) ph_count / (double) total) * 100) + "%");
                            }
                        });


                    } else {
                        break;
                    }

                }
            }
        }).start();

    }


    class QrCodeAsyncTask extends AsyncTask<BinaryBitmap, Void, Result> {

        @Override
        protected Result doInBackground(BinaryBitmap... params) {
            QRCodeReader reader = new QRCodeReader();
            Result result = null;
            try {
                result = reader.decode(params[0]);
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (ChecksumException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Result result) {
            super.onPostExecute(result);

            if (result != null) {
                File file = new File(up_path);
                if (file.delete()) {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                    qr_count++;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pro_count.setText("" + qr_count);
                        }
                    });
                }

            }
        }
    }


    private void two_ok() {

        AlertDialog two_dia = new AlertDialog.Builder(this)
                .setTitle("最后一次确认")
                .setMessage("当你点击确定按钮后,图库里的所有二维码都会消失!")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        try {
                            drop_all();
                        } catch (Exception e) {
                            Toast.makeText(context, "图库中没有图片", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).show();

        Window two = two_dia.getWindow();//对话框窗口
        two.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
        two.setWindowAnimations(R.style.dialog_style_bottom);//添加动画


    }


    private void req_see() {


        AlertDialog ad = new AlertDialog.Builder(this).setCancelable(false).setNeutralButton("算了", null).setTitle("提示").setMessage("1·请确保你截图里边的二维码足够清晰,以保证程序正常识别。\n2·你可以授予\"查看使用情况权限\"来显示你在哪个应用截的图。").setPositiveButton("授权", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                try {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        //此处要判断用户的安全权限有没有打开，如果打开了就进行获取栈顶Activity的名字的方法
                        //当然，我们的要求是如果没打开就不获取了，要不然跳转会影响用户的体验
                        if (!isSecurityPermissionOpen(context)) {

                            //此处是跳转安全权限的跳转代码，如果你判断用户没有开启权限的话可以选择跳转，此处标明~~~
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);

                        } else {
                            Toast.makeText(context, "已经成功授权", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, Build.VERSION_CODES.LOLLIPOP + "以下不需要手动权限", Toast.LENGTH_SHORT).show();
                    }


                } catch (Exception e) {
                    Log.d(TAG, "onClick: " + e);
                    Toast.makeText(context, "授权失败,请手动开启", Toast.LENGTH_SHORT).show();
                }


            }
        }).show();

        Window window = ad.getWindow();//对话框窗口
        window.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
        window.setWindowAnimations(R.style.dialog_style_bottom);//添加动画

    }


    /**
     * 获取 相册内所有的图片
     *
     * @param context
     * @return
     */
    public static List<String> getSystemPhotoList(Context context) {
        List<String> result = new ArrayList<String>();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null || cursor.getCount() <= 0) return null; // 没有图片
        while (cursor.moveToNext()) {
            int index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            String path = cursor.getString(index); // 文件地址
            File file = new File(path);
            if (file.exists()) {
                result.add(path);
            }
        }

        return result;
    }


    String nettitle;

    private void updata_message() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String link = "https://www.coolapk.com/apk/233673";
                    nettitle = new HtmlUtils().getHtmlTitle(link).get(0);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String[] res = nettitle.split(" - ");
                            adddd.setMessage("当前版本 : " + new DabaiUtils().getVersionName(getApplicationContext())
                                    + "\n酷安最新版本 : " + res[2]);

                        }
                    });

                } catch (Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adddd.setMessage("当前版本 : " + new DabaiUtils().getVersionName(getApplicationContext())
                                    + "\n酷安最新版本 : 网络出现问题");
                        }
                    });

                }
            }
        }).start();


    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void addShortCut(Context context, String name, int icon, Intent intent) {
        ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(Context.SHORTCUT_SERVICE);

        if (shortcutManager.isRequestPinShortcutSupported()) {
            Intent shortcutInfoIntent = intent;
            shortcutInfoIntent.setAction(Intent.ACTION_VIEW); //action必须设置，不然报错

            ShortcutInfo info = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
                info = new ShortcutInfo.Builder(context, "The only id" + (int) (Math.random() * 10000))
                        .setIcon(Icon.createWithResource(context, icon))
                        .setShortLabel(name)
                        .setIntent(shortcutInfoIntent)
                        .build();

            }

            //当添加快捷方式的确认弹框弹出来时，将被回调
            PendingIntent shortcutCallbackIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            shortcutManager.requestPinShortcut(info, shortcutCallbackIntent.getIntentSender());
        }

    }


    //判断用户对应的安全权限有没有打开
    @SuppressLint("WrongConstant")
    private static boolean isSecurityPermissionOpen(Context context) {
        long endTime = System.currentTimeMillis();
        UsageStatsManager usageStatsManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            usageStatsManager = (UsageStatsManager) context.getApplicationContext().getSystemService("usagestats");
            List<UsageStats> queryUsageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, 0, endTime);
            if (queryUsageStats == null || queryUsageStats.isEmpty()) {
                return false;
            }
        }

        return true;
    }


}



