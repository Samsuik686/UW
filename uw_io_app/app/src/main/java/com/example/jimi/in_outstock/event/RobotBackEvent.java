package com.example.jimi.in_outstock.event;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.jimi.in_outstock.application.MyApplication;
import com.example.jimi.in_outstock.common.UrlData;

import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 叉车回库事件
 */
public class RobotBackEvent {
    private int taskId;
    // 返回成功
    private static final int SUCCESS_NUM = 200;
    // 返回失败
    private static final int FAILL_NUM = 0;

    public void robotBack(int myTaskId){
        Log.d("taskId",myTaskId+"");
        taskId = myTaskId;
        new RobotBackThread().start();
    }


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case SUCCESS_NUM:
                    Toast.makeText(MyApplication.getContext(),"叉车回库中",Toast.LENGTH_SHORT).show();
                    break;
                case FAILL_NUM:
                    Toast.makeText(MyApplication.getContext(),"叉车回库失败",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 启用线程请求
     */
    class RobotBackThread extends Thread{

        @Override
        public void run() {
            // 请求的网址
            RequestParams params = new RequestParams(UrlData.getUrlRobotback());
            params.addBodyParameter("id",taskId+"");
            params.addBodyParameter("#TOKEN#", MyApplication.getToken());
            x.http().post(params, new Callback.CacheCallback<String>() {
                @Override
                public void onCancelled(CancelledException cex) {

                }

                @Override
                public void onSuccess(String result) {
                    result= new String(result.trim());
                    Log.d("RobotBack--onSuccess",result);
                    Message message = new Message();
                    message.what = pareJSON(result);
                    handler.sendMessage(message);
                }

                @Override
                public void onError(Throwable ex, boolean isOnCallback) {
                    Log.d("RobotBack--onError :",ex.getMessage());
                }

                @Override
                public void onFinished() {

                }

                @Override
                public boolean onCache(String result) {
                    return false;
                }
            });

        }
    }

    /**
     * 数据解析
     * @param jsonData
     * @return
     */
    private int pareJSON(String jsonData){
        int result = FAILL_NUM;
        try{
            JSONObject jsonObject = new JSONObject(jsonData);
            result = jsonObject.getInt("result");
        }catch (Exception e){
            e.printStackTrace();
        }
        return  result;
    }
}
