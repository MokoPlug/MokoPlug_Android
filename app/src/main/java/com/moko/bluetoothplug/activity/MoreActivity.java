package com.moko.bluetoothplug.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.moko.bluetoothplug.R;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.event.ConnectStatusEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @Date 2020/4/30
 * @Author wenzheng.liu
 * @Description
 * @ClassPath com.moko.bluetoothplug.activity.MoreActivity
 */
public class MoreActivity extends BaseActivity {


    @Bind(R.id.tv_title)
    TextView tvTitle;
    @Bind(R.id.tv_product_name)
    TextView tvProductName;
    @Bind(R.id.tv_firmware_version)
    TextView tvFirmwareVersion;
    @Bind(R.id.tv_device_mac)
    TextView tvDeviceMac;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        tvTitle.setText(MokoSupport.getInstance().advName);
        tvProductName.setText(MokoSupport.getInstance().advName);
        tvFirmwareVersion.setText(MokoSupport.getInstance().firmwareVersion);
        tvDeviceMac.setText(MokoSupport.getInstance().mac);
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_CONN_STATUS_DISCONNECTED.equals(action)) {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void back(View view) {
        finish();
    }
}
