package com.jimi.uw_server.agv.handle;

import com.jfinal.json.Json;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVBuildTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.agv.entity.cmd.AGVLoadExceptionCmd;
import com.jimi.uw_server.agv.entity.cmd.AgvDelMissionExceptionCmd;
import com.jimi.uw_server.constant.BuildTaskItemState;
import com.jimi.uw_server.constant.IOTaskItemState;
import com.jimi.uw_server.lock.Lock;
/**
 * 异常处理器
 * <br>
 * <b>2018年7月13日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class ExceptionHandler {
	
	public static void handleLoadException(String message) {

		AGVLoadExceptionCmd loadExceptionCmd = Json.getJson().parse(message, AGVLoadExceptionCmd.class);
		String groupid = loadExceptionCmd.getMissiongroupid();
		// missiongroupid 包含“:”表示为出入库任务
		if (groupid.contains(":")) {
			for(AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[1]))) {
				if(item.getGroupId().equals(groupid)) {
					//把指定叉车的取空异常置为真
					RobotInfoRedisDAO.setloadException(item.getRobotId());

					break;
				}
			}
		} else if (groupid.contains("@")) {	// missiongroupid 包含“@”表示为盘点任务
			for(AGVInventoryTaskItem item1 : TaskItemRedisDAO.getInventoryTaskItems()) {
				if(item1.getGroupId().equals(groupid)) {
					//把指定叉车的取空异常置为真
					RobotInfoRedisDAO.setloadException(item1.getRobotId());

					break;
				}
			}
		}else {	// missiongroupid 不包含“:”表示为建仓任务
			for(AGVBuildTaskItem item2 : TaskItemRedisDAO.getBuildTaskItems()) {
				if(item2.getGroupId().equals(groupid)) {
					//把指定叉车的取空异常置为真
					RobotInfoRedisDAO.setloadException(item2.getRobotId());

					break;
				}
			}
		}
		

	}
	

	public static void handleDelMissionException(String message) {

		AgvDelMissionExceptionCmd delMissionExceptionCmd = Json.getJson().parse(message, AgvDelMissionExceptionCmd.class);
		String groupid = delMissionExceptionCmd.getMissiongroupid();
		// missiongroupid 包含“:”表示为出入库任务
		if (groupid.contains(":")) {
			for(AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[1]))) {
				if(item.getGroupId().equals(groupid) && item.getState().intValue() > IOTaskItemState.WAIT_ASSIGN) {
					synchronized (Lock.ROBOT_ORDER_REDIS_LOCK) {
						// 清除掉对应的出入库任务条目
						if (TaskItemRedisDAO.getRobotOrder(item.getRobotId()).equals(groupid)) {
							TaskItemRedisDAO.setRobotOrder(item.getRobotId(), IOHandler.UNDEFINED);
						}
					}
					TaskItemRedisDAO.removeTaskItemByPackingListId(Integer.valueOf(groupid.split(":")[1]), item.getId().intValue());
						
					break;
				}
			}
			IOHandler.clearTil(groupid);
		}  else if (groupid.contains("@")) {	// missiongroupid 包含“@”表示为盘点任务
			for(AGVInventoryTaskItem item1 : TaskItemRedisDAO.getInventoryTaskItems()) {
				if(item1.getGroupId().equals(groupid)) {
					synchronized (Lock.ROBOT_ORDER_REDIS_LOCK) {
						if (TaskItemRedisDAO.getRobotOrder(item1.getRobotId()).equals(groupid)) {
							TaskItemRedisDAO.setRobotOrder(item1.getRobotId(), IOHandler.UNDEFINED);
						}
					}
					TaskItemRedisDAO.removeInventoryTaskItemById(item1.getTaskId(), item1.getBoxId());
					break;
				}
			}
			IOHandler.clearInventoryTask(Integer.valueOf(groupid.split("@")[0]));
		}else {	// missiongroupid 不包含“:”表示为建仓任务
			for(AGVBuildTaskItem item2 : TaskItemRedisDAO.getBuildTaskItems()) {
				if(item2.getGroupId().equals(groupid) && item2.getState() > BuildTaskItemState.WAIT_MOVE) {
					// 清除掉对应的建仓任务条目
					TaskItemRedisDAO.removeBuildTaskItemByBoxId(item2.getBoxId().intValue());

					break;
				}
				BuildHandler.clearTil(item2.getSrcPosition());
			}
		}
		

	}


}
