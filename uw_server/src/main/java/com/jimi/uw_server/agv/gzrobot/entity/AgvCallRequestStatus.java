/**  
*  
*/  
package com.jimi.uw_server.agv.gzrobot.entity;

import java.io.Serializable;

/**  
 * <p>Title: AgvCallRequestStatus</p>  
 * <p>Description:  任务状态反馈</p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年6月17日
 *
 */
public class AgvCallRequestStatus implements Serializable{

	/**
	 * <p>Description: <p>
	 */
	private static final long serialVersionUID = 1468263035311639865L;

	private String orderId;
	
	private String taskId;
	
	private Integer phaseStatus;
	
	private Integer agvId;
	
	private String taskData;

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

	public Integer getPhaseStatus() {
		return phaseStatus;
	}

	public void setPhaseStatus(Integer phaseStatus) {
		this.phaseStatus = phaseStatus;
	}

	public Integer getAgvId() {
		return agvId;
	}

	public void setAgvId(Integer agvId) {
		this.agvId = agvId;
	}

	public String getTaskData() {
		return taskData;
	}

	public void setTaskData(String taskData) {
		this.taskData = taskData;
	}

	@Override
	public String toString() {
		return "AgvCallRequestStatus [orderId=" + orderId + ", taskId=" + taskId + ", phaseStatus=" + phaseStatus + ", agvId=" + agvId + ", taskData=" + taskData + "]";
	}
	
	
	
}
