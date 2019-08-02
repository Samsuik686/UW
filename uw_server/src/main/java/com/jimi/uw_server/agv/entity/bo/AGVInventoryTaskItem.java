package com.jimi.uw_server.agv.entity.bo;

import java.io.Serializable;

import com.jimi.uw_server.agv.entity.bo.base.BaseTaskItem;


/**
 * 
 * @author trjie
 * @createTime 2019年5月16日  上午9:38:15
 */
@SuppressWarnings("serial")
public class AGVInventoryTaskItem extends BaseTaskItem implements Serializable {

	/**
	 * 任务优先级，取值范围：1-9；数值越大，优先级越高
	 */
	private Integer priority;


	public AGVInventoryTaskItem() {
	}


	public AGVInventoryTaskItem(Integer taskId, Integer boxId, Integer state, Integer priority, Integer windowId) {
		this.taskId = taskId;
		this.robotId = 0;
		this.state = state;
		this.boxId = boxId;
		this.isForceFinish = false;
		this.priority = priority;
		this.windowId = windowId;
		this.goodsLocationId = 0;
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


	public String getGroupId() {
		return boxId + "@" + taskId;
	}


	public Integer getWindowId() {
		return windowId;
	}


	public void setWindowId(Integer windowId) {
		this.windowId = windowId;
	}

}
