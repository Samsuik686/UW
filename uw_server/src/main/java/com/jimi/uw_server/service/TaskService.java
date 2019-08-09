package com.jimi.uw_server.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jfinal.aop.Aop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.InputHelper.send.MyInputHelper;
import com.jimi.uw_server.agv.dao.InputMaterialRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.agv.handle.IOTaskHandler;
import com.jimi.uw_server.constant.BoxState;
import com.jimi.uw_server.constant.InventoryTaskSQL;
import com.jimi.uw_server.constant.SQL;
import com.jimi.uw_server.constant.SampleTaskSQL;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.ErrorLog;
import com.jimi.uw_server.model.ExternalWhLog;
import com.jimi.uw_server.model.FormerSupplier;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.InventoryLog;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.SampleOutRecord;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.PackingListItemBO;
import com.jimi.uw_server.model.vo.IOTaskDetailVO;
import com.jimi.uw_server.model.vo.PackingListItemDetailsVO;
import com.jimi.uw_server.model.vo.TaskVO;
import com.jimi.uw_server.model.vo.WindowParkingListItemVO;
import com.jimi.uw_server.model.vo.WindowTaskItemsVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ExcelHelper;
import com.jimi.uw_server.util.ExcelWritter;
import com.jimi.uw_server.util.MaterialHelper;


/**
 * 任务业务层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class TaskService {

	private static SelectService selectService = Aop.get(SelectService.class);

	private static MaterialService materialService = Aop.get(MaterialService.class);

	private static ExternalWhLogService externalWhLogService = Aop.get(ExternalWhLogService.class);

	private static final Object CREATEIOTASK_LOCK = new Object();

	private static final Object START_LOCK = new Object();

	private static final Object PASS_LOCK = new Object();

	private static final Object CANCEL_LOCK = new Object();

	private static final Object IN_LOCK = new Object();

	private static final Object OUT_LOCK = new Object();

	private static final int UW_ID = 0;

	private static final Object UPDATEOUTQUANTITYANDMATERIALINFO_LOCK = new Object();

	private static final String GET_FILE_NAME_SQL = "SELECT * FROM task WHERE file_name = ? and state < 4";

	private static final String GET_MATERIAL_TYPE_BY_NO_SQL = "SELECT * FROM material_type WHERE no = ? AND supplier = ? AND enabled = 1";

	private static final String DELETE_PACKING_LIST_ITEM_BY_TASK_ID_SQL = "DELETE FROM packing_list_item WHERE task_id = ?";

	private static final String GET_TASK_ITEMS_SQL = "SELECT * FROM packing_list_item WHERE task_id = ?";

	private static final String GET_TASK_ITEM_DETAILS_SQL = "SELECT material_id AS materialId, quantity, production_time AS productionTime FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id";

	private static final String GET_MATERIAL_ID_IN_SAME_TASK_SQL = "SELECT * FROM task_log WHERE material_id = ? AND packing_list_item_id = ?";

	private static final String GET_MATERIAL_ID_BY_ID_SQL = "SELECT * FROM material WHERE id = ? AND is_in_box = 1";

	private static final String DELETE_TASK_LOG_SQL = "DELETE FROM task_log WHERE packing_list_item_id = ? AND material_id = ?";

	private static final String GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL = "SELECT * FROM task_log WHERE packing_list_item_id = ? AND material_id = ?";

	private static final String GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_SQL = "SELECT * FROM task_log WHERE packing_list_item_id = ? AND material_id IS NOT NULL";

	private static final String GET_MATERIAL_BY_BOX_ID = "SELECT * FROM material where box = ? and is_in_box = ? and remainder_quantity > 0";

	private static final String GET_MATERIAL_BY_BOX_SQL = "SELECT * FROM material WHERE box = ? AND remainder_quantity > 0";

	private static final String GET_COUNT_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 2) ORDER BY id ASC";

	private static final String GET_SAMPLE_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 7) ORDER BY id ASC";

	private static final String GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER = "SELECT * FROM task where state = 2 and type = 2 and supplier = ?";

	private static final String GET_TASKLOG_BY_PACKINGITEMID = "SELECT * FROM task_log WHERE packing_list_item_id = ? AND quantity > 0";

	private static final String GET_EWH_LOG_BY_TASKID_AND_MATERIALTYPEID = "SELECT * FROM external_wh_log WHERE external_wh_log.task_id = ? AND external_wh_log.material_type_id = ? AND external_wh_log.quantity < 0";

	private static final String GET_UNCOVER_INVENTORY_LOG_BY_TASKID = "SELECT * FROM inventory_log WHERE task_id = ? AND enabled = 1";

	private static final String GET_FREE_WINDOWS_SQL = "SELECT id FROM window WHERE `bind_task_id` IS NULL ORDER BY id ASC";

	private static final String GET_IN_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 0) ORDER BY id ASC";

	private static final String GET_OUT_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 1) ORDER BY id ASC";

	private static final String GET_RETURN_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 4) ORDER BY id ASC";

	private static final String GET_WINDOW_BY_TASK = "SELECT * FROM window WHERE bind_task_id = ? ORDER BY id ASC";

	private static final String GET_TASK_BY_TYPE_STATE_SUPPLIER = "select * from task where task.type = ? and task.state = ? and task.supplier = ?";

	private static final String GET_OLDER_MATERIAL_BY_BOX_AND_TIME = "select * from material where box = ? and type = ? and production_time < ? and remainder_quantity > 0 and is_in_box = 1 ORDER BY production_time asc";

	private static final String GET_OLDER_MATERIAL_BY_NO_BOX_AND_TIME = "select * from material where box != ? and type = ? and production_time < ? and remainder_quantity > 0 and is_in_box = 1 ORDER BY production_time asc";

	private static final String GET_FORMER_SUPPLIER_SQL = "SELECT * FROM former_supplier WHERE former_name = ? and supplier_id = ?";

	private IOTaskHandler ioTaskHandler = IOTaskHandler.getInstance();


	// 创建出入库/退料任务
	public String createIOTask(Integer type, String fileName, String fullFileName, Integer supplier, Integer destination, Boolean isInventoryApply, Integer inventoryTaskId, String remarks) throws Exception {
		String resultString = "添加成功！";
		File file = new File(fullFileName);

		// 如果文件格式不对，则提示检查文件格式
		if (!(fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
			// 清空upload目录下的文件
			deleteTempFileAndTaskRecords(file, null);
			resultString = "创建任务失败，请检查套料单的文件格式是否正确！";
			return resultString;
		}

		Map<Integer, Integer> taskDetailsMap = new LinkedHashMap<>();
		ExcelHelper fileReader = ExcelHelper.from(file);
		List<PackingListItemBO> items = fileReader.unfill(PackingListItemBO.class, 2);
		// 如果套料单表头不对或者表格中没有有效的任务记录
		if (items == null || items.size() == 0) {
			deleteTempFileAndTaskRecords(file, null);
			resultString = "创建任务失败，请检查套料单的表头是否正确以及套料单表格中是否有效的任务记录！";
			return resultString;
		} else {
			synchronized (CREATEIOTASK_LOCK) {
				// 如果已经用该套料单创建过任务，并且该任务没有被作废，则禁止再导入相同文件名的套料单
				if (Task.dao.find(GET_FILE_NAME_SQL, fileName).size() > 0) {
					// 清空upload目录下的文件
					deleteTempFileAndTaskRecords(file, null);
					resultString = "创建任务失败，已经用该套料单创建过任务，请先作废掉原来的套料单任务！或者修改原套料单文件名，如：套料单A-重新入库";
					return resultString;
				}

				// 从套料单电子表格第四行开始有任务记录
				int i = 4;
				// 读取excel表格的套料单数据，将数据一条条写入到套料单表
				for (PackingListItemBO item : items) {
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) { // 只读取有序号的行数据

						if (item.getNo() == null || item.getQuantity() == null || item.getNo().replaceAll(" ", "").equals("") || item.getQuantity().toString().replaceAll(" ", "").equals("")) {
							deleteTempFileAndTaskRecords(file, null);
							resultString = "创建任务失败，请检查套料单表格第" + i + "行的料号或需求数列是否填写了准确信息！";
							return resultString;
						}

						if (item.getQuantity() <= 0) {
							deleteTempFileAndTaskRecords(file, null);
							resultString = "创建任务失败，套料单表格第" + i + "行的需求数为" + item.getQuantity() + "，需求数必须大于0！";
							return resultString;
						}

						// 根据料号找到对应的物料类型
						MaterialType mType = MaterialType.dao.findFirst(GET_MATERIAL_TYPE_BY_NO_SQL, item.getNo(), supplier);
						// 判断物料类型表中是否存在对应的料号且未被禁用，若不存在，则将对应的任务记录删除掉，并提示操作员检查套料单、新增对应的物料类型
						if (mType == null) {
							deleteTempFileAndTaskRecords(file, null);
							resultString = "插入套料单失败，料号为" + item.getNo() + "的物料没有记录在物料类型表中或已被禁用，或者是供应商与料号对应不上！";
							return resultString;
						}
						if (taskDetailsMap.get(mType.getId()) == null) {
							taskDetailsMap.put(mType.getId(), item.getQuantity());
						} else {
							taskDetailsMap.put(mType.getId(), taskDetailsMap.get(mType.getId()) + item.getQuantity());
						}
						i++;
					} else if (i == 4) { // 若第四行就没有序号，则说明套料单表格没有一条任务记录
						deleteTempFileAndTaskRecords(file, null);
						resultString = "创建任务失败，套料单表格没有任何有效的物料信息记录！";
						return resultString;
					} else {
						break;
					}

				}

				// 创建一条新的任务记录
				Task task = new Task();
				task.setType(type);
				task.setFileName(fileName);
				task.setState(TaskState.WAIT_REVIEW);
				task.setCreateTime(new Date());
				task.setSupplier(supplier);
				task.setDestination(destination);
				task.setRemarks(remarks);
				if (isInventoryApply != null && isInventoryApply) {
					task.setIsInventoryApply(true);
					if (inventoryTaskId == null) {
						deleteTempFileAndTaskRecords(file, null);
						resultString = "创建任务失败，未选择盘点期间申补单所绑定的盘点任务！";
						return resultString;
					}
					task.setInventoryTaskId(inventoryTaskId);
				} else {
					task.setIsInventoryApply(false);
				}
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
				deleteTempFileAndTaskRecords(file, null);
			}
		}

		return resultString;
	}


	// 删除tomcat上的临时文件并删除对应的任务记录
	public void deleteTempFileAndTaskRecords(File file, Integer newTaskId) {
		// 清空upload目录下的文件
		if (file.exists()) {
			file.delete();
		}
		if (newTaskId != null) {
			Db.update(DELETE_PACKING_LIST_ITEM_BY_TASK_ID_SQL, newTaskId);
			Task.dao.deleteById(newTaskId);
		}
	}


	// 令指定任务通过审核
	public boolean pass(Integer id) {
		synchronized (PASS_LOCK) {
			Task task = Task.dao.findById(id);
			int state = task.getState();
			// 若该任务的状态不是“待审核”，则禁止通过审核
			if (state > TaskState.WAIT_REVIEW) {
				throw new OperationException("该任务已审核过或已作废！");
			} else {
				task.setState(TaskState.WAIT_START);
				return task.update();
			}
		}

	}


	// 令指定任务开始
	public boolean start(Integer id, Integer window) {
		synchronized (START_LOCK) {
			// 根据仓口id查找对应的仓口记录
			Window windowDao = Window.dao.findById(window);
			// 判断仓口是否被占用
			if (windowDao.getBindTaskId() != null) {
				throw new OperationException("该仓口已被占用，请选择其它仓口！");
			} else {
				Task task = Task.dao.findById(id);
				Task inventoryTask = Task.dao.findFirst(GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER, task.getSupplier());
				InventoryLog inventoryLog = null;
				if (inventoryTask != null) {
					inventoryLog = InventoryLog.dao.findFirst(GET_UNCOVER_INVENTORY_LOG_BY_TASKID, inventoryTask.getId());
				}
				if (inventoryTask != null && inventoryLog != null) {
					throw new OperationException("当前供应商存在进行中的UW仓盘点任务，请等待任务结束后再开始出入库任务!");
				}
				int state = task.getState();
				// 判断任务状态是否为“未开始”
				if (state != TaskState.WAIT_START) {
					throw new OperationException("该任务已开始过或已作废！");
				} else {
					// 设置仓口，并在仓口表绑定该任务id
					task.setWindow(window);
					windowDao.setBindTaskId(id);
					windowDao.update();
					// 根据套料单、物料类型表生成任务条目
					List<AGVIOTaskItem> taskItems = new ArrayList<AGVIOTaskItem>();
					List<PackingListItem> items = PackingListItem.dao.find(GET_TASK_ITEMS_SQL, id);
					// 如果任务类型为入库或退料入库，则将任务条目加载到redis中，将任务条目状态设置为不可分配
					if (task.getType() == TaskType.IN || task.getType() == TaskType.SEND_BACK) {
						for (PackingListItem item : items) {
							AGVIOTaskItem a = new AGVIOTaskItem(item, TaskItemState.WAIT_SCAN, 0);
							taskItems.add(a);
						}
					} else if (task.getType() == TaskType.OUT) { // 如果任务类型为出库，则将任务条目加载到redis中，将任务条目状态设置为未分配
						for (PackingListItem item : items) {
							AGVIOTaskItem a = new AGVIOTaskItem(item, TaskItemState.WAIT_ASSIGN, 0);
							taskItems.add(a);
						}
					}
					// 把任务条目均匀插入到队列til中
					TaskItemRedisDAO.addIOTaskItem(task.getId(), taskItems);
					// 将任务状态设置为进行中
					task.setState(TaskState.PROCESSING);
					return task.update();
				}

			}
		}
	}


	// 作废指定任务
	public boolean cancel(Integer id) {
		synchronized (CANCEL_LOCK) {
			Task task = Task.dao.findById(id);
			int state = task.getState();
			// 对于已完成的任务，禁止作废
			if (state == TaskState.FINISHED) {
				throw new OperationException("该任务已完成，禁止作废！");
			} else if (state == TaskState.CANCELED) { // 对于已作废过的任务，禁止作废
				throw new OperationException("该任务已作废！");
			} else {

				// 当前仓口存在未放入料盒的入库物料
				if ((task.getType().equals(TaskType.IN) || task.getType().equals(TaskType.SEND_BACK)) && !InputMaterialRedisDAO.getScanStatus(task.getWindow()).equals(-1)) {
					throw new OperationException("禁止作废，请先将之前的物料正确入库再进行作废任务操作");
				}
				boolean untiedWindowflag = true;
				// 判断任务是否处于进行中状态，若是，则把相关的任务条目从til中剔除;若存在已分配的任务条目，则不解绑任务仓口
				if (state == TaskState.PROCESSING) {
					TaskItemRedisDAO.removeUnAssignedTaskItemByTaskId(id);
					List<AGVIOTaskItem> redisTaskItems = TaskItemRedisDAO.getIOTaskItems(task.getId());
					if (redisTaskItems != null && redisTaskItems.size() > 0) {
						untiedWindowflag = false;
					}
				}
				// 如果任务状态为进行中且该组任务的全部任务条目都已从redis清空，则将任务绑定的仓口解绑
				if (state == TaskState.PROCESSING && untiedWindowflag) {
					Window window = Window.dao.findById(task.getWindow());
					window.setBindTaskId(null);
					window.update();
				}

				// 更新任务状态为作废
				task.setState(TaskState.CANCELED).update();
				ioTaskHandler.clearTask(task.getId());
				return true;
			}
		}

	}


	// 查看任务详情
	public Object getIOTaskDetail(Integer id, Integer type, Integer pageSize, Integer pageNo) {
		List<IOTaskDetailVO> ioTaskDetailVOs = new ArrayList<IOTaskDetailVO>();
		// 如果任务类型为出入库
		if (type == TaskType.IN || type == TaskType.OUT || type == TaskType.SEND_BACK) {
			// 先进行多表查询，查询出同一个任务id的套料单表的id,物料类型表的料号no,套料单表的计划出入库数量quantity,套料单表对应任务的实际完成时间finish_time
			Page<Record> packingListItems = selectService.select(new String[] {"packing_list_item", "material_type"}, new String[] {"packing_list_item.task_id = " + id.toString(), "material_type.id = packing_list_item.material_type_id"}, pageNo, pageSize, null, null, null);

			// 遍历同一个任务id的套料单数据
			for (Record packingListItem : packingListItems.getList()) {
				// 查询task_log中的material_id,quantity
				List<TaskLog> taskLog = TaskLog.dao.find(GET_TASK_ITEM_DETAILS_SQL, Integer.parseInt(packingListItem.get("PackingListItem_Id").toString()));
				Integer actualQuantity = 0;
				// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
				for (TaskLog tl : taskLog) {
					actualQuantity += tl.getQuantity();
				}
				Integer deductQuantity = 0;
				ExternalWhLog externalWhLog = ExternalWhLog.dao.findFirst(GET_EWH_LOG_BY_TASKID_AND_MATERIALTYPEID, id, packingListItem.getInt("PackingListItem_MaterialTypeId"));
				if (externalWhLog != null) {
					deductQuantity = externalWhLog.getQuantity();
				}
				IOTaskDetailVO io = new IOTaskDetailVO(packingListItem.get("PackingListItem_Id"), packingListItem.get("MaterialType_No"), packingListItem.get("PackingListItem_Quantity"), actualQuantity, deductQuantity, packingListItem.get("PackingListItem_FinishTime"), type);
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

		else if (type == TaskType.COUNT) { // 如果任务类型为盘点
			return null;
		}

		else if (type == TaskType.POSITION_OPTIZATION) { // 如果任务类型为位置优化
			return null;
		}

		return null;
	}


	public void exportUnfinishTaskDetails(Integer id, Integer type, String fileName, OutputStream output) throws IOException {
		// 如果任务类型为出入库
		if (type == TaskType.IN || type == TaskType.OUT || type == TaskType.SEND_BACK) {
			// 先进行多表查询，查询出同一个任务id的套料单表的id,物料类型表的料号no,套料单表的计划出入库数量quantity,套料单表对应任务的实际完成时间finish_time
			Page<Record> packingListItems = selectService.select(new String[] {"packing_list_item", "material_type"}, new String[] {"packing_list_item.task_id = " + id.toString(), "material_type.id = packing_list_item.material_type_id", "packing_list_item.finish_time IS NULL"}, null, null, null, null, null);

			// 遍历同一个任务id的套料单数据
			for (Record packingListItem : packingListItems.getList()) {
				// 查询task_log中的material_id,quantity
				List<TaskLog> taskLog = TaskLog.dao.find(GET_TASK_ITEM_DETAILS_SQL, Integer.parseInt(packingListItem.get("PackingListItem_Id").toString()));
				Integer actualQuantity = 0;
				// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
				for (TaskLog tl : taskLog) {
					actualQuantity += tl.getQuantity();
				}
				Integer deductQuantity = 0;
				ExternalWhLog externalWhLog = ExternalWhLog.dao.findFirst(GET_EWH_LOG_BY_TASKID_AND_MATERIALTYPEID, id, packingListItem.getInt("PackingListItem_MaterialTypeId"));
				if (externalWhLog != null) {
					deductQuantity = externalWhLog.getQuantity();
				}
				packingListItem.set("PackingListItem_actuallyQuantity", actualQuantity);
				packingListItem.set("PackingListItem_deductQuantity", deductQuantity);
				Integer lackQuantity = (packingListItem.getInt("PackingListItem_Quantity") - actualQuantity + deductQuantity) > 0 ? (packingListItem.getInt("PackingListItem_Quantity") - actualQuantity + deductQuantity) : 0;
				packingListItem.set("PackingListItem_lackQuantity", lackQuantity);
			}
			String[] field = null;
			String[] head = null;
			field = new String[] {"MaterialType_No", "PackingListItem_Quantity", "PackingListItem_actuallyQuantity", "PackingListItem_deductQuantity", "PackingListItem_lackQuantity"};
			head = new String[] {"料号", "计划数", "UW实际数", "物料仓抵扣数", "欠料数"};
			ExcelWritter writter = ExcelWritter.create(true);
			writter.fill(packingListItems.getList(), fileName, field, head);
			writter.write(output, true);
		}
	}


	public Object check(Integer id, Integer type, Integer pageSize, Integer pageNo) {
		List<IOTaskDetailVO> ioTaskDetailVOs = new ArrayList<IOTaskDetailVO>();
		// 如果任务类型为出入库
		if (type == TaskType.IN || type == TaskType.OUT || type == TaskType.SEND_BACK) {
			// 先进行多表查询，查询出同一个任务id的套料单表的id,物料类型表的料号no,套料单表的计划出入库数量quantity,套料单表对应任务的实际完成时间finish_time
			Page<Record> packingListItems = selectService.select(new String[] {"packing_list_item", "material_type"}, new String[] {"packing_list_item.task_id = " + id.toString(), "material_type.id = packing_list_item.material_type_id"}, pageNo, pageSize, null, null, null);

			// 遍历同一个任务id的套料单数据
			for (Record packingListItem : packingListItems.getList()) {
				// 查询task_log中的material_id,quantity
				List<TaskLog> taskLog = TaskLog.dao.find(GET_TASK_ITEM_DETAILS_SQL, Integer.parseInt(packingListItem.get("PackingListItem_Id").toString()));
				Integer actualQuantity = 0;
				// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
				for (TaskLog tl : taskLog) {
					actualQuantity += tl.getQuantity();
				}
				Task task = Task.dao.findById(packingListItem.getInt("PackingListItem_TaskId"));
				IOTaskDetailVO io = new IOTaskDetailVO(packingListItem.get("PackingListItem_Id"), packingListItem.get("MaterialType_No"), packingListItem.get("PackingListItem_Quantity"), actualQuantity, 0, packingListItem.get("PackingListItem_FinishTime"), type);
				int uwStoreNum = materialService.countAndReturnRemainderQuantityByMaterialTypeId(packingListItem.getInt("MaterialType_Id"));
				Integer whStoreNum = 0;
				if (task.getIsInventoryApply() != null && task.getIsInventoryApply()) {
					Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
					if (inventoryTask != null) {
						whStoreNum = externalWhLogService.getEWhMaterialQuantity(packingListItem.getInt("MaterialType_Id"), task.getDestination(), inventoryTask.getCreateTime());
					}
				} else {
					Task inventoryTask = Task.dao.findFirst(GET_TASK_BY_TYPE_STATE_SUPPLIER, TaskType.COUNT, TaskState.WAIT_START, task.getSupplier());
					if (inventoryTask != null) {
						whStoreNum = externalWhLogService.getEWhMaterialQuantity(packingListItem.getInt("MaterialType_Id"), task.getDestination()) - externalWhLogService.getEWhMaterialQuantity(packingListItem.getInt("MaterialType_Id"), task.getDestination(), inventoryTask.getCreateTime());
					} else {
						whStoreNum = externalWhLogService.getEWhMaterialQuantity(packingListItem.getInt("MaterialType_Id"), task.getDestination());
					}
				}
				io.setUwStoreNum(uwStoreNum);
				io.setWhStoreNum(whStoreNum);
				io.setStatus(uwStoreNum, whStoreNum, packingListItem.getInt("PackingListItem_Quantity"));
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

		else if (type == TaskType.COUNT) { // 如果任务类型为盘点
			return null;
		}

		else if (type == TaskType.POSITION_OPTIZATION) { // 如果任务类型为位置优化
			return null;
		}

		return null;
	}


	// 查询指定类型的仓口
	public List<Window> getWindows(int type) {
		List<Window> windowIds = null;
		switch (type) {
		case 0:
			windowIds = Window.dao.find(GET_FREE_WINDOWS_SQL);
			break;
		case 1:
			windowIds = Window.dao.find(GET_IN_TASK_WINDOWS_SQL);
			break;
		case 2:
			windowIds = Window.dao.find(GET_OUT_TASK_WINDOWS_SQL);
			break;
		case 3:
			windowIds = Window.dao.find(GET_RETURN_TASK_WINDOWS_SQL);
			break;
		case 4:
			windowIds = Window.dao.find(GET_COUNT_TASK_WINDOWS_SQL);
			break;
		case 5:
			windowIds = Window.dao.find(GET_SAMPLE_TASK_WINDOWS_SQL);
			break;
		default:
			break;
		}
		return windowIds;
	}


	// 查询所有任务
	public Object select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		if (filter == null) {
			filter = "task.type!=5#&#task.type!=6#&#task.type!=2#&#task.type!=7";
		} else {
			filter = filter + "#&#task.type!=5#&#task.type!=6#&#task.type!=2#&#task.type!=7";
		}
		Page<Record> result = selectService.select("task", pageNo, pageSize, ascBy, descBy, filter);
		List<TaskVO> taskVOs = new ArrayList<TaskVO>();
		Boolean status;
		for (Record res : result.getList()) {
			status = false;
			if (res.getInt("state").equals(TaskState.PROCESSING)) {
				status = TaskItemRedisDAO.getTaskStatus(res.getInt("id"));
			}
			TaskVO t = new TaskVO(res.get("id"), res.get("state"), res.get("type"), res.get("file_name"), res.get("create_time"), res.get("priority"), res.get("supplier"), res.get("remarks"), status);
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


	// 完成任务
	public void finish(Integer taskId, Boolean isUwLack) {
		Task task = Task.dao.findById(taskId);

		if (isUwLack) {
			for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(taskId)) {
				if (item.getState() == TaskItemState.LACK) {
					PackingListItem packingListItem = PackingListItem.dao.findById(item.getId());
					if (packingListItem.getFinishTime() == null) {
						packingListItem.setFinishTime(new Date());
						packingListItem.update();
						Integer acturallyNum = 0;
						String operatior = "";
						List<TaskLog> taskLogs = TaskLog.dao.find(GET_TASKLOG_BY_PACKINGITEMID, packingListItem.getId());
						for (TaskLog taskLog : taskLogs) {
							acturallyNum = acturallyNum + taskLog.getQuantity();
							if (operatior.equals("")) {
								operatior = taskLog.getOperator();
							}
							Material material = Material.dao.findById(taskLog.getMaterialId());
							if (material.getRemainderQuantity() > 0) {
								int remainderQuantity = material.getRemainderQuantity() - taskLog.getQuantity();
								// 若该料盘没有库存了，则将物料实体表记录置为无效
								if (remainderQuantity <= 0) {
									material.setRow(-1);
									material.setCol(-1);
									material.setRemainderQuantity(0);
									material.setIsInBox(false);
									material.update();
								}
							}
						}
						if (!packingListItem.getQuantity().equals(acturallyNum)) {

							if (packingListItem.getQuantity() < acturallyNum) {
								// 超发
								ExternalWhLog externalWhLog = new ExternalWhLog();
								externalWhLog.setMaterialTypeId(packingListItem.getMaterialTypeId());
								externalWhLog.setDestination(task.getDestination());
								externalWhLog.setSourceWh(UW_ID);
								externalWhLog.setTaskId(task.getId());
								externalWhLog.setQuantity(acturallyNum - packingListItem.getQuantity());
								if (task.getIsInventoryApply()) {
									Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
									externalWhLog.setTime(inventoryTask.getCreateTime());
								} else {
									externalWhLog.setTime(new Date());
								}
								externalWhLog.setOperatior(operatior);
								externalWhLog.save();
							} else if (packingListItem.getQuantity() > acturallyNum) {
								// 欠发
								if (externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination()) > 0) {
									ExternalWhLog externalWhLog = new ExternalWhLog();
									externalWhLog.setMaterialTypeId(packingListItem.getMaterialTypeId());
									externalWhLog.setDestination(task.getDestination());
									externalWhLog.setSourceWh(UW_ID);
									externalWhLog.setTaskId(task.getId());
									int lackNum = packingListItem.getQuantity() - acturallyNum;
									if (task.getIsInventoryApply()) {
										Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
										externalWhLog.setTime(inventoryTask.getCreateTime());
										int storeNum = externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination(), inventoryTask.getCreateTime());
										if (storeNum > lackNum) {
											externalWhLog.setQuantity(0 - lackNum);
										} else {
											externalWhLog.setQuantity(0 - storeNum);
										}
									} else {
										int storeNum = 0;
										List<Task> inventoryTasks = InventoryTaskService.me.getUnStartInventoryTask(task.getSupplier());
										if (inventoryTasks.size() > 0) {
											storeNum = externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination()) - externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination(), inventoryTasks.get(0).getCreateTime());
										} else {
											storeNum = externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination());
										}
										if (storeNum > lackNum) {
											externalWhLog.setQuantity(0 - lackNum);
										} else if (storeNum <= 0) {
											externalWhLog.setQuantity(0);
										} else {
											externalWhLog.setQuantity(0 - storeNum);
										}

										externalWhLog.setTime(new Date());
									}
									externalWhLog.setOperatior(operatior);
									externalWhLog.save();
								}
							}
						}
					}
				}

			}
		}
		Boolean isLack = false;

		List<PackingListItem> packingListItems = PackingListItem.dao.find(SQL.GET_ALL_TASK_ITEM_BY_TASK_ID, task.getId());
		for (PackingListItem packingListItem : packingListItems) {
			TaskLog taskLog = TaskLog.dao.findFirst(SQL.GET_OUT_QUANTITY_BY_PACKINGITEMID, packingListItem.getId());
			Integer uwQuantity = taskLog.getInt("totalQuantity");
			if (uwQuantity == null) {
				uwQuantity = 0;
			}
			Integer deductQuantity = 0;
			ExternalWhLog externalWhLog = ExternalWhLog.dao.findFirst(GET_EWH_LOG_BY_TASKID_AND_MATERIALTYPEID, task.getId(), packingListItem.getMaterialTypeId());
			if (externalWhLog != null) {
				deductQuantity = externalWhLog.getQuantity();
			}
			if (packingListItem.getQuantity() > (uwQuantity - deductQuantity)) {
				isLack = true;
				break;
			}
		}
		if (task.getState() == TaskState.PROCESSING && !isLack) {
			task.setState(TaskState.FINISHED);
			task.update();
		} else if (task.getState() == TaskState.PROCESSING && isLack) {
			task.setState(TaskState.EXIST_LACK);
			task.update();
		}
		// 将仓口解绑(作废任务时，如果还有任务条目没跑完就不会解绑仓口，因此不管任务状态是为进行中还是作废，这里都需要解绑仓口)
		List<Window> windows = Window.dao.find(GET_WINDOW_BY_TASK, taskId);
		synchronized (Lock.WINDOW_LOCK) {
			for (Window window : windows) {
				List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, window.getId());
				for (GoodsLocation goodsLocation : goodsLocations) {
					TaskItemRedisDAO.delLocationStatus(window.getId(), goodsLocation.getId());
				}
				window.setBindTaskId(null).update();
			}
		}
	}


	// 获取指定仓口任务条目
	public Object getWindowTaskItems(Integer id, Integer pageNo, Integer pageSize) {
		if (id != null) {
			Window window = Window.dao.findById(id);
			// 若任务执行完毕，仓口被解绑，如果操作员还未手动刷新页面，则前端会继续传递仓口id调用该接口，此时会报500，加上这个判断，可避免这个错误
			if (window.getBindTaskId() == null) {
				return null;
			}
			Task task = Task.dao.findById(window.getBindTaskId());
			Map<Integer, GoodsLocation> map = new HashMap<>();
			List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, id);
			for (GoodsLocation goodsLocation : goodsLocations) {
				map.put(goodsLocation.getId(), goodsLocation);
			}
			// 先进行多表查询，查询出仓口id绑定的正在执行中的任务的套料单表的id，套料单文件名，物料类型表的料号no，套料单表的计划出入库数量quantity
			Page<Record> windowTaskItems = selectService.select(new String[] {"packing_list_item", "material_type",}, new String[] {"packing_list_item.task_id = " + window.getBindTaskId(), "material_type.id = packing_list_item.material_type_id"}, null, null, null, null, null);
			List<WindowTaskItemsVO> windowTaskItemVOs = new ArrayList<WindowTaskItemsVO>();
			int totalRow = 0;
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(window.getBindTaskId())) {
				// 查询task_log中的material_id,quantity
				List<TaskLog> taskLogs = TaskLog.dao.find(GET_TASK_ITEM_DETAILS_SQL, redisTaskItem.getId());
				GoodsLocation goodsLocation = map.get(redisTaskItem.getGoodsLocationId());
				if (goodsLocation == null) {
					goodsLocation = new GoodsLocation();
					goodsLocation.setId(0);
					goodsLocation.setName("无");
				}
				totalRow += 1;
				for (Record windowTaskItem : windowTaskItems.getList()) {
					Integer actualQuantity = 0;
					// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
					for (TaskLog tl : taskLogs) {
						actualQuantity += tl.getQuantity();
					}
					if (windowTaskItem.get("PackingListItem_Id").equals(redisTaskItem.getId())) {

						WindowTaskItemsVO wt = new WindowTaskItemsVO(windowTaskItem.getInt("PackingListItem_Id"), task.getFileName(), task.getType(), windowTaskItem.getStr("MaterialType_No"), windowTaskItem.getInt("PackingListItem_Quantity"), actualQuantity, windowTaskItem.getDate("PackingListItem_FinishTime"), redisTaskItem.getState(), redisTaskItem.getBoxId(), goodsLocation.getId(), goodsLocation.getName(), redisTaskItem.getRobotId());
						wt.setDetails(taskLogs);
						windowTaskItemVOs.add(wt);
					}
				}
			}
			List<WindowTaskItemsVO> windowTaskItemSubVOs = new ArrayList<WindowTaskItemsVO>();
			int startIndex = (pageNo - 1) * pageSize;
			int endIndex = (pageNo - 1) * pageSize + pageSize;
			// 不用 endIndex 作为数组结尾是为了避免数组越界
			for (int i = startIndex; i < windowTaskItemVOs.size(); i++) {
				windowTaskItemSubVOs.add(windowTaskItemVOs.get(i));
				if (i == endIndex - 1) {
					break;
				}
			}
			// 分页，设置页码，每页显示条目等
			PagePaginate pagePaginate = new PagePaginate();
			pagePaginate.setPageSize(pageSize);
			pagePaginate.setPageNumber(pageNo);
			pagePaginate.setTotalRow(totalRow);
			pagePaginate.setList(windowTaskItemSubVOs);
			return pagePaginate;
		} else {
			return null;
		}
	}


	// 获取指定仓口停泊条目
	public Object getWindowParkingItem(Integer id) {
		Window window = Window.dao.findById(id);
		if (window == null || window.getBindTaskId() == null) {
			throw new OperationException("仓口不存在任务");
		}
		Map<Integer, WindowParkingListItemVO> map = new LinkedHashMap<>();
		List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, id);
		for (GoodsLocation goodsLocation : goodsLocations) {
			map.put(goodsLocation.getId(), new WindowParkingListItemVO(goodsLocation));
		}
		Task task = Task.dao.findById(window.getBindTaskId());
		for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(window.getBindTaskId())) {
			if (redisTaskItem.getState().intValue() == TaskItemState.ARRIVED_WINDOW && redisTaskItem.getWindowId().equals(id)) {

				WindowParkingListItemVO windowParkingListItemVO = map.get(redisTaskItem.getGoodsLocationId());
				if (windowParkingListItemVO == null) {
					throw new OperationException("仓口 " + id + "没有对应货位" + redisTaskItem.getGoodsLocationId());
				}
				if (windowParkingListItemVO.getId() != null) {
					throw new OperationException("仓口 " + id + "的货位" + redisTaskItem.getGoodsLocationId() + "有一个以上的到站任务条目，请检查");
				}
				Integer packingListItemId = redisTaskItem.getId();
				// 查询task_log中的material_id,quantity
				List<TaskLog> taskLogs = TaskLog.dao.find(SQL.GET_PACKING_LIST_ITEM_DETAILS_SQL, redisTaskItem.getId());
				// 先进行多表查询，查询出仓口id绑定的正在执行中的任务的套料单表的id,套料单文件名，物料类型表的料号no,套料单表的计划出入库数量quantity
				Page<Record> windowParkingListItems = selectService.select(new String[] {"packing_list_item", "material_type", "supplier", "task"}, new String[] {"packing_list_item.id = " + packingListItemId, "material_type.id = packing_list_item.material_type_id", "material_type.supplier = supplier.id", "packing_list_item.task_id = task.id"}, null, null, null, null, null);
				if (!windowParkingListItems.getList().isEmpty() && windowParkingListItems.getList().size() == 1) {
					Record windowParkingListItem = windowParkingListItems.getList().get(0);
					Integer actualQuantity = 0;
					List<PackingListItemDetailsVO> packingListItemDetailsVOs = new ArrayList<PackingListItemDetailsVO>();
					for (TaskLog tl : taskLogs) {
						// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
						actualQuantity += tl.getQuantity();
						// 封装任务条目详情数据
						PackingListItemDetailsVO pl = new PackingListItemDetailsVO(tl.getId(), tl.get("materialId"), tl.getQuantity(), tl.get("remainderQuantity"), tl.get("productionTime"), tl.get("isInBox"), tl.getInt("row"), tl.getInt("col"), tl.getInt("boxId"));
						packingListItemDetailsVOs.add(pl);
					}
					Integer eWhStoreQuantity = 0;
					if (task.getIsInventoryApply() != null && task.getIsInventoryApply()) {
						Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
						eWhStoreQuantity = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination(), inventoryTask.getCreateTime());
					} else {
						Task inventoryTask = Task.dao.findFirst(GET_TASK_BY_TYPE_STATE_SUPPLIER, TaskType.COUNT, TaskState.WAIT_START, task.getSupplier());
						if (inventoryTask != null) {
							eWhStoreQuantity = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination()) - externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination(), inventoryTask.getCreateTime());
						} else {
							eWhStoreQuantity = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination());
						}
					}
					List<Material> materials1 = Material.dao.find(SQL.GET_MATERIAL_BY_BOX, redisTaskItem.getMaterialTypeId(), redisTaskItem.getBoxId());
					Integer reelNum = materials1.size();
					List<Material> materials2 = null;
					if (reelNum > 0 && task.getType().equals(TaskType.OUT)) {
						Material materialTemp2 = Material.dao.findFirst(GET_OLDER_MATERIAL_BY_NO_BOX_AND_TIME, redisTaskItem.getBoxId(), redisTaskItem.getMaterialTypeId(), materials1.get(0).getProductionTime());
						if (materialTemp2 == null) {
							materials2 = Material.dao.find(SQL.GET_MATERIALS_BY_TIME_AND_BOX, redisTaskItem.getMaterialTypeId(), redisTaskItem.getBoxId(), materials1.get(0).getProductionTime());
						}
					}
					Integer uwStoreQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(redisTaskItem.getMaterialTypeId());
					windowParkingListItemVO.fill(packingListItemDetailsVOs, windowParkingListItem, eWhStoreQuantity, uwStoreQuantity, actualQuantity, reelNum, redisTaskItem.getIsForceFinish(), redisTaskItem.getBoxId(), materials2);

				}
			}

		}
		if (map.isEmpty()) {
			return null;
		}

		return new ArrayList<>(map.values());
	}


	// 新增入库料盘记录并写入库任务日志记录
	public Material in(Integer packListItemId, String materialId, Integer quantity, Date productionTime, String supplierName, User user) {
		synchronized (IN_LOCK) {
			// 通过任务条目id获取套料单记录
			PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
			// 通过套料单记录获取物料类型id
			MaterialType materialType = MaterialType.dao.findById(packingListItem.getMaterialTypeId());
			// 通过物料类型获取对应的供应商id
			Integer supplierId = materialType.getSupplier();
			// 通过供应商id获取供应商名
			String sName = Supplier.dao.findById(supplierId).getName();
			int materialBoxCapacity = PropKit.use("properties.ini").getInt("materialBoxCapacity");
			FormerSupplier formerSupplier = FormerSupplier.dao.findFirst(GET_FORMER_SUPPLIER_SQL, supplierName, materialType.getSupplier());
			if (!supplierName.equals(sName) && formerSupplier == null) {
				throw new OperationException("扫码错误，供应商 " + supplierName + " 对应的任务目前没有在本仓口进行任务，" + "本仓口已绑定 " + sName + " 的任务单！");
			}
			if (TaskLog.dao.find(GET_MATERIAL_ID_IN_SAME_TASK_SQL, materialId, packListItemId).size() != 0) {
				throw new OperationException("时间戳为" + materialId + "的料盘已在同一个任务中被扫描过，请勿在同一个入库任务中重复扫描同一个料盘！");
			}
			Material material = Material.dao.findById(materialId);
			if (material != null && !material.getIsRepeated()) {
				throw new OperationException("时间戳为" + materialId + "的料盘已入过库，请勿重复入库！");
			}
			// 新增物料表记录
			int boxId = 0;
			int windowId = 0;
			for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(packingListItem.getTaskId())) {
				if (item.getId().intValue() == packListItemId) {
					boxId = item.getBoxId().intValue();
					windowId = item.getWindowId();
				}
			}

			// 当前仓口存在未放入料盒的入库物料
			if (!InputMaterialRedisDAO.getScanStatus(windowId).equals(-1)) {
				throw new OperationException("扫码错误，请先将之前的物料正确入库再进行扫码操作");
			}

			Task task = Task.dao.findById(packingListItem.getTaskId());
			Integer reelNum = 0;
			if (materialType.getRadius().equals(7)) {
				List<Material> materials = Material.dao.find(GET_MATERIAL_BY_BOX_SQL, boxId);
				reelNum = materials.size();
				if (reelNum == materialBoxCapacity) {
					throw new OperationException("当前料盒已满，请停止扫码！");
				}
			}
			if (material != null) {
				material.setBox(boxId);
				material.setRow(-1);
				material.setCol(-1);
				material.setRemainderQuantity(quantity);
				material.setProductionTime(productionTime);
				try {
					material.setStoreTime(getDateTime());
				} catch (ParseException e) {
					e.printStackTrace();
					throw new OperationException("入库日期转化失败,入库失败！");
				}
				material.setIsInBox(true);
				material.update();
			} else {
				material = new Material();
				material.setId(materialId);
				material.setType(packingListItem.getMaterialTypeId());
				material.setBox(boxId);
				material.setRow(-1);
				material.setCol(-1);
				material.setRemainderQuantity(quantity);
				material.setProductionTime(productionTime);
				try {
					material.setStoreTime(getDateTime());
				} catch (ParseException e) {
					e.printStackTrace();
					throw new OperationException("入库日期转化失败,入库失败！");
				}
				material.setIsInBox(true);
				material.save();
			}
			if (materialType.getRadius().equals(7)) {

				if (reelNum > 0) {
					MaterialHelper.getMaterialLocation(material, false);
				} else {
					Material material2 = Material.dao.findFirst(SQL.GET_MATERIAL_WITH_LOCATION_BY_SUPPLIER, supplierId);
					Material material3 = Material.dao.findFirst(SQL.GET_MATERIAL_BY_SUPPLIER, supplierId, material.getId());
					if (material2 != null || material3 == null) {
						MaterialHelper.getMaterialLocation(material, true);
					} else {
						MaterialHelper.getMaterialLocation(material, false);
					}
				}
			}

			// 写入一条入库任务日志
			TaskLog taskLog = new TaskLog();
			taskLog.setPackingListItemId(packListItemId);
			taskLog.setMaterialId(materialId);
			taskLog.setQuantity(quantity);
			taskLog.setOperator(user.getUid());
			// 区分入库操作人工还是机器操作,目前的版本暂时先统一写成人工操作
			taskLog.setAuto(false);
			taskLog.setTime(new Date());
			taskLog.setDestination(task.getDestination());
			taskLog.save();
			if (!material.getCol().equals(-1) && !material.getRow().equals(-1)) {
				int positionNo = material.getCol() * 20 + material.getRow();
				String result = MyInputHelper.getInstance().switchLight(windowId, positionNo, true);
				if (result.contains("succeed")) {
					InputMaterialRedisDAO.setScanStatus(windowId, positionNo);
				} else {
					taskLog.delete();
					if (material.getIsRepeated() != null && material.getIsRepeated() ) {
						material.setRemainderQuantity(0);
						material.setCol(-1);
						material.setRow(-1);
						material.setIsInBox(false);
						material.setIsRepeated(true);
						material.update();
					} else {
						material.delete();
					}
					throw new OperationException("入库失败，开启投料辅助器LED灯失败|Error|LED OPERN FAILED|" + result);
				}
			}
			material.setIsRepeated(false).update();
			return material;
		}
	}


	// 写出库任务日志
	public boolean out(Integer packListItemId, String materialId, Integer quantity, String supplierName, User user) {
		synchronized (OUT_LOCK) {
			// 通过任务条目id获取套料单记录
			PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
			// 通过套料单记录获取物料类型id
			MaterialType materialType = MaterialType.dao.findById(packingListItem.getMaterialTypeId());
			// 通过物料类型获取对应的供应商id
			Integer supplierId = materialType.getSupplier();
			// 通过供应商id获取供应商名
			String sName = Supplier.dao.findById(supplierId).getName();
			FormerSupplier formerSupplier = FormerSupplier.dao.findFirst(GET_FORMER_SUPPLIER_SQL, supplierName, materialType.getSupplier());
			if (!supplierName.equals(sName) && formerSupplier == null) {
				throw new OperationException("扫码错误，供应商 " + supplierName + " 对应的任务目前没有在本仓口进行任务，" + "本仓口已绑定 " + sName + " 的任务单！");
			}

			// 若扫描的料盘记录不存在于数据库中或不在盒内，则抛出OperationException
			if (Material.dao.find(GET_MATERIAL_ID_BY_ID_SQL, materialId).size() == 0) {
				throw new OperationException("时间戳为" + materialId + "的料盘没有入过库或者不在盒内，不能对其进行出库操作！");
			}
			// 对于不在已到站料盒的物料，禁止对其进行操作
			Material material = Material.dao.findById(materialId);
			if (!packingListItem.getMaterialTypeId().equals(material.getType())) {
				throw new OperationException("时间戳为" + materialId + "的料盘并非当前出库料号，不能对其进行出库操作！");
			}
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(packingListItem.getTaskId())) {
				if (redisTaskItem.getId().intValue() == packListItemId.intValue()) {
					if (redisTaskItem.getBoxId().intValue() != material.getBox().intValue()) {
						throw new OperationException("时间戳为" + materialId + "的料盘不在该料盒中，无法对其进行出库操作！");
					}
				}
			}
			Material materialTemp1 = Material.dao.findFirst(GET_OLDER_MATERIAL_BY_BOX_AND_TIME, material.getBox(), material.getType(), material.getProductionTime());
			if (materialTemp1 != null) {
				Material materialTemp2 = Material.dao.findFirst(GET_OLDER_MATERIAL_BY_NO_BOX_AND_TIME, material.getBox(), material.getType(), materialTemp1.getProductionTime());
				if (materialTemp2 != null) {
					throw new OperationException("最旧料盘不在当前料盒，请点击'稍后再见'取其他料盒！");
				}
			} else {
				Material materialTemp2 = Material.dao.findFirst(GET_OLDER_MATERIAL_BY_NO_BOX_AND_TIME, material.getBox(), material.getType(), material.getProductionTime());
				if (materialTemp2 != null) {
					throw new OperationException("最旧料盘不在当前料盒，请点击'稍后再见'取其他料盒！");
				}
			}
			if (materialTemp1 != null) {
				throw new OperationException("当前料盒存在更旧的物料，请选择其他料盘！");
			}
			// 若在同一个出库任务中重复扫同一个料盘时间戳，则抛出OperationException
			if (TaskLog.dao.find(GET_MATERIAL_ID_IN_SAME_TASK_SQL, materialId, packListItemId).size() != 0) {
				throw new OperationException("时间戳为" + materialId + "的料盘已在同一个任务中被扫描过，请勿在同一个出库任务中重复扫描同一个料盘！");
			}

			// 判断物料二维码中包含的料盘数量信息是否与数据库中的料盘剩余数相匹配
			Integer remainderQuantity = Material.dao.findById(materialId).getRemainderQuantity();
			if (remainderQuantity.intValue() != quantity) {
				throw new OperationException("时间戳为" + materialId + "的料盘数量与数据库中记录的料盘剩余数量不一致，请扫描正确的料盘二维码！");
			}
			// 扫码出库后，将料盘设置为不在盒内
			material.setIsInBox(false);
			material.update();

			Task task = Task.dao.findById(packingListItem.getTaskId());
			// 写入一条出库任务日志
			TaskLog taskLog = new TaskLog();
			taskLog.setPackingListItemId(packListItemId);
			taskLog.setMaterialId(materialId);
			taskLog.setQuantity(quantity);
			taskLog.setOperator(user.getUid());
			// 区分出库操作人工还是机器操作,目前的版本暂时先统一写成人工操作
			taskLog.setAuto(false);
			taskLog.setTime(new Date());
			taskLog.setDestination(task.getDestination());
			return taskLog.save();
		}
	}


	// 删除错误的料盘记录
	public Material deleteMaterialRecord(Integer packListItemId, String materialId) {
		PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
		int taskId = packingListItem.getTaskId();
		Task task = Task.dao.findById(taskId);
		Material material = Material.dao.findById(materialId);
		// 对于不在已到站料盒的物料，禁止对其进行操作
		AGVIOTaskItem agvioTaskItem = null;
		for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(taskId)) {
			if (redisTaskItem.getId().intValue() == packListItemId.intValue()) {
				agvioTaskItem = redisTaskItem;
				if (redisTaskItem.getBoxId().intValue() != material.getBox().intValue()) {
					throw new OperationException("时间戳为" + materialId + "的料盘不在该料盒中，禁止删除！");
				}
			}
		}
		if (task.getType() == TaskType.IN || task.getType() == TaskType.SEND_BACK) { // 若是入库或退料入库任务，则删除掉入库记录，并删除掉物料实体表记录
			TaskLog taskLog = TaskLog.dao.findFirst(GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, packListItemId, materialId);
			if (!material.getIsInBox() || !material.getRemainderQuantity().equals(taskLog.getQuantity())) {
				throw new OperationException("时间戳为" + materialId + "的料盘已被出库，禁止删除！");
			}
			SampleOutRecord sampleOutRecord = SampleOutRecord.dao.findFirst(SampleTaskSQL.GET_SAMPLE_OUT_RECORD_BY_MATERIALID, materialId);
			if (sampleOutRecord != null) {
				throw new OperationException("抽检出库或者丢失的料盘入库后，禁止删除！");
			}
			Db.update(DELETE_TASK_LOG_SQL, packListItemId, materialId);
			Material.dao.deleteById(materialId);
			return material;
		} else { // 若是出库任务，删除掉出库记录；若已经执行过删除操作，则将物料实体表对应的料盘记录还原
			TaskLog taskLog = TaskLog.dao.findFirst(GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, packListItemId, materialId);
			Record record = Db.findFirst(SQL.GET_NEWEST_MATERIAL_TASKLOG_BY_ITEM_ID_SQL, packingListItem);
			if (record != null && !record.getDate("production_time").equals(material.getProductionTime())) {
				throw new OperationException("时间戳为" + materialId + "的料盘并非当前出库记录中最新的料盘，禁止删除！");
			}
			material.setIsInBox(true);
			material.update();
			TaskLog.dao.deleteById(taskLog.getId());
			// 判断删除的是否是截料的那一盘，是的话，消除其截料状态
			record = Db.findFirst(SQL.GET_CUT_MATERIAL_RECORD_SQL, packListItemId);
			if (record == null) {
				TaskItemRedisDAO.updateIOTaskItemInfo(agvioTaskItem, null, null, null, null, null, null, false);
			}
			return material;
		}
	}


	// 更新标准料盘出库数量以及料盘信息
	public void updateOutQuantityAndMaterialInfo(AGVIOTaskItem item, Boolean afterCut, User user) {
		synchronized (UPDATEOUTQUANTITYANDMATERIALINFO_LOCK) {
			int acturallyNum = 0;
			List<Record> records = Db.find(SQL.GET_TASKLOG_BY_ITEM_ID_SQL, item.getId());
			if (!records.isEmpty()) {
				for (Record record : records) {
					acturallyNum += record.getInt("quantity");
					int remainderQuantity = record.getInt("remainder_quantity") - record.getInt("quantity");

					if (!afterCut && item.getIsForceFinish()) {
						Material material = Material.dao.findById(record.getStr("material_id"));
						if (remainderQuantity <= 0) {
							material.setRow(-1);
							material.setCol(-1);
							material.setRemainderQuantity(0);
							material.setIsInBox(false);
							material.update();
						} else {
							material.setRemainderQuantity(remainderQuantity);
							material.setIsInBox(false);
							material.update();
						}
					}
				}
				Material material2 = Material.dao.findFirst(GET_MATERIAL_BY_BOX_ID, item.getBoxId(), true);
				if (material2 == null) {
					MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
					materialBox.setStatus(BoxState.EMPTY).update();
				}
			}

			// 出库超发，欠发的记录写入外仓
			if (!item.getIsCut() && item.getIsForceFinish()) {

				PackingListItem packingListItem = PackingListItem.dao.findById(item.getId());
				if (!packingListItem.getQuantity().equals(acturallyNum)) {
					Task task = Task.dao.findById(item.getTaskId());
					if (packingListItem.getQuantity() < acturallyNum) {
						// 超发
						ExternalWhLog externalWhLog = new ExternalWhLog();
						externalWhLog.setMaterialTypeId(packingListItem.getMaterialTypeId());
						externalWhLog.setDestination(task.getDestination());
						externalWhLog.setSourceWh(UW_ID);
						externalWhLog.setTaskId(task.getId());
						externalWhLog.setQuantity(acturallyNum - packingListItem.getQuantity());
						if (task.getIsInventoryApply()) {
							Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
							externalWhLog.setTime(inventoryTask.getCreateTime());
						} else {
							externalWhLog.setTime(new Date());
						}
						externalWhLog.setOperatior(user.getUid());
						externalWhLog.save();
					} else if (packingListItem.getQuantity() > acturallyNum) {
						// 欠发
						if (externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination()) > 0) {
							ExternalWhLog externalWhLog = new ExternalWhLog();
							externalWhLog.setMaterialTypeId(packingListItem.getMaterialTypeId());
							externalWhLog.setDestination(task.getDestination());
							externalWhLog.setSourceWh(UW_ID);
							externalWhLog.setTaskId(task.getId());
							int lackNum = packingListItem.getQuantity() - acturallyNum;
							if (task.getIsInventoryApply()) {
								Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
								externalWhLog.setTime(inventoryTask.getCreateTime());
								int storeNum = externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination(), inventoryTask.getCreateTime());
								if (storeNum > lackNum) {
									externalWhLog.setQuantity(0 - lackNum);
								} else {
									externalWhLog.setQuantity(0 - storeNum);
								}
							} else {
								int storeNum = 0;
								List<Task> inventoryTasks = InventoryTaskService.me.getUnStartInventoryTask(task.getSupplier());
								if (inventoryTasks.size() > 0) {
									storeNum = externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination()) - externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination(), inventoryTasks.get(0).getCreateTime());

								} else {
									storeNum = externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination());
								}
								if (storeNum > lackNum) {
									externalWhLog.setQuantity(0 - lackNum);
								} else if (storeNum <= 0) {
									externalWhLog.setQuantity(0);
								} else {
									externalWhLog.setQuantity(0 - storeNum);
								}
								externalWhLog.setTime(new Date());
							}

							externalWhLog.setOperatior(user.getUid());
							externalWhLog.save();
						}
					}
				}

			}
		}

	}


	public String modifyOutQuantity(Integer taskLogId, Integer packListItemId, String materialId, Integer quantity) {
		String result = "操作成功";
		PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
		int taskId = packingListItem.getTaskId();
		Material material = Material.dao.findById(materialId);
		// 对于不在已到站料盒的物料，禁止对其进行操作
		for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(taskId)) {
			if (redisTaskItem.getId().intValue() == packListItemId.intValue() && redisTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW)) {
				if (material.getRemainderQuantity() <= 0 && !material.getIsInBox()) {
					throw new OperationException("时间戳为" + materialId + "的料盘不存在或者尚未出库扫码，禁止修改！");
				}
				if (material.getRemainderQuantity() < quantity) {
					throw new OperationException("时间戳为" + materialId + "的料盘的修改数量大于实际数量，修改失败！");
				}
				TaskLog taskLog = TaskLog.dao.findById(taskLogId);
				if (!taskLog.getPackingListItemId().equals(packListItemId) || !taskLog.getMaterialId().equals(material.getId())) {
					throw new OperationException("找不到当前任务条目时间戳为" + materialId + "的料盘的出库记录，修改失败！");
				}
				if (!material.getBox().equals(redisTaskItem.getBoxId())) {
					throw new OperationException("时间戳为" + materialId + "的料盘并未存在于当前料盒，禁止修改！");
				}
				List<TaskLog> taskLogs = TaskLog.dao.find(SQL.GET_OUT_MATERIAL_SQL_BY_BOX, taskLog.getPackingListItemId(), material.getBox());
				Date date = material.getProductionTime();
				for (TaskLog taskLog2 : taskLogs) {
					if (date.before(taskLog2.getDate("productionTime"))) {
						throw new OperationException("时间戳为" + materialId + "并非当前料盒出库料盘中的最新料盘，禁止修改！");
					}
				}
				Record record = Db.findFirst(SQL.GET_CUT_MATERIAL_RECORD_SQL, packListItemId);
				if (record != null && !record.getStr("material_id").equals(materialId)) {
					throw new OperationException("当前该料号已存在截料的料盘，无法再次进行截料，禁止修改！");
				}
				taskLog.setQuantity(quantity).update();
				record = Db.findFirst(SQL.GET_CUT_MATERIAL_RECORD_SQL, packListItemId);
				if (record != null) {
					TaskItemRedisDAO.updateIOTaskItemInfo(redisTaskItem, null, null, null, null, null, null, true);
				} else {
					TaskItemRedisDAO.updateIOTaskItemInfo(redisTaskItem, null, null, null, null, null, null, false);
				}
				result = "操作成功";
				return result;
			}
		}

		throw new OperationException("找不到该任务条目，可能该任务条目被删除或者尚未到站");
	}


	// 将截料后剩余的物料置为在盒内
	public Material backAfterCutting(Integer packingListItemId, String materialId, Integer quantity, String supplierName) {
		// 通过任务条目id获取套料单记录
		PackingListItem packingListItem = PackingListItem.dao.findById(packingListItemId);
		// 通过套料单记录获取物料类型id
		MaterialType materialType = MaterialType.dao.findById(packingListItem.getMaterialTypeId());
		// 通过物料类型获取对应的供应商id
		Integer supplierId = materialType.getSupplier();
		// 通过供应商id获取供应商名
		String sName = Supplier.dao.findById(supplierId).getName();
		FormerSupplier formerSupplier = FormerSupplier.dao.findFirst(GET_FORMER_SUPPLIER_SQL, supplierName, materialType.getSupplier());
		TaskLog taskLog = TaskLog.dao.findFirst(GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, packingListItemId, materialId);
		Material material = Material.dao.findById(materialId);
		if (!supplierName.equals(sName) && formerSupplier == null) {
			throw new OperationException("扫码错误，供应商 " + supplierName + " 对应的任务目前没有在本仓口进行任务，" + "本仓口已绑定 " + sName + " 的任务单！");
		} else if (taskLog == null) {
			throw new OperationException("扫错料盘，该料盘不需要放回该料盒!");
		} else if (material.getRemainderQuantity().intValue() != quantity) {
			throw new OperationException("请扫描修改出库数时所打印出的新料盘二维码!");
		} else if (material.getRemainderQuantity() == 0) {
			throw new OperationException("该料盘已全部出库!");
		} else if (material.getIsInBox()) {
			throw new OperationException("该料盘已设置为在盒内，请将料盘放入料盒内!");
		} else {
			material.setIsInBox(true).update();
			return material;
		}
	}


	// 判断是否对已截过料的料盘重新扫码过
	public Boolean isScanAgain(Integer packingListItemId) {
		Boolean flag = true;
		List<TaskLog> taskLogList = TaskLog.dao.find(GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_SQL, packingListItemId);
		for (TaskLog taskLog : taskLogList) {
			/*
			 * 这里之前有bug，因为出库时，库存是实时更新的，有可能本来这个任务条目是不缺料的，但是后来又缺料了。 对于缺料任务条目，会记录一条 material_id
			 * 为 null，quantity 为 null 的出库日志，这样就会报NPE。目前在查询是加了非常判断，也许能解决该问题。
			 * 若在截料后重新入库时出现bug，重点关注这里。
			 */
			Material material = Material.dao.findById(taskLog.getMaterialId());
			int remainderQuantity = material.getRemainderQuantity();
			if (remainderQuantity > 0 && !material.getIsInBox()) {
				flag = false;
			}
		}

		return flag;
	}


	/**
	 * 设置任务指定的仓口
	 * @param taskId
	 * @param windowIds
	 */
	public synchronized void setTaskWindow(Integer taskId, String windowIds) {
		Task task = Task.dao.findById(taskId);
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("任务未处于进行中状态，无法指定仓口！");
		}
		if (TaskItemRedisDAO.getTaskStatus(taskId)) {
			throw new OperationException("任务未处于暂停状态，无法指定或更改仓口！");
		}
		List<Window> windows = Window.dao.find(GET_WINDOW_BY_TASK, task.getId());
		if (task.getType().equals(TaskType.COUNT)) {
			List<InventoryLog> logs = InventoryLog.dao.find(InventoryTaskSQL.GET_UN_INVENTORY_LOG_BY_TASKID, taskId);
			if (logs.size() == 0 && windows.isEmpty()) {
				throw new OperationException("盘点任务UW仓盘点阶段已结束，无法指定或更改仓口！");
			}
		}
		if ((task.getType().equals(TaskType.IN) || task.getType().equals(TaskType.SEND_BACK)) && !InputMaterialRedisDAO.getScanStatus(task.getWindow()).equals(-1)) {
			throw new OperationException("无法指定或更改仓口，请先将之前的物料正确入库再进行更改仓口操作");
		}
		List<Integer> windowIdList = new ArrayList<>();
		if (windowIds != null && !windowIds.trim().equals("")) {
			String[] windowIdArr = windowIds.split(",");

			for (String windowId : windowIdArr) {
				windowIdList.add(Integer.valueOf(windowId.trim()));
			}
		}

		Boolean flag = true;
		for (Window window : windows) {
			if (!windowIdList.contains(window.getId())) {
				flag = false;
			}
		}
		if (!flag && !windows.isEmpty()) {
			if (task.getType().equals(TaskType.IN) || task.getType().equals(TaskType.OUT) || task.getType().equals(TaskType.SEND_BACK)) {
				for (AGVIOTaskItem agvioTaskItem : TaskItemRedisDAO.getIOTaskItems(taskId)) {
					if (agvioTaskItem.getState() >= TaskItemState.ASSIGNED && agvioTaskItem.getState() < TaskItemState.BACK_BOX) {
						throw new OperationException("仓口" + agvioTaskItem.getWindowId() + "存在已分配叉车或已到站的任务条目，无法解绑或重新设置该任务的仓口");
					}
				}
			} else if (task.getType().equals(TaskType.COUNT)) {
				for (AGVInventoryTaskItem agvInventoryTaskItem : TaskItemRedisDAO.getInventoryTaskItems(taskId)) {
					if (agvInventoryTaskItem.getState() >= TaskItemState.ASSIGNED && agvInventoryTaskItem.getState() < TaskItemState.BACK_BOX) {
						throw new OperationException("仓口" + agvInventoryTaskItem.getWindowId() + "存在已分配叉车或已到站的任务条目，无法解绑或重新设置该任务的仓口");
					}
				}
			} else if (task.getType().equals(TaskType.SAMPLE)) {
				for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems(taskId)) {
					if (agvSampleTaskItem.getState() >= TaskItemState.ASSIGNED && agvSampleTaskItem.getState() < TaskItemState.BACK_BOX) {
						throw new OperationException("仓口" + agvSampleTaskItem.getWindowId() + "存在已分配叉车或已到站的任务条目，无法解绑或重新设置该任务的仓口");
					}
				}
			}

		}

		if (windowIds != null && !windowIds.trim().equals("")) {
			List<Window> bindWindows = new ArrayList<>(10);
			for (Integer windowId : windowIdList) {
				Window window = Window.dao.findById(windowId);
				if (window != null && window.getBindTaskId() != null && !window.getBindTaskId().equals(taskId)) {
					throw new OperationException("仓口" + windowId + "存在已绑定其他任务，无法绑定该任务");
				}
				bindWindows.add(window);
			}
			for (Window window : windows) {
				window.setBindTaskId(null);
				window.update();
			}
			for (Window window : bindWindows) {
				window.setBindTaskId(taskId).update();
			}
		} else {
			for (Window window : windows) {
				window.setBindTaskId(null);
				window.update();
			}
		}
	}


	/**
	 * 获取任务仓口
	 * @param taskId
	 */
	public List<Window> getTaskWindow(Integer taskId) {
		Task task = Task.dao.findById(taskId);
		List<Window> windows = Window.dao.find(GET_WINDOW_BY_TASK, task.getId());
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


	public Date getDateTime() throws ParseException {
		Date date = new Date();
		SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = sFormat.format(date) + " 00:00:00";
		return sFormat.parse(dateString);
	}


	public String getTaskName(Integer id) {
		Task task = Task.dao.findById(id);
		if (task != null) {
			return task.getFileName();
		}
		return null;
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


	public String switchTask(Integer taskId, Boolean flag) {
		Task task = Task.dao.findById(taskId);
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("任务并未处于进行中状态，无法开始或暂停！");
		}
		if (flag) {
			Window window = Window.dao.findFirst(GET_WINDOW_BY_TASK, taskId);
			if (window == null) {
				throw new OperationException("任务没有绑定仓口，任务无法开始！");
			}
		}
		TaskItemRedisDAO.setTaskStatus(taskId, flag);
		return "操作成功";
	}
}