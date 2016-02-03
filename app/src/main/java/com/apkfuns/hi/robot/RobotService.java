package com.apkfuns.hi.robot;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengwei on 16/2/2.
 */
public class RobotService extends AccessibilityService {

    private static final String TAG = "RobotService";
    private boolean currentIsChatActivity = false;

    // 聊天界面
    private static final String CHAT_CLASS_NAME = "com.baidu.hi.activities.Chat";
    // 红包界面
    private static final String LUCK_MONEY_CLASS_NAME = "com.baidu.hi.luckymoney.LuckyMoneyActivity";

    // 待领取红包列表
    private List<AccessibilityNodeInfo> nodesToFetch = new ArrayList<>();
    // 已经获取红包列表
    private List<String> fetchedIdentifiers = new ArrayList<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        Log.e(TAG, String.valueOf(eventType));
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                Log.e(TAG, String.valueOf(currentIsChatActivity));
                if (currentIsChatActivity) {
//                    getPacket();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                currentIsChatActivity = className.equals(CHAT_CLASS_NAME);
                if (className.equals(LUCK_MONEY_CLASS_NAME)) {
                    openPacket();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 打开红包
     */
    @SuppressLint("NewApi")
    private void openPacket() {
        final AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        for (int i = 0; i < nodeInfo.getChildCount(); i++) {
            Log.e(TAG, nodeInfo.getChild(i).getClassName().toString());
        }
        if (nodeInfo != null) {
            // 拆红包
            AccessibilityNodeInfo openLucky = findNodeById(nodeInfo, "com.baidu.hi:id/envelope_open");
            if (openLucky != null) {
                openLucky.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } else {
                // 关闭页面
                AccessibilityNodeInfo closeNode = null;
                if ((closeNode = findNodeById(nodeInfo, "com.baidu.hi:id/close_btn")) != null
                        || (closeNode = findNodeById(nodeInfo, "com.baidu.hi:id/btn_close")) != null) {
                    closeNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
        }
    }

    /**
     * 结点是否存在
     *
     * @param nodeInfo
     * @param viewId
     * @return
     */
    private AccessibilityNodeInfo findNodeById(AccessibilityNodeInfo nodeInfo, String viewId) {
        if (nodeInfo == null || viewId == null) {
            return null;
        }
        List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(viewId);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
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
                if (info.getText().equals("百度红包")) {
                    info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        Log.e(TAG, "parent isClick:" + parent.isClickable());
                        if (parent.isClickable()) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }
                        parent = parent.getParent();
                    }
                }
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
