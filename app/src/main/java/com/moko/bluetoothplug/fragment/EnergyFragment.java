package com.moko.bluetoothplug.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.moko.bluetoothplug.R;
import com.moko.bluetoothplug.activity.DeviceInfoActivity;
import com.moko.bluetoothplug.adapter.EnergyListAdapter;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.EnergyInfo;
import com.moko.support.event.DataChangedEvent;
import com.moko.support.utils.MokoUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EnergyFragment extends Fragment implements RadioGroup.OnCheckedChangeListener {

    private static final String TAG = EnergyFragment.class.getSimpleName();
    @Bind(R.id.rg_energy)
    RadioGroup rgEnergy;
    @Bind(R.id.tv_energy_total)
    TextView tvEnergyTotal;
    @Bind(R.id.tv_duration)
    TextView tvDuration;
    @Bind(R.id.tv_unit)
    TextView tvUnit;
    @Bind(R.id.rv_energy)
    RecyclerView rvEnergy;
    @Bind(R.id.rb_daily)
    RadioButton rbDaily;
    @Bind(R.id.rb_monthly)
    RadioButton rbMonthly;
    private EnergyListAdapter adapter;

    private DeviceInfoActivity activity;

    public EnergyFragment() {
    }

    public static EnergyFragment newInstance() {
        EnergyFragment fragment = new EnergyFragment();
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataChangedEvent(DataChangedEvent event) {
        final int function = event.getFunction();
        switch (function) {
            case MokoConstants.NOTIFY_FUNCTION_ENERGY:
                if (rbDaily.isChecked()) {
                    tvEnergyTotal.setText(MokoSupport.getInstance().eneryTotalToday);
                    List<EnergyInfo> energyHistoryToday = MokoSupport.getInstance().energyHistoryToday;
                    adapter.replaceData(energyHistoryToday);
                } else {
                    tvEnergyTotal.setText(MokoSupport.getInstance().eneryTotalMonthly);
                    List<EnergyInfo> energyHistory = MokoSupport.getInstance().energyHistory;
                    adapter = new EnergyListAdapter();
                    adapter.replaceData(energyHistory);
                }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_energy, container, false);
        ButterKnife.bind(this, view);
        tvEnergyTotal.setText(MokoSupport.getInstance().eneryTotalToday);
        Calendar calendar = Calendar.getInstance();
        String time = MokoUtils.calendar2StrDate(calendar, "HH");
        String date = MokoUtils.calendar2StrDate(calendar, "MM-dd");
        tvDuration.setText(String.format("00:00 to %s:00,%s", time, date));
        List<EnergyInfo> energyInfos = MokoSupport.getInstance().energyHistoryToday;
        if (energyInfos != null) {
            adapter = new EnergyListAdapter();
            adapter.replaceData(energyInfos);
            adapter.openLoadAnimation();
            rvEnergy.setLayoutManager(new LinearLayoutManager(getActivity()));
            rvEnergy.setAdapter(adapter);
        }
        activity = (DeviceInfoActivity) getActivity();
        rgEnergy.setOnCheckedChangeListener(this);
        EventBus.getDefault().register(this);
        return view;
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        Log.i(TAG, "onDestroyView: ");
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        ButterKnife.unbind(this);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_daily:
                // 切换日
                tvEnergyTotal.setText(MokoSupport.getInstance().eneryTotalToday);
                Calendar calendarDaily = Calendar.getInstance();
                String time = MokoUtils.calendar2StrDate(calendarDaily, "HH");
                String date = MokoUtils.calendar2StrDate(calendarDaily, "MM-dd");
                tvDuration.setText(String.format("00:00 to %s:00,%s", time, date));
                tvUnit.setText("Hour/KWh");
                List<EnergyInfo> energyHistoryToday = MokoSupport.getInstance().energyHistoryToday;
                if (energyHistoryToday != null) {
                    adapter.replaceData(energyHistoryToday);
                }
                break;
            case R.id.rb_monthly:
                // 切换月
                tvEnergyTotal.setText(MokoSupport.getInstance().eneryTotalMonthly);
                Calendar calendarMonthly = Calendar.getInstance();
                String end = MokoUtils.calendar2StrDate(calendarMonthly, "MM-dd");
                calendarMonthly.add(Calendar.DAY_OF_MONTH, -30);
                String start = MokoUtils.calendar2StrDate(calendarMonthly, "MM-dd");
                tvDuration.setText(String.format("%s to %s", start, end));
                tvUnit.setText("Date/KWh");
                List<EnergyInfo> energyHistory = MokoSupport.getInstance().energyHistory;
                if (energyHistory != null) {
                    adapter.replaceData(energyHistory);
                }
                break;
        }
    }
}
