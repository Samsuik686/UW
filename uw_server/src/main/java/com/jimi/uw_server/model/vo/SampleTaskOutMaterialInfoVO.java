package com.jimi.uw_server.model.vo;

import java.util.Date;


public class SampleTaskOutMaterialInfoVO {

	private String materialId;

	private String operator;

	private Integer quantity;

	private Integer outType;

	private Date time;

	private String outTypeString;

	private Integer id;


	public String getMaterialId() {
		return materialId;
	}


	public void setMaterialId(String materialId) {
		this.materialId = materialId;
	}


	public String getOperator() {
		return operator;
	}


	public void setOperator(String operator) {
		this.operator = operator;
	}


	public Integer getQuantity() {
		return quantity;
	}


	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}


	public Integer getOutType() {
		return outType;
	}


	public void setOutType(Integer outType) {
		this.outType = outType;
		if (outType == null) {
			this.outTypeString = "正常";
		} else if (outType.equals(1)) {
			this.outTypeString = "异常出库";
		} else if (outType.equals(0)) {
			this.outTypeString = "抽检出库";
		} else {
			this.outTypeString = "料盘丢失";
		}
	}


	public Date getTime() {
		return time;
	}


	public void setTime(Date time) {
		this.time = time;
	}


	public String getOutTypeString() {
		return outTypeString;
	}


	public void setOutTypeString(String outTypeString) {
		this.outTypeString = outTypeString;
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}
}
