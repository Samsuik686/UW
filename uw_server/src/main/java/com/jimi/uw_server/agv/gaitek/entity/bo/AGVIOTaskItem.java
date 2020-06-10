package com.jimi.uw_server.agv.gaitek.entity.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jimi.uw_server.agv.gaitek.entity.bo.base.BaseTaskItem;
import com.jimi.uw_server.model.PackingListItem;


/**
 * AGV出入库任务条目 <br>
 * <b>2018年6月15日</b>
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */

public class AGVIOTaskItem extends BaseTaskItem {

	private Integer id;

	private Integer materialTypeId;

	private Integer planQuantity;

	/**
	 * 任务优先级，取值范围：1-9；数值越大，优先级越高
	 */
	private Integer priority;

	private Boolean isCut;

	private Boolean isSuperable;

	private Integer oldWindowId;

	private Integer uwQuantity;

	private Integer deductionQuantity;


	public AGVIOTaskItem() {
	}


	public AGVIOTaskItem(PackingListItem packingListItem, Integer state, Integer priority, Boolean isSuperable) {
		this.id = packingListItem.getId();
		this.taskId = packingListItem.getTaskId();
		this.materialTypeId = packingListItem.getMaterialTypeId();
		this.planQuantity = packingListItem.getQuantity();
		this.robotId = 0;
		this.state = state;
		this.boxId = 0;
		this.windowId = 0;
		this.goodsLocationId = 0;
		this.isForceFinish = false;
		this.priority = priority;
		this.isCut = false;
		this.isSuperable = isSuperable;
		this.oldWindowId = 0;
		this.uwQuantity = 0;
		this.deductionQuantity = 0;
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


	public Integer getPlanQuantity() {
		return planQuantity;
	}


	public void setPlanQuantity(Integer planQuantity) {
		this.planQuantity = planQuantity;
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


	@JsonIgnore
	public String getGroupId() {
		return this.id + ":" + this.boxId + ":" + this.taskId;
	}


	public Boolean getIsSuperable() {
		return isSuperable;
	}


	public void setIsSuperable(Boolean isSuperable) {
		this.isSuperable = isSuperable;
	}


	public Integer getOldWindowId() {
		return oldWindowId;
	}


	public void setOldWindowId(Integer oldWindowId) {
		this.oldWindowId = oldWindowId;
	}


	public Integer getUwQuantity() {
		return uwQuantity;
	}


	public void setUwQuantity(Integer uwQuantity) {
		this.uwQuantity = uwQuantity;
	}


	public Integer getDeductionQuantity() {
		return deductionQuantity;
	}


	public void setDeductionQuantity(Integer deductionQuantity) {
		this.deductionQuantity = deductionQuantity;
	}


	@Override
	public String toString() {
		return "AGVIOTaskItem [id=" + id + ", materialTypeId=" + materialTypeId + ", planQuantity=" + planQuantity + ", priority=" + priority + ", isCut=" + isCut + ", isSuperable=" + isSuperable
				+ ", oldWindowId=" + oldWindowId + ", uwQuantity=" + uwQuantity + ", deductionQuantity=" + deductionQuantity + ", taskId=" + taskId + ", robotId=" + robotId + ", boxId=" + boxId
				+ ", state=" + state + ", isForceFinish=" + isForceFinish + ", windowId=" + windowId + ", goodsLocationId=" + goodsLocationId + "]";
	}
}
