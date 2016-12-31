package com.apkfuns.hi.robot;

/**
 * Created by pengwei on 16/2/6.
 */
public final class Constant {

    // 是否为debug包
    public static boolean isDebug = false;
    // 当前的activity
    public static String currentActivity = "";

    // 包名
    public static final String HI_PACKAGE = "com.baidu.hi";
    public static final String HI_PACKAGE_DEBUG = HI_PACKAGE + ".debug";

    /**
     * 获取包名
     * @return
     */
    public static String getHiPackage() {
        return isDebug ? HI_PACKAGE_DEBUG : HI_PACKAGE;
    }

    /**
     * 获取资源ID
     * @return
     */
    public static String getResId(String id) {
        return getHiPackage() + ":id/" + id;
    }

    // 红包标题
    public static String getLuckyMoneyTitle() {
        return getResId("lucky_money_title");
    }

    // 发送内容
    public static String getChatLeftContent () {
        return getResId("chat_item_left_text_content");
    }

    // 红包领取提示
    public static String getLuckyMoneyNotify() {
        return getResId("chat_item_lucky_money_notify");
    }

    // 已读提示
    public static String getUnreadLine() {
        return getResId("chat_item_text_unread_line");
    }

    // 日期提示
    public static String getTextDate() {
        return getResId("chat_item_text_date");
    }

    // 打开红包按钮
    public static String getEnvelopeOpen() {
        return getResId("envelope_open");
    }

    // 关闭按钮
    public static String getCloseBtn() {
        return getResId("close_btn");
    }
    public static String getBtnClose() {
        return getResId("btn_close");
    }

    // 聊天列表
    public static String getChatListView() {
        return getResId("chat_listview");
    }

    // 通知栏文本
    public static String getMsgNotifyInfo() {
        return getResId("chat_msg_notification_info");
    }

    // 聊天列表提示
    public static String getTextViewMessage() {
        return getResId("tv_message");
    }
}
