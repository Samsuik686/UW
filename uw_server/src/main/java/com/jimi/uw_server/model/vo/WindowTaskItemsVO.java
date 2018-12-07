package com.jimi.uw_server.model.vo;

import java.util.Date;
import java.util.List;

import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.model.TaskLog;

/**
 * 
 * @author HardyYao
 * @createTime 2018年7月23日 下午4:04:25
 */
@SuppressWarnings("serial")
public class WindowTaskItemsVO extends TaskLog {

	private List<?> details;

	private String typeString;

	private String stateString;


	public String getType(Integer type) {
		if (type == TaskType.IN) {
			typeString = "入库";
		} else if (type == TaskType.OUT) {
			typeString = "出库";
		} else if (type == TaskType.COUNT) {
			typeString = "盘点";
		} else if (type == TaskType.POSITION_OPTIZATION) {
			typeString = "位置优化";
		}
		return typeString;
	}

	public WindowTaskItemsVO(Integer packingListItemId, String fileName, Integer type, String materialNo, Integer planQuantity, Integer actualQuantity, Date finishTime, Integer state) {
		this.set("id", packingListItemId);
		this.set("fileName", fileName);
		this.set("type", getType(type));
		this.set("materialNo", materialNo);
		this.set("planQuantity", planQuantity);
		this.set("actualQuantity", actualQuantity);
		if (finishTime == null) {
			this.set("finishTime", "no");
		} else {
			this.set("finishTime", finishTime);
		}
		this.set("state", state);
		this.setStateString(state);
		this.set("stateString", getStateString());
	}

	public List<?> getDetails() {
		return details;
	}

	public void setDetails(List<?> details) {
		this.set("details", details);
	}

	public String getStateString() {
		return stateString;
	}

	public void setStateString(Integer state) {
		switch (state) {
		case TaskItemState.UNASSIGNABLED:
			this.stateString = "不可分配";
			break;
		case TaskItemState.WAIT_ASSIGN:
			this.stateString = "未分配";	
			break;
		case TaskItemState.ASSIGNED:
			this.stateString = "已分配拣料";
			break;
		case TaskItemState.ARRIVED_WINDOW:
			this.stateString = "已拣料到站";
			break;
		case TaskItemState.START_BACK:
			this.stateString = "已分配回库";
			break;
		case TaskItemState.FINISH_BACK:
			this.stateString = "已回库完成";
			break;
		default:
			this.stateString = "异常状态";
			break;
		}
	}

}
