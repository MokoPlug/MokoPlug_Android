package com.moko.support;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.moko.support.callback.MokoResponseCallback;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.EnergyInfo;
import com.moko.support.entity.MokoCharacteristic;
import com.moko.support.entity.OrderType;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.event.DataChangedEvent;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.handler.MokoCharacteristicHandler;
import com.moko.support.handler.MokoLeScanHandler;
import com.moko.support.log.LogModule;
import com.moko.support.task.OrderTask;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * @Date 2017/12/7 0007
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.MokoSupport
 */
public class MokoSupport implements MokoResponseCallback {
    public static final UUID DESCRIPTOR_UUID_NOTIFY = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private MokoLeScanHandler mMokoLeScanHandler;
    private HashMap<OrderType, MokoCharacteristic> mCharacteristicMap;
    private BlockingQueue<OrderTask> mQueue;
    private MokoScanDeviceCallback mMokoScanDeviceCallback;

    private static volatile MokoSupport INSTANCE;

    private Context mContext;

    private MokoBleManager mokoBleManager;

    private Handler mHandler;

    private MokoSupport() {
        //no instance
        mQueue = new LinkedBlockingQueue<>();
    }

    public static MokoSupport getInstance() {
        if (INSTANCE == null) {
            synchronized (MokoSupport.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MokoSupport();
                }
            }
        }
        return INSTANCE;
    }

    public void init(Context context) {
        LogModule.init(context);
        mContext = context;
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mHandler = new Handler(Looper.getMainLooper());
        mokoBleManager = MokoBleManager.getMokoBleManager(context);
        mokoBleManager.setBeaconResponseCallback(this);
        mokoBleManager.setGattCallbacks(new BleManagerCallbacks() {
            @Override
            public void onDeviceConnecting(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onDeviceConnected(@NonNull BluetoothDevice device) {
            }

            @Override
            public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onDeviceDisconnected(@NonNull BluetoothDevice device) {
                if (isSyncData()) {
                    mQueue.clear();
                }
                ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
                connectStatusEvent.setAction(MokoConstants.ACTION_CONN_STATUS_DISCONNECTED);
                EventBus.getDefault().post(connectStatusEvent);
            }

            @Override
            public void onLinkLossOccurred(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {

            }

            @Override
            public void onDeviceReady(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onBondingRequired(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onBonded(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onBondingFailed(@NonNull BluetoothDevice device) {

            }

            @Override
            public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {

            }

            @Override
            public void onDeviceNotSupported(@NonNull BluetoothDevice device) {

            }
        });
    }

    public boolean isBluetoothOpen() {
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }

    public boolean isConnDevice(Context context, String address) {
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        int connState = bluetoothManager.getConnectionState(mBluetoothAdapter.getRemoteDevice(address), BluetoothProfile.GATT);
        return connState == BluetoothProfile.STATE_CONNECTED;
    }

    public void startScanDevice(MokoScanDeviceCallback mokoScanDeviceCallback) {
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            LogModule.i("Start scan");
        }
        final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        List<ScanFilter> scanFilterList = Collections.singletonList(new ScanFilter.Builder().build());
        mMokoLeScanHandler = new MokoLeScanHandler(mokoScanDeviceCallback);
        scanner.startScan(scanFilterList, settings, mMokoLeScanHandler);
        mMokoScanDeviceCallback = mokoScanDeviceCallback;
        mokoScanDeviceCallback.onStartScan();
    }

    public void stopScanDevice() {
        if (isBluetoothOpen() && mMokoLeScanHandler != null && mMokoScanDeviceCallback != null) {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                LogModule.i("End scan");
            }
            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(mMokoLeScanHandler);
            mMokoScanDeviceCallback.onStopScan();
            mMokoLeScanHandler = null;
            mMokoScanDeviceCallback = null;
        }
    }

    public void connDevice(final Context context, final String address) {
        if (TextUtils.isEmpty(address)) {
            LogModule.i("connDevice: address null");
            return;
        }
        if (!isBluetoothOpen()) {
            LogModule.i("connDevice: blutooth close");
            return;
        }
        if (isConnDevice(context, address)) {
            LogModule.i("connDevice: device connected");
            disConnectBle();
            return;
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    LogModule.i("start connect");
                    mokoBleManager.connect(device)
                            .retry(5, 200)
                            .timeout(50000)
                            .enqueue();
                }
            });
        } else {
            LogModule.i("the device is null");
        }
    }

    public void sendOrder(OrderTask... orderTasks) {
        if (orderTasks.length == 0) {
            return;
        }
        if (!isSyncData()) {
            for (OrderTask ordertask : orderTasks) {
                if (ordertask == null) {
                    continue;
                }
                mQueue.offer(ordertask);
            }
            executeTask();
        } else {
            for (OrderTask ordertask : orderTasks) {
                if (ordertask == null) {
                    continue;
                }
                mQueue.offer(ordertask);
            }
        }
    }

    public void executeTask() {
        if (!isSyncData()) {
            OrderTaskResponseEvent event = new OrderTaskResponseEvent();
            event.setAction(MokoConstants.ACTION_ORDER_FINISH);
            EventBus.getDefault().post(event);
            return;
        }
        if (mQueue.isEmpty()) {
            return;
        }
        final OrderTask orderTask = mQueue.peek();
        if (mBluetoothGatt == null) {
            LogModule.i("executeTask : BluetoothGatt is null");
            return;
        }
        if (orderTask == null) {
            LogModule.i("executeTask : orderTask is null");
            return;
        }
        if (mCharacteristicMap == null || mCharacteristicMap.isEmpty()) {
            LogModule.i("executeTask : characteristicMap is null");
            disConnectBle();
            return;
        }
        final MokoCharacteristic mokoCharacteristic = mCharacteristicMap.get(orderTask.orderType);
        if (mokoCharacteristic == null) {
            LogModule.i("executeTask : mokoCharacteristic is null");
            return;
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_READ) {
            sendReadOrder(orderTask, mokoCharacteristic);
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_WRITE) {
            sendWriteOrder(orderTask, mokoCharacteristic);
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE) {
            sendWriteNoResponseOrder(orderTask, mokoCharacteristic);
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_NOTIFY) {
            sendNotifyOrder(orderTask, mokoCharacteristic);
        }
        timeoutHandler(orderTask);
    }

    public synchronized boolean isSyncData() {
        return mQueue != null && !mQueue.isEmpty();
    }

    public void disConnectBle() {
        mokoBleManager.disconnect().enqueue();
    }

    public void enableBluetooth() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.enable();
        }
    }

    public void disableBluetooth() {
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.disable();
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    // 发送可监听命令
    private void sendNotifyOrder(OrderTask orderTask, final MokoCharacteristic mokoCharacteristic) {
        LogModule.i("app set device notify : " + orderTask.orderType.getName());
        mokoCharacteristic.characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        final BluetoothGattDescriptor descriptor = mokoCharacteristic.characteristic.getDescriptor(DESCRIPTOR_UUID_NOTIFY);
        if (descriptor == null) {
            return;
        }
        if ((mokoCharacteristic.characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else if ((mokoCharacteristic.characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) != 0) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeDescriptor(descriptor);
            }
        });
    }

    // 发送可写命令
    private void sendWriteOrder(OrderTask orderTask, final MokoCharacteristic mokoCharacteristic) {
        LogModule.i("app to device write : " + orderTask.orderType.getName());
        LogModule.i(MokoUtils.bytesToHexString(orderTask.assemble()));
        mokoCharacteristic.characteristic.setValue(orderTask.assemble());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(mokoCharacteristic.characteristic);
            }
        });
    }

    // 发送可写无应答命令
    private void sendWriteNoResponseOrder(OrderTask orderTask, final MokoCharacteristic mokoCharacteristic) {
        LogModule.i("app to device write no response : " + orderTask.orderType.getName());
        LogModule.i(MokoUtils.bytesToHexString(orderTask.assemble()));
        mokoCharacteristic.characteristic.setValue(orderTask.assemble());
        mokoCharacteristic.characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(mokoCharacteristic.characteristic);
            }
        });
    }

    // 发送可读命令
    private void sendReadOrder(OrderTask orderTask, final MokoCharacteristic mokoCharacteristic) {
        LogModule.i("app to device read : " + orderTask.orderType.getName());
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.readCharacteristic(mokoCharacteristic.characteristic);
            }
        });
    }

    // 发送自定义命令（无队列）
    public void sendCustomOrder(OrderTask orderTask) {
        final MokoCharacteristic mokoCharacteristic = mCharacteristicMap.get(orderTask.orderType);
        if (mokoCharacteristic == null) {
            LogModule.i("executeTask : mokoCharacteristic is null");
            return;
        }
        if (orderTask.response.responseType == OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE) {
            sendWriteNoResponseOrder(orderTask, mokoCharacteristic);
        }
    }

    // 直接发送命令(升级专用)
    public void sendDirectOrder(OrderTask orderTask) {
        final MokoCharacteristic mokoCharacteristic = mCharacteristicMap.get(orderTask.orderType);
        if (mokoCharacteristic == null) {
            LogModule.i("executeTask : mokoCharacteristic is null");
            return;
        }
        LogModule.i("app to device write no response : " + orderTask.orderType.getName());
        LogModule.i(MokoUtils.bytesToHexString(orderTask.assemble()));
        mokoCharacteristic.characteristic.setValue(orderTask.assemble());
        mokoCharacteristic.characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBluetoothGatt.writeCharacteristic(mokoCharacteristic.characteristic);
            }
        });
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public void onCharacteristicChanged(BluetoothGattCharacteristic characteristic, byte[] value) {
        OrderType orderType = null;
        if (characteristic.getUuid().toString().equals(OrderType.NOTIFY_CHARACTER.getUuid())) {
            // 通知
            orderType = OrderType.NOTIFY_CHARACTER;
        }
        // 延时应答
        if (orderType != null) {
            LogModule.i(orderType.getName());
            if (MokoConstants.HEADER_NOTIFY != (value[0] & 0xFF))
                return;
            DataChangedEvent event = new DataChangedEvent();
            final int function = value[1] & 0xFF;
            final int length = value[2] & 0xFF;
            switch (function) {
                case 1:
                    if (length != 1)
                        return;
                    final int switchState = value[3] & 0xFF;
                    this.switchState = switchState;
                    event.setFunction(MokoConstants.NOTIFY_FUNCTION_SWITCH);
                    break;
                case 2:
                    if (length != 1)
                        return;
                    if (1 == (value[3] & 0xFF))
                        event.setFunction(MokoConstants.NOTIFY_FUNCTION_LOAD);
                    break;
                case 3:
                    if (length != 2)
                        return;
                    this.overloadState = 1;
                    event.setFunction(MokoConstants.NOTIFY_FUNCTION_OVERLOAD);
                    break;
                case 4:
                    if (length != 9)
                        return;
                    byte[] countDownInitBytes = Arrays.copyOfRange(value, 4, 8);
                    final int countDownInit = MokoUtils.toIntUnsigned(countDownInitBytes);
                    MokoSupport.getInstance().countDownInit = countDownInit;
                    byte[] countDownBytes = Arrays.copyOfRange(value, 8, 12);
                    final int countDown = MokoUtils.toIntUnsigned(countDownBytes);
                    this.countDown = countDown;
                    event.setFunction(MokoConstants.NOTIFY_FUNCTION_COUNTDOWN);
                    break;
                case 5:
                    if (length == 7) {
                        byte[] vBytes = Arrays.copyOfRange(value, 3, 5);
                        final int v = MokoUtils.toIntUnsigned(vBytes);
                        MokoSupport.getInstance().electricityV = MokoUtils.getDecimalFormat("0.#").format(v * 0.1f);

                        byte[] cBytes = Arrays.copyOfRange(value, 5, 8);
                        final int c = MokoUtils.toIntUnsigned(cBytes);
                        MokoSupport.getInstance().electricityC = String.valueOf(c);

                        byte[] pBytes = Arrays.copyOfRange(value, 8, 10);
                        final int p = MokoUtils.toIntUnsigned(pBytes);
                        MokoSupport.getInstance().electricityP = MokoUtils.getDecimalFormat("0.#").format(p * 0.1f);
                    } else if (length == 10) {
                        byte[] vBytes = Arrays.copyOfRange(value, 3, 5);
                        final int v = MokoUtils.toIntUnsigned(vBytes);
                        MokoSupport.getInstance().electricityV = MokoUtils.getDecimalFormat("0.#").format(v * 0.1f);

                        byte[] cBytes = Arrays.copyOfRange(value, 5, 9);
                        final int c = MokoUtils.toIntSigned(cBytes);
                        MokoSupport.getInstance().electricityC = String.valueOf(c);

                        byte[] pBytes = Arrays.copyOfRange(value, 9, 13);
                        final int p = MokoUtils.toIntSigned(pBytes);
                        MokoSupport.getInstance().electricityP = MokoUtils.getDecimalFormat("0.#").format(p * 0.1f);
                    } else {
                        return;
                    }
                    event.setFunction(MokoConstants.NOTIFY_FUNCTION_ELECTRICITY);
                    break;
                case 6:
                    if (length != 17)
                        return;
                    EnergyInfo energyInfo = new EnergyInfo();
                    int year = MokoUtils.toIntUnsigned(Arrays.copyOfRange(value, 3, 5));
                    int month = value[5] & 0xFF;
                    int day = value[6] & 0xFF;
                    int hour = value[7] & 0xFF;
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month - 1);
                    calendar.set(Calendar.DAY_OF_MONTH, day);
                    calendar.set(Calendar.HOUR_OF_DAY, hour);
                    byte[] totalBytes = Arrays.copyOfRange(value, 8, 12);
                    long total = MokoUtils.longFrom8Bytes(totalBytes);
                    this.eneryTotal = total;

                    byte[] totalMonthlyBytes = Arrays.copyOfRange(value, 12, 15);
                    final int totalMonthly = MokoUtils.toIntUnsigned(totalMonthlyBytes);
                    this.eneryTotalMonthly = totalMonthly;

                    byte[] totalTodayBytes = Arrays.copyOfRange(value, 15, 18);
                    final int totalToday = MokoUtils.toIntUnsigned(totalTodayBytes);
                    this.eneryTotalToday = totalToday;

                    byte[] currentBytes = Arrays.copyOfRange(value, 18, 20);
                    final int current = MokoUtils.toIntUnsigned(currentBytes);
                    String energyCurrent = String.valueOf(current);

                    energyInfo.recordDate = MokoUtils.calendar2StrDate(calendar, "yyyy-MM-dd HH");
                    energyInfo.date = energyInfo.recordDate.substring(5, 10);
                    energyInfo.hour = energyInfo.recordDate.substring(11);
                    energyInfo.value = energyCurrent;
                    if (energyHistory != null) {
                        EnergyInfo first = energyHistory.get(0);
                        if (energyInfo.date.equals(first.date)) {
                            first.value = String.valueOf(eneryTotalToday);
                        } else {
                            energyInfo.type = 1;
                            energyInfo.value = String.valueOf(eneryTotalToday);
                            energyHistory.add(0, energyInfo);
                        }
                    } else {
                        energyHistory = new ArrayList<>();
                        energyInfo.type = 1;
                        energyHistory.add(energyInfo);
                    }
                    if (energyHistoryToday != null) {
                        EnergyInfo first = energyHistoryToday.get(0);
                        if (energyInfo.recordDate.equals(first.recordDate)) {
                            first.value = energyCurrent;
                        } else {
                            energyInfo.type = 0;
                            energyHistoryToday.add(0, energyInfo);
                        }
                    } else {
                        energyHistoryToday = new ArrayList<>();
                        energyInfo.type = 0;
                        energyHistoryToday.add(energyInfo);
                    }
                    event.setFunction(MokoConstants.NOTIFY_FUNCTION_ENERGY);
                    break;
            }
            event.setValue(value);
            EventBus.getDefault().post(event);
        } else {
            // 非延时应答
            OrderTask orderTask = mQueue.peek();
            String characteristicUuid = characteristic.getUuid().toString();
            if (value != null && value.length > 0 && orderTask != null) {
                if (characteristicUuid.equals(OrderType.READ_CHARACTER) && MokoConstants.HEADER_READ_GET != (value[0] & 0xFF)) {
                    return;
                }
                if (characteristicUuid.equals(OrderType.WRITE_CHARACTER) && MokoConstants.HEADER_WRITE_GET != (value[0] & 0xFF)) {
                    return;
                }
                orderTask.parseValue(value);
            }
        }

    }

    @Override
    public void onCharacteristicWrite(byte[] value) {

    }

    @Override
    public void onCharacteristicRead(byte[] value) {

    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt) {
        mBluetoothGatt = gatt;
        mCharacteristicMap = MokoCharacteristicHandler.getInstance().getCharacteristics(gatt);
        ConnectStatusEvent connectStatusEvent = new ConnectStatusEvent();
        connectStatusEvent.setAction(MokoConstants.ACTION_DISCOVER_SUCCESS);
        EventBus.getDefault().post(connectStatusEvent);
    }

    private void formatCommonOrder(OrderTask task, byte[] value) {
        task.orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
        task.response.responseValue = value;
        mQueue.poll();
        executeTask();
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_RESULT);
        event.setResponse(task.response);
        EventBus.getDefault().post(event);
    }

    public void onOpenNotifyTimeout() {
        if (!mQueue.isEmpty()) {
            mQueue.clear();
        }
        disConnectBle();
    }


    public void pollTask() {
        if (mQueue != null && !mQueue.isEmpty()) {
            OrderTask orderTask = mQueue.peek();
            LogModule.i("remove " + orderTask.orderType.getName());
            mQueue.poll();
        }
    }

    public void timeoutHandler(OrderTask orderTask) {
        mHandler.postDelayed(orderTask.timeoutRunner, orderTask.delayTime);
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////
    public String advName;

    public int advInterval;
    public int switchState;
    public int powerState;
    public int overloadState;
    public int overloadTopValue;
    public String electricityV;
    public String electricityC;
    public String electricityP;
    public long eneryTotal;
    public int eneryTotalToday;
    public int eneryTotalMonthly;
    public int countDown;
    public int countDownInit;
    public String firmwareVersion;
    public String mac;
    public int energySavedInterval;
    public int energySavedPercent;
    public List<EnergyInfo> energyHistory;
    public List<EnergyInfo> energyHistoryToday;
    public int overloadValue;
    public int electricityConstant;
}
