package com.jimi.uw_server.model.vo;

import java.util.Date;

/**
 * 
 * @author HardyYao
 * @createTime 2019年5月21日  下午2:31:47
 */

public class InventoryTaskDetailVO {

	private Integer id;
	
	private Integer taskId;
	
	private String materialId;
	
	private Integer materialTypeId;
	
	private Integer whId;
	
	private String whName;
	
	private Integer beforeNum;
	
	private Integer atrualNum;
	
	private Integer differentNum;
	
	private Integer materialreturnNum;
	
	private String InventoryOperatior;
	
	private Date inventoryTime;
	
	private String coverOperatior;
	
	private Date coverTime;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public Integer getMaterialTypeId() {
		return materialTypeId;
	}

	public void setMaterialTypeId(Integer materialTypeId) {
		this.materialTypeId = materialTypeId;
	}

	public Integer getWhId() {
		return whId;
	}

	public void setWhId(Integer whId) {
		this.whId = whId;
	}

	public String getWhName() {
		return whName;
	}

	public void setWhName(String whName) {
		this.whName = whName;
	}

	public Integer getBeforeNum() {
		return beforeNum;
	}

	public void setBeforeNum(Integer beforeNum) {
		this.beforeNum = beforeNum;
	}

	public Integer getAtrualNum() {
		return atrualNum;
	}

	public void setAtrualNum(Integer atrualNum) {
		this.atrualNum = atrualNum;
	}

	public Integer getDifferentNum() {
		return differentNum;
	}

	public void setDifferentNum(Integer differentNum) {
		this.differentNum = differentNum;
	}

	public String getInventoryOperatior() {
		return InventoryOperatior;
	}

	public void setInventoryOperatior(String inventoryOperatior) {
		InventoryOperatior = inventoryOperatior;
	}

	public Date getInventoryTime() {
		return inventoryTime;
	}

	public void setInventoryTime(Date inventoryTime) {
		this.inventoryTime = inventoryTime;
	}

	public String getCoverOperatior() {
		return coverOperatior;
	}

	public void setCoverOperatior(String coverOperatior) {
		this.coverOperatior = coverOperatior;
	}

	public Date getCoverTime() {
		return coverTime;
	}

	public void setCoverTime(Date coverTime) {
		this.coverTime = coverTime;
	}

	public String getMaterialId() {
		return materialId;
	}

	public void setMaterialId(String materialId) {
		this.materialId = materialId;
	}

	public Integer getMaterialreturnNum() {
		return materialreturnNum;
	}

	public void setMaterialreturnNum(Integer materialreturnNum) {
		this.materialreturnNum = materialreturnNum;
	}
	
	
}
