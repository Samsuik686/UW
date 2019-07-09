package com.jimi.uw_server.model.vo;

import java.util.Date;

public class SampleTaskOutMaterialInfoVO {
	
	private String materialId;
	
	private String operator;
	
	private Integer quantity;
	
	private Boolean isSingular;
	
	private Date time;
	
	private String isSingularString;
	
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

	public Boolean getIsSingular() {
		return isSingular;
	}

	public void setIsSingular(Boolean isSingular) {
		this.isSingular = isSingular;
		if (isSingular == null){
			this.isSingularString = "正常";
		}else if (isSingular) {
			this.isSingularString = "异常出库";
		}else {
			this.isSingularString = "抽检出库";
		}
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getIsSingularString() {
		return isSingularString;
	}

	public void setIsSingularString(String isSingularString) {
		this.isSingularString = isSingularString;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
}
