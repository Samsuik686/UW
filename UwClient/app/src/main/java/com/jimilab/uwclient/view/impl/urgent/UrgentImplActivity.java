package com.jimilab.uwclient.view.impl.urgent;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jimilab.uwclient.R;
import com.jimilab.uwclient.bean.Constant;
import com.jimilab.uwclient.bean.InMaterial;
import com.jimilab.uwclient.bean.UrgentBean;
import com.jimilab.uwclient.utils.CacheMemory;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.utils.Tool;
import com.jimilab.uwclient.view.IBaseView;
import com.jimilab.uwclient.view.impl.BaseActivity;

import java.util.HashSet;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UrgentImplActivity extends BaseActivity {

    private static final String TAG = UrgentImplActivity.class.getSimpleName();
    @BindView(R.id.iv_back)
    RelativeLayout ivBack;
    @BindView(R.id.tv_in_ware)
    TextView tvInWare;
    @BindView(R.id.tv_out_ware)
    TextView tvOutWare;
    @BindView(R.id.layouts_content)
    FrameLayout layoutsContent;
    private FragmentManager fragmentManager;
    private OutFragment outFragment;
    private InfoFragment infoFragment;
    private FragmentTransaction transaction;
    private Intent serviceIntent;
    private List<InMaterial> curAllMaterial;
    private int curMaterialIndex = -1;
    private int curTaskState = Constant.TASK_NO_CREATE;
    private boolean taskSave = false;
    private int destinationIndex = -1;
    private String taskName;
    public CacheMemory mCacheMemory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.urgent_activity_operate);
        ButterKnife.bind(this);

        mCacheMemory = CacheMemory.getmCacheMemory(this, 8);
        registerLiveData();
        serviceIntent = new Intent(this, UrgentService.class);
        //bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        //startService(serviceIntent);
        fragmentManager = getSupportFragmentManager();

        setTabSelection(0);
    }

    private void registerLiveData() {
        //监听当前操作的数据
        Tool.getUrgentBean()
                .observe(this, new Observer<UrgentBean>() {
                    @Override
                    public void onChanged(UrgentBean urgentBean) {
                        if (urgentBean != null) {
                            curAllMaterial = urgentBean.getCurAllMaterial();
                            curMaterialIndex = urgentBean.getCurMaterialIndex();
                            curTaskState = urgentBean.getCurTaskState();
                            taskSave = urgentBean.isTaskSave();
                            destinationIndex = urgentBean.getDestinationIndex();
                            taskName = urgentBean.getTaskName();
                            Log.d(TAG, "postUrgentBean: " + urgentBean.toString());
                            if (urgentBean.isExit()) {
                                checkIsExit();
                            }
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        Log.d(TAG, "onStart: ");
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    private void setTabSelection(int i) {
        transaction = fragmentManager.beginTransaction();
        switch (i) {
            case 0:
                if (null == outFragment) {
                    resetTitle();
                    hideFragments(transaction);
                    tvInWare.setBackgroundResource(R.drawable.operate_left_select);
                    outFragment = new OutFragment();
                    transaction.add(R.id.layouts_content, outFragment);
                } else if (outFragment.isHidden()) {
                    resetTitle();
                    hideFragments(transaction);
                    tvInWare.setBackgroundResource(R.drawable.operate_left_select);
                    transaction.show(outFragment);
                }
                break;

            case 1:
                if (null == infoFragment) {
                    resetTitle();
                    hideFragments(transaction);
                    tvOutWare.setBackgroundResource(R.drawable.operate_right_select);
                    infoFragment = new InfoFragment();
                    transaction.add(R.id.layouts_content, infoFragment);
                } else if (infoFragment.isHidden()) {
                    resetTitle();
                    hideFragments(transaction);
                    tvOutWare.setBackgroundResource(R.drawable.operate_right_select);
                    transaction.show(infoFragment);
                }
                break;

        }
        transaction.commit();
    }

    @OnClick({R.id.iv_back, R.id.tv_in_ware, R.id.tv_out_ware})
    public void onViewClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                //checkIsExit();
                Tool.postRequestUrgentBean(true);
                break;
            case R.id.tv_in_ware:
                setTabSelection(0);
                break;
            case R.id.tv_out_ware:
                setTabSelection(1);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        //checkIsExit();
        Tool.postRequestUrgentBean(true);
    }

    //按退出时，判断是否可以退出
    private void checkIsExit() {
        int res = -1;
        if (curAllMaterial != null) {
            res = Tool.isOutWareComplete(curAllMaterial, curMaterialIndex);
        }

        Log.d(TAG, "checkIsExit: " + res);

        if ((res == 0 || res == 2) && curTaskState == Constant.TASK_NO_CREATE) {
            if (destinationIndex > 0 && taskSave) {
                Tool.showDialog(new IBaseView.onDialogListener() {
                    @Override
                    public void onPositiveClick(DialogInterface dialog, int which, int condition) {

                        Log.d(TAG, "onPositiveClick: " + condition);
                        if (condition == 0) {//缓存出库信息
                            try {
                                if (!(taskName.isEmpty()) && (curAllMaterial != null && curAllMaterial.size() > 0)) {
                                    HashSet<String> taskNames = mCacheMemory.getTaskNames("taskNames");
                                    if (taskNames == null) {
                                        taskNames = new HashSet<>();
                                    }
                                    taskNames.add(taskName);
                                    mCacheMemory.putTaskNames("taskNames", taskNames);
                                    mCacheMemory.putList(taskName, curAllMaterial);
                                }

                            } catch (Exception e) {
                                Log.d(TAG, "mCacheMemory - " + e.toString());
                            }
                        }
                        //退出
                        finish();
                    }

                    @Override
                    public void onNegativeClick(DialogInterface dialog, int which, int condition) {

                    }
                }, this, "警告", "当前出库任务未完成!\n\n       确定退出?", 0);
            } else if (destinationIndex < 0) {
                Tool.showToast(getApplicationContext(), "请先为该任务选择目的地！");
            } else if (!taskSave) {
                Tool.showToast(getApplicationContext(), "请保存该任务目的地！");
            }
        } else {
            //退出
            UrgentImplActivity.super.onBackPressed();
        }
    }

    //设置标题为原状态
    private void resetTitle() {
        tvInWare.setBackgroundResource(R.drawable.operate_left_unselect);
        tvOutWare.setBackgroundResource(R.drawable.operate_right_unselect);
    }

    private void hideFragments(FragmentTransaction transaction) {
        if (null != outFragment && outFragment.isVisible()) {
            transaction.hide(outFragment);
        }
        if (null != infoFragment && infoFragment.isVisible()) {
            transaction.hide(infoFragment);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
        Log.d(TAG, "onStop: ");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: " + curAllMaterial);

        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

}
