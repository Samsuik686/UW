package com.jimilab.uwclient.view;

import com.jimilab.uwclient.bean.InMaterial;

import java.util.List;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-01
 * @描述 :
 */
public interface IUrgentView extends IBaseView {

    void initDisplay();

    void showMemoryTaskName(String taskName, List<InMaterial> list);

    void onLoadTaskFile(boolean daoExitName, String loadName, List<InMaterial> loadList);

    void setMaterialRequestFocus();

    void setBoxRequestFocus();

    void requestDisallowScan();

    void setAdapterNotifyDataChanged();

    void showDialog(String title, String msg, int condition);

}
