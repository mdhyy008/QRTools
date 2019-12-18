package com.dabai.qrtools;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class WxScanActivity extends AppCompatActivity {

    @SuppressLint("WrongConstant")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isapp = new DabaiUtils().checkApkExist(getApplicationContext(),"com.tencent.mm");


        if (isapp){


            //dark
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

            try {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"));
                intent.putExtra("LauncherUI.From.Scaner.Shortcut", true);
                intent.setFlags(335544320);
                intent.setAction("android.intent.action.VIEW");
                startActivity(intent);
                finish();
            } catch (Exception e) {
            }

        }else {
            Toast.makeText(this, "未安装微信app", Toast.LENGTH_SHORT).show();
            finish();
        }


    }
}
