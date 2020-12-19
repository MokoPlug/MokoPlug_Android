package com.moko.support;


import com.moko.support.task.OrderTask;
import com.moko.support.task.ReadAdvIntervalTask;
import com.moko.support.task.ReadAdvNameTask;
import com.moko.support.task.ReadCountdownTask;
import com.moko.support.task.ReadElectricityConstantTask;
import com.moko.support.task.ReadElectricityTask;
import com.moko.support.task.ReadEnergyHistoryTask;
import com.moko.support.task.ReadEnergyHistoryTodayTask;
import com.moko.support.task.ReadEnergySavedParamsTask;
import com.moko.support.task.ReadEnergyTotalTask;
import com.moko.support.task.ReadFirmwareVersionTask;
import com.moko.support.task.ReadLoadStateTask;
import com.moko.support.task.ReadMacTask;
import com.moko.support.task.ReadOverloadTopValueTask;
import com.moko.support.task.ReadOverloadValueTask;
import com.moko.support.task.ReadPowerStateTask;
import com.moko.support.task.ReadSwitchStateTask;
import com.moko.support.task.WriteAdvIntervalTask;
import com.moko.support.task.WriteAdvNameTask;
import com.moko.support.task.WriteCountdownTask;
import com.moko.support.task.WriteEnergySavedParamsTask;
import com.moko.support.task.WriteOverloadTopValueTask;
import com.moko.support.task.WritePowerStateTask;
import com.moko.support.task.WriteResetEnergyTotalTask;
import com.moko.support.task.WriteResetTask;
import com.moko.support.task.WriteSwitchStateTask;
import com.moko.support.task.WriteSystemTimeTask;

public class OrderTaskAssembler {
    public static OrderTask readAdvInterval() {
        ReadAdvIntervalTask task = new ReadAdvIntervalTask();
        return task;
    }

    public static OrderTask readAdvName() {
        ReadAdvNameTask task = new ReadAdvNameTask();
        return task;
    }

    public static OrderTask readCountdown() {
        ReadCountdownTask task = new ReadCountdownTask();
        return task;
    }

    public static OrderTask readElectricity() {
        ReadElectricityTask task = new ReadElectricityTask();
        return task;
    }

    public static OrderTask readEnergyHistory() {
        ReadEnergyHistoryTask task = new ReadEnergyHistoryTask();
        return task;
    }

    public static OrderTask readEnergyHistoryToday() {
        ReadEnergyHistoryTodayTask task = new ReadEnergyHistoryTodayTask();
        return task;
    }

    public static OrderTask readEnergySavedParams() {
        ReadEnergySavedParamsTask task = new ReadEnergySavedParamsTask();
        return task;
    }

    public static OrderTask readEnergyTotal() {
        ReadEnergyTotalTask task = new ReadEnergyTotalTask();
        return task;
    }

    public static OrderTask readFirmwareVersion() {
        ReadFirmwareVersionTask task = new ReadFirmwareVersionTask();
        return task;
    }

    public static OrderTask readLoadState() {
        ReadLoadStateTask task = new ReadLoadStateTask();
        return task;
    }

    public static OrderTask readMac() {
        ReadMacTask task = new ReadMacTask();
        return task;
    }

    public static OrderTask readOverloadTopValue() {
        ReadOverloadTopValueTask task = new ReadOverloadTopValueTask();
        return task;
    }

    public static OrderTask readOverloadValue() {
        ReadOverloadValueTask task = new ReadOverloadValueTask();
        return task;
    }

    public static OrderTask readPowerState() {
        ReadPowerStateTask task = new ReadPowerStateTask();
        return task;
    }

    public static OrderTask readSwitchState() {
        ReadSwitchStateTask task = new ReadSwitchStateTask();
        return task;
    }

    public static OrderTask readElectricityConstant() {
        ReadElectricityConstantTask task = new ReadElectricityConstantTask();
        return task;
    }

    public static OrderTask writeAdvInterval(int advInterval) {
        WriteAdvIntervalTask task = new WriteAdvIntervalTask();
        task.setData(advInterval);
        return task;
    }

    public static OrderTask writeAdvName(String advName) {
        WriteAdvNameTask task = new WriteAdvNameTask();
        task.setData(advName);
        return task;
    }

    public static OrderTask writeCountdown(int countdown) {
        WriteCountdownTask task = new WriteCountdownTask();
        task.setData(countdown);
        return task;
    }

    public static OrderTask writeEnergySavedParams(int savedInterval, int changed) {
        WriteEnergySavedParamsTask task = new WriteEnergySavedParamsTask();
        task.setData(savedInterval, changed);
        return task;
    }

    public static OrderTask writeOverloadTopValue(int topValue) {
        WriteOverloadTopValueTask task = new WriteOverloadTopValueTask();
        task.setData(topValue);
        return task;
    }

    public static OrderTask writePowerState(int powerState) {
        WritePowerStateTask task = new WritePowerStateTask();
        task.setData(powerState);
        return task;
    }

    public static OrderTask writeResetEnergyTotal() {
        WriteResetEnergyTotalTask task = new WriteResetEnergyTotalTask();
        return task;
    }

    public static OrderTask writeReset() {
        WriteResetTask task = new WriteResetTask();
        return task;
    }

    public static OrderTask writeSwitchState(int switchState) {
        WriteSwitchStateTask task = new WriteSwitchStateTask();
        task.setData(switchState);
        return task;
    }

    public static OrderTask writeSystemTime() {
        WriteSystemTimeTask task = new WriteSystemTimeTask();
        return task;
    }
}
