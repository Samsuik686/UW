package com.jimi.uw_server.agv.handle;

import com.jfinal.aop.Aop;
import com.jfinal.aop.Enhancer;
import com.jfinal.json.Json;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVBuildTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.agv.entity.cmd.AGVLoadExceptionCmd;
import com.jimi.uw_server.agv.entity.cmd.AgvDelMissionExceptionCmd;
import com.jimi.uw_server.constant.BuildTaskItemState;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.TaskType;
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
	
	private static IOTaskHandler ioTaskHandler = IOTaskHandler.getInstance();
	
	private static InvTaskHandler invTaskHandler = InvTaskHandler.getInstance();
	
	private static SamTaskHandler samTaskHandler = SamTaskHandler.getInstance();
	
	public static void handleLoadException(String message) {

		AGVLoadExceptionCmd loadExceptionCmd = Json.getJson().parse(message, AGVLoadExceptionCmd.class);
		RobotInfoRedisDAO.setloadException(loadExceptionCmd.getRobotid());
		/*String missionGroupId = loadExceptionCmd.getMissiongroupid();
		// missiongroupid 包含“:”表示为出入库任务
		String groupid = missionGroupId.split("_")[0];
		// missiongroupid 包含“:”表示为出入库任务
		if (groupid.contains(":")) {
			for(AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {
				if(item.getGroupId().equals(groupid)) {
					//把指定叉车的取空异常置为真
					RobotInfoRedisDAO.setloadException(item.getRobotId());
					break;
				}
			}
		} else if (groupid.contains("@")) {	// missiongroupid 包含“@”表示为盘点任务
			for(AGVInventoryTaskItem item1 : TaskItemRedisDAO.getInventoryTaskItems(Integer.valueOf(groupid.split("@")[1]))) {
				if(item1.getGroupId().equals(groupid)) {
					//把指定叉车的取空异常置为真
					RobotInfoRedisDAO.setloadException(item1.getRobotId());

					break;
				}
			}
		}else if (groupid.contains("#")) {	// missiongroupid 包含“@”表示为盘点任务
			for(AGVInventoryTaskItem item1 : TaskItemRedisDAO.getInventoryTaskItems(Integer.valueOf(groupid.split("#")[1]))) {
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
		}*/
		

	}

	
	public static void handleDelMissionException(String message) {

		AgvDelMissionExceptionCmd delMissionExceptionCmd = Json.getJson().parse(message, AgvDelMissionExceptionCmd.class);
		String missionGroupId = delMissionExceptionCmd.getMissiongroupid();
		// missiongroupid 包含“:”表示为出入库任务
		String groupid = missionGroupId.split("_")[0];
		if (groupid.contains(":") && missionGroupId.contains("B")) {
			AGVIOTaskItem agvioTaskItem = null;
			for(AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {
				if (item.getGroupId().equals(groupid) && item.getState() > TaskItemState.ARRIVED_WINDOW) {
					agvioTaskItem = item;
					if (agvioTaskItem.getState() == TaskItemState.START_BACK) {
						TaskItemRedisDAO.delLocationStatus(agvioTaskItem.getWindowId(), agvioTaskItem.getGoodsLocationId());
					}
					break;
				}
				
			}
			if (agvioTaskItem != null) {
				for(AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {
					if(item.getBoxId().equals(agvioTaskItem.getBoxId()) && item.getWindowId().equals(agvioTaskItem.getWindowId()) && item.getGoodsLocationId().equals(agvioTaskItem.getGoodsLocationId()) && item.getState() > TaskItemState.ARRIVED_WINDOW) {
						
						Task task = Task.dao.findById(item.getTaskId());
						if (item.getIsForceFinish().equals(false) && task.getType().equals(TaskType.OUT)) {
							Integer remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(item.getMaterialTypeId());
							if (remainderQuantity <= 0) {
								item.setState(TaskItemState.LACK);
								item.setIsForceFinish(true);
								TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.LACK, null, null, null, null, true, null);
							}else {
								TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null, null, null);
							}
						}else {
							TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null, null, null);
						}

						// 设置料盒在架
						MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
						materialBox.setIsOnShelf(true);
						materialBox.update();
						if (item.getIsCut()) {
							TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_CUT, null, null, null, null, null, false);
						}
						if (!item.getIsForceFinish()) {
							// 如果是出库任务，若实际出库数量小于计划出库数量，则将任务条目状态回滚到未分配状态
							if (task.getType() == TaskType.OUT) {
								TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.WAIT_ASSIGN, 0, 0, 0, 0, null, null);
							} else {	// 如果是入库或退料入库任务，若实际入库或退料入库数量小于计划入库或退料入库数量，则将任务条目状态回滚到等待扫码状态
								TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.WAIT_SCAN, 0, 0, 0, 0, null, null);
							}
						}
						
					}
				}
			}
			
			
			ioTaskHandler.clearTask(Integer.valueOf(groupid.split(":")[2]));
			
		} else if (groupid.contains("@") && missionGroupId.contains("B")) {	// missiongroupid 包含“@”表示为盘点任务
			for(AGVInventoryTaskItem item : TaskItemRedisDAO.getInventoryTaskItems(Integer.valueOf(groupid.split(":")[1]))) {
				if(item.getGroupId().equals(groupid) && item.getState() > TaskItemState.ARRIVED_WINDOW) {
					if (item.getState() == TaskItemState.START_BACK) {
						TaskItemRedisDAO.delLocationStatus(item.getWindowId(), item.getGoodsLocationId());
					}
					MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
					materialBox.setIsOnShelf(true);
					materialBox.update();
					TaskItemRedisDAO.updateInventoryTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null);
				}
			}
			invTaskHandler.clearTask(Integer.valueOf(groupid.split("@")[1]));
		} else if (groupid.contains("#") && missionGroupId.contains("B")) {	// missiongroupid 包含“@”表示为盘点任务
			for(AGVSampleTaskItem item : TaskItemRedisDAO.getSampleTaskItems(Integer.valueOf(groupid.split(":")[1]))) {
				if(item.getGroupId().equals(groupid) && item.getState() > TaskItemState.ARRIVED_WINDOW) {
					if (item.getState() == TaskItemState.START_BACK) {
						TaskItemRedisDAO.delLocationStatus(item.getWindowId(), item.getGoodsLocationId());
					}
					MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
					materialBox.setIsOnShelf(true);
					materialBox.update();
					TaskItemRedisDAO.updateSampleTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null);
				}
			}
			samTaskHandler.clearTask(Integer.valueOf(groupid.split("#")[1]));
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
