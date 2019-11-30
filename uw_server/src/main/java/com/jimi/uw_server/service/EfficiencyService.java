/**  
*  
*/  
package com.jimi.uw_server.service;

import com.jimi.uw_server.agv.dao.EfficiencyRedisDAO;
import com.jimi.uw_server.model.ActionLog;
import com.jimi.uw_server.model.SocketLog;
import com.jimi.uw_server.model.TaskLog;

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
	
	private String GET_USER_LAST_SCAN_MATERIAL_TIME = "SELECT * FROM task_log INNER JOIN packing_list_item ON task_log.packing_list_item_id = packing_list_item.id WHERE packing_list_item.task_id = ? ORDER BY task_log.time DESC";
	
	private String GET_TASK_LAST_START_TIME = "SELECT * FROM action_log WHERE action_log.action = ? AND result_code = 200 ORDER BY time DESC";
	
	private String GET_BOX_ARRIVED_TIME = "SELECT * FROM socket_log WHERE cmdcode = 'status' AND json like ? AND time < ? AND json like '%status\":2}' ORDER BY time DESC";
	
	public Long getUserLastOperationTime(Integer taskId, Integer boxId, String uid) {
		Long userLastOperationTime = EfficiencyRedisDAO.getUserLastOperationTime(taskId, uid);
		Long taskBoxArrivedTime = EfficiencyRedisDAO.getTaskBoxArrivedTime(taskId, boxId);
		Long taskLastStartTime = EfficiencyRedisDAO.getTaskStartTime(taskId);
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
				taskLastStartTime = (long) -1;
				EfficiencyRedisDAO.putTaskStartTime(taskId, (long)-1);
			}
		}
		if (userLastOperationTime == null) {
			//获取该任务最后一次出入库操作的时间
			TaskLog taskLog = TaskLog.dao.findFirst(GET_USER_LAST_SCAN_MATERIAL_TIME, taskId);
			if (taskLog != null && taskLog.getOperator().equals(uid)) {
				userLastOperationTime = taskLog.getTime().getTime();
				EfficiencyRedisDAO.putUserLastOperationTime(taskId, uid, taskLog.getTime().getTime());
			}else if (taskLog != null && !taskLog.getOperator().equals(uid)) {
				//中途切换人员操作后没有暂停过任务
				if (taskLastStartTime != null && taskLog.getTime().getTime() > taskLastStartTime) {
					userLastOperationTime = (long) -2;
				}
			}
		}
		
		if (taskBoxArrivedTime != null && userLastOperationTime != null && userLastOperationTime.intValue() != -2) {
			return getBiggerTime(taskLastStartTime, taskBoxArrivedTime, userLastOperationTime);
		}
		if (taskBoxArrivedTime != null && userLastOperationTime != null && userLastOperationTime.intValue() == -2) {
			return getBiggerTime(taskLastStartTime, taskBoxArrivedTime);
		}
		if (taskBoxArrivedTime != null && userLastOperationTime == null) {
			return getBiggerTime(taskLastStartTime, taskBoxArrivedTime);
		}
		if (taskBoxArrivedTime == null) {
			return null;
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
