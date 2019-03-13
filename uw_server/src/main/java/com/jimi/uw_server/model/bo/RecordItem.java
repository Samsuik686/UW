package com.jimi.uw_server.model.bo;

import java.util.Date;

/**
 * 出入库记录业务对象
 * @author HardyYao
 * @createTime 2018年11月14日  下午2:38:54
 */

public class RecordItem {

	private Integer materialTypeId;

	private String fileName;

	private String taskType;

	private Integer planQuantity;

	private Integer actualQuantity;

	/**
	 * 累计超发数
	 */
	private Integer superIssuedQuantity;

	private String operator;

	private Date ioTime;


	public RecordItem(Integer materialTypeId, Integer planQuantity, String fileName, Integer type, Integer actualQuantity, Integer superIssuedQuantity, String operator, Date ioTime) {
		this.materialTypeId = materialTypeId;
		this.fileName = fileName;
		this.setTaskType(type);
		this.planQuantity = planQuantity;
		this.actualQuantity = actualQuantity;
		this.superIssuedQuantity = superIssuedQuantity;
		this.operator = operator;
		this.ioTime = ioTime;
	}

	public Integer getMaterialTypeId() {
		return materialTypeId;
	}

	public void setMaterialTypeId(Integer materialTypeId) {
		this.materialTypeId = materialTypeId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getTaskType() {
		return this.taskType;
	}

	public void setTaskType(Integer type) {
		if (type == 0) {
			this.taskType = "入库";
		} else if (type == 1) {
			this.taskType = "出库";
		} else if (type == 4) {
			this.taskType = "退料";
		}
	}

	public Integer getPlanQuantity() {
		return planQuantity;
	}

	public void setPlanQuantity(Integer planQuantity) {
		this.planQuantity = planQuantity;
	}

	public Integer getActualQuantity() {
		return actualQuantity;
	}

	public void setActualQuantity(Integer actualQuantity) {
		this.actualQuantity = actualQuantity;
	}

	public Integer getSuperIssuedQuantity() {
		return superIssuedQuantity;
	}

	public void setSuperIssuedQuantity(Integer superIssuedQuantity) {
		this.superIssuedQuantity = superIssuedQuantity;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Date getIoTime() {
		return ioTime;
	}

	public void setIoTime(Date ioTime) {
		this.ioTime = ioTime;
	}


}
