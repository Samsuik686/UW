package com.jimi.uw_server.model.vo;



/**
 * 目的地表示层
 * @author HardyYao
 * @createTime 2019年3月11日  上午11:53:36
 */

public class DestinationVO{

	private Integer id;
	
	private String name;
	
	private Boolean enabled;
	
	private String companyName;
	
	private String enabledString;


	public DestinationVO(Integer id, String name, String companyName, Boolean enabled) {
		this.setId(id);
		this.setName(name);
		this.setCompanyName(companyName);
		this.setEnabled(enabled);
		this.setEnabledString(enabled);
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Boolean getEnabled() {
		return enabled;
	}


	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}


	public String getCompanyName() {
		return companyName;
	}


	public void setCompanyName(String companyName) {
		this.companyName = companyName;
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
