package com.jimi.uw_server.model.vo;

import java.util.Date;

import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;

/**
 * 
 * @author trjie
 * @createTime 2019年5月21日  下午3:00:34
 */

public class InventoryTaskVO {

	private String typeString;
	
	private String stateString;

	private String supplierName;
	
	private Integer taskId;
	
	private Integer type;
	
	private String taskName;
	
	private Integer state;
	
	private Integer supplierId;
	
	private Date startTime;
	
	private Date endTime;
	
	private String checkedOperatior;
	
	private Date checkedTime;
	
	private Date createTime;
	
	public String getTypeString() {
		return typeString;
	}

	public void setTypeString(Integer type) {
		if (type == TaskType.IN) {
			this.typeString = "入库";
		} else if (type == TaskType.OUT) {
			this.typeString = "出库";
		} else if (type == TaskType.COUNT) {
			this.typeString = "盘点";
		}  else if (type == TaskType.POSITION_OPTIZATION) {
			this.typeString = "位置优化";
		} else if (type == 4) {
			this.typeString = "调拨入库";
		}
	}
	
	public String getStateString() {
		return stateString;
	}

	public void setStateString(Integer state) {
		if (state == TaskState.WAIT_REVIEW) {
			this.stateString = "未审核";
		} else if (state == TaskState.WAIT_START) {
			this.stateString = "未开始";
		} else if (state == TaskState.PROCESSING) {
			this.stateString = "进行中";
		}  else if (state == TaskState.FINISHED) {
			this.stateString = "已完成";
		} else if (state == TaskState.CANCELED) {
			this.stateString = "已作废";
		} else if (state == TaskState.EXIST_LACK) {
			this.stateString = "存在缺料";
		}
	}

	public String getSupplierName() {
		return supplierName;
	}
	
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getCheckedOperatior() {
		return checkedOperatior;
	}

	public void setCheckedOperatior(String checkedOperatior) {
		this.checkedOperatior = checkedOperatior;
	}

	public Date getCheckedTime() {
		return checkedTime;
	}

	public void setCheckedTime(Date checkedTime) {
		this.checkedTime = checkedTime;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	
	
}
