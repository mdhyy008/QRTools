package com.dabai.qrtools;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.hardware.biometrics.BiometricPrompt;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class ScanResultActivity extends AppCompatActivity {

    TextView tv_res;
    ConstraintLayout cons;

    String restext;
    AlertDialog alertDialog1;

    boolean isChorme = true;


    CardView cd1, cd2, cd3, cd4;


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
        cons = findViewById(R.id.cons);
        tv_res = findViewById(R.id.result_text);
        tv_res.setText(result);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        cd1 = findViewById(R.id.cd1);
        cd2 = findViewById(R.id.cd2);
        cd3 = findViewById(R.id.cd3);
        cd4 = findViewById(R.id.cd4);




        help_checked();
    }

    /**
     * 检查得到的结果做出推荐操作
     */
    public void help_checked() {

        //识别得到的结果\n试别联系人名片\n试别网址\n识别

        if (!restext.equals("")) {
            cd1.setVisibility(View.VISIBLE);
        }


        if (restext.contains("http") || restext.contains("ftp")) {
            cd2.setVisibility(View.VISIBLE);
        }

        if (restext.replace(" ", "").length() == 11 || restext.replace("-", "").length() == 11) {
            cd3.setVisibility(View.VISIBLE);
        }

        if (restext.toUpperCase().contains("BEGIN")) {
            cd4.setVisibility(View.VISIBLE);
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

    //添加到手机联系人
    public static void addContact(Context context, String name, String phone) {
        Uri insertUri = android.provider.ContactsContract.Contacts.CONTENT_URI;
        Intent intent = new Intent(Intent.ACTION_INSERT, insertUri);
        intent.putExtra(android.provider.ContactsContract.Intents.Insert.NAME, name);//名字显示在名字框
        intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, phone);//号码显示在号码框
//        intent.putExtra(ContactsContract.Intents.Insert.POSTAL,"");//地址显示在地址框
        context.startActivity(intent);
    }

    //保存至已有联系人
    public static void saveExistContact(Context context, String name, String phone) {
        Intent intent = new Intent(Intent.ACTION_INSERT_OR_EDIT);
        intent.setType("vnd.android.cursor.item/person");
        intent.setType("vnd.android.cursor.item/contact");
        intent.setType("vnd.android.cursor.item/raw_contact");
        //    intent.putExtra(android.provider.ContactsContract.Intents.Insert.NAME, name);
        intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, phone);
        intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE_TYPE, 3);
        context.startActivity(intent);
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
        clipboardManager.setText(tv_res.getText().toString());
        Snackbar.make(cons, "复制成功", Snackbar.LENGTH_SHORT).show();
    }

    //分享文本
    public void res_share(View view) {
        sendText(tv_res.getText().toString());
    }

    //打开链接
    public void res_openlink(View view) {

        if (restext.startsWith("http")) {
            //浏览器
            new DabaiUtils().openLink(this, restext);
        } else {
            Snackbar.make(cons, "不是链接", Snackbar.LENGTH_SHORT).show();
        }

    }


    //创建联系人
    public void res_createman(View view) {
        addContact(this, "", restext.replace("-", ""));
    }
    //保存联系人
    public void res_saveman(View view) {
        saveExistContact(this, "", restext.replace("-", ""));
    }

    //打开微信扫一扫
    @SuppressLint("WrongConstant")
    public void res_openwx(View view) {

        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
            intent.setFlags(335544320);
            intent.setAction("android.intent.action.VIEW");
            startActivity(intent);
        } catch (Exception e) {
        }

    }


    public void res_openweb(View view) {
        //调用本程序
        Intent intent = new Intent(this,WebActivity.class);
        intent.putExtra("link", restext);
        startActivity(intent);
    }
}
