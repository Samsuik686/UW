package com.jimi.uw_server.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jfinal.aop.Enhancer;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.agv.handle.InvTaskHandler;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.constant.SQL;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.Destination;
import com.jimi.uw_server.model.ExternalInventoryLog;
import com.jimi.uw_server.model.ExternalWhLog;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.InventoryLog;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.EWhInventoryRecordBO;
import com.jimi.uw_server.model.vo.ExternalWhInfoVO;
import com.jimi.uw_server.model.vo.InventoryTaskDetailVO;
import com.jimi.uw_server.model.vo.InventoryTaskVO;
import com.jimi.uw_server.model.vo.MaterialInfoVO;
import com.jimi.uw_server.model.vo.PackingInventoryInfoVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ExcelHelper;
import com.jimi.uw_server.util.ExcelWritter;

/**
 * 
 * @author trjie
 * @createTime 2019年5月16日  上午10:06:35
 */

public class InventoryTaskService {
	

	public static InventoryTaskService me = new InventoryTaskService();
	
	private static final String GET_ALL_WINDOWS = "SELECT * FROM window";
	
	private static final String GET_NOT_ON_SHELF_BOX = "SELECT * FROM material_box WHERE material_box.supplier = ? AND material_box.is_on_shelf = 0 AND material_box.enabled = 1";
	
	private static final String GET_NOT_IN_BOX_MATERIAL = "SELECT material.id FROM material INNER JOIN material_box ON material.box = material_box.id WHERE material.remainder_quantity > 0 AND material_box.supplier = ? AND material_box.enabled = 1 AND material.is_in_box = 0";
	
	private static final String GET_ALL_BOX_BY_SUPPLIER = "SELECT DISTINCT material_box.id AS box FROM material_box INNER JOIN material ON material_box.id = material.box WHERE material.remainder_quantity > 0 AND material_box.supplier = ? AND material_box.enabled = 1";	

	private static final String GET_ALL_MATERIAL_BY_SUPPLIER = "SELECT material.* FROM material_box INNER JOIN material ON material_box.id = material.box WHERE material.remainder_quantity > 0 AND material_box.supplier = ? AND material_box.enabled = 1";
	
	private static final String GET_INVENTORY_LOG_BY_BOX_AND_TASKID = "SELECT * FROM inventory_log WHERE inventory_log.box_id = ? AND inventory_log.task_id = ?";
	
	private static final String GET_INVENTORY_LOG_BY_BOX_AND_TASKID_AND_MATERIALID = "SELECT * FROM inventory_log WHERE inventory_log.box_id = ? AND inventory_log.task_id = ? AND inventory_log.material_id = ?";
	
	private static final String GET_UNCOVER_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE = "SELECT inventory_log.* FROM inventory_log INNER JOIN material ON inventory_log.material_id = material.id WHERE material.type = ? AND inventory_log.task_id = ? AND inventory_log.enabled = 1";
		
	private static final String GET_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE = "SELECT inventory_log.* FROM inventory_log INNER JOIN material ON inventory_log.material_id = material.id WHERE material.type = ? AND inventory_log.task_id = ?";

	private static final String GET_UN_INVENTORY_LOG_BY_TASKID = "SELECT inventory_log.* FROM inventory_log INNER JOIN material ON inventory_log.material_id = material.id WHERE inventory_log.task_id = ? AND inventory_log.inventory_time is null AND inventory_log.enabled = 1";

	private static final String GET_EWH_INVENTORY_LOG_BY_TASKID_MATERIALTYPEID_WHID = "SELECT * FROM external_inventory_log WHERE task_id = ? AND wh_id = ? AND material_type_id = ? AND enabled = 1";
	
	private static final String GET_EWH_INVENTORY_LOG_BY_TASKID_MATERIALTYPEID = "SELECT external_inventory_log.*, destination.`name` as wh_name FROM external_inventory_log INNER JOIN destination ON external_inventory_log.wh_id = destination.id WHERE external_inventory_log.task_id = ? AND external_inventory_log.material_type_id = ?";
	
	private static final String GET_UN_EWH_INVENTORY_LOG_BY_TASKID = "SELECT * FROM external_inventory_log WHERE task_id = ? AND external_inventory_log.inventory_time IS NULL AND enabled = 1";
	
	private static final String GET_MATERIAL_TYPE_BY_NO_SQL = "SELECT * FROM material_type WHERE no = ? AND supplier = ? AND enabled = 1";
	
	private static final String GET_DESTINATION_BY_DESTINATION_NAME = "SELECT * FROM destination WHERE name = ? AND enabled = 1";
	
	private static final String GET_UNCOVER_EWH_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE = "SELECT * FROM external_inventory_log WHERE task_id = ? AND material_type_id = ? AND enabled = 1";
	
	private static final String GET_ALL_EWH_INVENTORY_LOG_BY_TASK = "SELECT * FROM external_inventory_log WHERE task_id = ?";
	
	private static final String GET_UNCOVER_EWH_INVENTORY_LOG_BY_TASKID = "SELECT * FROM external_inventory_log WHERE task_id = ? AND enabled = 1";
	
	private static final String GET_UNCOVER_INVENTORY_LOG_BY_TASKID = "SELECT * FROM inventory_log WHERE task_id = ? AND enabled = 1";
	
	private static final String GET_MATERIAL_INFO_BY_BOX = "SELECT material.id as id, material.type as material_type_id, material.remainder_quantity as quantity, material.production_time as production_time, material_type.`no` as `no`, material_type.specification as specification, supplier.id as supplier_id, supplier.`name` as supplier_name FROM material INNER JOIN material_type INNER JOIN supplier ON material.type = material_type.id AND supplier.id = material_type.supplier WHERE material.box = ? AND material.is_in_box = 1 AND remainder_quantity > 0";
	
	private static final String GET_INVENTORY_TASK_BY_SUPPLIERID = "SELECT * FROM task WHERE type = ? AND supplier = ? AND state != 1 AND state != 4 ORDER BY create_time DESC";
		
	private static final String GET_EWH_INVENTORY_TASK_INFO = "SELECT external_inventory_log.material_type_id AS material_type_id, material_type.`no` AS `no`, material_type.specification, external_inventory_log.task_id AS task_id, task.file_name AS task_name, task.start_time, task.end_time, task.state, task.ewh_checked_operatior AS check_operatior, task.ewh_checked_time AS check_time, SUM(external_inventory_log.before_num ) AS before_num, SUM(external_inventory_log.actural_num ) AS actural_num, SUM( external_inventory_log.different_num ) AS different_num, SUM(external_inventory_log.material_return_num) as material_return_num, supplier.id AS supplier_id, supplier.`name` AS supplier_name, external_inventory_log.inventory_operatior FROM external_inventory_log INNER JOIN material_type INNER JOIN task INNER JOIN supplier ON external_inventory_log.task_id = task.id AND external_inventory_log.material_type_id = material_type.id AND supplier.id = material_type.supplier WHERE external_inventory_log.task_id = ? GROUP BY external_inventory_log.material_type_id ORDER BY material_type.`no` DESC";
	
	private static final String GET_UW_INVWNTORY_TASK_INFO = "SELECT material_type.id AS material_type_id, material_type.`no`, material_type.specification, inventory_log.task_id, task.file_name AS task_name, task.start_time, task.end_time, task.state, task.uw_checked_operatior AS check_operatior, task.uw_checked_time AS check_time, SUM(before_num) AS before_num, SUM(actural_num) AS actural_num, SUM(different_num) AS different_num, supplier.id AS supplier_id, supplier.`name` AS supplier_name, inventory_log.inventory_operatior AS inventory_operatior FROM inventory_log INNER JOIN task INNER JOIN material INNER JOIN material_type INNER JOIN supplier ON inventory_log.task_id = task.id AND inventory_log.material_id = material.id AND material_type.id = material.type AND material_type.supplier = supplier.id WHERE inventory_log.task_id = ? GROUP BY material.type ORDER BY material_type.`no` DESC";
	
	private static final String GET_EWH_INVENTORY_TASK_INFO_BY_NO = "SELECT external_inventory_log.material_type_id AS material_type_id, material_type.`no` AS `no`, material_type.specification, external_inventory_log.task_id AS task_id, task.file_name AS task_name, task.start_time, task.end_time, task.state, task.ewh_checked_operatior AS check_operatior, task.ewh_checked_time AS check_time, SUM(external_inventory_log.before_num ) AS before_num, SUM(external_inventory_log.actural_num ) AS actural_num, SUM( external_inventory_log.different_num ) AS different_num, SUM(external_inventory_log.material_return_num) as material_return_num, supplier.id AS supplier_id, supplier.`name` AS supplier_name, external_inventory_log.inventory_operatior FROM external_inventory_log INNER JOIN material_type INNER JOIN task INNER JOIN supplier ON external_inventory_log.task_id = task.id AND external_inventory_log.material_type_id = material_type.id AND supplier.id = material_type.supplier WHERE external_inventory_log.task_id = ? AND material_type.`no` like ? GROUP BY external_inventory_log.material_type_id ORDER BY material_type.`no` DESC";
	
	private static final String GET_UW_INVWNTORY_TASK_INFO_BY_NO = "SELECT material_type.id AS material_type_id, material_type.`no`, material_type.specification, inventory_log.task_id, task.file_name AS task_name, task.start_time, task.end_time, task.state, task.uw_checked_operatior AS check_operatior, task.uw_checked_time AS check_time, SUM(before_num) AS before_num, SUM(actural_num) AS actural_num, SUM(different_num) AS different_num, supplier.id AS supplier_id, supplier.`name` AS supplier_name, inventory_log.inventory_operatior AS inventory_operatior FROM inventory_log INNER JOIN task INNER JOIN material INNER JOIN material_type INNER JOIN supplier ON inventory_log.task_id = task.id AND inventory_log.material_id = material.id AND material_type.id = material.type AND material_type.supplier = supplier.id WHERE inventory_log.task_id = ? AND material_type.`no` like ? GROUP BY material.type ORDER BY material_type.`no` DESC";
		
	private static final String GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER = "SELECT * FROM task where state = 2 and type = 2 and supplier = ?";

	private static final String GET_UNSTART_INVENTORY_TASK_BY_SUPPLIER = "SELECT * FROM task where state = 1 and type = 2 and supplier = ? order by create_time desc";
	
	private static final String UPDATE_INVENTORY_LOG_UNCOVER = "UPDATE inventory_log SET enabled = 0 WHERE different_num = 0 AND task_id = ?";
	
	private static final String UPDATE_EWH_INVENTORY_LOG_UNCOVER = "UPDATE external_inventory_log SET enabled = 0 WHERE different_num = 0 AND task_id = ?";
	
	private static final String GET_USEFUL_TASK_BY_TYPE_SUPPLIER = "select * from task where task.type = ? and task.state < 3 and task.supplier = ?";
	
	private static final String UPDATE_MATERIAL_RETURN_RECORD_UNENABLED = "UPDATE material_return_record SET enabled = 0 WHERE time <= ? AND enabled = 1 AND material_type_id IN (SELECT id from material_type WHERE material_type.supplier = ? AND material_type.enabled = 1)";
	
	private static ExternalWhTaskService externalWhTaskService = ExternalWhTaskService.me;
	
	private static InvTaskHandler invTaskHandler = InvTaskHandler.getInstance();
	
	private static SelectService selectService = Enhancer.enhance(SelectService.class);
	
	/**
	 * 创建盘点任务
	 * @param supplierId
	 * @return
	 */
	public String createInventoryTask(Integer supplierId) {
		Supplier supplier = Supplier.dao.findById(supplierId);
		if (supplier == null) {
			throw new OperationException("供应商不存在！");
		}
		Task task = Task.dao.findFirst(GET_USEFUL_TASK_BY_TYPE_SUPPLIER, TaskType.COUNT, supplierId);
		if (task != null) {
			throw new OperationException("该供应商已存在未开始或进行中的盘点任务，无法创建新的盘点任务！");
		}
		task = new Task();
		Date date = new Date();
		task.setFileName(getTaskName(date));
		task.setCreateTime(date);
		task.setType(TaskType.COUNT);
		task.setState(TaskState.WAIT_START);
		task.setSupplier(supplierId);
		task.save();
		return "操作成功";
	}
	

	/**
	 * 开始盘点任务
	 * @param taskId
	 * 
	 * @return
	 */
	public String startInventoryTask(Integer taskId, String windows) {
		List<Window> windowList = Window.dao.find(GET_ALL_WINDOWS);
		Task task = Task.dao.findById(taskId);
		if (task == null) {
			throw new OperationException("任务不存在!");
		}else if (!task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("该任务并非盘点任务！");
		}else if (!task.getState().equals(TaskState.WAIT_START)) {
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
			//盘点期间该供应商不得出入库
			for (Window window : windowList) {
				if (window.getBindTaskId() == null) {
					continue;
				}
				Task tempTask = Task.dao.findFirst(GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER, task.getSupplier());
				if (tempTask != null) {
					throw new OperationException("当前盘点的供应商存在进行中的盘点任务，请等待任务结束后再开始盘点任务!");
				}
				tempTask = Task.dao.findById(window.getBindTaskId());
				if (tempTask != null && tempTask.getSupplier().equals(task.getSupplier())) {
					throw new OperationException("当前盘点的供应商存在其他出入库任务，请等待其他任务结束后再开始盘点任务!");
				}
			}
			
			//盘点前该供应商的料盒必须在架
			MaterialBox materialBox = MaterialBox.dao.findFirst(GET_NOT_ON_SHELF_BOX, task.getSupplier());
			if (materialBox != null) {
				throw new OperationException("当前盘点的供应商存在不在架的料盒，请检查!");
			}
			Material material = Material.dao.findFirst(GET_NOT_IN_BOX_MATERIAL, task.getSupplier());
			if (material != null) {
				throw new OperationException("当前盘点的供应商存在不在料盒内的物料，请检查!");
			}
			task.setState(TaskState.PROCESSING);
			task.setStartTime(new Date());
			List<AGVInventoryTaskItem> agvInventoryTaskItems = new ArrayList<>();
			List<MaterialBox> materialBoxs = MaterialBox.dao.find(GET_ALL_BOX_BY_SUPPLIER, task.getSupplier());
			for (MaterialBox box : materialBoxs) {
				AGVInventoryTaskItem agvInventoryTaskItem = new AGVInventoryTaskItem(taskId, box.getInt("box"), TaskItemState.WAIT_ASSIGN, 0, 0);
				agvInventoryTaskItems.add(agvInventoryTaskItem);
			}
			
			List<Material> materials = Material.dao.find(GET_ALL_MATERIAL_BY_SUPPLIER, task.getSupplier());
			for (Material material2 : materials) {
				InventoryLog inventoryLog = new InventoryLog();
				inventoryLog.setMaterialId(material2.getId());
				inventoryLog.setTaskId(task.getId());
				inventoryLog.setBeforeNum(material2.getRemainderQuantity());
				inventoryLog.setBoxId(material2.getBox());
				inventoryLog.setEnabled(true);
				inventoryLog.save();
			}
			List<ExternalWhInfoVO> externalWhInfoVOs = externalWhTaskService.selectExternalWhInfo(null, task.getSupplier(), null, task);
			
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
				externalInventoryLog.save();
				
				ExternalWhLog externalWhLog = new ExternalWhLog();
				externalWhLog.setMaterialTypeId(externalWhInfoVO.getMaterialTypeId());
				externalWhLog.setTaskId(taskId);
				externalWhLog.setSourceWh(externalWhInfoVO.getWhId());
				externalWhLog.setDestination(externalInventoryLog.getWhId());
				externalWhLog.setQuantity(0 - externalWhInfoVO.getQuantity());
				externalWhLog.setTime(new Date());
				externalWhLog.save();
			}
			Db.update(UPDATE_MATERIAL_RETURN_RECORD_UNENABLED, task.getCreateTime(), task.getSupplier());
			task.update();
			if (agvInventoryTaskItems.size() <= 0) {
				 return "当前供应商在UW无人仓没有物料，无需绑定仓口！";
			}
			for (Window window : wList) {
				window.setBindTaskId(task.getId());
				window.update();
			}
			TaskItemRedisDAO.addInventoryTaskItem(taskId, agvInventoryTaskItems);
		}
		return "操作成功";
	}
	
	
	/**
	 * 让盘点任务的叉车回库
	 * @param taskId
	 * @param boxId
	 * @param windowId
	 * @param user
	 * @return
	 */
	public String backInventoryBox(Integer taskId, Integer boxId, Integer windowId, User user) {
		synchronized (Lock.INV_TASK_BACK_LOCK) {
			
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
				throw new OperationException("当前盘点的料盒不存在，请检查参数是否正确!");
			}
			Window window = Window.dao.findById(windowId);
			if (window == null) {
				throw new OperationException("当前盘点的仓口不存在，请检查参数是否正确!");
			}
			for (AGVInventoryTaskItem inventoryTaskItem : TaskItemRedisDAO.getInventoryTaskItems(taskId)) {
				if ( inventoryTaskItem.getBoxId().equals(boxId) && inventoryTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW)) {
					try {
						GoodsLocation goodsLocation = GoodsLocation.dao.findById(inventoryTaskItem.getGoodsLocationId());
						if (goodsLocation != null) {
							invTaskHandler.sendBackLL(inventoryTaskItem, materialBox, goodsLocation);
						}else {
							throw new OperationException("找不到目的货位，仓口：" + inventoryTaskItem.getWindowId() + "货位：" + inventoryTaskItem.getGoodsLocationId());
						}
						TaskItemRedisDAO.updateInventoryTaskItemInfo(inventoryTaskItem, TaskItemState.START_BACK, null, null, null, true);
					} catch (Exception e) {
						e.printStackTrace();
						throw new OperationException(e.getMessage());
					}
					break;
				}
			}
			for (InventoryLog inventoryLog : inventoryLogs) {
				if (!inventoryLog.getBeforeNum().equals(inventoryLog.getActuralNum())) {
					//改变实际库存
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
	 * @param materialId
	 * @param boxId
	 * @param taskId
	 * @param acturalNum
	 * @param user
	 * @return
	 */
	public String inventoryMaterial(String materialId, Integer boxId, Integer taskId, Integer acturalNum, User user) {
		InventoryLog inventoryLog = InventoryLog.dao.findFirst(GET_INVENTORY_LOG_BY_BOX_AND_TASKID_AND_MATERIALID, boxId, taskId, materialId);
		if (inventoryLog == null) {
			throw new OperationException("当前盘点的物料记录不存在，请检查参数是否正确!");
		}
		if (acturalNum < 0) {
			throw new OperationException("料盘的实际数量需要大于或等于0!");
		}
		
		//改变盘点记录
		inventoryLog.setActuralNum(acturalNum);
		inventoryLog.setDifferentNum(acturalNum - inventoryLog.getBeforeNum());
		inventoryLog.setInventoryOperatior(user.getUid());
		inventoryLog.setInventoryTime(new Date());
		inventoryLog.update();
		return "操作成功";
	}
	
	
	/**
	 * 平外仓物料
	 * @param id
	 * @param taskId
	 * @param user
	 * @return
	 */
	public String coverEWhMaterial(Integer id, Integer taskId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getEwhChecked()) {
			throw new OperationException("盘点任务记录尚未审核，请先审核再平仓！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法平仓！");
		}
		ExternalInventoryLog externalInventoryLog = ExternalInventoryLog.dao.findById(id);
		if (externalInventoryLog == null) {
			throw new OperationException("该盘点记录不存在，请检查参数");
		}
		Date date = new Date();
		if (externalInventoryLog.getCoverTime() != null) {
			throw new OperationException("该盘点记录已平仓，请不要重复平仓");
		}
			
		externalInventoryLog.setEnabled(false);
		externalInventoryLog.setDifferentNum(0);
		externalInventoryLog.setCoverOperatior(user.getUid());
		externalInventoryLog.setCoverTime(date);
		externalInventoryLog.update();
		return "操作成功";
	}
	
	
	
	/**
	 * UW平仓，根据记录ID和任务ID
	 * @param id
	 * @param taskId
	 * @param user
	 * @return
	 */
	public String coverMaterial(Integer id, Integer taskId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getUwChecked()) {
			throw new OperationException("盘点任务记录尚未审核，请先审核再平仓！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法平仓！");
		}
		InventoryLog inventoryLog = InventoryLog.dao.findById(id);
		if (inventoryLog == null) {
			throw new OperationException("该盘点记录不存在，请检查参数！");
		}
		if (!inventoryLog.getEnabled()) {
			throw new OperationException("该盘点记录已平或者不需要平仓！");
		}
		inventoryLog.setCoverOperatior(user.getUid());
		inventoryLog.setCoverTime(new Date());
		inventoryLog.setDifferentNum(0);
		inventoryLog.setEnabled(false);
		inventoryLog.update();
		return "操作成功";
	}
	
	
	/**
	 * UW仓批量平仓，根据任务ID和物料类型ID
	 * @param materialTypeId
	 * @param taskId
	 * @param user
	 * @return
	 */
	public String coverUwMaterialByTaskId(Integer materialTypeId, Integer taskId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法平仓！");
		}
		if (!task.getUwChecked()) {
			throw new OperationException("盘点任务记录尚未审核，请先审核再平仓！");
		}
		List<InventoryLog> inventoryLogs = InventoryLog.dao.find(GET_UNCOVER_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE, materialTypeId, taskId);
		Date date = new Date();
		for (InventoryLog inventoryLog : inventoryLogs) {
			inventoryLog.setCoverOperatior(user.getUid());
			inventoryLog.setDifferentNum(0);
			inventoryLog.setCoverTime(date);
			inventoryLog.setEnabled(false);
			inventoryLog.update();
		}
		return "操作成功";
	}
	
	
	/**
	 * 一键平仓UW
	 * @param materialTypeId
	 * @param taskId
	 * @param user
	 * @return
	 */
	
	public String coverUwMaterialOneKey(Integer taskId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法平仓！");
		}
		if (!task.getUwChecked()) {
			throw new OperationException("盘点任务记录尚未审核，请先审核再平仓！");
		}
		List<InventoryLog> inventoryLogs = InventoryLog.dao.find(GET_UNCOVER_INVENTORY_LOG_BY_TASKID, taskId);
		Date date = new Date();
		for (InventoryLog inventoryLog : inventoryLogs) {
			inventoryLog.setCoverOperatior(user.getUid());
			inventoryLog.setDifferentNum(0);
			inventoryLog.setCoverTime(date);
			inventoryLog.setEnabled(false);
			inventoryLog.update();
		}
		return "操作成功";
	}
	
	
	/**
	 * 物料仓批量平仓，根据任务ID和物料类型ID
	 * @param materialTypeId
	 * @param taskId
	 * @param user
	 * @return
	 */
	public String coverEwhMaterialByTaskId(Integer materialTypeId, Integer taskId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法平仓！");
		}
		if (!task.getEwhChecked()) {
			throw new OperationException("盘点任务记录尚未审核，请先审核再平仓！");
		}
		Date date = new Date();
		List<ExternalInventoryLog> externalInventoryLogs = ExternalInventoryLog.dao.find(GET_UNCOVER_EWH_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE, taskId, materialTypeId);
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
	 * @param materialTypeId
	 * @param taskId
	 * @param user
	 * @return
	 */
	public String coverEwhMaterialOneKey(Integer taskId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法平仓！");
		}
		if (!task.getEwhChecked()) {
			throw new OperationException("盘点任务记录尚未审核，请先审核再平仓！");
		}
		Date date = new Date();
		List<ExternalInventoryLog> externalInventoryLogs = ExternalInventoryLog.dao.find(GET_UNCOVER_EWH_INVENTORY_LOG_BY_TASKID, taskId);
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
	 * @param taskId
	 * @return
	 */
	public String checkUwInventoryTask(Integer taskId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法审核！");
		}
		if (task.getUwChecked()) {
			throw new OperationException("盘点任务记录已审核，请勿重复审核！");
		}
		List<InventoryLog> logs = InventoryLog.dao.find(GET_UN_INVENTORY_LOG_BY_TASKID, taskId);
		
		Window window = Window.dao.findFirst(SQL.GET_WINDOW_BY_TASKID, taskId);
		if (logs.size() > 0 || window != null) {
			throw new OperationException("UW仓盘点阶段未结束，请结束后再审核！");
		}
		Db.update(UPDATE_INVENTORY_LOG_UNCOVER, taskId);
		
		task.setUwChecked(true);
		task.setUwCheckedOperatior(user.getUid());
		task.setUwCheckedTime(new Date());
		task.update();
		return "操作成功";
	}
	
	
	/**
	 * 审核任务
	 * @param taskId
	 * @return
	 */
	public String checkEwhInventoryTask(Integer taskId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法审核！");
		}
		if (task.getEwhChecked()) {
			throw new OperationException("盘点任务记录已审核，请勿重复审核！");
		}
		List<ExternalInventoryLog> logs1 = ExternalInventoryLog.dao.find(GET_UN_EWH_INVENTORY_LOG_BY_TASKID, taskId);
		String nos = "";
		if (logs1.size() > 0) {
			for (ExternalInventoryLog externalInventoryLog : logs1) {
				if (externalInventoryLog.getActuralNum() == null) {
					if (nos.equals("")) {
						nos = String.valueOf(externalInventoryLog.getMaterialTypeId());
					}else {
						nos = nos + "," +String.valueOf(externalInventoryLog.getMaterialTypeId());
					}
				}
			}
			return "存在以下料号类型ID[" + nos + "]未盘点";
		}
		Db.update(UPDATE_EWH_INVENTORY_LOG_UNCOVER, taskId);
		
		task.setEwhChecked(true);
		task.setEwhCheckedOperatior(user.getUid());
		task.setEwhCheckedTime(new Date());
		task.update();
		return "操作成功";
	}
	
	
	/**
	 * 导入外仓盘点数据
	 * @param file
	 * @param taskId
	 * @param user
	 * @return
	 */
	public String importEWhInventoryRecord(File file, Integer taskId, User user) {
		String resultString = "导入成功";
		synchronized (Lock.IMPORT_EWH_INVENTORY_FILE_LOCK) {
			
			ExcelHelper fileReader;
			Task task = Task.dao.findById(taskId);
			if (task == null || !task.getType().equals(TaskType.COUNT) || !task.getState().equals(TaskState.PROCESSING)) {
				throw new OperationException("盘点任务不存在或未处于进行中状态，请检查参数是否正确！");
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
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) {		// 只读取有序号的行数据
						if (item.getNo() == null || item.getQuantity() == null || item.getNo().replaceAll(" ", "").equals("") || item.getQuantity().toString().replaceAll(" ", "").equals("") || item.getWh() == null || item.getWh().replaceAll(" ", "").equals("")) {
							resultString = "导入失败，请检查表格第" + i + "行的料号或数量是否填写了准确信息！";
							return resultString;
						}
	
						if (item.getQuantity() < 0) {
							resultString = "导入失败，表格第" + i + "行的数量为" + item.getQuantity() + "，数量必须大于等于0！";
							return resultString;
						}
						Destination destination = Destination.dao.findFirst(GET_DESTINATION_BY_DESTINATION_NAME, item.getWh());
						if (destination == null) {
							resultString = "导入失败，表格第" + i + "行的仓库名为" + item.getQuantity() + "，请确保系统中存在该仓库和该仓库存在该料号！";
							return resultString;
						}
						if (destination.getId().equals(0)) {
							resultString = "导入失败，表格第" + i + "行的仓库名为" + item.getQuantity() + "，不可导入无人仓库的盘点数据！！";
							return resultString;
						}
						// 根据料号找到对应的物料类型
						MaterialType mType = MaterialType.dao.findFirst(GET_MATERIAL_TYPE_BY_NO_SQL, item.getNo(), task.getSupplier());
						// 判断物料类型表中是否存在对应的料号且未被禁用，若不存在，则将对应的任务记录删除掉，并提示操作员检查套料单、新增对应的物料类型
						if (mType == null) {
							resultString = "导入失败，料号为" + item.getNo() + "的物料没有记录在物料类型表中或已被禁用，或者是供应商与料号对应不上！";
							return resultString;
						}
						if (inventoryDataMap.get(mType.getId()) != null && inventoryDataMap.get(mType.getId()).contains(destination.getId())) {
							resultString = "导入失败，仓库为"+ item.getWh() +"，料号为" + item.getNo() + "的物料表中存在重复项！";
							return resultString;
						}
						if (inventoryDataMap.get(mType.getId()) == null) {
							Set<Integer> whMaterialSet = new HashSet<>();
							whMaterialSet.add(destination.getId());
							inventoryDataMap.put(mType.getId(), whMaterialSet);
						}else {
							inventoryDataMap.get(mType.getId()).add(destination.getId());
						}
						
						ExternalInventoryLog externalInventoryLog = ExternalInventoryLog.dao.findFirst(GET_EWH_INVENTORY_LOG_BY_TASKID_MATERIALTYPEID_WHID, taskId, destination.getId(), mType.getId());
						
						//如果没有记录就说明该物料仓这个物料一开始没有进过这个料
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
						}else {
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
					}else {
						externalInventoryLog.update();
					}
				}
				task.setEwhChecked(false);
				task.setEwhCheckedOperatior(null);
				task.setEwhCheckedTime(null);
				task.update();
				
			} catch (Exception e) {
				e.printStackTrace();
				throw new OperationException(e.getMessage());
			}finally {
				file.delete();
			}
			
		}
		return resultString;
	}
	
	
	/**
	 * 获取当前仓口的盘点物料清单
	 * @param windowId
	 * @return
	 */
	public List<PackingInventoryInfoVO> getPackingInventory(Integer windowId){
		Window window = Window.dao.findById(windowId);
		if (window == null || window.getBindTaskId() == null) {
			throw new OperationException("仓口不存在任务");
		}
		int boxId = 0;
		Map<Integer, PackingInventoryInfoVO> map = new LinkedHashMap<>();
		List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, windowId);
		for (GoodsLocation goodsLocation : goodsLocations) {
			map.put(goodsLocation.getId(), new PackingInventoryInfoVO(goodsLocation, new ArrayList<>()));
		}
		for(AGVInventoryTaskItem inventoryTaskItem : TaskItemRedisDAO.getInventoryTaskItems(window.getBindTaskId())) {
			if (inventoryTaskItem.getWindowId().equals(windowId) && inventoryTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW)) {
				boxId = inventoryTaskItem.getBoxId();
				PackingInventoryInfoVO info = map.get(inventoryTaskItem.getGoodsLocationId());
				if (info == null ) {
					throw new OperationException("仓口 " + windowId + "没有对应货位" + inventoryTaskItem.getGoodsLocationId());
				}
				if (info.getBoxId() != null) {
					throw new OperationException("仓口 " + windowId + "的货位" + inventoryTaskItem.getGoodsLocationId() + "有一个以上的到站任务条目，请检查!");
				}
				List<MaterialInfoVO> materialInfoVOs = info.getList();
				List<Record> records = Db.find(GET_MATERIAL_INFO_BY_BOX, boxId);
				for (Record record : records) {
					MaterialInfoVO materialInfoVO = new MaterialInfoVO();
					materialInfoVO.setMaterailTypeId(record.getInt("material_type_id"));
					materialInfoVO.setMaterialId(record.getStr("id"));
					materialInfoVO.setNo(record.getStr("no"));
					materialInfoVO.setSpecification(record.getStr("specification"));
					materialInfoVO.setStoreNum(record.getInt("quantity"));
					materialInfoVO.setSupplierId(record.getInt("supplier_id"));
					materialInfoVO.setSupplier(record.getStr("supplier_name"));
					materialInfoVO.setProductionTime(record.getDate("production_time"));
					InventoryLog inventoryLog = InventoryLog.dao.findFirst(GET_INVENTORY_LOG_BY_BOX_AND_TASKID_AND_MATERIALID, boxId, window.getBindTaskId(), record.getStr("id"));
					if (inventoryLog != null && inventoryLog.getActuralNum() != null) {
						materialInfoVO.setActualNum(inventoryLog.getActuralNum());
					}
					materialInfoVOs.add(materialInfoVO);
				}
				info.setBoxId(boxId);
				info.setTaskId(window.getBindTaskId());
				info.setWindowId(windowId);
				info.setList(materialInfoVOs);
			}
		}
		return new ArrayList<>(map.values());
	}
	
	
	/**
	 * 根据供应商获取盘点任务(下拉框使用)
	 * @param supplierId
	 * @return
	 */
	public List<Task> getInventoryTask(Integer supplierId) {
		
		List<Task> tasks = Task.dao.find(GET_INVENTORY_TASK_BY_SUPPLIERID, TaskType.COUNT, supplierId);
		return tasks;
	}
	
	
	public PagePaginate selectAllInventoryTask(String filter, Integer pageNo, Integer pageSize){
		String[] tables = new String[] {"task", "supplier"};
		String[] refers = new String[] {"task.supplier = supplier.id"};
		if (filter == null || filter.trim().equals("")) {
			filter = "task.type=" + TaskType.COUNT;
		}
		else {
			filter += "#&#task.type="+TaskType.COUNT;
		}
		Boolean status;
		Page<Record> page = selectService.select(tables, refers, pageNo, pageSize, null, "task.state ASC, task.create_time", filter);
		List<InventoryTaskVO> taskVOs = new ArrayList<>();
		for (Record record : page.getList()) {
			
			InventoryTaskVO taskVO = new InventoryTaskVO();
			taskVO.setTaskId(record.getInt("Task_Id"));
			taskVO.setType(record.getInt("Task_Type"));
			taskVO.setTypeString(record.getInt("Task_Type"));
			taskVO.setTaskName(record.getStr("Task_FileName"));
			taskVO.setState(record.getInt("Task_State"));
			taskVO.setStateString(record.getInt("Task_State"));
			taskVO.setStartTime(record.getDate("Task_StartTime"));
			taskVO.setEndTime(record.getDate("Task_EndTime"));
			taskVO.setCheckedOperatior(record.getStr("Task_CheckedOperatior"));
			taskVO.setCheckedTime(record.getDate("Task_CheckedTime"));
			taskVO.setCheckedTime(record.getDate("Task_CreateTime"));
			taskVO.setSupplierId(record.getInt("Task_Supplier"));
			taskVO.setSupplierName(record.getStr("Supplier_Name"));
			status  = false;
			if (record.getInt("Task_State").equals(TaskState.PROCESSING)) {
				status = TaskItemRedisDAO.getTaskStatus(record.getInt("Task_Id"));
			}
			
			taskVO.setStatus(status);
			taskVOs.add(taskVO);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageNumber(page.getPageNumber());
		pagePaginate.setPageSize(page.getPageSize());
		pagePaginate.setTotalPage(page.getTotalPage());
		pagePaginate.setTotalRow(page.getTotalRow());
		pagePaginate.setList(taskVOs);
		return pagePaginate;
	}
	
	
	@Log("导出盘点任务ID为{taskId}的盘点记录")
	public void exportEwhInventoryTask(Integer taskId, String no, String fileName, OutputStream output) throws IOException {
		SqlPara sqlPara = new SqlPara();
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (no == null) {
			sqlPara.setSql(GET_EWH_INVENTORY_TASK_INFO);
			sqlPara.addPara(taskId);
		}else {
			sqlPara.setSql(GET_EWH_INVENTORY_TASK_INFO_BY_NO);
			sqlPara.addPara(taskId);
			sqlPara.addPara("%" + no + "%");
		}
		List<Record> inventoryRecords =Db.find(sqlPara);

		String[] field = null;
		String[] head = null;
		field = new String[] {"supplier_name", "no", "before_num", "actural_num", "return_num", "different_num"};
		head =  new String[] {"供应商", "料号", "盘前数量", "盘点数量", "退料","盈亏"};
		ExcelWritter writter = ExcelWritter.create(true);
		writter.fill(inventoryRecords, fileName, field, head);
		writter.write(output, true);
	}
	
	
	public List<Record> getUwInventoryTaskInfo(Integer taskId, String no) {
		SqlPara sqlPara = new SqlPara();
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (no == null || no.equals("")) {
			sqlPara.setSql(GET_UW_INVWNTORY_TASK_INFO);
			sqlPara.addPara(taskId);
		}else {
			sqlPara.setSql(GET_UW_INVWNTORY_TASK_INFO_BY_NO);
			sqlPara.addPara(taskId);
			sqlPara.addPara("%" + no + "%");
		}
		List<Record> inventoryRecords =Db.find(sqlPara);
		
		return inventoryRecords; 
	}
	
	
	public List<Record> getEwhInventoryTaskInfo(Integer taskId, String no) {
		SqlPara sqlPara = new SqlPara();
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (no == null) {
			sqlPara.setSql(GET_EWH_INVENTORY_TASK_INFO);
			sqlPara.addPara(taskId);
		}else {
			sqlPara.setSql(GET_EWH_INVENTORY_TASK_INFO_BY_NO);
			sqlPara.addPara(taskId);
			sqlPara.addPara("%" + no + "%");
		}
		List<Record> inventoryRecords =Db.find(sqlPara);
		return inventoryRecords; 
	}
	
	
	public List<InventoryTaskDetailVO> getUwInventoryTaskDetails(Integer taskId, Integer materialTypeId) {
		List<InventoryLog> inventoryLogs = InventoryLog.dao.find(GET_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE, materialTypeId, taskId );
				
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
			details.add(inventoryTaskDetail);
		}
		
		return details;
	}
	
	
	public List<InventoryTaskDetailVO> getEwhInventoryTaskDetails(Integer taskId, Integer materialTypeId) {
		
		List<ExternalInventoryLog> externalInventoryLogs = ExternalInventoryLog.dao.find(GET_EWH_INVENTORY_LOG_BY_TASKID_MATERIALTYPEID, taskId, materialTypeId);
		
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
	
	
	
	public void exportUwInventoryTask(Integer taskId, String no, String fileName, OutputStream output) throws IOException {
		SqlPara sqlPara = new SqlPara();
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (no == null || no.equals("")) {
			sqlPara.setSql(GET_UW_INVWNTORY_TASK_INFO);
			sqlPara.addPara(taskId);
		}else {
			sqlPara.setSql(GET_UW_INVWNTORY_TASK_INFO_BY_NO);
			sqlPara.addPara(taskId);
			sqlPara.addPara("%" + no + "%");
		}
		List<Record> inventoryRecords =Db.find(sqlPara);

		String[] field = null;
		String[] head = null;
		field = new String[] {"supplier_name", "no", "before_num", "actural_num",  "different_num"};
		head =  new String[] {"供应商", "料号", "盘前数量", "盘点数量","盈亏"};
		ExcelWritter writter = ExcelWritter.create(true);
		writter.fill(inventoryRecords, fileName, field, head);
		writter.write(output, true);
	}
	
	
	public String finishInventoryTask(Integer taskId) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法完成！");
		}
		if (!task.getUwChecked()) {
			throw new OperationException("UW仓盘点任务未审核，无法完成！");
		}
		if (!task.getEwhChecked()) {
			throw new OperationException("物料仓盘点任务未审核，无法完成！");
		}
		ExternalInventoryLog log1 = ExternalInventoryLog.dao.findFirst(GET_UNCOVER_EWH_INVENTORY_LOG_BY_TASKID, taskId);
		InventoryLog log2 = InventoryLog.dao.findFirst(GET_UNCOVER_INVENTORY_LOG_BY_TASKID, taskId);
		if (log1 == null && log2 == null) {
			task.setState(TaskState.FINISHED);
			task.setEndTime(new Date());
			task.update();
		}else {
			throw new OperationException("盘点任务存在未平仓的盘点条目，无法完成！");
		}
		List<ExternalInventoryLog> externalInventoryLogs = ExternalInventoryLog.dao.find(GET_ALL_EWH_INVENTORY_LOG_BY_TASK, taskId);
		for (ExternalInventoryLog externalInventoryLog : externalInventoryLogs) {
			ExternalWhLog externalWhLog = new ExternalWhLog();
			externalWhLog.setMaterialTypeId(externalInventoryLog.getMaterialTypeId());
			externalWhLog.setTaskId(taskId);
			externalWhLog.setSourceWh(externalInventoryLog.getWhId());
			externalWhLog.setDestination(externalInventoryLog.getWhId());
			externalWhLog.setQuantity(externalInventoryLog.getActuralNum());
			externalWhLog.setOperatior(externalInventoryLog.getCoverOperatior());
			externalWhLog.setTime(new Date());
			externalWhLog.save();
		}
		return "操作成功！";
	}
	
	
	public String editEwhInventoryLog(Integer id, Integer acturanlNum, Integer returnNum , User user) {
		ExternalInventoryLog externalInventoryLog = ExternalInventoryLog.dao.findById(id);
		
		if (externalInventoryLog == null) {
			throw new OperationException("该盘点记录不存在，请检查！");
		}
		Task task = Task.dao.findById(externalInventoryLog.getTaskId());
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("该盘点任务不处于进行中状态，无法修改，请检查！");
		}
		externalInventoryLog.setActuralNum(acturanlNum);
		externalInventoryLog.setMaterialReturnNum(returnNum);
		externalInventoryLog.setDifferentNum(acturanlNum - externalInventoryLog.getBeforeNum()  + returnNum);
		externalInventoryLog.setInventoryOperatior(user.getUid());
		externalInventoryLog.setInventoryTime(new Date());
		externalInventoryLog.setCoverOperatior(null);
		externalInventoryLog.setCoverTime(null);
		externalInventoryLog.setEnabled(true);
		externalInventoryLog.update();
		
		task.setEwhChecked(false);
		task.setEwhCheckedOperatior(null);
		task.setEwhCheckedTime(null);
		task.update();
		return "操作成功！";
	}
	
	
	public String getTaskName(Date date) {
		
		String fileName = "盘点_";
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = formatter.format(date);
		fileName = fileName + time;
		return fileName;
	}
	
	
	public List<Task> getUnStartInventoryTask(Integer supplierId) {
		
		List<Task> tasks = Task.dao.find(GET_UNSTART_INVENTORY_TASK_BY_SUPPLIER, supplierId);
		return tasks;
	}
	
	public String getTaskName(Integer id) {
		Task task = Task.dao.findById(id);
		if (task != null) {
			return task.getFileName();
		}
		return null;
	}
}
