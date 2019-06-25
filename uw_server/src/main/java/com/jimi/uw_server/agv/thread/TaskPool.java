package com.jimi.uw_server.agv.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jfinal.aop.Enhancer;
import com.jfinal.kit.PropKit;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVBuildTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.agv.handle.BuildHandler;
import com.jimi.uw_server.agv.handle.IOHandler;
import com.jimi.uw_server.constant.BoxState;
import com.jimi.uw_server.constant.BuildTaskItemState;
import com.jimi.uw_server.constant.IOTaskItemState;
import com.jimi.uw_server.constant.InventoryTaskItemState;
import com.jimi.uw_server.constant.SampleTaskItemState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.RobotBO;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.util.ErrorLogWritter;

/**
 * 任务池，负责分配任务
 * <br>
 * <b>2018年7月13日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class TaskPool extends Thread{

	private static MaterialService materialService = Enhancer.enhance(MaterialService.class);
	
	private static final String GET_STANDARD_SAME_TYPE_MATERIAL_BOX_SQL = "SELECT material_box.id AS box FROM material_box INNER JOIN material ON material.box = material_box.id WHERE material.type = ? AND material_box.supplier = ? AND material_box.type = ?";
	
	private static final String GET_SAME_TYPE_MATERIAL_BOX_SQL = "SELECT material_box.id AS box FROM material_box INNER JOIN material ON material.box = material_box.id WHERE material.type = ? AND material_box.supplier = ? AND material_box.type = ? AND material_box.status = ?";

	private static final String GET_MATERIAL_BOX_USED_CAPACITY_SQL = "SELECT * FROM material WHERE box = ? AND remainder_quantity > 0";

	private static final String GET_DIFFERENT_TYPE_MATERIAL_BOX_SQL = "SELECT * FROM material_box WHERE enabled = 1 AND id NOT IN (SELECT material_box.id AS boxId FROM material_box INNER JOIN material ON material.box = material_box.id WHERE material.type = ? AND material_box.supplier = ?) AND supplier = ? AND material_box.type = ?";
	
	private static final String GET_EMPTY_DIFFERENT_TYPE_MATERIAL_BOX_SQL = "SELECT * FROM material_box WHERE enabled = 1 AND id NOT IN (SELECT material_box.id AS boxId FROM material_box INNER JOIN material ON material.box = material_box.id WHERE material.type = ? AND material_box.supplier = ?) AND supplier = ? AND material_box.type = ? AND material_box.status = ?";

	private static final String GET_UNFULL_DIFFERENT_TYPE_MATERIAL_BOX_SQL = "SELECT * FROM material_box WHERE enabled = 1 AND id NOT IN (SELECT material_box.id AS boxId FROM material_box INNER JOIN material ON material.box = material_box.id WHERE material.type = ? AND material_box.supplier = ?) AND supplier = ? AND material_box.type = ? AND material_box.status = ? ORDER BY update_time DESC ";

	private static final String GET_MATERIAL_BY_TYPE_SQL = "SELECT * FROM material WHERE type = ? AND remainder_quantity > 0";

	private static final String GET_IN_BOX_MATERIAL_BY_TYPE_SQL = "SELECT * FROM material WHERE remainder_quantity > 0 AND type = ? AND is_in_box = b'1' ORDER BY production_time ASC";

	private static final String GET_MATERIALBOX_BY_TYPE_AND_STORETIME_SQL = "SELECT DISTINCT material_box.* FROM material INNER JOIN material_box ON material.box = material_box.id WHERE material.remainder_quantity > 0 AND material.type = ? AND is_in_box = b'1' AND material.production_time = ? AND material.box != ?";
	
	private static final String GET_MATERIAL_BY_TYPE_AND_BOX_SQL = "SELECT * FROM material WHERE remainder_quantity > 0 AND type = ? AND box = ? AND is_in_box = b'1'";
	
	private static final String GET_WORKING_WINDOWS = "SELECT * FROM window WHERE bind_task_id IS NOT NULL";
	
	private static final String GET_WINDOWS_BY_TASKID = "SELECT * FROM window WHERE bind_task_id = ?";
	
	public static final String UNDEFINED = "undefined";
	
	@Override
	public void run() {
		int taskPoolCycle = PropKit.use("properties.ini").getInt("taskPoolCycle");
		System.out.println("TaskPool is running NOW...");
		while(true) {
			try {
				sleep(taskPoolCycle);
				if (!TaskItemRedisDAO.getAgvWebSocketStatus()) {
					continue;
				}
				//判断是否存在停止分配标志位
				List<RobotBO> robotBOs = countFreeRobot();
				//判断til是否为空或者cn为0
				if (!robotBOs.isEmpty()) {
					synchronized (Lock.IO_TASK_REDIS_LOCK) {
						sendIOCmds(robotBOs);
					}
				}
				robotBOs = countFreeRobot();
				synchronized (Lock.INVENTORY_REDIS_LOCK) {
					List<AGVInventoryTaskItem> inventoryTaskItems = new ArrayList<>();
					TaskItemRedisDAO.appendInventoryTaskItems(inventoryTaskItems);
					if (!inventoryTaskItems.isEmpty() && robotBOs.size() > 0) {
						sendInventoryTaskItemCmds(robotBOs, inventoryTaskItems);
					}
				}
				synchronized (Lock.SAMPLE_TASK_REDIS_LOCK) {
					List<AGVSampleTaskItem> agvSampleTaskItems = new ArrayList<>();
					TaskItemRedisDAO.appendSampleTaskItems(agvSampleTaskItems);
					if (!agvSampleTaskItems.isEmpty() && robotBOs.size() > 0) {
						sendSampleTaskItemCmds(robotBOs, agvSampleTaskItems);
					}
				}
				//判断tilOfBuild是否为空或者cn为0
				robotBOs = countFreeRobot();
				List<AGVBuildTaskItem> buildTaskItems = new ArrayList<>();
				TaskItemRedisDAO.appendBuildTaskItems(buildTaskItems);
				if (buildTaskItems.isEmpty() || robotBOs.size() == 0) {
					continue;
				}
				sendBuildCmds(robotBOs.size(), buildTaskItems);
			} catch (Exception e) {
				ErrorLogWritter.save(e.getClass().getSimpleName()+ ":" +e.getMessage());
				e.printStackTrace();
			}
		}
	}

	
	public static void sendIOCmds(List<RobotBO> robotBOs) throws Exception {
		List<Window> windows = Window.dao.find(GET_WORKING_WINDOWS);
		for (Window window : windows) {
			Task task = Task.dao.findById(window.getBindTaskId());
			//获取出入库任务
			if (task != null && (task.getType().equals(TaskType.IN) || task.getType().equals(TaskType.OUT) || task.getType().equals(TaskType.SEND_BACK))) {
				String robots = TaskItemRedisDAO.getWindowTaskInfo(window.getId(), task.getId());
				if (robots == null || robots.equals(UNDEFINED) || robots.trim().equals("")) {
					continue;
				}
				
				String[] robotIdArr = robots.split(",");
				for (String robotIdStr : robotIdArr) {
					Integer robotId = Integer.valueOf(robotIdStr.trim());
					for (RobotBO robotBO : robotBOs) {
						//仓口指定叉车和闲置叉车一致
						
						if (robotId.equals(robotBO.getId())) {
							List<AGVIOTaskItem> ioTaskItems = new ArrayList<>();
							TaskItemRedisDAO.appendIOTaskItems(task.getId(), ioTaskItems);
							if (!ioTaskItems.isEmpty()) {
								//获取第a个元素
								int a = 0;
								boolean flag = false;
								do {
									//获取对应item
									AGVIOTaskItem item = ioTaskItems.get(a);
									// 0. 判断任务条目状态是否为未分配
									if (item.getState().intValue() == IOTaskItemState.WAIT_ASSIGN) {
										Integer taskType = task.getType();
										Integer boxId = 0;
			
										// 对于入库和退料入库，根据类型和挑盒子算法获取最佳盒号
										if (taskType == TaskType.IN || taskType == TaskType.SEND_BACK) {
											boxId = getMaximumCapacityBox(item.getMaterialTypeId(),item.getTaskId());
										}
										// 对于出库， 根据类型获取最旧物料实体的盒号
										else if (taskType == TaskType.OUT) {
											// 对于出库任务，需要判断库存是否为0
			
											// 根据物料类型号获取物料库存数量，若库存数为0，则将任务条目状态设置为缺料并记录一条出库数为0的出库日志，然后跳出循环;否则，调用获取最旧物料算法
											Integer remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(item.getMaterialTypeId());
											if (remainderQuantity == 0 && !item.getIsForceFinish().equals(true)) {
												
												TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.LACK);
												TaskItemRedisDAO.updateTaskIsForceFinish(item, true);
												// 为将该出库日志关联到对应的物料，需要查找对应的料盘唯一码，因为出库数是设置为0的，所以不会影响系统数据
												TaskLog taskLog = new TaskLog();
												taskLog.setPackingListItemId(item.getId());
												taskLog.setMaterialId(null);
												taskLog.setQuantity(0);
												taskLog.setOperator(null);
												// 区分出库操作人工还是机器操作,目前的版本暂时先统一写成机器操作
												taskLog.setAuto(false);
												taskLog.setTime(new Date());
												taskLog.setDestination(task.getDestination());
												taskLog.save();
												IOHandler.clearIoTil(item.getGroupId());
												break;
											} else {
												boxId = getOldestMaterialBox(item.getMaterialTypeId(), item.getId());
											}
			
										}
										MaterialBox materialBox = MaterialBox.dao.findById(boxId);
										// 3. 将盒号填入item并update到Redis
										TaskItemRedisDAO.updateTaskItemBoxId(item, boxId);
										// 4. 判断任务条目的boxId是否已更新，同时判断料盒是否在架
										if (!flag && boxId > 0 && item.getBoxId().intValue() == boxId && materialBox.getIsOnShelf() && (TaskItemRedisDAO.getRobotOrder(robotId) == null || TaskItemRedisDAO.getRobotOrder(robotId).equals(UNDEFINED))) {
											// 5. 发送LS指令
											
											IOHandler.sendLS(item, materialBox, robotId);
											flag = true;
										}
									} else if (item.getState().intValue() == IOTaskItemState.LACK) {	// 对于缺料的任务条目，若对应的物料已经补完库且该任务未结束，则将对应的任务条目更新为“等待分配”
										// 根据物料类型号获取物料库存数量
										Integer remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(item.getMaterialTypeId());
										if (remainderQuantity > 0) {
											TaskItemRedisDAO.updateTaskIsForceFinish(item, false);
											TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.WAIT_ASSIGN);
										}
									}
			
									a++;
								} while(a != ioTaskItems.size());
							}
						}
						
					}
				}
				
			}
		}
		
		
	}

	
	public static void sendInventoryTaskItemCmds(List<RobotBO> robotBOs, List<AGVInventoryTaskItem> inventoryTaskItems) throws Exception {
		//获取第a个元素
		int a = 0;
		do {
			//获取对应item
			AGVInventoryTaskItem item = inventoryTaskItems.get(a);
			Boolean flag = false;
			// 判断任务条目状态是否为0
			if (item.getState().intValue() == InventoryTaskItemState.WAIT_ASSIGN) {

				List<Window> windows = Window.dao.find(GET_WINDOWS_BY_TASKID, item.getTaskId());
				if (windows.size() == 0) {
					continue;
				}
				MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
				// 判断料盒是否在架
				for (Window window : windows) {
					if (flag.equals(true)) {
						break;
					}
					String robots = TaskItemRedisDAO.getWindowTaskInfo(window.getId(), window.getBindTaskId());
					if (robots != null && !robots.equals(UNDEFINED)) {
						String[] robotArr = robots.split(",");
						for (String string : robotArr) {
							if (flag.equals(true)) {
								break;
							}
							for (RobotBO robotBO : robotBOs) {
								if (robotBO.getId().equals(Integer.parseInt(string))) {
									if (TaskItemRedisDAO.getRobotOrder(robotBO.getId()) != null && !TaskItemRedisDAO.getRobotOrder(robotBO.getId()).equals(UNDEFINED)) {
										continue;
									}
									if (materialBox.getIsOnShelf()) {
										IOHandler.sendLS(item, materialBox, window, robotBO.getId());
										flag = true;
										break;
									}
								}
								
							}
						}
					}
				}
				
				
			}
			a++;
		} while( a != inventoryTaskItems.size());
	}
	

	public static void sendSampleTaskItemCmds(List<RobotBO> robotBOs, List<AGVSampleTaskItem> sampleTaskItems) throws Exception {
		//获取第a个元素
		int a = 0;
		do {
			//获取对应item
			AGVSampleTaskItem item = sampleTaskItems.get(a);
			Boolean flag = false;
			// 判断任务条目状态是否为0
			if (item.getState().intValue() == SampleTaskItemState.WAIT_ASSIGN) {

				List<Window> windows = Window.dao.find(GET_WINDOWS_BY_TASKID, item.getTaskId());
				if (windows.size() == 0) {
					continue;
				}
				MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
				// 判断料盒是否在架
				for (Window window : windows) {
					if (flag.equals(true)) {
						break;
					}
					String robots = TaskItemRedisDAO.getWindowTaskInfo(window.getId(), window.getBindTaskId());
					if (robots != null && !robots.equals(UNDEFINED)) {
						String[] robotArr = robots.split(",");
						for (String string : robotArr) {
							if (flag.equals(true)) {
								break;
							}
							for (RobotBO robotBO : robotBOs) {
								if (robotBO.getId().equals(Integer.parseInt(string))) {
									if (TaskItemRedisDAO.getRobotOrder(robotBO.getId()) != null && !TaskItemRedisDAO.getRobotOrder(robotBO.getId()).equals(UNDEFINED)) {
										continue;
									}
									if (materialBox.getIsOnShelf()) {
										IOHandler.sendLS(item, materialBox, window, robotBO.getId());
										flag = true;
										break;
									}
								}
								
							}
						}
					}
				}
				
				
			}
			a++;
		} while( a != sampleTaskItems.size());
	}
	
	
	public static void sendBuildCmds(int cn, List<AGVBuildTaskItem> buildTaskItems) throws Exception {
		//获取第a个元素
		int a = 0;
		do {
			//获取对应item
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
		} while(cn != 0 && a != buildTaskItems.size());
	}


	private static List<RobotBO> countFreeRobot() {
		List<RobotBO> freeRobots = new ArrayList<>();
		for (RobotBO robot : RobotInfoRedisDAO.check()) {
			//筛选空闲或充电状态的处于启用中的叉车
			if((robot.getStatus() == 0 || robot.getStatus() == 4) && robot.getEnabled() == 2) {
				
				freeRobots.add(robot);
			}
		}
		return freeRobots;
	}


	private static int getMaximumCapacityBox(Integer materialTypeId, Integer taskId) {
		MaterialType materialType = MaterialType.dao.findById(materialTypeId);
		
		int boxId = 0;
		int boxRemainderCapacity = 0;
		Set<Integer> diffTaskBoxs = new HashSet<>();
		List<Window> windows = Window.dao.find(GET_WORKING_WINDOWS);
		for (Window window : windows) {
			if (window.getBindTaskId().equals(taskId)) {
				continue;
			}
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(window.getBindTaskId())) {
				if (redisTaskItem.getState() > IOTaskItemState.WAIT_ASSIGN && redisTaskItem.getIsForceFinish().equals(false)) {
					diffTaskBoxs.add(redisTaskItem.getBoxId().intValue());
				}
			}
		}
		for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(taskId)) {
			
			if (redisTaskItem.getTaskId().equals(taskId) && redisTaskItem.getState() >= IOTaskItemState.ASSIGNED && redisTaskItem.getMaterialTypeId().equals(materialTypeId)) {
				return redisTaskItem.getBoxId();
			}
		}
		if (materialType.getRadius().equals(7)) {
			List<Material> sameTypeMaterialBoxList = Material.dao.find(GET_STANDARD_SAME_TYPE_MATERIAL_BOX_SQL, materialTypeId, materialType.getSupplier(), 1);
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
				for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(taskId)) {
					if (redisTaskItem.getState() == IOTaskItemState.ARRIVED_WINDOW) {
						int tempBoxId = redisTaskItem.getBoxId();
						MaterialBox materialBox = MaterialBox.dao.findById(tempBoxId);
						if (materialBox == null || !materialBox.getType().equals(1)) {
							break;
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
					List<MaterialBox> differentTypeMaterialBoxList = MaterialBox.dao.find(GET_DIFFERENT_TYPE_MATERIAL_BOX_SQL, materialTypeId, materialType.getSupplier(), materialType.getSupplier(), 1);
					
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
			
		}else {
			//非标准物料，即非7寸盘
			List<Material> sameUnfullTypeMaterialBoxs = Material.dao.find(GET_SAME_TYPE_MATERIAL_BOX_SQL, materialTypeId, materialType.getSupplier(), 2, BoxState.UNFULL);
			// 如果存在同类型不满的料盒
			for (Material sameUnfullTypeMaterialBox : sameUnfullTypeMaterialBoxs) {
				if (diffTaskBoxs.contains(sameUnfullTypeMaterialBox.getBox().intValue())) {
					continue;
				}
				boxId = sameUnfullTypeMaterialBox.getBox();
				
			}
			// 如果不存在同类型的料盒或者同类型的料盒都已装满
			if (boxId == 0) {
				//批量入库推送仓口
				for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(taskId)) {
					if (redisTaskItem.getState() == IOTaskItemState.ARRIVED_WINDOW) {
						int tempBoxId = redisTaskItem.getBoxId();
						MaterialBox materialBox = MaterialBox.dao.findById(tempBoxId);
						if (materialBox.getType().equals(2) && materialBox.getStatus() != BoxState.FULL) {
							boxId = tempBoxId;
						}
					}
				}
				// 如果存在同类型空的料盒
				if (boxId == 0) {
					List<Material> sameEmptyTypeMaterialBoxs = Material.dao.find(GET_SAME_TYPE_MATERIAL_BOX_SQL, materialTypeId, materialType.getSupplier(), 2, BoxState.EMPTY);
					for (Material sameEmptyTypeMaterialBox : sameEmptyTypeMaterialBoxs) {
						if (diffTaskBoxs.contains(sameEmptyTypeMaterialBox.getBox().intValue())) {
							continue;
						}
						boxId = sameEmptyTypeMaterialBox.getBox();
					}
				}
				
				if (boxId == 0) {
					//推送不同类型空料盒
					List<MaterialBox> differentTypeMaterialBoxList = MaterialBox.dao.find(GET_EMPTY_DIFFERENT_TYPE_MATERIAL_BOX_SQL, materialTypeId, materialType.getSupplier(), materialType.getSupplier(), 2, BoxState.EMPTY);
					
					for (MaterialBox differentTypeMaterialBox : differentTypeMaterialBoxList) {
						if (diffTaskBoxs.contains(differentTypeMaterialBox.getId().intValue())) {
							continue;
						}
						boxId = differentTypeMaterialBox.getId();
						
					}
				}
				if (boxId == 0) {
					//推送不同类型非空料盒并按照出库时间排序
					List<MaterialBox> differentTypeMaterialBoxList = MaterialBox.dao.find(GET_UNFULL_DIFFERENT_TYPE_MATERIAL_BOX_SQL, materialTypeId, materialType.getSupplier(), materialType.getSupplier(), 2, BoxState.UNFULL);
					
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


	private static int getOldestMaterialBox(Integer materialTypeId, Integer packingListItemId) {
		//List<Material> materialList = Material.dao.find(GET_MATERIAL_BY_TYPE_SQL, materialTypeId);
		int boxId = 0;
		Date productionTime = new Date();
		Set<Integer> boxs = new HashSet<>();
		PackingListItem packingListItem = PackingListItem.dao.findById(packingListItemId);
		/*List<Window> windows = Window.dao.find(GET_WORKING_WINDOWS);
		for (Window window : windows) {
			if (window.getBindTaskId().equals(packingListItem.getTaskId())) {
				continue;
			}
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(window.getBindTaskId())) {
				if (redisTaskItem.getState() > IOTaskItemState.WAIT_ASSIGN && redisTaskItem.getIsForceFinish().equals(false)) {
					boxs.add(redisTaskItem.getBoxId().intValue());
				}
			}
		}*/
		for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(packingListItem.getTaskId())) {
			// 根据任务条目id匹配到redis中的任务条目
			if ((redisTaskItem.getId().equals(packingListItem.getId()) && redisTaskItem.getIsForceFinish())) {
				if (redisTaskItem.getBoxId() != 0) {
					return redisTaskItem.getBoxId();
				}
			}
		}
		/*// 如果物料实体表中有多条该物料类型的记录，且库存大于0
		if (materialList.size() > 0) {
			for (Material m : materialList) {
				
				if (m.getProductionTime().before(productionTime) && (m.getIsInBox() || Material.dao.find(GET_MATERIAL_BY_TYPE_AND_BOX_SQL, materialTypeId, m.getBox()).size() > 0)) {

					if (boxs.contains(m.getBox().intValue())) {
						continue;
					}
					productionTime = m.getProductionTime();
					boxId = m.getBox();
				}
				
			}
		}*/
		Material material = Material.dao.findFirst(GET_IN_BOX_MATERIAL_BY_TYPE_SQL, materialTypeId);
		if (material != null) {
			MaterialBox materialBox = MaterialBox.dao.findById(material.getBox());
			if (materialBox.getIsOnShelf()) {
				boxId = material.getBox();
			}else {
				List<MaterialBox> materialBoxs = MaterialBox.dao.find(GET_MATERIALBOX_BY_TYPE_AND_STORETIME_SQL, materialTypeId, material.getProductionTime(), material.getBox());
				if (!materialBoxs.isEmpty()) {
					for (MaterialBox materialBox2 : materialBoxs) {
						if (materialBox2.getIsOnShelf()) {
							boxId = materialBox2.getId();
							break;
						}
					}
				}else {
					boxId = material.getBox();
				}
				
			}
			
		}

		return boxId;
	}

}
