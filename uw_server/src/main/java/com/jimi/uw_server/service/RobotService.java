package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.handle.IOTaskHandler;
import com.jimi.uw_server.agv.handle.SwitchHandler;
import com.jimi.uw_server.comparator.RobotComparator;
import com.jimi.uw_server.constant.BoxState;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.constant.sql.SQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.ExternalWhLog;
import com.jimi.uw_server.model.FormerSupplier;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialReturnRecord;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.User;
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

	private static IOTaskService taskService = Aop.get(IOTaskService.class);

	private static final String GET_MATERIAL_TYPE_ID_SQL = "SELECT * FROM packing_list_item WHERE task_id = ? AND material_type_id = (SELECT id FROM material_type WHERE enabled = 1 AND no = ? AND supplier = ? AND type = ?)";

	private static final String GET_TASK_ITEM_DETAILS_SQL = "SELECT material_id AS materialId, quantity, production_time AS productionTime FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id";

	private static final String GET_MATERIAL_BOX_USED_CAPACITY_SQL = "SELECT * FROM material WHERE box = ? AND remainder_quantity > 0";

	private static final String GET_TASK_BY_TYPE_STATE_SUPPLIER = "select * from task where task.type = ? and task.state = ? and task.supplier = ?";

	private static final String GET_FORMER_SUPPLIER_SQL = "SELECT * FROM former_supplier WHERE former_name = ? and supplier_id = ?";

	private static ExternalWhLogService externalWhLogService = ExternalWhLogService.me;

	private static IOTaskHandler ioTaskHandler = IOTaskHandler.getInstance();

	private static final int UW_ID = 0;


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
	public String back(Integer id, Boolean isLater, Integer state, User user) throws Exception {
		String resultString = "已成功发送回库指令！";
		PackingListItem packingListItem = PackingListItem.dao.findById(id);
		if (packingListItem == null) {
			throw new OperationException("无此任务条目，回库失败");
		}
		Task task = Task.dao.findById(packingListItem.getTaskId());
		Boolean afterCut = false;
		AGVIOTaskItem agvioTaskItem = null;
		synchronized (Lock.IO_TASK_BACK_LOCK) {
			for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(packingListItem.getTaskId())) {
				if (item.getId().intValue() == id) {
					if (item.getState().intValue() == TaskItemState.ARRIVED_WINDOW) {
						agvioTaskItem = item;
						// 若是出库任务且为截料后重新入库，则需要判断是否对已截过料的料盘重新扫码过
						if (agvioTaskItem.getIsForceFinish() && task.getType() == TaskType.OUT) {
							afterCut = true;
							if (!taskService.isScanAgain(agvioTaskItem.getId())) {
								throw new OperationException("请扫描修改出库数时所打印出的新料盘二维码!");
							}
						}

						// 查询对应料盒
						MaterialBox materialBox = MaterialBox.dao.findById(agvioTaskItem.getBoxId());
						materialBox.setStatus(state);

						if (task.getType().equals(TaskType.OUT)) {
							materialBox.setUpdateTime(new Date());
						}
						materialBox.update();
						if (materialBox != null && materialBox.getType().equals(1)) {
							// 若任务队列中不存在其他料盒号与仓库停泊条目料盒号相同，且未被分配任务的任务条目，则发送回库指令

							int materialBoxCapacity = PropKit.use("properties.ini").getInt("materialBoxCapacity");
							int usedcapacity = Material.dao.find(GET_MATERIAL_BOX_USED_CAPACITY_SQL, agvioTaskItem.getBoxId()).size();
							int unusedcapacity = materialBoxCapacity - usedcapacity;
							synchronized (Lock.IO_TASK_REDIS_LOCK) {
								AGVIOTaskItem sameBoxItem = getSameBoxItem(agvioTaskItem);
								if ((sameBoxItem == null) || (task.getType() != TaskType.OUT && unusedcapacity <= 0)) {
									GoodsLocation goodsLocation = GoodsLocation.dao.findById(agvioTaskItem.getGoodsLocationId());
									if (goodsLocation != null) {
										ioTaskHandler.sendBackLL(agvioTaskItem, materialBox, goodsLocation, task.getPriority());
									} else {
										throw new OperationException("找不到目的货位，仓口：" + agvioTaskItem.getWindowId() + "货位：" + agvioTaskItem.getGoodsLocationId());
									}
								} else { // 否则，将同料盒号、未被分配任务的任务条目状态更新为已到达仓口
									TaskItemRedisDAO.updateIOTaskItemInfo(sameBoxItem, TaskItemState.ARRIVED_WINDOW, agvioTaskItem.getWindowId(), agvioTaskItem.getGoodsLocationId(), agvioTaskItem.getBoxId(), agvioTaskItem.getRobotId(), null, null);
									resultString = "料盒中还有其他需要出库的物料，叉车暂时不回库！";

								}
							}

						} else if (materialBox != null && materialBox.getType().equals(2)) {

							// 若任务队列中不存在其他料盒号与仓库停泊条目料盒号相同，且未被分配任务的任务条目，则发送回库指令
							synchronized (Lock.IO_TASK_REDIS_LOCK) {
								AGVIOTaskItem sameBoxItem = getSameBoxItem(agvioTaskItem);
								if ((sameBoxItem == null) || (task.getType() != TaskType.OUT && materialBox.getStatus().equals(BoxState.FULL))) {
									GoodsLocation goodsLocation = GoodsLocation.dao.findById(agvioTaskItem.getGoodsLocationId());
									if (goodsLocation != null) {
										ioTaskHandler.sendBackLL(agvioTaskItem, materialBox, goodsLocation, task.getPriority());
									} else {
										throw new OperationException("找不到目的货位，仓口：" + agvioTaskItem.getWindowId() + "货位：" + agvioTaskItem.getGoodsLocationId());
									}
								} else { // 否则，将同料盒号、未被分配任务的任务条目状态更新为已到达仓口
										TaskItemRedisDAO.updateIOTaskItemInfo(sameBoxItem, TaskItemState.ARRIVED_WINDOW, agvioTaskItem.getWindowId(), agvioTaskItem.getGoodsLocationId(), agvioTaskItem.getBoxId(), agvioTaskItem.getRobotId(), null, null);
									// 更新任务条目绑定的叉车id
									resultString = "料盒中还有其他需要出库的物料，叉车暂时不回库！";
								}
							}
						}
						// 更新任务条目状态为已分配回库
						if (!isLater || task.getState().equals(TaskState.CANCELED)) {
							agvioTaskItem.setIsForceFinish(true);
							packingListItem.setFinishTime(new Date());
							packingListItem.update();
							TaskItemRedisDAO.updateIOTaskItemInfo(agvioTaskItem, TaskItemState.START_BACK, null, null, null, null, true, null);

						} else {
							TaskItemRedisDAO.updateIOTaskItemInfo(agvioTaskItem, TaskItemState.START_BACK, null, null, null, null, null, null);
						}
						// 在对出库任务执行回库操作时，调用 updateOutQuantity 方法，以便「修改出库数」
						if (task.getType() == TaskType.OUT) {
							taskService.updateOutQuantityAndMaterialInfo(agvioTaskItem, afterCut, user);
						}
					} else {
						resultString = "该任务条目已发送过回库指令，请勿重复发送回库指令！";
						return resultString;
					}
					break;
				}
			}
		}

		if (agvioTaskItem != null) {

			// 对回库进行抵扣进行处理
			if (task.getType() == TaskType.SEND_BACK && agvioTaskItem.getIsForceFinish()) {
				List<Record> records = Db.find(SQL.GET_TASKLOG_BY_ITEM_ID_SQL, agvioTaskItem.getId());
				if (!records.isEmpty()) {
					Integer acturallyNum = 0;
					Task task2 = Task.dao.findFirst(GET_TASK_BY_TYPE_STATE_SUPPLIER, TaskType.COUNT, TaskState.WAIT_START, task.getSupplier());
					for (Record record : records) {
						acturallyNum += record.getInt("quantity");
					}
					ExternalWhLog externalWhLog = new ExternalWhLog();
					externalWhLog.setMaterialTypeId(packingListItem.getMaterialTypeId());
					externalWhLog.setDestination(UW_ID);
					externalWhLog.setSourceWh(task.getDestination());
					externalWhLog.setTaskId(task.getId());
					int storeNum = 0;
					if (task2 != null) {
						storeNum = externalWhLogService.getEWhMaterialQuantity(agvioTaskItem.getMaterialTypeId(), task.getDestination(), task2.getCreateTime());
						if (acturallyNum - storeNum > 0) {
							externalWhLog.setQuantity(storeNum);
							MaterialReturnRecord materialReturnRecord = new MaterialReturnRecord();
							materialReturnRecord.setTaskId(task.getId());
							materialReturnRecord.setMaterialTypeId(packingListItem.getMaterialTypeId());
							materialReturnRecord.setWhId(task.getDestination());
							materialReturnRecord.setQuantity(acturallyNum - storeNum);
							materialReturnRecord.setTime(task2.getCreateTime());
							materialReturnRecord.setEnabled(true);
							materialReturnRecord.save();
						} else {
							externalWhLog.setQuantity(acturallyNum);
						}
						externalWhLog.setTime(task2.getCreateTime());
						externalWhLog.setOperationTime(new Date());
					} else {
						storeNum = externalWhLogService.getEWhMaterialQuantity(agvioTaskItem.getMaterialTypeId(), task.getDestination());
						if (acturallyNum - storeNum > 0) {
							externalWhLog.setQuantity(storeNum);
							MaterialReturnRecord materialReturnRecord = new MaterialReturnRecord();
							materialReturnRecord.setTaskId(task.getId());
							materialReturnRecord.setMaterialTypeId(packingListItem.getMaterialTypeId());
							materialReturnRecord.setWhId(task.getDestination());
							materialReturnRecord.setQuantity(acturallyNum - storeNum);
							materialReturnRecord.setTime(new Date());
							materialReturnRecord.setEnabled(true);
							materialReturnRecord.save();
						} else {
							externalWhLog.setQuantity(acturallyNum);
						}
						externalWhLog.setTime(new Date());
						externalWhLog.setOperationTime(new Date());
					}
					externalWhLog.setOperatior(user.getUid());
					externalWhLog.save();

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
		for (AGVIOTaskItem item1 : TaskItemRedisDAO.getIOTaskItems(item.getTaskId())) {
			if (item1.getBoxId().intValue() == item.getBoxId().intValue() && item1.getGoodsLocationId() == 0 && item1.getWindowId() == 0 && item1.getState().intValue() == TaskItemState.WAIT_ASSIGN) {
				return item1;
			}
		}
		return null;

	}


	/**
	 * 物料入库/截料后重新入库扫料盘，用于呼叫叉车
	 */
	public String call(Integer id, String no, String supplierName) throws Exception {
		synchronized (Lock.IO_TASK_CALL_LOCK) {
			String resultString = "调用成功！";
			// 只在有选择仓口时才读取仓口和任务信息，避免出现NPE异常
			if (id != null) {
				Window window = Window.dao.findById(id);
				Integer taskId = window.getBindTaskId();
				Task task = Task.dao.findById(taskId);
				if (task == null) {
					resultString = "该物料暂时不需要入库或截料！";
					return resultString;
				}

				// 通过物料类型获取对应的客户id
				Integer supplierId = task.getSupplier();
				// 通过客户id获取客户名
				FormerSupplier formerSupplier = FormerSupplier.dao.findFirst(GET_FORMER_SUPPLIER_SQL, supplierName, supplierId);
				String sName = Supplier.dao.findById(supplierId).getName();
				if (!supplierName.equals(sName) && formerSupplier == null) {
					resultString = "扫码错误，客户 " + supplierName + " 对应的任务目前没有在本仓口进行任务，" + "本仓口已绑定 " + sName + " 的任务单！";
					return resultString;
				}
				// 通过任务id，料号和客户获取套料单条目
				PackingListItem item = PackingListItem.dao.findFirst(GET_MATERIAL_TYPE_ID_SQL, taskId, no.trim(), supplierId, WarehouseType.REGULAR.getId());

				// 若是扫描到一些不属于当前仓口任务的料盘二维码，需要捕获该异常，不然会出现NPE异常
				if (item == null) {
					resultString = "该物料暂时不需要入库或截料！";
					return resultString;
				}
				// 若是扫描到属于当前仓口任务的料盘二维码，则逐条读取任务队列中的任务条目
				else {
					for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(taskId)) {
						// 若扫描的料号对应的任务条目与任务队列读取到的数据匹配
						if (item.getId().intValue() == redisTaskItem.getId().intValue()) {
							// 若任务条目已完成，则提示不要重复执行已完成任务条目
							if (redisTaskItem.getState().intValue() == TaskItemState.FINISH_BACK) {
								resultString = "该任务条目已完成，请不要重复执行已完成任务条目";
								return resultString;
							}

							// 若该任务条目正在执行当中，则提示不能再调用该接口
							else if ((redisTaskItem.getState().intValue() > TaskItemState.WAIT_SCAN) && (redisTaskItem.getState().intValue() < TaskItemState.FINISH_BACK)) {
								resultString = "该物料对应的任务条目正在执行中，请勿重复调用叉车！";
								return resultString;
							}

							// 若任务条目状态为等待扫码，则将其状态更新为未分配拣料
							else if (redisTaskItem.getState().intValue() == TaskItemState.WAIT_SCAN) {
								TaskItemRedisDAO.updateIOTaskItemInfo(redisTaskItem, TaskItemState.WAIT_ASSIGN, 0, 0, 0, 0, null, null);
								return resultString;
							}

							// 若任务条目状态为已完成截料，且判断其对应的料盒是否在架，根据料盒在架情况更新其状态
							else if (redisTaskItem.getState().intValue() == TaskItemState.FINISH_CUT) {
								// 若料盒在架，则将其状态更新为未分配拣料
								TaskItemRedisDAO.updateIOTaskItemInfo(redisTaskItem, TaskItemState.WAIT_ASSIGN, 0, 0, null, 0, null, null);
								return resultString;
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
