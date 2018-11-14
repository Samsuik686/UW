package com.jimi.uw_server.agv.entity.bo;

import com.jimi.uw_server.model.PackingListItem;

/**
 * AGV出入库任务条目 <br>
 * <b>2018年6月15日</b>
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class AGVIOTaskItem {

	private Integer id;

	private Integer taskId;

	private Integer materialTypeId;

	private Integer quantity;

	private Integer robotId;

	private Integer boxId;

	/**
	 * 0：未分配 1：已分配拣料 2：已拣料到站 3：已分配回库 4：已回库完成
	 */
	private Integer state;

	/**
	 * false：出入库数量尚未满足实际需求	true：出入库数量已满足实际需求
	 */
	private Boolean isForceFinish;

	/**
	 * 1：最低优先级 2：普通优先级  3：最高优先级
	 */
	private Integer priority;
	
	public AGVIOTaskItem() {}
	
	
	public AGVIOTaskItem(PackingListItem packingListItem, Integer priority) {
		this.id = packingListItem.getId();
		this.taskId = packingListItem.getTaskId();
		this.materialTypeId = packingListItem.getMaterialTypeId();
		this.quantity = packingListItem.getQuantity();
		this.robotId = 0;
		this.state = 0;
		this.boxId = 0;
		this.isForceFinish = false;
		this.priority = priority;
	}

	public AGVIOTaskItem(PackingListItem packingListItem, Integer state, Integer priority) {
		this.id = packingListItem.getId();
		this.taskId = packingListItem.getTaskId();
		this.materialTypeId = packingListItem.getMaterialTypeId();
		this.quantity = packingListItem.getQuantity();
		this.robotId = 0;
		this.state = state;
		this.boxId = 0;
		this.isForceFinish = false;
		this.priority = priority;
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

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public Integer getMaterialTypeId() {
		return materialTypeId;
	}

	public void setMaterialTypeId(Integer materialTypeId) {
		this.materialTypeId = materialTypeId;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
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
		return boxId + ":" + taskId;
	}

}
