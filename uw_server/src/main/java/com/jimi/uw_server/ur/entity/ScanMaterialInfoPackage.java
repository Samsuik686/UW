package com.jimi.uw_server.ur.entity;

import com.jimi.uw_server.ur.entity.base.UrBasePackage;


/**
 * 出库结果包
 * <br>
 * <b>2019年5月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class ScanMaterialInfoPackage extends UrBasePackage {

	private Integer taskId;
	
	private Integer boxId;
	
	private String materialId;

	private Integer quantity;

	/**
	 * <p>Title<p>
	 * <p>Description<p>
	 */
	public ScanMaterialInfoPackage() {
		// TODO Auto-generated constructor stub
	}
	
	
	public boolean isContainsNullFields() {
		if (taskId == null || materialId == null || quantity == null) {
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

	
}
