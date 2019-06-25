package com.jimi.uw_server.model.vo;

import java.util.Date;

import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.User;

/**
 * 任务日志表示层对象
 * @author HardyYao
 * @createTime 2018年7月5日 上午11:22:18 
 */
@SuppressWarnings("serial")
public class TaskLogVO extends TaskLog {
	
	private String autoString;

	private String taskTypeString;
	
	private String operatorName;


	public TaskLogVO(Integer id, Integer packingListItemId, Integer type, String materialId, String materialNo, Integer quantity, String operator, boolean auto, Date time) {
		this.setId(id);
		this.set("packingListItemId", packingListItemId);
		this.setTaskTypeString(type);
		this.set("taskType", getTaskType());
		this.set("materialId", materialId);
		this.set("materialNo", materialNo);
		this.setQuantity(quantity);
		this.setOperator(operator);
		this.setAutoString(auto);
		this.set("auto", getAutoString());
		this.setOperatorName(operator);
		this.set("operatorName", getOperatorName());
		this.setTime(time);
	}


	public void setTaskTypeString(Integer type) {
		switch (type) {
		case TaskType.IN:
			this.taskTypeString = "入库";
			break;
		case TaskType.OUT:
			this.taskTypeString = "出库";
			break;
		case TaskType.COUNT:
			this.taskTypeString = "盘点";
			break;
		case TaskType.POSITION_OPTIZATION:
			this.taskTypeString = "位置优化";
			break;
		case TaskType.SEND_BACK:
			this.taskTypeString = "调拨入库";
			break;
		case TaskType.SAMPLE:
			this.taskTypeString = "抽检";
			break;
		default:
			this.taskTypeString = "错误类型";
			break;
		}
	}


	public String getTaskType() {
		return taskTypeString;
	}


	public void setAutoString(boolean auto) {
		if(auto) {
			this.autoString = "自动";
		} else {
			this.autoString = "手动";
		}
	}


	public String getAutoString() {
		return autoString;
	}

	
	public String setOperatorName(String operator) {
		if (operator != null) {
			User user = User.dao.findById(operator);
			operatorName = user.getName();
		} else {
			operatorName = "缺料记录";
		}
		return operatorName;
	}


	public String getOperatorName() {
		return operatorName;
	}


}
