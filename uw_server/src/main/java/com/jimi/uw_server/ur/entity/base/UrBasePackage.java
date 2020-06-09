package com.jimi.uw_server.ur.entity.base;

/**
 * Ur指令父类 <br>
 * <b>2018年6月15日</b>
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class UrBasePackage {

	protected String cmdCode;

	protected Integer cmdId;


	/**
	 * <p>
	 * Title
	 * <p>
	 * <p>
	 * Description
	 * <p>
	 */
	public UrBasePackage() {
		// TODO Auto-generated constructor stub
	}


	public String getCmdCode() {
		return cmdCode;
	}


	public void setCmdCode(String cmdCode) {
		this.cmdCode = cmdCode;
	}


	public Integer getCmdId() {
		return cmdId;
	}


	public void setCmdId(Integer cmdId) {
		this.cmdId = cmdId;
	}

}
