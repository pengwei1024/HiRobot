package com.apkfuns.hi.robot;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;


import com.apkfuns.hi.robot.utils.SharedPreferenceUtil;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        AccessibilityManager.AccessibilityStateChangeListener {

    private static final String KEY_DELAY_TIME = "KEY_DELAY_TIME";
    private static long delayTime = 0;

    private AccessibilityManager accessibilityManager;
    private Button clickBtn;
    private EditText delayTimeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager != null) {
            accessibilityManager.addAccessibilityStateChangeListener(this);
        }
        clickBtn = findViewById(R.id.click_func);
        clickBtn.setOnClickListener(this);
        delayTimeText = findViewById(R.id.delayTime);
        delayTime = SharedPreferenceUtil.getInstance().getLong(KEY_DELAY_TIME);
        delayTimeText.setText(String.valueOf(delayTime));
        Button delayBtn = findViewById(R.id.btnSaveDelay);
        delayBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.click_func:
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case R.id.btnSaveDelay:
                delayTime = Long.parseLong(delayTimeText.getText().toString());
                SharedPreferenceUtil.getInstance().put(KEY_DELAY_TIME, delayTime);
                break;
            default:
                break;
        }
    }

    /**
     * 抢红包服务是否启用
     *
     * @return bool
     */
    private boolean isServiceEnabled() {
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(getPackageName() + "/.RobotService")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAccessibilityStateChanged(boolean enabled) {
        if (isServiceEnabled()) {
            clickBtn.setText("关闭辅助");
        } else {
            clickBtn.setText("打开辅助功能");
        }
    }

    public static long getDelayTime() {
        if (delayTime <= 0) {
            return 0;
        }
        Random random = new Random();
        int rand = random.nextInt((int) delayTime);
        return rand < delayTime / 3 ? delayTime / 3 : rand;
    }
}
