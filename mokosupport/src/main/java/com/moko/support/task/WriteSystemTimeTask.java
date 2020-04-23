package com.moko.support.task;


import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import java.util.Calendar;

public class WriteSystemTimeTask extends OrderTask {
    public byte[] orderData;

    public WriteSystemTimeTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE_CHARACTER, OrderEnum.WRITE_SYSTEM_TIME, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int date = calendar.get(Calendar.DATE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        byte[] yearBytes = MokoUtils.toByteArray(year, 2);
        orderData = new byte[10];
        orderData[0] = (byte) MokoConstants.HEADER_WRITE_SEND;
        orderData[1] = (byte) order.getOrderHeader();
        orderData[2] = (byte) 0x07;
        orderData[3] = yearBytes[0];
        orderData[4] = yearBytes[1];
        orderData[5] = (byte) month;
        orderData[6] = (byte) date;
        orderData[7] = (byte) hour;
        orderData[8] = (byte) minute;
        orderData[9] = (byte) second;
    }

    @Override
    public byte[] assemble() {
        return orderData;
    }

    @Override
    public void parseValue(byte[] value) {
        if (order.getOrderHeader() != (value[1] & 0xFF))
            return;

        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
