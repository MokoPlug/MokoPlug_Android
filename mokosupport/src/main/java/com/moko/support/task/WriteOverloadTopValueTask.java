package com.moko.support.task;


import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;

public class WriteOverloadTopValueTask extends OrderTask {
    public byte[] orderData;

    public WriteOverloadTopValueTask() {
        super(OrderType.WRITE_CHARACTER, OrderEnum.WRITE_OVERLOAD_TOP_VALUE, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    public void setData(int topValue) {
        orderData = new byte[5];
        orderData[0] = (byte) MokoConstants.HEADER_WRITE_SEND;
        orderData[1] = (byte) order.getOrderHeader();
        orderData[2] = (byte) 0x02;
        byte[] topValueBytes = MokoUtils.toByteArray(topValue, 2);
        orderData[3] = topValueBytes[0];
        orderData[4] = topValueBytes[1];
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
        MokoSupport.getInstance().executeTask();
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_RESULT);
        event.setResponse(response);
        EventBus.getDefault().post(event);
    }
}
