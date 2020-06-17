/**  
*  
*/  
package com.jimi.uw_server.agv.gzrobot.entity.base;

import java.io.Serializable;

import javax.json.bind.annotation.JsonbProperty;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**  
 * <p>Title: BaseRequest</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年6月17日
 *
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BaseRequest<T> implements Serializable{

	
	/**
	 * <p>Description: <p>
	 */
	private static final long serialVersionUID = -7895268135470044220L;

	private T requestStr;
	
	private Integer requestType;

	
	public T getRequestStr() {
		return requestStr;
	}

	public void setRequestStr(T requestStr) {
		this.requestStr = requestStr;
	}

	public Integer getRequestType() {
		return requestType;
	}

	public void setRequestType(Integer requestType) {
		this.requestType = requestType;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	
}
