package com.jimi.uw_server.model.vo;

import java.util.Date;

/**
 * 停泊任务条目详情表示层
 * @author HardyYao
 * @createTime 2019年1月23日  下午4:22:14
 */

public class PackingListItemDetailsVO {

	private String materialId;

	private Integer quantity;

	private Integer remainderQuantity;

	private String isInBoxString;

	private Date productionTime;


	public PackingListItemDetailsVO(String materialId, Integer ioQuantity, Integer remainderQuantity, Date productionTime, Boolean isInBox) {
		this.setMaterialId(materialId);
		this.setQuantity(ioQuantity);
		this.setRemainderQuantity(remainderQuantity);
		this.setProductionTime(productionTime);
		this.setIsInBoxString(isInBox);
		this.getIsInBoxString();
	}

	public String getIsInBoxString() {
		return isInBoxString;
	}

	public void setIsInBoxString(Boolean isInBox) {
		if (isInBox) {
			this.isInBoxString = "是";
		} else {
			this.isInBoxString = "否";
		}
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

	public Date getProductionTime() {
		return productionTime;
	}

	public void setProductionTime(Date productionTime) {
		this.productionTime = productionTime;
	}

	public Integer getRemainderQuantity() {
		return remainderQuantity;
	}

	public void setRemainderQuantity(Integer remainderQuantity) {
		this.remainderQuantity = remainderQuantity;
	}

}
