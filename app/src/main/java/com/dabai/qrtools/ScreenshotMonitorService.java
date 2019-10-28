package com.dabai.qrtools;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScreenshotMonitorService extends Service {

    ScreenShotListenManager manager;
    String imgpath2;

    String topName = "";
    private String TAG = "dabai";

    public ScreenshotMonitorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        handler.post(task);//立即调用
        Log.d(TAG, "onStartCommand: 剪切板 保活服务启动");

        try {
            //如果API在26以上即版本为O则调用startForefround()方法启动服务
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setForegroundService();
            }
        } catch (Exception e) {

        }


        manager = ScreenShotListenManager.newInstance(this);
        manager.setListener(
                new ScreenShotListenManager.OnScreenShotListener() {
                    public void onShot(String imagePath) {
                        //Toast.makeText(ScreenshotMonitorService.this, "新截图:"+imagePath, Toast.LENGTH_SHORT).show();

                        //Log.d("dabai", "栈顶包名: " + getTopActivityName(getApplicationContext()));

                        topName = "";
                        try {
                            if (getTopActivityName(getApplicationContext()) != null) {
                                PackageManager pm = getPackageManager();
                                ApplicationInfo info = pm.getApplicationInfo(getTopActivityName(getApplicationContext()), 0);
                                String appname = (String) info.loadLabel(pm);
                                topName = "在{" + appname + "}里的截图";
                            }
                        } catch (PackageManager.NameNotFoundException e) {

                        }


                        //文件 转 bitmap
                        Bitmap obmp = BitmapFactory.decodeFile(imagePath);
                        imgpath2 = imagePath;
                        int width = obmp.getWidth();
                        int height = obmp.getHeight();
                        int[] data = new int[width * height];
                        obmp.getPixels(data, 0, width, 0, 0, width, height);
                        RGBLuminanceSource source = new RGBLuminanceSource(width, height, data);
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                        new QrCodeAsyncTask().execute(bitmap);

                    }
                }
        );
        manager.startListen();

        super.onCreate();
    }


    public static String getApplicationNameByPackageName(Context context, String packageName) {

        PackageManager pm = context.getPackageManager();
        String Name;
        try {
            Name = pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            Name = "";
        }
        return Name;
    }


    @Override
    public void onDestroy() {

        manager.stopListen();
        super.onDestroy();
    }


    /**
     * 通过通知启动服务
     */
    @TargetApi(Build.VERSION_CODES.O)
    public void setForegroundService() {
        //设定的通知渠道名称
        String channelName = "截图监听^前台服务";
        //设置通知的重要程度
        int importance = NotificationManager.IMPORTANCE_LOW;
        //构建通知渠道
        String CHANNEL_ID = "11";
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);

        //在创建的通知渠道上发送通知
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.drawable.ic_stat_name) //设置通知图标
                .setColor(Color.parseColor("#69F0AE"))
                .setContentText("截图监听服务正在运行")//设置通知内容
                .setOngoing(true);//设置处于运行状态
        //向系统注册通知渠道，注册后不能改变重要性以及其他通知行为
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(channel);
        //将服务置于启动状态 NOTIFICATION_ID指的是创建的通知的ID
        startForeground(12, builder.build());
    }


    class QrCodeAsyncTask extends AsyncTask<BinaryBitmap, Void, Result> {

        private String text;

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
                text = result.getText();
                //sendNotification(getApplicationContext(), "15", "截图监听结果^跳转Result活动", "检测到你的最新截图里包含二维码", "点击识别" + topName + "并显示结果", text);


                AlertDialog ad = new AlertDialog.Builder(getApplicationContext()).setCancelable(false).setTitle("截图监听").setMessage("检测到" + topName + "里边包含二维码信息,是否查看")

                        .setNegativeButton("删除截图并查看", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                File file = new File(imgpath2);

                                if (new File(imgpath2).delete()) {
                                    Toast.makeText(ScreenshotMonitorService.this, "删除截图成功", Toast.LENGTH_SHORT).show();
                                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));

                                } else {
                                    Toast.makeText(ScreenshotMonitorService.this, "删除截图失败", Toast.LENGTH_SHORT).show();
                                }

                                Intent resultIntent = new Intent(getApplicationContext(), ScanResultActivity.class);
                                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                resultIntent.putExtra("result", text);

                                startActivity(resultIntent);

                            }
                        })

                        .setPositiveButton("直接查看", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                Intent resultIntent = new Intent(ScreenshotMonitorService.this, ScanResultActivity.class);
                                resultIntent.putExtra("result", text);
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


        @TargetApi(Build.VERSION_CODES.O)
        public void sendNotification(Context context, String channelID, String channelName, String subText, String title, String text) {


            NotificationManager manager;
            manager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);


            try {
                //创建通道管理器
                NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            } catch (Exception e) {

            }

            Notification.Builder builder = new Notification.Builder(context);
            //设置小图标
            builder.setSmallIcon(R.drawable.ic_stat_name);
            //设置通知 标题，内容，小标题
            builder.setContentTitle(title);
            builder.setSubText(subText);
            //设置通知颜色
            builder.setColor(Color.parseColor("#1565C0"));
            //设置创建时间
            builder.setWhen(System.nanoTime());

            try {
                //创建通知时指定channelID
                builder.setChannelId(channelID);
            } catch (Exception e) {

            }

            Intent resultIntent = new Intent(context, ScanResultActivity.class);
            resultIntent.putExtra("result", text);
            PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            Notification notification = builder.build();
            notification.flags |= Notification.FLAG_AUTO_CANCEL;
            manager.notify(100, notification);

        }
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


    /**
     * 初始化各种东西
     */

    boolean clip_monitor;
    Intent clipintent;

    private void init() {

        SharedPreferences sp = this.getSharedPreferences("com.dabai.qrtools_preferences", 0);

        //获取 设置 值  来设置是否启动监听

        clip_monitor = sp.getBoolean("clip_monitor", false);

        //初始化剪切板监听
        clipintent = new Intent(this, ClipService.class);

        if (clip_monitor) {
            Log.d(TAG, "run: ClipService已被保活");
            // Android 8.0使用startForegroundService在前台启动新服务
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(clipintent);
            } else {
                startService(clipintent);
            }
        }


    }


    private Handler handler = new Handler();
    private Runnable task = new Runnable() {
        public void run() {

            handler.postDelayed(this, 5 * 1000);//设置循环时间，此处是5秒

            if (!isServiceRunning(getApplicationContext(), "com.dabai.qrtools.ClipService")) {
                init();

            }

        }


    };


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


}

