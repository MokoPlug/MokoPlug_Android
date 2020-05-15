package com.moko.bluetoothplug.activity;


import android.app.FragmentManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.IdRes;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.moko.bluetoothplug.R;
import com.moko.bluetoothplug.dialog.AlertMessageDialog;
import com.moko.bluetoothplug.dialog.LoadingMessageDialog;
import com.moko.bluetoothplug.fragment.EnergyFragment;
import com.moko.bluetoothplug.fragment.PowerFragment;
import com.moko.bluetoothplug.fragment.SettingFragment;
import com.moko.bluetoothplug.fragment.TimerFragment;
import com.moko.bluetoothplug.service.MokoService;
import com.moko.bluetoothplug.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderEnum;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.task.OrderTask;
import com.moko.support.task.OrderTaskResponse;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    @Bind(R.id.frame_container)
    FrameLayout frameContainer;
    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.radioBtn_power)
    RadioButton radioBtnPower;
    @Bind(R.id.radioBtn_energy)
    RadioButton radioBtnEnergy;
    @Bind(R.id.radioBtn_timer)
    RadioButton radioBtnTimer;
    @Bind(R.id.radioBtn_setting)
    RadioButton radioBtnSetting;
    @Bind(R.id.rg_options)
    RadioGroup rgOptions;
    public MokoService mMokoService;
    private FragmentManager fragmentManager;
    private PowerFragment powerFragment;
    private EnergyFragment energyFragment;
    private TimerFragment timerFragment;
    private SettingFragment settingFragment;
    public String mDeviceMac;
    public String mDeviceName;
    private int validCount;
    private boolean mReceiverTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);
        Intent intent = new Intent(this, MokoService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        initFragment();
        rgOptions.setOnCheckedChangeListener(this);
        radioBtnPower.setChecked(true);
        EventBus.getDefault().register(this);
        tvTitle.setText(MokoSupport.getInstance().advName);
        mDeviceName = MokoSupport.getInstance().advName;
        mDeviceMac = MokoSupport.getInstance().mac;
    }

    private void initFragment() {
        fragmentManager = getFragmentManager();
        powerFragment = PowerFragment.newInstance();
        energyFragment = EnergyFragment.newInstance();
        timerFragment = TimerFragment.newInstance();
        settingFragment = SettingFragment.newInstance();
        fragmentManager.beginTransaction()
                .add(R.id.frame_container, powerFragment)
                .add(R.id.frame_container, energyFragment)
                .add(R.id.frame_container, timerFragment)
                .add(R.id.frame_container, settingFragment)
                .show(powerFragment)
                .hide(energyFragment)
                .hide(timerFragment)
                .hide(settingFragment)
                .commit();
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMokoService = ((MokoService.LocalBinder) service).getService();
            // 注册广播接收器
            IntentFilter filter = new IntentFilter();
            filter.addAction(MokoConstants.ACTION_ORDER_RESULT);
            filter.addAction(MokoConstants.ACTION_ORDER_TIMEOUT);
            filter.addAction(MokoConstants.ACTION_ORDER_FINISH);
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.setPriority(200);
            registerReceiver(mReceiver, filter);
            mReceiverTag = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        });
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                String action = intent.getAction();
                if (!BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    abortBroadcast();
                }
                if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                }
                if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                    dismissSyncProgressDialog();
                }
                if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                    OrderTaskResponse response = (OrderTaskResponse) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK);
                    OrderEnum order = response.order;
                    byte[] value = response.responseValue;
                    switch (order) {
//                        case WRITE_SWITCH_STATE:
//                            if (0x00 == (value[3] & 0xFF)) {
//                                powerFragment.changePowerState();
//                            } else {
//                                ToastUtils.showToast(DeviceInfoActivity.this, "Error");
//                            }
//                            break;
                        case WRITE_RESET_ENERGY_TOTAL:
                            settingFragment.resetEnergyTotal();
                            energyFragment.resetEnergyData();
                            break;
                        case WRITE_RESET:
                            break;
                    }
                }
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
//                            AlertDialog.Builder builder = new AlertDialog.Builder(DeviceInfoActivity.this);
//                            builder.setTitle("Dismiss");
//                            builder.setCancelable(false);
//                            builder.setMessage("The current system of bluetooth is not available!");
//                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    back();
//                                }
//                            });
//                            builder.show();
                            finish();
                            break;
                    }
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        unbindService(mServiceConnection);
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    @OnClick({R.id.tv_back, R.id.iv_more})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tv_back:
                back();
                break;
            case R.id.iv_more:
                startActivity(new Intent(this, MoreActivity.class));
                break;
        }
    }

    private void back() {
        if (MokoSupport.getInstance().isBluetoothOpen()) {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Disconnect Device");
            dialog.setMessage("Please confirm again whether to disconnect the device.");
            dialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
                @Override
                public void onClick() {
                    MokoSupport.getInstance().disConnectBle();
                }
            });
            dialog.show(getSupportFragmentManager());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            back();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        switch (checkedId) {
            case R.id.radioBtn_power:
                fragmentManager.beginTransaction()
                        .show(powerFragment)
                        .hide(energyFragment)
                        .hide(timerFragment)
                        .hide(settingFragment)
                        .commit();
                break;
            case R.id.radioBtn_energy:
                fragmentManager.beginTransaction()
                        .hide(powerFragment)
                        .show(energyFragment)
                        .hide(timerFragment)
                        .hide(settingFragment)
                        .commit();
                break;
            case R.id.radioBtn_timer:
                fragmentManager.beginTransaction()
                        .hide(powerFragment)
                        .hide(energyFragment)
                        .show(timerFragment)
                        .hide(settingFragment)
                        .commit();
                break;
            case R.id.radioBtn_setting:
                fragmentManager.beginTransaction()
                        .hide(powerFragment)
                        .hide(energyFragment)
                        .hide(timerFragment)
                        .show(settingFragment)
                        .commit();
                break;
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Power
    ///////////////////////////////////////////////////////////////////////////

    public void changeSwitchState(boolean switchState) {
        showSyncingProgressDialog();
        OrderTask orderTask = mMokoService.writeSwitchState(switchState ? 1 : 0);
        MokoSupport.getInstance().sendOrder(orderTask);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Timer
    ///////////////////////////////////////////////////////////////////////////

    public void setTimer(int countdown) {
        showSyncingProgressDialog();
        OrderTask orderTask = mMokoService.writeCountdown(countdown);
        MokoSupport.getInstance().sendOrder(orderTask);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Setting
    ///////////////////////////////////////////////////////////////////////////

    public void resetEnergyConsumption() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Reset Energy Consumption");
        dialog.setMessage("Please confirm again whether to reset the accumulated electricity? Value will be recounted after clearing.");
        dialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
            @Override
            public void onClick() {
                showSyncingProgressDialog();
                OrderTask orderTask = mMokoService.writeResetEnergyTotal();
                MokoSupport.getInstance().sendOrder(orderTask);
            }
        });
        dialog.show(getSupportFragmentManager());
    }

    public void reset() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Reset Device");
        dialog.setMessage("After reset,the relevant data will be totally cleared");
        dialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
            @Override
            public void onClick() {
                showSyncingProgressDialog();
                OrderTask orderTask = mMokoService.writeReset();
                MokoSupport.getInstance().sendOrder(orderTask);
            }
        });
        dialog.show(getSupportFragmentManager());
    }

    public void changeName() {
        tvTitle.setText(MokoSupport.getInstance().advName);
    }
}
