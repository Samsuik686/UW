/**  
*  
*/
package com.jimi.uw_server.service.io;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.constant.MaterialStatus;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.constant.sql.IOTaskSQL;
import com.jimi.uw_server.constant.sql.InventoryTaskSQL;
import com.jimi.uw_server.constant.sql.SQL;
import com.jimi.uw_server.constant.sql.SampleTaskSQL;
import com.jimi.uw_server.constant.sql.SupplierSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.PreciousTaskLock;
import com.jimi.uw_server.model.FormerSupplier;
import com.jimi.uw_server.model.InventoryLog;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.SampleOutRecord;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.service.base.BaseIOTaskService;

/**
 * <p>
 * Title: PreciousIOTaskService
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
 * @date 2020年5月18日
 *
 */
public class PreciousIOTaskService extends BaseIOTaskService {

	// 创建出入库/退料任务
	public void create(Integer type, String fileName, File file, Integer supplier, Integer destination, Boolean isInventoryApply, Integer inventoryTaskId, String remarks, Boolean isForced)
			throws Exception {
		synchronized (PreciousTaskLock.CREATE_IO_LOCK) {
			super.createTask(type, fileName, file, supplier, destination, isInventoryApply, inventoryTaskId, remarks, WarehouseType.PRECIOUS.getId(), isForced, false);
		}
	}
	// 令指定任务开始
	public boolean start(Integer id) {
		synchronized (PreciousTaskLock.START_IO_LOCK) {
			// 根据仓口id查找对应的仓口记录
			Task task = Task.dao.findById(id);
			// 任务仓库为贵重仓
			if (task.getWarehouseType().equals(WarehouseType.PRECIOUS.getId())) {
				Task inventoryTask = Task.dao.findFirst(InventoryTaskSQL.GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER, task.getSupplier(), WarehouseType.PRECIOUS.getId());
				InventoryLog inventoryLog = null;
				if (inventoryTask != null) {
					inventoryLog = InventoryLog.dao.findFirst(InventoryTaskSQL.GET_UNCOVER_INVENTORY_LOG_BY_TASKID, inventoryTask.getId());
				}
				if (inventoryTask != null && inventoryLog != null) {
					throw new OperationException("当前客户存在进行中的UW仓盘点任务，请等待任务结束后再开始出入库任务!");
				}
				int state = task.getState();
				// 判断任务状态是否为“未开始”
				if (state != TaskState.WAIT_START) {
					throw new OperationException("该任务已开始过或已作废！");
				} else {
					return task.setStartTime(new Date()).setState(TaskState.PROCESSING).update();
				}
			}
		}
		return false;
	}

	// 作废指定任务
	public boolean cancel(Integer id) {
		synchronized (PreciousTaskLock.CANCEL_IO_LOCK) {
			Task task = Task.dao.findById(id);
			int state = task.getState();
			// 对于已完成的任务，禁止作废
			if (state == TaskState.FINISHED) {
				throw new OperationException("该任务已完成，禁止作废！");
			}
			if (state == TaskState.CANCELED) { // 对于已作废过的任务，禁止作废
				throw new OperationException("该任务已作废！");
			}
			if (state < TaskState.PROCESSING) {
				if (task.getStartTime() == null) {
					task.setStartTime(new Date());
				}
				task.setEndTime(new Date()).setState(TaskState.CANCELED).update();
				return true;
			} 
			if (task.getWarehouseType().equals(WarehouseType.PRECIOUS.getId())) {
				if (task.getType().equals(TaskType.IN) || task.getType().equals(TaskType.SEND_BACK)) {
					Record problemRecords = Db.findFirst(IOTaskSQL.GET_IN_WRONG_IOTASK_ITEM_BY_TASK_ID, task.getId());
					if (problemRecords != null) {
						throw new OperationException("该任务存在入料与需求数不符的情况！");
					}
					List<Record> unfinshRecords = Db.find(IOTaskSQL.GET_UNFINISH_PACKING_LIST_ITEM, task.getId());
					for (Record record : unfinshRecords) {
						List<Material> materials = Material.dao.find(IOTaskSQL.GET_MATERIAL_AND_OUTQUANTITY_BY_PACKING_LIST_ITEM_ID, record.getInt("id"));
						for (Material material : materials) {
							material.setStatus(MaterialStatus.NORMAL).update();
						}
					}

					task.setEndTime(new Date()).setState(TaskState.CANCELED).update();
					return true;
				} else if (task.getType().equals(TaskType.OUT)) {
					Record cuttingRecord = Db.findFirst(IOTaskSQL.GET_IOTASK_CUTTING_PACKING_LIST_ITEM, task.getId());
					if (cuttingRecord != null) {
						throw new OperationException("该任务存在截料待处理的情况！");
					}
					Record overQuantityRecord = Db.findFirst(IOTaskSQL.GET_OUT_OVER_PRECIOUS_IOTASK_ITEM_BY_TASKID, task.getId());
					if (overQuantityRecord != null) {
						throw new OperationException("该任务存在发料超发的情况！");
					}
					Record lackRecord = Db.findFirst(IOTaskSQL.GET_OUT_LACK_PRECIOUS_IOTASK_ITEM_BY_TASKID, task.getId());
					if (lackRecord != null) {
						throw new OperationException("该任务存在发料缺发的情况！");
					}
					task.setEndTime(new Date()).setState(TaskState.CANCELED).update();
					return true;
				}
			}
		}
		return false;
		
	}

	// 新增入库料盘记录并写入库任务日志记录
	public Material in(Integer packingListItemId, String materialId, Integer quantity, Date productionTime, String supplierName, String cycle, String manufacturer, Date printTime, User user) {
		synchronized (PreciousTaskLock.IN_SCAN_IO_LOCK) {
			// 通过任务条目id获取套料单记录
			PackingListItem packingListItem = PackingListItem.dao.findById(packingListItemId);
			if (packingListItem == null || packingListItem.getFinishTime() != null) {
				throw new OperationException("任务不存在，入库失败！");
			}
			Task task = Task.dao.findById(packingListItem.getTaskId());
			if (task == null || !task.getState().equals(TaskState.PROCESSING) || !task.getWarehouseType().equals(WarehouseType.PRECIOUS.getId())) {
				throw new OperationException("任务不存在或未处于进行中，入库失败！");
			}
			// 通过套料单记录获取物料类型id
			MaterialType materialType = MaterialType.dao.findById(packingListItem.getMaterialTypeId());
			// 通过物料类型获取对应的客户id
			Integer supplierId = materialType.getSupplier();
			// 通过客户id获取客户名
			Supplier supplier = Supplier.dao.findById(supplierId);
			String sName = supplier.getName();
			FormerSupplier formerSupplier = FormerSupplier.dao.findFirst(SupplierSQL.GET_FORMER_SUPPLIER_BY_NAME_AND_SUPPLIER_SQL, supplierName, materialType.getSupplier());
			if (!supplierName.equals(sName) && formerSupplier == null) {
				throw new OperationException("扫码错误，客户 " + supplierName + " 对应的任务目前没有在本界面进行任务，" + "本界面已绑定 " + sName + " 的任务单！");
			}
			if (TaskLog.dao.find(IOTaskSQL.GET_MATERIAL_ID_IN_SAME_TASK_SQL, materialId, packingListItem.getId()).size() != 0) {
				throw new OperationException("时间戳为" + materialId + "的料盘已在同一个任务中被扫描过，请勿在同一个入库任务中重复扫描同一个料盘！");
			}
			Material material = Material.dao.findById(materialId);
			if (material != null && !material.getIsRepeated()) {
				throw new OperationException("时间戳为" + materialId + "的料盘已入过库，请勿重复入库！");
			}
			// 新增物料表记录
			if (cycle == null || cycle.trim().equals("") || cycle.trim().equals("无")) {
				cycle = "无";
			}
			TaskLog taskLog = TaskLog.dao.findFirst(SQL.GET_OUT_QUANTITY_BY_PACKINGITEMID, packingListItem.getId());
			if (taskLog != null && taskLog.getInt("totalQuantity") != null && taskLog.getInt("totalQuantity") >= packingListItem.getQuantity()) {
				throw new OperationException("扫描数量足够！");
			}
			putInMaterialToDb(material, materialId, null, quantity, productionTime, printTime, materialType.getId(), cycle, manufacturer, MaterialStatus.INING, supplier.getCompanyId());
			// 写入库日志 // 写入库日志
			createTaskLog(packingListItem.getId(), materialId, quantity, user, task);
			return material;
		}
	}

	// 写贵重仓出库任务日志
	public boolean out(Integer packingListItemId, String materialId, Integer quantity, String supplierName, User user, Boolean isForced) {
		synchronized (PreciousTaskLock.OUT_SCAN_IO_LOCK) {
			// 通过任务条目id获取套料单记录
			PackingListItem packingListItem = PackingListItem.dao.findById(packingListItemId);
			if (packingListItem == null || packingListItem.getFinishTime() != null) {
				throw new OperationException("任务不存在，出库失败！");
			}
			Task task = Task.dao.findById(packingListItem.getTaskId());
			if (task == null || !task.getState().equals(TaskState.PROCESSING) || !task.getType().equals(TaskType.OUT) || !task.getWarehouseType().equals(WarehouseType.PRECIOUS.getId())) {
				throw new OperationException("任务不存在或未处于进行中，出库失败！");
			}

			// 通过套料单记录获取物料类型id
			MaterialType materialType = MaterialType.dao.findById(packingListItem.getMaterialTypeId());

			// 通过物料类型获取对应的客户id
			Integer supplierId = materialType.getSupplier();
			// 通过客户id获取客户名
			String sName = Supplier.dao.findById(supplierId).getName();
			FormerSupplier formerSupplier = FormerSupplier.dao.findFirst(SupplierSQL.GET_FORMER_SUPPLIER_BY_NAME_AND_SUPPLIER_SQL, supplierName, materialType.getSupplier());
			if (!supplierName.equals(sName) && formerSupplier == null) {
				throw new OperationException("扫码错误，客户 " + supplierName + " 对应的任务目前没有在本仓口进行任务，" + "本仓口已绑定 " + sName + " 的任务单！");
			}

			// 对于不在已到站料盒的物料，禁止对其进行操作
			Material material = Material.dao.findById(materialId);
			// 若扫描的料盘记录不存在于数据库中或不在盒内，则抛出OperationException
			if (material == null || !material.getStatus().equals(MaterialStatus.NORMAL)) {
				throw new OperationException("时间戳为" + materialId + "的料盘没有入过库或尚未入库及出库完成，不能对其进行出库操作！");
			}

			if (!packingListItem.getMaterialTypeId().equals(material.getType())) {
				throw new OperationException("时间戳为" + materialId + "的料盘并非当前出库料号，不能对其进行出库操作！");
			}
			// 若在同一个出库任务中重复扫同一个料盘时间戳，则抛出OperationException
			if (TaskLog.dao.findFirst(IOTaskSQL.GET_MATERIAL_ID_IN_SAME_TASK_SQL, materialId, packingListItem.getId()) != null) {
				throw new OperationException("时间戳为" + materialId + "的料盘已在同一个任务中被扫描过，请勿在同一个出库任务中重复扫描同一个料盘！");
			}
			Record outRecord = Db.findFirst(SQL.GET_OUT_QUANTITY_BY_PACKINGITEMID, packingListItem.getId());
			int outQuantity = 0;
			if (outRecord != null && outRecord.getInt("totalQuantity") != null) {
				if (outRecord.getInt("totalQuantity") >= packingListItem.getQuantity()) {
					throw new OperationException("扫描数量足够！");
				}
				outQuantity = outRecord.getInt("totalQuantity");
			}
			// 判断是否存在更旧的料盘
			if (isForced == null || !isForced) {
				Material materialTemp1 = Material.dao.findFirst(IOTaskSQL.GET_OLDER_MATERIAL_BY_BOX_AND_TIME, material.getType(), material.getProductionTime(), MaterialStatus.NORMAL);

				if (materialTemp1 != null) {
					if (materialTemp1.getCycle() == null || materialTemp1.getCycle().trim().equals("")) {
						throw new OperationException("存在更旧的物料，日期是 " + materialTemp1.getPrintTime() + "！");
					}
					throw new OperationException("存在更旧的物料，周期是 " + materialTemp1.getCycle() + "！");

				}
			}
			// 判断物料二维码中包含的料盘数量信息是否与数据库中的料盘剩余数相匹配
			Integer remainderQuantity = material.getRemainderQuantity();
			if (!remainderQuantity.equals(quantity)) {
				throw new OperationException("时间戳为" + materialId + "的料盘数量与数据库中记录的料盘剩余数量不一致，请扫描正确的料盘二维码！");
			}
			// 扫码出库后，将料盘设置为不在盒内
			material.setStatus(MaterialStatus.OUTTING);
			material.update();

			TaskLog taskLog = createTaskLog(packingListItem.getId(), materialId, quantity, user, task);
			// 判断物料是否需要截料
			if (outQuantity + quantity > packingListItem.getQuantity()) {
				taskLog.setQuantity(packingListItem.getQuantity() - outQuantity).update();
				material.setStatus(MaterialStatus.CUTTING).setCutTaskLogId(taskLog.getId()).update();
			}
			return true;
		}
	}

	// 将普通仓截料后剩余的物料置为在盒内
	public Material backAfterCutting(Integer packingListItemId, String materialId, Integer quantity, String supplierName) {
		// 通过任务条目id获取套料单记录
		PackingListItem packingListItem = PackingListItem.dao.findById(packingListItemId);
		// 通过套料单记录获取物料类型id
		MaterialType materialType = MaterialType.dao.findById(packingListItem.getMaterialTypeId());
		// 通过物料类型获取对应的客户id
		Integer supplierId = materialType.getSupplier();
		// 通过客户id获取客户名
		String sName = Supplier.dao.findById(supplierId).getName();
		FormerSupplier formerSupplier = FormerSupplier.dao.findFirst(SupplierSQL.GET_FORMER_SUPPLIER_BY_NAME_AND_SUPPLIER_SQL, supplierName, materialType.getSupplier());
		TaskLog taskLog = TaskLog.dao.findFirst(IOTaskSQL.GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, packingListItemId, materialId);
		Material material = Material.dao.findById(materialId);
		if (!supplierName.equals(sName) && formerSupplier == null) {
			throw new OperationException("扫码错误，客户 " + supplierName + " 对应的任务目前没有在本界面进行任务，" + "本界面已绑定 " + sName + " 的任务单！");
		} else if (taskLog == null || !material.getStatus().equals(MaterialStatus.CUTTING)) {
			throw new OperationException("扫错料盘，该料盘未处于截料状态!");
		} else if ((material.getRemainderQuantity() - taskLog.getQuantity()) != quantity) {
			throw new OperationException("扫描错误，请扫描截料返库的料盘二维码!");
		} else if (material.getRemainderQuantity() == 0) {
			throw new OperationException("扫描错误，该料盘已全部出库!");
		} else {
			material.setStatus(MaterialStatus.OUTTING).setCutTaskLogId(null).update();
			return material;
		}
	}

	// 删除错误的料盘记录
	public Material deleteMaterialRecord(Integer packListItemId, String materialId) {
		PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
		int taskId = packingListItem.getTaskId();
		Task task = Task.dao.findById(taskId);
		Material material = Material.dao.findById(materialId);
		if (task.getType() == TaskType.IN || task.getType() == TaskType.SEND_BACK) { // 若是入库或退料入库任务，则删除掉入库记录，并删除掉物料实体表记录
			if (!material.getStatus().equals(MaterialStatus.INING)) {
				throw new OperationException("时间戳为" + materialId + "的料盘已被出库，禁止删除！");
			}
			SampleOutRecord sampleOutRecord = SampleOutRecord.dao.findFirst(SampleTaskSQL.GET_SAMPLE_OUT_RECORD_BY_MATERIALID, materialId);
			if (sampleOutRecord != null) {
				throw new OperationException("抽检出库或者丢失的料盘入库后，禁止删除！");
			}
			Db.update(IOTaskSQL.DELETE_TASK_LOG_SQL, packListItemId, materialId);
			Material.dao.deleteById(materialId);
			return material;
		} else { // 若是出库任务，删除掉出库记录；若已经执行过删除操作，则将物料实体表对应的料盘记录还原
			TaskLog taskLog = TaskLog.dao.findFirst(IOTaskSQL.GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, packListItemId, materialId);
			Record record = Db.findFirst(SQL.GET_NEWEST_MATERIAL_TASKLOG_BY_ITEM_ID_SQL, packingListItem);
			if (record != null && !record.getDate("production_time").equals(material.getProductionTime())) {
				throw new OperationException("时间戳为" + materialId + "的料盘并非当前出库记录中最新的料盘，禁止删除！");
			}
			material.setStatus(MaterialStatus.NORMAL);
			material.setCutTaskLogId(null);
			material.update();
			TaskLog.dao.deleteById(taskLog.getId());
			return material;
		}
	}

	
	
	/**
	 * 完成贵重仓出库任务缺料条目
	 * 
	 * @param taskId
	 * @return
	 */
	public Boolean finishTaskLackItem(Integer taskId) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.OUT) || !task.getWarehouseType().equals(WarehouseType.PRECIOUS.getId()) || !task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("出库任务不存在或者任务未处于进行中状态");
		}
		List<PackingListItem> cutPackingListItems = PackingListItem.dao.find(IOTaskSQL.GET_CUTTING_PACKING_LIST_ITEM_BY_TASK_ID, taskId);
		Set<Integer> cutPackingListItemsIdSet = new HashSet<>();
		if (!cutPackingListItems.isEmpty()) {
			for (PackingListItem cutPackingListItem : cutPackingListItems) {
				cutPackingListItemsIdSet.add(cutPackingListItem.getId());
			}
		}
		List<PackingListItem> unfinishLackRecoed = PackingListItem.dao.find(IOTaskSQL.GET_ALL_UNFINISH_PRECIOUS_IOTASK_ITEM, taskId);
		int finishLackItemSize = 0;
		for (PackingListItem record : unfinishLackRecoed) {
			if (cutPackingListItemsIdSet.contains(record.getId())) {
				continue;
			}
			if (materialService.countPreciousQuantityByMaterialTypeId(record.getMaterialTypeId()) > 0) {
				continue;
			}
			List<Material> materials = Material.dao.find(IOTaskSQL.GET_MATERIAL_AND_OUTQUANTITY_BY_PACKING_LIST_ITEM_ID, record.getId());
			boolean flag = false;
			if (materials != null && materials.size() > 0) {
				for (Material material : materials) {
					if (!material.getRemainderQuantity().equals(material.getInt("outQuantity"))) {
						flag = true;
						break;
					}
				}
				if (flag) {
					continue;
				}
				for (Material material : materials) {
					material.setRemainderQuantity(0);
					material.setStatus(MaterialStatus.NORMAL).update();
				}
			}
			PackingListItem packingListItem = PackingListItem.dao.findById(record.getId());
			packingListItem.setFinishTime(new Date()).update();
			finishLackItemSize++;
		}
		if (finishLackItemSize == unfinishLackRecoed.size()) {
			Record problemRecord = Db.findFirst(IOTaskSQL.GET_PROBLEM_IOTASK_ITEM_BY_TASK_ID, taskId);
			if (problemRecord != null) {
				task.setState(TaskState.EXIST_LACK);
			} else {
				task.setState(TaskState.FINISHED);
			}
			task.update();
		}
		return true;
	}

	// 令指定任务通过审核
	@Override
	public void pass(Integer id) {
		synchronized (PreciousTaskLock.PASS_IO_LOCK) {
			super.pass(id);
		}
	}

	/**
	 * 完成贵重仓出库任务条目
	 * 
	 * @param taskId
	 * @return
	 */
	public String finish(Integer taskId) {
		Task task = Task.dao.findById(taskId);
		if (task == null) {
			throw new OperationException("任务不存在！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("任务未处于进行中状态！");
		}
		if (task.getType().equals(TaskType.IN) || task.getType().equals(TaskType.SEND_BACK)) {
			// 完成已经可以完成（数量一致）的任务条目
			List<PackingListItem> canFinishTaskItems = PackingListItem.dao.find(IOTaskSQL.GET_CAN_FINSH_IOTASK_ITEM_BY_TASK_ID, taskId);
			if (!canFinishTaskItems.isEmpty()) {
				for (PackingListItem taskItem : canFinishTaskItems) {
					taskItem.setFinishTime(new Date()).update();
					List<Material> materials = Material.dao.find(IOTaskSQL.GET_MATERIAL_AND_OUTQUANTITY_BY_PACKING_LIST_ITEM_ID, taskItem.getId());
					for (Material material : materials) {
						material.setStatus(MaterialStatus.NORMAL).update();
					}
				}
			}
			PackingListItem packingListItem = PackingListItem.dao.findFirst(IOTaskSQL.GET_UNFINSH_PROBLEM_IOTASK_ITEM_BY_TASK_ID, taskId);
			if (packingListItem != null) {
				return "存在入库数量不符条目，请先完成后再操作";
			}
			task.setState(TaskState.FINISHED).setEndTime(new Date()).update();
		} else {
			List<PackingListItem> cutPackingListItems = PackingListItem.dao.find(IOTaskSQL.GET_CUTTING_PACKING_LIST_ITEM_BY_TASK_ID, taskId);
			Set<Integer> cutPackingListItemsIdSet = new HashSet<>();
			if (!cutPackingListItems.isEmpty()) {
				for (PackingListItem cutPackingListItem : cutPackingListItems) {
					cutPackingListItemsIdSet.add(cutPackingListItem.getId());
				}
			}
			// 完成已经可以完成（数量一致）的任务条目
			List<PackingListItem> canFinishTaskItems = PackingListItem.dao.find(IOTaskSQL.GET_CAN_FINSH_IOTASK_ITEM_BY_TASK_ID, taskId);
			if (!canFinishTaskItems.isEmpty()) {
				for (PackingListItem taskItem : canFinishTaskItems) {
					if (cutPackingListItemsIdSet.contains(taskItem.getId())) {
						continue;
					}
					taskItem.setFinishTime(new Date()).update();
					List<Material> materials = Material.dao.find(IOTaskSQL.GET_MATERIAL_AND_OUTQUANTITY_BY_PACKING_LIST_ITEM_ID, taskItem.getId());
					for (Material material : materials) {
						material.setStatus(MaterialStatus.NORMAL).setRemainderQuantity(material.getRemainderQuantity() - material.getInt("outQuantity")).update();
					}
				}
			}
			if (!cutPackingListItems.isEmpty()) {
				return "存在未截料的物料，请进行截料后再完成任务";
			}
			PackingListItem packingListItem = PackingListItem.dao.findFirst(IOTaskSQL.GET_UNFINSH_PROBLEM_IOTASK_ITEM_BY_TASK_ID, taskId);
			if (packingListItem != null) {
				return "存在出库数量不符条目，请先完成后再操作";
			}
		}
		Record problemRecord = Db.findFirst(IOTaskSQL.GET_PROBLEM_IOTASK_ITEM_BY_TASK_ID, taskId);
		if (problemRecord != null) {
			task.setState(TaskState.EXIST_LACK);
		} else {
			task.setState(TaskState.FINISHED);
		}
		task.update();

		return "操作成功";
	}

}
