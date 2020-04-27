package com.jimilab.uwclient.model;

import com.jimilab.uwclient.bean.EmergencyTask;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-26
 * @描述 :
 */
public interface InComeAreaModel {


    void loadTaskList(String token, onLoadListener loadListener);

    void uploadMaterialItem(String taskID, String[] material, onItemLoadListener itemLoadListener);

    interface onItemLoadListener {
        void onComplete(String[] item);

        void onError(Throwable throwable);
    }

    interface onLoadListener {
        void onComplete(EmergencyTask emergencyTask);

        void onError(Throwable throwable);
    }
}
