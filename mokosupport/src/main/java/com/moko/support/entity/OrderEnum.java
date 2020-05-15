package com.moko.support.entity;

import java.io.Serializable;

/**
 * @Date 2017/5/11
 * @Author wenzheng.liu
 * @Description 命令枚举
 * @ClassPath com.fitpolo.support.entity.OrderEnum
 */
public enum OrderEnum implements Serializable {
    READ_NOTIFY("打开读取通知", 0),
    WRITE_NOTIFY("打开设置通知", 0),
    NOTIFY("打开通知", 0),

    READ_ADV_NAME("读取广播名字", 0x01),
    READ_ADV_INTERVAL("读取广播间隔", 0x02),
    READ_SWITCH_STATE("读取开关状态", 0x03),
    READ_POWER_STATE("读取上电状态", 0x04),
    READ_LOAD_STATE("读取负载状态", 0x05),
    READ_OVERLOAD_TOP_VALUE("读取过载保护值", 0x06),
    READ_ELECTRICITY_VALUE("读取电压电流功率值", 0x07),
    READ_ENERGY_TOTAL("读取累计电能", 0x08),
    READ_COUNTDOWN("读取倒计时", 0x09),
    READ_FIRMWARE_VERISON("读取固件版本", 0x0A),
    READ_MAC("读取MAC地址", 0x0B),
    READ_ENERGY_SAVED_PARAMS("读取累计电能存储参数", 0x0C),
    READ_ENERGY_HISTORY("读取历史累计电能", 0x0D),
    READ_OVERLOAD_VALUE("读取过载状态", 0x10),
    READ_ENERGY_HISTORY_TODAY("读取当天每小时数据", 0x11),
    READ_ELECTRICITY_CONSTANT("读取脉冲常数", 0x13),

    WRITE_ADV_NAME("设置广播名字", 0x01),
    WRITE_ADV_INTERVAL("设置广播间隔", 0x02),
    WRITE_SWITCH_STATE("设置开关状态", 0x03),
    WRITE_POWER_STATE("设置上电状态", 0x04),
    WRITE_OVERLOAD_TOP_VALUE("设置过载保护值", 0x05),
    WRITE_RESET_ENERGY_TOTAL("重置累计电能", 0x06),
    WRITE_COUNTDOWN("设置倒计时", 0x07),
    WRITE_RESET("恢复出厂设置", 0x08),
    WRITE_ENERGY_SAVED_PARAMS("设置累计电能存储参数", 0x09),
    WRITE_SYSTEM_TIME("时间同步", 0x0A),
    ;


    private String orderName;
    private int orderHeader;

    OrderEnum(String orderName, int orderHeader) {
        this.orderName = orderName;
        this.orderHeader = orderHeader;
    }

    public int getOrderHeader() {
        return orderHeader;
    }

    public String getOrderName() {
        return orderName;
    }
}
