package com.jimi.uw_server.agv.handle;

import java.util.List;

import com.jfinal.json.Json;
import com.jimi.uw_server.agv.dao.SampleTaskRedisDAO;
import com.jimi.uw_server.agv.dao.TaskUtilsRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.agv.entity.bo.base.BaseTaskItem;
import com.jimi.uw_server.agv.entity.cmd.AGVMoveCmd;
import com.jimi.uw_server.agv.entity.cmd.AGVStatusCmd;
import com.jimi.uw_server.agv.handle.base.BaseTaskHandler;
import com.jimi.uw_server.agv.socket.AGVMainSocket;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.Window;


public class SampleTaskHandler extends BaseTaskHandler {

	private static String GET_WINDOW_BY_TASK_ID = "select * from window where bind_task_id = ?";

	private volatile static SampleTaskHandler me;


	private SampleTaskHandler() {
	}


	public static SampleTaskHandler getInstance() {
		if (me == null) {
			synchronized (InvTaskHandler.class) {
				if (me == null) {
					me = new SampleTaskHandler();
				}
			}
		}
		return me;
	}


	@Override
	public void sendSendLL(BaseTaskItem item, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority) throws Exception {
		AGVSampleTaskItem agvSampleTaskItem = (AGVSampleTaskItem) item;
		synchronized (Lock.TASK_REDIS_LOCK) {
			// 构建SL指令，令指定robot把料送回原仓位
			if (TaskUtilsRedisDAO.getLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId()) != null && !TaskUtilsRedisDAO.getLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId()).equals(0)) {
				return;
			}
			AGVMoveCmd moveCmd = createSendLLCmd(agvSampleTaskItem.getGroupId(), materialBox, goodsLocation, priority);
			// 发送取货LL>>>
			AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
			materialBox.setIsOnShelf(false).update();
			SampleTaskRedisDAO.updateSampleTaskItemInfo(agvSampleTaskItem, TaskItemState.ASSIGNED, goodsLocation.getWindowId(), goodsLocation.getId(), null, null);
			TaskUtilsRedisDAO.setLocationStatus(goodsLocation.getWindowId(), goodsLocation.getId(), 1);
		}
	}


	@Override
	public void sendBackLL(BaseTaskItem item, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority) throws Exception {
		AGVSampleTaskItem agvSampleTaskItem = (AGVSampleTaskItem) item;
		synchronized (Lock.TASK_REDIS_LOCK) {
			// 发送回库LL>>>
			AGVMoveCmd moveCmd = createBackLLCmd(agvSampleTaskItem.getGroupId(), materialBox, goodsLocation, priority);
			AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
			SampleTaskRedisDAO.updateSampleTaskItemInfo(agvSampleTaskItem, TaskItemState.START_BACK, null, null, null, null);
		}
	}


	@Override
	protected void handleStartStatus(AGVStatusCmd statusCmd) throws Exception {
		// 获取groupid
		String missionGroupId = statusCmd.getMissiongroupid();
		String groupid = missionGroupId.split("_")[0];
		// 匹配groupid
		for (AGVSampleTaskItem item : SampleTaskRedisDAO.getSampleTaskItems(Integer.valueOf(groupid.split("#")[1]))) {
			if (groupid.equals(item.getGroupId())) {
				// 更新tsakitems里对应item的robotid
				SampleTaskRedisDAO.updateSampleTaskItemInfo(item, null, null, null, statusCmd.getRobotid(), null);
			}
		}
	}


	@Override
	protected void handleProcessStatus(AGVStatusCmd statusCmd) throws Exception {
		String missionGroupId = statusCmd.getMissiongroupid();
		String groupid = missionGroupId.split("_")[0];
		for (AGVSampleTaskItem item : SampleTaskRedisDAO.getSampleTaskItems(Integer.valueOf(groupid.split("#")[1]))) {
			// 判断是LS指令还是SL指令第二动作完成，状态是1说明是LS，状态2是SL
			if (item.getGroupId().equals(groupid.trim()) && item.getState() == TaskItemState.ASSIGNED && missionGroupId.contains("S")) {// 判断是取料盒并且叉到料盒
				// 更改taskitems里对应item状态为2（已拣料到站）***
				SampleTaskRedisDAO.updateSampleTaskItemInfo(item, TaskItemState.SEND_BOX, null, null, null, null);
				break;
			} else if (item.getGroupId().equals(groupid.trim()) && item.getState() == TaskItemState.START_BACK && missionGroupId.contains("B")) {// 判断是回库并且叉到料盒
				// 更改taskitems里对应item状态为4（已回库完成）***
				SampleTaskRedisDAO.updateSampleTaskItemInfo(item, TaskItemState.BACK_BOX, null, null, null, null);
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
		// 匹配groupid
		for (AGVSampleTaskItem item : SampleTaskRedisDAO.getSampleTaskItems(Integer.valueOf(groupid.split("#")[1]))) {
			if (groupid.equals(item.getGroupId())) {

				// 判断是LS指令还是SL指令第二动作完成，状态是1说明是LS，状态2是SL
				if (item.getState() == TaskItemState.SEND_BOX) {// LS执行完成时
					// 更改taskitems里对应item状态为2（已拣料到站）***
					SampleTaskRedisDAO.updateSampleTaskItemInfo(item, TaskItemState.ARRIVED_WINDOW, null, null, null, null);
					break;
				} else if (item.getState() == TaskItemState.BACK_BOX) {// SL执行完成时：
					// 更改taskitems里对应item状态为4（已回库完成）***
					SampleTaskRedisDAO.updateSampleTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null);
					MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
					materialBox.setIsOnShelf(true);
					materialBox.update();
					clearTask(item.getTaskId());
					break;
				}
			}
		}

	}


	public void clearTask(Integer taskId) {
		boolean flag = true;
		for (AGVSampleTaskItem item : SampleTaskRedisDAO.getSampleTaskItems(taskId)) {
			if (!item.getState().equals(TaskItemState.FINISH_BACK)) {
				flag = false;
				break;
			}
		}
		if (flag) {
			SampleTaskRedisDAO.removeSampleTaskItemByTaskId(taskId);
			List<Window> windows = Window.dao.find(GET_WINDOW_BY_TASK_ID, taskId);
			// 将仓口解绑(作废任务时，如果还有任务条目没跑完就不会解绑仓口，因此不管任务状态是为进行中还是作废，这里都需要解绑仓口)
			synchronized (Lock.WINDOW_LOCK) {
				for (Window window : windows) {
					window.setBindTaskId(null).update();
				}
			}

			Task task = Task.dao.findById(taskId);
			if (!task.getState().equals(TaskState.CANCELED)) {
				task.setState(TaskState.FINISHED);
			}
			TaskUtilsRedisDAO.delTaskStatus(taskId);
			task.update();
		}
	}

}
