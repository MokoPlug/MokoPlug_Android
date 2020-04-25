package com.moko.support.task;


import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import java.util.Arrays;

public class ReadElectricityTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 3;

    public byte[] orderData;

    public ReadElectricityTask(MokoOrderTaskCallback callback) {
        super(OrderType.READ_CHARACTER, OrderEnum.READ_ELECTRICITY_VALUE, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
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
        if (0x07 != (value[2] & 0xFF))
            return;
        byte[] vBytes = Arrays.copyOfRange(value, 3, 5);
        final int v = MokoUtils.toInt(vBytes);
        MokoSupport.getInstance().electricityV = MokoUtils.getDecimalFormat("0.#").format(v * 0.1f);

        byte[] cBytes = Arrays.copyOfRange(value, 5, 8);
        final int c = MokoUtils.toInt(cBytes);
        MokoSupport.getInstance().electricityC =  String.valueOf(c);

        byte[] pBytes = Arrays.copyOfRange(value, 8, 10);
        final int p = MokoUtils.toInt(pBytes);
        MokoSupport.getInstance().electricityP =  MokoUtils.getDecimalFormat("0.#").format(p * 0.1f);

        LogModule.i(order.getOrderName() + "成功");
        orderStatus = OrderTask.ORDER_STATUS_SUCCESS;

        MokoSupport.getInstance().pollTask();
        callback.onOrderResult(response);
        MokoSupport.getInstance().executeTask(callback);
    }
}
