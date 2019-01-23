package com.jimi.uw_server.model.vo;

import java.util.Date;
import java.util.List;

import com.jimi.uw_server.constant.IOTaskItemState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.model.TaskLog;

/**
 * 仓口任务条目表示层
 * @author HardyYao
 * @createTime 2018年7月23日 下午4:04:25
 */
@SuppressWarnings("serial")
public class WindowTaskItemsVO extends TaskLog {

	private List<?> details;

	private String typeString;

	private String stateString;


	public WindowTaskItemsVO(Integer packingListItemId, String fileName, Integer type, String materialNo, Integer planQuantity, Integer actualQuantity, Date finishTime, Integer state) {
		this.set("id", packingListItemId);
		this.set("fileName", fileName);
		this.setTypeString(type);
		this.set("type", getTypeString());
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
		case IOTaskItemState.FINISH_CUT:
			this.stateString = "等待截料返库";
			break;
		case IOTaskItemState.WAIT_SCAN:
			this.stateString = "等待扫码";
			break;
		case IOTaskItemState.WAIT_ASSIGN:
			this.stateString = "未分配给叉车";	
			break;
		case IOTaskItemState.ASSIGNED:
			this.stateString = "已分配拣料";
			break;
		case IOTaskItemState.ARRIVED_WINDOW:
			this.stateString = "已拣料到站";
			break;
		case IOTaskItemState.START_BACK:
			this.stateString = "已分配回库";
			break;
		case IOTaskItemState.FINISH_BACK:
			this.stateString = "已回库完成";
			break;
		default:
			this.stateString = "异常状态";
			break;
		}
	}


	public String getTypeString() {
		return typeString;
	}


	public void setTypeString(Integer type) {
		switch (type) {
		case TaskType.IN:
			this.typeString = "入库";
			break;
		case TaskType.OUT:
			this.typeString = "出库";
			break;
		case TaskType.COUNT:
			this.typeString = "盘点";
			break;
		case TaskType.POSITION_OPTIZATION:
			this.typeString = "位置优化";
			break;
		case TaskType.SEND_BACK:
			this.typeString = "退料入库";
			break;
		default:
			this.typeString = "错误类型";
			break;
		}
	}


}
