package com.dabai.qrtools.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.dabai.qrtools.R;
import com.dabai.qrtools.TextQRActivity;
import com.dabai.qrtools.utils.Base64;


public class QQshare2Activity extends Activity {
    private String TAG = "dabaizzz";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            Intent intent = getIntent();

            String getdata = "" + intent.getData();

            if (!getdata.contains("546L6ICF6I2j6ICA")){
                Toast.makeText(this, "此链接不是王者荣耀\"邀请QQ好友\"功能分享的链接!", Toast.LENGTH_LONG).show();
                finish();
            }else {
                String base64_link = getdata.substring(getdata.indexOf("&url")+5,getdata.indexOf("&app_name"));
                Base64 base64 = new Base64();
                String link = base64.decode(base64_link);
                finish();
                ToRes(link);


               /*
                Toast toast = Toast.makeText(this, "邀请好友使用QQ扫一扫即可", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.TOP, 0, 0);
                toast.show();
                */


                Display display = getWindowManager().getDefaultDisplay();
                // 获取屏幕高度
                int height = display.getHeight()/2;
                //加载Toast布局
                View toastRoot = LayoutInflater.from(getApplicationContext()).inflate(R.layout.toast_wangzhe, null);
                //初始化布局控件
                TextView mTextView = toastRoot.findViewById(R.id.message);


                Toast toast = new Toast(getApplicationContext());
                toast.setGravity(Gravity.TOP, 0, 100);
                toast.setDuration(Toast.LENGTH_LONG);
                toast.setView(toastRoot);
                toast.show();

            }
        } catch (Exception e) {
            Toast.makeText(this, "王者荣耀链接转二维码异常:\n"+e.getMessage(), Toast.LENGTH_LONG).show();
        }


    }

    /**
     * 传值给 生成二维码界面
     * @param text
     */
    void ToRes(String text) {
        Intent resultIntent = new Intent(this, TextQRActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        resultIntent.putExtra("download", text);
        startActivity(resultIntent);
    }
}
