package com.jimilab.uwclient;

import android.app.Application;
import android.content.Context;

import com.jeremyliao.liveeventbus.LiveEventBus;
import com.jimilab.uwclient.bean.Constant;
import com.jimilab.uwclient.dao.GreenDaoManager;
import com.jimilab.uwclient.utils.NetApi;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-10-29
 * @描述 :
 */
public class UwClientApplication extends Application {
    private static final String TAG = "UwClientApplication";
    private NetApi mNetApi;
    private Context mContext;
    private String ip = BuildConfig.BASE_URL;
    private static String TOKEN;

    public Context getAppContext() {
        return mContext;
    }

    public NetApi getNetApi() {
        return mNetApi;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //配置LiveEventBus
        LiveEventBus.config();
        mContext = this.getApplicationContext();


        GreenDaoManager.getInstance(mContext);

        setIp(Constant.IP);
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
        mNetApi = NetApi.NetApiFactory.getNetApi(this);
    }

    public String getTOKEN() {
        return TOKEN;
    }

    public void setTOKEN(String TOKEN) {
        UwClientApplication.TOKEN = TOKEN;
    }

}
