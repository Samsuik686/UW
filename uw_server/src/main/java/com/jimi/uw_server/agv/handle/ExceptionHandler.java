package com.jimi.uw_server.agv.handle;

import com.jfinal.aop.Enhancer;
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
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.service.MaterialService;
/**
 * 异常处理器
 * <br>
 * <b>2018年7月13日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class ExceptionHandler {
	
	private static MaterialService materialService = Enhancer.enhance(MaterialService.class);
	
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
	

	/*public static void handleDelMissionException(String message) {

		AgvDelMissionExceptionCmd delMissionExceptionCmd = Json.getJson().parse(message, AgvDelMissionExceptionCmd.class);
		String groupid = delMissionExceptionCmd.getMissiongroupid();
		// missiongroupid 包含“:”表示为出入库任务
		if (groupid.contains(":")) {
			for(AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[1]))) {
				if(item.getGroupId().equals(groupid) && item.getState().intValue() > IOTaskItemState.WAIT_ASSIGN) {
					
					if (item.getState() > IOTaskItemState.ARRIVED_WINDOW) {
						synchronized (Lock.IO_TASK_REDIS_LOCK) {
							Task task = Task.dao.findById(item.getTaskId());
							if (item.getIsForceFinish().equals(false) && task.getType().equals(TaskType.OUT)) {
								Integer remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(item.getMaterialTypeId());
								
								if (remainderQuantity <= 0) {
									item.setState(IOTaskItemState.LACK);
									item.setIsForceFinish(true);
									TaskItemRedisDAO.updateTaskIsForceFinish(item, true);
									TaskItemRedisDAO.updateIOTaskItemState( item, IOTaskItemState.LACK);
								}else {
									TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.FINISH_BACK);
								}
							}else {
								TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.FINISH_BACK);
							}

							// 设置料盒在架
							MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
							materialBox.setIsOnShelf(true);
							materialBox.update();
							if (item.getIsCut()) {
								TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.FINISH_CUT);
								TaskItemRedisDAO.updateIOTaskItemIsCut(item, false);
							}
							if (!item.getIsForceFinish()) {
								// 如果是出库任务，若实际出库数量小于计划出库数量，则将任务条目状态回滚到未分配状态
								if (task.getType() == TaskType.OUT) {
									TaskItemRedisDAO.updateIOTaskItemRobot(item, 0);
									TaskItemRedisDAO.updateTaskItemBoxId(item, 0);
									TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.WAIT_ASSIGN);
								} else {	// 如果是入库或退料入库任务，若实际入库或退料入库数量小于计划入库或退料入库数量，则将任务条目状态回滚到等待扫码状态
									TaskItemRedisDAO.updateIOTaskItemRobot(item, 0);
									TaskItemRedisDAO.updateTaskItemBoxId(item, 0);
									TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.WAIT_SCAN);
								}
							}
							IOHandler.clearTil(item.getGroupId());
						}
					}
					
					synchronized (Lock.ROBOT_ORDER_REDIS_LOCK) {
						// 清除掉对应的出入库任务条目
						if (TaskItemRedisDAO.getRobotOrder(item.getRobotId()).equals(groupid)) {
							TaskItemRedisDAO.setRobotOrder(item.getRobotId(), IOHandler.UNDEFINED);
						}
					}
						
					break;
				}
			}
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
		

	}*/
	
	
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
						
					break;
				}
			}
			IOHandler.clearIoTil(groupid);
		}  else if (groupid.contains("@")) {	// missiongroupid 包含“@”表示为盘点任务
			for(AGVInventoryTaskItem item1 : TaskItemRedisDAO.getInventoryTaskItems()) {
				if(item1.getGroupId().equals(groupid)) {
					synchronized (Lock.ROBOT_ORDER_REDIS_LOCK) {
						if (TaskItemRedisDAO.getRobotOrder(item1.getRobotId()).equals(groupid)) {
							TaskItemRedisDAO.setRobotOrder(item1.getRobotId(), IOHandler.UNDEFINED);
						}
					}
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
