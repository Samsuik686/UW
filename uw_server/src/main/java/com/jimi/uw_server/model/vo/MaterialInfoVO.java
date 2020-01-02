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

	private Integer urExceptionCode;
	
	private String urExceptionString;
	
	
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


	public Integer getUrExceptionCode() {
		return urExceptionCode;
	}


	public void setUrExceptionCode(Integer urExceptionCode) {
		this.urExceptionCode = urExceptionCode;
		switch (urExceptionCode) {
		case 0:
			this.setUrExceptionString("正常");
			break;
		case 1:
			this.setUrExceptionString("机械臂夹空");
			break;
		case 2:
			this.setUrExceptionString("机械臂夹取物料码与位置不对应");
			break;
		case 3:
			this.setUrExceptionString("扫码出错，数量异常（找不到，或者数量小于0）");
			break;
		case 4:
			this.setUrExceptionString("机械臂夹取物料码与系统记录的数量不匹配");
			break;
		case 5:
			this.setUrExceptionString("机械臂异常");
			break;
		default:
			break;
		}
	}


	public String getUrExceptionString() {
		return urExceptionString;
	}


	public void setUrExceptionString(String urExceptionString) {
		this.urExceptionString = urExceptionString;
	}

	
}
