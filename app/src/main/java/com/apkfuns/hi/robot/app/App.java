package com.apkfuns.hi.robot.app;

import android.app.Application;

import com.apkfuns.logutils.LogUtils;

/**
 * Created by pengwei on 16/2/4.
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.configAllowLog = true;
        LogUtils.configTagPrefix = "HiRobot";
    }
}
