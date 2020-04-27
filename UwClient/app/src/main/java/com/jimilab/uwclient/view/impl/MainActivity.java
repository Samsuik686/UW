package com.jimilab.uwclient.view.impl;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.jimilab.uwclient.BuildConfig;
import com.jimilab.uwclient.R;
import com.jimilab.uwclient.UwClientApplication;
import com.jimilab.uwclient.bean.Constant;
import com.jimilab.uwclient.utils.CheckPermissionUtils;
import com.jimilab.uwclient.view.custom.ItemView;
import com.jimilab.uwclient.view.impl.incomeArea.InComeAreaLoginActivity;
import com.jimilab.uwclient.view.impl.urgent.UrgentLoginActivity;
import com.jimilab.uwclient.view.impl.valuable.ValuableLoginActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";
    @BindView(R.id.tv_version)
    TextView tvVersion;
    @BindView(R.id.rl_urgent)
    ItemView rlUrgent;
    @BindView(R.id.rl_valuable)
    ItemView rlValuable;
    @BindView(R.id.rl_income)
    ItemView rlIncome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (getActionBar() != null) {
            getActionBar().hide();
        }
        setContentView(R.layout.activity_main);
        //使屏幕常亮
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //申请权限
        CheckPermissionUtils.initPermission(this);
        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        tvVersion.setText(BuildConfig.VERSION_NAME);
        rlIncome.setOnClickListener(mClickListener);
        rlValuable.setOnClickListener(mClickListener);
        rlUrgent.setOnClickListener(mClickListener);

    }

    private void resetItem(View view) {
        switch (view.getId()) {
            case R.id.rl_urgent:
                rlIncome.reset();
                rlValuable.reset();
                ((UwClientApplication) getApplication()).setIp(Constant.IP);
                startActivity(new Intent(this, UrgentLoginActivity.class));
                break;
            case R.id.rl_valuable:
                rlUrgent.reset();
                rlIncome.reset();
                ((UwClientApplication) getApplication()).setIp("http://10.10.11.90:8080/uw_server/");
                startActivity(new Intent(this, ValuableLoginActivity.class));
                break;
            case R.id.rl_income:
                rlUrgent.reset();
                rlValuable.reset();
                ((UwClientApplication) getApplication()).setIp("http://10.10.11.90:8080/uw_server/");
                startActivity(new Intent(this, InComeAreaLoginActivity.class));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private final View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            resetItem(view);
        }
    };
}
