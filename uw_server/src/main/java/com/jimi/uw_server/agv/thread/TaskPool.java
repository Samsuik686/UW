package com.jimi.uw_server.agv.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.kit.PropKit;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.handle.LSSLHandler;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.bo.RobotBO;
import com.jimi.uw_server.util.ErrorLogWritter;

/**
 * 任务池，负责分配任务
 * <br>
 * <b>2018年7月13日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class TaskPool extends Thread{

	private static final String GET_SAME_TYPE_MATERIAL_BOX_SQL = "SELECT DISTINCT(box) AS boxId FROM material WHERE type = ? AND remainder_quantity > 0";

	private static final String GET_MATERIAL_BOX_USED_CAPACITY_SQL = "SELECT * FROM material WHERE box = ? AND remainder_quantity > 0";

	private static final String GET_DIFFERENT_TYPE_MATERIAL_BOX_SQL = "SELECT * FROM material_box WHERE enabled = 1 AND id NOT IN (SELECT box FROM material WHERE type = ? AND remainder_quantity > 0)";

	private static final String GET_SURPLUS_MATERIAL_TYPE_MATERIAL_BOX_SQL = "SELECT * FROM material WHERE type = ? AND remainder_quantity > 0";


	@Override
	public void run() {
		int taskPoolCycle = PropKit.use("properties.ini").getInt("taskPoolCycle");
		System.out.println("TaskPool is running NOW...");
		while(true) {
			try {
				sleep(taskPoolCycle);
				//判断是否存在停止分配标志位
				if(TaskItemRedisDAO.isPauseAssign() == 1){
					continue;
				}

				//判断til是否为空或者cn为0
				int cn = countFreeRobot();
				List<AGVIOTaskItem> taskItems = new ArrayList<>();
				TaskItemRedisDAO.appendTaskItems(taskItems);
				if (taskItems.isEmpty() || cn == 0) {
					continue;
				}

				sendLSs(cn, taskItems);
			} catch (Exception e) {
				if(e instanceof InterruptedException) {
					break;
				}else {
					ErrorLogWritter.save(e.getClass().getSimpleName()+ ":" +e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}


	public static void sendLSs(int cn, List<AGVIOTaskItem> taskItems) throws Exception {
		//获取第a个元素
		int a = 0;
		do {
			//获取对应item
			AGVIOTaskItem item = taskItems.get(a);

			// 0. 判断任务条目状态是否为0
			if (item.getState() == 0) {
				// 1. 根据item的任务id获取任务类型
				Task task = Task.dao.findById(item.getTaskId());
				Integer taskType = task.getType();
				Integer boxId = 0;

				// 对于入库
					// 2. 根据类型和挑盒子算法获取最佳盒号
				if (taskType == 0) {
					boxId = getMaximumCapacityBox(item.getMaterialTypeId());
				}

				// 对于出库
					// 2. 根据类型获取最旧物料实体的盒号
				else if (taskType == 1) {
					boxId = getOldestMaterialBox(item.getMaterialTypeId());
				}

				MaterialBox materialBox = MaterialBox.dao.findById(boxId);
				// 3. 将盒号填入item并update到Redis
				TaskItemRedisDAO.updateTaskItemBoxId(item, boxId);
				// 4. 判断盒子是否在架
				// 若 boxId 为 0，则查询出来的 materialBox 为  null，如果还调用 materialBox.getIsOnShelf()，会出现空指针异常，需要避免这个错误
				if (materialBox != null && materialBox.getIsOnShelf()) {
					// 在架
					// 5. 发送LS指令
					LSSLHandler.sendLS(item, materialBox);
					cn--;
				}
			}

			a++;
		} while(cn != 0 && a != taskItems.size());
	}


	private static int countFreeRobot() {
		List<RobotBO> freeRobots = new ArrayList<>();
		for (RobotBO robot : RobotInfoRedisDAO.check()) {
			//筛选空闲或充电状态的处于启用中的叉车
			if((robot.getStatus() == 0 || robot.getStatus() == 4) && robot.getEnabled() == 2) {
				freeRobots.add(robot);
			}
		}
		return freeRobots.size();
	}


	private static int getMaximumCapacityBox(Integer materialTypeId) {
		List<Material> sameTypeMaterialBoxList = Material.dao.find(GET_SAME_TYPE_MATERIAL_BOX_SQL, materialTypeId);
		int boxId = 0;
		int boxRemainderCapacity = 0;
		// 如果存在同类型的料盒
		if (sameTypeMaterialBoxList.size() > 0) {
			// 获取料盒容量
			int materialBoxCapacity = PropKit.use("properties.ini").getInt("materialBoxCapacity");
			for (Material sameTypeMaterialBox : sameTypeMaterialBoxList) {
				int usedcapacity = Material.dao.find(GET_MATERIAL_BOX_USED_CAPACITY_SQL, sameTypeMaterialBox.get("boxId").toString()).size();
				int unusedcapacity = materialBoxCapacity - usedcapacity;
				if (unusedcapacity > boxRemainderCapacity) {
					boxRemainderCapacity = unusedcapacity;
					boxId = sameTypeMaterialBox.get("boxId");
				}
			}
		}

		// 如果不存在同类型的料盒或者同类型的料盒都已装满
		if (sameTypeMaterialBoxList.size() == 0 || boxRemainderCapacity == 0) {
			List<MaterialBox> differentTypeMaterialBoxList = MaterialBox.dao.find(GET_DIFFERENT_TYPE_MATERIAL_BOX_SQL, materialTypeId);
			// 获取料盒容量
			int materialBoxCapacity = PropKit.use("properties.ini").getInt("materialBoxCapacity");
			for (MaterialBox differentTypeMaterialBox : differentTypeMaterialBoxList) {
				int usedcapacity = Material.dao.find(GET_MATERIAL_BOX_USED_CAPACITY_SQL, differentTypeMaterialBox.getId()).size();
				int unusedcapacity = materialBoxCapacity - usedcapacity;
				if (unusedcapacity > boxRemainderCapacity) {
					boxRemainderCapacity = unusedcapacity;
					boxId = differentTypeMaterialBox.getId();
				}
			}
//			if (boxId == 0) {
				/*
				 * 如果这里pause了，因为我们暂时没有手动出库的接口，那么除非人为修改数据库，否则这里仓库会一直处于料盒已满状态，系统将永远无法在发送指令给叉车
				所以这里建议不pause，直接记录错误日志，对应的任务条目不执行，等仓库工作人员发现了，再创建出库任务进行出库，腾出料盒位置即可 */
				// TaskItemRedisDAO.setPauseAssign(1);

				/*
				 * 由于任务池遍历周期很短暂，如果记录错误日志，短短几分钟就会记录几百条重复的错误日志 */
//				throw new OperationException("仓库的料盒全满了，请尽快处理！");
//			}

		}

		return boxId;
	}


	private static int getOldestMaterialBox(Integer materialTypeId) {
		List<Material> materialList = Material.dao.find(GET_SURPLUS_MATERIAL_TYPE_MATERIAL_BOX_SQL, materialTypeId);
		int boxId = 0;
		Date productionTime = new Date();

		// 如果库存数量大于0
		if (materialList.size() > 0) {
			for (Material m : materialList) {
				if (m.getProductionTime().before(productionTime)) {
					productionTime = m.getProductionTime();
					boxId = m.getBox();
				}
			}
		}
//		else {
//			MaterialType materialType = MaterialType.dao.findById(materialTypeId);
			/*
			 * 与料盒已满的情况相类似，如果这里pause了，因为我们暂时没有手动入库的接口，那么除非人为修改数据库，否则对应的物料会一直处于缺料状态，系统将永远无法在发送指令给叉车
			所以这里建议不pause，直接记录错误日志，对应的任务条目不执行，等仓库工作人员发现了，再创建入库任务进行补料即可 */
			// TaskItemRedisDAO.setPauseAssign(1);

			/*
			 * 也不抛异常或记录错误日志，抛异常会导致任务池不断重复遍历缺料的任务条目；由于任务池遍历周期很短暂，如果记录错误日志，短短几分钟就会记录几百条重复的错误日志 */
//			throw new OperationException("仓库缺料了，请尽快对料号为" + materialType.getNo() + "的物料进行补料！");
//		}

		return boxId;
	}

}