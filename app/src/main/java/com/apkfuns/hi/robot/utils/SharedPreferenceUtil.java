package com.apkfuns.hi.robot.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.apkfuns.hi.robot.app.App;

public class SharedPreferenceUtil {

    private SharedPreferences sp;
    private static SharedPreferenceUtil singleton;

    private SharedPreferenceUtil() {
        sp = App.getContext().getSharedPreferences("cache", Context.MODE_PRIVATE);
    }

    public static SharedPreferenceUtil getInstance() {
        if (singleton == null) {
            synchronized (SharedPreferenceUtil.class) {
                if (singleton == null) {
                    singleton = new SharedPreferenceUtil();
                }
            }
        }
        return singleton;
    }

    public void put(@NonNull String key, @NonNull Object value) {
        SharedPreferences.Editor editor = sp.edit();
        String type = value.getClass().getSimpleName();
        try {
            switch (type) {
                case "Boolean":
                    editor.putBoolean(key, (Boolean) value);
                    break;
                case "Long":
                    editor.putLong(key, (Long) value);
                    break;
                case "Float":
                    editor.putFloat(key, (Float) value);
                    break;
                case "String":
                    editor.putString(key, (String) value);
                    break;
                case "Integer":
                    editor.putInt(key, (Integer) value);
                    break;
                default:
                    editor.putString(key, value.toString());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        editor.apply();
    }

    public long getLong(@NonNull String key) {
        return sp.getLong(key, 0);
    }

    public String getString(@NonNull String key) {
        return sp.getString(key, "");
    }

}
