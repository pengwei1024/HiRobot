package com.apkfuns.hi.robot;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.apkfuns.hi.robot.model.Ids;
import com.apkfuns.hi.robot.utils.NodeUtils;
import com.apkfuns.hi.robot.utils.PowerUtil;
import com.apkfuns.logutils.LogUtils;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by pengwei on 16/2/2.
 */
public class RobotService extends AccessibilityService {

    // 聊天界面
    private static final String CHAT_CLASS_NAME = "com.baidu.hi.activities.Chat";
    // 红包界面
    private static final String LUCKY_MONEY_CLASS_NAME = "com.baidu.hi.luckymoney.LuckyMoneyActivity";
    // 聊天列表
    private static final String CONTACT_CLASS_NAME = "com.baidu.hi.activities.Contact";

    // 上一个聊天记录
    private List<AccessibilityNodeInfo> prevFetchList = new ArrayList<>();
    // 上一个记录红包个数
    private int prevPackageCount = 0;
    // 当前activity的className
    private String currentClassName = getClass().toString();
    // 熄屏管理
    private PowerUtil power;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        power = new PowerUtil(this);
        power.handleWakeLock(true);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (currentClassName.equals(CHAT_CLASS_NAME)) {
                    getPacket();
                } else if (currentClassName.equals(CONTACT_CLASS_NAME)) {
                    intoChat();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                currentClassName = event.getClassName().toString();
                if (currentClassName.equals(LUCKY_MONEY_CLASS_NAME)) {
                    openPacketDetail();
                }
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                listenerNotification(event);
                break;
            default:
                break;
        }
    }

    /**
     * 监听通知栏变化
     *
     * @param event
     */
    private void listenerNotification(AccessibilityEvent event) {
        List<CharSequence> texts = event.getText();
        if (texts != null && !texts.isEmpty()) {
            for (CharSequence text : texts) {
                if (text.toString().contains("[百度红包]")) {
                    if (event.getParcelableData() != null &&
                            event.getParcelableData() instanceof Notification) {
                        Notification notification = (Notification) event.getParcelableData();
                        PendingIntent pendingIntent = notification.contentIntent;
                        try {
                            pendingIntent.send();
                        } catch (PendingIntent.CanceledException e) {
                            LogUtils.e(e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 进去聊天界面
     */
    private void intoChat() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        List<AccessibilityNodeInfo> messageList = rootNode.findAccessibilityNodeInfosByViewId(Ids.TEXT_VIEW_MESSAGE);
        for (AccessibilityNodeInfo msgNode : messageList) {
            if (msgNode.getText().toString().contains(": [百度红包]")) {
                msgNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
            }
        }
    }


    /**
     * 打开红包
     * 1.能抢，点击拆红包并退出页面
     * 2.完成
     * 2.1 没抢到，退出页面
     * 2.2 抢过的，退出页面
     */
    private void openPacketDetail() {
        final AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            // 拆红包
            AccessibilityNodeInfo openLucky = NodeUtils.findNodeById(nodeInfo, Ids.ENVELOPE_OPEN);
            if (openLucky != null) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                openLucky.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } else {
                // 关闭页面
                AccessibilityNodeInfo closeNode = null;
                if ((closeNode = NodeUtils.findNodeById(nodeInfo, Ids.CLOSE_BTN)) != null
                        || (closeNode = NodeUtils.findNodeById(nodeInfo, Ids.BTN_CLOSE)) != null) {
                    closeNode.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
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

    /**
     * 获取红包列表
     */
    private void getPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        AccessibilityNodeInfo listNode = NodeUtils.findNodeById(rootNode, Ids.CHAT_LIST_VIEW);
        int packageCount = rootNode.findAccessibilityNodeInfosByViewId(Ids.LUCKY_MONEY_TITLE).size();
        if (rootNode == null || listNode == null || listNode.getChildCount() == 0) {
            return;
        } else if (packageCount != prevPackageCount) {
            // 页面中红包个数不相等
            openPacket(rootNode);
            prevPackageCount = packageCount;
        } else {
            if (prevFetchList.size() == 0) {
                LogUtils.e("*1");
                openPacket(rootNode);
            } else {
                if (prevFetchList.size() != listNode.getChildCount()) {
                    openPacket(rootNode);
                    LogUtils.e("*2");
                } else {
                    if (listNode.getChildCount() == 1) {
                        if (!NodeUtils.isSame(listNode.getChild(0), prevFetchList.get(0))) {
                            openPacket(rootNode);
                            LogUtils.e("*3");
                        } else {
                            LogUtils.e("*7");
                        }
                    } else {
                        int last = listNode.getChildCount() - 1;
                        if (!NodeUtils.isSame(listNode.getChild(0), prevFetchList.get(0))
                                || !NodeUtils.isSame(listNode.getChild(last), prevFetchList.get(last))) {
                            openPacket(rootNode);
                            LogUtils.e("*4");
                        } else {
                            LogUtils.e("*5");
                        }
                    }
                }
            }
            prevFetchList.clear();
            for (int i = 0; i < listNode.getChildCount(); ++i) {
                prevFetchList.add(listNode.getChild(i));
            }
        }
        // 顶部消息通知
        AccessibilityNodeInfo notifyText = NodeUtils.findNodeById(rootNode, Ids.MSG_NOTIFY_INFO);
        if (notifyText != null && notifyText.getText().toString().contains("[百度红包]")) {
            notifyText.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 打开红包
     *
     * @param rootNode
     */
    private void openPacket(AccessibilityNodeInfo rootNode) {
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(Ids.LUCKY_MONEY_TITLE);
        // 倒顺，先抢最新的红包
        for (int i = nodes.size() - 1; i >= 0; i--) {
            recycle(nodes.get(i));
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

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onDestroy() {
        power.handleWakeLock(false);
        super.onDestroy();
    }
}
