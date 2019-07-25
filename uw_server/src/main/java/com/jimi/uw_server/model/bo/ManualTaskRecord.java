package com.jimi.uw_server.model.bo;

import java.util.List;


public class ManualTaskRecord {

	private String supplierName;
	
	private String no;
	
	private Integer planQuantity;
	
	private List<MaterialReel> materialReels;


	public String getSupplierName() {
		return supplierName;
	}


	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}


	public String getNo() {
		return no;
	}


	public void setNo(String no) {
		this.no = no;
	}


	public Integer getPlanQuantity() {
		return planQuantity;
	}


	public void setPlanQuantity(Integer planQuantity) {
		this.planQuantity = planQuantity;
	}


	public List<MaterialReel> getMaterialReels() {
		return materialReels;
	}


	public void setMaterialReels(List<MaterialReel> materialReels) {
		this.materialReels = materialReels;
	}

}
