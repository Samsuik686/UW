package com.jimi.uw_server.agv.gaitek.handle;

import java.util.Date;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jfinal.json.Json;
import com.jimi.uw_server.agv.gaitek.dao.EfficiencyRedisDAO;
import com.jimi.uw_server.agv.gaitek.dao.IOTaskItemRedisDAO;
import com.jimi.uw_server.agv.gaitek.dao.TaskPropertyRedisDAO;
import com.jimi.uw_server.agv.gaitek.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.gaitek.entity.bo.base.BaseTaskItem;
import com.jimi.uw_server.agv.gaitek.entity.cmd.AGVMoveCmd;
import com.jimi.uw_server.agv.gaitek.entity.cmd.AGVStatusCmd;
import com.jimi.uw_server.agv.gaitek.handle.base.BaseTaskHandler;
import com.jimi.uw_server.agv.gaitek.socket.AGVMainSocket;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.service.io.RegularIOTaskService;
import com.jimi.uw_server.ur.entity.ForkliftReachPackage;
import com.jimi.uw_server.ur.handler.assist.PackSender;


/**
 * 出入库LS、SL命令处理器 <br>
 * <b>2018年7月10日</b>
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */

public class IOTaskHandler extends BaseTaskHandler {

	private static RegularIOTaskService taskService = Aop.get(RegularIOTaskService.class);

	private static MaterialService materialService = Aop.get(MaterialService.class);

	public final static String UNDEFINED = "undefined";

	private volatile static IOTaskHandler me;


	private IOTaskHandler() {
	}


	public static IOTaskHandler getInstance() {
		if (me == null) {
			synchronized (InvTaskHandler.class) {
				if (me == null) {
					me = new IOTaskHandler();
				}
			}
		}
		return me;
	}


	@Override
	public void sendSendLL(BaseTaskItem item, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority) throws Exception {
		AGVIOTaskItem agvioTaskItem = (AGVIOTaskItem) item;
		synchronized (Lock.TASK_REDIS_LOCK) {
			// 构建SL指令，令指定robot把料送回原仓位
			if (TaskPropertyRedisDAO.getLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId()) != null
					&& !TaskPropertyRedisDAO.getLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId()).equals(0)) {
				return;
			}
			AGVMoveCmd moveCmd = createSendLLCmd(agvioTaskItem.getGroupId(), materialBox, goodsLocation, priority);
			// 发送取货LL>>>
			AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
			materialBox.setIsOnShelf(false).update();
			IOTaskItemRedisDAO.updateIOTaskItemInfo(agvioTaskItem, TaskItemState.ASSIGNED, goodsLocation.getWindowId(), goodsLocation.getId(), null, null, null, null, goodsLocation.getWindowId(),
					null, null);
			TaskPropertyRedisDAO.setLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId(), 1);
		}
	}


	@Override
	public void sendBackLL(BaseTaskItem item, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority) throws Exception {
		AGVIOTaskItem agvioTaskItem = (AGVIOTaskItem) item;
		synchronized (Lock.TASK_REDIS_LOCK) {
			// 发送回库LL>>>
			AGVMoveCmd moveCmd = createBackLLCmd(agvioTaskItem.getGroupId(), materialBox, goodsLocation, priority);
			AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
			IOTaskItemRedisDAO.updateIOTaskItemInfo(agvioTaskItem, TaskItemState.START_BACK, null, null, null, null, null, null, null, null, null);
		}
	}


	@Override
	protected void handleStartStatus(AGVStatusCmd statusCmd) {
		String missionGroupId = statusCmd.getMissiongroupid();
		// missiongroupid 包含“:”表示为出入库任务
		String groupid = missionGroupId.split("_")[0];
		// 匹配groupid
		AGVIOTaskItem item = IOTaskItemRedisDAO.getIOTaskItem(Integer.valueOf(groupid.split(":")[2]), Integer.valueOf(groupid.split(":")[0]));
		if (item == null) {
			return;
		}
		IOTaskItemRedisDAO.updateIOTaskItemInfo(item, null, null, null, null, statusCmd.getRobotid(), null, null, null, null, null);

	}


	@Override
	protected void handleProcessStatus(AGVStatusCmd statusCmd) throws Exception {
		String missionGroupId = statusCmd.getMissiongroupid();
		String groupid = missionGroupId.split("_")[0];
		if (missionGroupId.contains("S")) {
			AGVIOTaskItem item = IOTaskItemRedisDAO.getIOTaskItem(Integer.valueOf(groupid.split(":")[2]), Integer.valueOf(groupid.split(":")[0]));
			if (item == null || item.getState() != TaskItemState.ASSIGNED) {
				return;
			}
			IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.SEND_BOX, null, null, null, null, null, null, null, null, null);
		} else if (missionGroupId.contains("B")) {
			for (AGVIOTaskItem item : IOTaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {
				if (item.getState() == TaskItemState.START_BACK && item.getBoxId().equals(Integer.valueOf(groupid.split(":")[1]))) {// 判断是回库并且叉到料盒
					// 更改taskitems里对应item状态为4（已回库完成）***
					IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.BACK_BOX, null, null, null, null, null, null, null, null, null);
					TaskPropertyRedisDAO.setLocationStatus(item.getWindowId(), item.getGoodsLocationId(), 0);
				}
			}
		}
	}


	@Override
	protected void handleFinishStatus(AGVStatusCmd statusCmd) throws Exception {
		// 获取groupid
		String missionGroupId = statusCmd.getMissiongroupid();
		// missiongroupid 包含“:”表示为出入库任务
		String groupid = missionGroupId.split("_")[0];
		Task task = Task.dao.findById(Integer.valueOf(groupid.split(":")[2]));
		// 匹配groupid
		if (missionGroupId.contains("S")) {
			AGVIOTaskItem item = IOTaskItemRedisDAO.getIOTaskItem(Integer.valueOf(groupid.split(":")[2]), Integer.valueOf(groupid.split(":")[0]));
			if (item == null || item.getState() != TaskItemState.SEND_BOX) {
				return;
			}
			Window window = Window.dao.findById(item.getWindowId());
			if (window.getAuto()) {
				synchronized (Lock.IO_TASK_REDIS_LOCK) {
					// 发送到站包
					taskService.putUrOutTaskMaterialInfoToRedis(item, task);
					ForkliftReachPackage pack = new ForkliftReachPackage(item.getTaskId(), item.getBoxId());
					PackSender.sendForkliftReachPackage("robot1", pack);
					IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.ARRIVED_WINDOW, null, null, null, null, null, null, null, item.getUwQuantity(), item.getDeductionQuantity());

				}
			} else {
				EfficiencyRedisDAO.putTaskBoxArrivedTime(item.getTaskId(), item.getBoxId(), new Date().getTime());
				IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.ARRIVED_WINDOW, null, null, null, null, null, null, null, item.getUwQuantity(), item.getDeductionQuantity());
			}
		} else if (missionGroupId.contains("B")) {
			for (AGVIOTaskItem item : IOTaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {
				if (item.getState() == TaskItemState.BACK_BOX && item.getBoxId().equals(Integer.valueOf(groupid.split(":")[1]))) {// 判断是回库并且叉到料盒
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
					materialBox.setIsOnShelf(true).update();
					if (item.getIsCut()) {
						IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_CUT, null, null, null, null, null, false, null, null, null);
					}
					nextRound(task, item);
					EfficiencyRedisDAO.removeTaskBoxArrivedTimeByTaskAndBox(item.getTaskId(), item.getBoxId());
					clearTask(task.getId(), false);
				}
			}
		}
	}


	private void nextRound(Task task, AGVIOTaskItem item) {
		// 获取任务类型
		Integer taskType = Task.dao.findById(item.getTaskId()).getType();
		// 判断实际出入库数量是否不满足计划数
		if (task.getState() != TaskState.CANCELED) {
			if (!item.getIsForceFinish()) {
				// 如果是出库任务，若实际出库数量小于计划出库数量，则将任务条目状态回滚到未分配状态
				if (taskType == TaskType.OUT) {
					IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.WAIT_ASSIGN, 0, 0, 0, 0, null, null, null, null, null);
				} else { // 如果是入库或退料入库任务，若实际入库或退料入库数量小于计划入库或退料入库数量，则将任务条目状态回滚到等待扫码状态
					IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.WAIT_SCAN, 0, 0, 0, 0, null, null, null, null, null);
				}
			}
		}

	}


	/**
	 * 判断该groupid所在的任务是否全部条目状态为"已回库完成"并且没有需要截料返库的，也如果是，
	 * 则清除所有该任务id对应的条目，释放内存，并修改数据库任务状态***
	 */
	public void clearTask(Integer taskId, Boolean isCanceled) {
		boolean isAllFinish = true;
		Task task = Task.dao.findById(taskId);
		List<AGVIOTaskItem> items = IOTaskItemRedisDAO.getIOTaskItems(taskId);
		if (task != null) {
			isAllFinish = checkIOTaskFinish(task, items, isCanceled);
		}
		if (isAllFinish) {
			if (task.getState() == TaskState.CANCELED || isCanceled) {
				taskService.cancelRegualrTask(taskId);
			} else {
				taskService.finish(taskId);
			}
			EfficiencyRedisDAO.removeTaskBoxArrivedTimeByTask(taskId);
			EfficiencyRedisDAO.removeTaskLastOperationUserByTask(taskId);
			EfficiencyRedisDAO.removeTaskStartTimeByTask(taskId);
			EfficiencyRedisDAO.removeTaskLastOperationTimeByTask(taskId);
			IOTaskItemRedisDAO.removeTaskItemByTaskId(taskId);
			TaskPropertyRedisDAO.delTaskStatus(taskId);
		}
	}


	private boolean checkIOTaskFinish(Task task, List<AGVIOTaskItem> items, Boolean isCanceled) {

		if (task.getState() == TaskState.CANCELED || isCanceled) {
			for (AGVIOTaskItem item : items) {
				if (item.getState() != TaskItemState.FINISH_BACK && item.getState() != TaskItemState.LACK && item.getState() != TaskItemState.WAIT_SCAN) {
					return false;
				}
				if (item.getIsCut()) {
					return false;
				}
			}
		} else {
			for (AGVIOTaskItem item : items) {
				if (item.getState() != TaskItemState.FINISH_BACK && item.getState() != TaskItemState.LACK && !item.getIsForceFinish()) {
					return false;
				}
				if (item.getState() == TaskItemState.FINISH_CUT || item.getIsCut().equals(true)) {
					return false;
				}
				if (item.getIsForceFinish() && item.getState() >= 0 && item.getState() <= 5) {
					return false;
				}
			}
		}
		return true;
	}
}
