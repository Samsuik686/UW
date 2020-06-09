package com.jimi.uw_server.service.inventory;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jimi.uw_server.agv.dao.InventoryTaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.agv.handle.InvTaskHandler;
import com.jimi.uw_server.constant.*;
import com.jimi.uw_server.constant.sql.*;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.lock.RegularTaskLock;
import com.jimi.uw_server.model.*;
import com.jimi.uw_server.model.bo.EWhInventoryRecordBO;
import com.jimi.uw_server.model.vo.*;
import com.jimi.uw_server.service.ExternalWhTaskService;
import com.jimi.uw_server.service.base.BaseInventoryTaskService;
import com.jimi.uw_server.ur.dao.UrTaskInfoDAO;
import com.jimi.uw_server.ur.dao.UrOperationMaterialInfoDAO;
import com.jimi.uw_server.ur.entity.UrMaterialInfo;
import com.jimi.uw_server.util.ExcelHelper;
import com.jimi.uw_server.util.ExcelWritter;
import com.jimi.uw_server.util.MaterialHelper;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * 
 * @author trjie
 * @createTime 2019年5月16日 上午10:06:35
 */

public class RegularInventoryTaskService extends BaseInventoryTaskService {

	public static RegularInventoryTaskService me = new RegularInventoryTaskService();

	private static final String GET_INVENTORY_LOG_BY_BOX_AND_TASKID = "SELECT * FROM inventory_log WHERE inventory_log.box_id = ? AND inventory_log.task_id = ?";

	private static final String GET_INVENTORY_LOG_BY_BOX_AND_TASKID_AND_MATERIALID = "SELECT * FROM inventory_log WHERE inventory_log.box_id = ? AND inventory_log.task_id = ? AND inventory_log.material_id = ?";

	private static final String GET_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE = "SELECT inventory_log.*, material.production_time FROM inventory_log INNER JOIN material ON inventory_log.material_id = material.id WHERE material.type = ? AND inventory_log.task_id = ?";

	private static final String GET_EWH_INVENTORY_LOG_BY_TASKID_MATERIALTYPEID_WHID = "SELECT * FROM external_inventory_log WHERE task_id = ? AND wh_id = ? AND material_type_id = ?";

	private static final String GET_EWH_INVENTORY_LOG_BY_TASKID_MATERIALTYPEID = "SELECT external_inventory_log.*, destination.`name` as wh_name FROM external_inventory_log INNER JOIN destination ON external_inventory_log.wh_id = destination.id WHERE external_inventory_log.task_id = ? AND external_inventory_log.material_type_id = ? AND external_inventory_log.wh_id = ?";

	private static final String GET_UN_EWH_INVENTORY_LOG_BY_TASKID = "SELECT * FROM external_inventory_log WHERE task_id = ? AND wh_id = ? AND external_inventory_log.inventory_time IS NULL AND enabled = 1";

	private static final String GET_UNCOVER_EWH_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE = "SELECT external_inventory_log.* FROM external_inventory_log INNER JOIN inventory_task_base_info ON inventory_task_base_info.task_id = external_inventory_log.task_id and external_inventory_log.wh_id = inventory_task_base_info.destination_id WHERE external_inventory_log.task_id = ? AND inventory_task_base_info.destination_id = ? AND material_type_id = ? AND enabled = 1";

	private static final String GET_ALL_EWH_INVENTORY_LOG_BY_TASK = "SELECT external_inventory_log.* FROM external_inventory_log INNER JOIN inventory_task_base_info ON inventory_task_base_info.task_id = external_inventory_log.task_id and external_inventory_log.wh_id = inventory_task_base_info.destination_id WHERE inventory_task_base_info.task_id = ? AND inventory_task_base_info.destination_id = ?";

	private static final String GET_UNCOVER_EWH_INVENTORY_LOG_BY_TASKID = "SELECT external_inventory_log.* FROM external_inventory_log INNER JOIN inventory_task_base_info ON inventory_task_base_info.task_id = external_inventory_log.task_id and external_inventory_log.wh_id = inventory_task_base_info.destination_id WHERE inventory_task_base_info.task_id = ? AND inventory_task_base_info.destination_id = ?  AND enabled = 1";

	private static final String GET_EWH_INVENTORY_TASK_INFO = "SELECT external_inventory_log.material_type_id AS material_type_id, material_type.`no` AS `no`, material_type.specification, external_inventory_log.task_id AS task_id, SUM( external_inventory_log.before_num ) AS before_num, SUM( external_inventory_log.actural_num ) AS actural_num, SUM( external_inventory_log.different_num ) AS different_num, SUM( external_inventory_log.material_return_num ) AS material_return_num, inventory_task_base_info.check_time AS check_time, inventory_task_base_info.check_operator AS check_operator, supplier.id AS supplier_id, supplier.`name` AS supplier_name, external_inventory_log.inventory_operatior FROM external_inventory_log INNER JOIN material_type INNER JOIN  inventory_task_base_info INNER JOIN supplier ON external_inventory_log.material_type_id = material_type.id AND supplier.id = material_type.supplier AND external_inventory_log.task_id = inventory_task_base_info.task_id AND external_inventory_log.wh_id = inventory_task_base_info.destination_id WHERE external_inventory_log.task_id = ? AND inventory_task_base_info.destination_id = ? GROUP BY external_inventory_log.material_type_id ORDER BY material_type.`no` DESC";

	private static final String GET_EWH_INVENTORY_TASK_INFO_BY_NO = "SELECT external_inventory_log.material_type_id AS material_type_id, material_type.`no` AS `no`, material_type.specification, external_inventory_log.task_id AS task_id, SUM( external_inventory_log.before_num ) AS before_num, SUM( external_inventory_log.actural_num ) AS actural_num, SUM( external_inventory_log.different_num ) AS different_num, SUM( external_inventory_log.material_return_num ) AS material_return_num, inventory_task_base_info.check_time AS check_time, inventory_task_base_info.check_operator AS check_operator, supplier.id AS supplier_id, supplier.`name` AS supplier_name, external_inventory_log.inventory_operatior FROM external_inventory_log INNER JOIN material_type INNER JOIN  inventory_task_base_info INNER JOIN supplier ON external_inventory_log.material_type_id = material_type.id AND supplier.id = material_type.supplier AND external_inventory_log.task_id = inventory_task_base_info.task_id AND external_inventory_log.wh_id = inventory_task_base_info.destination_id WHERE external_inventory_log.task_id = ? AND inventory_task_base_info.destination_id = ? AND material_type.`no` like ? GROUP BY external_inventory_log.material_type_id ORDER BY material_type.`no` DESC";

	private static final String UPDATE_EWH_INVENTORY_LOG_UNCOVER = "UPDATE external_inventory_log SET enabled = 0 WHERE different_num = 0 AND task_id = ?";

	private static final String GET_EXPORT_EWH_INVENTORY_INFO = "SELECT external_inventory_log.material_type_id AS material_type_id, material_type.`no` AS `no`, material_type.specification, destination.`name` AS `wh_name`, external_inventory_log.task_id AS task_id, SUM( external_inventory_log.before_num ) AS before_num, SUM( external_inventory_log.actural_num ) AS actural_num, SUM( external_inventory_log.different_num ) AS different_num, SUM( external_inventory_log.material_return_num ) AS material_return_num, supplier.id AS supplier_id, supplier.`name` AS supplier_name, external_inventory_log.inventory_operatior FROM external_inventory_log INNER JOIN material_type INNER JOIN inventory_task_base_info INNER JOIN supplier INNER JOIN destination ON external_inventory_log.task_id = inventory_task_base_info.task_id AND external_inventory_log.material_type_id = material_type.id AND supplier.id = material_type.supplier AND destination.id = external_inventory_log.wh_id AND destination.id = inventory_task_base_info.destination_id WHERE external_inventory_log.task_id = ? and inventory_task_base_info.destination_id = ? GROUP BY destination.id, external_inventory_log.material_type_id ORDER BY destination.id, material_type.`no` DESC";

	private static final String GET_EXPORT_EWH_INVENTORY_INFO_BY_NO = "SELECT external_inventory_log.material_type_id AS material_type_id, material_type.`no` AS `no`, material_type.specification, destination.`name` AS `wh_name`, external_inventory_log.task_id AS task_id, SUM( external_inventory_log.before_num ) AS before_num, SUM( external_inventory_log.actural_num ) AS actural_num, SUM( external_inventory_log.different_num ) AS different_num, SUM( external_inventory_log.material_return_num ) AS material_return_num, supplier.id AS supplier_id, supplier.`name` AS supplier_name, external_inventory_log.inventory_operatior FROM external_inventory_log INNER JOIN material_type INNER JOIN inventory_task_base_info INNER JOIN supplier INNER JOIN destination ON external_inventory_log.task_id = inventory_task_base_info.task_id AND external_inventory_log.material_type_id = material_type.id AND supplier.id = material_type.supplier AND destination.id = external_inventory_log.wh_id AND destination.id = inventory_task_base_info.destination_id WHERE external_inventory_log.task_id = ? and inventory_task_base_info.destination_id = ? AND material_type.`no` like ?  GROUP BY destination.id, external_inventory_log.material_type_id ORDER BY destination.id, material_type.`no` DESC";

	private static ExternalWhTaskService externalWhTaskService = ExternalWhTaskService.me;

	private static InvTaskHandler invTaskHandler = InvTaskHandler.getInstance();

	private Integer batchSize = 2000;

	private Integer uwId = 0;


	/**
	 * 创建盘点任务
	 * 
	 * @param supplierId
	 * @return
	 */
	public String create(Integer supplierId, String destinationIds) {
		Supplier supplier = Supplier.dao.findById(supplierId);
		if (supplier == null) {
			throw new OperationException("客户不存在！");
		}
		Task task = Task.dao.findFirst(InventoryTaskSQL.GET_USEFUL_TASK_BY_TYPE_SUPPLIER, TaskType.COUNT, supplierId, WarehouseType.REGULAR.getId());
		if (task != null) {
			throw new OperationException("该客户已存在未开始或进行中的盘点任务，无法创建新的盘点任务！");
		}
		List<Integer> destinationIdIntegerArr = new ArrayList<>();
		if (destinationIds != null && !destinationIds.trim().equals("")) {
			String[] destinationIdStringArr = destinationIds.split(",");
			for (String string : destinationIdStringArr) {
				try {
					Integer destinationId = Integer.valueOf(string);
					Destination destination = Destination.dao.findById(destinationId);
					if (destination == null) {
						throw new OperationException("盘点的目的仓库不存在！");
					}
					destinationIdIntegerArr.add(destinationId);
				} catch (NumberFormatException e) {
					throw new OperationException("盘点的目的仓库ID格式不正确！");
				}
			}
		}

		if (!destinationIdIntegerArr.contains(uwId)) {
			destinationIdIntegerArr.add(uwId);
		}
		task = new Task();
		Date date = new Date();
		task.setFileName(getTaskName(date, WarehouseType.REGULAR.getId()));
		task.setCreateTime(date);
		task.setType(TaskType.COUNT);
		task.setState(TaskState.WAIT_START);
		task.setWarehouseType(WarehouseType.REGULAR.getId());
		task.setSupplier(supplierId);
		task.save();
		for (Integer destinationId : destinationIdIntegerArr) {
			InventoryTaskBaseInfo info = new InventoryTaskBaseInfo();
			info.setTaskId(task.getId()).setDestinationId(destinationId).save();
		}
		return "操作成功";
	}


	/**
	 * 开始盘点任务
	 * 
	 * @param taskId
	 * 
	 * @return
	 */
	public String start(Integer taskId, String windows) {
		synchronized (RegularTaskLock.START_INVENTORY_LOCK) {
			List<Window> windowList = Window.dao.find(WindowSQL.GET_ALL_WINDOWS_SQL);
			Task task = Task.dao.findById(taskId);
			if (task == null) {
				throw new OperationException("任务不存在!");
			} else if (!task.getType().equals(TaskType.COUNT)) {
				throw new OperationException("该任务并非盘点任务！");
			} else if (!task.getState().equals(TaskState.WAIT_START)) {
				throw new OperationException("该任务并未处于未开始状态，无法开始任务！");
			}
			String[] windowArr = windows.split(",");
			List<Window> wList = new ArrayList<>();
			synchronized (Lock.WINDOW_LOCK) {

				for (String w : windowArr) {
					Integer windowId = 0;
					try {
						windowId = Integer.valueOf(w.trim());
					} catch (Exception e) {
						throw new OperationException("仓口参数解析错误，请检查参数格式");
					}
					Window window = Window.dao.findById(windowId);
					if (window.getBindTaskId() != null) {
						throw new OperationException("仓口" + windowId + "已经被其他任务绑定");
					}
					wList.add(window);
				}
				if (wList.size() == 0) {
					throw new OperationException("仓口参数不能为空，请检查参数及其格式");
				}
				// 盘点期间该客户不得出入库
				for (Window window : windowList) {
					if (window.getBindTaskId() == null) {
						continue;
					}
					Task tempTask = Task.dao.findFirst(InventoryTaskSQL.GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER, task.getSupplier(), WarehouseType.REGULAR.getId());
					if (tempTask != null) {
						throw new OperationException("当前盘点的客户存在进行中的盘点任务，请等待任务结束后再开始盘点任务!");
					}
					tempTask = Task.dao.findById(window.getBindTaskId());
					if (tempTask != null && tempTask.getSupplier().equals(task.getSupplier())) {
						throw new OperationException("当前盘点的客户存在其他出入库任务，请等待其他任务结束后再开始盘点任务!");
					}
				}
				List<InventoryTaskBaseInfo> infos = InventoryTaskBaseInfo.dao.find(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID, task.getId());
				List<InventoryLog> inventoryLogs = new ArrayList<>();
				List<ExternalInventoryLog> externalInventoryLogs = new ArrayList<>();
				List<ExternalWhLog> externalWhLogs = new ArrayList<>();
				List<AGVInventoryTaskItem> agvInventoryTaskItems = new ArrayList<>();
				if (!infos.isEmpty()) {
					for (InventoryTaskBaseInfo info : infos) {
						if (info.getDestinationId().equals(uwId)) {
							// 盘点前该客户的料盒必须在架
							MaterialBox materialBox = MaterialBox.dao.findFirst(MaterialBoxSQL.GET_NOT_ON_SHELF_BOX_BY_SUPPLIER_SQL, task.getSupplier());
							if (materialBox != null) {
								throw new OperationException("当前盘点的客户存在不在架的料盒，请检查!");
							}
							Material material = Material.dao.findFirst(MaterialSQL.GET_NOT_IN_BOX_MATERIAL_SQL, task.getSupplier());
							if (material != null) {
								throw new OperationException("当前盘点的客户存在不在料盒内的物料，请检查!");
							}

							List<MaterialBox> materialBoxs = MaterialBox.dao.find(MaterialBoxSQL.GET_ALL_NOT_EMPTY_BOX_BY_SUPPLIER_SQL, task.getSupplier());
							for (MaterialBox box : materialBoxs) {
								AGVInventoryTaskItem agvInventoryTaskItem = new AGVInventoryTaskItem(taskId, box.getId(), TaskItemState.WAIT_ASSIGN, 0, 0, box.getType());
								agvInventoryTaskItems.add(agvInventoryTaskItem);
							}

							List<Material> materials = Material.dao.find(MaterialSQL.GET_ALL_REGULAR_MATERIAL_BY_SUPPLIER_SQL, task.getSupplier());

							for (Material material2 : materials) {
								InventoryLog inventoryLog = new InventoryLog();
								inventoryLog.setMaterialId(material2.getId());
								inventoryLog.setTaskId(task.getId());
								inventoryLog.setBeforeNum(material2.getRemainderQuantity());
								inventoryLog.setBoxId(material2.getBox());
								inventoryLog.setEnabled(true);
								inventoryLogs.add(inventoryLog);
							}
						} else {
							Supplier supplier = Supplier.dao.findById(task.getSupplier());
							List<ExternalWhInfoVO> externalWhInfoVOs = externalWhTaskService.selectExternalWhInfo(supplier.getCompanyId(), info.getDestinationId(), task.getSupplier(), null, task);

							for (ExternalWhInfoVO externalWhInfoVO : externalWhInfoVOs) {
								if (externalWhInfoVO.getQuantity() == 0 && externalWhInfoVO.getReturnQuantity() == 0) {
									continue;
								}
								ExternalInventoryLog externalInventoryLog = new ExternalInventoryLog();
								externalInventoryLog.setTaskId(taskId);
								externalInventoryLog.setWhId(externalWhInfoVO.getWhId());
								externalInventoryLog.setMaterialTypeId(externalWhInfoVO.getMaterialTypeId());
								externalInventoryLog.setBeforeNum(externalWhInfoVO.getQuantity());
								externalInventoryLog.setMaterialReturnNum(externalWhInfoVO.getReturnQuantity());
								externalInventoryLog.setEnabled(true);
								externalInventoryLogs.add(externalInventoryLog);

								ExternalWhLog externalWhLog = new ExternalWhLog();
								externalWhLog.setMaterialTypeId(externalWhInfoVO.getMaterialTypeId());
								externalWhLog.setTaskId(taskId);
								externalWhLog.setSourceWh(externalWhInfoVO.getWhId());
								externalWhLog.setDestination(externalInventoryLog.getWhId());
								externalWhLog.setQuantity(0 - externalWhInfoVO.getQuantity());
								externalWhLog.setTime(new Date());
								externalWhLog.setOperationTime(new Date());
								externalWhLogs.add(externalWhLog);
							}
						}
					}
				}

				Db.batchSave(inventoryLogs, batchSize);
				Db.batchSave(externalInventoryLogs, batchSize);
				Db.batchSave(externalWhLogs, batchSize);
				if (!infos.isEmpty()) {
					for (InventoryTaskBaseInfo info : infos) {
						Db.update(InventoryTaskSQL.UPDATE_MATERIAL_RETURN_RECORD_UNENABLED, task.getCreateTime(), info.getDestinationId(), task.getSupplier(), WarehouseType.REGULAR.getId());

					}
				}
				task.setState(TaskState.PROCESSING);
				task.setStartTime(new Date());
				task.update();
				if (agvInventoryTaskItems.size() <= 0) {
					return "当前客户在UW无人仓没有物料，无需绑定仓口！";
				}
				for (Window window : wList) {
					window.setBindTaskId(task.getId());
					window.update();
				}
				Collections.shuffle(agvInventoryTaskItems);
				InventoryTaskItemRedisDAO.addInventoryTaskItem(taskId, agvInventoryTaskItems);
			}
		}

		return "操作成功";
	}


	/**
	 * 让盘点任务的叉车回库
	 * 
	 * @param taskId
	 * @param boxId
	 * @param windowId
	 * @param user
	 * @return
	 */
	public String backUWBox(Integer taskId, Integer boxId, Integer windowId, User user) {
		synchronized (RegularTaskLock.INV_TASK_BACK_LOCK) {
			Task task = Task.dao.findById(taskId);
			if (task == null) {
				throw new OperationException("任务不存在，请检查参数是否正确!");
			}
			List<InventoryLog> inventoryLogs = InventoryLog.dao.find(GET_INVENTORY_LOG_BY_BOX_AND_TASKID, boxId, taskId);
			for (InventoryLog inventoryLog : inventoryLogs) {
				if (inventoryLog.getActuralNum() == null) {
					inventoryLog.setActuralNum(inventoryLog.getBeforeNum());
					inventoryLog.setDifferentNum(0);
					inventoryLog.setEnabled(true);
					inventoryLog.setInventoryOperatior(user.getUid());
					inventoryLog.setInventoryTime(new Date());
					inventoryLog.update();
				}
			}
			MaterialBox materialBox = MaterialBox.dao.findById(boxId);
			if (materialBox == null) {
				throw new OperationException("当前盘点的料盒不存在，请检查参数是否正确！");
			}
			AGVInventoryTaskItem inventoryTaskItem = InventoryTaskItemRedisDAO.getInventoryTaskItem(taskId, boxId);
			if (inventoryTaskItem == null || inventoryTaskItem.getState() != TaskItemState.ARRIVED_WINDOW) {
				throw new OperationException("任务条目不存在或者未到站！");
			}
			try {
				GoodsLocation goodsLocation = GoodsLocation.dao.findById(inventoryTaskItem.getGoodsLocationId());
				if (goodsLocation != null) {
					invTaskHandler.sendBackLL(inventoryTaskItem, materialBox, goodsLocation, task.getPriority());
				} else {
					throw new OperationException("找不到目的货位，仓口：" + inventoryTaskItem.getWindowId() + "货位：" + inventoryTaskItem.getGoodsLocationId());
				}
				InventoryTaskItemRedisDAO.updateInventoryTaskItemInfo(inventoryTaskItem, TaskItemState.START_BACK, null, null, null, true);
			} catch (Exception e) {
				e.printStackTrace();
				throw new OperationException(e.getMessage());
			}


			for (InventoryLog inventoryLog : inventoryLogs) {
				if (!inventoryLog.getBeforeNum().equals(inventoryLog.getActuralNum())) {
					// 改变实际库存
					Material material = Material.dao.findById(inventoryLog.getMaterialId());
					material.setRemainderQuantity(inventoryLog.getActuralNum());
					if (inventoryLog.getActuralNum().equals(0)) {
						material.setRow(-1);
						material.setCol(-1);
						material.setIsInBox(false);
					}
					material.update();

				}
			}

			Window window = Window.dao.findById(windowId);
			if (window != null && window.getAuto()) {
				synchronized (Lock.UR_INV_TASK_LOCK) {
					UrTaskInfoDAO.removeUrMaterialInfosByTaskAndBox(taskId, boxId);
				}
			}
		}
		return "操作成功";
	}


	public String backUrUWBox(Integer taskId, Integer boxId, String user) {
		synchronized (RegularTaskLock.INV_TASK_BACK_LOCK) {
			Task task = Task.dao.findById(taskId);
			if (task == null) {
				throw new OperationException("任务不存在，请检查参数是否正确!");
			}
			List<InventoryLog> inventoryLogs = InventoryLog.dao.find(GET_INVENTORY_LOG_BY_BOX_AND_TASKID, boxId, taskId);
			for (InventoryLog inventoryLog : inventoryLogs) {
				if (inventoryLog.getActuralNum() == null) {
					inventoryLog.setActuralNum(inventoryLog.getBeforeNum());
					inventoryLog.setDifferentNum(0);
					inventoryLog.setEnabled(true);
					inventoryLog.setInventoryOperatior(user);
					inventoryLog.setInventoryTime(new Date());
					inventoryLog.update();
				}
			}
			MaterialBox materialBox = MaterialBox.dao.findById(boxId);
			if (materialBox == null) {
				throw new OperationException("当前盘点的料盒不存在，请检查参数是否正确!");
			}
			AGVInventoryTaskItem inventoryTaskItem = InventoryTaskItemRedisDAO.getInventoryTaskItem(taskId, boxId);
			if (inventoryTaskItem == null || inventoryTaskItem.getState() != TaskItemState.ARRIVED_WINDOW) {
				throw new OperationException("任务条目不存在或者未到站！");
			}
			try {
				GoodsLocation goodsLocation = GoodsLocation.dao.findById(inventoryTaskItem.getGoodsLocationId());
				if (goodsLocation != null) {
					invTaskHandler.sendBackLL(inventoryTaskItem, materialBox, goodsLocation, task.getPriority());
				} else {
					throw new OperationException("找不到目的货位，仓口：" + inventoryTaskItem.getWindowId() + "货位：" + inventoryTaskItem.getGoodsLocationId());
				}
				InventoryTaskItemRedisDAO.updateInventoryTaskItemInfo(inventoryTaskItem, TaskItemState.START_BACK, null, null, null, true);
			} catch (Exception e) {
				e.printStackTrace();
				throw new OperationException(e.getMessage());
			}
			for (InventoryLog inventoryLog : inventoryLogs) {
				if (!inventoryLog.getBeforeNum().equals(inventoryLog.getActuralNum())) {
					// 改变实际库存
					Material material = Material.dao.findById(inventoryLog.getMaterialId());
					material.setRemainderQuantity(inventoryLog.getActuralNum());
					if (inventoryLog.getActuralNum().equals(0)) {
						material.setRow(-1);
						material.setCol(-1);
						material.setIsInBox(false);
					}
					material.update();

				}
			}
		}
		return "操作成功";
	}


	/**
	 * 盘点UW物料
	 * 
	 * @param materialId
	 * @param boxId
	 * @param taskId
	 * @param acturalNum
	 * @param user
	 * @return
	 */
	public Material inventoryUWMaterial(String materialId, Integer boxId, Integer taskId, Integer acturalNum, User user) {
		InventoryLog inventoryLog = InventoryLog.dao.findFirst(GET_INVENTORY_LOG_BY_BOX_AND_TASKID_AND_MATERIALID, boxId, taskId, materialId);
		if (inventoryLog == null) {
			throw new OperationException("当前盘点的物料记录不存在，请检查参数是否正确!");
		}
		if (acturalNum < 0) {
			throw new OperationException("料盘的实际数量需要大于或等于0!");
		}
		Material material = Material.dao.findById(materialId);
		// 改变盘点记录
		inventoryLog.setActuralNum(acturalNum).setDifferentNum(acturalNum - inventoryLog.getBeforeNum()).setInventoryOperatior(user.getUid()).setInventoryTime(new Date()).update();
		if (MaterialType.dao.findById(material.getType()).getRadius().equals(7)) {
			if ((material.getRow().equals(-1) || material.getCol().equals(-1)) && acturalNum > 0) {
				MaterialHelper.getMaterialLocation(material, true);
				if (material.getRow().equals(-1) || material.getCol().equals(-1)) {
					throw new OperationException("位置不明，料盒已无空余位置");
				}
			}
			if (acturalNum == 0) {
				material.setCol(-1).setRow(-1);
				material.update();
			}
		}
		synchronized (Lock.UR_INV_TASK_LOCK) {
			List<UrMaterialInfo> infos = UrTaskInfoDAO.getUrMaterialInfos(taskId, boxId);
			if (infos != null && !infos.isEmpty()) {
				for (UrMaterialInfo urMaterialInfo : infos) {
					if (urMaterialInfo.getMaterialId().equals(material.getId())) {
						urMaterialInfo.setIsScaned(true);
						urMaterialInfo.setExceptionCode(0);
						break;
					}
				}
				UrMaterialInfo info1 = UrOperationMaterialInfoDAO.getUrOperationMaterialInfoByUrName("robot1");
				if (info1 != null && info1.getMaterialId().equals(materialId)) {
					UrOperationMaterialInfoDAO.removeUrTaskBoxArrivedPack("robot1");
				}
				UrTaskInfoDAO.putUrMaterialInfos(taskId, boxId, infos);
			}
		}
		return material;
	}


	/**
	 * 机械臂盘点UW物料
	 * 
	 * @param materialId
	 * @param boxId
	 * @param taskId
	 * @return
	 */
	public void inventoryUrUWMaterial(String materialId, Integer boxId, Integer taskId) {
		InventoryLog inventoryLog = InventoryLog.dao.findFirst(GET_INVENTORY_LOG_BY_BOX_AND_TASKID_AND_MATERIALID, boxId, taskId, materialId);
		Material material = Material.dao.findById(materialId);
		// 改变盘点记录
		inventoryLog.setActuralNum(material.getRemainderQuantity()).setDifferentNum(0).setInventoryOperatior("robot1").setInventoryTime(new Date()).update();
	}


	/**
	 * 平外仓物料
	 * 
	 * @param id
	 * @param taskId
	 * @param user
	 * @return
	 */
	public String coverEWhMaterial(Integer id, Integer taskId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null) {
			throw new OperationException("盘点任务不存在！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法平仓！");
		}
		ExternalInventoryLog externalInventoryLog = ExternalInventoryLog.dao.findById(id);
		if (externalInventoryLog == null) {
			throw new OperationException("该盘点记录不存在，请检查参数！");
		}
		InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID, task.getId(), externalInventoryLog.getWhId());
		if (info == null) {
			throw new OperationException("该仓库并未进行盘点！");
		}
		if (info.getCheckTime() == null) {
			throw new OperationException("该仓库盘点任务记录尚未审核，请先审核再平仓！");
		}
		if (info.getFinishTime() != null) {
			throw new OperationException("该仓库盘点任务已完成，无法平仓！");
		}

		Date date = new Date();
		if (externalInventoryLog.getCoverTime() != null) {
			throw new OperationException("该盘点记录已平仓，请不要重复平仓！");
		}

		externalInventoryLog.setEnabled(false);
		externalInventoryLog.setDifferentNum(0);
		externalInventoryLog.setCoverOperatior(user.getUid());
		externalInventoryLog.setCoverTime(date);
		externalInventoryLog.update();
		return "操作成功";
	}


	/**
	 * 物料仓批量平仓，根据任务ID和物料类型ID
	 * 
	 * @param materialTypeId
	 * @param taskId
	 * @param user
	 * @return
	 */
	public String coverEwhMaterialByTaskId(Integer materialTypeId, Integer taskId, Integer whId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法平仓！");
		}
		InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID, task.getId(), whId);
		if (info == null) {
			throw new OperationException("该仓库并未进行盘点！");
		}
		if (info.getCheckTime() == null) {
			throw new OperationException("该仓库盘点任务记录尚未审核，请先审核再平仓！");
		}
		if (info.getFinishTime() != null) {
			throw new OperationException("该仓库盘点任务已完成，无法平仓！");
		}
		Date date = new Date();
		List<ExternalInventoryLog> externalInventoryLogs = ExternalInventoryLog.dao.find(GET_UNCOVER_EWH_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE, taskId, whId, materialTypeId);
		for (ExternalInventoryLog externalInventoryLog : externalInventoryLogs) {
			externalInventoryLog.setEnabled(false);
			externalInventoryLog.setCoverOperatior(user.getUid());
			externalInventoryLog.setDifferentNum(0);
			externalInventoryLog.setCoverTime(date);
			externalInventoryLog.update();

		}
		return "操作成功";
	}


	/**
	 * 物料仓一键批量平仓
	 * 
	 * @param taskId
	 * @param user
	 * @return
	 */
	public String coverEwhMaterialOneKey(Integer taskId, Integer whId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法平仓！");
		}
		InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID, task.getId(), whId);
		if (info == null) {
			throw new OperationException("该仓库并未进行盘点！");
		}
		if (info.getCheckTime() == null) {
			throw new OperationException("该仓库盘点任务记录尚未审核，请先审核再平仓！");
		}
		if (info.getFinishTime() != null) {
			throw new OperationException("该仓库盘点任务已完成，无法平仓！");
		}
		Date date = new Date();
		List<ExternalInventoryLog> externalInventoryLogs = ExternalInventoryLog.dao.find(GET_UNCOVER_EWH_INVENTORY_LOG_BY_TASKID, taskId, whId);
		for (ExternalInventoryLog externalInventoryLog : externalInventoryLogs) {
			externalInventoryLog.setEnabled(false);
			externalInventoryLog.setCoverOperatior(user.getUid());
			externalInventoryLog.setDifferentNum(0);
			externalInventoryLog.setCoverTime(date);
			externalInventoryLog.update();
		}
		return "操作成功";
	}


	/**
	 * 审核任务
	 * 
	 * @param taskId
	 * @return
	 */
	public String checkUwTask(Integer taskId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法审核！");
		}
		InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID, task.getId(), uwId);
		if (info == null) {
			throw new OperationException("该仓库并未进行盘点！");
		}
		if (info.getCheckTime() != null) {
			throw new OperationException("盘点任务记录已经审核，请勿重复审核！");
		}
		if (info.getFinishTime() != null) {
			throw new OperationException("该仓库盘点任务已完成，无法审核！");
		}
		List<InventoryLog> logs = InventoryLog.dao.find(InventoryTaskSQL.GET_UN_INVENTORY_LOG_BY_TASKID, taskId);

		Window window = Window.dao.findFirst(SQL.GET_WINDOW_BY_TASKID, taskId);
		if (logs.size() > 0 || window != null) {
			throw new OperationException("UW仓盘点阶段未结束，请结束后再审核！");
		}
		Db.update(InventoryTaskSQL.UPDATE_INVENTORY_LOG_UNCOVER, taskId);

		info.setCheckTime(new Date()).setCheckOperator(user.getUid()).update();
		return "操作成功";
	}


	/**
	 * 审核任务
	 * 
	 * @param taskId
	 * @return
	 */
	public String checkEwhTask(Integer taskId, Integer whId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法审核！");
		}
		InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID, task.getId(), whId);
		if (info == null) {
			throw new OperationException("该仓库并未进行盘点！");
		}
		if (info.getCheckTime() != null) {
			throw new OperationException("盘点任务记录已经审核，请勿重复审核！");
		}
		if (info.getFinishTime() != null) {
			throw new OperationException("该仓库盘点任务已完成，无法审核！");
		}
		List<ExternalInventoryLog> logs1 = ExternalInventoryLog.dao.find(GET_UN_EWH_INVENTORY_LOG_BY_TASKID, taskId, whId);
		String nos = "";
		if (logs1.size() > 0) {
			for (ExternalInventoryLog externalInventoryLog : logs1) {
				if (externalInventoryLog.getActuralNum() == null) {
					if (nos.equals("")) {
						nos = String.valueOf(externalInventoryLog.getMaterialTypeId());
					} else {
						nos = nos + "," + String.valueOf(externalInventoryLog.getMaterialTypeId());
					}
				}
			}
			return "存在以下料号类型ID[" + nos + "]未盘点";
		}
		Db.update(UPDATE_EWH_INVENTORY_LOG_UNCOVER, taskId);

		info.setCheckTime(new Date()).setCheckOperator(user.getUid()).update();
		return "操作成功";
	}


	/**
	 * 导入外仓盘点数据
	 * 
	 * @param file
	 * @param taskId
	 * @param user
	 * @return
	 */
	public String importEWhInventoryRecord(File file, Integer taskId, Integer whId, User user) {
		String resultString = "导入成功";
		synchronized (RegularTaskLock.IMPORT_EWH_INVENTORY_FILE_LOCK) {

			ExcelHelper fileReader;
			Task task = Task.dao.findById(taskId);
			if (task == null || !task.getType().equals(TaskType.COUNT) || !task.getState().equals(TaskState.PROCESSING)) {
				throw new OperationException("盘点任务不存在或未处于进行中状态，请检查参数是否正确！");
			}
			InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID, task.getId(), whId);
			if (info == null) {
				throw new OperationException("该仓库并未进行盘点！");
			}
			if (info.getFinishTime() != null) {
				throw new OperationException("该仓库盘点任务已完成，无法导入！");
			}
			try {
				fileReader = ExcelHelper.from(file);
				List<EWhInventoryRecordBO> items = fileReader.unfill(EWhInventoryRecordBO.class, 0);
				if (items == null) {
					throw new OperationException("表格无有效数据或者表格格式不正确！");
				}
				int i = 0;
				Map<Integer, Set<Integer>> inventoryDataMap = new HashMap<>();

				List<ExternalInventoryLog> externalInventoryLogs = new ArrayList<>();
				for (EWhInventoryRecordBO item : items) {
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) { // 只读取有序号的行数据
						if (item.getNo() == null || item.getQuantity() == null || item.getNo().replaceAll(" ", "").equals("") || item.getQuantity().toString().replaceAll(" ", "").equals("")
								|| item.getWhName() == null || item.getWhName().trim().equals("")) {
							resultString = "导入失败，请检查表格第" + i + "行的料号或数量是否填写了准确信息！";
							return resultString;
						}

						if (item.getQuantity() < 0) {
							resultString = "导入失败，表格第" + i + "行的数量为" + item.getQuantity() + "，数量必须大于等于0！";
							return resultString;
						}
						Destination destination = Destination.dao.findById(whId);

						if (destination == null) {
							resultString = "导入失败，目的仓库不存在";
							return resultString;
						}
						if (destination.getId().equals(0)) {
							resultString = "导入失败，不可导入无人仓库的盘点数据！！";
							return resultString;
						}
						if (!destination.getName().trim().equals(item.getWhName().trim())) {
							resultString = "导入失败，存在不属于该仓库的盘点数据！！";
							return resultString;
						}
						String no = item.getNo().trim().toUpperCase();
						// 根据料号找到对应的物料类型
						MaterialType mType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_AND_TYPE_SQL, no, task.getSupplier(), WarehouseType.REGULAR.getId());
						// 判断物料类型表中是否存在对应的料号且未被禁用，若不存在，则将对应的任务记录删除掉，并提示操作员检查套料单、新增对应的物料类型
						if (mType == null) {
							resultString = "导入失败，料号为" + item.getNo() + "的物料没有记录在物料类型表中或已被禁用，或者是客户与料号对应不上！";
							return resultString;
						}
						if (inventoryDataMap.get(mType.getId()) != null && inventoryDataMap.get(mType.getId()).contains(destination.getId())) {
							resultString = "导入失败，料号为" + item.getNo() + "的物料表中存在重复项！";
							return resultString;
						}
						if (inventoryDataMap.get(mType.getId()) == null) {
							Set<Integer> whMaterialSet = new HashSet<>();
							whMaterialSet.add(destination.getId());
							inventoryDataMap.put(mType.getId(), whMaterialSet);
						} else {
							inventoryDataMap.get(mType.getId()).add(destination.getId());
						}

						ExternalInventoryLog externalInventoryLog = ExternalInventoryLog.dao.findFirst(GET_EWH_INVENTORY_LOG_BY_TASKID_MATERIALTYPEID_WHID, taskId, destination.getId(), mType.getId());

						// 如果没有记录就说明该物料仓这个物料一开始没有进过这个料
						if (externalInventoryLog == null) {
							externalInventoryLog = new ExternalInventoryLog();
							externalInventoryLog.setTaskId(taskId);
							externalInventoryLog.setWhId(destination.getId());
							externalInventoryLog.setMaterialTypeId(mType.getId());
							externalInventoryLog.setBeforeNum(0);
							externalInventoryLog.setActuralNum(item.getQuantity());
							externalInventoryLog.setEnabled(true);
							externalInventoryLog.setDifferentNum(item.getQuantity() - 0);
							externalInventoryLog.setMaterialReturnNum(0);
							externalInventoryLog.setInventoryOperatior(user.getUid());
							externalInventoryLog.setInventoryTime(new Date());
						} else {
							externalInventoryLog.setActuralNum(item.getQuantity());
							externalInventoryLog.setEnabled(true);
							externalInventoryLog.setDifferentNum(item.getQuantity() - externalInventoryLog.getBeforeNum() + externalInventoryLog.getMaterialReturnNum());
							externalInventoryLog.setInventoryOperatior(user.getUid());
							externalInventoryLog.setCoverOperatior(null);
							externalInventoryLog.setCoverTime(null);
							externalInventoryLog.setInventoryTime(new Date());
						}
						externalInventoryLogs.add(externalInventoryLog);
						i++;
					} else {
						break;
					}
				}

				for (ExternalInventoryLog externalInventoryLog : externalInventoryLogs) {
					if (externalInventoryLog.getId() == null) {
						externalInventoryLog.save();
					} else {
						externalInventoryLog.update();
					}
				}
				info.setCheckTime(null).setCheckOperator(null).update();

			} catch (Exception e) {
				e.printStackTrace();
				throw new OperationException(e.getMessage());
			} finally {
				file.delete();
			}

		}
		return resultString;
	}


	/**
	 * 获取当前仓口的盘点物料清单
	 * 
	 * @param windowId
	 * @return
	 */
	public List<PackingInventoryInfoVO> getPackingInventory(Integer windowId) {
		Window window = Window.dao.findById(windowId);
		if (window == null || window.getBindTaskId() == null) {
			throw new OperationException("仓口不存在任务！");
		}
		int boxId = 0;
		Map<Integer, PackingInventoryInfoVO> map = new LinkedHashMap<>();
		List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, windowId);
		for (GoodsLocation goodsLocation : goodsLocations) {
			map.put(goodsLocation.getId(), new PackingInventoryInfoVO(goodsLocation, new ArrayList<>()));
		}
		for (AGVInventoryTaskItem inventoryTaskItem : InventoryTaskItemRedisDAO.getInventoryTaskItems(window.getBindTaskId())) {
			if (inventoryTaskItem.getWindowId().equals(windowId) && inventoryTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW)) {
				boxId = inventoryTaskItem.getBoxId();
				PackingInventoryInfoVO info = map.get(inventoryTaskItem.getGoodsLocationId());
				if (info == null) {
					throw new OperationException("仓口 " + windowId + "没有对应货位" + inventoryTaskItem.getGoodsLocationId() + "！");
				}
				if (info.getBoxId() != null) {
					throw new OperationException("仓口 " + windowId + "的货位" + inventoryTaskItem.getGoodsLocationId() + "有一个以上的到站任务条目，请检查！");
				}
				MaterialBox materialBox = MaterialBox.dao.findById(inventoryTaskItem.getBoxId());
				if (materialBox == null) {
					throw new OperationException("料盒不存在或者未启用！");
				}
				Supplier supplier = Supplier.dao.findById(materialBox.getSupplier());
				List<MaterialDetialsVO> materialInfoVOs = info.getList();
				List<Record> records = Db.find(MaterialSQL.GET_ENTITIES_SELECT_SQL + MaterialSQL.GET_ENTITIES_BY_BOX_EXCEPT_SELECT_SQL, boxId, supplier.getId());
				for (Record record : records) {
					MaterialDetialsVO materialInfoVO = new MaterialDetialsVO();
					materialInfoVO.setMaterialTypeId(record.getInt("Material_MaterialTypeId"));
					materialInfoVO.setMaterialId(record.getStr("Material_Id"));
					materialInfoVO.setNo(record.getStr("MaterialType_No"));
					materialInfoVO.setSpecification(record.getStr("MaterialType_Specification"));
					materialInfoVO.setStoreNum(record.getInt("Material_RemainderQuantity"));
					materialInfoVO.setSupplierId(supplier.getId());
					materialInfoVO.setSupplierName(supplier.getName());
					materialInfoVO.setProductionTime(record.getDate("Material_ProductionTime"));
					materialInfoVO.setRow(record.getInt("Material_Row"));
					materialInfoVO.setCol(record.getInt("Material_Col"));
					InventoryLog inventoryLog = InventoryLog.dao.findFirst(GET_INVENTORY_LOG_BY_BOX_AND_TASKID_AND_MATERIALID, boxId, window.getBindTaskId(), record.getStr("Material_Id"));
					if (inventoryLog != null && inventoryLog.getActuralNum() != null) {
						materialInfoVO.setActualNum(inventoryLog.getActuralNum());
					}
					materialInfoVOs.add(materialInfoVO);
				}
				// 当盘点任务仓口为机械臂盘点仓口时
				if (window.getAuto()) {
					List<UrMaterialInfo> urMaterialInfos = UrTaskInfoDAO.getUrMaterialInfos(inventoryTaskItem.getTaskId(), boxId);
					// 设置物料的扫描状态和异常状态
					if (!urMaterialInfos.isEmpty()) {
						Set<String> materialScanSet = new HashSet<String>();
						Map<String, Integer> materialExceptionMap = new HashMap<>();
						for (UrMaterialInfo urMaterialInfo : urMaterialInfos) {
							if (urMaterialInfo.getIsScaned()) {
								materialScanSet.add(urMaterialInfo.getMaterialId());
							}
							materialExceptionMap.put(urMaterialInfo.getMaterialId(), urMaterialInfo.getExceptionCode());
						}
						for (MaterialDetialsVO materialDetialsVO : materialInfoVOs) {
							if (materialExceptionMap.containsKey(materialDetialsVO.getMaterialId())) {
								materialDetialsVO.setExceptionCode(materialExceptionMap.get(materialDetialsVO.getMaterialId()));
								if (materialScanSet.contains(materialDetialsVO.getMaterialId())) {
									materialDetialsVO.setIsScaned(true);
								}
							}
						}
					}
				}
				info.setBoxId(boxId);
				info.setTaskId(window.getBindTaskId());
				info.setWindowId(windowId);
				info.setList(materialInfoVOs);
			}
		}
		return new ArrayList<>(map.values());
	}


	public void exportEwhInventoryTask(Integer taskId, String no, Integer whId, String fileName, OutputStream output) throws IOException {
		SqlPara sqlPara = new SqlPara();
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (no == null) {
			sqlPara.setSql(GET_EXPORT_EWH_INVENTORY_INFO);
			sqlPara.addPara(taskId);
			sqlPara.addPara(whId);
		} else {
			sqlPara.setSql(GET_EXPORT_EWH_INVENTORY_INFO_BY_NO);
			sqlPara.addPara(taskId);
			sqlPara.addPara(whId);
			sqlPara.addPara("%" + no + "%");
		}
		List<Record> inventoryRecords = Db.find(sqlPara);

		String[] field = null;
		String[] head = null;
		field = new String[] { "wh_name", "supplier_name", "no", "before_num", "actural_num", "return_num", "different_num" };
		head = new String[] { "仓库名", "客户", "料号", "盘前数量", "盘点数量", "退料", "盈亏" };
		ExcelWritter writter = ExcelWritter.create(true);
		writter.fill(inventoryRecords, fileName, field, head);
		writter.write(output, true);
	}


	public List<Record> getEwhInventoryTaskInfo(Integer taskId, Integer whId, String no) {
		SqlPara sqlPara = new SqlPara();
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (no == null) {
			sqlPara.setSql(GET_EWH_INVENTORY_TASK_INFO);
			sqlPara.addPara(taskId);
			sqlPara.addPara(whId);
		} else {
			sqlPara.setSql(GET_EWH_INVENTORY_TASK_INFO_BY_NO);
			sqlPara.addPara(taskId);
			sqlPara.addPara(whId);
			sqlPara.addPara("%" + no + "%");
		}
		List<Record> inventoryRecords = Db.find(sqlPara);
		return inventoryRecords;
	}


	public List<InventoryTaskDetailVO> getUwInventoryTaskDetails(Integer taskId, Integer materialTypeId) {
		List<InventoryLog> inventoryLogs = InventoryLog.dao.find(GET_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE, materialTypeId, taskId);

		List<InventoryTaskDetailVO> details = new ArrayList<>();
		for (InventoryLog inventoryLog : inventoryLogs) {
			InventoryTaskDetailVO inventoryTaskDetail = new InventoryTaskDetailVO();
			inventoryTaskDetail.setId(inventoryLog.getId());
			inventoryTaskDetail.setTaskId(inventoryLog.getTaskId());
			inventoryTaskDetail.setMaterialId(inventoryLog.getMaterialId());
			inventoryTaskDetail.setMaterialTypeId(materialTypeId);
			inventoryTaskDetail.setBeforeNum(inventoryLog.getBeforeNum());
			inventoryTaskDetail.setAtrualNum(inventoryLog.getActuralNum());
			inventoryTaskDetail.setDifferentNum(inventoryLog.getDifferentNum());
			inventoryTaskDetail.setInventoryOperatior(inventoryLog.getInventoryOperatior());
			inventoryTaskDetail.setInventoryTime(inventoryLog.getInventoryTime());
			inventoryTaskDetail.setCoverOperatior(inventoryLog.getCoverOperatior());
			inventoryTaskDetail.setCoverTime(inventoryLog.getCoverTime());
			inventoryTaskDetail.setWhId(0);
			inventoryTaskDetail.setWhName("UW无人仓库");
			inventoryTaskDetail.setProductionTime(inventoryLog.getDate("production_time"));
			details.add(inventoryTaskDetail);
		}

		return details;
	}


	public List<InventoryTaskDetailVO> getEwhInventoryTaskDetails(Integer taskId, Integer materialTypeId, Integer whId) {

		List<ExternalInventoryLog> externalInventoryLogs = ExternalInventoryLog.dao.find(GET_EWH_INVENTORY_LOG_BY_TASKID_MATERIALTYPEID, taskId, materialTypeId, whId);

		List<InventoryTaskDetailVO> details = new ArrayList<>();

		for (ExternalInventoryLog externalInventoryLog : externalInventoryLogs) {
			InventoryTaskDetailVO inventoryTaskDetail = new InventoryTaskDetailVO();
			inventoryTaskDetail.setId(externalInventoryLog.getId());
			inventoryTaskDetail.setTaskId(externalInventoryLog.getTaskId());
			inventoryTaskDetail.setMaterialTypeId(materialTypeId);
			inventoryTaskDetail.setBeforeNum(externalInventoryLog.getBeforeNum());
			inventoryTaskDetail.setAtrualNum(externalInventoryLog.getActuralNum());
			inventoryTaskDetail.setDifferentNum(externalInventoryLog.getDifferentNum());
			inventoryTaskDetail.setMaterialreturnNum(externalInventoryLog.getMaterialReturnNum());
			inventoryTaskDetail.setInventoryOperatior(externalInventoryLog.getInventoryOperatior());
			inventoryTaskDetail.setInventoryTime(externalInventoryLog.getInventoryTime());
			inventoryTaskDetail.setCoverOperatior(externalInventoryLog.getCoverOperatior());
			inventoryTaskDetail.setCoverTime(externalInventoryLog.getCoverTime());
			inventoryTaskDetail.setWhId(externalInventoryLog.getWhId());
			inventoryTaskDetail.setWhName(externalInventoryLog.getStr("wh_name"));
			details.add(inventoryTaskDetail);
		}
		return details;
	}


	public String finishTask(Integer taskId, Integer whId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法完成！");
		}
		InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID, task.getId(), whId);
		if (info == null) {
			throw new OperationException("该仓库尚未进行盘点，无法完成！");
		}
		if (info.getCheckTime() == null) {
			throw new OperationException("该仓库盘点任务未审核，无法完成！");
		}

		ExternalInventoryLog log1 = ExternalInventoryLog.dao.findFirst(GET_UNCOVER_EWH_INVENTORY_LOG_BY_TASKID, taskId, whId);
		InventoryLog log2 = InventoryLog.dao.findFirst(InventoryTaskSQL.GET_UNCOVER_INVENTORY_LOG_BY_TASKID, taskId);
		if (log1 == null && log2 == null) {

			List<ExternalInventoryLog> externalInventoryLogs = ExternalInventoryLog.dao.find(GET_ALL_EWH_INVENTORY_LOG_BY_TASK, taskId, whId);
			List<ExternalWhLog> newExternalLogs = new ArrayList<>(externalInventoryLogs.size());
			for (ExternalInventoryLog externalInventoryLog : externalInventoryLogs) {
				ExternalWhLog externalWhLog = new ExternalWhLog();
				externalWhLog.setMaterialTypeId(externalInventoryLog.getMaterialTypeId());
				externalWhLog.setTaskId(taskId);
				externalWhLog.setSourceWh(externalInventoryLog.getWhId());
				externalWhLog.setDestination(externalInventoryLog.getWhId());
				externalWhLog.setQuantity(externalInventoryLog.getActuralNum());
				externalWhLog.setOperatior(externalInventoryLog.getCoverOperatior());
				externalWhLog.setTime(new Date());
				externalWhLog.setOperationTime(new Date());
				newExternalLogs.add(externalWhLog);
			}

			Db.batchSave(newExternalLogs, batchSize);
			info.setFinishTime(new Date()).setFinishOperator(user.getUid()).update();
			List<InventoryTaskBaseInfo> infos = InventoryTaskBaseInfo.dao.find(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID, task.getId());
			boolean flag = true;
			for (InventoryTaskBaseInfo inventoryTaskBaseInfo : infos) {
				if (!inventoryTaskBaseInfo.getId().equals(info.getId()) && inventoryTaskBaseInfo.getFinishTime() == null) {
					flag = false;
				}
			}

			if (flag) {
				task.setState(TaskState.FINISHED);
				task.setEndTime(new Date());
				task.update();
			}
		} else {
			throw new OperationException("盘点任务存在未平仓的盘点条目，无法完成！");
		}

		return "操作成功！";
	}


	/**
	 * 
	 * <p>
	 * Description: 修改物料仓盘点数据
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月25日
	 */
	public String editEwhInventoryLog(Integer id, Integer acturanlNum, Integer returnNum, User user) {
		ExternalInventoryLog externalInventoryLog = ExternalInventoryLog.dao.findById(id);

		if (externalInventoryLog == null) {
			throw new OperationException("该盘点记录不存在，请检查！");
		}
		Task task = Task.dao.findById(externalInventoryLog.getTaskId());
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("该盘点任务不处于进行中状态，无法修改，请检查！");
		}
		InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID, task.getId(), externalInventoryLog.getWhId());
		if (info == null) {
			throw new OperationException("该仓库尚未进行盘点，无法修改！");
		}
		if (info.getFinishTime() != null) {
			throw new OperationException("该仓库盘点任务已完成，无法修改！");
		}
		externalInventoryLog.setActuralNum(acturanlNum);
		externalInventoryLog.setMaterialReturnNum(returnNum);
		externalInventoryLog.setDifferentNum(acturanlNum - externalInventoryLog.getBeforeNum() + returnNum);
		externalInventoryLog.setInventoryOperatior(user.getUid());
		externalInventoryLog.setInventoryTime(new Date());
		externalInventoryLog.setCoverOperatior(null);
		externalInventoryLog.setCoverTime(null);
		externalInventoryLog.setEnabled(true);
		externalInventoryLog.update();
		info.setCheckTime(null).setCheckOperator(null).update();
		return "操作成功！";
	}

}
