/**  
*  
*/  
package com.jimi.uw_server.agv.gzrobot.entity;

import java.io.Serializable;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**  
 * <p>Title: AgvCallRequest</p>  
 * <p>Description: 下发任务</p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年6月17日
 *
 */
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AgvCallRequest implements Serializable{
	
	/**
	 * <p>Description: <p>
	 */
	private static final long serialVersionUID = -587052875723382021L;

	private String orderId;
	
	private String taskId;
	
	private String preTaskId;
	
	private Integer hasNextTask;
	
	private Integer taskType;
	
	private String destName;
	
	private Integer priority;
	
	private Integer agv_id;
	
	private String customParam1;
	
	private String customParam2;

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

	public String getPreTaskId() {
		return preTaskId;
	}

	public void setPreTaskId(String preTaskId) {
		this.preTaskId = preTaskId;
	}

	public Integer getHasNextTask() {
		return hasNextTask;
	}

	public void setHasNextTask(Integer hasNextTask) {
		this.hasNextTask = hasNextTask;
	}

	public Integer getTaskType() {
		return taskType;
	}

	public void setTaskType(Integer taskType) {
		this.taskType = taskType;
	}

	public String getDestName() {
		return destName;
	}

	public void setDestName(String destName) {
		this.destName = destName;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public Integer getAgv_id() {
		return agv_id;
	}

	public void setAgv_id(Integer agv_id) {
		this.agv_id = agv_id;
	}

	public String getCustomParam1() {
		return customParam1;
	}

	public void setCustomParam1(String customParam1) {
		this.customParam1 = customParam1;
	}

	public String getCustomParam2() {
		return customParam2;
	}

	public void setCustonParam2(String customParam2) {
		this.customParam2 = customParam2;
	}

	@Override
	public String toString() {
		return "AgvCallRequest [orderId=" + orderId + ", taskId=" + taskId + ", preTaskId=" + preTaskId + ", hasNextTask=" + hasNextTask + ", taskType=" + taskType + ", destName=" + destName
				+ ", priority=" + priority + ", agv_id=" + agv_id + ", customParam1=" + customParam1 + ", customParam2=" + customParam2 + "]";
	}
}
