/**  
*  
*/  
package com.jimi.uw_server.ur.entity;

import com.jimi.uw_server.ur.entity.base.UrBasePackage;

/**  
 * <p>Title: AskPostionPackage</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2019年12月18日
 *
 */
public class AskPostionPackage extends UrBasePackage {

	private Integer taskId;
	
	private Integer boxId;
	
	
	/**
	 * <p>Title<p>
	 * <p>Description<p>
	 */
	public AskPostionPackage() {
		// TODO Auto-generated constructor stub
	}

	public Integer getTaskId() {
		return taskId;
	}

	
	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}
	

	public Integer getBoxId() {
		return boxId;
	}

	
	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}
	
	
}
