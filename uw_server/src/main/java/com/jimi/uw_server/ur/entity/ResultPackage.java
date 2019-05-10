package com.jimi.uw_server.ur.entity;

import com.jimi.uw_server.ur.entity.base.UrBasePackage;

/**
 * 出库结果包
 * <br>
 * <b>2019年5月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class ResultPackage extends UrBasePackage{

	private Integer aimid;
	
	private Integer result;
	
	private String message;

	
	public boolean isContainsNullFields() {
		if(aimid == null || result == null || message == null) {
			return true;
		}else {
			return false;
		}
	}
	

	public Integer getResult() {
		return result;
	}

	public void setResult(Integer result) {
		this.result = result;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Integer getAimid() {
		return aimid;
	}

	public void setAimid(Integer aimid) {
		this.aimid = aimid;
	}
	
	
}
