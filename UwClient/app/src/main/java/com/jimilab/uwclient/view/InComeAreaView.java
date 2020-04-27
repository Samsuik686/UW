package com.jimilab.uwclient.view;

import com.jimilab.uwclient.bean.BaseResult;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-26
 * @描述 :
 */
public interface InComeAreaView extends IBaseView {
    void showTaskList(String token, BaseResult taskList);

    void showMaterial(String[] material);

    void clearMaterial();

    void setScanMaterialRequestFocus();

    void showResult(int resCode, String res);

    void clearResult();
}
