package com.jimilab.uwclient.model;

import com.jimilab.uwclient.bean.InMaterial;

import java.util.HashSet;
import java.util.List;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-01
 * @描述 :
 */
public interface IUrgentModel {
    void getMemoryTaskNames(String key, OnGetMemoryTaskNames onGetMemoryTaskNames);

    void loadTaskFile(int requestCode, String curTaskName, String loadAbsolutePath, List<InMaterial> curAllMaterials, OnLoadTaskFileListener loadTaskFileListener);

    interface OnGetMemoryTaskNames {
        void onComplete(HashSet<String> taskNames);
    }

    interface OnLoadTaskFileListener {
        void onLoaded(boolean daoExitName, List<InMaterial> loadList);

        void onError(Throwable throwable);
    }
}
