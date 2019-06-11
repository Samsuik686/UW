package com.jimi.uw_server.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Enhancer;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.handle.IOHandler;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.constant.BoxState;
import com.jimi.uw_server.constant.IOTaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.ExternalWhLog;
import com.jimi.uw_server.model.InventoryLog;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.MaterialInfoBO;
import com.jimi.uw_server.model.bo.PackingListItemBO;
import com.jimi.uw_server.model.vo.IOTaskDetailVO;
import com.jimi.uw_server.model.vo.PackingListItemDetailsVO;
import com.jimi.uw_server.model.vo.TaskVO;
import com.jimi.uw_server.model.vo.WindowParkingListItemVO;
import com.jimi.uw_server.model.vo.WindowTaskItemsVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ExcelHelper;

/**
 * 任务业务层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class TaskService {

	private static SelectService selectService = Enhancer.enhance(SelectService.class);
	
	private static MaterialService materialService = Enhancer.enhance(MaterialService.class);
	
	private static ExternalWhLogService externalWhLogService = Enhancer.enhance(ExternalWhLogService.class);

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
	
	private static final String GET_MATERIAL_TYPE_ID_SQL = "SELECT material_type_id FROM packing_list_item WHERE material_type_id = ? AND task_id = ?";

	private static final String GET_TASK_ITEMS_SQL = "SELECT * FROM packing_list_item WHERE task_id = ?";

	private static final String GET_TASK_ITEM_DETAILS_SQL = "SELECT material_id AS materialId, quantity, production_time AS productionTime FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id";

	private static final String GET_PACKING_LIST_ITEM_DETAILS_SQL = "SELECT material_id AS materialId, quantity, production_time AS productionTime, is_in_box AS isInBox, remainder_quantity  AS remainderQuantity FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id";
	
	private static final String GET_FREE_WINDOWS_SQL = "SELECT id FROM window WHERE `bind_task_id` IS NULL";

	private static final String GET_IN_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 0)";

	private static final String GET_OUT_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 1)";

	private static final String GET_RETURN_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 4)";

	private static final String GET_TASK_IN_REDIS_SQL = "SELECT * FROM task WHERE id = ?";

	private static final String GET_MATERIAL_ID_IN_SAME_TASK_SQL = "SELECT * FROM task_log WHERE material_id = ? AND packing_list_item_id = ?";

	private static final String GET_MATERIAL_ID_BY_ID_SQL = "SELECT * FROM material WHERE id = ? AND is_in_box = 1";

	private static final String GET_MATERIAL_BY_ID_SQL = "SELECT * FROM material WHERE id = ?";

	private static final String DELETE_TASK_LOG_SQL = "DELETE FROM task_log WHERE packing_list_item_id = ? AND material_id = ?";

	private static final String GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL = "SELECT * FROM task_log WHERE packing_list_item_id = ? AND material_id = ?";

	private static final String GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_SQL = "SELECT * FROM task_log WHERE packing_list_item_id = ? AND material_id IS NOT NULL";

	private static final String GET_MATERIAL_BY_BOX_ID = "SELECT * FROM material where box = ? and is_in_box = ? and remainder_quantity > 0";
	
	private static final String GET_MATERIAL_BY_BOX_SQL = "SELECT * FROM material WHERE box = ? AND remainder_quantity > 0";
	
	private static final String GET_SUPPLIER_BY_NAME = "SELECT * FROM supplier WHERE name = ? AND enabled = 1";
	
	private static final String GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_SQL = "SELECT * FROM material_type WHERE no = ? AND supplier = ? AND enabled = 1";
	
	private static final String GET_TASK_ITEMS_BY_TASK_AND_TYPE_SQL = "SELECT * FROM packing_list_item WHERE task_id = ? AND material_type_id = ?";
	
	private static final String SET_PACKING_LIST_ITEM_FINISH = "UPDATE packing_list_item SET finish_time = ? WHERE task_id = ?";
	
	private static final String GET_REELNUM_BY_BOX = "SELECT COUNT(1) AS reelNum FROM material WHERE material.type = ? AND material.box = ? AND material.is_in_box = 1 AND material.remainder_quantity > 0";
	
	private static final String GET_COUNT_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 2)";

	private static final String GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER = "SELECT * FROM task where state = 2 and type = 2 and supplier = ?";

	private static final String GET_TASKLOG_BY_PACKINGITEMID = "SELECT * FROM task_log WHERE packing_list_item_id = ? AND quantity > 0";
	
	private static final String GET_EWH_LOG_BY_TASKID_AND_MATERIALTYPEID = "SELECT * FROM external_wh_log WHERE external_wh_log.task_id = ? AND external_wh_log.material_type_id = ? AND external_wh_log.quantity < 0";

	private static final String GET_UNCOVER_INVENTORY_LOG_BY_TASKID = "SELECT * FROM inventory_log WHERE task_id = ? AND enabled = 1";

	private static final String GET_WORKING_WINDOWS = "SELECT * FROM window WHERE bind_task_id IS NOT NULL";
	
	private static final String GET_TASK_BY_TYPE_STATE_SUPPLIER = "select * from task where task.type = ? and task.state = ? and task.supplier = ?";
	
	// 创建出入库/退料任务
	public String createIOTask(Integer type, String fileName, String fullFileName, Integer supplier, Integer destination, Boolean isInventoryApply, Integer inventoryTaskId) throws Exception {
		String resultString = "添加成功！";
		File file = new File(fullFileName);

		// 如果文件格式不对，则提示检查文件格式
		if (!(fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
			// 清空upload目录下的文件
			deleteTempFileAndTaskRecords(file, null);
			resultString = "创建任务失败，请检查套料单的文件格式是否正确！";
			return resultString;
		}

		ExcelHelper fileReader = ExcelHelper.from(file);
		List<PackingListItemBO> items = fileReader.unfill(PackingListItemBO.class, 2);
		// 如果套料单表头不对或者表格中没有有效的任务记录
		if (items == null || items.size() == 0) {
			deleteTempFileAndTaskRecords(file, null);
	  		resultString = "创建任务失败，请检查套料单的表头是否正确以及套料单表格中是否有效的任务记录！";
			return resultString;
		} else {
			synchronized(CREATEIOTASK_LOCK) {
				// 如果已经用该套料单创建过任务，并且该任务没有被作废，则禁止再导入相同文件名的套料单
				if (Task.dao.find(GET_FILE_NAME_SQL, fileName).size() > 0) {
					//清空upload目录下的文件
					deleteTempFileAndTaskRecords(file, null);
					resultString = "创建任务失败，已经用该套料单创建过任务，请先作废掉原来的套料单任务！或者修改原套料单文件名，如：套料单A-重新入库";
					return resultString;
				}
				// 创建一条新的任务记录
				Task task = new Task();
				task.setType(type);
				task.setFileName(fileName);
				task.setState(TaskState.WAIT_REVIEW);
				task.setCreateTime(new Date());
				task.setSupplier(supplier);
				task.setDestination(destination);
				if (isInventoryApply != null && isInventoryApply) {
					task.setIsInventoryApply(true);
					if (inventoryTaskId == null) {
						deleteTempFileAndTaskRecords(file, null);
						resultString = "创建任务失败，未选择盘点期间申补单所绑定的盘点任务！";
						return resultString;
					}
					task.setInventoryTaskId(inventoryTaskId);
				}else {
					task.setIsInventoryApply(false);
				}
				task.save();
				// 获取新任务id
				Integer newTaskId = task.getId();

				// 从套料单电子表格第四行开始有任务记录
				int i = 4;
				// 读取excel表格的套料单数据，将数据一条条写入到套料单表
				for (PackingListItemBO item : items) {
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) {		// 只读取有序号的行数据

						if (item.getNo() == null || item.getQuantity() == null || item.getNo().replaceAll(" ", "").equals("") || item.getQuantity().toString().replaceAll(" ", "").equals("")) {
							deleteTempFileAndTaskRecords(file, newTaskId);
							resultString = "创建任务失败，请检查套料单表格第" + i + "行的料号或需求数列是否填写了准确信息！";
							return resultString;
						}

						if (item.getQuantity() <= 0) {
							deleteTempFileAndTaskRecords(file, newTaskId);
							resultString = "创建任务失败，套料单表格第" + i + "行的需求数为" + item.getQuantity() + "，需求数必须大于0！";
							return resultString;
						}

						// 根据料号找到对应的物料类型
						MaterialType mType = MaterialType.dao.findFirst(GET_MATERIAL_TYPE_BY_NO_SQL, item.getNo(), supplier);
						// 判断物料类型表中是否存在对应的料号且未被禁用，若不存在，则将对应的任务记录删除掉，并提示操作员检查套料单、新增对应的物料类型
						if (mType == null) {
							deleteTempFileAndTaskRecords(file, newTaskId);
							resultString = "插入套料单失败，料号为" + item.getNo() + "的物料没有记录在物料类型表中或已被禁用，或者是供应商与料号对应不上！";
							return resultString;
						}

						// 判断套料单中是否存在相同的料号
						if (MaterialType.dao.find(GET_MATERIAL_TYPE_ID_SQL, mType.getId(), newTaskId).size() > 0) {
							deleteTempFileAndTaskRecords(file, newTaskId);
							resultString = "创建任务失败，套料单表格第" + i + "行，料号为“" + item.getNo() + "”的物料在套料单中重复出现！";
							return resultString;
						}

						PackingListItem packingListItem = new PackingListItem();

						// 将任务条目插入套料单
						Integer materialTypeId = mType.getId();
						// 添加物料类型id
						packingListItem.setMaterialTypeId(materialTypeId);
						// 获取计划出库数量
						Integer planQuantity = item.getQuantity();
						// 添加计划出入库数量
						packingListItem.setQuantity(planQuantity);
						// 添加任务id
						packingListItem.setTaskId(newTaskId);
						// 保存该记录到套料单表
						packingListItem.save();

						i++;
					} else if (i == 4) {	// 若第四行就没有序号，则说明套料单表格没有一条任务记录
						deleteTempFileAndTaskRecords(file, newTaskId);
						resultString = "创建任务失败，套料单表格没有任何有效的物料信息记录！";
						return resultString;
					} else {
						break;
					}

					}
					
				deleteTempFileAndTaskRecords(file, null);
				}
			}

		return resultString;
	}


	// 删除tomcat上的临时文件并删除对应的任务记录
	public void deleteTempFileAndTaskRecords(File file, Integer newTaskId) {
		//清空upload目录下的文件
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
		synchronized(START_LOCK) {
			// 根据仓口id查找对应的仓口记录
			Window windowDao = Window.dao.findById(window);
			// 判断仓口是否被占用
			if (windowDao.getBindTaskId() != null) {
				throw new OperationException("该仓口已被占用，请选择其它仓口！");
			} else {
				Task task = Task.dao.findById(id);
				Task inventoryTask = Task.dao.findFirst(GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER, task.getSupplier());
				InventoryLog inventoryLog = InventoryLog.dao.findFirst(GET_UNCOVER_INVENTORY_LOG_BY_TASKID, task.getId());
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
					if (task.getType() == TaskType.IN  || task.getType() == TaskType.SEND_BACK) {
						for (PackingListItem item : items) {
							AGVIOTaskItem a = new AGVIOTaskItem(item, IOTaskItemState.WAIT_SCAN, task.getPriority());
							taskItems.add(a);
						}
					} else if (task.getType() == TaskType.OUT) {		// 如果任务类型为出库，则将任务条目加载到redis中，将任务条目状态设置为未分配
						for (PackingListItem item : items) {
							AGVIOTaskItem a = new AGVIOTaskItem(item, IOTaskItemState.WAIT_ASSIGN, task.getPriority());
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
			} else if (state == TaskState.CANCELED) {	// 对于已作废过的任务，禁止作废
				throw new OperationException("该任务已作废！");
			} else {
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
				for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(task.getId())) {
						IOHandler.clearTil(item.getGroupId().toString());
						break;
				}
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
				IOTaskDetailVO io = new IOTaskDetailVO(packingListItem.get("PackingListItem_Id"), packingListItem.get("MaterialType_No"), packingListItem.get("PackingListItem_Quantity"), actualQuantity, deductQuantity, packingListItem.get("PackingListItem_FinishTime"));
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

		else if (type == TaskType.COUNT) {		//如果任务类型为盘点
			return null;
		}

		else if (type == TaskType.POSITION_OPTIZATION) {		//如果任务类型为位置优化
			return null;
		}

		return null;
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
				IOTaskDetailVO io = new IOTaskDetailVO(packingListItem.get("PackingListItem_Id"), packingListItem.get("MaterialType_No"), packingListItem.get("PackingListItem_Quantity"), actualQuantity, 0, packingListItem.get("PackingListItem_FinishTime"));
				int uwStoreNum = materialService.countAndReturnRemainderQuantityByMaterialTypeId(packingListItem.getInt("MaterialType_Id"));
				Integer whStoreNum = 0;
				if (task.getIsInventoryApply() != null && task.getIsInventoryApply()) {
					Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
					if (inventoryTask != null) {
						whStoreNum = externalWhLogService.getEWhMaterialQuantity(packingListItem.getInt("MaterialType_Id"), task.getDestination(), inventoryTask.getCreateTime());
					}
				}else {
					Task inventoryTask = Task.dao.findFirst(GET_TASK_BY_TYPE_STATE_SUPPLIER, TaskType.COUNT, TaskState.WAIT_START, task.getSupplier());
					if (inventoryTask != null) {
						whStoreNum = externalWhLogService.getEWhMaterialQuantity(packingListItem.getInt("MaterialType_Id"), task.getDestination()) - externalWhLogService.getEWhMaterialQuantity(packingListItem.getInt("MaterialType_Id"), task.getDestination(), inventoryTask.getCreateTime());
					}else {
						whStoreNum = externalWhLogService.getEWhMaterialQuantity(packingListItem.getInt("MaterialType_Id"), task.getDestination());
					}
				}
				io.setUwStoreNum(uwStoreNum);
				io.setWhStoreNum(whStoreNum);
				io.setStatus(uwStoreNum,whStoreNum,packingListItem.getInt("PackingListItem_Quantity"));
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

		else if (type == TaskType.COUNT) {		//如果任务类型为盘点
			return null;
		}

		else if (type == TaskType.POSITION_OPTIZATION) {		//如果任务类型为位置优化
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
			default:
				break;
		}
		return windowIds;
	}


	// 查询所有任务
	public Object select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		if (filter == null) {
			filter = "task.type!=5#&#task.type!=6";
		}else {
			filter = filter +  "#&#task.type!=5#&#task.type!=6";
		}
		Page<Record> result = selectService.select("task", pageNo, pageSize, ascBy, descBy, filter);
		List<TaskVO> taskVOs = new ArrayList<TaskVO>();
		for (Record res : result.getList()) {
			TaskVO t = new TaskVO(res.get("id"), res.get("state"), res.get("type"), res.get("file_name"), res.get("create_time"), res.get("priority"), res.get("supplier"));
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
	public void finish(Integer taskId, Boolean isLack) {
		Task task = Task.dao.findById(taskId);
		if (isLack) {
			for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(taskId)) {
				if (item.getState() == IOTaskItemState.LACK) {
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
								//超发
								ExternalWhLog externalWhLog = new ExternalWhLog();
								externalWhLog.setMaterialTypeId(packingListItem.getMaterialTypeId());
								externalWhLog.setDestination(task.getDestination());
								externalWhLog.setSourceWh(UW_ID);
								externalWhLog.setTaskId(task.getId());
								externalWhLog.setQuantity(acturallyNum - packingListItem.getQuantity());
								if (task.getIsInventoryApply()) {
									Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
									externalWhLog.setTime(inventoryTask.getCreateTime());
								}else {
									externalWhLog.setTime(new Date());
								}
								externalWhLog.setOperatior(operatior);
								externalWhLog.save();
							}else if (packingListItem.getQuantity() > acturallyNum) {
								//欠发
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
										}else {
											externalWhLog.setQuantity(0 - storeNum);
										}
									}else {
										int storeNum = 0;
										List<Task> inventoryTasks = InventoryTaskService.me.getUnStartInventoryTask(task.getSupplier());
										if (inventoryTasks.size() > 0) {
											storeNum = externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination()) - externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination(), inventoryTasks.get(0).getCreateTime());

										}else {
											storeNum = externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination());
										}
										if (storeNum > lackNum) {
											externalWhLog.setQuantity(0 - lackNum);
										}else if (storeNum <= 0 ) {
											externalWhLog.setQuantity(0);
										}else {
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
		
		
		if (task.getState() == TaskState.PROCESSING && !isLack) {
			task.setState(TaskState.FINISHED);
			task.update();
		}else if (task.getState() == TaskState.PROCESSING && isLack){
			task.setState(TaskState.EXIST_LACK);
			task.update();
		}
		// 将仓口解绑(作废任务时，如果还有任务条目没跑完就不会解绑仓口，因此不管任务状态是为进行中还是作废，这里都需要解绑仓口)
		Window window = Window.dao.findById(task.getWindow());
		synchronized (Lock.ROBOT_TASK_REDIS_LOCK) {
			TaskItemRedisDAO.delWindowTaskInfo(window.getId(), taskId);
		}
		window.setBindTaskId(null);
		window.update();
	}


	// 完成任务条目
	public boolean finishItem(Integer packListItemId, Boolean isForceFinish) {
		PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
		if (packingListItem != null) {
			if (isForceFinish) {
				for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(packingListItem.getTaskId())) {
					if (redisTaskItem.getId().intValue() == packListItemId) {
						TaskItemRedisDAO.updateTaskIsForceFinish(redisTaskItem, isForceFinish);
						packingListItem.setFinishTime(new Date());
						packingListItem.update();

						// 这里有缺陷，在sql语句中，但是不影响正常使用；至于是什么缺陷，等你自己发现。
						// 为计算超发数，对于出库数为0的任务，也需要记录一条日志
						TaskLog tl = TaskLog.dao.findFirst(GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_SQL, packListItemId);
						if (tl == null) {
							Task task = Task.dao.findById(packingListItem.getTaskId());
							// 为将该出库日志关联到对应的物料，需要查找对应的料盘唯一码，因为出库数是设置为0的，所以不会影响系统数据
							TaskLog taskLog = new TaskLog();
							taskLog.setPackingListItemId(packListItemId);
							taskLog.setMaterialId(null);
							taskLog.setQuantity(0);
							taskLog.setOperator(null);
							// 区分出库操作人工还是机器操作,目前的版本暂时先统一写成机器操作
							taskLog.setAuto(false);
							taskLog.setTime(new Date());
							taskLog.setDestination(task.getDestination());
							taskLog.save();
						}
					}
				}
			}
		}
		
		return true;
	}


	// 获取指定仓口任务条目
	public Object getWindowTaskItems(Integer id, Integer pageNo, Integer pageSize) {
		if (id != null) {
			Window window = Window.dao.findById(id);
			// 若任务执行完毕，仓口被解绑，如果操作员还未手动刷新页面，则前端会继续传递仓口id调用该接口，此时会报500，加上这个判断，可避免这个错误
			if (window.getBindTaskId() == null) {
				return null;
			}
			// 先进行多表查询，查询出仓口id绑定的正在执行中的任务的套料单表的id，套料单文件名，物料类型表的料号no，套料单表的计划出入库数量quantity
			Page<Record> windowTaskItems = selectService.select(new String[] {"packing_list_item", "material_type", }, new String[] {"packing_list_item.task_id = " + window.getBindTaskId(), "material_type.id = packing_list_item.material_type_id"}, null, null, null, null, null);
			List<WindowTaskItemsVO> windowTaskItemVOs = new ArrayList<WindowTaskItemsVO>();
			int totalRow = 0;
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(window.getBindTaskId())) {
				// 查询task_log中的material_id,quantity
				List<TaskLog> taskLogs = TaskLog.dao.find(GET_TASK_ITEM_DETAILS_SQL, redisTaskItem.getId());
				totalRow += 1;
				for (Record windowTaskItem : windowTaskItems.getList()) {
					Integer actualQuantity = 0;
					// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
					for (TaskLog tl : taskLogs) {
						actualQuantity += tl.getQuantity();
					}
					if (windowTaskItem.get("PackingListItem_Id").equals(redisTaskItem.getId())) {
						Task task = Task.dao.findFirst(GET_TASK_IN_REDIS_SQL, window.getBindTaskId());
						WindowTaskItemsVO wt = new WindowTaskItemsVO(windowTaskItem.get("PackingListItem_Id"), task.getFileName(), task.getType(), windowTaskItem.get("MaterialType_No"), windowTaskItem.get("PackingListItem_Quantity"), actualQuantity, windowTaskItem.get("PackingListItem_FinishTime"), redisTaskItem.getState());
						wt.setDetails(taskLogs);
						windowTaskItemVOs.add(wt);
					}
				}
			}
			List<WindowTaskItemsVO> windowTaskItemSubVOs = new ArrayList<WindowTaskItemsVO>();
			int startIndex = (pageNo-1) * pageSize;
			int endIndex = (pageNo-1) * pageSize + pageSize;
			// 不用 endIndex 作为数组结尾是为了避免数组越界
			for (int i=startIndex; i<windowTaskItemVOs.size(); i++) {
				windowTaskItemSubVOs.add(windowTaskItemVOs.get(i));
				if (i == endIndex-1) {
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
				return null;
			}
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(window.getBindTaskId())) {
				if(redisTaskItem.getState().intValue() == IOTaskItemState.ARRIVED_WINDOW) {
					Task task = Task.dao.findFirst(GET_TASK_IN_REDIS_SQL, redisTaskItem.getTaskId());
					if (task.getWindow() == id) {
						Integer packingListItemId = redisTaskItem.getId();
						// 查询task_log中的material_id,quantity
						List<TaskLog> taskLogs = TaskLog.dao.find(GET_PACKING_LIST_ITEM_DETAILS_SQL, redisTaskItem.getId());
						// 先进行多表查询，查询出仓口id绑定的正在执行中的任务的套料单表的id,套料单文件名，物料类型表的料号no,套料单表的计划出入库数量quantity
						Page<Record> windowParkingListItems = selectService.select(new String[] {"packing_list_item", "material_type", }, new String[] {"packing_list_item.id = " + packingListItemId, "material_type.id = packing_list_item.material_type_id"}, null, null, null, null, null);

						for (Record windowParkingListItem : windowParkingListItems.getList()) {
							Integer actualQuantity = 0;
							List<PackingListItemDetailsVO> packingListItemDetailsVOs = new ArrayList<PackingListItemDetailsVO>();
							for (TaskLog tl : taskLogs) {
								// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
								actualQuantity += tl.getQuantity();
								// 封装任务条目详情数据
								PackingListItemDetailsVO pl = new PackingListItemDetailsVO(tl.get("materialId"), tl.getQuantity(), tl.get("remainderQuantity"), tl.get("productionTime"), tl.get("isInBox"));
								packingListItemDetailsVOs.add(pl);
							}
							Integer storeNum = 0;
							if (task.getIsInventoryApply() != null && task.getIsInventoryApply()) {
								Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
								storeNum = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination(), inventoryTask.getCreateTime());
							}else {
								Task inventoryTask = Task.dao.findFirst(GET_TASK_BY_TYPE_STATE_SUPPLIER, TaskType.COUNT, TaskState.WAIT_START, task.getSupplier());
								if (inventoryTask != null) {
									storeNum = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination()) - externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination(), inventoryTask.getCreateTime());
								}else {
									storeNum = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination());
								}
							}
							WindowParkingListItemVO wp = new WindowParkingListItemVO(windowParkingListItem.get("PackingListItem_Id"), task.getFileName(),  task.getType(), windowParkingListItem.get("MaterialType_No"), windowParkingListItem.get("PackingListItem_Quantity"), actualQuantity, redisTaskItem.getMaterialTypeId(), redisTaskItem.getIsForceFinish(), storeNum);
							Material material = Material.dao.findFirst(GET_REELNUM_BY_BOX, redisTaskItem.getMaterialTypeId(), redisTaskItem.getBoxId());
							wp.setReelNum(material.getInt("reelNum"));
							wp.setDetails(packingListItemDetailsVOs);
							return wp;
						}
					}
				}
			}

			return null;
		}


	// 新增入库料盘记录并写入库任务日志记录
	public boolean in(Integer packListItemId, String materialId, Integer quantity, Date productionTime, String supplierName, User user) {
		synchronized(IN_LOCK) {
			// 通过任务条目id获取套料单记录
			PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
			// 通过套料单记录获取物料类型id
			MaterialType materialType = MaterialType.dao.findById(packingListItem.getMaterialTypeId());
			// 通过物料类型获取对应的供应商id
			Integer supplierId = materialType.getSupplier();
			// 通过供应商id获取供应商名
			String sName = Supplier.dao.findById(supplierId).getName();
			int materialBoxCapacity = PropKit.use("properties.ini").getInt("materialBoxCapacity");
			
			if (!supplierName.equals(sName)) {
				throw new OperationException("扫码错误，供应商 "  + supplierName + " 对应的任务目前没有在本仓口进行任务，" +  "本仓口已绑定 " + sName + " 的任务单！");
			}
			if (TaskLog.dao.find(GET_MATERIAL_ID_IN_SAME_TASK_SQL, materialId, packListItemId).size() != 0) {
				throw new OperationException("时间戳为" + materialId + "的料盘已在同一个任务中被扫描过，请勿在同一个入库任务中重复扫描同一个料盘！");
			}
			if(Material.dao.find(GET_MATERIAL_BY_ID_SQL, materialId).size() != 0) {
				throw new OperationException("时间戳为" + materialId + "的料盘已入过库，请勿重复入库！");
			}
			
			
			// 新增物料表记录
			int boxId = 0;
			for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(packingListItem.getTaskId())) {
				if (item.getId().intValue() == packListItemId) {
					boxId = item.getBoxId().intValue();
				}
			}
			if (materialType.getRadius().equals(7)) {
				List<Material> materials = Material.dao.find(GET_MATERIAL_BY_BOX_SQL, boxId);
				if (materials.size() == materialBoxCapacity) {
					throw new OperationException("当前料盒已满，请停止扫码！");
				}
			}
			Material material = new Material();
			material.setId(materialId);
			material.setType(packingListItem.getMaterialTypeId());
			material.setBox(boxId);
			material.setRow(0);
			material.setCol(0);
			material.setRemainderQuantity(quantity);
			material.setProductionTime(productionTime);
			material.setIsInBox(true);
			material.save();

			Task task = Task.dao.findById(packingListItem.getTaskId());

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
			//taskLog.setIsCleared(isCleared);
			return taskLog.save();
		}
	}


	// 写出库任务日志
	public boolean out(Integer packListItemId, String materialId, Integer quantity, String supplierName, User user) {
		synchronized(OUT_LOCK) {
			// 通过任务条目id获取套料单记录
			PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
			// 通过套料单记录获取物料类型id
			MaterialType materialType = MaterialType.dao.findById(packingListItem.getMaterialTypeId());
			// 通过物料类型获取对应的供应商id
			Integer supplierId = materialType.getSupplier();
			// 通过供应商id获取供应商名
			String sName = Supplier.dao.findById(supplierId).getName();
			if (!supplierName.equals(sName)) {
				throw new OperationException("扫码错误，供应商 " + supplierName + " 对应的任务目前没有在本仓口进行任务，" +  "本仓口已绑定 " + sName + " 的任务单！");
			}
			
			// 若扫描的料盘记录不存在于数据库中或不在盒内，则抛出OperationException
			if (Material.dao.find(GET_MATERIAL_ID_BY_ID_SQL, materialId).size() == 0) {
					throw new OperationException("时间戳为" + materialId + "的料盘没有入过库或者不在盒内，不能对其进行出库操作！");
			}
			
			// 对于不在已到站料盒的物料，禁止对其进行操作
			Material material = Material.dao.findById(materialId);
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(packingListItem.getTaskId())) {
				if (redisTaskItem.getId().intValue() == packListItemId.intValue()) {
					if (redisTaskItem.getBoxId().intValue() != material.getBox().intValue()) {
						throw new OperationException("时间戳为" + materialId + "的料盘不在该料盒中，无法对其进行出库操作！");
					}
				}
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

	
	public String importInRecords(Integer taskId, File file, User user) {
		ExcelHelper fileReader;
		String resultString = "导入成功！";
		try {
			Task task = Task.dao.findById(taskId);
			if (task == null) {
				resultString = "导入料盘记录表失败，任务不存在！";
				return resultString;
			}
			if (task.getState() != TaskState.WAIT_START) {
				resultString = "任务并未处于已审核状态，无法人工入库！";
				return resultString;
			}
			fileReader = ExcelHelper.from(file);
			List<MaterialInfoBO> items = fileReader.unfill(MaterialInfoBO.class, 0);
			if (items == null || items.size() == 0) {
				resultString = "导入料盘记录表失败，料盘记录表表头错误或者表格中没有任何有效的料盘信息记录！";
				return resultString;
			} else {
				
				synchronized(IN_LOCK) {
					List<Material> materials = new ArrayList<>();
					List<TaskLog> taskLogs = new ArrayList<>();
					Map<Integer, Integer> typeNumMap = new HashMap<>();
					int i = 2;
					for(MaterialInfoBO item : items) {
						if (item.getSerialNumber() != null && item.getSerialNumber() > 0) {
							if (item.getMaterialId() == null || item.getNo() == null || item.getSupplier() == null || item.getQuantity() == null || item.getProdutionDate() == null) {
								resultString = "导入料盘记录表失败，请检查单表格第" + i + "行的料盘号/料号/供应商/数量/生产日期列是否填写了准确信息！";
								return resultString;
							}
						
							if (item.getQuantity() <= 0) {
								resultString = "导入料盘记录表失败，请检查表格第" + i + "行的数量是否正确，料盘内物料数量需大于0";
								return resultString;
							}
							Supplier supplier = Supplier.dao.findFirst(GET_SUPPLIER_BY_NAME, item.getSupplier());
							if (supplier == null) {
								resultString = "导入料盘记录表失败，请检查表格第" + i + "行的供应商是否正确，确保在系统中已存在该供应商";
								return resultString;
							}
							MaterialType mType = MaterialType.dao.findFirst(GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_SQL, item.getNo(), supplier.getId());
							if (mType == null) {
								resultString = "导入料盘记录表失败，请检查表格第" + i + "行的料号和供应商是否正确，确保在系统中已存在该供应商和该料号";
								return resultString;
							}
							PackingListItem packingListItem = PackingListItem.dao.findFirst(GET_TASK_ITEMS_BY_TASK_AND_TYPE_SQL, taskId, mType.getId());
							if (packingListItem == null) {
								resultString = "导入料盘记录表失败，请检查表格第" + i + "行的料号是否正确，确保在该任务存在该物料的任务条目";
								return resultString;
							}
							Material material = Material.dao.findFirst(GET_MATERIAL_BY_ID_SQL, item.getMaterialId());
							if (material != null) {
								resultString = "导入料盘记录表失败，请检查表格第" + i + "行的料盘码是否正确，该料盘码已存在系统中";
								return resultString;
							}
							material = new Material();
							material.setId(item.getMaterialId().toString());
							material.setType(mType.getId());
							material.setProductionTime(item.getProdutionDate());
							material.setRow(0);
							material.setCol(0);
							material.setRemainderQuantity(item.getQuantity());
							material.setIsInBox(true);
							if (typeNumMap.get(mType.getId()) == null || typeNumMap.get(mType.getId()).equals(0)) {
								typeNumMap.put(mType.getId(), item.getQuantity());
							}else {
								int quantity = typeNumMap.get(mType.getId()) + item.getQuantity();
								typeNumMap.put(mType.getId(), quantity);
							}
							TaskLog taskLog = new TaskLog();
							taskLog.setPackingListItemId(packingListItem.getId());
							taskLog.setMaterialId(item.getMaterialId());
							taskLog.setQuantity(item.getQuantity());
							taskLog.setOperator(user.getUid());
							// 区分入库操作人工还是机器操作,目前的版本暂时先统一写成人工操作
							taskLog.setAuto(false);
							taskLog.setTime(new Date());
							taskLog.setDestination(task.getDestination());
							materials.add(material);
							taskLogs.add(taskLog);
							
						}
						i++;
					}
					List<PackingListItem> packingListItems = PackingListItem.dao.find(GET_TASK_ITEMS_SQL, taskId);
					for (PackingListItem packingListItem : packingListItems) {
						Integer acturalNum = typeNumMap.get(packingListItem.getMaterialTypeId());
						if (!packingListItem.getQuantity().equals(acturalNum)) {
							resultString = "导入失败，物料ID为" + packingListItem.getMaterialTypeId() + "的实际入库数量与计划数不等！";
							return resultString;
						}
					}
					for (Material material : materials) {
						material.save();
					}
					for (TaskLog taskLog : taskLogs) {
						taskLog.save();
					}
					Db.update(SET_PACKING_LIST_ITEM_FINISH, new Date(), taskId);
					task.setState(TaskState.FINISHED).update();
				}	
			}
			return resultString;
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException("解析表格失败 + " + e.getStackTrace());
		}
		
	}

	
	public String importOutRecords(Integer taskId, File file, User user) {
		ExcelHelper fileReader;
		String resultString = "导入成功！";
		try {
			Task task = Task.dao.findById(taskId);
			if (task == null) {
				resultString = "导入料盘记录表失败，任务不存在！";
				return resultString;
			}
			if (task.getState() != TaskState.WAIT_START) {
				resultString = "任务并未处于已审核状态，无法人工出库！";
				return resultString;
			}
			fileReader = ExcelHelper.from(file);
			List<MaterialInfoBO> items = fileReader.unfill(MaterialInfoBO.class, 0);
			if (items == null || items.size() == 0) {
				resultString = "导入料盘记录表失败，料盘记录表表头错误或者表格中没有任何有效的料盘信息记录！";
				return resultString;
			} else {
				
				synchronized(IN_LOCK) {
					List<Material> materials = new ArrayList<>();
					List<TaskLog> taskLogs = new ArrayList<>();
					Map<Integer, Integer> typeNumMap = new HashMap<>();
					int i = 2;
					for(MaterialInfoBO item : items) {
						if (item.getSerialNumber() != null && item.getSerialNumber() > 0) {
							if (item.getMaterialId() == null || item.getNo() == null || item.getSupplier() == null || item.getQuantity() == null || item.getProdutionDate() == null) {
								resultString = "导入料盘记录表失败，请检查单表格第" + i + "行的料盘号/料号/供应商/数量/生产日期列是否填写了准确信息！";
								return resultString;
							}
						
							if (item.getQuantity() <= 0) {
								resultString = "导入料盘记录表失败，请检查表格第" + i + "行的数量是否正确，料盘内物料数量需大于0";
								return resultString;
							}
							Supplier supplier = Supplier.dao.findFirst(GET_SUPPLIER_BY_NAME, item.getSupplier());
							if (supplier == null) {
								resultString = "导入料盘记录表失败，请检查表格第" + i + "行的供应商是否正确，确保在系统中已存在该供应商";
								return resultString;
							}
							MaterialType mType = MaterialType.dao.findFirst(GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_SQL, item.getNo(), supplier.getId());
							if (mType == null) {
								resultString = "导入料盘记录表失败，请检查表格第" + i + "行的料号和供应商是否正确，确保在系统中已存在该供应商和该料号";
								return resultString;
							}
							PackingListItem packingListItem = PackingListItem.dao.findFirst(GET_TASK_ITEMS_BY_TASK_AND_TYPE_SQL, taskId, mType.getId());
							if (packingListItem == null) {
								resultString = "导入料盘记录表失败，请检查表格第" + i + "行的料号是否正确，确保在该任务存在该物料的任务条目";
								return resultString;
							}
							Material material = Material.dao.findFirst(GET_MATERIAL_BY_ID_SQL, item.getMaterialId());
							if (material == null) {
								resultString = "导入料盘记录表失败，请检查表格第" + i + "行的料盘码是否正确，该料盘码不存在系统中";
								return resultString;
							}
							if (material.getBox() != null) {
								resultString = "导入料盘记录表失败，请检查表格第" + i + "行的料盘码是否正确，该料盘码非手动入库料盘，手动出库仅能出手动入库的料盘";
								return resultString;
							}
							if (!material.getRemainderQuantity().equals(item.getQuantity())) {
								resultString = "导入料盘记录表失败，请检查表格第" + i + "行的物料数量是否正确，数量与仓库内该料盘数量不等，手动出库必须出整个料盘";
								return resultString;
							}
							material.setRemainderQuantity(0);
							if (typeNumMap.get(mType.getId()) == null || typeNumMap.get(mType.getId()).equals(0)) {
								typeNumMap.put(mType.getId(), item.getQuantity());
							}else {
								int quantity = typeNumMap.get(mType.getId()) + item.getQuantity();
								typeNumMap.put(mType.getId(), quantity);
							}
							TaskLog taskLog = new TaskLog();
							taskLog.setPackingListItemId(packingListItem.getId());
							taskLog.setMaterialId(item.getMaterialId());
							taskLog.setQuantity(item.getQuantity());
							taskLog.setOperator(user.getUid());
							// 区分入库操作人工还是机器操作,目前的版本暂时先统一写成人工操作
							taskLog.setAuto(false);
							taskLog.setTime(new Date());
							taskLog.setDestination(task.getDestination());
							materials.add(material);
							taskLogs.add(taskLog);
							
						}
						i++;
					}
					for (Material material : materials) {
						material.update();
					}
					for (TaskLog taskLog : taskLogs) {
						taskLog.save();
					}
					Db.update(SET_PACKING_LIST_ITEM_FINISH, new Date(), taskId);
					task.setState(TaskState.FINISHED).update();
				}	
			}
			return resultString;
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException("解析表格失败 + " + e.getStackTrace());
		}
		
	}
	
	
	// 删除错误的料盘记录
	public boolean deleteMaterialRecord(Integer packListItemId, String materialId) {
		PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
		int taskId = packingListItem.getTaskId();
		Task task = Task.dao.findById(taskId);
		Material material = Material.dao.findById(materialId);
		// 对于不在已到站料盒的物料，禁止对其进行操作
		for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems(taskId)) {
			if (redisTaskItem.getId().intValue() == packListItemId.intValue()) {
				if (redisTaskItem.getBoxId().intValue() != material.getBox().intValue()) {
					throw new OperationException("时间戳为" + materialId + "的料盘不在该料盒中，禁止删除！");
				}
			}
		}
		if (task.getType() == TaskType.IN || task.getType() == TaskType.SEND_BACK) {	// 若是入库或退料入库任务，则删除掉入库记录，并删除掉物料实体表记录
			TaskLog taskLog = TaskLog.dao.findFirst(GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, packListItemId, materialId);
			if (!material.getIsInBox() || !material.getRemainderQuantity().equals(taskLog.getQuantity())) {
				throw new OperationException("时间戳为" + materialId + "的料盘已被出库！禁止删除！");
			}
			Db.update(DELETE_TASK_LOG_SQL, packListItemId, materialId);
			return Material.dao.deleteById(materialId);
		} else {	// 若是出库任务，删除掉出库记录；若已经执行过删除操作，则将物料实体表对应的料盘记录还原
			TaskLog taskLog = TaskLog.dao.findFirst(GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, packListItemId, materialId);
			int remainderQuantity = material.getRemainderQuantity();
			if (remainderQuantity == 0) {
				material.setRow(0);
				material.setCol(0);
				material.setRemainderQuantity(taskLog.getQuantity());
				material.setIsInBox(true);
				material.update();
			} else {
				material.setIsInBox(true);
				material.update();
			}
			return TaskLog.dao.deleteById(taskLog.getId());
		}
	}


	// 更新标准料盘出库数量以及料盘信息
	public void updateOutQuantityAndMaterialInfo(AGVIOTaskItem item, String materialOutputRecords, Boolean isLater, User user, Boolean cutFlag) {
		synchronized (UPDATEOUTQUANTITYANDMATERIALINFO_LOCK) {
			int acturallyNum = 0;
			if (materialOutputRecords != null) {
				JSONArray jsonArray = JSONArray.parseArray(materialOutputRecords);
				for (int i=0; i<jsonArray.size(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String materialId = jsonObject.getString("materialId");
					Integer quantity = Integer.parseInt(jsonObject.getString("quantity"));
					acturallyNum += quantity;
					// 修改任务日志的出库数量
					TaskLog taskLog = TaskLog.dao.findFirst(GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, item.getId(), materialId);
					taskLog.setQuantity(quantity).update();
					Material material = Material.dao.findById(materialId);
					if (quantity < material.getRemainderQuantity() && quantity > 0) {
						TaskItemRedisDAO.updateTaskIsForceFinish(item, true);
						item.setIsForceFinish(true);
						TaskItemRedisDAO.updateIOTaskItemIsCut(item, true);
					}
					/*if (isLater) {
						Integer remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(item.getMaterialTypeId());
						if (remainderQuantity <= 0) {
							item.setState(IOTaskItemState.LACK);
							TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.LACK);
						}
					}*/
					
					// 如果点击稍后再见后没有修改出库数量，则不更新库存，只将料盘设置为不在盒内
					if (!item.getIsForceFinish()) {
						material.setIsInBox(false);
						material.update();
					} else {
						// 修改物料实体表对应的料盘剩余数量
						int remainderQuantity = material.getRemainderQuantity() - quantity;
						// 若该料盘没有库存了，则将物料实体表记录置为无效
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
					Material material2 = Material.dao.findFirst(GET_MATERIAL_BY_BOX_ID, item.getBoxId(), true);
					if (material2 == null) {
						MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
						materialBox.setStatus(BoxState.EMPTY).update();
					}
				}
			}
			//出库超发，欠发的记录写入外仓
			if (!cutFlag && item.getIsForceFinish()) {
				
				PackingListItem packingListItem = PackingListItem.dao.findById(item.getId());
				if (!packingListItem.getQuantity().equals(acturallyNum)) {
					Task task = Task.dao.findById(item.getTaskId());
					if (packingListItem.getQuantity() < acturallyNum) {
						//超发
						ExternalWhLog externalWhLog = new ExternalWhLog();
						externalWhLog.setMaterialTypeId(packingListItem.getMaterialTypeId());
						externalWhLog.setDestination(task.getDestination());
						externalWhLog.setSourceWh(UW_ID);
						externalWhLog.setTaskId(task.getId());
						externalWhLog.setQuantity(acturallyNum - packingListItem.getQuantity());
						if (task.getIsInventoryApply()) {
							Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
							externalWhLog.setTime(inventoryTask.getCreateTime());
						}else {
							externalWhLog.setTime(new Date());
						}
						externalWhLog.setOperatior(user.getUid());
						externalWhLog.save();
					}else if (packingListItem.getQuantity() > acturallyNum) {
						//欠发
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
								}else {
									externalWhLog.setQuantity(0 - storeNum);
								}
							}else {
								int storeNum = 0;
								List<Task> inventoryTasks = InventoryTaskService.me.getUnStartInventoryTask(task.getSupplier());
								if (inventoryTasks.size() > 0) {
									storeNum = externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination()) - externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination(), inventoryTasks.get(0).getCreateTime());

								}else {
									storeNum = externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination());
								}
								if (storeNum > lackNum) {
									externalWhLog.setQuantity(0 - lackNum);
								}else if (storeNum <= 0 ) {
									externalWhLog.setQuantity(0);
								}else {
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


	// 将截料后剩余的物料置为在盒内
	public String backAfterCutting(Integer packingListItemId, String materialId, Integer quantity, String supplierName) {
		String resultString = "扫描成功，请将料盘放回料盒！";
		// 通过任务条目id获取套料单记录
		PackingListItem packingListItem = PackingListItem.dao.findById(packingListItemId);
		// 通过套料单记录获取物料类型id
		MaterialType materialType = MaterialType.dao.findById(packingListItem.getMaterialTypeId());
		// 通过物料类型获取对应的供应商id
		Integer supplierId = materialType.getSupplier();
		// 通过供应商id获取供应商名
		String sName = Supplier.dao.findById(supplierId).getName();
		
		TaskLog taskLog = TaskLog.dao.findFirst(GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, packingListItemId, materialId);
		Material material = Material.dao.findById(materialId);
		if (!supplierName.equals(sName)) {
			resultString = "扫码错误，供应商 " + supplierName + " 对应的任务目前没有在本仓口进行任务，" +  "本仓口已绑定 " + sName + " 的任务单！";
		} else if (taskLog == null) {
			resultString = "扫错料盘，该料盘不需要放回该料盒!";
		} else if (material.getRemainderQuantity().intValue() != quantity) {
			resultString = "请扫描修改出库数时所打印出的新料盘二维码!";
		} else if (material.getRemainderQuantity() == 0) {
			resultString = "该料盘已全部出库!";
		} else if (material.getIsInBox()) {
			resultString = "该料盘已设置为在盒内，请将料盘放入料盒内!";
		} else {
			material.setIsInBox(true).update();
		}
		return resultString;
	}


	// 判断是否对已截过料的料盘重新扫码过
	public String isScanAgain(Integer packingListItemId) {
		String resultString = "已成功发送回库指令！";
		List<TaskLog> taskLogList = TaskLog.dao.find(GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_SQL, packingListItemId);
		for (TaskLog taskLog : taskLogList) {
			/*
			这里之前有bug，因为出库时，库存是实时更新的，有可能本来这个任务条目是不缺料的，但是后来又缺料了。
			对于缺料任务条目，会记录一条 material_id 为 null，quantity 为 null 的出库日志，这样就会报NPE。目前在查询是加了非常判断，也许能解决该问题。
			若在截料后重新入库时出现bug，重点关注这里。
			*/
			Material material = Material.dao.findById(taskLog.getMaterialId());
			int remainderQuantity = material.getRemainderQuantity();
			if (remainderQuantity > 0 && !material.getIsInBox()) {
				resultString = "请扫描修改出库数时所打印出的新料盘二维码!";
			}
		}

		return resultString;
	}


	// 设置优先级
	public boolean setPriority(Integer id, Integer priority) {
		Task task = Task.dao.findById(id);
		task.setPriority(priority);
		return task.update();
	}
	
	@Log("设置仓口{windowId}所绑定的叉车{robots}")
	public String setWindowRobots(Integer windowId, String robots) {
		Window window = Window.dao.findById(windowId);
		if (window == null || window.getBindTaskId() == null) {
			throw new OperationException("当前仓库无任务运行！");
		}
		List<String> robotTempList =  new ArrayList<>();
		String result = "";
		if (robots != null && !robots.trim().equals("")) {
			String[] robotArr = robots.split(",");
			List<Window> windows = Window.dao.find(GET_WORKING_WINDOWS);
			//查找冲突的叉车
			synchronized (Lock.ROBOT_TASK_REDIS_LOCK) {
			
				for (Window windowTemp : windows) {
					if (windowTemp.getId().equals(window.getId())) {
						continue;
					}
					String rbTemp = TaskItemRedisDAO.getWindowTaskInfo(windowTemp.getId(), windowTemp.getBindTaskId());
					if (rbTemp != null && !rbTemp.equals(IOHandler.UNDEFINED) && !rbTemp.trim().equals("")) {
						String[] rbTempArr = rbTemp.split(",");
						for (String rbTempStr : rbTempArr) {
							for (String robotStr : robotArr) {
								if (rbTempStr.equals(robotStr)) {
									if (!result.equals("")) {
										result += "，";
									}
									robotTempList.add(robotStr);
									result += "叉车" + rbTempStr + "已被仓口" + windowTemp.getId() + "使用，请前往对应仓口解绑";
	
								}
							}
						}
					}
					
				}
				List<String> robotArrList = new ArrayList<>();
				for (String robotStr : robotArr) {
					robotArrList.add(robotStr);
				}
				if (robotTempList.size() == 0) {
					TaskItemRedisDAO.setWindowTaskInfo(windowId, window.getBindTaskId(), robots);
				}else {
					for (String sameRobot : robotTempList) {
						if (robotArrList.contains(sameRobot)) {
							robotArrList.remove(sameRobot);
						}
					}
					String insertRobots = "";
					for (String string : robotArrList) {
						if (!insertRobots.equals("")) {
							insertRobots += ",";
						}
						insertRobots += string;
					}
					if (!insertRobots.equals("")) {
						TaskItemRedisDAO.setWindowTaskInfo(windowId, window.getBindTaskId(), insertRobots);
					}
				}
			}	
			
		}else {
			TaskItemRedisDAO.delWindowTaskInfo(windowId, window.getBindTaskId());
		}
		if (!result.equals("")) {
			return result;
		}
		return "操作成功";
	}

	
	public String getWindowRobots(Integer windowId) {
		String result = "undefined";
		Window window = Window.dao.findById(windowId);
		Task task = Task.dao.findById(window.getBindTaskId());
		if (task != null) {
			synchronized (Lock.ROBOT_TASK_REDIS_LOCK) {
				result = TaskItemRedisDAO.getWindowTaskInfo(windowId, window.getBindTaskId());
			}
		}
		return result;
	}

}