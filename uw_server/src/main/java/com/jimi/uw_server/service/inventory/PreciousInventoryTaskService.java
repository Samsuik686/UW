/**  
*  
*/
package com.jimi.uw_server.service.inventory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jimi.uw_server.constant.MaterialStatus;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.constant.sql.InventoryTaskSQL;
import com.jimi.uw_server.constant.sql.MaterialSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.PreciousTaskLock;
import com.jimi.uw_server.model.InventoryLog;
import com.jimi.uw_server.model.InventoryTaskBaseInfo;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.service.base.BaseInventoryTaskService;

/**
 * <p>
 * Title: PreciousInventoryTaskService
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2019
 * </p>
 * <p>
 * Company: 惠州市几米物联技术有限公司
 * </p>
 * 
 * @author trjie
 * @date 2020年5月25日
 *
 */
public class PreciousInventoryTaskService extends BaseInventoryTaskService {

	private Integer batchSize = 2000;

	private Integer uwId = 0;


	/**
	 * 创建盘点任务
	 * 
	 * @param supplierId
	 * @return
	 */
	public String create(Integer supplierId) {
		Supplier supplier = Supplier.dao.findById(supplierId);
		if (supplier == null) {
			throw new OperationException("客户不存在！");
		}
		Task task = Task.dao.findFirst(InventoryTaskSQL.GET_USEFUL_TASK_BY_TYPE_SUPPLIER, TaskType.COUNT, supplierId, WarehouseType.PRECIOUS.getId());
		if (task != null) {
			throw new OperationException("该客户已存在未开始或进行中的盘点任务，无法创建新的盘点任务！");
		}
		Date date = new Date();
		task = new Task();
		task.setFileName(getTaskName(date, WarehouseType.PRECIOUS.getId())).setCreateTime(date).setType(TaskType.COUNT).setState(TaskState.WAIT_START).setSupplier(supplierId)
				.setWarehouseType(WarehouseType.PRECIOUS.getId()).save();
		InventoryTaskBaseInfo info = new InventoryTaskBaseInfo();
		info.setTaskId(task.getId()).setDestinationId(uwId).save();
		return "操作成功";
	}


	/**
	 * 
	 * <p>
	 * Description: 开始盘点任务
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月25日
	 */
	public String start(Integer taskId) {
		synchronized (PreciousTaskLock.START_INVENTORY_LOCK) {
			Task task = Task.dao.findById(taskId);
			if (task == null) {
				throw new OperationException("任务不存在!");
			} else if (!task.getType().equals(TaskType.COUNT)) {
				throw new OperationException("该任务并非盘点任务！");
			} else if (!task.getState().equals(TaskState.WAIT_START)) {
				throw new OperationException("该任务并未处于未开始状态，无法开始任务！");
			}

			Task tempTask = Task.dao.findFirst(InventoryTaskSQL.GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER, task.getSupplier(), WarehouseType.PRECIOUS.getId());
			if (tempTask != null) {
				throw new OperationException("当前盘点的客户存在进行中的盘点任务，请等待任务结束后再开始盘点任务!");
			}
			tempTask = Task.dao.findFirst(InventoryTaskSQL.GET_RUNNING_TASK_BY_SUPPLIER, task.getSupplier(), WarehouseType.PRECIOUS.getId());
			if (tempTask != null && tempTask.getSupplier().equals(task.getSupplier())) {
				throw new OperationException("当前盘点的客户存在其他出入库任务，请等待其他任务结束后再开始盘点任务!");
			}
			List<InventoryLog> inventoryLogs = new ArrayList<>();
			List<Material> materials = Material.dao.find(MaterialSQL.GET_ALL_MATERIAL_BY_SUPPLIER_AND_WAREHOUSE_SQL, task.getSupplier(), WarehouseType.PRECIOUS.getId(), MaterialStatus.NORMAL);
			for (Material material2 : materials) {
				InventoryLog inventoryLog = new InventoryLog();
				inventoryLog.setMaterialId(material2.getId());
				inventoryLog.setTaskId(task.getId());
				inventoryLog.setBeforeNum(material2.getRemainderQuantity());
				inventoryLog.setBoxId(material2.getBox());
				inventoryLog.setEnabled(true);
				inventoryLogs.add(inventoryLog);
			}
			Db.batchSave(inventoryLogs, batchSize);
			Db.update(InventoryTaskSQL.UPDATE_MATERIAL_RETURN_RECORD_UNENABLED, task.getCreateTime(), uwId, task.getSupplier(), WarehouseType.PRECIOUS.getId());
			task.setStartTime(new Date()).setState(TaskState.PROCESSING).update();
		}
		return "操作成功";
	}


	public Boolean inventoryUWMaterial(String materialId, Integer taskId, Integer acturalNum, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("任务不存在或者任务未处于进行中状态！");
		}
		InventoryLog inventoryLog = InventoryLog.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_LOG_BY_TASKID_AND_MATERIALID, taskId, materialId);
		if (inventoryLog == null) {
			throw new OperationException("当前盘点的物料记录不存在，请检查参数是否正确！");
		}
		if (!inventoryLog.getEnabled()) {
			throw new OperationException("当前盘点的物料记录进行平仓，无法修改数量！");
		}
		if (acturalNum < 0) {
			throw new OperationException("料盘的实际数量需要大于或等于0！");
		}
		// 改变盘点记录
		inventoryLog.setActuralNum(acturalNum);
		inventoryLog.setDifferentNum(acturalNum - inventoryLog.getBeforeNum());
		inventoryLog.setInventoryOperatior(user.getUid());
		inventoryLog.setInventoryTime(new Date());
		inventoryLog.update();
		InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID, task.getId());
		info.setCheckTime(null).setCheckOperator(null).update();
		return true;
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

		if (logs.size() > 0) {
			String result = "盘点任务存在未盘点物料，物料类型ID为[";
			for (InventoryLog inventoryLog : logs) {
				result += inventoryLog.getInt("material_type_id") + ",";
			}
			result += "]";
			throw new OperationException(result);
		}
		Db.update(InventoryTaskSQL.UPDATE_INVENTORY_LOG_UNCOVER, taskId);

		info.setCheckTime(new Date()).setCheckOperator(user.getUid()).update();
		return "操作成功";
	}


	/**
	 * 
	 * <p>
	 * Description: 完成任务
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月25日
	 */
	public String finish(Integer taskId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法完成！");
		}
		InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID, task.getId(), uwId);
		if (info == null) {
			throw new OperationException("该仓库尚未进行盘点，无法完成！");
		}
		if (info.getCheckTime() == null) {
			throw new OperationException("该仓库盘点任务未审核，无法完成！");
		}
		InventoryLog log2 = InventoryLog.dao.findFirst(InventoryTaskSQL.GET_UNCOVER_INVENTORY_LOG_BY_TASKID, taskId);
		if (log2 == null) {
			List<InventoryLog> inventoryLogs = InventoryLog.dao.find(InventoryTaskSQL.GET_INVENTORY_LOG_BY_TASKID, taskId);
			for (InventoryLog inventoryLog : inventoryLogs) {
				if (!inventoryLog.getBeforeNum().equals(inventoryLog.getActuralNum())) {
					// 改变实际库存
					Material material = Material.dao.findById(inventoryLog.getMaterialId());
					material.setRemainderQuantity(inventoryLog.getActuralNum());
					material.update();
				}
			}
			info.setFinishTime(new Date()).setFinishOperator(user.getUid()).update();
			task.setState(TaskState.FINISHED);
			task.setEndTime(new Date());
			task.update();
		} else {
			throw new OperationException("盘点任务存在未平仓的盘点条目，无法完成！");
		}
		return "操作成功！";
	}
}
