package com.jimilab.uwclient.view.impl.valuable;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jimilab.uwclient.R;
import com.jimilab.uwclient.view.impl.BaseActivity;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ValuableImplActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    RelativeLayout ivBack;
    @BindView(R.id.tv_check)
    TextView tvCheck;
    @BindView(R.id.tv_sampling)
    TextView tvSampling;
    @BindView(R.id.tv0)
    TextView tv0;
    @BindView(R.id.iv_type_icon)
    ImageView ivTypeIcon;
    @BindView(R.id.tv_operator)
    TextView tvOperator;
    @BindView(R.id.layouts_content)
    FrameLayout layoutsContent;

    private FragmentManager fragmentManager;
    private CheckFragment checkFragment;
    private SamplingFragment samplingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valuable_impl);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String intentUsr = intent.getStringExtra("usrname");
        tvOperator.setText(intentUsr);
        fragmentManager = getSupportFragmentManager();
        setTabSelection(0);
    }

    @OnClick({R.id.iv_back, R.id.tv_check, R.id.tv_sampling})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                exit();
                break;
            case R.id.tv_check:
                setTabSelection(0);
                break;
            case R.id.tv_sampling:
                setTabSelection(1);
                break;
        }
    }

    //选择操作项
    private void setTabSelection(int index) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        switch (index) {

            case 0:
                if (null == checkFragment) {
                    resetTitle();
                    hideFragments(transaction);
                    tvCheck.setBackgroundResource(R.drawable.operate_left_select);
                    ivTypeIcon.setImageResource(R.mipmap.check);
                    checkFragment = new CheckFragment();
                    transaction.add(R.id.layouts_content, checkFragment);
                } else if (checkFragment.isHidden()) {
                    resetTitle();
                    hideFragments(transaction);
                    tvCheck.setBackgroundResource(R.drawable.operate_left_select);
                    ivTypeIcon.setImageResource(R.mipmap.check);
                    transaction.show(checkFragment);
                }
                break;

            case 1:
                if (null == samplingFragment) {
                    resetTitle();
                    hideFragments(transaction);
                    tvSampling.setBackgroundResource(R.drawable.operate_right_select);
                    ivTypeIcon.setImageResource(R.mipmap.sampling);
                    samplingFragment = new SamplingFragment();
                    transaction.add(R.id.layouts_content, samplingFragment);
                } else if (samplingFragment.isHidden()) {
                    resetTitle();
                    hideFragments(transaction);
                    tvSampling.setBackgroundResource(R.drawable.operate_right_select);
                    ivTypeIcon.setImageResource(R.mipmap.sampling);
                    transaction.show(samplingFragment);
                }
                break;

        }

        transaction.commit();
    }

    //设置标题为原状态
    private void resetTitle() {
        tvCheck.setBackgroundResource(R.drawable.operate_left_unselect);
        tvSampling.setBackgroundResource(R.drawable.operate_right_unselect);
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (null != checkFragment && checkFragment.isVisible()) {
            transaction.hide(checkFragment);
        }
        if (null != samplingFragment && samplingFragment.isVisible()) {
            transaction.hide(samplingFragment);
        }
    }

}
