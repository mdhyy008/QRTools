package com.dabai.qrtools;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

public class ClipService extends Service {
    ClipboardManager cb;
    private ClipboardManager.OnPrimaryClipChangedListener cl;

    String TAG = "dabaizzz";
    String text;

    public ClipService() {

    }


    @Override
    public void onCreate() {
        super.onCreate();

        handler.post(task);//立即调用
        Log.d(TAG, "onStartCommand: 截屏 保活服务启动");

        try {
            //如果API在26以上即版本为O则调用startForefround()方法启动服务
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setForegroundService();
            }
        } catch (Exception e) {

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        cb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cl = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {


                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    android.text.ClipboardManager cb = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    text = null;
                    text = cb.getText().toString();
                } else {
                    ClipboardManager cb = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    text = null;
                    text = cb.getPrimaryClip().getItemAt(0).getText().toString();
                }

                if (text.length() > 1000) {

                    AlertDialog ad = new AlertDialog.Builder(getApplicationContext()).setCancelable(false).setTitle("剪切板监听失败").setMessage("由于剪切板内容超过了1000字节，不能成功生成二维码:)").setPositiveButton("好吧", null).create();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ad.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
                    } else {
                        ad.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    }
                    ad.show();
                    Window window = ad.getWindow();//对话框窗口
                    window.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
                    window.setWindowAnimations(R.style.dialog_style_bottom);//添加动画

                } else {

                    AlertDialog ad = new AlertDialog.Builder(getApplicationContext()).setCancelable(false).setTitle("剪切板监听").setMessage("剪切板内容:\n{\n" + (text.length() > 50 ? text.substring(0, 50) + "..." : text) + "\n}\n,是否生成二维码查看?")
                            .setPositiveButton("生成", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent resultIntent = new Intent(ClipService.this, ClipActivity.class);
                                    resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(resultIntent);

                                }
                            }).setNeutralButton("取消", null).create();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ad.getWindow().setType((WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY));
                    } else {
                        ad.getWindow().setType((WindowManager.LayoutParams.TYPE_SYSTEM_ALERT));
                    }
                    ad.show();
                    Window window = ad.getWindow();//对话框窗口
                    window.setGravity(Gravity.BOTTOM);//设置对话框显示在屏幕中间
                    window.setWindowAnimations(R.style.dialog_style_bottom);//添加动画

                }
            }
        };

        cb.addPrimaryClipChangedListener(cl);

        return super.onStartCommand(intent, flags, startId);
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


    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        public void run() {

            handler.postDelayed(this, 5 * 1000);//设置循环时间，此处是5秒

            if (!isServiceRunning(getApplicationContext(), "com.dabai.qrtools.ScreenshotMonitorService")) {
                init();

            }
        }


    };


    /**
     * 初始化各种东西
     */

    boolean screenshot_monitor;
    Intent screenintent;

    private void init() {

        SharedPreferences sp = this.getSharedPreferences("com.dabai.qrtools_preferences", 0);

        //获取 设置 值  来设置是否启动监听

        screenshot_monitor = sp.getBoolean("screenshot_monitor", false);

        //初始化截图监听
        screenintent = new Intent(this, ScreenshotMonitorService.class);

        if (screenshot_monitor) {
            Log.d(TAG, "run: ScreenshotMonitorService已被保活");

            // Android 8.0使用startForegroundService在前台启动新服务
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(screenintent);
            } else {
                startService(screenintent);
            }
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

    //得到栈顶Activity的名字，注意此处要进行判断，Android在5.0以后Google把getRunningTasks的方法给屏蔽掉了，所以要分开处理
    private static String getTopActivityName(Context context) {
        String topActivityPackageName;
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //此处要判断用户的安全权限有没有打开，如果打开了就进行获取栈顶Activity的名字的方法
            //当然，我们的要求是如果没打开就不获取了，要不然跳转会影响用户的体验
            if (isSecurityPermissionOpen(context)) {
                UsageStatsManager mUsageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
                long endTime = System.currentTimeMillis();
                long beginTime = endTime - 1000 * 60 * 2;
                UsageStats recentStats = null;

                List<UsageStats> queryUsageStats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_BEST, beginTime, endTime);
                if (queryUsageStats == null || queryUsageStats.isEmpty()) {
                    return null;
                }

                for (UsageStats usageStats : queryUsageStats) {
                    if (recentStats == null || recentStats.getLastTimeUsed() < usageStats.getLastTimeUsed()) {
                        recentStats = usageStats;
                    }
                }
                topActivityPackageName = recentStats.getPackageName();
                return topActivityPackageName;
            } else {

                return null;
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfos = manager.getRunningTasks(1);
            if (taskInfos.size() > 0)
                topActivityPackageName = taskInfos.get(0).topActivity.getPackageName();
            else
                return null;
            return topActivityPackageName;
        }
    }


    @Override
    public void onDestroy() {

        cb.removePrimaryClipChangedListener(cl);

        super.onDestroy();
    }



    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    /**
     * 通过通知启动服务
     */
    @TargetApi(Build.VERSION_CODES.O)
    public void setForegroundService() {
        //设定的通知渠道名称
        String channelName = "剪切板监听^前台服务";
        //设置通知的重要程度
        int importance = NotificationManager.IMPORTANCE_LOW;
        //构建通知渠道
        String CHANNEL_ID = "8";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);

        //在创建的通知渠道上发送通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_stat_name) //设置通知图标
                .setColor(Color.parseColor("#69F0AE"))
                .setContentText("剪切板监听服务正在运行")//设置通知内容
                .setOngoing(true);//设置处于运行状态
        //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        //将服务置于启动状态 NOTIFICATION_ID指的是创建的通知的ID
        startForeground(9, builder.build());
    }


}
