package com.jimi.uw_server.agv.handle;

import com.jfinal.aop.Aop;
import com.jfinal.json.Json;
import com.jimi.uw_server.agv.dao.BuildTaskItemDAO;
import com.jimi.uw_server.agv.dao.IOTaskItemRedisDAO;
import com.jimi.uw_server.agv.dao.InventoryTaskItemRedisDAO;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.SampleTaskItemRedisDAO;
import com.jimi.uw_server.agv.dao.TaskPropertyRedisDAO;
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

	private static MaterialService materialService = Aop.get(MaterialService.class);

	private static IOTaskHandler ioTaskHandler = IOTaskHandler.getInstance();

	private static InvTaskHandler invTaskHandler = InvTaskHandler.getInstance();

	private static SampleTaskHandler samTaskHandler = SampleTaskHandler.getInstance();


	public static void handleLoadException(String message) {

		AGVLoadExceptionCmd loadExceptionCmd = Json.getJson().parse(message, AGVLoadExceptionCmd.class);
		RobotInfoRedisDAO.setloadException(loadExceptionCmd.getRobotid());
	}


	public static void handleDelMissionException(String message) {

		AgvDelMissionExceptionCmd delMissionExceptionCmd = Json.getJson().parse(message, AgvDelMissionExceptionCmd.class);
		String missionGroupId = delMissionExceptionCmd.getMissiongroupid();
		// missiongroupid 包含“:”表示为出入库任务
		String groupid = missionGroupId.split("_")[0];
		if (groupid.contains(":") && missionGroupId.contains("B")) {
			AGVIOTaskItem agvioTaskItem = IOTaskItemRedisDAO.getIOTaskItem(Integer.valueOf(groupid.split(":")[2]), Integer.valueOf(groupid.split(":")[0]));
			if (agvioTaskItem == null) {
				return ;
			}
			if (agvioTaskItem.getState() > TaskItemState.ARRIVED_WINDOW) {
				if (agvioTaskItem.getState() == TaskItemState.START_BACK) {
					TaskPropertyRedisDAO.delLocationStatus(agvioTaskItem.getWindowId(), agvioTaskItem.getGoodsLocationId());
				}
			}
			for (AGVIOTaskItem item : IOTaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {
				if (item.getBoxId().equals(agvioTaskItem.getBoxId()) && item.getWindowId().equals(agvioTaskItem.getWindowId()) && item.getGoodsLocationId().equals(agvioTaskItem.getGoodsLocationId()) && item.getState() > TaskItemState.ARRIVED_WINDOW) {
					Task task = Task.dao.findById(item.getTaskId());
					if (item.getIsForceFinish().equals(false) && task.getType().equals(TaskType.OUT)) {
						Integer remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(item.getMaterialTypeId());
						if (remainderQuantity <= 0) {
							item.setState(TaskItemState.LACK);
							item.setIsForceFinish(true);
							IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.LACK, null, null, null, null, true, null, null, null, null);
						} else {
							IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null, null, null, null, null, null);
						}
					} else {
						IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null, null, null, null, null, null);
					}

					// 设置料盒在架
					MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
					materialBox.setIsOnShelf(true);
					materialBox.update();
					if (item.getIsCut()) {
						IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_CUT, null, null, null, null, null, false, null, null, null);
					}
					if (!item.getIsForceFinish()) {
						// 如果是出库任务，若实际出库数量小于计划出库数量，则将任务条目状态回滚到未分配状态
						if (task.getType() == TaskType.OUT) {
							IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.WAIT_ASSIGN, 0, 0, 0, 0, null, null, null, null, null);
						} else { // 如果是入库或退料入库任务，若实际入库或退料入库数量小于计划入库或退料入库数量，则将任务条目状态回滚到等待扫码状态
							IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.WAIT_SCAN, 0, 0, 0, 0, null, null, null, null, null);
						}
					}

				}
			}
			ioTaskHandler.clearTask(Integer.valueOf(groupid.split(":")[2]), false);
		} else if (groupid.contains("@") && missionGroupId.contains("B")) { // missiongroupid 包含“@”表示为盘点任务
			AGVInventoryTaskItem item = InventoryTaskItemRedisDAO.getInventoryTaskItem(Integer.valueOf(groupid.split("@")[1]), Integer.valueOf(groupid.split("@")[0]));
			if (item == null) {
				return ;
			}
			if (item.getState() > TaskItemState.ARRIVED_WINDOW) {
				if (item.getState() == TaskItemState.START_BACK) {
					TaskPropertyRedisDAO.delLocationStatus(item.getWindowId(), item.getGoodsLocationId());
				}
				MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
				materialBox.setIsOnShelf(true);
				materialBox.update();
				InventoryTaskItemRedisDAO.updateInventoryTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null);
			}
			invTaskHandler.clearTask(Integer.valueOf(groupid.split("@")[1]));
		} else if (groupid.contains("#") && missionGroupId.contains("B")) { // missiongroupid 包含“@”表示为盘点任务
			AGVSampleTaskItem item = SampleTaskItemRedisDAO.getSampleTaskItem(Integer.valueOf(groupid.split("#")[1]), Integer.valueOf(groupid.split("#")[0]));
			if (item == null) {
				return ;
			}
			if (item.getState() > TaskItemState.ARRIVED_WINDOW) {
				if (item.getState() == TaskItemState.START_BACK) {
					TaskPropertyRedisDAO.delLocationStatus(item.getWindowId(), item.getGoodsLocationId());
				}
				MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
				materialBox.setIsOnShelf(true);
				materialBox.update();
				SampleTaskItemRedisDAO.updateSampleTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null);
			}
			samTaskHandler.clearTask(Integer.valueOf(groupid.split("#")[1]));
		} else { // missiongroupid 不包含“:”表示为建仓任务
			for (AGVBuildTaskItem item2 : BuildTaskItemDAO.getBuildTaskItems()) {
				if (item2.getGroupId().equals(groupid) && item2.getState() > BuildTaskItemState.WAIT_MOVE) {
					// 清除掉对应的建仓任务条目
					BuildTaskItemDAO.removeBuildTaskItemByBoxId(item2.getBoxId().intValue());

					break;
				}
				BuildHandler.clearTil(item2.getSrcPosition());
			}
		}

	}

}
