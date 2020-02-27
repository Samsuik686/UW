/**  
*  
*/  
package com.jimi.uw_server.ur.entity;

import java.io.Serializable;

/**  
 * <p>Title: UrMaterialInfo</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2019年12月19日
 *
 */
public class UrMaterialInfo implements Serializable{

	/**
	 * <p>Description: <p>
	 */
	private static final long serialVersionUID = 1L;

	private String materialId;
	
	private Integer xPosition;
	
	private Integer yPosition;
	
	private Integer exceptionCode;
	
	private Integer taskId;
	
	private Integer boxId;
	
	private Integer quantity;
	
	private Integer windowId;
	
	private Integer goodsLocationId;
	
	private Boolean isScaned;
	
	/**
	 * <p>Title<p>
	 * <p>Description<p>
	 */
	public UrMaterialInfo(String materialId, Integer xPosition, Integer yPosition, Integer taskId, Integer boxId, Integer windowId, Integer goodsLocationId, Boolean isScaned, Integer exceptionCode, Integer quantity) {
		this.materialId = materialId;
		this.xPosition = xPosition;
		this.yPosition = yPosition;
		this.taskId = taskId;
		this.boxId = boxId;
		this.windowId = windowId;
		this.goodsLocationId = goodsLocationId;
		this.isScaned = isScaned;
		this.exceptionCode = exceptionCode;
		this.quantity = quantity;
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

	public Boolean getIsScaned() {
		return isScaned;
	}

	public void setIsScaned(Boolean isScaned) {
		this.isScaned = isScaned;
	}

	public Integer getExceptionCode() {
		return exceptionCode;
	}

	public void setExceptionCode(Integer exceptionCode) {
		this.exceptionCode = exceptionCode;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public Integer getBoxId() {
		return boxId;
	}

	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}

	public Integer getWindowId() {
		return windowId;
	}

	public void setWindowId(Integer windowId) {
		this.windowId = windowId;
	}

	public Integer getGoodsLocationId() {
		return goodsLocationId;
	}

	public void setGoodsLocationId(Integer goodsLocationId) {
		this.goodsLocationId = goodsLocationId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}
	
	
	
}
