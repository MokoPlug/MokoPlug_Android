package com.moko.bluetoothplug.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.moko.bluetoothplug.R;
import com.moko.bluetoothplug.dialog.LoadingMessageDialog;
import com.moko.bluetoothplug.service.MokoService;
import com.moko.bluetoothplug.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.entity.OrderEnum;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.task.OrderTaskResponse;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ModifyPowerStatusActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {


    public MokoService mMokoService;
    @Bind(R.id.rb_switch_off)
    RadioButton rbSwitchOff;
    @Bind(R.id.rb_switch_on)
    RadioButton rbSwitchOn;
    @Bind(R.id.rb_last_status)
    RadioButton rbLastStatus;
    @Bind(R.id.rg_power_status)
    RadioGroup rgPowerStatus;
    private boolean mReceiverTag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_status);
        ButterKnife.bind(this);

        int powerstate = MokoSupport.getInstance().powerState;
        switch (powerstate) {
            case 0:
                rbSwitchOff.setChecked(true);
                break;
            case 1:
                rbSwitchOn.setChecked(true);
                break;
            case 2:
                rbLastStatus.setChecked(true);
                break;
        }
        rgPowerStatus.setOnCheckedChangeListener(this);
        Intent intent = new Intent(this, MokoService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                    if (MokoSupport.getInstance().isBluetoothOpen()) {
                        dismissSyncProgressDialog();
                        finish();
                    }
                }
            }
        });
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
            filter.setPriority(300);
            registerReceiver(mReceiver, filter);
            mReceiverTag = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent != null) {
                String action = intent.getAction();
                if (!BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    abortBroadcast();
                }
                if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
                    ToastUtils.showToast(ModifyPowerStatusActivity.this, R.string.timeout);
                }
                if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                    dismissSyncProgressDialog();
                }
                if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                    OrderTaskResponse response = (OrderTaskResponse) intent.getSerializableExtra(MokoConstants.EXTRA_KEY_RESPONSE_ORDER_TASK);
                    OrderEnum order = response.order;
                    byte[] value = response.responseValue;
                    switch (order) {
                        case WRITE_POWER_STATE:
                            if (0 == (value[3] & 0xFF)) {
                                MokoSupport.getInstance().powerState = powerState;
                                ToastUtils.showToast(ModifyPowerStatusActivity.this, R.string.success);
                                ModifyPowerStatusActivity.this.setResult(ModifyPowerStatusActivity.this.RESULT_OK);
                                finish();
                            } else {
                                ToastUtils.showToast(ModifyPowerStatusActivity.this, R.string.failed);
                            }
                            break;
                    }
                }
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
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

    int powerState;

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.rb_switch_off:
                powerState = 0;
                break;
            case R.id.rb_switch_on:
                powerState = 1;
                break;
            case R.id.rb_last_status:
                powerState = 2;
                break;
        }
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(mMokoService.writePowerState(powerState));
    }

    public void back(View view) {
        finish();
    }
}
