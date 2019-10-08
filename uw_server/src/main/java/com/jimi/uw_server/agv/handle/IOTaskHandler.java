package com.jimi.uw_server.agv.handle;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.jfinal.aop.Aop;
import com.jfinal.json.Json;
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
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.service.SupplierService;
import com.jimi.uw_server.ur.entity.AckResponseManager;
import com.jimi.uw_server.ur.entity.CmdidManager;
import com.jimi.uw_server.ur.entity.IOPackage;
import com.jimi.uw_server.ur.entity.ReachInPackage;
import com.jimi.uw_server.ur.entity.ReachOutPackage;
import com.jimi.uw_server.ur.entity.SessionBox;
import com.jimi.uw_server.ur.entity.base.UrMaterialInfo;
import com.jimi.uw_server.ur.processor.OutPackageHolder;

import cc.darhao.dautils.api.DateUtil;

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

	private static final String GET_REMAIN_MATERIAL_BY_BOX = "SELECT * FROM material where box = ? and remainder_quantity > 0";

	private volatile static IOTaskHandler me;


	private IOTaskHandler() {
	}

	private int i = 0;

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
			TaskItemRedisDAO.updateIOTaskItemInfo(agvioTaskItem, TaskItemState.ASSIGNED, goodsLocation.getWindowId(), goodsLocation.getId(), null, null, null, null);
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
			TaskItemRedisDAO.updateIOTaskItemInfo(agvioTaskItem, TaskItemState.START_BACK, null, null, null, null, null, null);
		}
	}


	/*
	 * @Override protected void handleStartStatus(AGVStatusCmd statusCmd) { String
	 * missionGroupId = statusCmd.getMissiongroupid(); // missiongroupid
	 * 包含“:”表示为出入库任务 String groupid = missionGroupId.split("_")[0]; // 匹配groupid for
	 * (AGVIOTaskItem item :
	 * TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) { if
	 * (groupid.equals(item.getGroupId())) { // 更新tsakitems里对应item的robotid
	 * TaskItemRedisDAO.updateIOTaskItemInfo(item, null, null, null, null,
	 * statusCmd.getRobotid(), null, null); } } }
	 * 
	 * 
	 * @Override protected void handleProcessStatus(AGVStatusCmd statusCmd) throws
	 * Exception { String missionGroupId = statusCmd.getMissiongroupid(); String
	 * groupid = missionGroupId.split("_")[0]; for (AGVIOTaskItem item :
	 * TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {
	 * 
	 * if (item.getGroupId().equals(groupid.trim()) && item.getState() ==
	 * TaskItemState.ASSIGNED && missionGroupId.contains("S")) {// 判断是取料盒并且叉到料盒 //
	 * 更改taskitems里对应item状态为2（已拣料到站）*** TaskItemRedisDAO.updateIOTaskItemInfo(item,
	 * TaskItemState.SEND_BOX, null, null, null, null, null, null); break; } else if
	 * (item.getState() == TaskItemState.START_BACK &&
	 * item.getBoxId().equals(Integer.valueOf(groupid.split(":")[1])) &&
	 * missionGroupId.contains("B")) {// 判断是回库并且叉到料盒 //
	 * 更改taskitems里对应item状态为4（已回库完成）*** TaskItemRedisDAO.updateIOTaskItemInfo(item,
	 * TaskItemState.BACK_BOX, null, null, null, null, null, null);
	 * TaskItemRedisDAO.setLocationStatus(item.getWindowId(),
	 * item.getGoodsLocationId(), 0); } } }
	 * 
	 * 
	 * @Override protected void handleFinishStatus(AGVStatusCmd statusCmd) throws
	 * Exception { // 获取groupid String missionGroupId =
	 * statusCmd.getMissiongroupid(); // missiongroupid 包含“:”表示为出入库任务 String groupid
	 * = missionGroupId.split("_")[0]; // 匹配groupid for (AGVIOTaskItem item :
	 * TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) { //
	 * 判断是LS指令还是SL指令第二动作完成，状态是1说明是LS，状态2是SL if (groupid.equals(item.getGroupId()) &&
	 * item.getState() == TaskItemState.SEND_BOX && missionGroupId.contains("S"))
	 * {// LS执行完成时 // 更改taskitems里对应item状态为2（已拣料到站）***
	 * TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.ARRIVED_WINDOW,
	 * null, null, null, null, null, null); break; } else if (item.getState() ==
	 * TaskItemState.BACK_BOX &&
	 * item.getBoxId().equals(Integer.valueOf(groupid.split(":")[1])) &&
	 * missionGroupId.contains("B")) {// SL执行完成时： //
	 * 更改taskitems里对应item状态为4（已回库完成）*** Task task =
	 * Task.dao.findById(item.getTaskId()); if
	 * (item.getIsForceFinish().equals(false) &&
	 * task.getType().equals(TaskType.OUT)) { Integer remainderQuantity =
	 * materialService.countAndReturnRemainderQuantityByMaterialTypeId(item.
	 * getMaterialTypeId());
	 * 
	 * if (remainderQuantity <= 0) { item.setState(TaskItemState.LACK);
	 * item.setIsForceFinish(true); TaskItemRedisDAO.updateIOTaskItemInfo(item,
	 * TaskItemState.LACK, null, null, null, null, true, null); } else {
	 * TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_BACK, null,
	 * null, null, null, null, null); } } else {
	 * TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_BACK, null,
	 * null, null, null, null, null); }
	 * 
	 * // 设置料盒在架 MaterialBox materialBox =
	 * MaterialBox.dao.findById(item.getBoxId());
	 * materialBox.setIsOnShelf(true).update();
	 * 
	 * if (item.getIsCut()) { TaskItemRedisDAO.updateIOTaskItemInfo(item,
	 * TaskItemState.FINISH_CUT, null, null, null, null, null, false); }
	 * nextRound(item);
	 * 
	 * clearTask(task.getId()); }
	 * 
	 * } }
	 */

	@Override
	protected void handleStartStatus(AGVStatusCmd statusCmd) {
		String missionGroupId = statusCmd.getMissiongroupid();
		// missiongroupid 包含“:”表示为出入库任务
		String groupid = missionGroupId.split("_")[0];
		// 匹配groupid
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {
			if (groupid.equals(item.getGroupId())) {
				// 更新tsakitems里对应item的robotid
				TaskItemRedisDAO.updateIOTaskItemInfo(item, null, null, null, null, statusCmd.getRobotid(), null, null);
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
				TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.SEND_BOX, null, null, null, null, null, null);
				break;
			} else if (item.getState() == TaskItemState.START_BACK && item.getBoxId().equals(Integer.valueOf(groupid.split(":")[1])) && missionGroupId.contains("B")) {// 判断是回库并且叉到料盒
				// 更改taskitems里对应item状态为4（已回库完成）***
				TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.BACK_BOX, null, null, null, null, null, null);
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
		// 匹配groupid
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {
			// 判断是LS指令还是SL指令第二动作完成，状态是1说明是LS，状态2是SL
			if (groupid.equals(item.getGroupId()) && item.getState() == TaskItemState.SEND_BOX && missionGroupId.contains("S")) {// LS执行完成时
				// 更改taskitems里对应item状态为2（已拣料到站）***
				List<Material> materials = Material.dao.find(GET_REMAIN_MATERIAL_BY_BOX, item.getBoxId());
				List<UrMaterialInfo> urMaterialInfos = new ArrayList<>();

				// 将所有同任务的物料都设为到达仓口 并且 获取物料信息
				for (AGVIOTaskItem redisItem : TaskItemRedisDAO.getIOTaskItems(Integer.valueOf(groupid.split(":")[2]))) {
					TaskItemRedisDAO.updateIOTaskItemInfo(redisItem, TaskItemState.ARRIVED_WINDOW, null, null, null, null, null, null);
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

				Task task = Task.dao.findById(item.getTaskId());
				if (task.getType().equals(TaskType.IN)) {
					// 如果是入库任务，则把reach_in加入发送队列
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							pushReachIn(item);
						}
					}).start();
					
					
				} else {
					// 如果是出库任务，则把reach_out和out加入发送队列
					new Thread(new Runnable() {
						
						@Override
						public void run() {
							// TODO Auto-generated method stub
							pushReachOutAndOut(item, urMaterialInfos);
						}
					}).start();
					
				}
				break;
			} else if (item.getState() == TaskItemState.BACK_BOX && item.getBoxId().equals(Integer.valueOf(groupid.split(":")[1])) && missionGroupId.contains("B")) {// SL执行完成时：
				// 更改taskitems里对应item状态为4（已回库完成）***
				Task task = Task.dao.findById(item.getTaskId());
				
				if (item.getIsForceFinish().equals(false) && task.getType().equals(TaskType.OUT)) {
					Integer remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(item.getMaterialTypeId());

					if (remainderQuantity <= 0) {
						item.setState(TaskItemState.LACK);
						item.setIsForceFinish(true);
						TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.LACK, null, null, null, null, true, null);
					} else {
						TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null, null, null);
					}
				} else {
					TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_BACK, null, null, null, null, null, null);
				}
				Window window = Window.dao.findFirst("select * from window where bind_task_id = ?", task.getId());
				Integer windowId = window.getId();
				// 设置料盒在架
				MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
				materialBox.setIsOnShelf(true).update();

				if (item.getIsCut()) {
					TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_CUT, null, null, null, null, null, false);
				}
				nextRound(item);

				clearTask(task.getId());
				task = Task.dao.findById(task.getId());
				if (!task.getState().equals(TaskState.CANCELED)) {
					task.setEndTime(new Date()).setState(TaskState.FINISHED);
					task.update();
				}
				
				TaskItemRedisDAO.removeTaskItemByTaskId(task.getId());
				TaskItemRedisDAO.delTaskStatus(task.getId());
				window.setBindTaskId(null).update();

				if (task.getState().equals(TaskState.FINISHED) || task.getState().equals(TaskState.CANCELED)) {
					Task newTask = new Task();
					int type = 0;
					String fileName = "";
					Integer destination = null;
					i++;
					if (task.getType().equals(0) || task.getType().equals(4)) {
						type = 1;
						fileName = task.getFileName().replace("出", "入").split("_")[0] + "_"+ i;
						destination = 1;
						List<Material> materials = Material.dao.find("select * from material");
						if (!materials.isEmpty()) {
							for (Material material : materials) {
								material.setBox(item.getBoxId()).setRemainderQuantity(50).setIsInBox(true).update();
							}
						}
					}else {
						type = 0;
						fileName = task.getFileName().replace("入", "出").split("_")[0] + "_"+ i;
						List<Material> materials = Material.dao.find("select * from material");
						if (!materials.isEmpty()) {
							for (Material material : materials) {
								material.setBox(item.getBoxId()).setRemainderQuantity(0).setIsInBox(true).update();
							}
						}
					} 
					newTask.setType(type).setSupplier(task.getSupplier()).setFileName(fileName).setDestination(destination).setCreateTime(new Date()).setRemarks("自动").setWarehouseType(WarehouseType.REGULAR).setState(TaskState.WAIT_START).save();
					List<PackingListItem> packingListItems = PackingListItem.dao.find("select * from packing_list_item where task_id = ?", task.getId());
					for (PackingListItem packingListItem : packingListItems) {
						PackingListItem newPackingListItem = new PackingListItem();
						newPackingListItem.setMaterialTypeId(packingListItem.getMaterialTypeId());
						newPackingListItem.setQuantity(packingListItem.getQuantity());
						newPackingListItem.setTaskId(newTask.getId());
						newPackingListItem.save();
					}
					if (IOTaskService.flag.get()) {
						new Thread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								taskService.start(newTask.getId(), windowId);
							}
						}).start();
						
					}
				}
				
				break;
			}

		}
	}


	private static void pushReachOutAndOut(AGVIOTaskItem item, List<UrMaterialInfo> urMaterialInfos) {
		try {
			ReachOutPackage reachOutPackage = new ReachOutPackage();
			reachOutPackage.setTaskId(item.getTaskId());
			reachOutPackage.setCmdid(CmdidManager.getCmdid());
			CountDownLatch l = new CountDownLatch(1);
			AckResponseManager.putAckResponse(reachOutPackage.getCmdid(), l);
			SessionBox.getChannelHandlerContext("ur").channel().writeAndFlush(reachOutPackage);
			l.await(30000, TimeUnit.SECONDS);
			IOPackage outPackage = new IOPackage(false);
			outPackage.setTaskId(item.getTaskId());
			outPackage.setList(urMaterialInfos);
			// 获取供应商名称
			String supplierName = Supplier.dao.findFirst(SupplierService.GET_SUPPLIER_BY_MATERIAL_TYPE_ID, item.getMaterialTypeId()).getName();
			outPackage.setSupplier(supplierName);
			outPackage.setCmdid(CmdidManager.getCmdid());
			l = new CountDownLatch(1);
			AckResponseManager.putAckResponse(outPackage.getCmdid(), l);
			// 把out包放到holder里
			OutPackageHolder.me.put(outPackage);
			SessionBox.getChannelHandlerContext("ur").channel().writeAndFlush(outPackage);
			l.await(30000, TimeUnit.SECONDS);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	private static void pushReachIn(AGVIOTaskItem item) {
		ReachInPackage reachInPackage = new ReachInPackage();
		reachInPackage.setTaskId(item.getTaskId());
		reachInPackage.setCmdid(CmdidManager.getCmdid());
		CountDownLatch l = new CountDownLatch(1);
		AckResponseManager.putAckResponse(reachInPackage.getCmdid(), l);
		SessionBox.getChannelHandlerContext("ur").channel().writeAndFlush(reachInPackage);
		try {
			l.await(30000, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	private void nextRound(AGVIOTaskItem item) {
		// 获取任务类型
		Integer taskType = Task.dao.findById(item.getTaskId()).getType();
		// 判断实际出入库数量是否不满足计划数
		if (!item.getIsForceFinish()) {
			// 如果是出库任务，若实际出库数量小于计划出库数量，则将任务条目状态回滚到未分配状态
			if (taskType == TaskType.OUT) {
				TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.WAIT_ASSIGN, 0, 0, 0, 0, null, null);
			} else { // 如果是入库或退料入库任务，若实际入库或退料入库数量小于计划入库或退料入库数量，则将任务条目状态回滚到等待扫码状态
				TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.WAIT_SCAN, 0, 0, 0, 0, null, null);
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
		isAllFinish = true;
		isLack = false;
		if (isAllFinish) {

			taskService.finishRegualrTask(taskId, isLack);
			TaskItemRedisDAO.removeTaskItemByTaskId(taskId);
			TaskItemRedisDAO.delTaskStatus(taskId);
		}
	}

}
