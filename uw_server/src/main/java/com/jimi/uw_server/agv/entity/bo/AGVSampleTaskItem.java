package com.jimi.uw_server.agv.entity.bo;

import java.io.Serializable;


/**
 * AGV出入库任务条目 <br>
 * <b>2018年6月15日</b>
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
@SuppressWarnings("serial")
public class AGVSampleTaskItem implements Serializable {

	private Integer taskId;

	private Integer robotId;

	private Integer boxId;

	/**
	 * -1：不可分配  0：未分配  1：已分配拣料  2：已拣料到站  3：已分配回库  4：已回库完成
	 */
	private Integer state;

	/**
	 * false：出入库数量尚未满足实际需求	true：出入库数量已满足实际需求
	 */
	private Boolean isForceFinish;

	private Integer windowId;
	
	public AGVSampleTaskItem() {}
	
	public AGVSampleTaskItem(Integer taskId, Integer boxId) {
		this.robotId = 0;
		this.state = 0;
		this.boxId = boxId;
		this.windowId = 0;
		this.taskId = taskId;
		this.isForceFinish = false;
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


	public String getGroupId() {
		return boxId + "#" + taskId;
	}


	public Integer getWindowId() {
		return windowId;
	}

	public void setWindowId(Integer windowId) {
		this.windowId = windowId;
	}

}
