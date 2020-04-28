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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.moko.bluetoothplug.AppConstants;
import com.moko.bluetoothplug.PlugInfoParseableImpl;
import com.moko.bluetoothplug.R;
import com.moko.bluetoothplug.adapter.PlugListAdapter;
import com.moko.bluetoothplug.dialog.AlertMessageDialog;
import com.moko.bluetoothplug.dialog.LoadingDialog;
import com.moko.bluetoothplug.dialog.LoadingMessageDialog;
import com.moko.bluetoothplug.dialog.ScanFilterDialog;
import com.moko.bluetoothplug.entity.PlugInfo;
import com.moko.bluetoothplug.service.MokoService;
import com.moko.bluetoothplug.utils.ToastUtils;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.event.ConnectStatusEvent;
import com.moko.support.task.OrderTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends BaseActivity implements MokoScanDeviceCallback, BaseQuickAdapter.OnItemClickListener {


    @Bind(R.id.rv_devices)
    RecyclerView rvDevices;
    @Bind(R.id.iv_refresh)
    ImageView ivRefresh;
    @Bind(R.id.tv_device_num)
    TextView tvDeviceNum;
    @Bind(R.id.tv_filter)
    TextView tvFilter;
    @Bind(R.id.rl_filter)
    RelativeLayout rlFilter;
    @Bind(R.id.rl_edit_filter)
    RelativeLayout rlEditFilter;
    private MokoService mMokoService;
    private boolean mReceiverTag = false;
    private HashMap<String, PlugInfo> plugInfoHashMap;
    private ArrayList<PlugInfo> plugInfos;
    private PlugInfoParseableImpl plugInfoParseable;
    private PlugListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bindService(new Intent(this, MokoService.class), mServiceConnection, BIND_AUTO_CREATE);
        EventBus.getDefault().register(this);
        plugInfoHashMap = new HashMap<>();
        plugInfos = new ArrayList<>();
        adapter = new PlugListAdapter();
        adapter.replaceData(plugInfos);
        adapter.setOnItemClickListener(this);
        adapter.openLoadAnimation();
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        rvDevices.setAdapter(adapter);
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
            filter.setPriority(100);
            registerReceiver(mReceiver, filter);
            mReceiverTag = true;
            if (!MokoSupport.getInstance().isBluetoothOpen()) {
                // 蓝牙未打开，开启蓝牙
                MokoSupport.getInstance().enableBluetooth();
            } else {
                if (animation == null) {
                    startScan();
                }
            }
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
                if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {

                }
                if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                    dismissLoadingMessageDialog();

                }
                if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                }
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            if (animation != null) {
                                mMokoService.mHandler.removeMessages(0);
                                MokoSupport.getInstance().stopScanDevice();
                                onStopScan();
                            }
                            break;
                        case BluetoothAdapter.STATE_ON:
                            if (animation == null) {
                                startScan();
                            }
                            break;

                    }
                }
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
            // 设备断开，通知页面更新
            dismissLoadingProgressDialog();
            if (animation == null) {
                ToastUtils.showToast(MainActivity.this, "Disconnected");
                startScan();
            }
        }
        if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
            // 设备连接成功，通知页面更新
            dismissLoadingProgressDialog();
            showLoadingMessageDialog();
            mMokoService.mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    ArrayList<OrderTask> orderTasks = new ArrayList<>();
                    orderTasks.add(mMokoService.writeSystemTime());
                    orderTasks.add(mMokoService.readAdvInterval());
                    orderTasks.add(mMokoService.readAdvName());
                    orderTasks.add(mMokoService.readCountdown());
                    orderTasks.add(mMokoService.readElectricity());
                    orderTasks.add(mMokoService.readEnergyHistory());
                    orderTasks.add(mMokoService.readEnergyHistoryToday());
                    orderTasks.add(mMokoService.readEnergySavedParams());
                    orderTasks.add(mMokoService.readEnergyTotal());
                    orderTasks.add(mMokoService.readFirmwareVersion());
                    orderTasks.add(mMokoService.readLoadState());
                    orderTasks.add(mMokoService.readMac());
                    orderTasks.add(mMokoService.readOverloadTopValue());
                    orderTasks.add(mMokoService.readOverloadValue());
                    orderTasks.add(mMokoService.readPowerState());
                    orderTasks.add(mMokoService.readSwitchState());
                    MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
                }
            }, 1000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case AppConstants.REQUEST_CODE_ENABLE_BT:

                    break;

            }
        } else {
            switch (requestCode) {
                case AppConstants.REQUEST_CODE_ENABLE_BT:
                    // 未打开蓝牙
                    MainActivity.this.finish();
                    break;
            }
        }
    }

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
        stopService(new Intent(this, MokoService.class));
    }

    private void startScan() {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
        findViewById(R.id.iv_refresh).startAnimation(animation);
        plugInfoParseable = new PlugInfoParseableImpl();
        MokoSupport.getInstance().startScanDevice(this);
        mMokoService.mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MokoSupport.getInstance().stopScanDevice();
            }
        }, 1000 * 60);
    }


    @Override
    public void onStartScan() {
        plugInfoHashMap.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (animation != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.replaceData(plugInfos);
                            tvDeviceNum.setText(String.format("DEVICE(%d)", plugInfos.size()));
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updateDevices();
                }
            }
        }).start();
    }

    @Override
    public void onScanDevice(DeviceInfo deviceInfo) {
        final PlugInfo plugInfo = plugInfoParseable.parseDeviceInfo(deviceInfo);
        if (plugInfo == null)
            return;
        plugInfoHashMap.put(String.valueOf(plugInfo.rssi), plugInfo);
    }

    @Override
    public void onStopScan() {
        findViewById(R.id.iv_refresh).clearAnimation();
        animation = null;
    }

    private void updateDevices() {
        plugInfos.clear();
        if (!TextUtils.isEmpty(filterName) || filterRssi != -100) {
            ArrayList<PlugInfo> plugInfosFilter = new ArrayList<>(plugInfoHashMap.values());
            Iterator<PlugInfo> iterator = plugInfosFilter.iterator();
            while (iterator.hasNext()) {
                PlugInfo plugInfo = iterator.next();
                if (plugInfo.rssi > filterRssi) {
                    if (TextUtils.isEmpty(filterName)) {
                        continue;
                    } else {
                        if (TextUtils.isEmpty(plugInfo.name) && TextUtils.isEmpty(plugInfo.mac)) {
                            iterator.remove();
                        } else if (TextUtils.isEmpty(plugInfo.name) && plugInfo.mac.toLowerCase().replaceAll(":", "").contains(filterName.toLowerCase())) {
                            continue;
                        } else if (TextUtils.isEmpty(plugInfo.mac) && plugInfo.name.toLowerCase().contains(filterName.toLowerCase())) {
                            continue;
                        } else if (!TextUtils.isEmpty(plugInfo.name) && !TextUtils.isEmpty(plugInfo.mac) && (plugInfo.name.toLowerCase().contains(filterName.toLowerCase()) || plugInfo.mac.toLowerCase().replaceAll(":", "").contains(filterName.toLowerCase()))) {
                            continue;
                        } else {
                            iterator.remove();
                        }
                    }
                } else {
                    iterator.remove();
                }
            }
            plugInfos.addAll(plugInfosFilter);
        } else {
            plugInfos.addAll(plugInfoHashMap.values());
        }
        Collections.sort(plugInfos, new Comparator<PlugInfo>() {
            @Override
            public int compare(PlugInfo lhs, PlugInfo rhs) {
                if (lhs.rssi > rhs.rssi) {
                    return -1;
                } else if (lhs.rssi < rhs.rssi) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private Animation animation = null;
    public String filterName;
    public int filterRssi = -100;


    @OnClick({R.id.iv_refresh, R.id.iv_about, R.id.rl_edit_filter, R.id.rl_filter, R.id.iv_filter_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_refresh:
                if (isWindowLocked())
                    return;
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    // 蓝牙未打开，开启蓝牙
                    MokoSupport.getInstance().enableBluetooth();
                    return;
                }
                if (animation == null) {
                    startScan();
                } else {
                    mMokoService.mHandler.removeMessages(0);
                    MokoSupport.getInstance().stopScanDevice();
                }
                break;
            case R.id.iv_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.rl_edit_filter:
            case R.id.rl_filter:
                if (animation != null) {
                    mMokoService.mHandler.removeMessages(0);
                    MokoSupport.getInstance().stopScanDevice();
                }
                ScanFilterDialog scanFilterDialog = new ScanFilterDialog();
                scanFilterDialog.setFilterName(filterName);
                scanFilterDialog.setFilterRssi(filterRssi);
                scanFilterDialog.setOnScanFilterListener(new ScanFilterDialog.OnScanFilterListener() {
                    @Override
                    public void onDone(String filterName, int filterRssi) {
                        MainActivity.this.filterName = filterName;
                        MainActivity.this.filterRssi = filterRssi;
                        if (!TextUtils.isEmpty(filterName) || filterRssi != -100) {
                            rlFilter.setVisibility(View.VISIBLE);
                            rlEditFilter.setVisibility(View.GONE);
                            StringBuilder stringBuilder = new StringBuilder();
                            if (!TextUtils.isEmpty(filterName)) {
                                stringBuilder.append(filterName);
                                stringBuilder.append(";");
                            }
                            if (filterRssi != -100) {
                                stringBuilder.append(String.format("%sdBm", filterRssi + ""));
                                stringBuilder.append(";");
                            }
                            tvFilter.setText(stringBuilder.toString());
                        } else {
                            rlFilter.setVisibility(View.GONE);
                            rlEditFilter.setVisibility(View.VISIBLE);
                        }
                        if (isWindowLocked())
                            return;
                        if (animation == null) {
                            startScan();
                        }
                    }

                    @Override
                    public void onDismiss() {
                        if (isWindowLocked())
                            return;
                        if (animation == null) {
                            startScan();
                        }
                    }
                });
                scanFilterDialog.show(getSupportFragmentManager());
                break;
            case R.id.iv_filter_delete:
                if (animation != null) {
                    mMokoService.mHandler.removeMessages(0);
                    MokoSupport.getInstance().stopScanDevice();
                }
                rlFilter.setVisibility(View.GONE);
                rlEditFilter.setVisibility(View.VISIBLE);
                filterName = "";
                filterRssi = -100;
                if (isWindowLocked())
                    return;
                if (animation == null) {
                    startScan();
                }
                break;
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        final PlugInfo plugInfo = (PlugInfo) adapter.getItem(position);
        if (plugInfo != null && !isFinishing()) {
            if (animation != null) {
                mMokoService.mHandler.removeMessages(0);
                MokoSupport.getInstance().stopScanDevice();
            }
            showLoadingProgressDialog();
            ivRefresh.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMokoService.connectBluetoothDevice(plugInfo.mac);
                }
            }, 1000);
        }
    }

    private LoadingDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new LoadingDialog();
        mLoadingDialog.show(getSupportFragmentManager());

    }

    private void dismissLoadingProgressDialog() {
        if (mLoadingDialog != null)
            mLoadingDialog.dismissAllowingStateLoss();
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    private void showLoadingMessageDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Verifying..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    private void dismissLoadingMessageDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage(R.string.main_exit_tips);
        dialog.setOnAlertConfirmListener(new AlertMessageDialog.OnAlertConfirmListener() {
            @Override
            public void onClick() {
                MainActivity.this.finish();
            }
        });
        dialog.show(getSupportFragmentManager());
    }
}
