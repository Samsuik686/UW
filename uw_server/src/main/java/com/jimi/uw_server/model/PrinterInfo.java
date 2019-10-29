package com.jimi.uw_server.model;

public class PrinterInfo {

	private String id;

	private String materialId;

	private String user;

	private String specification;

	private String designator;

	private String productDate;

	private String remainingQuantity;

	private String materialNo;

	private String supplier;

	private String cycle;

	private String manufacturer;

	private Integer type;

	private String printTime;
	
	public String getMaterialId() {
		return materialId;
	}


	public void setMaterialId(String materialId) {
		this.materialId = materialId;
	}


	public String getUser() {
		return user;
	}


	public void setUser(String user) {
		this.user = user;
	}


	public String getProductDate() {
		return productDate;
	}


	public void setProductDate(String productDate) {
		this.productDate = productDate;
	}


	public String getMaterialNo() {
		return materialNo;
	}


	public void setMaterialNo(String materialNo) {
		this.materialNo = materialNo;
	}


	public String getSupplier() {
		return supplier;
	}


	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}


	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getRemainingQuantity() {
		return remainingQuantity;
	}


	public void setRemainingQuantity(String remainingQuantity) {
		this.remainingQuantity = remainingQuantity;
	}


	public String getCycle() {
		return cycle;
	}


	public void setCycle(String cycle) {
		this.cycle = cycle;
	}


	public String getManufacturer() {
		return manufacturer;
	}


	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}


	public String getSpecification() {
		return specification;
	}


	public void setSpecification(String specification) {
		this.specification = specification;
	}


	public String getDesignator() {
		return designator;
	}


	public void setDesignator(String designator) {
		this.designator = designator;
	}


	public Integer getType() {
		return type;
	}


	public void setType(Integer type) {
		this.type = type;
	}


	public String getPrintTime() {
		return printTime;
	}


	public void setPrintTime(String printTime) {
		this.printTime = printTime;
	}


	public PrinterInfo(String id, String materialId, String user, String productDate, String remainingQuantity, String materialNo, String supplier, String cycle, String manufacturer, String specification, String designator, Integer type, String printTime) {
		this.id = id;
		this.supplier = supplier;
		this.user = user;
		this.materialId = materialId;
		this.materialNo = materialNo;
		this.productDate = productDate;
		this.remainingQuantity = remainingQuantity;
		this.cycle = cycle;
		this.manufacturer = manufacturer;
		this.specification = specification;
		this.designator = designator;
		this.type = type;
		this.printTime = printTime;
	}
}
