package com.moko.support.task;


import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;

public class WriteCountdownTask extends OrderTask {
    public byte[] orderData;

    public WriteCountdownTask() {
        super(OrderType.WRITE_CHARACTER, OrderEnum.WRITE_COUNTDOWN, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    public void setData(int countdown) {
        byte[] countdownBytes = MokoUtils.toByteArray(countdown, 4);
        orderData = new byte[7];
        orderData[0] = (byte) MokoConstants.HEADER_WRITE_SEND;
        orderData[1] = (byte) order.getOrderHeader();
        orderData[2] = (byte) 0x04;
        orderData[3] = countdownBytes[0];
        orderData[4] = countdownBytes[1];
        orderData[5] = countdownBytes[2];
        orderData[6] = countdownBytes[3];
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
