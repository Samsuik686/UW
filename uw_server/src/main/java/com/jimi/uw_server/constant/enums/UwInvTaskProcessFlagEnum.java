/**  
*  
*/  
package com.jimi.uw_server.constant.enums;

/**  
 * <p>Title: UwInvTaskProcessFlag</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年4月28日
 *
 */
public enum UwInvTaskProcessFlagEnum {
	
	UNEXIST(0),
	
	UW_EXCEPTION_WAIT_PROCESS(1),
	
	UW_EXCEPTION_START_PROCESS(2);
	
	int code;
	
	UwInvTaskProcessFlagEnum(int code){
		this.code = code;
	}

	public int getCode() {
		return this.code;
	}
	
	
	public boolean isCodeEquals(int code) {
		if (this.code == code) {
			return true;
		}
		return false;
	}
}
