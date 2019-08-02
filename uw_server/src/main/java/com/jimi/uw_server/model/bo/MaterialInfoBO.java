package com.jimi.uw_server.model.bo;

import java.util.Date;

import com.jimi.uw_server.util.ExcelHelper.Excel;


/**
 * 
 * @author HardyYao
 * @createTime 2019年4月29日  上午9:25:51
 */

public class MaterialInfoBO {

	@Excel(col = 0, head = "序号")
	private Integer serialNumber;

	@Excel(col = 1, head = "料盘码")
	private String materialId;

	@Excel(col = 2, head = "料号")
	private String no;

	@Excel(col = 3, head = "供应商")
	private String supplier;

	@Excel(col = 4, head = "数量")
	private Integer quantity;

	@Excel(col = 5, head = "生产日期")
	private Date produtionDate;


	public Integer getSerialNumber() {
		return serialNumber;
	}


	public void setSerialNumber(Integer serialNumber) {
		this.serialNumber = serialNumber;
	}


	public String getNo() {
		return no;
	}


	public void setNo(String no) {
		this.no = no;
	}


	public String getMaterialId() {
		return materialId;
	}


	public void setMaterialId(String materialId) {
		this.materialId = materialId;
	}


	public String getSupplier() {
		return supplier;
	}


	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}


	public Integer getQuantity() {
		return quantity;
	}


	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}


	public Date getProdutionDate() {
		return produtionDate;
	}


	public void setProdutionDate(Date produtionDate) {
		this.produtionDate = produtionDate;
	}

}
