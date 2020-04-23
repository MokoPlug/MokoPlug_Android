package com.moko.support.task;


import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.log.LogModule;

import java.util.Arrays;

public class ReadFirmwareVersionTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 3;

    public byte[] orderData;

    public ReadFirmwareVersionTask(MokoOrderTaskCallback callback) {
        super(OrderType.READ_CHARACTER, OrderEnum.READ_FIRMWARE_VERISON, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        orderData = new byte[ORDERDATA_LENGTH];
        orderData[0] = (byte) MokoConstants.HEADER_READ_SEND;
        orderData[1] = (byte) order.getOrderHeader();
        orderData[2] = 0;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != (value[1] & 0xFF))
            return;
        int length = value[2] & 0xFF;
        byte[] data = Arrays.copyOfRange(value, 3, value.length);
        if (data.length != length)
            return;
        final String version = new String(data);
        MokoSupport.getInstance().firmwareVersion = version;

        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
