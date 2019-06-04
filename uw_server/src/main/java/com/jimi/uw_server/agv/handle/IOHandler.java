package com.jimi.uw_server.agv.handle;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jfinal.json.Json;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVMissionGroup;
import com.jimi.uw_server.agv.entity.cmd.AGVMoveCmd;
import com.jimi.uw_server.agv.entity.cmd.AGVStatusCmd;
import com.jimi.uw_server.agv.socket.AGVMainSocket;
import com.jimi.uw_server.constant.IOTaskItemState;
import com.jimi.uw_server.constant.InventoryTaskItemState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.RobotBO;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.service.TaskService;

/**
 * 出入库LS、SL命令处理器
 * <br>
 * <b>2018年7月10日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class IOHandler {

	private static TaskService taskService = Enhancer.enhance(TaskService.class);

	private static MaterialService materialService = Enhancer.enhance(MaterialService.class);

	private static String GET_WINDOW_BY_TASK_ID = "select * from window where bind_task_id = ?";
	
	public static final String UNDEFINED = "undefined";

	public static void sendSL(AGVIOTaskItem item, MaterialBox materialBox) throws Exception {
		synchronized (Lock.ROBOT_ORDER_REDIS_LOCK) {
			//构建SL指令，令指定robot把料送回原仓位
			AGVMoveCmd moveCmd = createSLCmd(materialBox, item);
			//发送SL>>>
			AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
			if (TaskItemRedisDAO.getRobotOrder(item.getRobotId()) != null && TaskItemRedisDAO.getRobotOrder(item.getRobotId()).equals(item.getGroupId())) {
				TaskItemRedisDAO.setRobotOrder(item.getRobotId(), UNDEFINED);
			}
		}
	}


	public static void sendLS(AGVIOTaskItem item, MaterialBox materialBox, Integer robotId) throws Exception {
		synchronized (Lock.ROBOT_ORDER_REDIS_LOCK) {
			//发送LS>>>
			if (TaskItemRedisDAO.getRobotOrder(robotId) == null || TaskItemRedisDAO.getRobotOrder(robotId).equals(UNDEFINED)) {
				
				AGVMoveCmd cmd = createLSCmd(materialBox, item, robotId);
				AGVMainSocket.sendMessage(Json.getJson().toJson(cmd));
				//在数据库标记所有处于该坐标的料盒为不在架***
				setMaterialBoxIsOnShelf(materialBox, false);
				TaskItemRedisDAO.setRobotOrder(robotId, item.getGroupId());
				//更新任务条目状态为已分配***
				TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.ASSIGNED);
			}
		}
		
	}
	
	
	/**
	 * 发送指定叉车盘点回库指令
	 * @param item
	 * @param materialBox
	 * @throws Exception
	 */
	public static void sendSL(AGVInventoryTaskItem item, MaterialBox materialBox, Window window) throws Exception {
		synchronized (Lock.ROBOT_ORDER_REDIS_LOCK) {
			//构建SL指令，令指定robot把料送回原仓位
			AGVMoveCmd moveCmd = createSLCmd(materialBox, item, window);
			//发送SL>>>
			AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
			if (TaskItemRedisDAO.getRobotOrder(item.getRobotId()) != null && TaskItemRedisDAO.getRobotOrder(item.getRobotId()).equals(item.getGroupId())) {
				TaskItemRedisDAO.setRobotOrder(item.getRobotId(), UNDEFINED);
			}
			TaskItemRedisDAO.setWindowFlag(window.getId(), false);
		}
		
	}


	/**
	 * 发送指定叉车盘点取货指令
	 * @param item
	 * @param materialBox
	 * @throws Exception
	 */
	public static  void sendLS(AGVInventoryTaskItem item, MaterialBox materialBox, Window window, Integer robotId) throws Exception {
		synchronized (Lock.ROBOT_ORDER_REDIS_LOCK) {
			//发送LS>>>
			if (TaskItemRedisDAO.getRobotOrder(robotId) == null || TaskItemRedisDAO.getRobotOrder(robotId).equals(UNDEFINED)) {
				
				AGVMoveCmd cmd = createLSCmd(materialBox, item, window, robotId);
				AGVMainSocket.sendMessage(Json.getJson().toJson(cmd));
				TaskItemRedisDAO.setRobotOrder(robotId, item.getGroupId());
				//在数据库标记所有处于该坐标的料盒为不在架***
				setMaterialBoxIsOnShelf(materialBox, false);
				//更新任务条目状态为已分配***
				TaskItemRedisDAO.updateInventoryTaskItemWindow(item, window.getId());
				TaskItemRedisDAO.setWindowFlag(window.getId(), true);
				TaskItemRedisDAO.updateInventoryTaskItemState(item, InventoryTaskItemState.ASSIGNED);
			}
		}

		
	}
	
	/**
	 * 处理Status指令
	 */
	public static  void handleStatus(String message) throws Exception {
		//转换成实体类
		AGVStatusCmd statusCmd = Json.getJson().parse(message, AGVStatusCmd.class);

		// missiongroupid 包含“:”表示为出入库任务
		if (statusCmd.getMissiongroupid().contains(":")) {
			//判断是否是开始执行任务
			if(statusCmd.getStatus() == IOTaskItemState.WAIT_ASSIGN) {
				handleStatus0(statusCmd);
			}

			//判断叉车是否已到达仓口
			if(statusCmd.getStatus() == IOTaskItemState.ARRIVED_WINDOW) {
				handleStatus2(statusCmd);
			}
		}else if (statusCmd.getMissiongroupid().contains("@")) {
			//盘点任务
			//判断是否是开始执行任务
			if(statusCmd.getStatus() == InventoryTaskItemState.WAIT_ASSIGN) {
				handleInventoryTaskStatus0(statusCmd);
			}

			//判断叉车是否已到达仓口
			if(statusCmd.getStatus() == InventoryTaskItemState.ARRIVED_WINDOW) {
				handleInventoryTaskStatus2(statusCmd);
			}
		}
	}


	private static  void handleStatus0(AGVStatusCmd statusCmd) {
		//获取groupid
		String groupid = statusCmd.getMissiongroupid();

		//匹配groupid
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[1]))) {
			if(groupid.equals(item.getGroupId())) {
				//更新tsakitems里对应item的robotid
				TaskItemRedisDAO.updateIOTaskItemRobot(item, statusCmd.getRobotid());
			}
		}
	}


	private static void handleStatus2(AGVStatusCmd statusCmd) throws Exception {
		//获取groupid
		String groupid = statusCmd.getMissiongroupid();
		
		//匹配groupid
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[1]))) {
			if(groupid.equals(item.getGroupId())) {
				
				//判断是LS指令还是SL指令第二动作完成，状态是1说明是LS，状态2是SL
				if(item.getState() == IOTaskItemState.ASSIGNED) {//LS执行完成时
					//更改taskitems里对应item状态为2（已拣料到站）***
					TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.ARRIVED_WINDOW);
					break;
				} else if(item.getState() == IOTaskItemState.START_BACK) {//SL执行完成时：
					//更改taskitems里对应item状态为4（已回库完成）***
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
					setMaterialBoxIsOnShelf(materialBox, true);

					if (item.getIsCut()) {
						TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.FINISH_CUT);
						TaskItemRedisDAO.updateIOTaskItemIsCut(item, false);
					}
					nextRound(item);

					clearTil(groupid);
				}
			}
		}
	}
	

	private static void nextRound(AGVIOTaskItem item) {
		// 获取任务类型
		Integer taskType = Task.dao.findById(item.getTaskId()).getType();
		// 判断实际出入库数量是否不满足计划数
		if (!item.getIsForceFinish()) {
			// 如果是出库任务，若实际出库数量小于计划出库数量，则将任务条目状态回滚到未分配状态
			if (taskType == TaskType.OUT) {
				TaskItemRedisDAO.updateIOTaskItemRobot(item, 0);
				TaskItemRedisDAO.updateTaskItemBoxId(item, 0);
				TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.WAIT_ASSIGN);
			} else {	// 如果是入库或退料入库任务，若实际入库或退料入库数量小于计划入库或退料入库数量，则将任务条目状态回滚到等待扫码状态
				TaskItemRedisDAO.updateIOTaskItemRobot(item, 0);
				TaskItemRedisDAO.updateTaskItemBoxId(item, 0);
				TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.WAIT_SCAN);
			}
		}	
	}


	/**
	 * 判断该groupid所在的任务是否全部条目状态为"已回库完成"并且没有需要截料返库的，也如果是，
	 * 则清除所有该任务id对应的条目，释放内存，并修改数据库任务状态***
	*/
	public static void clearTil(String groupid) {
		boolean isAllFinish = true;
		boolean isLack = false;
		int taskId = Integer.valueOf(groupid.split(":")[1]);
		for (AGVIOTaskItem item1 : TaskItemRedisDAO.getIOTaskItems(taskId)) {
			if (item1.getState() == IOTaskItemState.LACK) {
				isLack = true;
			}
			if( (item1.getState() != IOTaskItemState.FINISH_BACK && item1.getState() != IOTaskItemState.LACK && !item1.getIsForceFinish())) {
				isAllFinish = false;
			}
			if (item1.getState() == IOTaskItemState.FINISH_CUT || item1.getIsCut().equals(true)) {
				isAllFinish = false;
			}
			if (item1.getIsForceFinish() && item1.getState() >= 0 && item1.getState() <= 3) {
				isAllFinish = false;
			}
			
		}
		if(isAllFinish) {
			
			taskService.finish(taskId, isLack);
			TaskItemRedisDAO.removeTaskItemByTaskId(taskId);
		}
	}

	
	/**
	 * 盘点任务时，更改盘点任务条目绑定的叉车
	 * @param statusCmd
	 */
	private static void handleInventoryTaskStatus0(AGVStatusCmd statusCmd) {
		//获取groupid
		String groupid = statusCmd.getMissiongroupid();

		//匹配groupid
		for (AGVInventoryTaskItem item : TaskItemRedisDAO.getInventoryTaskItems()) {
			if(groupid.equals(item.getGroupId())) {
				//更新tsakitems里对应item的robotid
				TaskItemRedisDAO.updateInventoryTaskItemRobot(item, statusCmd.getRobotid());
			}
		}
	}
	
	
	/**
	 * 盘点任务时，叉车完成指令后后续操作，到站或者回库完成
	 * @param statusCmd
	 */
	private static void handleInventoryTaskStatus2(AGVStatusCmd statusCmd) throws Exception {
		//获取groupid
		String groupid = statusCmd.getMissiongroupid();
		
		//匹配groupid
		for (AGVInventoryTaskItem item : TaskItemRedisDAO.getInventoryTaskItems()) {
			if(groupid.equals(item.getGroupId())) {
				
				//判断是LS指令还是SL指令第二动作完成，状态是1说明是LS，状态2是SL
				if(item.getState() == InventoryTaskItemState.ASSIGNED) {//LS执行完成时
					//更改taskitems里对应item状态为2（已拣料到站）***
					TaskItemRedisDAO.updateInventoryTaskItemState(item, InventoryTaskItemState.ARRIVED_WINDOW);
					break;
				} else if(item.getState() == InventoryTaskItemState.START_BACK) {//SL执行完成时：
					//更改taskitems里对应item状态为4（已回库完成）***
					TaskItemRedisDAO.updateInventoryTaskItemState(item, InventoryTaskItemState.FINISH_BACK);
					MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
					materialBox.setIsOnShelf(true);
					materialBox.update();
					clearInventoryTask(item.getTaskId());
					break;
				}
			}
		}
	}
	
	
	public static void clearInventoryTask(Integer taskId) {
		boolean flag = true;
		for(AGVInventoryTaskItem item : TaskItemRedisDAO.getInventoryTaskItems()) {
			if (item.getTaskId().equals(taskId) && item.getIsForceFinish().equals(false)) {
				flag = false;
				break;
			}
		}
		if (flag) {
			TaskItemRedisDAO.removeInventoryTaskItemByTaskId(taskId);
			List<Window> windows = Window.dao.find(GET_WINDOW_BY_TASK_ID, taskId);
			// 将仓口解绑(作废任务时，如果还有任务条目没跑完就不会解绑仓口，因此不管任务状态是为进行中还是作废，这里都需要解绑仓口)
			synchronized (Lock.ROBOT_TASK_REDIS_LOCK) {
				List<RobotBO> robotBOs = RobotInfoRedisDAO.check();
				for (RobotBO robotBO : robotBOs) {
					Integer taskId2 = TaskItemRedisDAO.getRobotTask(robotBO.getId());
					if (taskId2 != null && taskId2.equals(taskId)) {
						TaskItemRedisDAO.delRobotTask(robotBO.getId());
					}
				}
			}
			for (Window window : windows) {
				TaskItemRedisDAO.delWindowFlag(window.getId());;
				window.setBindTaskId(null).update();
			}
			
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


	private static AGVMoveCmd createLSCmd(MaterialBox materialBox, AGVIOTaskItem item, Integer robotId) {
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getGroupId());
		group.setRobotid(robotId);//让AGV系统自动分配
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
			mb.update();
		}
	}
	
	
	private static AGVMoveCmd createSLCmd(MaterialBox materialBox, AGVInventoryTaskItem item, Window window) {
		List<AGVMissionGroup> groups = new ArrayList<>();
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getGroupId());//missionGroupId要和LS指令相同
		group.setRobotid(item.getRobotId());//robotId要和LS指令相同
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
	
	
	private static AGVMoveCmd createLSCmd(MaterialBox materialBox, AGVInventoryTaskItem item, Window window, Integer robotId) {
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getGroupId());
		group.setRobotid(robotId);//让AGV系统自动分配
		group.setStartx(materialBox.getRow());//物料Row
		group.setStarty(materialBox.getCol());//物料Col
		group.setStartz(materialBox.getHeight());//物料Height
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

}
