package com.moko.support;

public class MokoConstants {// 读取发送头
    public static final int HEADER_READ_SEND = 0xB0;
    // 读取接收头
    public static final int HEADER_READ_GET = 0xB1;
    // 设置发送头
    public static final int HEADER_WRITE_SEND = 0xB2;
    // 设置接收头
    public static final int HEADER_WRITE_GET = 0xB3;
    // 通知接收头
    public static final int HEADER_NOTIFY = 0xB4;
    // 开关状态通知
    public static final int NOTIFY_FUNCTION_SWITCH = 0x01;
    // 负载检测通知
    public static final int NOTIFY_FUNCTION_LOAD = 0x02;
    // 过载保护通知
    public static final int NOTIFY_FUNCTION_OVERLOAD = 0x03;
    // 倒计时通知
    public static final int NOTIFY_FUNCTION_COUNTDOWN = 0x04;
    // 当前电压、电流、功率通知
    public static final int NOTIFY_FUNCTION_ELECTRICITY = 0x05;
    // 当前电能数据通知
    public static final int NOTIFY_FUNCTION_ENERGY = 0x06;
    // 发现状态
    public static final String ACTION_DISCOVER_SUCCESS = "com.moko.bluetoothplug.ACTION_DISCOVER_SUCCESS";
    public static final String ACTION_DISCOVER_TIMEOUT = "com.moko.bluetoothplug.ACTION_DISCOVER_TIMEOUT";

    // 断开连接
    public static final String ACTION_CONN_STATUS_DISCONNECTED = "com.moko.bluetoothplug.ACTION_CONN_STATUS_DISCONNECTED";
    // 命令结果
    public static final String ACTION_ORDER_RESULT = "com.moko.bluetoothplug.ACTION_ORDER_RESULT";
    public static final String ACTION_ORDER_TIMEOUT = "com.moko.bluetoothplug.ACTION_ORDER_TIMEOUT";
    public static final String ACTION_ORDER_FINISH = "com.moko.bluetoothplug.ACTION_ORDER_FINISH";

    // extra_key
    public static final String EXTRA_KEY_RESPONSE_ORDER_TASK = "EXTRA_KEY_RESPONSE_ORDER_TASK";
}
