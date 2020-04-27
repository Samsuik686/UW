package com.jimilab.uwclient.dao;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity
public class TaskDao {

    @Id(autoincrement = true)
    private Long taskId;
    private String taskName;//任务名
    private String taskSupplier;//任务供应商
    private String destination;//目的地
    private String destinationIndex;//目的地序号

    @Generated(hash = 908631775)
    public TaskDao(Long taskId, String taskName, String taskSupplier, String destination, String destinationIndex) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskSupplier = taskSupplier;
        this.destination = destination;
        this.destinationIndex = destinationIndex;
    }

    @Generated(hash = 1688873893)
    public TaskDao() {
    }

    public String getTaskName() {
        return this.taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskSupplier() {
        return this.taskSupplier;
    }

    public void setTaskSupplier(String taskSupplier) {
        this.taskSupplier = taskSupplier;
    }

    public String getDestination() {
        return this.destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestinationIndex() {
        return this.destinationIndex;
    }

    public void setDestinationIndex(String destinationIndex) {
        this.destinationIndex = destinationIndex;
    }

    public String taskDaoToString() {
        return this.taskName + " - " + this.taskSupplier + " - " + this.destination + " - " + this.destinationIndex;
    }

    public Long getTaskId() {
        return this.taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
}
