package com.moko.bluetoothplug.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.bluetoothplug.R;
import com.moko.bluetoothplug.activity.DeviceInfoActivity;
import com.moko.bluetoothplug.view.ArcProgress;
import com.moko.support.MokoConstants;
import com.moko.support.MokoSupport;
import com.moko.support.event.DataChangedEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PowerFragment extends Fragment {

    private static final String TAG = PowerFragment.class.getSimpleName();
    @Bind(R.id.arc_progress)
    ArcProgress arcProgress;
    @Bind(R.id.tv_power)
    TextView tvPower;
    @Bind(R.id.tv_onoff)
    TextView tvOnoff;
    @Bind(R.id.cv_onoff)
    CardView cvOnoff;
    @Bind(R.id.tv_overload)
    TextView tvOverload;
    private boolean switchState = false;
    private DeviceInfoActivity activity;

    public PowerFragment() {
    }

    public static PowerFragment newInstance() {
        PowerFragment fragment = new PowerFragment();
        return fragment;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataChangedEvent(DataChangedEvent event) {
        final int function = event.getFunction();
        switch (function) {
            case MokoConstants.NOTIFY_FUNCTION_SWITCH:
                int onoff = MokoSupport.getInstance().switchState;
                setOnOff(onoff);
                break;
            case MokoConstants.NOTIFY_FUNCTION_OVERLOAD:
                setOverLoad();
                break;
            case MokoConstants.NOTIFY_FUNCTION_ELECTRICITY:
                String electricityP = MokoSupport.getInstance().electricityP;
                float progress = Float.parseFloat(electricityP) * 0.1f;
                arcProgress.setProgress(progress);
                tvPower.setText(electricityP);
                break;
        }
    }

    private void setOnOff(int onoff) {
        if (onoff == 0) {
            switchState = false;
            cvOnoff.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white_ffffff));
            tvOnoff.setTextColor(ContextCompat.getColor(getActivity(), R.color.blue_2681ff));
        } else {
            switchState = true;
            cvOnoff.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.blue_2681ff));
            tvOnoff.setTextColor(ContextCompat.getColor(getActivity(), R.color.white_ffffff));
        }
    }

    private void setOverLoad() {
        cvOnoff.setCardBackgroundColor(ContextCompat.getColor(getActivity(), R.color.grey_d9d9d9));
        tvOnoff.setTextColor(ContextCompat.getColor(getActivity(), R.color.white_ffffff));
        cvOnoff.setEnabled(false);
        tvOverload.setVisibility(View.VISIBLE);
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
        View view = inflater.inflate(R.layout.fragment_power, container, false);
        ButterKnife.bind(this, view);
        int onoff = MokoSupport.getInstance().switchState;
        int overloadState = MokoSupport.getInstance().overloadState;
        if (overloadState == 0) {
            setOnOff(onoff);
        } else {
            setOverLoad();
        }
        String electricityP = MokoSupport.getInstance().electricityP;
        float progress = Float.parseFloat(electricityP) * 0.1f;
        arcProgress.setProgress(progress);
        tvPower.setText(electricityP);
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

//    int electricityP = 0;

    @OnClick({R.id.arc_progress, R.id.cv_onoff})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.arc_progress:
                // TODO: 2020/4/24 测试
//                electricityP += 8;
//                float progress = electricityP * 0.1f;
//                arcProgress.setProgress(progress);
//                tvPower.setText(String.valueOf(electricityP));
                break;
            case R.id.cv_onoff:
                activity.changeSwitchState(switchState);
                break;
        }
    }
}
