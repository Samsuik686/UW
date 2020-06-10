package com.jimi.uw_server.agv.gaitek.handle;

import com.jfinal.json.Json;
import com.jimi.uw_server.agv.gaitek.dao.InventoryTaskItemRedisDAO;
import com.jimi.uw_server.agv.gaitek.dao.TaskPropertyRedisDAO;
import com.jimi.uw_server.agv.gaitek.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.agv.gaitek.entity.bo.base.BaseTaskItem;
import com.jimi.uw_server.agv.gaitek.entity.cmd.AGVMoveCmd;
import com.jimi.uw_server.agv.gaitek.entity.cmd.AGVStatusCmd;
import com.jimi.uw_server.agv.gaitek.handle.base.BaseTaskHandler;
import com.jimi.uw_server.agv.gaitek.socket.AGVMainSocket;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.sql.SQL;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.ur.dao.UrTaskInfoDAO;
import com.jimi.uw_server.ur.entity.ForkliftReachPackage;
import com.jimi.uw_server.ur.entity.UrMaterialInfo;
import com.jimi.uw_server.ur.handler.assist.PackSender;

import java.util.ArrayList;
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
			if (TaskPropertyRedisDAO.getLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId()) != null
					&& !TaskPropertyRedisDAO.getLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId()).equals(0)) {
				return;
			}
			AGVMoveCmd moveCmd = createSendLLCmd(agvInventoryTaskItem.getGroupId(), materialBox, goodsLocation, priority);
			// 发送取货LL>>>
			AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
			materialBox.setIsOnShelf(false).update();
			InventoryTaskItemRedisDAO.updateInventoryTaskItemInfo(agvInventoryTaskItem, TaskItemState.ASSIGNED, goodsLocation.getWindowId(), goodsLocation.getId(), null, null);
			TaskPropertyRedisDAO.setLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId(), 1);
		}
	}


	@Override
	public void sendBackLL(BaseTaskItem item, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority) throws Exception {
		AGVInventoryTaskItem agvInventoryTaskItem = (AGVInventoryTaskItem) item;
		synchronized (Lock.TASK_REDIS_LOCK) {
			// 发送回库LL>>>
			AGVMoveCmd moveCmd = createBackLLCmd(agvInventoryTaskItem.getGroupId(), materialBox, goodsLocation, priority);
			AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
			InventoryTaskItemRedisDAO.updateInventoryTaskItemInfo(agvInventoryTaskItem, TaskItemState.START_BACK, goodsLocation.getWindowId(), goodsLocation.getId(), null, null);
		}
	}


	@Override
	protected void handleStartStatus(AGVStatusCmd statusCmd) throws Exception {
		// 获取groupid
		String missionGroupId = statusCmd.getMissiongroupid();
		String groupid = missionGroupId.split("_")[0];
		// 匹配groupid
		AGVInventoryTaskItem item = InventoryTaskItemRedisDAO.getInventoryTaskItem(Integer.valueOf(groupid.split("@")[1]), Integer.valueOf(groupid.split("@")[0]));
		// 更新tsakitems里对应item的robotid
		if (item == null) {
			return;
		}
		InventoryTaskItemRedisDAO.updateInventoryTaskItemInfo(item, null, null, null, statusCmd.getRobotid(), null);

	}


	@Override
	protected void handleProcessStatus(AGVStatusCmd statusCmd) throws Exception {
		// 获取groupid
		String missionGroupId = statusCmd.getMissiongroupid();
		String groupid = missionGroupId.split("_")[0];
		AGVInventoryTaskItem item = InventoryTaskItemRedisDAO.getInventoryTaskItem(Integer.valueOf(groupid.split("@")[1]), Integer.valueOf(groupid.split("@")[0]));
		if (item == null) {
			return;
		}
		if (missionGroupId.contains("S") && item.getState() == TaskItemState.ASSIGNED) {
			InventoryTaskItemRedisDAO.updateInventoryTaskItemInfo(item, TaskItemState.SEND_BOX, null, null, null, null);
		} else if (missionGroupId.contains("B") && item.getState() == TaskItemState.START_BACK) {
			InventoryTaskItemRedisDAO.updateInventoryTaskItemInfo(item, TaskItemState.BACK_BOX, null, null, null, null);
			TaskPropertyRedisDAO.setLocationStatus(item.getWindowId(), item.getGoodsLocationId(), 0);
		}
	}


	@Override
	protected void handleFinishStatus(AGVStatusCmd statusCmd) throws Exception {
		// 获取groupid
		String missionGroupId = statusCmd.getMissiongroupid();
		String groupid = missionGroupId.split("_")[0];
		Integer taskId = Integer.valueOf(groupid.split("@")[1]);
		Integer boxId = Integer.valueOf(groupid.split("@")[0]);
		// 匹配groupid
		AGVInventoryTaskItem item = InventoryTaskItemRedisDAO.getInventoryTaskItem(taskId, boxId);
		if (item == null) {
			return;
		}
		if (item.getState() == TaskItemState.SEND_BOX && missionGroupId.contains("S")) {// LS执行完成时
			InventoryTaskItemRedisDAO.updateInventoryTaskItemInfo(item, TaskItemState.ARRIVED_WINDOW, null, null, null, null);
			Window window = Window.dao.findById(item.getWindowId());
			if (window.getAuto()) {
				List<Material> materials = Material.dao.find(SQL.GET_MATERIAL_BY_BOX, boxId);
				if (!materials.isEmpty()) {
					List<UrMaterialInfo> urMaterialInfos = new ArrayList<UrMaterialInfo>();
					for (Material material : materials) {
						UrMaterialInfo urMaterialInfo = new UrMaterialInfo(material.getId(), material.getRow(), material.getCol(), taskId, item.getBoxId(), item.getWindowId(),
								item.getGoodsLocationId(), false, 0, material.getRemainderQuantity(), 0);
						urMaterialInfos.add(urMaterialInfo);
					}
					UrTaskInfoDAO.putUrMaterialInfos(taskId, boxId, urMaterialInfos);
				}
				// 发送ready包
				ForkliftReachPackage pack = new ForkliftReachPackage(item.getTaskId(), item.getBoxId());
				PackSender.sendForkliftReachPackage("robot1", pack);
			}
		} else if (item.getState() == TaskItemState.BACK_BOX && missionGroupId.contains("B")) {
			InventoryTaskItemRedisDAO.updateInventoryTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null);
			MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
			materialBox.setIsOnShelf(true);
			materialBox.update();
			clearTask(item.getTaskId());
		}
	}


	public void clearTask(Integer taskId) {
		boolean flag = true;
		for (AGVInventoryTaskItem item : InventoryTaskItemRedisDAO.getInventoryTaskItems(taskId)) {
			if (!item.getState().equals(TaskItemState.FINISH_BACK)) {
				flag = false;
				break;
			}
		}
		if (flag) {
			InventoryTaskItemRedisDAO.removeInventoryTaskItemByTaskId(taskId);
			List<Window> windows = Window.dao.find(GET_WINDOW_BY_TASK_ID, taskId);
			// 将仓口解绑(作废任务时，如果还有任务条目没跑完就不会解绑仓口，因此不管任务状态是为进行中还是作废，这里都需要解绑仓口)
			synchronized (Lock.WINDOW_LOCK) {
				for (Window window : windows) {
					window.setBindTaskId(null).update();
				}
			}

			TaskPropertyRedisDAO.delTaskStatus(taskId);
		}
	}
}
