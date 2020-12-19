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

public class ReadEnergyHistoryTask extends OrderTask {
    private static final int ORDERDATA_LENGTH = 3;

    public byte[] orderData;
    private int total;
    private List<EnergyInfo> energyInfos;
    private Calendar calendar;

    private int eneryTotalMonth = 0;

    public ReadEnergyHistoryTask() {
        super(OrderType.READ_CHARACTER, OrderEnum.READ_ENERGY_HISTORY, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
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
        if (0x0D == (value[1] & 0xFF)) {
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
            if (0x07 == (value[2] & 0xFF)) {
                total = value[3] & 0xFF;
                energyInfos = new ArrayList<>();
                int year = MokoUtils.toIntUnsigned(Arrays.copyOfRange(value, 4, 6));
                int month = value[6] & 0xFF;
                int day = value[7] & 0xFF;
                int hour = value[8] & 0xFF;
                int minute = value[9] & 0xFF;
                calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month - 1);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
            }
        }
        if (0x0E == (value[1] & 0xFF)) {
            int count = (value[2] & 0xFF) / 4;
            for (int i = 0; i < count; i++) {
                EnergyInfo energyInfo = new EnergyInfo();
                total--;
                int day = value[3 + 4 * i];
                byte[] energyBytes = Arrays.copyOfRange(value, 4 + 4 * i, 7 + 4 * i);
                int energy = MokoUtils.toIntUnsigned(energyBytes);
                Calendar c = (Calendar) calendar.clone();
                c.add(Calendar.DAY_OF_MONTH, day);
                energyInfo.recordDate = MokoUtils.calendar2StrDate(c, "yyyy-MM-dd HH");
                energyInfo.type = 1;
                energyInfo.date = energyInfo.recordDate.substring(5, 10);
                energyInfo.value = String.valueOf(energy);
                energyInfo.energy = energy;
                eneryTotalMonth += energy;
                energyInfos.add(energyInfo);
            }
            if (total <= 0) {
                Collections.reverse(energyInfos);
                MokoSupport.getInstance().energyHistory = energyInfos;
                MokoSupport.getInstance().eneryTotalMonthly = eneryTotalMonth;

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
