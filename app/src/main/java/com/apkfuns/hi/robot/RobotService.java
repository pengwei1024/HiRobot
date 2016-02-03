package com.apkfuns.hi.robot;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by pengwei on 16/2/2.
 */
public class RobotService extends AccessibilityService {

    private static final String TAG = "RobotService";
    private boolean currentIsChatActivity = false;

    // 聊天界面
    private static final String CHAT_CLASS_NAME = "com.baidu.hi.activities.Chat";
    // 红包界面
    private static final String LUCKY_MONEY_CLASS_NAME = "com.baidu.hi.luckymoney.LuckyMoneyActivity";

    // 待领取红包列表
    private List<AccessibilityNodeInfo> nodesToFetch = new ArrayList<>();
    // 已经获取红包列表
    private List<String> fetchedIdentifiers = new ArrayList<>();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (currentIsChatActivity) {
                    getPacket();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                String className = event.getClassName().toString();
                currentIsChatActivity = className.equals(CHAT_CLASS_NAME);
                if (className.equals(LUCKY_MONEY_CLASS_NAME)) {
                    openPacket();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 打开红包
     * 1.能抢，点击拆红包并退出页面
     * 2.完成
     * 2.1 没抢到，退出页面
     * 2.2 抢过的，退出页面
     */
    private void openPacket() {
        final AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            // 拆红包
            AccessibilityNodeInfo openLucky = findNodeById(nodeInfo, "com.baidu.hi:id/envelope_open");
            if (openLucky != null) {
                performGlobalAction(GLOBAL_ACTION_BACK);
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
     * 根据id查找结点，存在返回不存在返回null
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

    private void getPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId("com.baidu.hi:id/lucky_money_title");
        for (AccessibilityNodeInfo currentNode : nodes) {
            String nodeId = getNodeId(currentNode);
            if (!fetchedIdentifiers.contains(nodeId)) {
                recycle(currentNode);
                fetchedIdentifiers.add(nodeId);
            }
        }
    }

    public void recycle(AccessibilityNodeInfo info) {
        if (info != null) {
            info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            AccessibilityNodeInfo parent = info.getParent();
            while (parent != null) {
                if (parent.isClickable()) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
                parent = parent.getParent();
            }
        }
    }

    /**
     * 获取结点唯一标识符
     *
     * @param node
     * @return
     */
    private String getNodeId(AccessibilityNodeInfo node) {
        Pattern pattern = Pattern.compile("(?<=@)[0-9|a-z]+(?=;)");
        Matcher matcher = pattern.matcher(node.toString());
        matcher.find();
        return matcher.group(0);
    }

    @Override
    public void onInterrupt() {

    }
}
