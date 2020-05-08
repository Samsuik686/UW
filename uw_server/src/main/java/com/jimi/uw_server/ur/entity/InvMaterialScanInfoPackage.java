package com.jimi.uw_server.ur.entity;

import com.jimi.uw_server.ur.entity.base.UrBasePackage;


/**
 * 出库结果包
 * <br>
 * <b>2019年5月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class InvMaterialScanInfoPackage extends UrBasePackage {

	private Integer taskId;
	
	private Integer boxId;
	
	private String materialId;

	private Integer quantity;
	
	private Integer xPosition;
	
	private Integer yPosition;
	
	private Integer result;
	
	private Integer endFlag;

	/**
	 * <p>Title<p>
	 * <p>Description<p>
	 */
	public InvMaterialScanInfoPackage() {
		
	}
	
	
	public boolean isContainsNullFields() {
		if (taskId == null || materialId == null || quantity == null || yPosition == null || xPosition == null || result == null || endFlag == null) {
			return true;
		} else {
			return false;
		}
	}


	public Integer getTaskId() {
		return taskId;
	}


	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}


	public String getMaterialId() {
		return materialId;
	}


	public void setMaterialId(String materialId) {
		this.materialId = materialId;
	}


	public Integer getQuantity() {
		return quantity;
	}


	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}


	public Integer getBoxId() {
		return boxId;
	}


	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}


	public Integer getxPosition() {
		return xPosition;
	}


	public void setxPosition(Integer xPosition) {
		this.xPosition = xPosition;
	}


	public Integer getyPosition() {
		return yPosition;
	}


	public void setyPosition(Integer yPosition) {
		this.yPosition = yPosition;
	}


	public Integer getResult() {
		return result;
	}


	public void setResult(Integer result) {
		this.result = result;
	}


	public Integer getEndFlag() {
		return endFlag;
	}


	public void setEndFlag(Integer endFlag) {
		this.endFlag = endFlag;
	}

	
}
