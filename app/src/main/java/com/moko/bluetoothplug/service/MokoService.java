package com.moko.bluetoothplug.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;

import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoConnStateCallback;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.handler.BaseMessageHandler;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;
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

import org.greenrobot.eventbus.EventBus;


/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.bluetoothplug.service.MokoService
 */
public class MokoService extends Service implements MokoConnStateCallback, MokoOrderTaskCallback {

    @Override
    public void onConnectSuccess() {
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCOVER_SUCCESS);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public void onDisConnected() {
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_CONN_STATUS_DISCONNECTED);
        EventBus.getDefault().post(connectStatusEvent);
    }

    @Override
    public void onOrderResult(OrderTaskResponse response) {
        Intent intent = new Intent(new Intent(MokoConstants.ACTION_ORDER_RESULT));
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK, response);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderTimeout(OrderTaskResponse response) {
        Intent intent = new Intent(new Intent(MokoConstants.ACTION_ORDER_TIMEOUT));
        intent.putExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK, response);
        sendOrderedBroadcast(intent, null);
    }

    @Override
    public void onOrderFinish() {
        sendOrderedBroadcast(new Intent(MokoConstants.ACTION_ORDER_FINISH), null);
    }

    @Override
    public void onCreate() {
        LogModule.v("创建MokoService...onCreate");
        mHandler = new ServiceHandler(this);
        super.onCreate();
    }

    public void connectBluetoothDevice(String address) {
        MokoSupport.getInstance().connDevice(this, address, this);
    }

    /**
     * @Date 2017/5/23
     * @Author wenzheng.liu
     * @Description 断开手环
     */
    public void disConnectBle() {
        MokoSupport.getInstance().disConnectBle();
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LogModule.v("启动MokoService...onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    private IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        LogModule.v("绑定MokoService...onBind");
        return mBinder;
    }

    @Override
    public void onLowMemory() {
        LogModule.v("内存吃紧，销毁MokoService...onLowMemory");
        disConnectBle();
        super.onLowMemory();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogModule.v("解绑MokoService...onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        LogModule.v("销毁MokoService...onDestroy");
        disConnectBle();
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        public MokoService getService() {
            return MokoService.this;
        }
    }

    public ServiceHandler mHandler;

    public class ServiceHandler extends BaseMessageHandler<MokoService> {

        public ServiceHandler(MokoService service) {
            super(service);
        }

        @Override
        protected void handleMessage(MokoService service, Message msg) {
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    public OrderTask readAdvInterval() {
        ReadAdvIntervalTask task = new ReadAdvIntervalTask(this);
        return task;
    }

    public OrderTask readAdvName() {
        ReadAdvNameTask task = new ReadAdvNameTask(this);
        return task;
    }

    public OrderTask readCountdown() {
        ReadCountdownTask task = new ReadCountdownTask(this);
        return task;
    }

    public OrderTask readElectricity() {
        ReadElectricityTask task = new ReadElectricityTask(this);
        return task;
    }

    public OrderTask readEnergyHistory() {
        ReadEnergyHistoryTask task = new ReadEnergyHistoryTask(this);
        return task;
    }

    public OrderTask readEnergyHistoryToday() {
        ReadEnergyHistoryTodayTask task = new ReadEnergyHistoryTodayTask(this);
        return task;
    }

    public OrderTask readEnergySavedParams() {
        ReadEnergySavedParamsTask task = new ReadEnergySavedParamsTask(this);
        return task;
    }

    public OrderTask readEnergyTotal() {
        ReadEnergyTotalTask task = new ReadEnergyTotalTask(this);
        return task;
    }

    public OrderTask readFirmwareVersion() {
        ReadFirmwareVersionTask task = new ReadFirmwareVersionTask(this);
        return task;
    }

    public OrderTask readLoadState() {
        ReadLoadStateTask task = new ReadLoadStateTask(this);
        return task;
    }

    public OrderTask readMac() {
        ReadMacTask task = new ReadMacTask(this);
        return task;
    }

    public OrderTask readOverloadTopValue() {
        ReadOverloadTopValueTask task = new ReadOverloadTopValueTask(this);
        return task;
    }

    public OrderTask readOverloadValue() {
        ReadOverloadValueTask task = new ReadOverloadValueTask(this);
        return task;
    }

    public OrderTask readPowerState() {
        ReadPowerStateTask task = new ReadPowerStateTask(this);
        return task;
    }

    public OrderTask readSwitchState() {
        ReadSwitchStateTask task = new ReadSwitchStateTask(this);
        return task;
    }

    public OrderTask readElectricityConstant() {
        ReadElectricityConstantTask task = new ReadElectricityConstantTask(this);
        return task;
    }

    public OrderTask writeAdvInterval(int advInterval) {
        WriteAdvIntervalTask task = new WriteAdvIntervalTask(this);
        task.setData(advInterval);
        return task;
    }

    public OrderTask writeAdvName(String advName) {
        WriteAdvNameTask task = new WriteAdvNameTask(this);
        task.setData(advName);
        return task;
    }

    public OrderTask writeCountdown(int countdown) {
        WriteCountdownTask task = new WriteCountdownTask(this);
        task.setData(countdown);
        return task;
    }

    public OrderTask writeEnergySavedParams(int savedInterval, int changed) {
        WriteEnergySavedParamsTask task = new WriteEnergySavedParamsTask(this);
        task.setData(savedInterval, changed);
        return task;
    }

    public OrderTask writeOverloadTopValue(int topValue) {
        WriteOverloadTopValueTask task = new WriteOverloadTopValueTask(this);
        task.setData(topValue);
        return task;
    }

    public OrderTask writePowerState(int powerState) {
        WritePowerStateTask task = new WritePowerStateTask(this);
        task.setData(powerState);
        return task;
    }

    public OrderTask writeResetEnergyTotal() {
        WriteResetEnergyTotalTask task = new WriteResetEnergyTotalTask(this);
        return task;
    }

    public OrderTask writeReset() {
        WriteResetTask task = new WriteResetTask(this);
        return task;
    }

    public OrderTask writeSwitchState(int switchState) {
        WriteSwitchStateTask task = new WriteSwitchStateTask(this);
        task.setData(switchState);
        return task;
    }

    public OrderTask writeSystemTime() {
        WriteSystemTimeTask task = new WriteSystemTimeTask(this);
        return task;
    }
}
