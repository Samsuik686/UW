/**  
*  
*/  
package com.jimi.uw_server.agv.gzrobot.entity;

import java.io.Serializable;

/**  
 * <p>Title: RequestResult</p>  
 * <p>Description: 请求结果 </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年6月17日
 *
 */
public class RequestResult implements Serializable{

	/**
	 * <p>Description: <p>
	 */
	private static final long serialVersionUID = -4907326065665401987L;

	private Integer resultCode;
	
	private String resultDesc;
	
	public Integer getResultCode() {
		return resultCode;
	}

	public void setResultCode(Integer resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultDesc() {
		return resultDesc;
	}

	public void setResultDesc(String resultDesc) {
		this.resultDesc = resultDesc;
	}

	@Override
	public String toString() {
		return "RequestResult [resultCode=" + resultCode + ", resultDesc=" + resultDesc + "]";
	}
	
}
