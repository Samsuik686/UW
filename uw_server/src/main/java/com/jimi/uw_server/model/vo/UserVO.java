package com.jimi.uw_server.model.vo;

import com.jimi.uw_server.model.User;


/**
 * 用户表示层对象
 * @author HardyYao
 * @createTime 2018年7月5日 上午11:19:22 
 */
@SuppressWarnings("serial")
public class UserVO extends User {

	private String enabledString;

	private String typeString;


	public UserVO(String uid, String password, String name, Integer type, boolean enabled) {
		this.setUid(uid);
		this.setPassword(password);
		this.setName(name);
		this.setType(type);
		this.setTypeString(type);
		this.set("typeString", getTypeString());
		this.setEnabled(enabled);
		this.setEnabledString(enabled);
		this.set("enabledString", getEnabledString());
	}


	public void setTypeString(Integer type) {
		if (this.getType() == 0) {
			this.typeString = "游客";
		} else if (this.getType() == 1) {
			this.typeString = "超级管理员";
		} else if (this.getType() == 2) {
			this.typeString = "普通管理员";
		}
	}


	public String getTypeString() {
		return typeString;
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
