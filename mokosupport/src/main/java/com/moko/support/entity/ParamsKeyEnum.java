package com.moko.support.entity;

import java.io.Serializable;


public enum ParamsKeyEnum implements Serializable {

    GET_ADV_NAME(0x01),
    GET_ADV_INTERVAL(0x02),
    GET_SWITCH_STATE(0x03),
    GET_POWER_STATE(0x04),
    GET_LOAD_STATE(0x05),
    GET_OVERLOAD_TOP_VALUE(0x06),
    GET_ELECTRICITY_VALUE(0x07),
    GET_ENERGY_TOTAL(0x08),
    GET_COUNTDOWN(0x09),
    GET_FIRMWARE_VERISON(0x0A),
    GET_MAC(0x0B),
    GET_ENERGY_SAVED_PARAMS(0x0C),
    GET_ENERGY_HISTORY(0x0D),
    GET_ENERGY_HISTORY_DATA(0x0E),
    GET_OVERLOAD_VALUE(0x10),
    GET_ENERGY_HISTORY_TODAY(0x11),
    GET_ENERGY_HISTORY_TODAY_DATA(0x12),
    GET_ELECTRICITY_CONSTANT(0x13),

    SET_ADV_NAME(0x01),
    SET_ADV_INTERVAL(0x02),
    SET_SWITCH_STATE(0x03),
    SET_POWER_STATE(0x04),
    SET_OVERLOAD_TOP_VALUE(0x05),
    SET_RESET_ENERGY_TOTAL(0x06),
    SET_COUNTDOWN(0x07),
    SET_RESET(0x08),
    SET_ENERGY_SAVED_PARAMS(0x09),
    SET_SYSTEM_TIME(0x0A),
    ;


    private int paramsKey;

    ParamsKeyEnum(int paramsKey) {
        this.paramsKey = paramsKey;
    }


    public int getParamsKey() {
        return paramsKey;
    }

    public static ParamsKeyEnum fromParamKey(int paramsKey) {
        for (ParamsKeyEnum paramsKeyEnum : ParamsKeyEnum.values()) {
            if (paramsKeyEnum.getParamsKey() == paramsKey) {
                return paramsKeyEnum;
            }
        }
        return null;
    }
}
