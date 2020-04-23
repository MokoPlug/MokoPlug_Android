package com.moko.support.task;


import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoOrderTaskCallback;
import com.moko.support.entity.EnergyInfo;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class ReadEnergyHistoryTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 3;

    public byte[] orderData;
    private int total;
    private List<EnergyInfo> energyInfos;
    private Calendar calendar;

    public ReadEnergyHistoryTask(MokoOrderTaskCallback callback) {
        super(OrderType.READ_CHARACTER, OrderEnum.READ_ENERGY_HISTORY, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
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
        if (0x0D == order.getOrderHeader()) {
            LogModule.i(order.getOrderName() + "成功");
            if (0x01 == (value[2] & 0xFF)) {
                // 没有历史数据
                orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                MokoSupport.getInstance().pollTask();
                callback.onOrderResult(response);
                MokoSupport.getInstance().executeTask(callback);
            }
            if (0x05 == (value[2] & 0xFF)) {
                total = value[3] & 0xFF;
                energyInfos = new ArrayList<>();
                int year = MokoUtils.toInt(Arrays.copyOfRange(value, 3, 5));
                int month = value[6] & 0xFF;
                int day = value[7] & 0xFF;
                calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
            }
        }
        if (0x0E == order.getOrderHeader()) {
            int count = (value[2] & 0xFF) / 3;
            for (int i = 0; i < count; i++) {
                EnergyInfo energyInfo = new EnergyInfo();
                total--;
                int day = value[3 + 3 * i];
                byte[] energyBytes = Arrays.copyOfRange(value, 4 + 3 * i, 6 + 3 * i);
                int energy = MokoUtils.toInt(energyBytes);
                Calendar c = (Calendar) calendar.clone();
                c.add(Calendar.DAY_OF_MONTH, day);
                energyInfo.recordDate = MokoUtils.calendar2StrDate(c, "yyyy-MM-dd");
                energyInfo.value = energy;
                energyInfos.add(energyInfo);
            }
            MokoSupport.getInstance().energyHistory = energyInfos;
            if (total <= 0) {
                orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                MokoSupport.getInstance().pollTask();
                callback.onOrderResult(response);
                MokoSupport.getInstance().executeTask(callback);
            }
        }

    }
}
