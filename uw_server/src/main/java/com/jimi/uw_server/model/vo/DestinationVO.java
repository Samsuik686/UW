package com.jimi.uw_server.model.vo;

import com.jimi.uw_server.model.Destination;


/**
 * 目的地表示层
 * @author HardyYao
 * @createTime 2019年3月11日  上午11:53:36
 */

@SuppressWarnings("serial")
public class DestinationVO extends Destination {

	private String enabledString;


	public DestinationVO(Integer id, String name, Boolean enabled) {
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
