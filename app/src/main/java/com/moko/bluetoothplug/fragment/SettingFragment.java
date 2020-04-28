package com.moko.bluetoothplug.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.bluetoothplug.AppConstants;
import com.moko.bluetoothplug.R;
import com.moko.bluetoothplug.activity.AdvIntervalActivity;
import com.moko.bluetoothplug.activity.DeviceInfoActivity;
import com.moko.bluetoothplug.activity.EnergySavedIntervalActivity;
import com.moko.bluetoothplug.activity.EnergySavedPercentActivity;
import com.moko.bluetoothplug.activity.FirmwareUpdateActivity;
import com.moko.bluetoothplug.activity.ModifyNameActivity;
import com.moko.bluetoothplug.activity.ModifyPowerStatusActivity;
import com.moko.bluetoothplug.activity.OverloadValueActivity;
import com.moko.bluetoothplug.dialog.AlertMessageDialog;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.event.DataChangedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingFragment extends Fragment {

    private static final String TAG = SettingFragment.class.getSimpleName();
    @Bind(R.id.tv_device_name)
    TextView tvDeviceName;
    @Bind(R.id.tv_adv_interval)
    TextView tvAdvInterval;
    @Bind(R.id.tv_overload_value)
    TextView tvOverloadValue;
    @Bind(R.id.tv_energy_saved_interval)
    TextView tvEnergySavedInterval;
    @Bind(R.id.tv_energy_saved_percent)
    TextView tvEnergySavedPercent;
    @Bind(R.id.tv_energy_consumption)
    TextView tvEnergyConsumption;

    private DeviceInfoActivity activity;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataChangedEvent(DataChangedEvent event) {
        final int function = event.getFunction();
        switch (function) {
            case MokoConstants.NOTIFY_FUNCTION_ENERGY:
                tvEnergyConsumption.setText(MokoSupport.getInstance().eneryTotal);
                break;
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
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        tvDeviceName.setText(MokoSupport.getInstance().advName);
        tvAdvInterval.setText(String.valueOf(MokoSupport.getInstance().advInterval));
        tvOverloadValue.setText(String.valueOf(MokoSupport.getInstance().overloadTopValue));
        tvEnergySavedInterval.setText(String.valueOf(MokoSupport.getInstance().energySavedInterval));
        tvEnergySavedPercent.setText(String.valueOf(MokoSupport.getInstance().energySavedPercent));
        tvEnergyConsumption.setText(MokoSupport.getInstance().eneryTotal);
        activity = (DeviceInfoActivity) getActivity();
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

    @OnClick({R.id.rl_modify_name, R.id.rl_modify_power_status, R.id.rl_check_update, R.id.rl_adv_interval
            , R.id.rl_overload_value, R.id.rl_power_report_interval, R.id.rl_power_change_notification
            , R.id.rl_energy_consumption, R.id.tv_reset})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.rl_modify_name:
                // 修改名称
                startActivityForResult(new Intent(getActivity(), ModifyNameActivity.class), AppConstants.REQUEST_CODE_MODIFY_NAME);
                break;
            case R.id.rl_modify_power_status:
                // 修改上电状态
                startActivityForResult(new Intent(getActivity(), ModifyPowerStatusActivity.class), AppConstants.REQUEST_CODE_MODIFY_POWER_STATUS);
                break;
            case R.id.rl_check_update:
                // 升级
                startActivityForResult(new Intent(getActivity(), FirmwareUpdateActivity.class), AppConstants.REQUEST_CODE_UPDATE);
                break;
            case R.id.rl_adv_interval:
                // 修改广播间隔
                startActivityForResult(new Intent(getActivity(), AdvIntervalActivity.class), AppConstants.REQUEST_CODE_ADV_INTERVAL);
                break;
            case R.id.rl_overload_value:
                // 修改过载保护值
                startActivityForResult(new Intent(getActivity(), OverloadValueActivity.class), AppConstants.REQUEST_CODE_OVERLOAD_VALUE);
                break;
            case R.id.rl_power_report_interval:
                // 修改电能上报间隔
                startActivityForResult(new Intent(getActivity(), EnergySavedIntervalActivity.class), AppConstants.REQUEST_CODE_ENERGY_SAVED_INTERVAL);
                break;
            case R.id.rl_power_change_notification:
                // 修改电能变化百分比
                startActivityForResult(new Intent(getActivity(), EnergySavedPercentActivity.class), AppConstants.REQUEST_CODE_ENERGY_SAVED_PERCENT);
                break;
            case R.id.rl_energy_consumption:
                // 重置累计电能
                activity.resetEnergyConsumption();
                break;
            case R.id.tv_reset:
                // 重置
                activity.reset();
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppConstants.REQUEST_CODE_MODIFY_NAME) {
            if (resultCode == getActivity().RESULT_OK) {
                final String deviceName = MokoSupport.getInstance().advName;
                tvDeviceName.setText(deviceName);
                activity.changeName();
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_ADV_INTERVAL) {
            if (resultCode == getActivity().RESULT_OK) {
                final int advInterval = MokoSupport.getInstance().advInterval;
                tvAdvInterval.setText(String.valueOf(advInterval));
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_OVERLOAD_VALUE) {
            if (resultCode == getActivity().RESULT_OK) {
                final int overloadTopValue = MokoSupport.getInstance().overloadTopValue;
                tvOverloadValue.setText(String.valueOf(overloadTopValue));
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_ENERGY_SAVED_INTERVAL) {
            if (resultCode == getActivity().RESULT_OK) {
                final int energySavedInterval = MokoSupport.getInstance().energySavedInterval;
                tvEnergySavedInterval.setText(String.valueOf(energySavedInterval));
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_ENERGY_SAVED_PERCENT) {
            if (resultCode == getActivity().RESULT_OK) {
                final int energySavedPercent = MokoSupport.getInstance().energySavedPercent;
                tvEnergySavedPercent.setText(String.valueOf(energySavedPercent));
            }
        }
    }

    public void resetEnergyTotal() {
        tvEnergyConsumption.setText("0");
    }
}
