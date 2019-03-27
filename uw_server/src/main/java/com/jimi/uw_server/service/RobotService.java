package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.handle.IOHandler;
import com.jimi.uw_server.agv.handle.SwitchHandler;
import com.jimi.uw_server.comparator.RobotComparator;
import com.jimi.uw_server.constant.IOTaskItemState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.RobotBO;
import com.jimi.uw_server.model.vo.RobotVO;
import com.jimi.uw_server.service.base.SelectService;


/**
 * 叉车业务层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class RobotService extends SelectService {

	private static TaskService taskService = Enhancer.enhance(TaskService.class);

	private static final String GET_MATERIAL_TYPE_ID_SQL = "SELECT * FROM packing_list_item WHERE task_id = ? AND material_type_id = (SELECT id FROM material_type WHERE enabled = 1 AND no = ? AND supplier = ?)";

	private static final String GET_TASK_ITEM_DETAILS_SQL = "SELECT material_id AS materialId, quantity, production_time AS productionTime FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id";

	private static final String GET_PACKING_LIST_ITEM_SQL = "SELECT * FROM packing_list_item WHERE task_id = ?";
	
	private static final Object BACK_LOCK = new Object();

	private static final Object CALL_LOCK = new Object();


	// 查询叉车
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


	// 启用/禁用叉车
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


	// 运行/停止所有叉车
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
		String resultString = "已成功发送回库指令！";
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems()) {
			if (item.getId().intValue() == id) {
				synchronized(BACK_LOCK) {
					if (item.getState().intValue() == IOTaskItemState.ARRIVED_WINDOW) {
						// 若是出库任务且为截料后重新入库，则需要判断是否对已截过料的料盘重新扫码过
						if (item.getIsForceFinish() && Task.dao.findById(item.getTaskId()).getType() == TaskType.OUT && materialOutputRecords == null) {
							resultString = taskService.isScanAgain(item.getId());
							if (resultString.equals("请扫描修改出库数时所打印出的新料盘二维码!")) {
								return resultString;
							}
						}

						// 更新任务条目状态为已分配回库
						TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.START_BACK);
						// 获取实际出入库数量，与计划出入库数量进行对比，若一致，则将该任务条目标记为已完成
						Integer actualQuantity = getActualIOQuantity(item.getId());
						if (actualQuantity >= item.getQuantity()) {
							taskService.finishItem(id, true);
						}

						// 查询对应料盒
						MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
						// 若任务队列中不存在其他料盒号与仓库停泊条目料盒号相同，且未被分配任务的任务条目，则发送回库指令
						AGVIOTaskItem sameBoxItem = getSameBoxItem(item);
						if (sameBoxItem == null) {
							IOHandler.sendSL(item, materialBox);
						} else {	// 否则，将同料盒号、未被分配任务的任务条目状态更新为已到达仓口
							TaskItemRedisDAO.updateIOTaskItemState(sameBoxItem, IOTaskItemState.ARRIVED_WINDOW);
							// 更新任务条目绑定的叉车id
							TaskItemRedisDAO.updateIOTaskItemRobot(sameBoxItem, item.getRobotId());
							resultString = "料盒中还有其他需要出库的物料，叉车暂时不回库！";
						}

						// 在对出库任务执行回库操作时，调用 updateOutQuantity 方法，以便「修改出库数」
						if (Task.dao.findById(item.getTaskId()).getType() == TaskType.OUT && materialOutputRecords != null) {
							taskService.updateOutQuantityAndMaterialInfo(item, materialOutputRecords);
						}

					} else {
						resultString = "该任务条目已发送过回库指令，请勿重复发送回库指令！";
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
	 * 获取同组任务、同料盒中尚未被分配任务的任务条目
	 * 若任务队列中存在其他料盒号与仓库停泊条目料盒号相同，且未被分配任务的任务条目，则返回其任务条目；否则返回null
	 */
	public AGVIOTaskItem getSameBoxItem(AGVIOTaskItem item) {
		for (AGVIOTaskItem item1 : TaskItemRedisDAO.getIOTaskItems()) {
			if (item1.getBoxId().intValue() == item.getBoxId().intValue() && item1.getTaskId().intValue() == item.getTaskId().intValue() && item1.getState().intValue() == IOTaskItemState.WAIT_ASSIGN) {
				return item1;
			}
		}
		return null;
	}


	/**
	 * 物料入库/截料后重新入库扫料盘，用于呼叫叉车
	 */
	public String call(Integer id, String no) throws Exception {
		synchronized(CALL_LOCK) {
			String resultString = "调用成功！";

			// 只在有选择仓口时才读取仓口和任务信息，避免出现NPE异常
			if (id != null) {
				Window window = Window.dao.findById(id);
				Integer taskId = window.getBindTaskId();
				// 通过任务条目id获取套料单记录
				PackingListItem packingListItem = PackingListItem.dao.findFirst(GET_PACKING_LIST_ITEM_SQL, taskId);
				// 通过套料单记录获取物料类型id
				MaterialType materialType = MaterialType.dao.findById(packingListItem.getMaterialTypeId());
				// 通过物料类型获取对应的供应商id
				Integer supplier = materialType.getSupplier();
				// 通过任务id，料号和供应商获取套料单条目
				PackingListItem item = PackingListItem.dao.findFirst(GET_MATERIAL_TYPE_ID_SQL, taskId, no, supplier);

				// 若是扫描到一些不属于当前仓口任务的料盘二维码，需要捕获该异常，不然会出现NPE异常
				if (item == null) {
					resultString = "该物料暂时不需要入库或截料！";
					return resultString;
				}

				// 若是扫描到属于当前仓口任务的料盘二维码，则逐条读取任务队列中的任务条目
				else {
					for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems()) {
						// 若扫描的料号对应的任务条目与任务队列读取到的数据匹配
						if (item.getId().intValue() == redisTaskItem.getId().intValue()) {

							// 若任务条目已完成，则提示不要重复执行已完成任务条目
							if (redisTaskItem.getState().intValue() == IOTaskItemState.FINISH_BACK) {
								resultString = "该任务条目已完成，请不要重复执行已完成任务条目";
								return resultString;
							}

							// 若该任务条目正在执行当中，则提示不能再调用该接口
							else if ((redisTaskItem.getState().intValue() > IOTaskItemState.WAIT_SCAN) && (redisTaskItem.getState().intValue() < IOTaskItemState.FINISH_BACK)) {
								resultString = "该物料对应的任务条目正在执行中，请勿重复调用叉车！";
								return resultString;
							}

							// 若任务条目状态为等待扫码，则将其状态更新为未分配拣料
							else if (redisTaskItem.getState().intValue() == IOTaskItemState.WAIT_SCAN) {
								TaskItemRedisDAO.updateIOTaskItemState(redisTaskItem, IOTaskItemState.WAIT_ASSIGN);
								TaskItemRedisDAO.updateIOTaskItemRobot(redisTaskItem, 0);
								TaskItemRedisDAO.updateTaskItemBoxId(redisTaskItem, 0);
								return resultString;
							}

							// 若任务条目状态为已完成截料，且判断其对应的料盒是否在架，根据料盒在架情况更新其状态
							else if (redisTaskItem.getState().intValue() == IOTaskItemState.FINISH_CUT) {
								MaterialBox materialBox = MaterialBox.dao.findById(redisTaskItem.getBoxId());
								// 若料盒在架，则将其状态更新为未分配拣料
								if (materialBox.getIsOnShelf()) {
									TaskItemRedisDAO.updateIOTaskItemState(redisTaskItem, IOTaskItemState.WAIT_ASSIGN);
									TaskItemRedisDAO.updateIOTaskItemRobot(redisTaskItem, 0);
									TaskItemRedisDAO.updateTaskItemBoxId(redisTaskItem, 0);
									return resultString;
								} else {	// 若料盒不在架，为避免 missiongroupid 重复，需要等上一个叉车任务执行完毕之后才可调用该接口发送相同 missiongroupid 的LS指令
									resultString = "请等叉车将对应的料盒放回货架之后再进行调用！";
									return resultString;
								}
							}

							// 如果该料号对应的任务条目不存在于任务队列中，则提示“该物料暂时不需要入库或截料！”
							else {
								resultString = "该物料暂时不需要入库或截料！";
								return resultString;
							}
						}

						// 若扫描的料号对应的任务条目与任务队列读取到的数据不匹配，则继续读取下一条任务条目数据
						else {
							continue;
						}

					}
				}
			}

			// 若在没有传递仓口id的情况下调用该接口，则返回警告信息
			else {
				resultString = "当前无任务，无需扫码呼叫叉车！";
			}

			return resultString;
		}
	}

}
