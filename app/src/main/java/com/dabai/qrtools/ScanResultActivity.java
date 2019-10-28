package com.dabai.qrtools;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.dabai.qrtools.utils.AESUtils3;
import com.dabai.qrtools.utils.Base64;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ScanResultActivity extends AppCompatActivity {

    TextView tv_res;
    ConstraintLayout cons;

    String restext;
    AlertDialog alertDialog1;

    boolean isChorme = true;


    CardView cd1, cd2, cd3, cd5;
    private String password, netWorkType, netWorkName;
    private WifiAdmin wifiAdmin;
    private ProgressDialog pd;
    private WifiManager mWifiManager;
    WifiInfo mWifiInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        Intent intent = getIntent();
        String result = intent.getStringExtra("result");
        //dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        if (result == null) {
            Toast.makeText(this, "接收到的值为空，无法显示", Toast.LENGTH_SHORT).show();
        }

        restext = result;

        if (restext.startsWith("funny:")){
            Toast.makeText(this, "滑稽控链接，可直接用浏览器打开", Toast.LENGTH_LONG).show();
        }

        cons = findViewById(R.id.cons);
        tv_res = findViewById(R.id.result_text);
        tv_res.setText(result);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cd1 = findViewById(R.id.cd1);
        cd2 = findViewById(R.id.cd2);
        cd3 = findViewById(R.id.cd3);

        cd5 = findViewById(R.id.cd5);

        wifiAdmin = new WifiAdmin(getApplicationContext());
        help_checked();

        pd = new ProgressDialog(ScanResultActivity.this);
        pd.setTitle("提示");
    }

    /**
     * 检查得到的结果做出推荐操作
     */
    public void help_checked() {

        //识别得到的结果\n试别联系人名片\n试别网址\n识别

        if (!restext.equals("")) {
            cd1.setVisibility(View.VISIBLE);
        }


        if (restext.contains("http") || restext.contains("ftp") || restext.contains("funny:") || restext.startsWith("dabai:")) {
            cd2.setVisibility(View.VISIBLE);
        }


        if (restext.toUpperCase().contains("BEGIN")) {
            cd3.setVisibility(View.VISIBLE);
        }

        if (restext.contains("P:") && restext.contains("T:")) {
            // 自动连接wifi
            cd5.setVisibility(View.VISIBLE);
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




    //发送文本
    private void sendText(String p0) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "This is text to send.");
        // 指定发送内容的类型
        sendIntent.setType("text/plain");
        sendIntent.putExtra(Intent.EXTRA_TEXT, p0);
        startActivity(Intent.createChooser(sendIntent, "Share"));
    }

    //复制Url
    public void res_copy(View view) {

        ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData mclipData = ClipData.newPlainText("Label", tv_res.getText().toString());
        clipboardManager.setPrimaryClip(mclipData);

        Snackbar.make(cons, "复制成功", Snackbar.LENGTH_SHORT).show();
    }

    //分享文本
    public void res_share(View view) {
        sendText(tv_res.getText().toString());
    }

    //打开链接
    public void res_openlink(View view) {

        if (restext.startsWith("http")||restext.startsWith("funny")||restext.startsWith("dabai:")) {
            //浏览器
            new DabaiUtils().openLink(this, restext);
        } else {
            Snackbar.make(cons, "不是链接", Snackbar.LENGTH_SHORT).show();
        }

    }



    /**创建新的联系人*/
    public void createNewContact(String name,String phone){
        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(android.provider.ContactsContract.Intents.Insert.NAME, name);
        intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, phone);
        startActivity(intent);
    }

    //保存至已有联系人
    public void saveExistContact(String name, String phone) {
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("vnd.android.cursor.item/person");
        intent.setType("vnd.android.cursor.item/contact");
        intent.setType("vnd.android.cursor.item/raw_contact");
        //    intent.putExtra(android.provider.ContactsContract.Intents.Insert.NAME, name);
        intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, phone);
        intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE_TYPE, 3);
        startActivity(intent);
    }

    //创建联系人
    public void res_createman(View view) {

        String[] people = restext.split("\n");

        String name = "";
        String phonenumber = "";


        for (String duan:people) {
            String[] tmp = duan.split(":");

            String key = tmp[0];
            String value = tmp[1];

            if (key.toUpperCase().equals("N")){
                name = value;
            }

            if (key.toUpperCase().equals("TEL")){
                phonenumber = value;
            }
        }

        createNewContact(""+name,""+phonenumber);
    }

    //保存联系人
    public void res_saveman(View view) {

        String[] people = restext.split("\n");

        String name = "";
        String phonenumber = "";


        for (String duan:people) {
            String[] tmp = duan.split(":");

            String key = tmp[0];
            String value = tmp[1];

            if (key.toUpperCase().equals("N")){
                name = value;
            }

            if (key.toUpperCase().equals("TEL")){
                phonenumber = value;
            }
        }

        saveExistContact(""+name,""+phonenumber);
    }




    public void res_openweb(View view) {
        //调用本程序
        Intent intent = new Intent(this, WebActivity.class);
        intent.putExtra("link", restext);
        startActivity(intent);
    }



    public void res_linkwifi(View view) {

        String passwordTemp = restext.substring(restext
                .indexOf("P:"));
        password = passwordTemp.substring(2,
                passwordTemp.indexOf(";"));
        String netWorkTypeTemp = restext.substring(restext
                .indexOf("T:"));
        netWorkType = netWorkTypeTemp.substring(2,
                netWorkTypeTemp.indexOf(";"));
        String netWorkNameTemp = restext.substring(restext
                .indexOf("S:"));
        netWorkName = netWorkNameTemp.substring(2,
                netWorkNameTemp.indexOf(";"));

        new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage("确定连接到 [" + netWorkName + "] 嘛?\n\nWiFi名称:" + netWorkName + "\n密码:" + password + "\n加密方式:" + netWorkType)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        pd.setMessage("正在连接 - " + netWorkName);

                        pd.show();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                if (!wifiAdmin.mWifiManager.isWifiEnabled()) {
                                    wifiAdmin.openWifi();
                                }

                                int net_type = 0x13;
                                if (netWorkType
                                        .compareToIgnoreCase("wpa") == 0) {
                                    net_type = WifiAdmin.TYPE_WPA;// wpa
                                } else if (netWorkType
                                        .compareToIgnoreCase("wep") == 0) {
                                    net_type = WifiAdmin.TYPE_WEP;// wep
                                } else {
                                    net_type = WifiAdmin.TYPE_NO_PASSWD;// 无加密
                                }

                                wifiAdmin.addNetwork(netWorkName, password, net_type);

                                try {
                                    addWifiDB(netWorkName, restext);
                                } catch (IOException e) {

                                }

                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {

                                }

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (isNetworkOnline()) {
                                            pd.dismiss();
                                            new AlertDialog.Builder(ScanResultActivity.this)
                                                    .setMessage("连接成功了呢O(∩_∩)O").setTitle("提示")
                                                    .setNeutralButton("亲自看一看", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            Intent intent = new Intent(ScanResultActivity.this, WebActivity.class);
                                                            intent.putExtra("link", "https://www.baidu.com/");
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .setPositiveButton("OK", null)
                                                    .show();
                                        } else {
                                            pd.dismiss();
                                            new AlertDialog.Builder(ScanResultActivity.this)
                                                    .setMessage("现在好像不能上网呦(T_T)").setTitle("提示")
                                                    .setNeutralButton("亲自看一看", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            Intent intent = new Intent(ScanResultActivity.this, WebActivity.class);
                                                            intent.putExtra("link", "https://www.baidu.com/");
                                                            startActivity(intent);
                                                        }
                                                    })
                                                    .setPositiveButton("OK", null).show();
                                        }


                                    }
                                });

                            }
                        }).start();


                    }
                }).setNeutralButton("取消", null).show();


    }


    public boolean isNetworkOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("ping -c 3 www.baidu.com");
            int exitValue = ipProcess.waitFor();
            Log.i("Avalible", "Process:" + exitValue);
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }


    /**
     * 写入WiFi库
     *
     * @param wifiname
     * @param wifiinfo
     */
    public void addWifiDB(String wifiname, String wifiinfo) throws IOException {
        //写入 QRT WiFi information 文件   简称 QWI

        File file = new File("/sdcard/QRTWifi/");
        if (file.exists()) {
            new FileUtils().writeText("/sdcard/QRTWifi/" + wifiname, wifiinfo, true);
        } else {
            file.mkdirs();
            new FileUtils().writeText("/sdcard/QRTWifi/" + wifiname, wifiinfo, true);
        }
    }



    /**
     * 判断字符串中是否包含中文
     * @param str
     * 待校验字符串
     * @return 是否为中文
     * @warn 不能校验是否为中文标点符号
     */
    public static boolean isContainChinese(String str) {
        Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
        Matcher m = p.matcher(str);
        if (m.find()) {
            return true;
        }
        return false;
    }

    String destxt;
    public void res_pass(View view) {

        new MaterialDialog.Builder(this)
                .title("解密文本")
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input("请输入密钥", null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {


                        try {
                            destxt = AESUtils3.decrypt(restext,"" + input);
                        } catch (Exception e) {
                        }

                        new MaterialDialog.Builder(ScanResultActivity.this)
                                .title("解密结果")
                                .content(""+destxt)
                                .positiveText("复制")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                        ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData mclipData = ClipData.newPlainText("Label", destxt);
                                        clipboardManager.setPrimaryClip(mclipData);

                                        Snackbar.make(cons, "复制成功", Snackbar.LENGTH_SHORT).show();
                                    }
                                })
                                .neutralText("关闭")
                                .show();


                    }
                })
                .positiveText("确定")
                .neutralText("取消")
                .show();
    }

    public void code_dnbase64(View view) {

        /**
         * 解码base64
         */
        try {
            Base64 base = new Base64();
            destxt =  base.decode(restext);
        } catch (Exception e) {
        }

        new MaterialDialog.Builder(ScanResultActivity.this)
                .title("结果")
                .content(""+destxt)
                .positiveText("复制")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        ClipboardManager clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData mclipData = ClipData.newPlainText("Label", destxt);
                        clipboardManager.setPrimaryClip(mclipData);

                        Snackbar.make(cons, "复制成功", Snackbar.LENGTH_SHORT).show();
                    }
                })
                .neutralText("关闭")
                .show();

    }


}
