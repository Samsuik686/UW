package com.jimi.uw_server.model.vo;

import com.jimi.uw_server.model.User;


/**
 * 用户表示层对象
 * 
 * @author HardyYao
 * @createTime 2018年7月5日 上午11:19:22
 */
@SuppressWarnings("serial")
public class UserVO extends User {

	private String enabledString;


	public UserVO(String uid, String password, String name, Integer type, boolean enabled, String typeString) {
		this.setUid(uid);
		this.setPassword(password);
		this.setName(name);
		this.setType(type);
		this.set("typeString", typeString);
		this.setEnabled(enabled);
		this.setEnabledString(enabled);
		this.set("enabledString", getEnabledString());
	}


	public void setEnabledString(boolean enabled) {
		if (enabled) {
			this.enabledString = "是";
		} else {
			this.enabledString = "否";
		}
	}


	public String getEnabledString() {
		return enabledString;
	}

}
