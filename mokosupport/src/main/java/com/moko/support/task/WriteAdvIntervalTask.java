package com.moko.support.task;


import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.log.LogModule;

public class WriteAdvIntervalTask extends OrderTask {
    public byte[] orderData;

    public WriteAdvIntervalTask(MokoOrderTaskCallback callback) {
        super(OrderType.WRITE_CHARACTER, OrderEnum.WRITE_ADV_INTERVAL, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    public void setData(int advInterval) {
        orderData = new byte[4];
        orderData[0] = (byte) MokoConstants.HEADER_WRITE_SEND;
        orderData[1] = (byte) order.getOrderHeader();
        orderData[2] = (byte) 0x01;
        orderData[3] = (byte) advInterval;
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
        response.responseValue = value;
        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
