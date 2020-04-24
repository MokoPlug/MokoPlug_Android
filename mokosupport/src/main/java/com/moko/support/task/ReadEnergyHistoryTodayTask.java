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

public class ReadEnergyHistoryTodayTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 3;

    public byte[] orderData;
    private int total;
    private List<EnergyInfo> energyInfos;
    private Calendar calendar;

    public ReadEnergyHistoryTodayTask(MokoOrderTaskCallback callback) {
        super(OrderType.READ_CHARACTER, OrderEnum.READ_ENERGY_HISTORY_TODAY, callback, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
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
        if (0x11 == (value[1] & 0xFF)) {
            LogModule.i(order.getOrderName() + "成功");
            if (0x01 == (value[2] & 0xFF)) {
                // 没有历史数据
                orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                MokoSupport.getInstance().pollTask();
                callback.onOrderResult(response);
                MokoSupport.getInstance().executeTask(callback);
            }
            if (0x03 == (value[2] & 0xFF)) {
                total = value[3] & 0xFF;
                byte[] totalTodayBytes = Arrays.copyOfRange(value, 4, 6);
                final int totalToday = MokoUtils.toInt(totalTodayBytes);
                MokoSupport.getInstance().eneryTotalToday = totalToday;
                energyInfos = new ArrayList<>();
                calendar = Calendar.getInstance();
            }
        }
        if (0x12 == (value[1] & 0xFF)) {
            int count = (value[2] & 0xFF) / 3;
            for (int i = 0; i < count; i++) {
                EnergyInfo energyInfo = new EnergyInfo();
                total--;
                int hour = value[3 + 3 * i];
                byte[] energyBytes = Arrays.copyOfRange(value, 4 + 3 * i, 6 + 3 * i);
                int energy = MokoUtils.toInt(energyBytes);
                Calendar c = (Calendar) calendar.clone();
                c.add(Calendar.HOUR_OF_DAY, hour);
                c.set(Calendar.MINUTE, 0);
                energyInfo.recordDate = MokoUtils.calendar2StrDate(c, "yyyy-MM-dd HH:mm");
                energyInfo.value = energy;
                energyInfos.add(energyInfo);
            }
            MokoSupport.getInstance().energyHistoryToday = energyInfos;
            if (total <= 0) {
                orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                MokoSupport.getInstance().pollTask();
                callback.onOrderResult(response);
                MokoSupport.getInstance().executeTask(callback);
            }
        }

    }
}
