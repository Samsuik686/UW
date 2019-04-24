package com.jimi.uw_server.model.vo;

import java.util.Date;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;

/**
 * 任务表示层对象
 * @author HardyYao
 * @createTime 2018年10月13日  上午10:01:54
 */

@SuppressWarnings("serial")
public class TaskVO extends Task{

	private String typeString;
	
	private String stateString;

	private String supplierName;


	public TaskVO(Integer id, Integer state, Integer type, String fileName, Date createTime, Integer priority, Integer supplier) {
		this.setId(id);
		this.setState(state);
		this.setStateString(state);
		this.set("stateString", getStateString());
		this.setType(type);
		this.setTypeString(type);
		this.set("typeString", getTypeString());
		this.set("fileName", fileName);
		this.setSupplier(supplier);
		this.setSupplierName(supplier);
		this.set("supplierName", getSupplierName());
		this.set("createTimeString", createTime);
		this.setPriority(priority);
	}

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
			this.typeString = "退料入库";
		}
	}

	public String getStateString(Integer state) {
		return stateString;
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

	public void setSupplierName(Integer supplierId) {
		Supplier s = Supplier.dao.findById(supplierId);
		String name = s.getName();
		this.supplierName = name;
	}


}
