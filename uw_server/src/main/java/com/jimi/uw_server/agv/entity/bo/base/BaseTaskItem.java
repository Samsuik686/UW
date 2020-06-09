package com.jimi.uw_server.agv.entity.bo.base;

public class BaseTaskItem {

	protected Integer taskId;

	protected Integer robotId;

	protected Integer boxId;

	/**
	 * -1：不可分配 0：未分配 1：已分配拣料 2：已拣料到站 3：已分配回库 4：已回库完成
	 */
	protected Integer state;

	/**
	 * false：未完成 true：已完成
	 */
	protected Boolean isForceFinish;

	protected Integer windowId;

	protected Integer goodsLocationId;


	public Integer getTaskId() {
		return taskId;
	}


	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}


	public Integer getRobotId() {
		return robotId;
	}


	public void setRobotId(Integer robotId) {
		this.robotId = robotId;
	}


	public Integer getBoxId() {
		return boxId;
	}


	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}


	public Integer getState() {
		return state;
	}


	public void setState(Integer state) {
		this.state = state;
	}


	public Boolean getIsForceFinish() {
		return isForceFinish;
	}


	public void setIsForceFinish(Boolean isForceFinish) {
		this.isForceFinish = isForceFinish;
	}


	public Integer getWindowId() {
		return windowId;
	}


	public void setWindowId(Integer windowId) {
		this.windowId = windowId;
	}


	public Integer getGoodsLocationId() {
		return goodsLocationId;
	}


	public void setGoodsLocationId(Integer goodsLocationId) {
		this.goodsLocationId = goodsLocationId;
	}

}
