package com.jimi.uw_server.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Enhancer;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.handle.IOHandler;
import com.jimi.uw_server.constant.BoxState;
import com.jimi.uw_server.constant.IOTaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
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

/**
 * 任务业务层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class TaskService {

	private static SelectService selectService = Enhancer.enhance(SelectService.class);
	
	private static final Object CREATEIOTASK_LOCK = new Object();

	private static final Object START_LOCK = new Object();

	private static final Object PASS_LOCK = new Object();

	private static final Object CANCEL_LOCK = new Object();

	private static final Object IN_LOCK = new Object();

	private static final Object OUT_LOCK = new Object();

	private static final Object UPDATEOUTQUANTITYANDMATERIALINFO_LOCK = new Object();

	private static final String GET_FILE_NAME_SQL = "SELECT * FROM task WHERE file_name = ? and state < 4";

	private static final String GET_MATERIAL_TYPE_BY_NO_SQL = "SELECT * FROM material_type WHERE no = ? AND supplier = ? AND enabled = 1";

	private static final String DELETE_PACKING_LIST_ITEM_BY_TASK_ID_SQL = "DELETE FROM packing_list_item WHERE task_id = ?";
	
	private static final String GET_MATERIAL_TYPE_ID_SQL = "SELECT material_type_id FROM packing_list_item WHERE material_type_id = ? AND task_id = ?";

	private static final String GET_TASK_ITEMS_SQL = "SELECT * FROM packing_list_item WHERE task_id = ?";
	
	private static final String GET_RUNNING_TASK_SQL = "SELECT * FROM task WHERE state = 2";

	private static final String GET_TASK_ITEM_DETAILS_SQL = "SELECT material_id AS materialId, quantity, production_time AS productionTime FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id";

	private static final String GET_PACKING_LIST_ITEM_DETAILS_SQL = "SELECT material_id AS materialId, quantity, production_time AS productionTime, is_in_box AS isInBox, remainder_quantity  AS remainderQuantity FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id";

	private static final String GET_FREE_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IS NULL";

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
	
	private static final String GET_PACKINGLIST_ITEM_BY_TYPE_AND_TASK = "SELECT * FORM packing_list_item WHERE material_type_id = ? AND task_id = ?";
	
	private static final String GET_MATERIAL_BOX_BY_SUPPLIER_AND_TYPE_SQL = "SELECT * FROM material_box WHERE enabled = 1 AND supplier = ? AND material_box.type = ?";

	private static final String GET_REMAIN_MATERIAL_BY_BOX = "SELECT * FROM material where box = ? and remainder_quantity > 0";
	
	private static final String GET_MATERIAL_BY_TYPE_SQL = "SELECT * FROM material WHERE type = ? AND remainder_quantity > 0";
	
	// 创建出入库/退料任务
	public String createIOTask(Integer type, String fileName, String fullFileName, Integer supplier, Integer destination) throws Exception {
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
			//判断是否存在其他正在执行的任务
			if(Task.dao.findFirst(GET_RUNNING_TASK_SQL) != null) {
				throw new OperationException("演示版本一次只能同时执行一个任务");
			}
			
			// 判断仓口是否被占用
			Window windowDao = Window.dao.findById(window);
			if (windowDao.getBindTaskId() != null) {
				throw new OperationException("该仓口已被占用，请选择其它仓口！");
			}
			
			// 判断任务状态是否为“未开始”
			Task task = Task.dao.findById(id);
			int state = task.getState();
			if (state != TaskState.WAIT_START) {
				throw new OperationException("该任务已开始过或已作废！");
			}
			
			// 设置仓口，并在仓口表绑定该任务id
			task.setWindow(window);
			windowDao.setBindTaskId(id);
			windowDao.update();
			
			//获取套料单条目
			List<PackingListItem> packingListItems = PackingListItem.dao.find(GET_TASK_ITEMS_SQL, id);
			
			if (task.getType() == TaskType.IN) { // 如果任务类型为入库
				
				//入库分配空料盒
				List<MaterialBox> materialBoxs = MaterialBox.dao.find(GET_MATERIAL_BOX_BY_SUPPLIER_AND_TYPE_SQL, task.getSupplier(), 1);
				MaterialBox emptyBox = null;
				for (MaterialBox materialBox : materialBoxs) {
					Material material = Material.dao.findFirst(GET_REMAIN_MATERIAL_BY_BOX, materialBox.getId());
					if (material == null) {
						emptyBox = materialBox;
						break;
					}
				}
				
				putTaskItemToRedisAndSendGetBoxCmd(packingListItems, emptyBox);
				
			} else if (task.getType() == TaskType.OUT) { // 如果任务类型为出库
				
				// 根据其中一个物料类型获取其中一个物料，进而获取它的盒号（物料获取料演示版本特例：任务所需物料都存在一个料盒中）
				Material material = Material.dao.findFirst(GET_MATERIAL_BY_TYPE_SQL, packingListItems.get(0).getMaterialTypeId());
				MaterialBox materialBox = MaterialBox.dao.findById(material.getBox());
				
				putTaskItemToRedisAndSendGetBoxCmd(packingListItems, materialBox);
				
			}
			
			// 将任务状态设置为进行中
			task.setState(TaskState.PROCESSING);
			return task.update();
		}
	}


	/**
	 * 把任务条目放到Redis并发送取盒指令到AGVServer
	 */
	private void putTaskItemToRedisAndSendGetBoxCmd(List<PackingListItem> packingListItems, MaterialBox materialBox) {
		//添加任务条目到Redis
		AGVIOTaskItem a = null;
		List<AGVIOTaskItem> taskItems = new ArrayList<AGVIOTaskItem>();
		for (PackingListItem item : packingListItems) {
			a = new AGVIOTaskItem(item, IOTaskItemState.ASSIGNED, 5);
			a.setBoxId(materialBox.getId());
			taskItems.add(a);
		}
		TaskItemRedisDAO.addIOTaskItem(taskItems);
		
		//发送取盒指令
		try {
			IOHandler.sendGetBoxCmd(a, materialBox);
		} catch (Exception e) {
			throw new RuntimeException(e);
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
					for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems()) {
						// 这里加这条if语句是为了判断任务队列中是否还存在该任务对应的任务条目，因为有可能任务队列中还存在不同任务id的任务条目，因此不能根据任务队列中还有任务条目就直接break
						if (redisTaskItem.getTaskId().intValue() == id) {
							untiedWindowflag = false;
							break;
						}
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
				for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems()) {
					if(task.getId().equals(item.getTaskId())) {
						IOHandler.clearTil(item.getGroupId().toString());
					}
				}
				return true;
			}
		}

	}


	// 查看任务详情
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
				IOTaskDetailVO io = new IOTaskDetailVO(packingListItem.get("PackingListItem_Id"), packingListItem.get("MaterialType_No"), packingListItem.get("PackingListItem_Quantity"), actualQuantity, packingListItem.get("PackingListItem_FinishTime"));
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
			default:
				break;
		}
		return windowIds;
	}


	// 查询所有任务
	public Object select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
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
		if (task.getState() == TaskState.PROCESSING && !isLack) {
			task.setState(TaskState.FINISHED);
			task.update();
		}else if (task.getState() == TaskState.PROCESSING && isLack){
			task.setState(TaskState.EXIST_LACK);
			task.update();
		}
		// 将仓口解绑(作废任务时，如果还有任务条目没跑完就不会解绑仓口，因此不管任务状态是为进行中还是作废，这里都需要解绑仓口)
		Window window = Window.dao.findById(task.getWindow());
		window.setBindTaskId(null);
		window.update();
	}


	// 完成任务条目
	public boolean finishItem(Integer packListItemId, Boolean isForceFinish) {
		if (isForceFinish) {
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems()) {
				if (redisTaskItem.getId().intValue() == packListItemId) {
					TaskItemRedisDAO.updateTaskIsForceFinish(redisTaskItem, isForceFinish);
					PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
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
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems()) {
				if (redisTaskItem.getTaskId().intValue() == window.getBindTaskId().intValue()) {
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
		for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems()) {
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

						WindowParkingListItemVO wp = new WindowParkingListItemVO(windowParkingListItem.get("PackingListItem_Id"), task.getFileName(),  task.getType(), windowParkingListItem.get("MaterialType_No"), windowParkingListItem.get("PackingListItem_Quantity"), actualQuantity, redisTaskItem.getMaterialTypeId(), redisTaskItem.getIsForceFinish());
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
			for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems()) {
				if (item.getId().intValue() == packListItemId) {
					boxId = item.getBoxId().intValue();
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
			material.save();

			Task task = Task.dao.findById(packingListItem.getTaskId());

			// 写入一条入库任务日志
			TaskLog taskLog = new TaskLog();
			taskLog.setPackingListItemId(packListItemId);
			taskLog.setMaterialId(materialId);
			taskLog.setQuantity(quantity);
			taskLog.setOperator(user.getUid());
			// 手动
			taskLog.setAuto(false);
			taskLog.setTime(new Date());
			taskLog.setDestination(task.getDestination());
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
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems()) {
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
			// 手动
			taskLog.setAuto(false);
			taskLog.setTime(new Date());
			taskLog.setDestination(task.getDestination());
			return taskLog.save();
		}
	}
	
	
	// 新增入库料盘记录并写入库任务日志记录
	public boolean in(Integer taskId, String materialId, Integer row, Integer col, Integer materialTypeId, Integer quantity, String supplierName, Date productionTime) {
		synchronized(IN_LOCK) {
			PackingListItem packingListItem = PackingListItem.dao.findFirst(GET_PACKINGLIST_ITEM_BY_TYPE_AND_TASK, materialTypeId, taskId);
	
			// 新增物料表记录
			int boxId = 0;
			for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems()) {
				if (item.getId().intValue() == packingListItem.getId()) {
					boxId = item.getBoxId().intValue();
				}
			}

			Material material = Material.dao.findById(materialTypeId);
			if(material == null) {
				material = new Material();
				setMaterialFields(quantity, productionTime, packingListItem, boxId, material, row, col);
				material.setId(materialId);
				material.save();
			}else {
				setMaterialFields(quantity, productionTime, packingListItem, boxId, material, row, col);
				material.update();
			}
			
			Task task = Task.dao.findById(packingListItem.getTaskId());

			// 写入一条入库任务日志
			TaskLog taskLog = new TaskLog();
			taskLog.setPackingListItemId(packingListItem.getId());
			taskLog.setMaterialId(materialId);
			taskLog.setQuantity(quantity);
			// 区分入库操作人工还是机器操作,目前的版本暂时先统一写成人工操作
			taskLog.setAuto(true);
			taskLog.setTime(new Date());
			taskLog.setDestination(task.getDestination());
			return taskLog.save();
		}
	}


	private void setMaterialFields(Integer quantity, Date productionTime, PackingListItem packingListItem, int boxId, Material material, Integer row, Integer col) {
		material.setType(packingListItem.getMaterialTypeId());
		material.setBox(boxId);
		material.setRow(row);
		material.setCol(col);
		material.setRemainderQuantity(quantity);
		material.setProductionTime(productionTime);
	}
		
		
	// 写出库任务日志
		public boolean out(Integer taskId, String materialId, Integer materialTypeId, Integer quantity, String supplierName) {
			synchronized(OUT_LOCK) {
				// 通过供应商id获取供应商名
				PackingListItem packingListItem = PackingListItem.dao.findFirst(GET_PACKINGLIST_ITEM_BY_TYPE_AND_TASK, materialTypeId, taskId);	
				// 对于不在已到站料盒的物料，禁止对其进行操作
				Material material = Material.dao.findById(materialId);

				// 扫码出库后，将料盘设置为不在盒内
				if (material.getRemainderQuantity().equals(quantity)) {
					material.setRemainderQuantity(0);
				}
				material.setIsInBox(false);
				material.update();

				Task task = Task.dao.findById(taskId);
				// 写入一条出库任务日志
				TaskLog taskLog = new TaskLog();
				taskLog.setPackingListItemId(packingListItem.getId());
				taskLog.setMaterialId(materialId);
				taskLog.setQuantity(quantity);
				// 区分出库操作人工还是机器操作,目前的版本暂时先统一写成人工操作
				taskLog.setAuto(true);
				taskLog.setTime(new Date());
				taskLog.setDestination(task.getDestination());
				return taskLog.save();
			}
		}


	// 删除错误的料盘记录
	public boolean deleteMaterialRecord(Integer packListItemId, String materialId) {
		PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
		int taskId = packingListItem.getTaskId();
		Task task = Task.dao.findById(taskId);
		Material material = Material.dao.findById(materialId);
		// 对于不在已到站料盒的物料，禁止对其进行操作
		for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getIOTaskItems()) {
			if (redisTaskItem.getId().intValue() == packListItemId.intValue()) {
				if (redisTaskItem.getBoxId().intValue() != material.getBox().intValue()) {
					throw new OperationException("时间戳为" + materialId + "的料盘不在该料盒中，禁止删除！");
				}
			}
		}
		if (task.getType() == TaskType.IN || task.getType() == TaskType.SEND_BACK) {	// 若是入库或退料入库任务，则删除掉入库记录，并删除掉物料实体表记录
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
	public void updateOutQuantityAndMaterialInfo(AGVIOTaskItem item, String materialOutputRecords, Boolean isLater) {
		synchronized (UPDATEOUTQUANTITYANDMATERIALINFO_LOCK) {
			if (materialOutputRecords != null) {
				JSONArray jsonArray = JSONArray.parseArray(materialOutputRecords);
				for (int i=0; i<jsonArray.size(); i++) {
					JSONObject jsonObject = jsonArray.getJSONObject(i);
					String materialId = jsonObject.getString("materialId");
					Integer quantity = Integer.parseInt(jsonObject.getString("quantity"));

					// 修改任务日志的出库数量
					TaskLog taskLog = TaskLog.dao.findFirst(GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, item.getId(), materialId);
					taskLog.setQuantity(quantity).update();
					Material material = Material.dao.findById(materialId);
					if (quantity < material.getRemainderQuantity() && quantity > 0) {
						TaskItemRedisDAO.updateTaskIsForceFinish(item, true);
						item.setIsForceFinish(true);
						TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.FINISH_CUT);
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


}