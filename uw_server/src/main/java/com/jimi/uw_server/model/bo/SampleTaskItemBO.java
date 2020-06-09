package com.jimi.uw_server.model.bo;

import com.jimi.uw_server.util.ExcelHelper.Excel;


/**
 * 
 * @author HardyYao
 * @createTime 2019年6月20日 下午5:29:26
 */

public class SampleTaskItemBO {

	@Excel(col = 0, head = "序号")
	private Integer serialNumber;
	@Excel(col = 1, head = "料号")
	private String no;


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
}
