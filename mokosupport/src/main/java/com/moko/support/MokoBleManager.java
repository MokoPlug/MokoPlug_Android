package com.moko.support;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.moko.support.callback.MokoResponseCallback;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import java.util.UUID;

import androidx.annotation.NonNull;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

public class MokoBleManager extends BleManager {

    private MokoResponseCallback mMokoResponseCallback;
    private static MokoBleManager managerInstance = null;
    private final static UUID SERVICE_UUID = UUID.fromString("0000FFB0-0000-1000-8000-00805F9B34FB");
    private final static UUID READ_UUID = UUID.fromString("0000FFB0-0000-1000-8000-00805F9B34FB");
    private final static UUID WRITE_UUID = UUID.fromString("0000FFB1-0000-1000-8000-00805F9B34FB");
    private final static UUID NOTIFY_UUID = UUID.fromString("0000FFB2-0000-1000-8000-00805F9B34FB");

    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic notifyCharacteristic;

    public static synchronized MokoBleManager getMokoBleManager(final Context context) {
        if (managerInstance == null) {
            managerInstance = new MokoBleManager(context);
        }
        return managerInstance;
    }

    @Override
    public void log(int priority, @NonNull String message) {
        LogModule.v(message);
    }

    public MokoBleManager(@NonNull Context context) {
        super(context);
    }

    public void setBeaconResponseCallback(MokoResponseCallback mMokoResponseCallback) {
        this.mMokoResponseCallback = mMokoResponseCallback;
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MokoBleManagerGattCallback();
    }

    public class MokoBleManagerGattCallback extends BleManagerGattCallback {

        @Override
        protected boolean isRequiredServiceSupported(@NonNull BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(SERVICE_UUID);
            if (service != null) {
                readCharacteristic = service.getCharacteristic(READ_UUID);
                writeCharacteristic = service.getCharacteristic(WRITE_UUID);
                notifyCharacteristic = service.getCharacteristic(NOTIFY_UUID);
                enableRead();
                enableWrite();
                enableNotify();
                return true;
            }
            return false;
        }

        @Override
        protected void onDeviceDisconnected() {

        }

        @Override
        protected void onCharacteristicWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            LogModule.e("onCharacteristicWrite");
            LogModule.e("device to app : " + MokoUtils.bytesToHexString(characteristic.getValue()));
            mMokoResponseCallback.onCharacteristicWrite(characteristic.getValue());
        }

        @Override
        protected void onCharacteristicRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic) {
            LogModule.e("onCharacteristicRead");
            LogModule.e("device to app : " + MokoUtils.bytesToHexString(characteristic.getValue()));
            mMokoResponseCallback.onCharacteristicRead(characteristic.getValue());
        }

        @Override
        protected void onDescriptorWrite(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor) {
            LogModule.e("onDescriptorWrite");
            String characteristicUUIDStr = descriptor.getCharacteristic().getUuid().toString().toLowerCase();
            if (notifyCharacteristic.getUuid().toString().toLowerCase().equals(characteristicUUIDStr))
                mMokoResponseCallback.onServicesDiscovered(gatt);
        }
    }

    public void enableNotify() {
        setIndicationCallback(notifyCharacteristic).with(new DataReceivedCallback() {
            @Override
            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                final byte[] value = data.getValue();
                LogModule.e("onDataReceived");
                LogModule.e("device to app : " + MokoUtils.bytesToHexString(value));
                mMokoResponseCallback.onCharacteristicChanged(notifyCharacteristic, value);
            }
        });
        enableNotifications(notifyCharacteristic).enqueue();
    }

    public void disableNotify() {
        disableNotifications(notifyCharacteristic).enqueue();
    }

    public void enableRead() {
        setIndicationCallback(readCharacteristic).with(new DataReceivedCallback() {
            @Override
            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                final byte[] value = data.getValue();
                LogModule.e("onDataReceived");
                LogModule.e("device to app : " + MokoUtils.bytesToHexString(value));
                mMokoResponseCallback.onCharacteristicChanged(readCharacteristic, value);
            }
        });
        enableNotifications(readCharacteristic).enqueue();
    }

    public void disableRead() {
        disableNotifications(readCharacteristic).enqueue();
    }

    public void enableWrite() {
        setIndicationCallback(writeCharacteristic).with(new DataReceivedCallback() {
            @Override
            public void onDataReceived(@NonNull BluetoothDevice device, @NonNull Data data) {
                final byte[] value = data.getValue();
                LogModule.e("onDataReceived");
                LogModule.e("device to app : " + MokoUtils.bytesToHexString(value));
                mMokoResponseCallback.onCharacteristicChanged(writeCharacteristic, value);
            }
        });
        enableNotifications(writeCharacteristic).enqueue();
    }

    public void disableWrite() {
        disableNotifications(writeCharacteristic).enqueue();
    }
}
