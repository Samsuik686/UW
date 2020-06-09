package com.jimi.uw_server.agv.entity.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jimi.uw_server.agv.entity.bo.base.BaseTaskItem;


/**
 * 
 * @author trjie
 * @createTime 2019年5月16日 上午9:38:15
 */
public class AGVInventoryTaskItem extends BaseTaskItem {

	/**
	 * 任务优先级，取值范围：1-9；数值越大，优先级越高
	 */
	private Integer priority;

	private Integer boxType;


	public AGVInventoryTaskItem() {
	}


	public AGVInventoryTaskItem(Integer taskId, Integer boxId, Integer state, Integer priority, Integer windowId, Integer boxType) {
		this.taskId = taskId;
		this.robotId = 0;
		this.state = state;
		this.boxId = boxId;
		this.isForceFinish = false;
		this.priority = priority;
		this.windowId = windowId;
		this.goodsLocationId = 0;
		this.boxType = boxType;

	}


	public Integer getRobotId() {
		return robotId;
	}


	public void setRobotId(Integer robotId) {
		this.robotId = robotId;
	}


	public Integer getState() {
		return state;
	}


	public void setState(Integer state) {
		this.state = state;
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


	public Boolean getIsForceFinish() {
		return isForceFinish;
	}


	public void setIsForceFinish(Boolean isForceFinish) {
		this.isForceFinish = isForceFinish;
	}


	public Integer getPriority() {
		return priority;
	}


	public void setPriority(Integer priority) {
		this.priority = priority;
	}


	@JsonIgnore
	public String getGroupId() {
		return boxId + "@" + taskId;
	}


	public Integer getWindowId() {
		return windowId;
	}


	public void setWindowId(Integer windowId) {
		this.windowId = windowId;
	}


	public Integer getBoxType() {
		return boxType;
	}


	public void setBoxType(Integer boxType) {
		this.boxType = boxType;
	}


	@Override
	public String toString() {
		return "AGVInventoryTaskItem [priority=" + priority + ", boxType=" + boxType + ", taskId=" + taskId + ", robotId=" + robotId + ", boxId=" + boxId + ", state=" + state + ", isForceFinish="
				+ isForceFinish + ", windowId=" + windowId + ", goodsLocationId=" + goodsLocationId + "]";
	}


}
