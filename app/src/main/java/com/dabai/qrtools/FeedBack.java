package com.dabai.qrtools;


import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


//记得在清单声明在后台隐藏
public class FeedBack extends Activity {
    private EditText ed1;
    private Button bu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(android.R.style.Theme_Material_Light_Dialog_Alert);

        setTitle("反馈");
        LinearLayout ll1 = new LinearLayout(this);
        ll1.setOrientation(LinearLayout.VERTICAL);

        ed1 = new EditText(this);
        ed1.setMaxLines(8);
        ed1.setMinLines(5);
        ed1.setGravity(Gravity.TOP);
        //ed1.setBackground(getDrawable(R.drawable.edit_shape));
        ed1.setPadding(10,10,10,10);

        bu = new Button(this);
        ed1.setHint("请输入要反馈的问题");

        bu.setText("发送反馈");
        ll1.addView(ed1);
        ll1.addView(bu);
        bu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View p1) {

                try {
                    if (!ed1.getText().toString().isEmpty()) {
                        bu.setText("正在发送......");
                        feedback(getString(R.string.app_name), ed1.getText().toString());
                    }else {
                        ed1.setError("这里还空着哦!");
                    }
                } catch (Exception e) {
                    Toast.makeText(FeedBack.this, "哦~~被玩坏了!", Toast.LENGTH_SHORT).show();
                }

            }
        });
        setContentView(ll1);
    }

    public void feedback(final String title, final String text) {
        new Thread(new Runnable() {

            private int qucode;

            @Override
            public void run() {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss EEE", Locale.CHINA);
                    String titlem = "[来自:" + Build.MODEL + "的反馈]";
                    String textm = " **[时间：" +sdf.format(new Date())+ "]** **[版本：" + new DabaiUtils().getVersionName(getApplicationContext()) + "]**";

                    URL url = new URL("https://sc.ftqq.com/SCU35649Tec88ecad70ac8f2375a6c5a6e323c8425be9602402c5b.send?text=" + title + titlem + "&desp=" + text + textm);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();
                    final int code = urlConnection.getResponseCode();
                    qucode = code;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (code == 200) {
                                Toast.makeText(getApplicationContext(), "发送成功", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "发送失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "ERROR:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }

}
