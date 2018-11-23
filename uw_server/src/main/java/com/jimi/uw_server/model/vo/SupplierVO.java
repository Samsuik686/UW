package com.jimi.uw_server.model.vo;

import com.jimi.uw_server.model.Supplier;

/**
 * 供应商实体类
 * @author HardyYao
 * @createTime 2018年11月22日  下午4:39:50
 */

@SuppressWarnings("serial")
public class SupplierVO extends Supplier {

	private String enabledString;


	public SupplierVO(Integer id, String name, Boolean enabled) {
		this.setId(id);
		this.setName(name);
		this.setEnabled(enabled);
		this.setEnabledString(enabled);
		this.set("enabledString", getEnabledString());
	}


	public String getEnabledString() {
		return enabledString;
	}


	public void setEnabledString(Boolean enabled) {
		if (enabled) {
			this.enabledString = "是";
		} else {
			this.enabledString = "否";
		}
	}

}
