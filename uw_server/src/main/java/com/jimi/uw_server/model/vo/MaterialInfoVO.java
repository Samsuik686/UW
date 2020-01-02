package com.jimi.uw_server.model.vo;

import java.util.Date;


/**
 * 
 * @author trjie
 * @createTime 2019年5月20日  下午2:48:55
 */

public class MaterialInfoVO {

	private String materialId;

	private Integer row;

	private Integer col;

	private String no;

	private Integer materailTypeId;

	private String specification;

	private String supplier;

	private Integer supplierId;

	private Integer storeNum;

	private Date productionTime;

	private Integer actualNum;

	private Boolean isScaned;

	private Integer isOuted;


	public String getMaterialId() {
		return materialId;
	}


	public void setMaterialId(String materialId) {
		this.materialId = materialId;
	}


	public String getNo() {
		return no;
	}


	public void setNo(String no) {
		this.no = no;
	}


	public Integer getMaterailTypeId() {
		return materailTypeId;
	}


	public void setMaterailTypeId(Integer materailTypeId) {
		this.materailTypeId = materailTypeId;
	}


	public String getSpecification() {
		return specification;
	}


	public void setSpecification(String specification) {
		this.specification = specification;
	}


	public String getSupplier() {
		return supplier;
	}


	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}


	public Integer getSupplierId() {
		return supplierId;
	}


	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}


	public Integer getStoreNum() {
		return storeNum;
	}


	public void setStoreNum(Integer storeNum) {
		this.storeNum = storeNum;
	}


	public Date getProductionTime() {
		return productionTime;
	}


	public void setProductionTime(Date productionTime) {
		this.productionTime = productionTime;
	}


	public Integer getActualNum() {
		return actualNum;
	}


	public void setActualNum(Integer actualNum) {
		this.actualNum = actualNum;
	}


	public Boolean getIsScaned() {
		return isScaned;
	}


	public void setIsScaned(Boolean isScaned) {
		this.isScaned = isScaned;
	}


	public Integer getIsOuted() {
		return isOuted;
	}


	public void setIsOuted(Integer isOuted) {
		this.isOuted = isOuted;
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

}
