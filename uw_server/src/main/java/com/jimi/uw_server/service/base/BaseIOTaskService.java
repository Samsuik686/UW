/**  
*  
*/
package com.jimi.uw_server.service.base;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jimi.uw_server.agv.dao.IOTaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.constant.MaterialStatus;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.constant.sql.IOTaskSQL;
import com.jimi.uw_server.constant.sql.MaterialTypeSQL;
import com.jimi.uw_server.constant.sql.SQL;
import com.jimi.uw_server.constant.sql.WindowSQL;
import com.jimi.uw_server.exception.FormDataValidateFailedException;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.PackingListItemBO;
import com.jimi.uw_server.model.vo.IOTaskDetailVO;
import com.jimi.uw_server.model.vo.IOTaskInfo;
import com.jimi.uw_server.model.vo.TaskVO;
import com.jimi.uw_server.service.ExternalWhLogService;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.service.inventory.RegularInventoryTaskService;
import com.jimi.uw_server.util.ExcelHelper;
import com.jimi.uw_server.util.ExcelWritter;
import com.jimi.uw_server.util.PagePaginate;

/**
 * <p>
 * Title: BaseTaskService
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
public class BaseIOTaskService {

	protected static SelectService selectService = Aop.get(SelectService.class);

	protected static MaterialService materialService = Aop.get(MaterialService.class);

	protected static ExternalWhLogService externalWhLogService = Aop.get(ExternalWhLogService.class);

	protected static RegularInventoryTaskService inventoryTaskService = Aop.get(RegularInventoryTaskService.class);

	private static final String DELETE_PACKING_LIST_ITEM_BY_TASK_ID_SQL = "DELETE FROM packing_list_item WHERE task_id = ?";

	private static final String GET_UNCANCLE_TASK_BY_FILE_NAME_SQL = "SELECT * FROM task WHERE file_name = ? and state < 4";

	// 创建出入库/退料任务
	public void createTask(Integer type, String fileName, File file, Integer supplier, Integer destination, Boolean isInventoryApply, Integer inventoryTaskId, String remarks, Integer warehouseType,
			Boolean isForced, Boolean isDeducted) throws Exception {
		String resultString = "";

		// 如果文件格式不对，则提示检查文件格式
		if (!(fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
			// 清空upload目录下的文件
			resultString = "创建任务失败，请检查套料单的文件格式是否正确！";
			throw new OperationException(resultString);
		}

		Map<Integer, Integer> taskDetailsMap = new LinkedHashMap<>();
		ExcelHelper fileReader = ExcelHelper.from(file);
		List<PackingListItemBO> items = fileReader.unfill(PackingListItemBO.class, 2);
		// 如果套料单表头不对或者表格中没有有效的任务记录
		if (items == null || items.size() == 0) {
			resultString = "创建任务失败，请检查套料单的表头是否正确以及套料单表格中是否有效的任务记录！";
			throw new OperationException(resultString);
		} else {
			synchronized (Lock.CREATE_IOTASK_LOCK) {
				// 如果已经用该套料单创建过任务，并且该任务没有被作废，则禁止再导入相同文件名的套料单
				if (Task.dao.find(GET_UNCANCLE_TASK_BY_FILE_NAME_SQL, fileName).size() > 0) {
					resultString = "创建任务失败，已经用该套料单创建过任务，请先作废掉原来的套料单任务！或者修改原套料单文件名，如：套料单A-重新入库";
					throw new OperationException(resultString);
				}
				// 从套料单电子表格第四行开始有任务记录
				int i = 4;
				// 读取excel表格的套料单数据，将数据一条条写入到套料单表
				List<PackingListItemBO> validationFailedRecords = new ArrayList<>(items.size());
				for (PackingListItemBO item : items) {
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) { // 只读取有序号的行数据

						if (item.getNo() == null || item.getQuantity() == null || item.getNo().replaceAll(" ", "").equals("") || item.getQuantity().toString().replaceAll(" ", "").equals("")) {
							resultString = "创建任务失败，请检查套料单表格第" + i + "行的料号或需求数列是否填写了准确信息！";
							throw new OperationException(resultString);
						}

						if (item.getQuantity() <= 0) {
							resultString = "创建任务失败，套料单表格第" + i + "行的需求数为" + item.getQuantity() + "，需求数必须大于0！";
							throw new OperationException(resultString);
						}
						// 根据料号找到对应的物料类型
						MaterialType mType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_AND_TYPE_SQL, item.getNo().trim(), supplier, warehouseType);
						// 判断物料类型表中是否存在对应的料号且未被禁用，若不存在，则将对应的任务记录删除掉，并提示操作员检查套料单、新增对应的物料类型
						if (mType == null) {
							if (!isForced) {
								validationFailedRecords.add(item);
							}
							continue;
						}
						if (taskDetailsMap.get(mType.getId()) == null) {
							taskDetailsMap.put(mType.getId(), item.getQuantity());
						} else {
							taskDetailsMap.put(mType.getId(), taskDetailsMap.get(mType.getId()) + item.getQuantity());
						}
						i++;
					} else if (i == 4) { // 若第四行就没有序号，则说明套料单表格没有一条任务记录
						resultString = "创建任务失败，套料单表格没有任何有效的物料信息记录！";
						throw new OperationException(resultString);
					} else {
						break;
					}
				}
				if (!validationFailedRecords.isEmpty()) {
					throw new FormDataValidateFailedException("表格校验存在问题！", validationFailedRecords);
				}
				if (taskDetailsMap.isEmpty()) {
					resultString = "创建任务失败，套料单表格没有任何有效的物料信息记录！";
					throw new OperationException(resultString);
				}
				Task task = new Task();
				task.setType(type);
				task.setFileName(fileName);
				task.setState(TaskState.WAIT_REVIEW);
				task.setCreateTime(new Date());
				task.setSupplier(supplier);
				task.setDestination(destination);
				task.setRemarks(remarks);
				task.setIsDeducted(isDeducted);
				if (isInventoryApply != null && isInventoryApply) {
					task.setIsInventoryApply(true);
					if (inventoryTaskId == null) {
						resultString = "创建任务失败，未选择盘点期间申补单所绑定的盘点任务！";
						throw new OperationException(resultString);
					}
					task.setInventoryTaskId(inventoryTaskId);
				} else {
					task.setIsInventoryApply(false);
				}
				task.setWarehouseType(warehouseType);
				task.save();

				for (Entry<Integer, Integer> entry : taskDetailsMap.entrySet()) {
					PackingListItem packingListItem = new PackingListItem();
					// 将任务条目插入套料单
					Integer materialTypeId = entry.getKey();
					// 添加物料类型id
					packingListItem.setMaterialTypeId(materialTypeId);
					// 添加计划出入库数量
					packingListItem.setQuantity(entry.getValue());
					// 添加任务id
					packingListItem.setTaskId(task.getId());
					// 保存该记录到套料单表
					packingListItem.save();
				}
			}
		}
	}

	public PagePaginate check(Integer id, Integer type, Integer pageSize, Integer pageNo) {
		List<IOTaskDetailVO> ioTaskDetailVOs = new ArrayList<IOTaskDetailVO>();
		// 如果任务类型为出入库
		if (type == TaskType.IN || type == TaskType.OUT || type == TaskType.SEND_BACK || type == TaskType.EMERGENCY_OUT) {
			// 先进行多表查询，查询出同一个任务id的套料单表的id,物料类型表的料号no,套料单表的计划出入库数量quantity,套料单表对应任务的实际完成时间finish_time
			Page<Record> packingListItems = selectService.select(new String[] { "packing_list_item", "material_type" },
					new String[] { "packing_list_item.task_id = " + id.toString(), "material_type.id = packing_list_item.material_type_id" }, pageNo, pageSize, null, null, null);

			// 遍历同一个任务id的套料单数据
			for (Record packingListItem : packingListItems.getList()) {
				Task task = Task.dao.findById(packingListItem.getInt("PackingListItem_TaskId"));
				IOTaskDetailVO io = new IOTaskDetailVO(packingListItem.get("PackingListItem_Id"), packingListItem.get("MaterialType_No"), packingListItem.get("PackingListItem_Quantity"), 0, 0,
						packingListItem.get("PackingListItem_FinishTime"), type);
				MaterialType materialType = MaterialType.dao.findById(packingListItem.getInt("MaterialType_Id"));
				int uwStoreNum = 0;
				Integer whStoreNum = 0;
				if (materialType.getType().equals(WarehouseType.REGULAR.getId())) {
					uwStoreNum = materialService.countAndReturnRemainderQuantityByMaterialTypeId(materialType.getId());
				} else {
					uwStoreNum = materialService.countPreciousQuantityByMaterialTypeId(materialType.getId());
				}
				if (materialType.getType().equals(WarehouseType.REGULAR.getId()) && task.getType().equals(TaskType.OUT)) {
					if (task.getIsInventoryApply() != null && task.getIsInventoryApply()) {
						Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
						if (inventoryTask != null) {
							whStoreNum = externalWhLogService.getEWhMaterialQuantity(packingListItem.getInt("MaterialType_Id"), task.getDestination(), inventoryTask.getCreateTime());
						}
					} else {
						Task inventoryTask = inventoryTaskService.getOneUnStartInventoryTask(task.getSupplier(), task.getWarehouseType(), task.getDestination());
						if (inventoryTask != null) {
							whStoreNum = externalWhLogService.getEWhMaterialQuantity(packingListItem.getInt("MaterialType_Id"), task.getDestination())
									- externalWhLogService.getEWhMaterialQuantity(packingListItem.getInt("MaterialType_Id"), task.getDestination(), inventoryTask.getCreateTime());
						} else {
							whStoreNum = externalWhLogService.getEWhMaterialQuantity(packingListItem.getInt("MaterialType_Id"), task.getDestination());
						}
					}
				}
				io.setUwStoreNum(uwStoreNum);
				io.setWhStoreNum(whStoreNum);
				io.setStatus(uwStoreNum, whStoreNum, packingListItem.getInt("PackingListItem_Quantity"));
				io.setDetails(null);
				ioTaskDetailVOs.add(io);
			}
			// 分页，设置页码，每页显示条目等
			PagePaginate pagePaginate = new PagePaginate();
			pagePaginate.setPageSize(pageSize);
			pagePaginate.setPageNumber(pageNo);
			pagePaginate.setTotalRow(packingListItems.getTotalRow());
			pagePaginate.setList(ioTaskDetailVOs);

			return pagePaginate;
		}
		return null;
	}

	public void pass(Integer id) {
		Task task = Task.dao.findById(id);
		int state = task.getState();
		// 若该任务的状态不是“待审核”，则禁止通过审核
		if (state > TaskState.WAIT_REVIEW) {
			throw new OperationException("该任务已审核过或已作废！");
		} else {
			task.setState(TaskState.WAIT_START).update();
		}
	}

	// 查询所有任务
	public PagePaginate select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		if (filter == null) {
			filter = "task.type!=5#&#task.type!=6#&#task.type!=2#&#task.type!=7#&#supplier.enabled=1";
		} else {
			filter = filter + "#&#task.type!=5#&#task.type!=6#&#task.type!=2#&#task.type!=7#&#supplier.enabled=1";
		}
		Page<Record> result = selectService.select(new String[] { "task", "supplier" }, new String[] { "task.supplier=supplier.id" }, pageNo, pageSize, ascBy, descBy, filter);
		List<Window> windows = Window.dao.find(SQL.GET_WORKING_WINDOWS);
		Set<Integer> windowBindTaskSet = new HashSet<>();
		if (!windows.isEmpty()) {
			for (Window window : windows) {
				windowBindTaskSet.add(window.getBindTaskId());
			}
		}
		List<TaskVO> taskVOs = TaskVO.fillList(result.getList(), windowBindTaskSet);

		// 分页，设置页码，每页显示条目等
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(taskVOs);

		return pagePaginate;
	}

	// 查看任务详情
	public PagePaginate getIOTaskDetails(Integer id, Integer type, String no, Integer pageSize, Integer pageNo) {
		List<IOTaskDetailVO> ioTaskDetailVOs = new ArrayList<IOTaskDetailVO>();
		// 如果任务类型为出入库
		
		String filter = null;
		if (no != null && !no.trim().equals("")) {
			filter = "material_type.no like " + no;
		}
		if (type == TaskType.IN || type == TaskType.OUT || type == TaskType.SEND_BACK || type == TaskType.EMERGENCY_OUT) {
			// 先进行多表查询，查询出同一个任务id的套料单表的id,物料类型表的料号no,套料单表的计划出入库数量quantity,套料单表对应任务的实际完成时间finish_time
			Page<Record> packingListItems = selectService.select(new String[] { "packing_list_item", "material_type" },
					new String[] { "packing_list_item.task_id = " + id.toString(), "material_type.id = packing_list_item.material_type_id" }, pageNo, pageSize, null, null, filter);

			// 遍历同一个任务id的套料单数据
			for (Record packingListItem : packingListItems.getList()) {
				// 查询task_log中的material_id,quantity
				List<TaskLog> taskLog = TaskLog.dao.find(IOTaskSQL.GET_TASK_ITEM_DETAILS_SQL, Integer.parseInt(packingListItem.get("PackingListItem_Id").toString()));
				Integer actualQuantity = 0;
				// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
				for (TaskLog tl : taskLog) {
					actualQuantity += tl.getQuantity();
				}
				Integer deductQuantity = externalWhLogService.getDeductEwhMaterialQuantityByOutTask(packingListItem.getInt("PackingListItem_TaskId"), packingListItem.getInt("PackingListItem_MaterialTypeId"));
				IOTaskDetailVO io = new IOTaskDetailVO(packingListItem.get("PackingListItem_Id"), packingListItem.get("MaterialType_No"), packingListItem.get("PackingListItem_Quantity"),
						actualQuantity, deductQuantity, packingListItem.get("PackingListItem_FinishTime"), type);
				io.setDetails(taskLog);
				ioTaskDetailVOs.add(io);
			}

			// 分页，设置页码，每页显示条目等
			PagePaginate pagePaginate = new PagePaginate();
			pagePaginate.setPageSize(pageSize);
			pagePaginate.setPageNumber(pageNo);
			pagePaginate.setTotalRow(packingListItems.getTotalRow());
			pagePaginate.setList(ioTaskDetailVOs);

			return pagePaginate;
		}
		return null;
	}

	// 获取任务详情
	public List<IOTaskInfo> getIOTaskInfos(Integer taskId) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("任务不存在或者任务未处于进行中状态");
		}
		List<Record> taskInfoRecords = Db.find(IOTaskSQL.GET_PRECIOUS_IOTASK_INFOS, taskId, taskId);
		List<Record> uwStoreRecords = Db.find(IOTaskSQL.GET_PRECIOUS_IO_TASK_UW_STORE, taskId);
		List<Record> oldestMaterialRecords = Db.find(IOTaskSQL.GET_OLDEST_MATERIAL_UW_STORE, taskId);
		List<IOTaskInfo> infos = IOTaskInfo.fillList(task, taskInfoRecords, uwStoreRecords, oldestMaterialRecords);
		return infos;
	}

	public void exportIOTaskDetails(Integer id, Integer type, String fileName, OutputStream output) throws IOException {
		// 如果任务类型为出入库
		if (type == TaskType.IN || type == TaskType.OUT || type == TaskType.SEND_BACK || type == TaskType.EMERGENCY_OUT) {
			// 先进行多表查询，查询出同一个任务id的套料单表的id,物料类型表的料号no,套料单表的计划出入库数量quantity,套料单表对应任务的实际完成时间finish_time
			Page<Record> packingListItems = selectService.select(new String[] { "packing_list_item", "material_type" },
					new String[] { "packing_list_item.task_id = " + id.toString(), "material_type.id = packing_list_item.material_type_id" }, null, null, null, null, null);

			// 遍历同一个任务id的套料单数据
			for (Record packingListItem : packingListItems.getList()) {
				// 查询task_log中的material_id,quantity
				List<TaskLog> taskLog = TaskLog.dao.find(IOTaskSQL.GET_TASK_ITEM_DETAILS_SQL, Integer.parseInt(packingListItem.get("PackingListItem_Id").toString()));
				Integer actualQuantity = 0;
				// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
				for (TaskLog tl : taskLog) {
					actualQuantity += tl.getQuantity();
				}
				Integer deductQuantity = externalWhLogService.getDeductEwhMaterialQuantityByOutTask(packingListItem.getInt("PackingListItem_TaskId"), packingListItem.getInt("PackingListItem_MaterialTypeId"));
				packingListItem.set("PackingListItem_actuallyQuantity", actualQuantity);
				packingListItem.set("PackingListItem_deductQuantity", deductQuantity);
				Integer lackQuantity = (packingListItem.getInt("PackingListItem_Quantity") - actualQuantity + deductQuantity) > 0
						? (packingListItem.getInt("PackingListItem_Quantity") - actualQuantity + deductQuantity)
						: 0;
				packingListItem.set("PackingListItem_lackQuantity", lackQuantity);
			}
			String[] field = null;
			String[] head = null;
			field = new String[] { "MaterialType_No", "PackingListItem_Quantity", "PackingListItem_actuallyQuantity", "PackingListItem_deductQuantity", "PackingListItem_lackQuantity" };
			head = new String[] { "料号", "计划数", "UW实际数", "物料仓抵扣数", "欠料数" };
			ExcelWritter writter = ExcelWritter.create(true);
			writter.fill(packingListItems.getList(), fileName, field, head);
			writter.write(output, true);
		}
	}

	/**
	 * 获取进行中的出库任务
	 * 
	 * @return
	 */
	public List<Task> getCuttingTask() {
		List<Task> tasks = Task.dao.find(IOTaskSQL.GET_TASK_BY_TYPE, TaskType.OUT, TaskState.PROCESSING, WarehouseType.REGULAR.getId());
		Set<Integer> taskIdSet = new HashSet<>();
		if (!tasks.isEmpty()) {
			for (Task task : tasks) {
				taskIdSet.add(task.getId());
			}
		}

		List<Window> windows = Window.dao.find(WindowSQL.GET_WINDOWS_BY_TASK_TYPE_SQL, TaskType.OUT);
		if (!windows.isEmpty()) {
			for (Window window : windows) {
				if (taskIdSet.contains(window.getBindTaskId())) {
					continue;
				}
				Task task = Task.dao.findById(window.getBindTaskId());
				tasks.add(task);
			}
		}
		return tasks;
	}

	/**
	 * 获取截料中的物料信息
	 * 
	 * @return
	 */
	public List<Record> getCuttingMaterial(Integer taskId) {
		Map<Integer, Record> cutitingMaterialMap = new HashMap<>();
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.OUT)) {
			throw new OperationException("任务不存在或者该任务非出库任务");
		}
		SqlPara sqlPara = new SqlPara();
		sqlPara.setSql(IOTaskSQL.GET_CUTTING_MATERIAL_SQL);
		sqlPara.addPara(MaterialStatus.CUTTING);
		sqlPara.addPara(WarehouseType.REGULAR.getId());
		List<Record> list = Db.find(sqlPara);
		for (Record record : list) {
			if (cutitingMaterialMap.get(record.getInt("packingListItemId")) != null) {
				throw new OperationException("同个任务同种物料存在两盘截料中的物料，请检查！");
			}
			cutitingMaterialMap.put(record.getInt("packingListItemId"), record);
		}
		List<Record> records = new ArrayList<>(list.size());
		List<AGVIOTaskItem> agvioTaskItems = IOTaskItemRedisDAO.getIOTaskItems(taskId);
		if (!agvioTaskItems.isEmpty()) {
			for (AGVIOTaskItem item : agvioTaskItems) {
				if (item.getIsForceFinish() && cutitingMaterialMap.get(item.getId()) != null) {
					records.add(cutitingMaterialMap.get(item.getId()));
				}
			}
		}
		return records;
	}

	protected Material putInMaterialToDb(Material material, String materialId, Integer boxId, Integer quantity, Date productionTime, Date printTime, Integer materialTypeId, String cycle,
			String manufacturer, Integer materialStatus, Integer companyId) {
		if (material != null) {
			material.setBox(boxId);
			material.setRow(-1);
			material.setCol(-1);
			material.setRemainderQuantity(quantity);
			material.setCycle(cycle);
			material.setProductionTime(productionTime);
			material.setPrintTime(printTime);
			material.setStoreTime(getDateTime());
			material.setStatus(materialStatus);
			material.setIsInBox(true);
			material.setIsRepeated(false);
			material.setCompanyId(companyId);
			material.update();
		} else {
			material = new Material();
			material.setId(materialId);
			material.setType(materialTypeId);
			material.setBox(boxId);
			material.setRow(-1);
			material.setCol(-1);
			material.setRemainderQuantity(quantity);
			material.setCycle(cycle);
			material.setProductionTime(productionTime);
			material.setPrintTime(printTime);
			material.setStoreTime(getDateTime());
			material.setStatus(materialStatus);
			material.setCompanyId(companyId);
			if (manufacturer == null || manufacturer.equals("")) {
				material.setManufacturer("无");
			} else {
				material.setManufacturer(manufacturer);
			}
			material.setIsInBox(true);
			material.setIsRepeated(false);
			material.save();
		}
		return material;
	}

	public TaskLog createTaskLog(Integer packListItemId, String materialId, Integer quantity, User user, Task task) {
		TaskLog taskLog = new TaskLog();
		taskLog.setPackingListItemId(packListItemId);
		taskLog.setMaterialId(materialId);
		taskLog.setQuantity(quantity);
		if (user != null) {
			taskLog.setOperator(user.getUid());
		} else {
			taskLog.setOperator(null);
		}
		// 区分出库操作人工还是机器操作,目前的版本暂时先统一写成人工操作
		taskLog.setAuto(false);
		taskLog.setTime(new Date());
		taskLog.setDestination(task.getDestination());
		taskLog.save();
		return taskLog;
	}
	
	
	public TaskLog createAutoTaskLog(Integer packListItemId, String materialId, Integer quantity, String user, Task task) {
		TaskLog taskLog = new TaskLog();
		taskLog.setPackingListItemId(packListItemId);
		taskLog.setMaterialId(materialId);
		taskLog.setQuantity(quantity);
		taskLog.setOperator(user);
		taskLog.setAuto(true);
		taskLog.setTime(new Date());
		taskLog.setDestination(task.getDestination());
		taskLog.save();
		return taskLog;
	}

	// 设置优先级
	public boolean setPriority(Integer id, Integer priority) {
		Task task = Task.dao.findById(id);
		if (priority.equals(0)) {
			task.setPriority(priority);
		} else {
			task.setPriority(1);
		}

		return task.update();
	}

	/**
	 * 将入库时间换算成年月日
	 * 
	 * @return
	 * @throws ParseException
	 */
	public Date getDateTime() {
		Date date = new Date();
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = sFormat.format(date) + " 00:00:00";
		try {
			return sFormat.parse(dateString);
		} catch (ParseException e) {
			return date;
		}
	}

	public String getTaskName(Integer id) {
		Task task = Task.dao.findById(id);
		if (task != null) {
			return task.getFileName();
		}
		return null;
	}

	/**
	 * 获取任务仓口
	 * 
	 * @param taskId
	 */
	public List<Window> getTaskWindow(Integer taskId) {
		List<Window> windows = Window.dao.find(WindowSQL.GET_WINDOW_BY_TASK, taskId);
		return windows;
	}

	public String editTaskRemarks(Integer taskId, String remarks) {
		Task task = Task.dao.findById(taskId);
		if (task == null) {
			throw new OperationException("任务不存在！");
		}
		task.setRemarks(remarks);
		task.update();
		return "操作成功";
	}

	public void clearTaskItem(Integer taskId) {
		Db.update(DELETE_PACKING_LIST_ITEM_BY_TASK_ID_SQL, taskId);
		Task.dao.deleteById(taskId);
	}
}
