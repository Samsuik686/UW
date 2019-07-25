package com.jimi.uw_server.model.bo;

public class MaterialReel {

	private String materialId;

	private String supplierName;

	private Integer quantity;

	private String productionTime;

	private Integer boxId;


	public String getMaterialId() {
		return materialId;
	}


	public void setMaterialId(String materialId) {
		this.materialId = materialId;
	}


	public String getSupplierName() {
		return supplierName;
	}


	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}


	public Integer getQuantity() {
		return quantity;
	}


	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}


	public String getProductionTime() {
		return productionTime;
	}


	public void setProductionTime(String productionTime) {
		this.productionTime = productionTime;
	}


	public Integer getBoxId() {
		return boxId;
	}


	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}
}
