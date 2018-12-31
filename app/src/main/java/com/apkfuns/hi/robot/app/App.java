package com.apkfuns.hi.robot.app;

import android.app.Application;
import android.content.Context;

import com.apkfuns.logutils.LogUtils;

/**
 * Created by pengwei on 16/2/4.
 */
public class App extends Application {

    private static App singleton;

    @Override
    public void onCreate() {
        singleton = this;
        super.onCreate();
        LogUtils.configAllowLog = true;
        LogUtils.configTagPrefix = "HiRobot";
    }

    public static Context getContext() {
        return singleton;
    }
}
