package com.jimi.uw_server.agv.handle;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.json.Json;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVBuildTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVMissionGroup;
import com.jimi.uw_server.agv.entity.cmd.AGVMoveCmd;
import com.jimi.uw_server.agv.entity.cmd.AGVStatusCmd;
import com.jimi.uw_server.agv.socket.AGVMainSocket;
import com.jimi.uw_server.constant.BuildTaskItemState;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.model.MaterialBox;


/**
 * 建仓LL命令处理器
 * @author HardyYao
 * @createTime 2018年12月12日  下午5:52:16
 */

public class BuildHandler {

	public static void sendBuildCmd(AGVBuildTaskItem item, MaterialBox materialBox) throws Exception {
		// 构建LL指令，令指定robot把料盒入仓
		AGVMoveCmd moveCmd = createLLCmd(materialBox, item);
		// 发送LL>>>
		AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
		// 更新任务条目状态为1
		TaskItemRedisDAO.updateBuildTaskItemState(item, TaskItemState.ASSIGNED);
	}


	private static AGVMoveCmd createLLCmd(MaterialBox materialBox, AGVBuildTaskItem item) {
		List<AGVMissionGroup> groups = new ArrayList<>();
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getGroupId()); // missionGroupId要和LL指令相同
		group.setRobotid(item.getRobotId()); // robotId要和LL指令相同
		Integer srcX = Integer.parseInt(item.getSrcPosition().split(":")[0]);
		Integer srcY = Integer.parseInt(item.getSrcPosition().split(":")[1]);
		Integer srcZ = Integer.parseInt(item.getSrcPosition().split(":")[2]);
		group.setStartx(srcX); // 起点X
		group.setStarty(srcY); // 起点Y
		group.setStartz(srcZ); // 起点Z
		group.setEndx(materialBox.getRow()); // 终点X
		group.setEndy(materialBox.getCol()); // 终点Y
		group.setEndz(materialBox.getHeight()); // 终点Z
		groups.add(group);
		AGVMoveCmd moveCmd = new AGVMoveCmd();
		moveCmd.setCmdcode("LL");
		moveCmd.setCmdid(TaskItemRedisDAO.getCmdId());
		moveCmd.setMissiongroups(groups);
		return moveCmd;
	}


	/**
	 * 处理Status指令
	 */
	public static void handleStatus(String message) throws Exception {
		// 转换成实体类
		AGVStatusCmd statusCmd = Json.getJson().parse(message, AGVStatusCmd.class);

		// missiongroupid 不包含“:”表示为建仓任务
		if (!statusCmd.getMissiongroupid().contains(":")) {
			// 判断是否是开始执行任务
			if (statusCmd.getStatus() == BuildTaskItemState.WAIT_MOVE) {
				handleStatus0(statusCmd);
			}

			// 判断是否是完成了一个建仓任务
			if (statusCmd.getStatus() == BuildTaskItemState.FINISH_ONE_BUILD_TASK) {
				handleStatus2(statusCmd);
			}
		}
	}


	private static void handleStatus0(AGVStatusCmd statusCmd) {
		// 获取groupid
		String groupid = statusCmd.getMissiongroupid();

		// 匹配groupid
		for (AGVBuildTaskItem item : TaskItemRedisDAO.getBuildTaskItems()) {
			if (groupid.equals(item.getGroupId())) {
				// 更新buildTsakitems里对应item的robotid
				TaskItemRedisDAO.updateBuildTaskItemRobot(item, statusCmd.getRobotid());
			}
		}
	}


	private static void handleStatus2(AGVStatusCmd statusCmd) throws Exception {
		// 获取groupid
		String groupid = statusCmd.getMissiongroupid();

		// 匹配groupid
		for (AGVBuildTaskItem item : TaskItemRedisDAO.getBuildTaskItems()) {
			if (groupid.equals(item.getGroupId())) {
				// 更改taskitems里对应item状态为2（已入仓完成）***
				TaskItemRedisDAO.updateBuildTaskItemState(item, BuildTaskItemState.FINISH_ONE_BUILD_TASK);

				// 设置料盒在架
				MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
				materialBox.setIsOnShelf(true).update();

				clearTil(item.getSrcPosition());
			}
		}
	}


	/**
	 * 判断该srcPosition所在的任务是否全部条目状态为"已完成"
	 * 如果是，则清除所有该任务id对应的条目，释放内存***
	*/
	public static void clearTil(String srcPosition) {
		boolean isAllFinish = true;
		for (AGVBuildTaskItem item1 : TaskItemRedisDAO.getBuildTaskItems()) {
			if (srcPosition.equals(item1.getSrcPosition()) && item1.getState() != BuildTaskItemState.FINISH_ONE_BUILD_TASK) {
				isAllFinish = false;
			}
		}
		if (isAllFinish) {
			TaskItemRedisDAO.removeBuildTaskItemBySrcPosition(srcPosition);
		}
	}

}
