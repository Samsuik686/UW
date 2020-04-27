package com.jimilab.uwclient.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import androidx.annotation.NonNull;

/**
 * @Author : LiangGuoChang
 * @Date : 219-10-29
 * @描述 :
 */
public class BaseResult<T> implements Serializable {

    @SerializedName("result")
    private int result;
    @SerializedName("data")
    private T data;

    public int getResultCode() {
        return result;
    }

    public void setResultCode(int resultCode) {
        this.result = resultCode;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @NonNull
    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
