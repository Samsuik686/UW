package com.jimi.uw_server.agv.handle;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jfinal.json.Json;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVMissionGroup;
import com.jimi.uw_server.agv.entity.cmd.AGVMoveCmd;
import com.jimi.uw_server.agv.entity.cmd.AGVStatusCmd;
import com.jimi.uw_server.agv.socket.AGVMainSocket;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.service.TaskService;

/**
 * LS、SL命令处理器
 * <br>
 * <b>2018年7月10日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class LSSLHandler {

	private static TaskService taskService = Enhancer.enhance(TaskService.class);

	private static MaterialService materialService = Enhancer.enhance(MaterialService.class);


	public static void sendSL(AGVIOTaskItem item, MaterialBox materialBox) throws Exception {
		//构建SL指令，令指定robot把料送回原仓位
		AGVMoveCmd moveCmd = createSLCmd(materialBox, item);
		//发送SL>>>
		AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
		//更新任务条目状态为已分配回库***
//		TaskItemRedisDAO.updateTaskItemState(item, 3);
	}


	public static void sendLS(AGVIOTaskItem item, MaterialBox materialBox) throws Exception {
		//发送LS>>>
		AGVMoveCmd cmd = createLSCmd(materialBox, item);
		AGVMainSocket.sendMessage(Json.getJson().toJson(cmd));

		//在数据库标记所有处于该坐标的料盒为不在架***
		setMaterialBoxIsOnShelf(materialBox, false);

		//更新任务条目状态为已分配***
		TaskItemRedisDAO.updateTaskItemState(item, 1);
	}


	/**
	 * 处理Status指令
	 */
	public static void handleStatus(String message) throws Exception {
		//转换成实体类
		AGVStatusCmd statusCmd = Json.getJson().parse(message, AGVStatusCmd.class);

		//判断是否是开始执行任务
		if(statusCmd.getStatus() == 0) {
			handleStatus0(statusCmd);
		}

		//判断是否是第二动作完成
		if(statusCmd.getStatus() == 2) {
			handleStatus2(statusCmd);
		}
	}


	private static void handleStatus0(AGVStatusCmd statusCmd) {
		//获取groupid
		String groupid = statusCmd.getMissiongroupid();
		
		//匹配groupid
		for (AGVIOTaskItem item : TaskItemRedisDAO.getTaskItems()) {
			if(groupid.equals(item.getGroupId())) {
				//更新tsakitems里对应item的robotid
				TaskItemRedisDAO.updateTaskItemRobot(item, statusCmd.getRobotid());
			}
		}
	}


	private static void handleStatus2(AGVStatusCmd statusCmd) throws Exception {
		//获取groupid
		String groupid = statusCmd.getMissiongroupid();
		
		//匹配groupid
		for (AGVIOTaskItem item : TaskItemRedisDAO.getTaskItems()) {
			if(groupid.equals(item.getGroupId())) {
				
				//判断是LS指令还是SL指令第二动作完成，状态是1说明是LS，状态2是SL
				if(item.getState() == 1) {//LS执行完成时
					//更改taskitems里对应item状态为2（已拣料到站）***
					TaskItemRedisDAO.updateTaskItemState(item, 2);

				}else if(item.getState() == 3) {//SL执行完成时：
					//更改taskitems里对应item状态为4（已回库完成）***
					TaskItemRedisDAO.updateTaskItemState(item, 4);

					// 设置料盒在架
					MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
					setMaterialBoxIsOnShelf(materialBox, true);

					nextRound(item);

					clearTil(groupid);
				}
			}
		}
	}


	private static void nextRound(AGVIOTaskItem item) {
		// 如果是出库任务，若计划出库数量小于实际出库数量，则将任务条目状态回滚到0
		if (Task.dao.findById(item.getTaskId()).getType() == 1) {
			if (!item.getIsForceFinish()) {
				TaskItemRedisDAO.updateTaskItemState(item, 0);
				TaskItemRedisDAO.updateTaskItemRobot(item, 0);
				TaskItemRedisDAO.updateTaskItemBoxId(item, 0);
			}
		}
	}


	/**
	 * 判断该groupid所在的任务是否全部条目状态为4（已回库完成），如果是，
	 * 则清除所有该任务id对应的条目，释放内存，并修改数据库任务状态***
	*/
	private static void clearTil(String groupid) {
		boolean isAllFinish = true;
		for (AGVIOTaskItem item1 : TaskItemRedisDAO.getTaskItems()) {
			if(groupid.split(":")[1].equals(item1.getGroupId().split(":")[1]) && item1.getState() != 4) {
				isAllFinish = false;
			}
		}
		if(isAllFinish) {
			int taskId = Integer.valueOf(groupid.split(":")[1]);
			TaskItemRedisDAO.removeTaskItemByTaskId(taskId);
			taskService.finish(taskId);
		}
	}


	private static AGVMoveCmd createSLCmd(MaterialBox materialBox, AGVIOTaskItem item) {
		List<AGVMissionGroup> groups = new ArrayList<>();
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getGroupId());//missionGroupId要和LS指令相同
		group.setRobotid(item.getRobotId());//robotId要和LS指令相同
		int windowId = Task.dao.findById(item.getTaskId()).getWindow();
		Window window = Window.dao.findById(windowId);
		group.setStartx(window.getRow());//起点X为仓口X
		group.setStarty(window.getCol());//起点Y为仓口Y
		group.setEndx(materialBox.getRow());//设置X
		group.setEndy(materialBox.getCol());//设置Y
		group.setEndz(materialBox.getHeight());//设置Z
		groups.add(group);
		AGVMoveCmd moveCmd = new AGVMoveCmd();
		moveCmd.setCmdcode("SL");
		moveCmd.setCmdid(TaskItemRedisDAO.getCmdId());
		moveCmd.setMissiongroups(groups);
		return moveCmd;
	}


	private static AGVMoveCmd createLSCmd(MaterialBox materialBox, AGVIOTaskItem item) {
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getGroupId());
		group.setRobotid(0);//让AGV系统自动分配
		group.setStartx(materialBox.getRow());//物料Row
		group.setStarty(materialBox.getCol());//物料Col
		group.setStartz(materialBox.getHeight());//物料Height
		int windowId = Task.dao.findById(item.getTaskId()).getWindow();
		Window window = Window.dao.findById(windowId);
		group.setEndx(window.getRow());//终点X为仓口X
		group.setEndy(window.getCol());//终点Y为仓口Y
		List<AGVMissionGroup> groups = new ArrayList<>();
		groups.add(group);
		AGVMoveCmd cmd = new AGVMoveCmd();
		cmd.setCmdcode("LS");
		cmd.setCmdid(TaskItemRedisDAO.getCmdId());
		cmd.setMissiongroups(groups);
		return cmd;
	}


	private static void setMaterialBoxIsOnShelf(MaterialBox materialBox, boolean isOnShelf) {
		List<MaterialBox> specifiedPositionMaterialBoxes = materialService.listByXYZ(materialBox.getRow(), materialBox.getCol(), materialBox.getHeight());
		for (MaterialBox mb: specifiedPositionMaterialBoxes) {
			mb.setIsOnShelf(isOnShelf);
			materialService.updateBox(mb);
		}
	}

}
