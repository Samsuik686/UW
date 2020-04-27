package com.jimilab.uwclient.view.impl.urgent;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.google.gson.Gson;
import com.jimilab.uwclient.UwClientApplication;
import com.jimilab.uwclient.bean.BaseResult;
import com.jimilab.uwclient.bean.Constant;
import com.jimilab.uwclient.utils.Log;
import com.jimilab.uwclient.utils.Tool;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class UrgentService extends Service {

    private static final String TAG = "UrgentService";
    private Thread mThread;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: ");
        mThread = new Thread(mRunnable);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");

        mThread.start();
        return new Binder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //if (mThread.isAlive()) {
        //    mThread.interrupt();
        //    mThread = null;
        //    if (mRunnable != null) {
        //        mRunnable = null;
        //    }
        //}
        Log.d(TAG, "onUnbind: ");
        return super.onUnbind(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand: ");

        return super.onStartCommand(intent, flags, startId);
    }


    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (mRunnable == null) {
                    break;
                }
                //判断UW是否可用
                ((UwClientApplication) getApplication()).getNetApi()
                        .ping()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<BaseResult<String>>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(BaseResult<String> stringBaseResult) {
                                if (stringBaseResult != null && stringBaseResult.getResultCode() == 200) {
                                    //发送消息
                                    Tool.postUWLiveState(Constant.UW_ALIVE);
                                } else {
                                    //发送消息
                                    Tool.postUWLiveState(Constant.UW_DOWNTIME);
                                }

                                Log.d(TAG, "onNext: " + ((UwClientApplication) getApplication()).getIp() + "\n" +
                                        new Gson().toJson(stringBaseResult));
                            }

                            @Override
                            public void onError(Throwable e) {
                                //发送消息
                                Tool.postUWLiveState(Constant.UW_DOWNTIME);
                                Log.d(TAG, "onError: " + ((UwClientApplication) getApplication()).getIp() + " -- " +
                                        e.toString());

                            }

                            @Override
                            public void onComplete() {

                            }
                        });

                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
    };


    @Override
    public void onDestroy() {
        if (mThread != null && mThread.isAlive()) {
            mThread.interrupt();
            mThread = null;
            if (mRunnable != null) {
                mRunnable = null;
            }
        }
        stopSelf();
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }
}
