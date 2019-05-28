package com.jimi.uw_server.model.vo;

import java.util.Date;

/**
 * 
 * @author trjie
 * @createTime 2019年5月21日  上午10:52:14
 */

public class InventoryTaskInfoVO {
	
	private Integer materialTypeId;
	
	private String no;
	
	private String specification;
	
	private Integer taskId;

	private String taskName;
	
	private Date startTime;
	
	private Date endTime;
	
	private Integer beforeNum;
	
	private Integer acturalNum;
	
	private Integer differentNum;
	
	private Integer supplierId;
	
	private String supplierName;
	
	private String inventoryOperatior;

	public Integer getMaterialTypeId() {
		return materialTypeId;
	}

	public void setMaterialTypeId(Integer materialTypeId) {
		this.materialTypeId = materialTypeId;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getSpecification() {
		return specification;
	}

	public void setSpecification(String specification) {
		this.specification = specification;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Integer getBeforeNum() {
		return beforeNum;
	}

	public void setBeforeNum(Integer beforeNum) {
		this.beforeNum = beforeNum;
	}

	public Integer getActuralNum() {
		return acturalNum;
	}

	public void setActuralNum(Integer acturalNum) {
		this.acturalNum = acturalNum;
	}

	public Integer getDifferentNum() {
		return differentNum;
	}

	public void setDifferentNum(Integer differentNum) {
		this.differentNum = differentNum;
	}

	public Integer getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getInventoryOperatior() {
		return inventoryOperatior;
	}

	public void setInventoryOperatior(String inventoryOperatior) {
		this.inventoryOperatior = inventoryOperatior;
	}
}
