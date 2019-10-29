package com.jimi.uw_server.model.bo;

import com.jimi.uw_server.util.ExcelHelper.Excel;


/**
 * 
 * @author HardyYao
 * @createTime 2019年4月29日  上午9:25:51
 */

public class EWhInventoryRecordBO {

	@Excel(col = 0, head = "序号")
	private Integer serialNumber;

	@Excel(col = 1, head = "仓库")
	private String whName;
	
	@Excel(col = 2, head = "料号")
	private String no;

	@Excel(col = 3, head = "数量")
	private Integer quantity;


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


	public Integer getQuantity() {
		return quantity;
	}


	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}


	public String getWhName() {
		return whName;
	}


	public void setWhName(String whName) {
		this.whName = whName;
	}

}
