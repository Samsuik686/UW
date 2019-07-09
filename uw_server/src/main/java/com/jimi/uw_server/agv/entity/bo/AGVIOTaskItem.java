package com.jimi.uw_server.agv.entity.bo;

import java.io.Serializable;

import com.jimi.uw_server.agv.entity.bo.base.BaseTaskItem;
import com.jimi.uw_server.model.PackingListItem;

/**
 * AGV出入库任务条目 <br>
 * <b>2018年6月15日</b>
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
@SuppressWarnings("serial")
public class AGVIOTaskItem extends BaseTaskItem implements Serializable {

	private Integer id;

	private Integer materialTypeId;

	private Integer quantity;

	
	/**
	 * 任务优先级，取值范围：1-9；数值越大，优先级越高
	 */
	private Integer priority;

	private Boolean isCut;
	
	public AGVIOTaskItem() {}

	public AGVIOTaskItem(PackingListItem packingListItem, Integer state, Integer priority) {
		this.id = packingListItem.getId();
		this.taskId = packingListItem.getTaskId();
		this.materialTypeId = packingListItem.getMaterialTypeId();
		this.quantity = packingListItem.getQuantity();
		this.robotId = 0;
		this.state = state;
		this.boxId = 0;
		this.windowId = 0;
		this.goodsLocationId = 0;
		this.isForceFinish = false;
		this.priority = priority;
		this.isCut = false;
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


	public Boolean getIsCut() {
		return isCut;
	}

	public void setIsCut(Boolean isCut) {
		this.isCut = isCut;
	}

	public Integer getWindowId() {
		return windowId;
	}

	public void setWindowId(Integer windowId) {
		this.windowId = windowId;
	}

	public String getGroupId() {
		return this.id + ":" + this.boxId + ":" + this.taskId;
	}

}
