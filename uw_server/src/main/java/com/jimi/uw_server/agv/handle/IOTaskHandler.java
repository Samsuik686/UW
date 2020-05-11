package com.jimi.uw_server.agv.handle;

import java.util.Date;

import com.jfinal.aop.Aop;
import com.jfinal.json.Json;
import com.jimi.uw_server.agv.dao.EfficiencyRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.entity.bo.base.BaseTaskItem;
import com.jimi.uw_server.agv.entity.cmd.AGVMoveCmd;
import com.jimi.uw_server.agv.entity.cmd.AGVStatusCmd;
import com.jimi.uw_server.agv.handle.base.BaseTaskHandler;
import com.jimi.uw_server.agv.socket.AGVMainSocket;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.ur.entity.ForkliftReachPackage;
import com.jimi.uw_server.ur.handler.assist.PackSender;
import com.jimi.uw_server.service.ExternalWhLogService;
import com.jimi.uw_server.service.IOTaskService;


/**
 * 出入库LS、SL命令处理器
 * <br>
 * <b>2018年7月10日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */

public class IOTaskHandler extends BaseTaskHandler {

	private static IOTaskService taskService = Aop.get(IOTaskService.class);

	private static MaterialService materialService = Aop.get(MaterialService.class);


	public final static String UNDEFINED = "undefined";
	
	public final static Integer UW_ID = 0;
	
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
			if (TaskItemRedisDAO.getLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId()) != null && !TaskItemRedisDAO.getLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId()).equals(0)) {
				return;
			}
			AGVMoveCmd moveCmd = createSendLLCmd(agvioTaskItem.getGroupId(), materialBox, goodsLocation, priority);
			// 发送取货LL>>>
			AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
			materialBox.setIsOnShelf(false).update();
			TaskItemRedisDAO.updateIOTaskItemInfo(agvioTaskItem, TaskItemState.ASSIGNED, goodsLocation.getWindowId(), goodsLocation.getId(), null, null, null, null, goodsLocation.getWindowId(), null, null);
			TaskItemRedisDAO.setLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId(), 1);
		}
	}


	@Override
	public void sendBackLL(BaseTaskItem item, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority) throws Exception {
		AGVIOTaskItem agvioTaskItem = (AGVIOTaskItem) item;
		synchronized (Lock.TASK_REDIS_LOCK) {
			// 发送回库LL>>>
			AGVMoveCmd moveCmd = createBackLLCmd(agvioTaskItem.getGroupId(), materialBox, goodsLocation, priority);
			AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
			TaskItemRedisDAO.updateIOTaskItemInfo(agvioTaskItem, TaskItemState.START_BACK, null, null, null, null, null, null, null, null, null);
		}
	}


	@Override
	protected void handleStartStatus(AGVStatusCmd statusCmd) {
		String missionGroupId = statusCmd.getMissiongroupid();
		// missiongroupid 包含“:”表示为出入库任务
		String groupid = missionGroupId.split("_")[0];
		// 匹配groupid
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {
			if (groupid.equals(item.getGroupId())) {
				// 更新tsakitems里对应item的robotid
				TaskItemRedisDAO.updateIOTaskItemInfo(item, null, null, null, null, statusCmd.getRobotid(), null, null, null, null, null);
			}
		}
	}


	@Override
	protected void handleProcessStatus(AGVStatusCmd statusCmd) throws Exception {
		String missionGroupId = statusCmd.getMissiongroupid();
		String groupid = missionGroupId.split("_")[0];
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {

			if (item.getGroupId().equals(groupid.trim()) && item.getState() == TaskItemState.ASSIGNED && missionGroupId.contains("S")) {// 判断是取料盒并且叉到料盒
				// 更改taskitems里对应item状态为2（已拣料到站）***
				TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.SEND_BOX, null, null, null, null, null, null, null, null, null);
				break;
			} else if (item.getState() == TaskItemState.START_BACK && item.getBoxId().equals(Integer.valueOf(groupid.split(":")[1])) && missionGroupId.contains("B")) {// 判断是回库并且叉到料盒
				// 更改taskitems里对应item状态为4（已回库完成）***
				TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.BACK_BOX, null, null, null, null, null, null, null, null, null);
				TaskItemRedisDAO.setLocationStatus(item.getWindowId(), item.getGoodsLocationId(), 0);
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
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {
			// 判断是LS指令还是SL指令第二动作完成，状态是1说明是LS，状态2是SL
			if (groupid.equals(item.getGroupId()) && item.getState() == TaskItemState.SEND_BOX && missionGroupId.contains("S")) {// LS执行完成时
				// 更改taskitems里对应item状态为2（已拣料到站）***
				Window window = Window.dao.findById(item.getWindowId());
				if (window.getAuto()) {
					synchronized (Lock.IO_TASK_REDIS_LOCK) {
						//发送到站包
						taskService.putUrOutTaskMaterialInfoToRedis(item, task);
						ForkliftReachPackage pack = new ForkliftReachPackage(item.getTaskId(), item.getBoxId());
						PackSender.sendForkliftReachPackage("robot1", pack);
						TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.ARRIVED_WINDOW, null, null, null, null, null, null, null, item.getUwQuantity(), item.getDeductionQuantity());
					}
				}else {
					TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.ARRIVED_WINDOW, null, null, null, null, null, null, null, null, null);
					EfficiencyRedisDAO.putTaskBoxArrivedTime(item.getTaskId(), item.getBoxId(), new Date().getTime());
				}
				break;
			} else if (item.getState() == TaskItemState.BACK_BOX && item.getBoxId().equals(Integer.valueOf(groupid.split(":")[1])) && missionGroupId.contains("B")) {// SL执行完成时：
				// 更改taskitems里对应item状态为4（已回库完成）***
				if (item.getIsForceFinish().equals(false) && task.getType().equals(TaskType.OUT)) {
					Integer remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(item.getMaterialTypeId());

					if (remainderQuantity <= 0) {
						item.setState(TaskItemState.LACK);
						item.setIsForceFinish(true);
						TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.LACK, null, null, null, null, true, null, null, null, null);
					} else {
						TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null, null, null, null, null, null);
					}
					
				} else {
					TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null, null, null, null, null, null);
				}

				// 设置料盒在架
				MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
				materialBox.setIsOnShelf(true).update();

				if (item.getIsCut()) {
					TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_CUT, null, null, null, null, null, false, null, null, null);
				}
				nextRound(item);
				EfficiencyRedisDAO.removeTaskBoxArrivedTimeByTaskAndBox(item.getTaskId(), item.getBoxId());
				clearTask(task.getId());
			}

		}
	}


	private void nextRound(AGVIOTaskItem item) {
		// 获取任务类型
		Integer taskType = Task.dao.findById(item.getTaskId()).getType();
		// 判断实际出入库数量是否不满足计划数
		if (!item.getIsForceFinish()) {
			// 如果是出库任务，若实际出库数量小于计划出库数量，则将任务条目状态回滚到未分配状态
			if (taskType == TaskType.OUT) {
				TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.WAIT_ASSIGN, 0, 0, 0, 0, null, null, null, null, null);
			} else { // 如果是入库或退料入库任务，若实际入库或退料入库数量小于计划入库或退料入库数量，则将任务条目状态回滚到等待扫码状态
				TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.WAIT_SCAN, 0, 0, 0, 0, null, null, null, null, null);
			}
		}
	}


	/**
	 * 判断该groupid所在的任务是否全部条目状态为"已回库完成"并且没有需要截料返库的，也如果是，
	 * 则清除所有该任务id对应的条目，释放内存，并修改数据库任务状态***
	*/
	public void clearTask(Integer taskId) {
		boolean isAllFinish = true;
		boolean isLack = false;
		Task task = Task.dao.findById(taskId);
		if (task != null) {
			if (!task.getState().equals(TaskState.CANCELED)) {
				for (AGVIOTaskItem item1 : TaskItemRedisDAO.getIOTaskItems(taskId)) {
					if (item1.getState() == TaskItemState.LACK) {
						isLack = true;
					}
					if ((item1.getState() != TaskItemState.FINISH_BACK && item1.getState() != TaskItemState.LACK && !item1.getIsForceFinish())) {
						isAllFinish = false;
					}
					if (item1.getState() == TaskItemState.FINISH_CUT || item1.getIsCut().equals(true)) {
						isAllFinish = false;
					}
					if (item1.getIsForceFinish() && item1.getState() >= 0 && item1.getState() <= 5) {
						isAllFinish = false;
					}
				}
			} else {
				for (AGVIOTaskItem item1 : TaskItemRedisDAO.getIOTaskItems(taskId)) {
					if (item1.getState() == TaskItemState.LACK) {
						isLack = true;
					}
					if (item1.getState() != TaskItemState.FINISH_BACK && item1.getState() != TaskItemState.LACK && !item1.getIsForceFinish() && item1.getState() != TaskItemState.WAIT_SCAN) {
						isAllFinish = false;
					}
					if (item1.getState() == TaskItemState.FINISH_CUT || item1.getIsCut().equals(true)) {
						isAllFinish = false;
					}
					if (item1.getIsForceFinish() && item1.getState() >= 0 && item1.getState() <= 5) {
						isAllFinish = false;
					}
				}
			}

		}

		if (isAllFinish) {

			taskService.finishRegualrTask(taskId, isLack);
			EfficiencyRedisDAO.removeTaskBoxArrivedTimeByTask(taskId);
			EfficiencyRedisDAO.removeTaskLastOperationUserByTask(taskId);
			EfficiencyRedisDAO.removeTaskStartTimeByTask(taskId);
			EfficiencyRedisDAO.removeTaskLastOperationTimeByTask(taskId);
			TaskItemRedisDAO.removeTaskItemByTaskId(taskId);
			TaskItemRedisDAO.delTaskStatus(taskId);
		}
	}

}
