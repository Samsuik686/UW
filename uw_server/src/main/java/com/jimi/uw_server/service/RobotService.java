package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.handle.LSSLHandler;
import com.jimi.uw_server.agv.handle.SwitchHandler;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.RobotBO;
import com.jimi.uw_server.model.vo.RobotVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.util.RobotComparator;

/**
 * 叉车业务层
 * 
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class RobotService extends SelectService {

	private static TaskService taskService = Enhancer.enhance(TaskService.class);

	private static final String GET_MATERIAL_TYPE_ID_SQL = "SELECT * FROM packing_list_item WHERE task_id = ? AND material_type_id = (SELECT id FROM material_type WHERE enabled = 1 AND no = ?)";

	private static final String GET_TASK_ITEM_DETAILS_SQL = "SELECT material_id AS materialId, quantity, production_time AS productionTime FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id";
	
	private static final Object BACK_LOCK = new Object();

	private static final Object CALL_LOCK = new Object();


	public List<RobotVO> select() {
		List<RobotBO> robotBOs = RobotInfoRedisDAO.check();
		List<RobotVO> robotVOs = new ArrayList<>();
		for (RobotBO robotBO : robotBOs) {
			RobotVO robotVO = new RobotVO(robotBO);
			robotVOs.add(robotVO);
		}
		// 根据叉车ID对叉车进行升序排序
		Collections.sort(robotVOs, new RobotComparator());
		return robotVOs;
	}

	
	public void robotSwitch(String id, Integer enabled) throws Exception {
		List<Integer> idList = new ArrayList<>();
		String[] ids = id.split(",");
		for (String string : ids) {
			idList.add(Integer.parseInt(string));
		}
		if (enabled == 2) {
			SwitchHandler.sendEnable(idList);
		} else if (enabled == 1) {
			SwitchHandler.sendDisable(idList);
		}
	}

	
	public void pause(Boolean pause) throws Exception {
        if (pause) {
            SwitchHandler.sendAllStart();
            RobotInfoRedisDAO.clearLoadException();
        } else {
            SwitchHandler.sendAllPause();
        }
	}


	/**
	 * 叉车回库SL
	 */
	public String back(Integer id, String materialOutputRecords) throws Exception {
		String resultString = "已成功发送SL指令！";
		for (AGVIOTaskItem item : TaskItemRedisDAO.getTaskItems()) {
			if (item.getId().intValue() == id) {
				synchronized(BACK_LOCK) {
					if (item.getState().intValue() < 3) {
						// 更新任务条目状态为已分配回库
						TaskItemRedisDAO.updateTaskItemState(item, 3);
						// 获取实际出入库数量，与计划出入库数量进行对比，若一致，则将该任务条目标记为已完成
						Integer actualQuantity = getActualIOQuantity(item.getId());
						if (actualQuantity >= item.getQuantity()) {
							taskService.finishItem(id, true);
						}

						// 查询对应料盒
						MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
						// 若任务队列中不存在其他料盒号与仓库停泊条目料盒号相同，且未被分配任务的任务条目，则发送回库指令
						Integer sameBoxItemId = getSameBoxItemId(item);
						if (sameBoxItemId == null) {
							LSSLHandler.sendSL(item, materialBox);
						} else {	// 否则，将同料盒号、未被分配任务的任务条目状态更新为已到达仓口
							PackingListItem packingListItem = PackingListItem.dao.findById(sameBoxItemId);
							AGVIOTaskItem itemInSameBox = new AGVIOTaskItem(packingListItem, item.getPriority());
							// 更新任务条目状态为已到达仓口
							TaskItemRedisDAO.updateTaskItemState(itemInSameBox, 2);
							resultString = "料盒中还有其他需要出库的物料，叉车暂时不回库！";
						}

						// 在对出库任务执行回库操作时，调用 updateOutputQuantity 方法，以便「修改出库数」
						if (Task.dao.findById(item.getTaskId()).getType() == 1) {
							taskService.updateOutputQuantityAndMaterialInfo(item, materialOutputRecords);
						}

					} else {
						resultString = "该任务条目已发送过SL指令，请勿重复发送SL指令！";
						return resultString;
					}
				}
			}
		}
		return resultString;

	}


	/**
	 * 获取任务条目实际出入库数量
	 */
	public Integer getActualIOQuantity(Integer packingListItemId) {
		// 查询task_log中的material_id,quantity
		List<TaskLog> taskLogs = TaskLog.dao.find(GET_TASK_ITEM_DETAILS_SQL, packingListItemId);
		Integer actualQuantity = 0;
		// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
		for (TaskLog tl : taskLogs) {
			actualQuantity += tl.getQuantity();
		}
		return actualQuantity;
	}


	/**
	 * 获取同组任务、同料盒中尚未被分配任务的任务条目id
	 * 若任务队列中存在其他料盒号与仓库停泊条目料盒号相同，且未被分配任务的任务条目，则返回其任务条目id；否则返回null
	 */
	public Integer getSameBoxItemId(AGVIOTaskItem item) {
		for (AGVIOTaskItem item1 : TaskItemRedisDAO.getTaskItems()) {
			if (item1.getBoxId().intValue() == item.getBoxId().intValue() && item1.getTaskId().intValue() == item.getTaskId().intValue() && item1.getState().intValue() == 0) {
				return item1.getId().intValue();
			}
		}
		return null;
	}


	/**
	 * 物料入库/截料后重新入库扫料盘，将任务条目状态设置为0
	 */
	public String call(Integer id, String no) throws Exception {
		synchronized(CALL_LOCK) {
			Window window = Window.dao.findById(id);
			Integer taskId = window.getBindTaskId();
			PackingListItem item = PackingListItem.dao.findFirst(GET_MATERIAL_TYPE_ID_SQL, taskId, no);
			String resultString = "调用成功！";

			if (item == null) {
				resultString = "该物料暂时不需要入库！";
				return resultString;
			}

			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getTaskItems()) {
				// 如果该料号对应的任务条目存在redis中
				if (item.getId().equals(redisTaskItem.getId())) {
					// 若任务条目状态为 -1，则将其状态更新为 0
					if (redisTaskItem.getState() == -1) {
						TaskItemRedisDAO.updateTaskItemState(redisTaskItem, 0);
						TaskItemRedisDAO.updateTaskItemRobot(redisTaskItem, 0);
						TaskItemRedisDAO.updateTaskItemBoxId(redisTaskItem, 0);
						return resultString;
					} else {
						resultString = "该物料对应的任务条目正在执行中或已完成，请勿重复扫描！";
						return resultString;
					}
				}
			}
			return resultString;
		}
	}

}
