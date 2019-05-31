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
import com.jimi.uw_server.constant.IOTaskItemState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.service.SupplierService;
import com.jimi.uw_server.service.TaskService;
import com.jimi.uw_server.ur.entity.IOPackage;
import com.jimi.uw_server.ur.entity.ReachInPackage;
import com.jimi.uw_server.ur.entity.ReachOutPackage;
import com.jimi.uw_server.ur.entity.base.UrMaterialInfo;
import com.jimi.uw_server.ur.socket.UrSocekt;

import cc.darhao.dautils.api.DateUtil;

/**
 * 出入库命令处理器
 * <br>
 * <b>2018年7月10日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class IOHandler {
	
	private static Integer cmdid = 0;

	private static TaskService taskService = Enhancer.enhance(TaskService.class);

	private static MaterialService materialService = Enhancer.enhance(MaterialService.class);

	private static final String GET_REMAIN_MATERIAL_BY_BOX = "SELECT * FROM material where box = ? and remainder_quantity > 0";

	public static void sendReturnBoxCmd(AGVIOTaskItem item, MaterialBox materialBox, Window window) throws Exception {
		//构建指令，令指定robot把料送回原仓位
		AGVMoveCmd moveCmd = createReturnBoxCmd(materialBox, item, window);
		//发送>>>
		AGVMainSocket.sendMessage(Json.getJson().toJson(moveCmd));
	}


	public static void sendGetBoxCmd(AGVIOTaskItem item, MaterialBox materialBox, Window window) throws Exception {
		//发送>>>
		AGVMoveCmd cmd = createGetBoxCmd(materialBox, item, window);
		AGVMainSocket.sendMessage(Json.getJson().toJson(cmd));

		//在数据库标记所有处于该坐标的料盒为不在架***
		setMaterialBoxIsOnShelf(materialBox, false);

		//更新任务条目状态为已分配***
		TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.ASSIGNED);
	}


	/**
	 * 处理Status指令
	 */
	public static void handleStatus(String message) throws Exception {
		//转换成实体类
		AGVStatusCmd statusCmd = Json.getJson().parse(message, AGVStatusCmd.class);

		// missiongroupid 包含“:”表示为出入库任务
		if (statusCmd.getMissiongroupid().contains(":")) {
			//判断是否是开始执行任务
			if(statusCmd.getStatus() == 0) {
				handleStatus0(statusCmd);
			}

			//判断叉车是否已到达仓口
			if(statusCmd.getStatus() == 2) {
				handleStatus2(statusCmd);
			}
		}
	}


	private static void handleStatus0(AGVStatusCmd statusCmd) {
		//获取groupid
		String groupid = statusCmd.getMissiongroupid();

		//匹配groupid
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems()) {
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
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems()) {
			if(groupid.equals(item.getGroupId())) {
				
				//判断是GetBox指令还是returnBox指令第二动作完成，状态是1说明是GetBox，状态2是returnBox
				if(item.getState() == IOTaskItemState.ASSIGNED) {//GetBox执行完成时
					//更改taskitems里对应item状态为2（已拣料到站）***

					List<Material> materials = Material.dao.find(GET_REMAIN_MATERIAL_BY_BOX, item.getBoxId());
					List<UrMaterialInfo> urMaterialInfos = new ArrayList<>();
					
					//将所有同任务的物料都设为到达仓口 并且 获取物料信息
					for (AGVIOTaskItem redisItem : TaskItemRedisDAO.getIOTaskItems()) {
						if (redisItem.getTaskId().equals(item.getTaskId())) {
							TaskItemRedisDAO.updateIOTaskItemState(redisItem, IOTaskItemState.ARRIVED_WINDOW);
							for (Material material : materials) {
								if (material.getType().equals(redisItem.getMaterialTypeId())) {
									UrMaterialInfo urMaterialInfo = new UrMaterialInfo();
									urMaterialInfo.setMaterialNo(material.getId());
									urMaterialInfo.setMaterialTypeId(material.getType());
									urMaterialInfo.setQuantity(material.getRemainderQuantity());
									urMaterialInfo.setProductionTime(DateUtil.yyyyMMdd(material.getProductionTime()));
									urMaterialInfo.setCol(material.getCol());
									urMaterialInfo.setRow(material.getRow());
									urMaterialInfos.add(urMaterialInfo);
									break;
								}
							}
						}
					}
					
					Task task = Task.dao.findById(item.getTaskId());
					if (task.getType().equals(TaskType.IN)) {
						//如果是入库任务，则把reach_in加入发送队列
						pushReachIn(item);
					}else {
						//如果是出库任务，则把reach_out和out加入发送队列
						pushReachOutAndOut(item, urMaterialInfos);
					}
					
					//只发一次，跳出循环
					break;
					
				} else if(item.getState() == IOTaskItemState.START_BACK) {//returnBox执行完成时：
					//更改taskitems里对应item状态为4（已回库完成）***
					Task task = Task.dao.findById(item.getTaskId());
					if (item.getIsForceFinish().equals(false) && task.getType().equals(TaskType.OUT)) {
						Integer remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(item.getMaterialTypeId());
						if (remainderQuantity <= 0) {
							item.setState(IOTaskItemState.LACK);
							item.setIsForceFinish(true);
							TaskItemRedisDAO.updateTaskIsForceFinish(item, true);
							TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.LACK);
						}else {
							TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.FINISH_BACK);
						}
					}else {
						TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.FINISH_BACK);
					}
					

					// 设置料盒在架
					MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
					setMaterialBoxIsOnShelf(materialBox, true);

					nextRound(item);

					clearTil(groupid);
				} else if(item.getState() == IOTaskItemState.FINISH_CUT) {
					// 设置料盒在架
					MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
					setMaterialBoxIsOnShelf(materialBox, true);
				}
			}
		}
	}


	private static void pushReachOutAndOut(AGVIOTaskItem item, List<UrMaterialInfo> urMaterialInfos) {
		ReachOutPackage reachOutPackage = new ReachOutPackage();
		reachOutPackage.setCmdid(getCmdid());
		reachOutPackage.setTaskId(item.getTaskId());
		UrSocekt.queueHolder.push(reachOutPackage);
		
		IOPackage outPackage = new IOPackage(false);
		outPackage.setCmdid(getCmdid());
		outPackage.setTaskId(item.getTaskId());
		outPackage.setList(urMaterialInfos);
		//获取供应商名称
		String supplierName = Supplier.dao.findFirst(SupplierService.GET_SUPPLIER_BY_MATERIAL_TYPE_ID, item.getMaterialTypeId()).getName();
		outPackage.setSupplier(supplierName);
		UrSocekt.queueHolder.push(outPackage);
		
		//把out包放到holder里
		UrSocekt.outPackageHolder.put(outPackage);
	}


	private static void pushReachIn(AGVIOTaskItem item) {
		ReachInPackage reachInPackage = new ReachInPackage();
		reachInPackage.setCmdid(getCmdid());
		reachInPackage.setTaskId(item.getTaskId());
		UrSocekt.queueHolder.push(reachInPackage);
	}
	
	
	private static Integer getCmdid() {
		synchronized (IOHandler.class) {
			if ((cmdid % 65535) == 0) {
				cmdid = 0;
			}
			cmdid ++;
			return cmdid;
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
	 * 判断该groupid所在的任务是否全部条目状态为"已回库完成"并且没有需要截料返库的，如果是，
	 * 则清除所有该任务id对应的条目，释放内存，并修改数据库任务状态***
	*/
	public static void clearTil(String groupid) {
		boolean isAllFinish = true;
		boolean isLack = false;
		int taskId = Integer.valueOf(groupid.split(":")[1]);
		for (AGVIOTaskItem item1 : TaskItemRedisDAO.getIOTaskItems()) {
			if (groupid.split(":")[1].equals(item1.getGroupId().split(":")[1])) {
				if (item1.getState() == IOTaskItemState.LACK) {
					isLack = true;
				}
				if( (item1.getState() != IOTaskItemState.FINISH_BACK &&  item1.getState() != IOTaskItemState.LACK && !item1.getIsForceFinish())) {
					isAllFinish = false;
					
				}
				if (item1.getState() == IOTaskItemState.FINISH_CUT) {
					isAllFinish = false;
				}
			}
		}
		if(isAllFinish) {
			
			TaskItemRedisDAO.removeTaskItemByTaskId(taskId);
			taskService.finish(taskId, isLack);
		}
	}


	private static AGVMoveCmd createReturnBoxCmd(MaterialBox materialBox, AGVIOTaskItem item, Window window) {
		List<AGVMissionGroup> groups = new ArrayList<>();
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getGroupId());//missionGroupId要和LS指令相同
		group.setRobotid(item.getRobotId());//robotId要和LS指令相同
		group.setStartx(window.getRow());//起点X为仓口X
		group.setStarty(window.getCol());//起点Y为仓口Y
		group.setStartz(1);//仓口高度1
		group.setEndx(materialBox.getRow());//设置X
		group.setEndy(materialBox.getCol());//设置Y
		group.setEndz(materialBox.getHeight());//设置Z
		groups.add(group);
		AGVMoveCmd moveCmd = new AGVMoveCmd();
		moveCmd.setCmdcode("LL");
		moveCmd.setCmdid(TaskItemRedisDAO.getCmdId());
		moveCmd.setMissiongroups(groups);
		return moveCmd;
	}
	

	private static AGVMoveCmd createGetBoxCmd(MaterialBox materialBox, AGVIOTaskItem item, Window window) {
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getGroupId());
		group.setRobotid(0);//让AGV系统自动分配
		group.setStartx(materialBox.getRow());//物料Row
		group.setStarty(materialBox.getCol());//物料Col
		group.setStartz(materialBox.getHeight());//物料Height
		group.setEndx(window.getRow());//终点X为仓口X
		group.setEndy(window.getCol());//终点Y为仓口Y
		group.setEndz(1);//仓口高度1
		List<AGVMissionGroup> groups = new ArrayList<>();
		groups.add(group);
		AGVMoveCmd cmd = new AGVMoveCmd();
		cmd.setCmdcode("LL");
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

}
