/**  
*  
*/  
package com.jimi.uw_server.exception;

/**  
 * <p>Title: FormDataValidateFailedException</p>  
 * <p>Description: 表单验证失败</p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2019年12月10日
 *
 */
public class FormDataValidateFailedException extends RuntimeException{

	/**
	 * <p>Description: <p>
	 */
	private static final long serialVersionUID = 1L;

	private Object data;
	/**
	 * <p>Title<p>
	 * <p>Description<p>
	 */
	public FormDataValidateFailedException(String message, Object data) {
		super(message);
		this.data = data;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
	
	
}
