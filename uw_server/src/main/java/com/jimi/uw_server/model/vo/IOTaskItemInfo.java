package com.jimi.uw_server.model.vo;

import java.util.Date;

import com.jfinal.plugin.activerecord.Record;


public class IOTaskItemInfo {

	private Integer taskLogId;

	private Integer taskLogQuantity;

	private String materialId;

	private Integer materialQuantity;

	private Integer materialStatus;

	private Date taskLogTime;

	private Date productionTime;


	public Integer getTaskLogId() {
		return taskLogId;
	}


	public void setTaskLogId(Integer taskLogId) {
		this.taskLogId = taskLogId;
	}


	public Integer getTaskLogQuantity() {
		return taskLogQuantity;
	}


	public void setTaskLogQuantity(Integer taskLogQuantity) {
		this.taskLogQuantity = taskLogQuantity;
	}


	public String getMaterialId() {
		return materialId;
	}


	public void setMaterialId(String materialId) {
		this.materialId = materialId;
	}


	public Integer getMaterialQuantity() {
		return materialQuantity;
	}


	public void setMaterialQuantity(Integer materialQuantity) {
		this.materialQuantity = materialQuantity;
	}


	public Integer getMaterialStatus() {
		return materialStatus;
	}


	public void setMaterialStatus(Integer materialStatus) {
		this.materialStatus = materialStatus;
	}


	public Date getTaskLogTime() {
		return taskLogTime;
	}


	public void setTaskLogTime(Date taskLogTime) {
		this.taskLogTime = taskLogTime;
	}


	public Date getProductionTime() {
		return productionTime;
	}


	public void setProductionTime(Date productionTime) {
		this.productionTime = productionTime;
	}


	public static IOTaskItemInfo fill(Record record) {
		IOTaskItemInfo info = new IOTaskItemInfo();
		info.setMaterialId(record.getStr("TaskLog_MaterialId"));
		info.setTaskLogId(record.getInt("TaskLog_Id"));
		info.setMaterialQuantity(record.getInt("Material_RemainderQuantity"));
		info.setProductionTime(record.getDate("Material_ProductionTime"));
		info.setMaterialStatus(record.getInt("Material_Status"));
		info.setTaskLogQuantity(record.getInt("TaskLog_Quantity"));
		info.setTaskLogTime(record.getDate("TaskLog_Time"));
		return info;
	}
}
