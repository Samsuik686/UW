package com.jimi.uw_server.model.bo;

import com.jimi.uw_server.util.ExcelHelper.Excel;


/**
 * 
 * @author HardyYao
 * @createTime 2019年5月8日  下午4:04:31
 */

public class TaskItemBO {

	@Excel(col = 0, head = "序号")
	private Integer serialNumber;
	@Excel(col = 1, head = "料号")
	private String no;
	@Excel(col = 2, head = "数量")
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

}
