package com.apkfuns.hi.robot.model;

import android.view.accessibility.AccessibilityNodeInfo;

import com.apkfuns.hi.robot.utils.NodeUtils;

/**
 * Created by pengwei on 16/2/4.
 * 消息类型
 */
public class MessageType {

    // 文本，图片，文件
    public static final int MSG_TEXT = 1;
    // 历史消息
    public static final int MSG_HISTORY = 3;
    // 红包
    public static final int MSG_PACKAGE = 4;
    // 红包结果
    public static final int MSG_PACKAGE_RESULT = 5;
    // 时间
    public static final int MSG_TIME = 6;

    /**
     * 获取结点类型
     *
     * @param node
     * @return
     */
    public static int getMsgType(AccessibilityNodeInfo node) {
        if (NodeUtils.exist(node, "com.baidu.hi:id/chat_item_lucky_money_notify")) {
            return MSG_PACKAGE_RESULT;
        } else if (NodeUtils.exist(node, "com.baidu.hi:id/lucky_money_title")) {
            return MSG_PACKAGE;
        } else if (NodeUtils.exist(node, "com.baidu.hi:id/chat_item_text_unread_line")) {
            return MSG_HISTORY;
        } else if (NodeUtils.exist(node, "com.baidu.hi:id/chat_item_text_date")) {
            return MSG_TIME;
        }
        return MSG_TEXT;
    }


}
