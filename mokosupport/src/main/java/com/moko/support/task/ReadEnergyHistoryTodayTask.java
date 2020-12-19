package com.moko.support.task;


import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.EnergyInfo;
import com.moko.support.entity.OrderEnum;
import com.moko.support.entity.OrderType;
import com.moko.support.event.OrderTaskResponseEvent;
import com.moko.support.log.LogModule;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class ReadEnergyHistoryTodayTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 3;

    public byte[] orderData;
    private int total;
    private List<EnergyInfo> energyInfos;
    private Calendar calendar;

    public ReadEnergyHistoryTodayTask() {
        super(OrderType.READ_CHARACTER, OrderEnum.READ_ENERGY_HISTORY_TODAY, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
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
                MokoSupport.getInstance().executeTask();
                OrderTaskResponseEvent event = new OrderTaskResponseEvent();
                event.setAction(MokoConstants.ACTION_ORDER_RESULT);
                event.setResponse(response);
                EventBus.getDefault().post(event);
            }
            if (0x04 == (value[2] & 0xFF)) {
                total = value[3] & 0xFF;
                byte[] totalTodayBytes = Arrays.copyOfRange(value, 4, 7);
                final int totalToday = MokoUtils.toIntUnsigned(totalTodayBytes);
                MokoSupport.getInstance().eneryTotalToday = totalToday;
                energyInfos = new ArrayList<>();
                calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
            }
        }
        if (0x12 == (value[1] & 0xFF)) {
            int count = (value[2] & 0xFF) / 3;
            for (int i = 0; i < count; i++) {
                EnergyInfo energyInfo = new EnergyInfo();
                total--;
                int hour = value[3 + 3 * i];
                byte[] energyBytes = Arrays.copyOfRange(value, 4 + 3 * i, 6 + 3 * i);
                int energy = MokoUtils.toIntUnsigned(energyBytes);
                Calendar c = (Calendar) calendar.clone();
                c.add(Calendar.HOUR_OF_DAY, hour);
                c.set(Calendar.MINUTE, 0);
                energyInfo.recordDate = MokoUtils.calendar2StrDate(c, "yyyy-MM-dd HH");
                energyInfo.type = 0;
                energyInfo.hour = energyInfo.recordDate.substring(11);
                energyInfo.value = String.valueOf(energy);
                energyInfo.energy = energy;
                energyInfos.add(energyInfo);
            }
            if (total <= 0) {
                Collections.reverse(energyInfos);
                MokoSupport.getInstance().energyHistoryToday = energyInfos;

                orderStatus = OrderTask.ORDER_STATUS_SUCCESS;
                MokoSupport.getInstance().pollTask();
                MokoSupport.getInstance().executeTask();
                OrderTaskResponseEvent event = new OrderTaskResponseEvent();
                event.setAction(MokoConstants.ACTION_ORDER_RESULT);
                event.setResponse(response);
                EventBus.getDefault().post(event);
            }
        }

    }
}
