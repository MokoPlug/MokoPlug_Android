package com.moko.support.handler;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.text.TextUtils;

import com.moko.support.entity.MokoCharacteristic;
import com.moko.support.entity.OrderType;

import java.util.HashMap;
import java.util.List;

/**
 * @Date 2017/12/13 0013
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.support.handler.MokoCharacteristicHandler
 */
public class MokoCharacteristicHandler {
    private static MokoCharacteristicHandler INSTANCE;

    public static final String SERVICE_UUID_HEADER = "0000ffb0";

    public HashMap<OrderType, MokoCharacteristic> mokoCharacteristicMap;

    private MokoCharacteristicHandler() {
        //no instance
        mokoCharacteristicMap = new HashMap<>();
    }

    public static MokoCharacteristicHandler getInstance() {
        if (INSTANCE == null) {
            synchronized (MokoCharacteristicHandler.class) {
                if (INSTANCE == null) {
                    INSTANCE = new MokoCharacteristicHandler();
                }
            }
        }
        return INSTANCE;
    }

    public HashMap<OrderType, MokoCharacteristic> getCharacteristics(final BluetoothGatt gatt) {
        if (mokoCharacteristicMap != null && !mokoCharacteristicMap.isEmpty()) {
            mokoCharacteristicMap.clear();
        }
        List<BluetoothGattService> services = gatt.getServices();
        for (BluetoothGattService service : services) {
            String serviceUuid = service.getUuid().toString();
            if (TextUtils.isEmpty(serviceUuid)) {
                continue;
            }
            if (serviceUuid.startsWith("00001800")||serviceUuid.startsWith("00001801")) {
                continue;
            }
            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
            if (service.getUuid().toString().startsWith(SERVICE_UUID_HEADER)) {
                for (BluetoothGattCharacteristic characteristic : characteristics) {
                    String characteristicUuid = characteristic.getUuid().toString();
                    if (TextUtils.isEmpty(characteristicUuid)) {
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.READ_CHARACTER.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.READ_CHARACTER, new MokoCharacteristic(characteristic, OrderType.READ_CHARACTER));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.WRITE_CHARACTER.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.WRITE_CHARACTER, new MokoCharacteristic(characteristic, OrderType.WRITE_CHARACTER));
                        continue;
                    }
                    if (characteristicUuid.equals(OrderType.NOTIFY_CHARACTER.getUuid())) {
                        gatt.setCharacteristicNotification(characteristic, true);
                        mokoCharacteristicMap.put(OrderType.NOTIFY_CHARACTER, new MokoCharacteristic(characteristic, OrderType.WRITE_CHARACTER));
                        continue;
                    }
                }
            }
//            LogModule.i("service uuid:" + service.getUuid().toString());
//            List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
//            for (BluetoothGattCharacteristic characteristic : characteristics) {
//                LogModule.i("characteristic uuid:" + characteristic.getUuid().toString());
//                LogModule.i("characteristic properties:" + MokoUtils.getCharPropertie(characteristic.getProperties()));
//                List<BluetoothGattDescriptor> descriptors = characteristic.getDescriptors();
//                for (BluetoothGattDescriptor descriptor : descriptors) {
//                    LogModule.i("descriptor uuid:" + descriptor.getUuid().toString());
//                    LogModule.i("descriptor value:" + MokoUtils.bytesToHexString(descriptor.getValue()));
//                }
//            }
        }
        return mokoCharacteristicMap;
    }
}
