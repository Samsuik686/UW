package com.jimi.uw_server.agv.thread;

import com.jfinal.aop.Aop;
import com.jfinal.kit.PropKit;
import com.jimi.uw_server.agv.dao.BuildTaskItemDAO;
import com.jimi.uw_server.agv.dao.IOTaskItemRedisDAO;
import com.jimi.uw_server.agv.dao.InventoryTaskItemRedisDAO;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.SampleTaskItemRedisDAO;
import com.jimi.uw_server.agv.dao.TaskPropertyRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVBuildTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.agv.entity.bo.base.BaseTaskItem;
import com.jimi.uw_server.agv.handle.BuildHandler;
import com.jimi.uw_server.agv.handle.IOTaskHandler;
import com.jimi.uw_server.agv.handle.InvTaskHandler;
import com.jimi.uw_server.agv.handle.SampleTaskHandler;
import com.jimi.uw_server.agv.handle.base.BaseTaskHandler;
import com.jimi.uw_server.constant.*;
import com.jimi.uw_server.constant.sql.SQL;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.*;
import com.jimi.uw_server.model.bo.RobotBO;
import com.jimi.uw_server.service.ExternalWhLogService;
import com.jimi.uw_server.service.io.RegularIOTaskService;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.service.inventory.RegularInventoryTaskService;
import com.jimi.uw_server.util.ErrorLogWritter;
import com.jimi.uw_server.util.UwProcessorExcutor;

import java.util.*;


/**
 * 任务池，负责分配任务 <br>
 * <b>2018年7月13日</b>
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class TaskPool extends Thread {

	private static final String GET_STANDARD_SAME_TYPE_MATERIAL_BOX_SQL = "SELECT material_box.id AS box FROM material_box INNER JOIN material ON material.box = material_box.id WHERE material.type = ? AND material_box.supplier = ? AND material_box.type = ? AND material_box.enabled = 1";

	private static final String GET_SAME_TYPE_MATERIAL_BOX_SQL = "SELECT material_box.id AS box FROM material_box INNER JOIN material ON material.box = material_box.id WHERE material.type = ? AND material_box.supplier = ? AND material_box.type = ? AND material_box.status = ? AND material_box.enabled = 1";

	private static final String GET_MATERIAL_BOX_USED_CAPACITY_SQL = "SELECT * FROM material WHERE box = ? AND remainder_quantity > 0";

	private static final String GET_DIFFERENT_TYPE_MATERIAL_BOX_SQL = "SELECT * FROM material_box WHERE enabled = 1 AND id NOT IN (SELECT material_box.id AS boxId FROM material_box INNER JOIN material ON material.box = material_box.id WHERE material.type = ? AND material_box.supplier = ?) AND supplier = ? AND material_box.type = ? AND material_box.enabled = 1";

	private static final String GET_EMPTY_DIFFERENT_TYPE_MATERIAL_BOX_SQL = "SELECT * FROM material_box WHERE enabled = 1 AND id NOT IN (SELECT material_box.id AS boxId FROM material_box INNER JOIN material ON material.box = material_box.id WHERE material.type = ? AND material_box.supplier = ?) AND supplier = ? AND material_box.type = ? AND material_box.status = ? AND material_box.enabled = 1";

	private static final String GET_UNFULL_DIFFERENT_TYPE_MATERIAL_BOX_SQL = "SELECT * FROM material_box WHERE enabled = 1 AND id NOT IN (SELECT material_box.id AS boxId FROM material_box INNER JOIN material ON material.box = material_box.id WHERE material.type = ? AND material_box.supplier = ?) AND supplier = ? AND material_box.type = ? AND material_box.status = ?  AND material_box.enabled = 1 ORDER BY update_time DESC ";

	private static final String GET_IN_BOX_MATERIAL_BY_TYPE_SQL = "SELECT * FROM material WHERE remainder_quantity > 0 AND type = ? AND is_in_box = b'1' ORDER BY production_time ASC";

	private static final String GET_MATERIALBOX_BY_TYPE_AND_STORETIME_SQL = "SELECT DISTINCT material_box.* FROM material INNER JOIN material_box ON material.box = material_box.id WHERE material.remainder_quantity > 0 AND material.type = ? AND is_in_box = b'1' AND material.production_time = ? AND material.box != ?  AND material_box.enabled = 1";

	private static final String GET_WORKING_WINDOWS = "SELECT * FROM window WHERE bind_task_id IS NOT NULL";

	private static IOTaskHandler ioTaskHandler = IOTaskHandler.getInstance();

	private static InvTaskHandler invTaskHandler = InvTaskHandler.getInstance();

	private static SampleTaskHandler samTaskHandler = SampleTaskHandler.getInstance();

	private static ExternalWhLogService externalWhLogService = Aop.get(ExternalWhLogService.class);

	private static RegularIOTaskService taskService = Aop.get(RegularIOTaskService.class);

	private static MaterialService materialService = Aop.get(MaterialService.class);

	private static final Integer UW_ID = 0;


	@Override
	public void run() {
		int taskPoolCycle = PropKit.use("properties.ini").getInt("taskPoolCycle");
		System.out.println("TaskPool is running NOW...");
		while (true) {
			try {
				sleep(taskPoolCycle);
				if (!TaskPropertyRedisDAO.getAgvWebSocketStatus()) {
					continue;
				}
				// 判断是否存在停止分配标志位
				List<RobotBO> robotBOs = countFreeRobot();
				// 判断til是否为空或者cn为0
				if (!robotBOs.isEmpty()) {
					List<Window> windows = Window.dao.find(GET_WORKING_WINDOWS);
					for (Window window : windows) {
						Task task = Task.dao.findById(window.getBindTaskId());
						// 获取出入库任务
						if (task == null || !(task.getState() == TaskState.PROCESSING || task.getState() == TaskState.CANCELED)
								|| (TaskPropertyRedisDAO.getTaskStatus(task.getId()) != null && !TaskPropertyRedisDAO.getTaskStatus(task.getId()))) {
							continue;
						}
						if (task.getType().equals(TaskType.IN) || task.getType().equals(TaskType.SEND_BACK)) {
							sendInTaskItemCmds(task, window);
							continue;
						}
						if (task.getType().equals(TaskType.OUT)) {
							sendOutTaskItemCmds(task, window);
							continue;
						}
						if (task.getType().equals(TaskType.COUNT)) {
							synchronized (Lock.INVENTORY_REDIS_LOCK) {
								sendInventoryTaskItemCmds(task, window);
							}
							continue;
						}
						if (task.getType().equals(TaskType.SAMPLE)) {
							synchronized (Lock.SAMPLE_TASK_REDIS_LOCK) {
								sendSampleTaskItemCmds(task, window);
							}
							continue;
						}
					}
				}
				List<AGVBuildTaskItem> buildTaskItems = new ArrayList<>();
				BuildTaskItemDAO.appendBuildTaskItems(buildTaskItems);
				if (buildTaskItems.isEmpty() || robotBOs.size() == 0) {
					continue;
				}
				sendBuildCmds(robotBOs.size(), buildTaskItems);
			} catch (Exception e) {
				ErrorLogWritter.save(e.getClass().getSimpleName() + ":" + e.getMessage());
				e.printStackTrace();
			}
		}
	}


	public void sendOutTaskItemCmds(Task task, Window window) throws Exception {
		List<AGVIOTaskItem> ioTaskItems = IOTaskItemRedisDAO.getIOTaskItems(task.getId());
		if (ioTaskItems.isEmpty()) {
			return;
		}
		int windowSize = window.getSize();
		List<GoodsLocation> goodsLocations = getEmptyGoodsLocations(window);
		int i = goodsLocations.size();
		Task inventoryTask = null;
		if (task.getType().equals(TaskType.OUT) && task.getIsInventoryApply()) {
			inventoryTask = Task.dao.findById(task.getInventoryTaskId());
		} else if (task.getType().equals(TaskType.OUT) && !task.getIsInventoryApply()) {
			inventoryTask = RegularInventoryTaskService.me.getOneUnStartInventoryTask(task.getSupplier(), WarehouseType.REGULAR.getId(), task.getDestination());
		}
		List<AGVIOTaskItem> standardItems = new ArrayList<AGVIOTaskItem>(4);
		List<AGVIOTaskItem> nonStandardItems = new ArrayList<AGVIOTaskItem>(4);
		Map<Integer, Integer> taskItemBoxMap = new HashMap<Integer, Integer>();
		for (AGVIOTaskItem item : ioTaskItems) {
			if (item.getState().intValue() == TaskItemState.LACK) {
				// 对于缺料的任务条目，若对应的物料已经补完库且该任务未结束，则将对应的任务条目更新为“等待分配”
				// 根据物料类型号获取物料库存数量
				Integer remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(item.getMaterialTypeId());
				if (remainderQuantity > 0) {
					IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.WAIT_ASSIGN, 0, 0, 0, 0, false, null, null, null, null);
				}
				continue;
			}
			if (item.getState().intValue() != TaskItemState.WAIT_ASSIGN) {
				continue;
			}
			if (task.getIsDeducted()) {
				boolean isDeducted = deductOutTaskQauntity(item, task, inventoryTask);
				if (isDeducted) {
					continue;
				}
			}
			boolean isLacked = checkAndHandleLackOutTaskItem(item, task);
			if (isLacked) {
				continue;
			}
			int boxId = getOldestMaterialBox(item);
			if (boxId == 0) {
				continue;
			}
			distributeBoxToItem(item, boxId, inventoryTask);
			MaterialBox materialBox = MaterialBox.dao.findById(boxId);
			if (materialBox == null || !materialBox.getIsOnShelf()) {
				continue;
			}
			if (window.getAuto()) {
				if (standardItems.size() > i) {
					continue;
				}
				sieveAutoOutTaskItem(item, materialBox, window, standardItems, taskItemBoxMap);
			} else {
				if (nonStandardItems.size() > i) {
					continue;
				}
				sieveManualOutTaskItem(item, materialBox, window, standardItems, nonStandardItems, taskItemBoxMap);
			}
		}
		if (!window.getAuto()) {
			for (AGVIOTaskItem nonStandardItem : nonStandardItems) {
				if (goodsLocations.isEmpty()) {
					return;
				}
				// 5. 发送LS指令
				Integer boxId = taskItemBoxMap.get(nonStandardItem.getId());
				if (boxId == null || boxId == 0) {
					continue;
				}
				MaterialBox materialBox = MaterialBox.dao.findById(boxId);
				sendTaskMoveBoxOrder(nonStandardItem, task, goodsLocations.get(0), materialBox, windowSize, i);
				goodsLocations.remove(0);
				if (i > 0) {
					i--;
				}
			}
		}
		for (AGVIOTaskItem standardItem : standardItems) {
			if (goodsLocations.isEmpty()) {
				return;
			}
			// 5. 发送LS指令
			Integer boxId = taskItemBoxMap.get(standardItem.getId());
			if (boxId == null || boxId == 0) {
				continue;
			}
			MaterialBox materialBox = MaterialBox.dao.findById(boxId);
			sendTaskMoveBoxOrder(standardItem, task, goodsLocations.get(0), materialBox, windowSize, i);
			goodsLocations.remove(0);
			if (i > 0) {
				i--;
			}
		}
	}


	public void sendInTaskItemCmds(Task task, Window window) throws Exception {
		List<AGVIOTaskItem> ioTaskItems = IOTaskItemRedisDAO.getIOTaskItems(task.getId());
		int windowSize = window.getSize();
		if (ioTaskItems.isEmpty()) {
			return;
		}
		List<GoodsLocation> goodsLocations = getEmptyGoodsLocations(window);
		int i = goodsLocations.size();
		for (AGVIOTaskItem item : ioTaskItems) {
			if (item.getState().intValue() != TaskItemState.WAIT_ASSIGN) {
				continue;
			}
			int boxId = getMaximumCapacityBox(item);
			if (boxId == 0) {
				continue;
			}
			distributeBoxToItem(item, boxId, task);
			MaterialBox materialBox = MaterialBox.dao.findById(boxId);
			if (item.getBoxId().intValue() == boxId && materialBox.getIsOnShelf() && !goodsLocations.isEmpty()) {
				// 5. 发送LS指令
				sendTaskMoveBoxOrder(item, task, goodsLocations.get(0), materialBox, windowSize, i);
				goodsLocations.remove(0);
				if (i > 0) {
					i--;
				}
			}
		}
	}


	public void sendInventoryTaskItemCmds(Task task, Window window) throws Exception {

		List<AGVInventoryTaskItem> agvInventoryTaskItems = InventoryTaskItemRedisDAO.getInventoryTaskItems(task.getId());
		int windowSize = window.getSize();
		if (agvInventoryTaskItems.isEmpty()) {
			return;
		}
		List<GoodsLocation> goodsLocations = getEmptyGoodsLocations(window);
		int i = goodsLocations.size();
		if (i == 0) {
			return;
		}
		List<AGVInventoryTaskItem> presentItems = new ArrayList<AGVInventoryTaskItem>();
		if (!window.getAuto()) {
			// 不使用机械臂的仓口
			// 优先发送非标准料盒，标准料盒条目作为备用
			List<AGVInventoryTaskItem> backupItems = new ArrayList<AGVInventoryTaskItem>();
			for (AGVInventoryTaskItem item : agvInventoryTaskItems) {
				// 取够非标准料盒条目时停止（非标准料盒条目==空闲货位数）
				if (presentItems.size() > i) {
					break;
				}
				if (item.getBoxType().equals(MaterialBoxType.NONSTANDARD) && item.getState().intValue() == TaskItemState.WAIT_ASSIGN) {
					presentItems.add(item);
				} else if (item.getBoxType().equals(MaterialBoxType.STANDARD) && item.getState().intValue() == TaskItemState.WAIT_ASSIGN) {
					backupItems.add(item);
					continue;
				}
			}
			if (!backupItems.isEmpty()) {
				presentItems.addAll(backupItems);
			}

		} else {
			// 使用机械臂盘点
			for (AGVInventoryTaskItem item : agvInventoryTaskItems) {
				// 取够非标准料盒条目时停止（非标准料盒条目==空闲货位数）
				if (presentItems.size() > i) {
					break;
				}
				if (item.getBoxType().equals(MaterialBoxType.STANDARD) && item.getState().intValue() == TaskItemState.WAIT_ASSIGN) {
					presentItems.add(item);
				}
			}
		}
		for (AGVInventoryTaskItem presentItem : presentItems) {
			if (goodsLocations.isEmpty()) {
				return;
			}
			MaterialBox materialBox = MaterialBox.dao.findById(presentItem.getBoxId());
			if (materialBox.getIsOnShelf()) {
				sendTaskMoveBoxOrder(presentItem, task, goodsLocations.get(0), materialBox, windowSize, i);
				goodsLocations.remove(0);
				if (i > 0) {
					i--;
				}
			}

		}

	}


	public void sendSampleTaskItemCmds(Task task, Window window) throws Exception {

		List<AGVSampleTaskItem> agvSampleTaskItems = SampleTaskItemRedisDAO.getSampleTaskItems(task.getId());
		int i = 0;
		int windowSize = window.getSize();
		if (!agvSampleTaskItems.isEmpty()) {
			List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, window.getId());
			if (goodsLocations.isEmpty()) {
				return;
			}
			Iterator<GoodsLocation> iterator = goodsLocations.iterator();
			while (iterator.hasNext()) {
				GoodsLocation goodsLocation = iterator.next();
				if (TaskPropertyRedisDAO.getLocationStatus(window.getId(), goodsLocation.getId()) != null && !TaskPropertyRedisDAO.getLocationStatus(window.getId(), goodsLocation.getId()).equals(0)) {
					iterator.remove();
					continue;
				}
			}
			i = goodsLocations.size();
			for (AGVSampleTaskItem item : agvSampleTaskItems) {
				if (goodsLocations.isEmpty()) {
					return;
				}
				MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
				if (item.getState().intValue() == TaskItemState.WAIT_ASSIGN) {
					if (materialBox.getIsOnShelf()) {
						sendTaskMoveBoxOrder(item, task, goodsLocations.get(0), materialBox, windowSize, i);
						goodsLocations.remove(0);
						if (i > 0) {
							i--;
						}
					}
				}

			}
		}

	}


	public void sendBuildCmds(int cn, List<AGVBuildTaskItem> buildTaskItems) throws Exception {
		// 获取第a个元素
		int a = 0;
		do {
			// 获取对应item
			AGVBuildTaskItem item = buildTaskItems.get(a);

			// 判断任务条目状态是否为0
			if (item.getState().intValue() == BuildTaskItemState.WAIT_MOVE) {

				MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
				// 判断料盒是否不在架(即该料盒还未入仓)
				if (!materialBox.getIsOnShelf()) {
					// 发送建仓指令
					BuildHandler.sendBuildCmd(item, materialBox);
					cn--;
				}
			}

			a++;
		} while (cn != 0 && a != buildTaskItems.size());
	}


	private static List<RobotBO> countFreeRobot() {
		List<RobotBO> freeRobots = new ArrayList<>();
		for (RobotBO robot : RobotInfoRedisDAO.check()) {
			// 筛选空闲或充电状态的处于启用中的叉车
			if ((robot.getStatus() == 0 || robot.getStatus() == 4) && robot.getEnabled() == 2) {

				freeRobots.add(robot);
			}
		}
		return freeRobots;
	}


	private static int getMaximumCapacityBox(AGVIOTaskItem item) {
		int boxId = 0;
		int boxRemainderCapacity = 0;
		AGVIOTaskItem taskItem = IOTaskItemRedisDAO.getIOTaskItem(item.getTaskId(), item.getId());
		if (taskItem == null) {
			return boxId;
		}
		if (taskItem.getState() >= TaskItemState.ASSIGNED && taskItem.getBoxId() != 0) {
			return taskItem.getBoxId();
		}
		MaterialType materialType = MaterialType.dao.findById(item.getMaterialTypeId());
		Set<Integer> diffTaskBoxs = new HashSet<>();
		List<Window> windows = Window.dao.find(GET_WORKING_WINDOWS);
		// 获取其他任务分配的料盒
		for (Window window : windows) {
			if (window.getBindTaskId().equals(item.getTaskId())) {
				continue;
			}
			for (AGVIOTaskItem redisTaskItem : IOTaskItemRedisDAO.getIOTaskItems(window.getBindTaskId())) {
				if (redisTaskItem.getState() > TaskItemState.WAIT_ASSIGN && redisTaskItem.getIsForceFinish().equals(false)) {
					diffTaskBoxs.add(redisTaskItem.getBoxId().intValue());
				}
			}
		}
		if (materialType.getRadius().equals(7)) {
			List<Material> sameTypeMaterialBoxList = Material.dao.find(GET_STANDARD_SAME_TYPE_MATERIAL_BOX_SQL, item.getMaterialTypeId(), materialType.getSupplier(), 1);
			// 获取料盒容量
			int materialBoxCapacity = PropKit.use("properties.ini").getInt("materialBoxCapacity");
			// 如果存在同类型的料盒
			if (sameTypeMaterialBoxList.size() > 0) {
				for (Material sameTypeMaterialBox : sameTypeMaterialBoxList) {
					if (diffTaskBoxs.contains(sameTypeMaterialBox.getBox().intValue())) {
						continue;
					}
					int usedcapacity = Material.dao.find(GET_MATERIAL_BOX_USED_CAPACITY_SQL, sameTypeMaterialBox.get("box").toString()).size();
					int unusedcapacity = materialBoxCapacity - usedcapacity;
					if (unusedcapacity > boxRemainderCapacity) {
						boxRemainderCapacity = unusedcapacity;
						boxId = sameTypeMaterialBox.get("box");
					}
				}
			}

			// 如果不存在同类型的料盒或者同类型的料盒都已装满
			if (sameTypeMaterialBoxList.size() == 0 || boxRemainderCapacity == 0) {
				for (AGVIOTaskItem redisTaskItem : IOTaskItemRedisDAO.getIOTaskItems(item.getTaskId())) {
					if (redisTaskItem.getState() == TaskItemState.ARRIVED_WINDOW) {
						int tempBoxId = redisTaskItem.getBoxId();
						MaterialBox materialBox = MaterialBox.dao.findById(tempBoxId);
						if (materialBox == null || !materialBox.getType().equals(1)) {
							continue;
						}
						int usedcapacity = Material.dao.find(GET_MATERIAL_BOX_USED_CAPACITY_SQL, tempBoxId).size();
						int unusedcapacity = materialBoxCapacity - usedcapacity;
						if (unusedcapacity > 0) {
							boxId = tempBoxId;
							break;
						}
					}
				}
				if (boxId == 0) {
					List<MaterialBox> differentTypeMaterialBoxList = MaterialBox.dao.find(GET_DIFFERENT_TYPE_MATERIAL_BOX_SQL, item.getMaterialTypeId(), materialType.getSupplier(),
							materialType.getSupplier(), 1);

					for (MaterialBox differentTypeMaterialBox : differentTypeMaterialBoxList) {
						if (diffTaskBoxs.contains(differentTypeMaterialBox.getId().intValue())) {
							continue;
						}
						int usedcapacity = Material.dao.find(GET_MATERIAL_BOX_USED_CAPACITY_SQL, differentTypeMaterialBox.getId()).size();
						int unusedcapacity = materialBoxCapacity - usedcapacity;
						if (unusedcapacity > boxRemainderCapacity) {
							boxRemainderCapacity = unusedcapacity;
							boxId = differentTypeMaterialBox.getId();
						}
					}
				}
			}

		} else {
			// 非标准物料，即非7寸盘
			List<Material> sameUnfullTypeMaterialBoxs = Material.dao.find(GET_SAME_TYPE_MATERIAL_BOX_SQL, item.getMaterialTypeId(), materialType.getSupplier(), 2, BoxState.UNFULL);
			// 如果存在同类型不满的料盒
			for (Material sameUnfullTypeMaterialBox : sameUnfullTypeMaterialBoxs) {
				if (diffTaskBoxs.contains(sameUnfullTypeMaterialBox.getBox().intValue())) {
					continue;
				}
				boxId = sameUnfullTypeMaterialBox.getBox();

			}
			// 如果不存在同类型的料盒或者同类型的料盒都已装满
			if (boxId == 0) {
				// 批量入库推送仓口
				for (AGVIOTaskItem redisTaskItem : IOTaskItemRedisDAO.getIOTaskItems(item.getTaskId())) {
					if (redisTaskItem.getState() == TaskItemState.ARRIVED_WINDOW) {
						int tempBoxId = redisTaskItem.getBoxId();
						MaterialBox materialBox = MaterialBox.dao.findById(tempBoxId);
						if (materialBox.getType().equals(2) && materialBox.getStatus() != BoxState.FULL) {
							boxId = tempBoxId;
							break;
						}
					}
				}
				// 如果存在同类型空的料盒
				if (boxId == 0) {
					List<Material> sameEmptyTypeMaterialBoxs = Material.dao.find(GET_SAME_TYPE_MATERIAL_BOX_SQL, item.getMaterialTypeId(), materialType.getSupplier(), 2, BoxState.EMPTY);
					for (Material sameEmptyTypeMaterialBox : sameEmptyTypeMaterialBoxs) {
						if (diffTaskBoxs.contains(sameEmptyTypeMaterialBox.getBox().intValue())) {
							continue;
						}
						boxId = sameEmptyTypeMaterialBox.getBox();
					}
				}
				if (boxId == 0) {
					// 推送不同类型空料盒
					List<MaterialBox> differentTypeMaterialBoxList = MaterialBox.dao.find(GET_EMPTY_DIFFERENT_TYPE_MATERIAL_BOX_SQL, item.getMaterialTypeId(), materialType.getSupplier(),
							materialType.getSupplier(), 2, BoxState.EMPTY);
					for (MaterialBox differentTypeMaterialBox : differentTypeMaterialBoxList) {
						if (diffTaskBoxs.contains(differentTypeMaterialBox.getId().intValue())) {
							continue;
						}
						boxId = differentTypeMaterialBox.getId();
					}
				}
				if (boxId == 0) {
					// 推送不同类型非空料盒并按照出库时间排序
					List<MaterialBox> differentTypeMaterialBoxList = MaterialBox.dao.find(GET_UNFULL_DIFFERENT_TYPE_MATERIAL_BOX_SQL, item.getMaterialTypeId(), materialType.getSupplier(),
							materialType.getSupplier(), 2, BoxState.UNFULL);
					for (MaterialBox differentTypeMaterialBox : differentTypeMaterialBoxList) {
						if (diffTaskBoxs.contains(differentTypeMaterialBox.getId().intValue())) {
							continue;
						}
						boxId = differentTypeMaterialBox.getId();
					}
				}
			}
		}

		return boxId;
	}


	private static int getOldestMaterialBox(AGVIOTaskItem item) {
		int boxId = 0;
		AGVIOTaskItem redisTaskItem = IOTaskItemRedisDAO.getIOTaskItem(item.getTaskId(), item.getId());
		if ((redisTaskItem.getIsForceFinish() || redisTaskItem.getState() > TaskItemState.WAIT_ASSIGN) && redisTaskItem.getBoxId() != 0) {
			return redisTaskItem.getBoxId();
		}

		Material material = Material.dao.findFirst(GET_IN_BOX_MATERIAL_BY_TYPE_SQL, item.getMaterialTypeId());
		if (material != null) {
			MaterialBox materialBox = MaterialBox.dao.findById(material.getBox());
			if (materialBox.getIsOnShelf()) {
				boxId = material.getBox();
			} else {
				List<MaterialBox> materialBoxs = MaterialBox.dao.find(GET_MATERIALBOX_BY_TYPE_AND_STORETIME_SQL, item.getMaterialTypeId(), material.getProductionTime(), material.getBox());
				if (!materialBoxs.isEmpty()) {
					for (MaterialBox materialBox2 : materialBoxs) {
						if (materialBox2.getIsOnShelf()) {
							boxId = materialBox2.getId();
							break;
						}
					}
				}
				if (boxId == 0) {
					boxId = material.getBox();
				}
			}

		}

		return boxId;
	}


	private void sendTaskMoveBoxOrder(BaseTaskItem item, Task task, GoodsLocation goodsLocation, MaterialBox materialBox, Integer windowSize, Integer emptyGoodsLocationSize) throws Exception {
		BaseTaskHandler baseTaskHandler = null;
		if (item instanceof AGVSampleTaskItem) {
			baseTaskHandler = samTaskHandler;
		} else if (item instanceof AGVInventoryTaskItem) {
			baseTaskHandler = invTaskHandler;
		} else if (item instanceof AGVIOTaskItem) {
			baseTaskHandler = ioTaskHandler;
		}
		if (task.getPriority().equals(0)) {
			baseTaskHandler.sendSendLL(item, materialBox, goodsLocation, task.getPriority());
		} else {
			if (emptyGoodsLocationSize > Math.floor(windowSize / 2)) {
				baseTaskHandler.sendSendLL(item, materialBox, goodsLocation, task.getPriority());
			} else {
				baseTaskHandler.sendSendLL(item, materialBox, goodsLocation, task.getPriority() + 1);
			}
		}
	}


	private List<GoodsLocation> getEmptyGoodsLocations(Window window) {
		List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, window.getId());
		if (goodsLocations.isEmpty()) {
			return Collections.emptyList();
		}
		Iterator<GoodsLocation> iterator = goodsLocations.iterator();
		while (iterator.hasNext()) {
			GoodsLocation goodsLocation = iterator.next();
			if (TaskPropertyRedisDAO.getLocationStatus(window.getId(), goodsLocation.getId()) != null && !TaskPropertyRedisDAO.getLocationStatus(window.getId(), goodsLocation.getId()).equals(0)) {
				iterator.remove();
			}
		}
		return goodsLocations;
	}


	private Boolean deductOutTaskQauntity(AGVIOTaskItem item, Task task, Task inventoryTask) {
		Integer eWhStoreQuantity = externalWhLogService.getEwhMaterialQuantityByOutTask(task, inventoryTask, item.getMaterialTypeId(), task.getDestination());
		Integer outQuantity = taskService.getActualIOQuantity(item.getId());
		if (outQuantity == 0 && (eWhStoreQuantity - item.getPlanQuantity()) >= 5000) {
			final Date time = inventoryTask == null ? new Date() : inventoryTask.getCreateTime();
			UwProcessorExcutor.me.execute(new Runnable() {

				@Override
				public void run() {
					ExternalWhLog externalWhLog = new ExternalWhLog();
					externalWhLog.setMaterialTypeId(item.getMaterialTypeId());
					externalWhLog.setDestination(task.getDestination());
					externalWhLog.setSourceWh(UW_ID);
					externalWhLog.setTaskId(task.getId());
					externalWhLog.setQuantity(0 - item.getPlanQuantity());
					externalWhLog.setOperatior("robot1");
					externalWhLog.setTime(time);
					externalWhLog.setOperationTime(new Date());
					externalWhLog.save();

					PackingListItem packingListItem = PackingListItem.dao.findById(item.getId());
					packingListItem.setFinishTime(new Date()).update();
				}
			});

			synchronized (Lock.IO_TASK_REDIS_LOCK) {
				AGVIOTaskItem tempItem = IOTaskItemRedisDAO.getIOTaskItem(task.getId(), item.getId());
				if (tempItem.getState() <= TaskItemState.WAIT_ASSIGN) {
					IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.FINISH_BACK, 0, 0, 0, 0, true, false, 0, null, 0 - item.getPlanQuantity());
				}
			}
			ioTaskHandler.clearTask(task.getId(), false);
			return true;
		}
		return false;
	}


	private boolean checkAndHandleLackOutTaskItem(AGVIOTaskItem item, Task task) {
		Integer remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(item.getMaterialTypeId());
		if (remainderQuantity == 0 && !item.getIsForceFinish().equals(true)) {
			IOTaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.LACK, null, null, null, null, true, null, null, null, null);
			// 为将该出库日志关联到对应的物料，需要查找对应的料盘唯一码，因为出库数是设置为0的，所以不会影响系统数据
			taskService.createTaskLog(item.getId(), null, 0, null, task);
			ioTaskHandler.clearTask(task.getId(), false);
			return true;
		}
		return false;
	}


	/**
	 * 
	 * <p>
	 * Description: 分配料盒到redis任务条目上
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月30日
	 */
	private void distributeBoxToItem(AGVIOTaskItem item, Integer boxId, Task task) {
		if (item.getBoxId().equals(0)) {
			IOTaskItemRedisDAO.updateIOTaskItemInfo(item, null, null, null, boxId, 0, null, null, null, null, null);
			item.setBoxId(boxId);
		} else if (!item.getBoxId().equals(0) && boxId != item.getBoxId().intValue()) {
			// 防止任务条目在叉车回库已经分配了到站的料盒，导致再次修改料盒
			synchronized (Lock.IO_TASK_REDIS_LOCK) {
				AGVIOTaskItem tempItem = IOTaskItemRedisDAO.getIOTaskItem(task.getId(), item.getId());
				if (tempItem.getState() <= TaskItemState.WAIT_ASSIGN) {
					IOTaskItemRedisDAO.updateIOTaskItemInfo(item, null, null, null, boxId, 0, null, null, null, null, null);
					item.setBoxId(boxId);
				}
			}
		}
	}


	/**
	 * <p>
	 * Description: 筛选可用的机械臂出库任务条目
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月30日
	 */
	private void sieveAutoOutTaskItem(AGVIOTaskItem item, MaterialBox materialBox, Window window, List<AGVIOTaskItem> standardItems, Map<Integer, Integer> taskTtemBoxMap) {
		if (taskTtemBoxMap.containsValue(item.getBoxId())) {
			return;
		}
		if (materialBox.getType().equals(MaterialBoxType.STANDARD) && item.getIsSuperable() && (item.getOldWindowId() == 0 || window.getId().equals(item.getOldWindowId()))) {
			standardItems.add(item);
			taskTtemBoxMap.put(item.getId(), item.getBoxId());
		}
	}


	/**
	 * <p>
	 * Description: 筛选可用的人工出库任务条目
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月30日
	 */
	private void sieveManualOutTaskItem(AGVIOTaskItem item, MaterialBox materialBox, Window window, List<AGVIOTaskItem> standardItems, List<AGVIOTaskItem> nonStandardItems,
			Map<Integer, Integer> taskTtemBoxMap) {
		if (taskTtemBoxMap.containsValue(item.getBoxId())) {
			return;
		}
		if (materialBox.getType().equals(MaterialBoxType.NONSTANDARD)) {
			nonStandardItems.add(item);
			taskTtemBoxMap.put(item.getId(), item.getBoxId());
		} else if (item.getOldWindowId() == 0 || window.getId().equals(item.getOldWindowId())) {
			if (taskTtemBoxMap.containsValue(item.getBoxId())) {
				return;
			}
			standardItems.add(item);
			taskTtemBoxMap.put(item.getId(), item.getBoxId());
		}
	}
}
