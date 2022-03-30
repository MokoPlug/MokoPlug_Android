package com.moko.bluetoothplug.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.elvishew.xlog.XLog;
import com.moko.bluetoothplug.R;
import com.moko.bluetoothplug.dialog.AlertMessageDialog;
import com.moko.bluetoothplug.utils.Utils;
import com.moko.mokoplugpre.activity.PreMainActivity;
import com.moko.mokoplugpro.activity.ProMainActivity;

import butterknife.ButterKnife;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        StringBuffer buffer = new StringBuffer();
        // 记录机型
        buffer.append("机型：");
        buffer.append(android.os.Build.MODEL);
        buffer.append("=====");
        // 记录版本号
        buffer.append("手机系统版本：");
        buffer.append(android.os.Build.VERSION.RELEASE);
        buffer.append("=====");
        // 记录APP版本
        buffer.append("APP版本：");
        buffer.append(Utils.getVersionInfo(this));
        XLog.d(buffer.toString());
    }

    @Override
    public void onBackPressed() {
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage(R.string.main_exit_tips);
        dialog.setOnAlertConfirmListener(() -> MainActivity.this.finish());
        dialog.show(getSupportFragmentManager());
    }


    public void gotoMK114B(View view) {
        XLog.d("打开MK114B");
        Intent intent = new Intent(this, PreMainActivity.class);
        intent.putExtra("deviceType", 0);
        startActivity(intent);
    }

    public void gotoMK115B(View view) {
        XLog.d("打开MK115B");
        Intent intent = new Intent(this, PreMainActivity.class);
        intent.putExtra("deviceType", 1);
        startActivity(intent);
    }

    public void gotoMK116B(View view) {
        XLog.d("打开MK116B");
        Intent intent = new Intent(this, PreMainActivity.class);
        intent.putExtra("deviceType", 2);
        startActivity(intent);
    }

    public void gotoMK117B(View view) {
        XLog.d("打开MK117B");
        startActivity(new Intent(this, ProMainActivity.class));
    }

    public void onAbout(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }
}
