package com.moko.bluetoothplug;

import android.text.TextUtils;
import android.util.SparseArray;

import com.moko.bluetoothplug.entity.PlugInfo;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.service.DeviceInfoParseable;
import com.moko.support.utils.MokoUtils;

import java.util.Arrays;

import no.nordicsemi.android.support.v18.scanner.ScanResult;

public class PlugInfoParseableImpl implements DeviceInfoParseable<PlugInfo> {
    @Override
    public PlugInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        ScanResult scanResult = deviceInfo.scanResult;
        SparseArray<byte[]> manufacturer = scanResult.getScanRecord().getManufacturerSpecificData();
        if (manufacturer == null || manufacturer.size() == 0) {
            return null;
        }
        int manufacturerId = manufacturer.keyAt(0);
        // 20ff
        if (!"20ff".equalsIgnoreCase(String.format("%04X", manufacturerId)))
            return null;
        byte[] manufacturerData = manufacturer.get(manufacturerId);
        if (manufacturerData.length != 13)
            return null;
        byte[] electricityVBytes = Arrays.copyOfRange(manufacturerData, 2, 4);
        byte[] electricityCBytes = Arrays.copyOfRange(manufacturerData, 4, 7);
        byte[] electricityPBytes = Arrays.copyOfRange(manufacturerData, 7, 9);
        String binary = MokoUtils.hexString2binaryString(MokoUtils.byte2HexString(manufacturerData[12]));
        PlugInfo plugInfo = new PlugInfo();
        plugInfo.name = deviceInfo.name;
        plugInfo.mac = deviceInfo.mac;
        plugInfo.rssi = deviceInfo.rssi;
        plugInfo.electricityV = MokoUtils.getDecimalFormat("0.#").format(MokoUtils.toInt(electricityVBytes) * 0.1f);
        plugInfo.electricityC = MokoUtils.getDecimalFormat("0.###").format(MokoUtils.toInt(electricityCBytes) * 0.001f);
        plugInfo.electricityP = MokoUtils.getDecimalFormat("0.#").format(MokoUtils.toInt(electricityPBytes) * 0.1f);
        if (TextUtils.isEmpty(binary)) {
            plugInfo.overloadState = 0;
            plugInfo.onoff = 0;
            return plugInfo;
        }
        plugInfo.overloadState = Integer.parseInt(binary.substring(1, 2));
        plugInfo.onoff = Integer.parseInt(binary.substring(2, 3));
        return plugInfo;
    }
}
