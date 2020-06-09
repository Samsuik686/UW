package com.jimi.uw_server.model.vo;

import java.util.Date;


/**
 * 
 * @author trjie
 * @createTime 2019年5月20日 下午2:48:55
 */

public class MaterialDetialsVO {

	private String materialId;

	private Integer row;

	private Integer col;

	private String no;

	private Integer materialTypeId;

	private String specification;

	private String supplierName;

	private Integer supplierId;

	private Integer storeNum;

	private Date productionTime;

	private Integer actualNum;

	private Boolean isScaned;

	private Integer isOuted;

	private Integer exceptionCode;

	private String exceptionCodeString;


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


	public Integer getMaterialTypeId() {
		return materialTypeId;
	}


	public void setMaterialTypeId(Integer materialTypeId) {
		this.materialTypeId = materialTypeId;
	}


	public String getSpecification() {
		return specification;
	}


	public void setSpecification(String specification) {
		this.specification = specification;
	}


	public String getSupplierName() {
		return supplierName;
	}


	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
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


	public Integer getExceptionCode() {
		return exceptionCode;
	}


	public void setExceptionCode(Integer exceptionCode) {
		this.exceptionCode = exceptionCode;
		if (exceptionCode != null) {
			switch (exceptionCode) {
			case 1:
				this.exceptionCodeString = "机械臂夹空";
				break;
			case 2:
				this.exceptionCodeString = "物料扫码不对应";
			case 3:
				this.exceptionCodeString = "数量异常（找不到，或者数量小于0）";
			case 4:
				this.exceptionCodeString = "数量不匹配";
			case 5:
				this.exceptionCodeString = "机械臂异常";
			default:
				break;
			}
		}

	}


	public String getExceptionCodeString() {
		return exceptionCodeString;
	}


	public void setExceptionCodeString(String exceptionCodeString) {
		this.exceptionCodeString = exceptionCodeString;
	}


	/**
	 * <p>
	 * Title
	 * <p>
	 * <p>
	 * Description
	 * <p>
	 */
	public MaterialDetialsVO() {
		// TODO Auto-generated constructor stub
	}

}
