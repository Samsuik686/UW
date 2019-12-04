/**  
*  
*/  
package com.jimi.uw_server.service;

import java.util.List;

import com.jimi.uw_server.agv.dao.EfficiencyRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.constant.sql.SQL;
import com.jimi.uw_server.model.ActionLog;
import com.jimi.uw_server.model.SocketLog;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.Window;

/**  
 * <p>Title: EfficiencyService</p>  
 * <p>Description: 效率统计，统计用户出入库效率</p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2019年11月29日
 *
 */
public class EfficiencyService {
	
	private String GET_USER_LAST_SCAN_MATERIAL_TIME = "SELECT * FROM task_log INNER JOIN packing_list_item ON task_log.packing_list_item_id = packing_list_item.id WHERE task_log.operator = ? ORDER BY task_log.time DESC";
	
	private String GET_TASK_LAST_SCAN_MATERIAL_TIME = "SELECT * FROM task_log INNER JOIN packing_list_item ON task_log.packing_list_item_id = packing_list_item.id WHERE packing_list_item.task_id = ?  AND task_log.operator IS NOT NULL ORDER BY task_log.time DESC";
	
	private String GET_TASK_LAST_START_TIME = "SELECT * FROM action_log WHERE action_log.action = ? AND result_code = 200 ORDER BY time DESC";
	
	private String GET_BOX_ARRIVED_TIME = "SELECT * FROM socket_log WHERE cmdcode = 'status' AND json like ? AND json like '%status\":2}' ORDER BY time DESC";
	
	public static void initTaskEfficiency() {
		List<Window> windows = Window.dao.find(SQL.GET_WORKING_WINDOWS);
		if (!windows.isEmpty()) {
			for (Window window : windows) {
				TaskItemRedisDAO.delTaskStatus(window.getBindTaskId());
			}
		}
		EfficiencyRedisDAO.removeTaskBoxArrivedTime();
		EfficiencyRedisDAO.removeTaskStartTime();
		EfficiencyRedisDAO.removeTaskLastOperationUser();
		EfficiencyRedisDAO.removeTaskLastOperationTime();
		EfficiencyRedisDAO.removeUserLastOperationTime();
	}
	
	public Long getUserLastOperationTime(Integer taskId, Integer boxId, String uid) {
		Long TaskLastOperationTime = EfficiencyRedisDAO.getTaskLastOperationTime(taskId);
		Long taskBoxArrivedTime = EfficiencyRedisDAO.getTaskBoxArrivedTime(taskId, boxId);
		Long taskLastStartTime = EfficiencyRedisDAO.getTaskStartTime(taskId);
		Long userLastOperationTime = EfficiencyRedisDAO.getUserLastOperationTime(uid);
		
		String taskLastOperationUser = EfficiencyRedisDAO.getTaskLastOperationUser(taskId);
		if (taskBoxArrivedTime == null) {
			SocketLog socketLog = SocketLog.dao.findFirst(GET_BOX_ARRIVED_TIME, "%:" + boxId + ":" + taskId + "_S%");
			if (socketLog != null) {
				taskBoxArrivedTime = socketLog.getTime().getTime();
				EfficiencyRedisDAO.putTaskBoxArrivedTime(taskId, boxId, socketLog.getTime().getTime());
			}
		}
		if (taskLastStartTime == null) {
			ActionLog actionLog = ActionLog.dao.findFirst(GET_TASK_LAST_START_TIME, getTaskStartAction(taskId));
			if (actionLog != null) {
				taskLastStartTime = actionLog.getTime().getTime();
				EfficiencyRedisDAO.putTaskStartTime(taskId, actionLog.getTime().getTime());
			}else {
				taskLastStartTime = (long) 0;
				EfficiencyRedisDAO.putTaskStartTime(taskId, (long) 0);
			}
		}
		TaskLog taskLog = null;
		if (taskLastOperationUser == null) {
			taskLog = TaskLog.dao.findFirst(GET_TASK_LAST_SCAN_MATERIAL_TIME, taskId);
			if (taskLog != null) {
				taskLastOperationUser = taskLog.getOperator();
				EfficiencyRedisDAO.putTaskLastOperationUser(taskId, taskLog.getOperator());
			}
		}
		if (userLastOperationTime == null) {
			TaskLog userLastTaskLog = TaskLog.dao.findFirst(GET_USER_LAST_SCAN_MATERIAL_TIME, uid);
			if (userLastTaskLog != null) {
				userLastOperationTime = userLastTaskLog.getTime().getTime();
			}
		}
		if (TaskLastOperationTime == null && taskLastOperationUser != null) {
			//获取该任务最后一次出入库操作的时间
			if (taskLog == null) {
				taskLog = TaskLog.dao.findFirst(GET_TASK_LAST_SCAN_MATERIAL_TIME, taskId);
			}
			if (taskLog != null) {
				TaskLastOperationTime = taskLog.getTime().getTime();
				EfficiencyRedisDAO.putTaskLastOperationTime(taskId, taskLog.getTime().getTime());
			}
		}
		System.out.println("arrived:" + taskBoxArrivedTime);
		System.out.println("task_last:" + TaskLastOperationTime);
		System.out.println("user_last:" + userLastOperationTime);
		System.out.println("start：" + taskLastStartTime);
		if (taskBoxArrivedTime == null) {
			return null;
		}
		if (TaskLastOperationTime != null && userLastOperationTime == null) {
			return getBiggerTime(taskLastStartTime, taskBoxArrivedTime, TaskLastOperationTime);
		}
		if (TaskLastOperationTime != null && userLastOperationTime != null) {
			return getBiggerTime(getBiggerTime(taskLastStartTime, taskBoxArrivedTime, TaskLastOperationTime), userLastOperationTime);
		}
		if (TaskLastOperationTime == null && userLastOperationTime != null) {
			return getBiggerTime(taskLastStartTime, taskBoxArrivedTime, userLastOperationTime);
		}
		if (TaskLastOperationTime == null && userLastOperationTime == null) {
			return getBiggerTime(taskLastStartTime, taskBoxArrivedTime);
		}
		
		
		return null;
		
	}

	private String getTaskStartAction(Integer taskId) {
		return "开始/暂停任务" + taskId + "，Flagtrue";
	}
	
	
	private Long getBiggerTime(Long t1, Long t2) {
		if (t1 > t2) {
			return t1;
		}else {
			return t2;
		}
	}
	
	
	private Long getBiggerTime(Long t1, Long t2, Long t3) {
		if (t1 > t2) {
			if (t1 > t3) {
				return t1;
			}else {
				return t3;
			}
		}else {
			if (t2 > t3) {
				return t2;
			}else {
				return t3;
			}
		}
	}
}