package com.jimilab.uwclient.bean;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * @Author : LiangGuoChang
 * @Date : 2019-11-15
 * @描述 : 出库要同步订阅的数据
 */
public class UrgentBean {

    private List<InMaterial> curAllMaterial;
    private int curMaterialIndex;
    private int curTaskState;
    private boolean taskSave;
    private int destinationIndex;
    private String taskName;
    private boolean isExit;

    public List<InMaterial> getCurAllMaterial() {
        return curAllMaterial;
    }

    public void setCurAllMaterial(List<InMaterial> curAllMaterial) {
        this.curAllMaterial = curAllMaterial;
    }

    public int getCurMaterialIndex() {
        return curMaterialIndex;
    }

    public void setCurMaterialIndex(int curMaterialIndex) {
        this.curMaterialIndex = curMaterialIndex;
    }

    public int getCurTaskState() {
        return curTaskState;
    }

    public void setCurTaskState(int curTaskState) {
        this.curTaskState = curTaskState;
    }

    public boolean isTaskSave() {
        return taskSave;
    }

    public void setTaskSave(boolean taskSave) {
        this.taskSave = taskSave;
    }

    public int getDestinationIndex() {
        return destinationIndex;
    }

    public void setDestinationIndex(int destinationIndex) {
        this.destinationIndex = destinationIndex;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public boolean isExit() {
        return isExit;
    }

    public void setExit(boolean exit) {
        isExit = exit;
    }

    @NonNull
    @Override
    public String toString() {
        return "curMaterialIndex: " + curMaterialIndex + " curTaskState: " + curTaskState
                + " taskSave: " + taskSave + " destinationIndex: " + destinationIndex
                + " taskName: " + taskName + " isExit: " + isExit;
    }
}
