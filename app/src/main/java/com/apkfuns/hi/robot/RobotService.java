package com.apkfuns.hi.robot;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.PendingIntent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.apkfuns.hi.robot.model.MessageType;
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
    private static final String CLASS_CHAT = "com.baidu.hi.activities.Chat";
    // 红包界面
    private static final String CLASS_LUCKY_MONEY = "com.baidu.hi.luckymoney.LuckyMoneyActivity";
    // 最近会话
    private static final String CLASS_CONVERSATION = "com.baidu.hi.ui.MainActivity";

    // 上一个聊天记录
    private List<AccessibilityNodeInfo> prevFetchList = new ArrayList<>();
    // 上一个记录红包个数
    private int prevPackageCount = 0;
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
                if (Constant.currentActivity.equals(CLASS_CHAT)) {
                    getPacket();
                } else if (Constant.currentActivity.equals(CLASS_CONVERSATION)) {
                    intoChat();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Constant.isDebug = event.getPackageName().equals(Constant.HI_PACKAGE_DEBUG);
                Constant.currentActivity = event.getClassName().toString();
                if (Constant.currentActivity.equals(CLASS_LUCKY_MONEY)) {
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
        List<AccessibilityNodeInfo> messageList = rootNode.findAccessibilityNodeInfosByViewId(
                Constant.getTextViewMessage());
        if (messageList == null || messageList.isEmpty()) {
            return;
        }
        for (AccessibilityNodeInfo msgNode : messageList) {
            if (msgNode != null && (msgNode.getText().toString().contains("[百度红包]")
            || msgNode.getText().toString().contains("[点赞红包]"))) {
                if (msgNode.getParent() != null) {
                    msgNode.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    break;
                }
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
            AccessibilityNodeInfo openLucky = NodeUtils.findNodeById(nodeInfo, Constant.getEnvelopeOpen());
            if (openLucky != null) {
                performGlobalAction(GLOBAL_ACTION_BACK);
                openLucky.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            } else {
                // 关闭页面
                AccessibilityNodeInfo closeNode = null;
                if ((closeNode = NodeUtils.findNodeById(nodeInfo, Constant.getCloseBtn())) != null
                        || (closeNode = NodeUtils.findNodeById(nodeInfo, Constant.getBtnClose())) != null) {
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
        AccessibilityNodeInfo listNode = NodeUtils.findNodeById(rootNode, Constant.getChatListView());
        int packageCount = rootNode.findAccessibilityNodeInfosByViewId(Constant.getLuckyMoneyTitle()).size();
        if (listNode == null || listNode.getChildCount() == 0) {
            return;
        } else if (packageCount != prevPackageCount) {
            // 页面中红包个数不相等
            openPacket(rootNode);
            prevPackageCount = packageCount;
        } else {
            if (prevFetchList.size() == 0) {
                openPacket(rootNode);
            } else {
                if (prevFetchList.size() != listNode.getChildCount()) {
                    if (prevFetchList.size() == listNode.getChildCount() - 1
                            && MessageType.getMsgType(listNode.getChild(listNode.getChildCount() - 1))
                            != MessageType.MSG_PACKAGE) {
                        // 新增一条记录而且不是红包就不抢
                    } else {
                        openPacket(rootNode);
                    }
                } else {
                    if (listNode.getChildCount() == 1) {
                        if (!NodeUtils.isSame(listNode.getChild(0), prevFetchList.get(0))) {
                            openPacket(rootNode);
                        } else {
                        }
                    } else {
                        int last = listNode.getChildCount() - 1;
                        if (!NodeUtils.isSame(listNode.getChild(0), prevFetchList.get(0))
                                || !NodeUtils.isSame(listNode.getChild(last), prevFetchList.get(last))) {
                            openPacket(rootNode);
                        } else {
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
        AccessibilityNodeInfo notifyText = NodeUtils.findNodeById(rootNode, Constant.getMsgNotifyInfo());
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
        List<AccessibilityNodeInfo> nodes = rootNode.findAccessibilityNodeInfosByViewId(
                Constant.getLuckyMoneyTitle());
        nodes.addAll(rootNode.findAccessibilityNodeInfosByViewId(Constant.getResId("like_lm_msg")));
        // 倒序，先抢最新的红包
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
