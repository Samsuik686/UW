package com.jimi.uw_server.agv.handle;

import com.jfinal.json.Json;
import com.jfinal.plugin.activerecord.Db;
import com.jimi.uw_server.agv.dao.InvTaskRedisDAO;
import com.jimi.uw_server.agv.dao.TaskUtilsRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.agv.entity.bo.base.BaseTaskItem;
import com.jimi.uw_server.agv.entity.cmd.AGVMoveCmd;
import com.jimi.uw_server.agv.entity.cmd.AGVStatusCmd;
import com.jimi.uw_server.agv.handle.base.BaseTaskHandler;
import com.jimi.uw_server.agv.socket.AGVMainSocket;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.enums.UwInvTaskProcessFlagEnum;
import com.jimi.uw_server.constant.sql.InventoryTaskSQL;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.ur.dao.UrInvTaskBoxInfoDAO;
import com.jimi.uw_server.ur.entity.ForkliftReachPackage;
import com.jimi.uw_server.ur.entity.SessionBox;
import com.jimi.uw_server.ur.handler.assist.PackSender;
import com.jimi.uw_server.ur.processor.ProcessorExecutor;

import java.util.List;


public class InvTaskHandler extends BaseTaskHandler {

	private static String GET_WINDOW_BY_TASK_ID = "select * from window where bind_task_id = ?";

	private volatile static InvTaskHandler me;


	private InvTaskHandler() {
	}


	public static InvTaskHandler getInstance() {
		if (me == null) {
			synchronized (InvTaskHandler.class) {
				if (me == null) {
					me = new InvTaskHandler();
				}
			}
		}
		return me;
	}


	@Override
	public void sendSendLL(BaseTaskItem item, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority) throws Exception {
		AGVInventoryTaskItem agvInventoryTaskItem = (AGVInventoryTaskItem) item;
		synchronized (Lock.TASK_REDIS_LOCK) {
			// 构建SL指令，令指定robot把料送回原仓位
			if (TaskUtilsRedisDAO.getLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId()) != null && !TaskUtilsRedisDAO.getLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId()).equals(0)) {
				return;
			}
			AGVMoveCmd moveCmd = createSendLLCmd(agvInventoryTaskItem.getGroupId(), materialBox, goodsLocation, priority);
			// 发送取货LL>>>
			AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
			materialBox.setIsOnShelf(false).update();
			InvTaskRedisDAO.updateInventoryTaskItemInfo(agvInventoryTaskItem, TaskItemState.ASSIGNED, goodsLocation.getWindowId(), goodsLocation.getId(), null, null, null);
			TaskUtilsRedisDAO.setLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId(), 1);
		}
	}


	@Override
	public void sendBackLL(BaseTaskItem item, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority) throws Exception {
		AGVInventoryTaskItem agvInventoryTaskItem = (AGVInventoryTaskItem) item;
		synchronized (Lock.TASK_REDIS_LOCK) {
			// 发送回库LL>>>
			AGVMoveCmd moveCmd = createBackLLCmd(agvInventoryTaskItem.getGroupId(), materialBox, goodsLocation, priority);
			AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
			InvTaskRedisDAO.updateInventoryTaskItemInfo(agvInventoryTaskItem, TaskItemState.START_BACK, goodsLocation.getWindowId(), goodsLocation.getId(), null, null, null);
		}
	}


	@Override
	protected void handleStartStatus(AGVStatusCmd statusCmd) throws Exception {
		// 获取groupid
		String missionGroupId = statusCmd.getMissiongroupid();
		String groupid = missionGroupId.split("_")[0];
		// 匹配groupid
		for (AGVInventoryTaskItem item : InvTaskRedisDAO.getInventoryTaskItems(Integer.valueOf(groupid.split("@")[1]))) {
			if (groupid.equals(item.getGroupId())) {
				// 更新tsakitems里对应item的robotid
				InvTaskRedisDAO.updateInventoryTaskItemInfo(item, null, null, null, statusCmd.getRobotid(), null, null);
			}
		}
	}


	@Override
	protected void handleProcessStatus(AGVStatusCmd statusCmd) throws Exception {
		// 获取groupid
		String missionGroupId = statusCmd.getMissiongroupid();
		String groupid = missionGroupId.split("_")[0];
		for (AGVInventoryTaskItem item : InvTaskRedisDAO.getInventoryTaskItems(Integer.valueOf(groupid.split("@")[1]))) {
			// 判断是LS指令还是SL指令第二动作完成，状态是1说明是LS，状态2是SL
			if (item.getGroupId().equals(groupid.trim()) && item.getState() == TaskItemState.ASSIGNED && missionGroupId.contains("S")) {// 判断是取料盒并且叉到料盒
				// 更改taskitems里对应item状态为2（已拣料到站）***
				InvTaskRedisDAO.updateInventoryTaskItemInfo(item, TaskItemState.SEND_BOX, null, null, null, null, null);
				break;
			} else if (item.getGroupId().equals(groupid.trim()) && item.getState() == TaskItemState.START_BACK && missionGroupId.contains("B")) {// 判断是回库并且叉到料盒
				// 更改taskitems里对应item状态为4（已回库完成）***
				InvTaskRedisDAO.updateInventoryTaskItemInfo(item, TaskItemState.BACK_BOX, null, null, null, null, null);
				TaskUtilsRedisDAO.setLocationStatus(item.getWindowId(), item.getGoodsLocationId(), 0);
				break;
			}
		}
	}


	@Override
	protected void handleFinishStatus(AGVStatusCmd statusCmd) throws Exception {
		// 获取groupid
		String missionGroupId = statusCmd.getMissiongroupid();
		String groupid = missionGroupId.split("_")[0];
		Integer taskId = Integer.valueOf(groupid.split("@")[1]);
		// 匹配groupid
		for (AGVInventoryTaskItem item : InvTaskRedisDAO.getInventoryTaskItems(Integer.valueOf(groupid.split("@")[1]))) {
			if (groupid.trim().equals(item.getGroupId())) {

				// 判断是LS指令还是SL指令第二动作完成，状态是1说明是LS，状态2是SL
				if (item.getState() == TaskItemState.SEND_BOX && missionGroupId.contains("S")) {// LS执行完成时
					// 更改taskitems里对应item状态为2（已拣料到站）***
					InvTaskRedisDAO.updateInventoryTaskItemInfo(item, TaskItemState.ARRIVED_WINDOW, null, null, null, null, null);
					Window window = Window.dao.findById(item.getWindowId());
					int boxId = item.getBoxId();
					if (window.getAuto()) {
						Task task = Task.dao.findById(taskId);
						//发送ready包
						if (task != null) {
							Db.update(InventoryTaskSQL.UPDATE_AND_CLERAT_MATERIAL_POSITION, boxId);
							Runnable runnable = new Runnable() {

								@Override
								public void run() {
									ForkliftReachPackage pack = new ForkliftReachPackage(taskId, boxId, task.getType());
									Boolean flag = PackSender.sendPackage("robot1", pack);
									if (!flag) {
										UrInvTaskBoxInfoDAO.putUrTaskBoxArrivedPack("robot1", pack);
										SessionBox.remove("robot1");
									}
								}
							};
							ProcessorExecutor.me.execute(runnable);

						}
					}
					break;
				} else if (item.getState() == TaskItemState.BACK_BOX && missionGroupId.contains("B")) {// SL执行完成时：
					// 更改taskitems里对应item状态为4（已回库完成）***
					InvTaskRedisDAO.updateInventoryTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null, null);
					MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
					materialBox.setIsOnShelf(true);
					materialBox.update();
					if (UwInvTaskProcessFlagEnum.UW_EXCEPTION_START_PROCESS.isCodeEquals(InvTaskRedisDAO.getUwInvTaskProcess(taskId)) && item.getIsException()) {
						InvTaskRedisDAO.updateInventoryTaskItemInfo(item, TaskItemState.WAIT_ASSIGN, null, null, null, null, null);
					}else {
						clearTask(item.getTaskId());
					}
					
					break;
				}
			}
		}
	}


	public void clearTask(Integer taskId) {
		boolean backflag = true;
		boolean finishFlag = true;
		for (AGVInventoryTaskItem item : InvTaskRedisDAO.getInventoryTaskItems(taskId)) {
			if (!item.getState().equals(TaskItemState.FINISH_BACK)) {
				backflag = false;
				break;
			}
		}
		if (backflag) {
			for (AGVInventoryTaskItem item : InvTaskRedisDAO.getInventoryTaskItems(taskId)) {
				if (item.getIsException()) {
					finishFlag = false;
					break;
				}
			}
			
		}
		if (backflag && !finishFlag && !UwInvTaskProcessFlagEnum.UW_EXCEPTION_START_PROCESS.isCodeEquals(InvTaskRedisDAO.getUwInvTaskProcess(taskId))) {
			InvTaskRedisDAO.setUwInvTaskProcess(taskId, UwInvTaskProcessFlagEnum.UW_EXCEPTION_WAIT_PROCESS);
		}
		if (backflag && finishFlag) {
			InvTaskRedisDAO.removeInventoryTaskItemByTaskId(taskId);
			List<Window> windows = Window.dao.find(GET_WINDOW_BY_TASK_ID, taskId);
			// 将仓口解绑(作废任务时，如果还有任务条目没跑完就不会解绑仓口，因此不管任务状态是为进行中还是作废，这里都需要解绑仓口)
			synchronized (Lock.WINDOW_LOCK) {
				for (Window window : windows) {
					window.setBindTaskId(null).update();
				}
			}
			InvTaskRedisDAO.delUwInvTaskProcess(taskId);
			TaskUtilsRedisDAO.delTaskStatus(taskId);
		}
	}
}
