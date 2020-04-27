package com.jimilab.uwclient.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-21
 * @描述 :
 */
public class ValuableBaseResult {
    /**
     * result : 200
     * data : operation succeed
     */
    @SerializedName("result")
    private String result;
    @SerializedName("data")
    private String data;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @NonNull
    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
