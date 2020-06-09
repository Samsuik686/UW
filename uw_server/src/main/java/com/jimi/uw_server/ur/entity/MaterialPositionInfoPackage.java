package com.jimi.uw_server.ur.entity;

import com.jimi.uw_server.ur.entity.base.UrBasePackage;


/**
 * 出入库包 <br>
 * <b>2019年5月9日</b>
 * 
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class MaterialPositionInfoPackage extends UrBasePackage {

	private Integer taskId;

	private Integer boxId;

	private Integer type;

	private String materialId;

	private Integer xPosition;

	private Integer yPosition;

	private Integer quantity;


	public MaterialPositionInfoPackage() {
		this.cmdCode = "material_position_info";
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


	public Integer getBoxId() {
		return boxId;
	}


	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}


	public Integer getType() {
		return type;
	}


	public void setType(Integer type) {
		this.type = type;
	}


	public Integer getQuantity() {
		return quantity;
	}


	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}


}
