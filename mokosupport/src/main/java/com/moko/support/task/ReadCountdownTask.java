package com.moko.support.task;


import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

public class ReadCountdownTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 3;

    public byte[] orderData;

    public ReadCountdownTask() {
        super(OrderType.READ_CHARACTER, OrderEnum.READ_COUNTDOWN, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
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
        if (0x09 != (value[2] & 0xFF))
            return;

        if (0x01 == (value[3] & 0xFF)) {
            byte[] countDownInitBytes = Arrays.copyOfRange(value, 4, 8);
            final int countDownInit = MokoUtils.toIntUnsigned(countDownInitBytes);
            MokoSupport.getInstance().countDownInit = countDownInit;
            byte[] countDownBytes = Arrays.copyOfRange(value, 8, 12);
            final int countDown = MokoUtils.toIntUnsigned(countDownBytes);
            MokoSupport.getInstance().countDown = countDown;
        }

        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        MokoSupport.getInstance().executeTask();
        OrderTaskResponseEvent event = new OrderTaskResponseEvent();
        event.setAction(MokoConstants.ACTION_ORDER_RESULT);
        event.setResponse(response);
        EventBus.getDefault().post(event);
    }
}
