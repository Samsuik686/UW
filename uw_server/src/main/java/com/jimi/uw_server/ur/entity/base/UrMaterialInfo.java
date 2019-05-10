package com.jimi.uw_server.ur.entity.base;

import java.util.Date;

/**
 * 
 * @author HardyYao
 * @createTime 2019年4月25日  下午2:18:03
 */

public class UrMaterialInfo {

	private String materialNo;
	
	private Integer row;
	
	private Integer col;

	private Integer quantity;
	
	private Integer materialTypeId;
	
	private Date productionTime;
	
	
	public boolean isContainsNullFields() {
		if(materialNo == null || row == null || col == null || quantity == null || materialTypeId == null || productionTime == null) {
			return true;
		}else {
			return false;
		}
	}
	
	
	public String getMaterialNo() {
		return materialNo;
	}

	public void setMaterialNo(String materialNo) {
		this.materialNo = materialNo;
	}

	public Integer getRow() {
		return row;
	}

	public void setRow(Integer row) {
		this.row = row;
	}

	public Integer getCol() {
		return col;
	}

	public void setCol(Integer col) {
		this.col = col;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getMaterialTypeId() {
		return materialTypeId;
	}

	public void setMaterialTypeId(Integer materialTypeId) {
		this.materialTypeId = materialTypeId;
	}

	public Date getProductionTime() {
		return productionTime;
	}

	public void setProductionTime(Date productionTime) {
		this.productionTime = productionTime;
	}
	
}
