package com.jimi.uw_server.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jimi.uw_server.agv.dao.EfficiencyRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.agv.handle.SampleTaskHandler;
import com.jimi.uw_server.constant.MaterialStatus;
import com.jimi.uw_server.constant.SamplerOutType;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.constant.sql.InventoryTaskSQL;
import com.jimi.uw_server.constant.sql.SQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.InventoryLog;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.SampleOutRecord;
import com.jimi.uw_server.model.SampleTaskItem;
import com.jimi.uw_server.model.SampleTaskMaterialRecord;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.SampleTaskItemBO;
import com.jimi.uw_server.model.vo.MaterialInfoVO;
import com.jimi.uw_server.model.vo.PackingSampleInfoVO;
import com.jimi.uw_server.model.vo.SampleTaskDetialsVO;
import com.jimi.uw_server.model.vo.TaskVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ExcelHelper;
import com.jimi.uw_server.util.ExcelWritter;


/**
 * 
 * @author trjie
 * @createTime 2019年6月20日  下午5:20:35
 */

public class SampleTaskService {

	private static final String GET_MATERIAL_TYPE_BY_NO_SQL = "SELECT * FROM material_type WHERE no = ? AND type= ? AND supplier = ? AND enabled = 1";

	private static final String GET_SAMPLETASKITEM_BY_TASK = "SELECT * FROM sample_task_item WHERE task_id = ?";

	private static final String GET_SAMPLETASKITEM_BY_TASK_AND_TYPE = "SELECT sample_task_item.* FROM sample_task_item INNER JOIN material_type ON material_type.id = sample_task_item.material_type_id WHERE task_id = ? AND material_type_id = ? AND material_type.type = ?";

	private static final String GET_MATERIAL_BOX_BY_SAMPLE_TASK = "SELECT DISTINCT material_box.* FROM material_box JOIN material JOIN sample_task_item ON sample_task_item.material_type_id = material.type AND material_box.id = material.box WHERE sample_task_item.task_id = ? AND material.remainder_quantity > 0 AND material.is_in_box = 1";

	private static final String GET_REGULAR_MATERIAL_BY_SAMPLE_TASK = "SELECT DISTINCT material.* FROM material_box JOIN material JOIN sample_task_item JOIN material_type ON sample_task_item.material_type_id = material.type AND material.type = material_type.id AND material_box.id = material.box WHERE sample_task_item.task_id = ? AND material.remainder_quantity > 0 AND material_type.type = ? AND material.is_in_box = ?";

	private static final String GET_PRECIOUS_MATERIAL_BY_SAMPLE_TASK = "SELECT DISTINCT material.* FROM material JOIN sample_task_item JOIN material_type ON sample_task_item.material_type_id = material.type AND material.type = material_type.id WHERE sample_task_item.task_id = ? AND material.remainder_quantity > 0 AND material_type.type = ? AND material.`status` = ?";

	private static final String GET_PROBLEM_PRECIOUS_MATERIAL_BY_SAMPLE_TASK = "SELECT DISTINCT material.* FROM material JOIN sample_task_item JOIN material_type ON sample_task_item.material_type_id = material.type AND material.type = material_type.id WHERE sample_task_item.task_id = ? AND material.remainder_quantity > 0 AND material_type.type = ? AND material.`status` != ?";

	private static final String GET_SAMPLE_TASK_DETIALS = "SELECT a.*, sample_out_record.id AS id, sample_out_record.material_id AS material_id, sample_out_record.operator AS operator, sample_out_record.quantity AS quantity, sample_out_record.out_type AS out_type, sample_out_record.time AS time FROM ( SELECT sample_task_item.task_id AS task_id, sample_task_item.id AS sample_task_item_id, sample_task_item.material_type_id AS material_type_id, sample_task_item.store_quantity AS store_quantity, sample_task_item.scan_quantity AS scan_quantity, sample_task_item.regular_out_quantity AS regular_out_quantity, sample_task_item.singular_out_quantity AS singular_out_quantity, sample_task_item.lost_out_quantity AS lost_out_quantity, material_type.`no` AS `no` FROM sample_task_item INNER JOIN material_type ON material_type.id = sample_task_item.material_type_id WHERE sample_task_item.task_id = ? AND material_type.enabled = 1) a LEFT JOIN sample_out_record ON sample_out_record.sample_task_item_id = a.sample_task_item_id ORDER BY a.sample_task_item_id";

	private static final String GET_RECORD_BY_MATERIALID_TASK_BOX = "SELECT * FROM sample_task_material_record WHERE material_id = ? AND task_id = ? AND box_id = ?";

	private static final String GET_RECORD_BY_MATERIALID_TASK = "SELECT * FROM sample_task_material_record WHERE material_id = ? AND task_id = ?";

	private static final String GET_UNSCAN_MATERIAL_BY_TASK_AND_BOX = "SELECT * FROM sample_task_material_record WHERE task_id = ? AND box_id = ? AND is_scaned = 0";

	private static final String GET_UNSCAN_MATERIAL_BY_TASK = "SELECT * FROM sample_task_material_record WHERE task_id = ? AND is_scaned = 0";

	private static final String GET_REGULAR_UNSCAN_MATERIAL_BY_MATERIAL = "SELECT * FROM sample_task_material_record WHERE task_id = ? AND box_id = ? AND material_id = ? AND is_scaned = 0";

	private static final String GET_PRECIOUS_UNSCAN_MATERIAL_BY_MATERIAL = "SELECT * FROM sample_task_material_record WHERE task_id = ? AND material_id = ? AND is_scaned = 0";

	private static final String GET_EXPROT_SAMPLE_TASK_INFO = "SELECT sample_task_item.task_id AS task_id, sample_task_item.id AS sample_task_item_id, sample_task_item.material_type_id AS material_type_id, sample_task_item.store_quantity AS store_quantity, sample_task_item.scan_quantity AS scan_quantity, sample_task_item.regular_out_quantity AS regular_out_quantity, sample_task_item.singular_out_quantity AS singular_out_quantity, material_type.`no` AS `no`, supplier.`name` AS `supplier_name` FROM sample_task_item INNER JOIN material_type INNER JOIN supplier ON material_type.id = sample_task_item.material_type_id AND material_type.supplier = supplier.id WHERE sample_task_item.task_id = ? ORDER BY sample_task_item.id";

	private static SelectService selectService = Aop.get(SelectService.class);

	private static SampleTaskHandler samTaskHandler = SampleTaskHandler.getInstance();

	public static SampleTaskService me = new SampleTaskService();

	private static MaterialService materialService = Aop.get(MaterialService.class);

	private static Object CANCEL_LOCK = new Object();

	private static Object FINISH_LOCK = new Object();

	private Integer batchSize = 2000;


	public String createSampleTask(File file, Integer supplierId, String remarks, Integer warehouseType) {
		String resultString = "操作成功";
		synchronized (Lock.IMPORT_SAMPLE_TASK_FILE_LOCK) {
			try {
				ExcelHelper excelHelper = ExcelHelper.from(file);
				List<SampleTaskItemBO> sampleTaskItemBOs = excelHelper.unfill(SampleTaskItemBO.class, 0);
				if (sampleTaskItemBOs == null || sampleTaskItemBOs.size() == 0) {
					throw new OperationException("创建任务失败，请检查表格的表头是否正确以及表格中是否有效的任务记录！");
				}
				HashSet<Integer> materialTypeSet = new LinkedHashSet<>();
				for (int i = 0; i < sampleTaskItemBOs.size(); i++) {
					SampleTaskItemBO item = sampleTaskItemBOs.get(i);
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) {
						if (item.getNo() == null || item.getNo().replaceAll(" ", "").equals("")) {
							throw new OperationException("创建任务失败，请检查套料单表格第" + i + "行的料号是否填写了准确信息！");
						}

						// 根据料号找到对应的物料类型
						MaterialType mType = MaterialType.dao.findFirst(GET_MATERIAL_TYPE_BY_NO_SQL, item.getNo(), warehouseType, supplierId);
						// 判断物料类型表中是否存在对应的料号且未被禁用，若不存在，则将对应的任务记录删除掉，并提示操作员检查套料单、新增对应的物料类型
						if (mType == null) {
							throw new OperationException("插入套料单失败，料号为" + item.getNo() + "的物料没有记录在物料类型表中或已被禁用，或者是供应商与料号对应不上！");
						}
						materialTypeSet.add(mType.getId());
					} else {
						break;
					}
				}
				if (materialTypeSet.isEmpty()) {
					throw new OperationException("创建任务失败，请检查表格中是否有效的任务记录！");
				}
				Supplier supplier = Supplier.dao.findById(supplierId);
				Date date = new Date();
				Task task = new Task();
				task.setFileName(getSampleTaskName(date, warehouseType, supplier.getName()));
				task.setSupplier(supplierId);
				task.setCreateTime(date);
				task.setRemarks(remarks);
				task.setType(TaskType.SAMPLE);
				task.setState(TaskState.WAIT_START);
				task.setWarehouseType(warehouseType);
				task.save();
				for (Integer materialTypeId : materialTypeSet) {
					SampleTaskItem sampleTaskItem = new SampleTaskItem();
					sampleTaskItem.setMaterialTypeId(materialTypeId);
					sampleTaskItem.setTaskId(task.getId());
					sampleTaskItem.setScanQuantity(0);
					sampleTaskItem.setStoreQuantity(0);
					sampleTaskItem.setRegularOutQuantity(0);
					sampleTaskItem.setSingularOutQuantity(0);
					sampleTaskItem.setLostOutQuantity(0);
					sampleTaskItem.save();
				}
			} catch (Exception e) {

				e.printStackTrace();
				throw new OperationException(e.getMessage());
			}
		}
		return resultString;

	}


	public String startRegularTask(Integer taskId, String windows) {
		synchronized (Lock.START_REGUALR_SAMPLETASK_LOCK) {
			Task task = Task.dao.findById(taskId);
			if (task == null || !task.getType().equals(TaskType.SAMPLE) || !task.getState().equals(TaskState.WAIT_START)) {
				throw new OperationException("抽检任务不存在或并未处于未开始状态，无法开始任务！");
			}
			Task inventoryTask = Task.dao.findFirst(InventoryTaskSQL.GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER, task.getSupplier(), WarehouseType.REGULAR);
			InventoryLog inventoryLog = null;
			if (inventoryTask != null) {
				inventoryLog = InventoryLog.dao.findFirst(InventoryTaskSQL.GET_UNCOVER_INVENTORY_LOG_BY_TASKID, inventoryTask.getId());
			}
			if (inventoryTask != null && inventoryLog != null) {
				throw new OperationException("当前供应商存在进行中的UW仓盘点任务，请等待任务结束后再开抽检任务!");
			}
			List<Window> wList = new ArrayList<>();
			String[] windowArr = windows.split(",");
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
			}
			Material material = Material.dao.findFirst(GET_REGULAR_MATERIAL_BY_SAMPLE_TASK, task.getId(), WarehouseType.REGULAR, false);
			if (material != null) {
				throw new OperationException("该抽检任务所抽检的物料类型存在不在料盒内的物料，请检查！");
			}
			List<Material> materials = Material.dao.find(GET_REGULAR_MATERIAL_BY_SAMPLE_TASK, task.getId(), WarehouseType.REGULAR, true);
			List<SampleTaskMaterialRecord> sampleTaskMaterialRecords = new ArrayList<>();
			for (Material material2 : materials) {
				SampleTaskMaterialRecord record = new SampleTaskMaterialRecord();
				record.setMaterialId(material2.getId());
				record.setBoxId(material2.getBox());
				record.setTaskId(task.getId());
				sampleTaskMaterialRecords.add(record);
			}
			Db.batchSave(sampleTaskMaterialRecords, batchSize);
			List<MaterialBox> materialBoxs = MaterialBox.dao.find(GET_MATERIAL_BOX_BY_SAMPLE_TASK, task.getId());
			List<AGVSampleTaskItem> agvSampleTaskItems = new ArrayList<>();
			for (MaterialBox materialBox : materialBoxs) {
				AGVSampleTaskItem agvSampleTaskItem = new AGVSampleTaskItem(taskId, materialBox.getId());
				agvSampleTaskItems.add(agvSampleTaskItem);
			}
			List<SampleTaskItem> sampleTaskItems = SampleTaskItem.dao.find(GET_SAMPLETASKITEM_BY_TASK, task.getId());
			// 填写本次抽检库存
			for (SampleTaskItem sampleTaskItem : sampleTaskItems) {
				sampleTaskItem.setStoreQuantity(materialService.countAndReturnRemainderQuantityByMaterialTypeId(sampleTaskItem.getMaterialTypeId()));
				sampleTaskItem.update();
			}
			task.setState(TaskState.PROCESSING);
			task.setStartTime(new Date());
			task.update();
			// 绑定仓口
			if (!agvSampleTaskItems.isEmpty()) {
				TaskItemRedisDAO.addSampleTaskItem(taskId, agvSampleTaskItems);
				for (Window window : wList) {
					window.setBindTaskId(task.getId());
					window.update();
				}
			} else {
				task.setState(TaskState.FINISHED);
				task.setEndTime(new Date());
				task.update();
			}
		}

		return "操作成功！";
	}


	public String startPreciousTask(Integer taskId) {
		synchronized (Lock.START_PRECIOUS_SAMPLETASK_LOCK) {
			Task task = Task.dao.findById(taskId);
			if (task == null || !task.getType().equals(TaskType.SAMPLE) || !task.getState().equals(TaskState.WAIT_START)) {
				throw new OperationException("抽检任务不存在或并未处于未开始状态，无法开始任务！");
			}
			Material material = Material.dao.findFirst(GET_PROBLEM_PRECIOUS_MATERIAL_BY_SAMPLE_TASK, task.getId(), WarehouseType.PRECIOUS, MaterialStatus.NORMAL);
			if (material != null) {
				throw new OperationException("该抽检任务所抽检的物料类型存在处于出库、入库或截料状态的物料，请检查！");
			}
			List<Material> materials = Material.dao.find(GET_PRECIOUS_MATERIAL_BY_SAMPLE_TASK, task.getId(), WarehouseType.PRECIOUS, MaterialStatus.NORMAL);
			for (Material material2 : materials) {
				SampleTaskMaterialRecord record = new SampleTaskMaterialRecord();
				record.setMaterialId(material2.getId());
				record.setTaskId(task.getId());
				record.save();
			}

			List<SampleTaskItem> sampleTaskItems = SampleTaskItem.dao.find(GET_SAMPLETASKITEM_BY_TASK, task.getId());
			// 填写本次抽检库存
			boolean flag = true;
			for (SampleTaskItem sampleTaskItem : sampleTaskItems) {
				int store = materialService.countPreciousQuantityByMaterialTypeId(sampleTaskItem.getMaterialTypeId());
				if (store != 0) {
					flag = false;
				}
				sampleTaskItem.setStoreQuantity(store);
				sampleTaskItem.update();
			}

			if (flag) {
				task.setState(TaskState.FINISHED);
				task.setEndTime(new Date());
				task.update();
			} else {
				task.setState(TaskState.PROCESSING);
				task.setStartTime(new Date());
				task.update();
			}
		}
		return "操作成功！";
	}


	// 作废指定任务
	public boolean cancelRegularTask(Integer id) {
		synchronized (CANCEL_LOCK) {
			Task task = Task.dao.findById(id);
			int state = task.getState();
			// 对于已完成的任务，禁止作废
			if (state == TaskState.FINISHED) {
				throw new OperationException("该任务已完成，禁止作废！");
			} else if (state == TaskState.CANCELED) { // 对于已作废过的任务，禁止作废
				throw new OperationException("该任务已作废！");
			} else {
				// 判断任务是否处于进行中状态，若是，则把相关的任务条目从til中剔除;若存在已分配的任务条目，则不解绑任务仓口
				if (state == TaskState.PROCESSING) {
					TaskItemRedisDAO.removeUnAssignedSampleTaskItemByTaskId(id);
				}
				// 更新任务状态为作废
				task.setState(TaskState.CANCELED).update();
				samTaskHandler.clearTask(task.getId());
				return true;
			}
		}
	}


	// 作废指定任务
	public boolean cancelPreciousTask(Integer id) {
		synchronized (CANCEL_LOCK) {
			Task task = Task.dao.findById(id);
			int state = task.getState();
			// 对于已完成的任务，禁止作废
			if (state == TaskState.FINISHED) {
				throw new OperationException("该任务已完成，禁止作废！");
			} else if (state == TaskState.CANCELED) { // 对于已作废过的任务，禁止作废
				throw new OperationException("该任务已作废！");
			} else {
				// 更新任务状态为作废
				task.setState(TaskState.CANCELED).update();
				return true;
			}
		}
	}


	// 作废指定任务
	public boolean finishPreciousTask(Integer id) {
		synchronized (FINISH_LOCK) {
			Task task = Task.dao.findById(id);
			int state = task.getState();
			// 对于已完成的任务，禁止完成
			if (state != TaskState.PROCESSING) {
				throw new OperationException("该任务未处于进行中状态，无法完成！");
			}
			// 更新任务状态为完成
			SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(GET_UNSCAN_MATERIAL_BY_TASK, id);
			if (record != null) {
				throw new OperationException("存在未扫码物料，无法完成任务");
			}
			task.setState(TaskState.FINISHED).update();
			return true;

		}
	}


	public String backRegularUWBox(String groupId) {
		synchronized (Lock.SAMPLE_TASK_REDIS_LOCK) {
			Task task = Task.dao.findById(Integer.valueOf(groupId.split("#")[1]));
			if (task == null) {
				throw new OperationException("任务不存在，回库失败！");
			}
			for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems(Integer.valueOf(groupId.split("#")[1]))) {
				if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
					MaterialBox materialBox = MaterialBox.dao.findById(agvSampleTaskItem.getBoxId());
					SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(GET_UNSCAN_MATERIAL_BY_TASK_AND_BOX, agvSampleTaskItem.getTaskId(), agvSampleTaskItem.getBoxId());
					if (record != null) {
						throw new OperationException("存在尚未扫描的料盘，回库失败！");
					}
					try {
						GoodsLocation goodsLocation = GoodsLocation.dao.findById(agvSampleTaskItem.getGoodsLocationId());
						if (goodsLocation != null) {
							samTaskHandler.sendBackLL(agvSampleTaskItem, materialBox, goodsLocation, task.getPriority());
						} else {
							throw new OperationException("找不到目的货位，仓口：" + agvSampleTaskItem.getWindowId() + "货位：" + agvSampleTaskItem.getGoodsLocationId());
						}
						TaskItemRedisDAO.updateSampleTaskItemInfo(agvSampleTaskItem, TaskItemState.START_BACK, null, null, null, true);
					} catch (Exception e) {
						e.printStackTrace();
						throw new OperationException("发送回库指令失败，+ " + e.getMessage());
					}
					break;
				}
			}
		}
		return "操作完成";
	}


	public String outRegularTaskSingular(String materialId, String groupId, User user) {
		synchronized (Lock.REGULAR_SAMPLE_TASK_OUT_LOCK) {
			for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems(Integer.valueOf(groupId.split("#")[1]))) {
				if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
					Material material = Material.dao.findById(materialId);
					if (material == null || material.getRemainderQuantity() <= 0 || !material.getIsInBox()) {
						throw new OperationException("料盘不存在或者已经出库！");
					}
					SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, agvSampleTaskItem.getTaskId(), material.getType(), WarehouseType.REGULAR);
					if (sampleTaskItem == null) {
						throw new OperationException("该料盘不在本次抽检范围内！");
					}
					if (!material.getBox().equals(agvSampleTaskItem.getBoxId())) {
						throw new OperationException("该料盘不在当前料盒内！");
					}
					SampleTaskMaterialRecord sampleTaskMaterialRecord = SampleTaskMaterialRecord.dao.findFirst(GET_REGULAR_UNSCAN_MATERIAL_BY_MATERIAL, agvSampleTaskItem.getTaskId(), agvSampleTaskItem.getBoxId(), materialId);
					if (sampleTaskMaterialRecord != null) {
						throw new OperationException("该料盘尚未扫码，请先扫码后再出库！");
					}
					SampleOutRecord sampleOutRecord = new SampleOutRecord();
					sampleOutRecord.setMaterialId(materialId);
					sampleOutRecord.setSampleTaskItemId(sampleTaskItem.getId());
					sampleOutRecord.setOutType(SamplerOutType.SINGULAR);
					sampleOutRecord.setQuantity(material.getRemainderQuantity());
					sampleOutRecord.setBoxId(material.getBox());
					sampleOutRecord.setOperator(user.getUid());
					sampleOutRecord.setTime(new Date());
					sampleOutRecord.save();

					sampleTaskItem.setSingularOutQuantity(sampleTaskItem.getSingularOutQuantity() + material.getRemainderQuantity());
					sampleTaskItem.update();

					material.setRemainderQuantity(0);
					material.setCol(-1);
					material.setRow(-1);
					material.setIsInBox(false);
					material.update();
				}
			}
		}
		return "操作成功";
	}


	public String outPreciousTaskSingular(String materialId, Integer taskId, User user) {
		synchronized (Lock.PRECIOUS_SAMPLE_TASK_OUT_LOCK) {

			Material material = Material.dao.findById(materialId);
			if (material == null || material.getRemainderQuantity() <= 0) {
				throw new OperationException("料盘不存在或者已经出库！");
			}
			if (!material.getStatus().equals(MaterialStatus.NORMAL)) {
				throw new OperationException("该料盘目前不属于正常状态，可能出库、入库或者截料中！");
			}
			SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, taskId, material.getType(), WarehouseType.PRECIOUS);
			if (sampleTaskItem == null) {
				throw new OperationException("该料盘不在本次抽检范围内！");
			}
			SampleTaskMaterialRecord sampleTaskMaterialRecord = SampleTaskMaterialRecord.dao.findFirst(GET_PRECIOUS_UNSCAN_MATERIAL_BY_MATERIAL, taskId, materialId);
			if (sampleTaskMaterialRecord != null) {
				throw new OperationException("该料盘尚未扫码，请先扫码后再出库！");
			}
			SampleOutRecord sampleOutRecord = new SampleOutRecord();
			sampleOutRecord.setMaterialId(materialId);
			sampleOutRecord.setSampleTaskItemId(sampleTaskItem.getId());
			sampleOutRecord.setOutType(SamplerOutType.SINGULAR);
			sampleOutRecord.setQuantity(material.getRemainderQuantity());
			sampleOutRecord.setOperator(user.getUid());
			sampleOutRecord.setTime(new Date());
			sampleOutRecord.save();

			sampleTaskItem.setSingularOutQuantity(sampleTaskItem.getSingularOutQuantity() + material.getRemainderQuantity());
			sampleTaskItem.update();

			material.setRemainderQuantity(0);
			material.update();
		}

		return "操作成功";
	}


	public String outRegularTaskRegular(String materialId, String groupId, User user) {
		synchronized (Lock.REGULAR_SAMPLE_TASK_OUT_LOCK) {
			for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems(Integer.valueOf(groupId.split("#")[1]))) {
				if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
					Material material = Material.dao.findById(materialId);
					if (material == null || material.getRemainderQuantity() <= 0 || !material.getIsInBox()) {
						throw new OperationException("料盘不存在或者已经出库！");
					}
					SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, agvSampleTaskItem.getTaskId(), material.getType(), WarehouseType.REGULAR);
					if (sampleTaskItem == null) {
						throw new OperationException("该料盘不在本次抽检范围内！");
					}
					if (!material.getBox().equals(agvSampleTaskItem.getBoxId())) {
						throw new OperationException("该料盘不在当前料盒内！");
					}
					SampleTaskMaterialRecord sampleTaskMaterialRecord = SampleTaskMaterialRecord.dao.findFirst(GET_REGULAR_UNSCAN_MATERIAL_BY_MATERIAL, agvSampleTaskItem.getTaskId(), agvSampleTaskItem.getBoxId(), materialId);
					if (sampleTaskMaterialRecord != null) {
						throw new OperationException("该料盘尚未扫码，请先扫码后再出库！");
					}
					SampleOutRecord sampleOutRecord = new SampleOutRecord();
					sampleOutRecord.setMaterialId(materialId);
					sampleOutRecord.setSampleTaskItemId(sampleTaskItem.getId());
					sampleOutRecord.setOutType(SamplerOutType.REGULAR);
					sampleOutRecord.setQuantity(material.getRemainderQuantity());
					sampleOutRecord.setBoxId(material.getBox());
					sampleOutRecord.setOperator(user.getUid());
					sampleOutRecord.setTime(new Date());
					sampleOutRecord.save();

					sampleTaskItem.setRegularOutQuantity(sampleTaskItem.getRegularOutQuantity() + material.getRemainderQuantity());
					sampleTaskItem.update();

					material.setRemainderQuantity(0);
					material.setCol(-1);
					material.setRow(-1);
					material.setIsInBox(false);
					material.setIsRepeated(true);
					material.update();
				}
			}
		}
		return "操作成功";
	}


	public String outPreciousTaskRegular(String materialId, Integer taskId, User user) {
		synchronized (Lock.REGULAR_SAMPLE_TASK_OUT_LOCK) {
			Material material = Material.dao.findById(materialId);
			if (material == null || material.getRemainderQuantity() <= 0) {
				throw new OperationException("料盘不存在或者已经出库！");
			}
			if (!material.getStatus().equals(MaterialStatus.NORMAL)) {
				throw new OperationException("该料盘目前不属于正常状态，可能出库、入库或者截料中！");
			}
			SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, taskId, material.getType(), WarehouseType.PRECIOUS);
			if (sampleTaskItem == null) {
				throw new OperationException("该料盘不在本次抽检范围内！");
			}
			SampleTaskMaterialRecord sampleTaskMaterialRecord = SampleTaskMaterialRecord.dao.findFirst(GET_PRECIOUS_UNSCAN_MATERIAL_BY_MATERIAL, taskId, materialId);
			if (sampleTaskMaterialRecord != null) {
				throw new OperationException("该料盘尚未扫码，请先扫码后再出库！");
			}
			SampleOutRecord sampleOutRecord = new SampleOutRecord();
			sampleOutRecord.setMaterialId(materialId);
			sampleOutRecord.setSampleTaskItemId(sampleTaskItem.getId());
			sampleOutRecord.setOutType(SamplerOutType.REGULAR);
			sampleOutRecord.setQuantity(material.getRemainderQuantity());
			sampleOutRecord.setOperator(user.getUid());
			sampleOutRecord.setTime(new Date());
			sampleOutRecord.save();

			sampleTaskItem.setRegularOutQuantity(sampleTaskItem.getRegularOutQuantity() + material.getRemainderQuantity());
			sampleTaskItem.update();

			material.setRemainderQuantity(0);
			material.setIsRepeated(true);
			material.update();
		}

		return "操作成功";
	}


	public String outRegularTaskLost(String materialId, String groupId, User user) {
		synchronized (Lock.REGULAR_SAMPLE_TASK_OUT_LOCK) {
			for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems(Integer.valueOf(groupId.split("#")[1]))) {
				if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
					Material material = Material.dao.findById(materialId);
					if (material == null || material.getRemainderQuantity() <= 0 || !material.getIsInBox()) {
						throw new OperationException("料盘不存在或者已经出库！");
					}
					SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, agvSampleTaskItem.getTaskId(), material.getType(), WarehouseType.REGULAR);
					if (sampleTaskItem == null) {
						throw new OperationException("该料盘不在本次抽检范围内！");
					}
					if (!material.getBox().equals(agvSampleTaskItem.getBoxId())) {
						throw new OperationException("该料盘不在当前料盒内！");
					}
					SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(GET_RECORD_BY_MATERIALID_TASK_BOX, materialId, agvSampleTaskItem.getTaskId(), agvSampleTaskItem.getBoxId());
					if (!record.getIsScaned()) {
						record.setIsScaned(true).update();
						sampleTaskItem.setScanQuantity(sampleTaskItem.getScanQuantity() + material.getRemainderQuantity());
						sampleTaskItem.update();
					} else {
						throw new OperationException("料盘已扫码，不能进行料盘丢失操作！");
					}
					SampleOutRecord sampleOutRecord = new SampleOutRecord();
					sampleOutRecord.setMaterialId(materialId);
					sampleOutRecord.setSampleTaskItemId(sampleTaskItem.getId());
					sampleOutRecord.setOutType(SamplerOutType.LOST);
					sampleOutRecord.setQuantity(material.getRemainderQuantity());
					sampleOutRecord.setBoxId(material.getBox());
					sampleOutRecord.setOperator(user.getUid());
					sampleOutRecord.setTime(new Date());
					sampleOutRecord.save();

					sampleTaskItem.setLostOutQuantity(sampleTaskItem.getLostOutQuantity() + material.getRemainderQuantity());
					sampleTaskItem.update();

					material.setRemainderQuantity(0);
					material.setCol(-1);
					material.setRow(-1);
					material.setIsInBox(false);
					material.setIsRepeated(true);
					material.update();
				}
			}
		}
		return "操作成功";
	}


	public String outPreciousTaskLost(String materialId, Integer taskId, User user) {
		synchronized (Lock.REGULAR_SAMPLE_TASK_OUT_LOCK) {
			Material material = Material.dao.findById(materialId);
			if (material == null || material.getRemainderQuantity() <= 0) {
				throw new OperationException("料盘不存在或者已经出库！");
			}
			if (!material.getStatus().equals(MaterialStatus.NORMAL)) {
				throw new OperationException("该料盘目前不属于正常状态，可能出库、入库或者截料中！");
			}
			SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, taskId, material.getType(), WarehouseType.PRECIOUS);
			if (sampleTaskItem == null) {
				throw new OperationException("该料盘不在本次抽检范围内！");
			}
			SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(GET_RECORD_BY_MATERIALID_TASK, materialId, taskId);
			if (!record.getIsScaned()) {
				record.setIsScaned(true).update();
				sampleTaskItem.setScanQuantity(sampleTaskItem.getScanQuantity() + material.getRemainderQuantity());
				sampleTaskItem.update();
			} else {
				throw new OperationException("料盘已扫码，不能进行料盘丢失操作！");
			}
			SampleOutRecord sampleOutRecord = new SampleOutRecord();
			sampleOutRecord.setMaterialId(materialId);
			sampleOutRecord.setSampleTaskItemId(sampleTaskItem.getId());
			sampleOutRecord.setOutType(SamplerOutType.LOST);
			sampleOutRecord.setQuantity(material.getRemainderQuantity());
			sampleOutRecord.setOperator(user.getUid());
			sampleOutRecord.setTime(new Date());
			sampleOutRecord.save();

			sampleTaskItem.setLostOutQuantity(sampleTaskItem.getLostOutQuantity() + material.getRemainderQuantity());
			sampleTaskItem.update();

			material.setRemainderQuantity(0);
			material.setIsRepeated(true);
			material.update();
		}
		return "操作成功";
	}


	public void sampleRegularUWMaterial(String materialId, String groupId) {

		Integer boxId = Integer.valueOf(groupId.split("#")[0]);
		Integer taskId = Integer.valueOf(groupId.split("#")[1]);
		Material material = Material.dao.findById(materialId);
		if (material == null) {
			throw new OperationException("料盘不存在！");
		}
		if (boxId == null || taskId == null) {
			throw new OperationException("groupId解析失败，获取不到任务ID和料盒号！");
		}
		SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(GET_RECORD_BY_MATERIALID_TASK_BOX, materialId, taskId, boxId);
		if (record == null) {
			throw new OperationException("本次抽检抽检范围不包含该料盘！");
		}
		if (record.getIsScaned()) {
			throw new OperationException("该料盘已经扫描过，请勿重复扫描！");
		} else {

			SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, taskId, material.getType(), WarehouseType.REGULAR);
			if (sampleTaskItem == null) {
				throw new OperationException("本次抽检抽检范围不包含该料盘！");
			}
			record.setIsScaned(true).update();
			sampleTaskItem.setScanQuantity(sampleTaskItem.getScanQuantity() + material.getRemainderQuantity());
			sampleTaskItem.update();
		}

	}


	public void samplePreciousUWMaterial(String materialId, Integer taskId) {
		Task task = Task.dao.findById(taskId);
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("任务未处于进行中状态！");
		}
		Material material = Material.dao.findById(materialId);
		if (material == null) {
			throw new OperationException("料盘不存在！");
		}
		SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(GET_RECORD_BY_MATERIALID_TASK, materialId, taskId);
		if (record == null) {
			throw new OperationException("本次抽检抽检范围不包含该料盘！");
		}
		if (record.getIsScaned()) {
			throw new OperationException("该料盘已经扫描过，请勿重复扫描！");
		} else {
			SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, taskId, material.getType(), WarehouseType.PRECIOUS);
			if (sampleTaskItem == null) {
				throw new OperationException("本次抽检抽检范围不包含该料盘！");
			}
			record.setIsScaned(true).update();
			sampleTaskItem.setScanQuantity(sampleTaskItem.getScanQuantity() + material.getRemainderQuantity());
			sampleTaskItem.update();
		}

	}


	public List<PackingSampleInfoVO> getPackingSampleMaterialInfo(Integer windowId) {
		Integer boxId = 0;
		String groupId = "";
		Window window = Window.dao.findById(windowId);
		if (window == null || window.getBindTaskId() == null) {
			throw new OperationException("仓口不存在任务");
		}
		Map<Integer, PackingSampleInfoVO> map = new LinkedHashMap<>();
		List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, windowId);
		for (GoodsLocation goodsLocation : goodsLocations) {
			map.put(goodsLocation.getId(), new PackingSampleInfoVO(goodsLocation));
		}
		for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems(window.getBindTaskId())) {
			if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getWindowId().equals(windowId)) {
				boxId = agvSampleTaskItem.getBoxId();
				groupId = agvSampleTaskItem.getGroupId();
				PackingSampleInfoVO info = map.get(agvSampleTaskItem.getGoodsLocationId());
				if (info == null) {
					throw new OperationException("仓口 " + windowId + "没有对应货位" + agvSampleTaskItem.getGoodsLocationId());
				}
				if (info.getBoxId() != null) {
					throw new OperationException("仓口 " + windowId + "的货位" + agvSampleTaskItem.getGoodsLocationId() + "有一个以上的到站任务条目，请检查!");
				}
				List<MaterialInfoVO> materialInfoVOs = new ArrayList<>();

				List<Record> unOutRecords = Db.find(SQL.GET_REGULAR_UN_OUT_SAMPLE_TAKS_MATERIAL, boxId, window.getBindTaskId());
				Integer totalNum = unOutRecords.size();
				Integer scanNum = 0;
				for (Record record : unOutRecords) {
					MaterialInfoVO materialInfoVO = new MaterialInfoVO();
					materialInfoVO.setMaterailTypeId(record.getInt("material_type_id"));
					materialInfoVO.setMaterialId(record.getStr("id"));
					materialInfoVO.setNo(record.getStr("no"));
					materialInfoVO.setSpecification(record.getStr("specification"));
					materialInfoVO.setStoreNum(record.getInt("quantity"));
					materialInfoVO.setSupplierId(record.getInt("supplier_id"));
					materialInfoVO.setSupplier(record.getStr("supplier_name"));
					materialInfoVO.setProductionTime(record.getDate("production_time"));
					materialInfoVO.setActualNum(0);
					materialInfoVO.setIsScaned(record.getBoolean("is_scaned"));
					materialInfoVO.setCol(record.getInt("col"));
					materialInfoVO.setRow(record.getInt("row"));
					materialInfoVO.setIsOuted(-1);
					materialInfoVOs.add(materialInfoVO);
					if (record.getBoolean("is_scaned")) {
						scanNum++;
					}
				}
				List<Record> OutRecords = Db.find(SQL.GET_REGULAR_OUT_SAMPLE_TASK_MATERIAL, boxId, window.getBindTaskId());
				totalNum += OutRecords.size();
				Integer outNum = 0;
				for (Record record : OutRecords) {
					MaterialInfoVO materialInfoVO = new MaterialInfoVO();
					materialInfoVO.setMaterailTypeId(record.getInt("material_type_id"));
					materialInfoVO.setMaterialId(record.getStr("id"));
					materialInfoVO.setNo(record.getStr("no"));
					materialInfoVO.setSpecification(record.getStr("specification"));
					materialInfoVO.setStoreNum(record.getInt("quantity"));
					materialInfoVO.setSupplierId(record.getInt("supplier_id"));
					materialInfoVO.setSupplier(record.getStr("supplier_name"));
					materialInfoVO.setProductionTime(record.getDate("production_time"));
					materialInfoVO.setIsScaned(true);
					materialInfoVO.setIsOuted(record.getInt("out_type"));
					materialInfoVO.setActualNum(0);
					materialInfoVO.setCol(record.getInt("col"));
					materialInfoVO.setRow(record.getInt("row"));
					materialInfoVOs.add(materialInfoVO);
					outNum++;
				}
				scanNum = scanNum + outNum;
				info.setBoxId(boxId);
				info.setTaskId(window.getBindTaskId());
				info.setWindowId(windowId);
				info.setGroupId(groupId);
				info.setTotalNum(totalNum);
				info.setScanNum(scanNum);
				info.setOutNum(outNum);
				info.setList(materialInfoVOs);
			}
		}
		return new ArrayList<>(map.values());

	}


	public PackingSampleInfoVO getSampleTaskMaterialInfo(Integer taskId) {
		PackingSampleInfoVO infoVO = new PackingSampleInfoVO();
		List<MaterialInfoVO> materialInfoVOs = new ArrayList<>();
		List<Record> unOutRecords = Db.find(SQL.GET_PRECIOUS_UN_OUT_SAMPLE_TAKS_MATERIAL, MaterialStatus.NORMAL, taskId);
		Integer totalNum = unOutRecords.size();
		Integer scanNum = 0;
		for (Record record : unOutRecords) {
			MaterialInfoVO materialInfoVO = new MaterialInfoVO();
			materialInfoVO.setMaterailTypeId(record.getInt("material_type_id"));
			materialInfoVO.setMaterialId(record.getStr("id"));
			materialInfoVO.setNo(record.getStr("no"));
			materialInfoVO.setSpecification(record.getStr("specification"));
			materialInfoVO.setStoreNum(record.getInt("quantity"));
			materialInfoVO.setSupplierId(record.getInt("supplier_id"));
			materialInfoVO.setSupplier(record.getStr("supplier_name"));
			materialInfoVO.setProductionTime(record.getDate("production_time"));
			materialInfoVO.setActualNum(0);
			materialInfoVO.setIsScaned(record.getBoolean("is_scaned"));
			materialInfoVO.setCol(record.getInt("col"));
			materialInfoVO.setRow(record.getInt("row"));
			materialInfoVO.setIsOuted(-1);
			materialInfoVOs.add(materialInfoVO);
			if (record.getBoolean("is_scaned")) {
				scanNum++;
			}
		}
		List<Record> OutRecords = Db.find(SQL.GET_PRECIOUS_OUT_SAMPLE_TASK_MATERIAL, taskId);
		totalNum += OutRecords.size();
		Integer outNum = 0;
		for (Record record : OutRecords) {
			MaterialInfoVO materialInfoVO = new MaterialInfoVO();
			materialInfoVO.setMaterailTypeId(record.getInt("material_type_id"));
			materialInfoVO.setMaterialId(record.getStr("id"));
			materialInfoVO.setNo(record.getStr("no"));
			materialInfoVO.setSpecification(record.getStr("specification"));
			materialInfoVO.setStoreNum(record.getInt("quantity"));
			materialInfoVO.setSupplierId(record.getInt("supplier_id"));
			materialInfoVO.setSupplier(record.getStr("supplier_name"));
			materialInfoVO.setProductionTime(record.getDate("production_time"));
			materialInfoVO.setIsScaned(true);
			materialInfoVO.setIsOuted(record.getInt("out_type"));
			materialInfoVO.setActualNum(0);
			materialInfoVO.setCol(record.getInt("col"));
			materialInfoVO.setRow(record.getInt("row"));
			materialInfoVOs.add(materialInfoVO);
			outNum++;
		}
		scanNum = scanNum + outNum;

		infoVO.setTaskId(taskId);
		infoVO.setTotalNum(totalNum);
		infoVO.setScanNum(scanNum);
		infoVO.setOutNum(outNum);
		infoVO.setList(materialInfoVOs);
		return infoVO;

	}


	public List<SampleTaskDetialsVO> getSampleTaskDetials(Integer taskId) {

		SqlPara sqlPara = new SqlPara();
		sqlPara.setSql(GET_SAMPLE_TASK_DETIALS);
		sqlPara.addPara(taskId);
		List<Record> records = Db.find(sqlPara);
		List<SampleTaskDetialsVO> sampleTaskDetialsVOs = SampleTaskDetialsVO.fillList(records);
		return sampleTaskDetialsVOs;
	}


	// 查询所有任务
	public Object select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		if (filter == null) {
			filter = "task.type=7";
		} else {
			filter = filter + "#&#task.type=7";
		}
		Page<Record> result = selectService.select("task", pageNo, pageSize, ascBy, descBy, filter);
		List<TaskVO> taskVOs = new ArrayList<TaskVO>();
		Boolean status;
		List<Window> windows = Window.dao.find(SQL.GET_WORKING_WINDOWS);
		Set<Integer> windowBindTaskSet = new HashSet<>();
		if (!windows.isEmpty()) {
			for (Window window : windows) {
				windowBindTaskSet.add(window.getBindTaskId());
			}
		}
		for (Record record : result.getList()) {
			status = false;
			if (record.getInt("state").equals(TaskState.PROCESSING) && windowBindTaskSet.contains(record.getInt("id"))) {
				status = TaskItemRedisDAO.getTaskStatus(record.getInt("id"));
			}
			TaskVO t = new TaskVO(record.get("id"), record.get("state"), record.get("type"), record.get("file_name"), record.get("create_time"), record.get("priority"), record.get("supplier"), record.get("remarks"), status);
			taskVOs.add(t);
		}

		// 分页，设置页码，每页显示条目等
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(taskVOs);

		return pagePaginate;
	}


	public String getSampleTaskName(Date date, Integer warehouseType, String supplierName) {
		String fileName = "";
		if (warehouseType.equals(WarehouseType.REGULAR)) {
			fileName = "抽检_";
		} else if (warehouseType.equals(WarehouseType.PRECIOUS)) {
			fileName = supplierName + "贵重仓抽检_";
		}

		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = formatter.format(date);
		fileName = fileName + time;
		return fileName;
	}


	public void exportSampleTaskInfo(Integer taskId, String fileName, OutputStream output) {
		try {
			List<Record> records = Db.find(GET_EXPROT_SAMPLE_TASK_INFO, taskId);
			String[] field = null;
			String[] head = null;
			field = new String[] {"supplier_name", "no", "store_quantity", "scan_quantity", "regular_out_quantity", "singular_out_quantity"};
			head = new String[] {"供应商", "料号", "库存数量", "抽检数量", "抽检出库数量", "异常出库数量"};
			ExcelWritter writter = ExcelWritter.create(true);
			writter.fill(records, fileName, field, head);
			writter.write(output, true);
		} catch (IOException e) {
			e.printStackTrace();
			throw new OperationException("下载失败" + e.getMessage());
		}
	}


	/**
	 * <p>Description:强制解绑仓口，仅有作废任务可以解绑 <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2019年11月27日
	 */
	public void forceUnbundlingWindow(Integer taskId) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getState().equals(TaskState.CANCELED)) {
			throw new OperationException("仓口绑定任务未处于作废状态，无法解绑！");
		}
		List<Window> windows = Window.dao.find(SQL.GET_WINDOW_BY_TASK, task.getId());
		if (windows == null || windows.isEmpty()) {
			throw new OperationException("任务并未绑定仓口，无需解绑！");
		}
		TaskItemRedisDAO.removeSampleTaskItemByTaskId(task.getId());
		for (Window window : windows) {
			List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOW, window.getId());
			if (!goodsLocations.isEmpty()) {
				for (GoodsLocation goodsLocation : goodsLocations) {
					TaskItemRedisDAO.delLocationStatus(window.getId(), goodsLocation.getId());
				}
			}
			window.setBindTaskId(null).update();
		}
	}
	
	
	public String getTaskName(Integer taskId) {

		Task task = Task.dao.findById(taskId);
		if (task == null) {
			throw new OperationException("任务不存在!");
		}
		return task.getFileName();
	}
}
