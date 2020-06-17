/**  
*  
*/  
package com.jimi.uw_server.agv.gzrobot.entity;

import java.io.Serializable;

/**  
 * <p>Title: AgvCancelRequest</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年6月17日
 *
 */
public class AgvCancelRequest implements Serializable{

	/**
	 * <p>Description: <p>
	 */
	private static final long serialVersionUID = 3575194378971682891L;

	private String orderId;
	
	private String taskId;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	@Override
	public String toString() {
		return "AgvCancelRequest [orderId=" + orderId + ", taskId=" + taskId + "]";
	}
	
	
}
