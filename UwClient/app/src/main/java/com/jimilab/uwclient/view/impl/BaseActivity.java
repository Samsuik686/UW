package com.jimilab.uwclient.view.impl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.jimilab.uwclient.UwClientApplication;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.utils.Tool;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-10-31
 * @描述 :
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();
    private static final int EXIT_CODE = 110;
    private static boolean isExit = false;
    protected Context mContext;
    protected UwClientApplication mApplication;

    @SuppressLint("HandlerLeak")
    private static final Handler baseHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case EXIT_CODE:
                    isExit = false;
                    break;
            }
        }
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();
        mApplication = (UwClientApplication) getApplication();

    }

    @Override
    public void onBackPressed() {
        exit();
    }

    //返回主页
    protected void exit() {
        Log.d(TAG, "exit: " + isExit);
        if (!isExit) {
            isExit = true;
            showToast("再按一次退出");
            // 利用handler延迟2s发送更改状态信息
            Message message = Message.obtain();
            message.what = EXIT_CODE;
            baseHandler.sendMessageDelayed(message, 2000);
        } else {
            this.finish();
        }
    }

    protected void showToast(String msg) {
        Tool.showToast(this, msg);
    }

    protected void showLoading(String msg) {
        Tool.showLoading(this, msg);
    }

    protected void dismissLoading() {
        Tool.dismissLoading();
    }

}
