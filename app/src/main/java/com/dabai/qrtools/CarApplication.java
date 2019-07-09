package com.dabai.qrtools;
import android.app.Application;
import android.content.Context;

/**
 * Created by 。 on 2018/12/5.
 */

public class CarApplication extends Application {
    private static Context context;
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler.getInstance().init(this);
        context = this;
    }
    public static Context getContext(){
        return context;
    }
}
