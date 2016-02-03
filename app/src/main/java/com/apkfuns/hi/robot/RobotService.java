package com.apkfuns.hi.robot;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

/**
 * Created by pengwei on 16/2/2.
 */
public class RobotService extends AccessibilityService {

    private static final String TAG = "RobotService";
    private boolean currentIsChatActivity = false;
    private static final String CHAT_CLASS_NAME = "com.baidu.hi.activities.Chat";

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        Log.e(TAG, String.valueOf(eventType));
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                Log.e(TAG, String.valueOf(currentIsChatActivity));
                if (currentIsChatActivity) {
                    getPacket();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                currentIsChatActivity = className.equals(CHAT_CLASS_NAME);
                break;
            default:
                break;
        }
    }

    /**
     * toast提示
     *
     * @param msg
     */
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

    }

    @SuppressLint("NewApi")
    private void getPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        rootNode.findAccessibilityNodeInfosByText("");
        recycle(rootNode);
    }

    @SuppressLint("NewApi")
    public void recycle(AccessibilityNodeInfo info) {
        if (info.getChildCount() == 0) {
            if (info.getText() != null) {
                Log.e(TAG, info.getText().toString());
            }
        } else {
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i));
                }
            }
        }
    }

    @Override
    public void onInterrupt() {

    }
}
