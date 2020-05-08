/**  
*  
*/  
package com.jimi.uw_server.constant.enums;

/**  
 * <p>Title: MaterialBoxPositionStatusEnum</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年4月29日
 *
 */
public enum MaterialBoxPositionStatusEnum {

	EMPTY(0,"空位置"),
	NORMAL(1,"正常"),
	SCAN_ERROR(2,"扫码异常"),
	CARDBOARD_ERROR(3,"刀卡异常"),
	POSITION_ERROR(4,"位置异常");
	
	private Integer code;
	
	private String description;
	
	
	/**
	 * <p>Title<p>
	 * <p>Description<p>
	 */
	MaterialBoxPositionStatusEnum(Integer code, String description) {

		this.code = code;
		this.description = description;
	}
	
	
	public Integer getCode() {
		return this.code;
	}
	
	
	public String getDescription() {
		return this.description;
	}
}
