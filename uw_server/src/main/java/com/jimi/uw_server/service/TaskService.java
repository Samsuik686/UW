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
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.PackingListItemBO;
import com.jimi.uw_server.model.vo.IOTaskDetailVO;
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

	private static final String GET_FILE_NAME_SQL = "SELECT * FROM task WHERE file_name = ? and state < 4";

	private static final String GET_MATERIAL_TYPE_BY_NO_SQL = "SELECT * FROM material_type WHERE no = ? and enabled = 1";

	private static final String DELETE_PACKING_LIST_ITEM_BY_TASK_ID_SQL = "DELETE FROM packing_list_item WHERE task_id = ?";
	
	private static final String GET_MATERIAL_TYPE_ID_SQL = "SELECT material_type_id FROM packing_list_item WHERE material_type_id = ? AND task_id = ?";

	private static final String GET_TASK_ITEMS_SQL = "SELECT * FROM packing_list_item WHERE task_id = ?";

	private static final String GET_TASK_ITEM_DETAILS_SQL = "SELECT material_id as materialId, quantity FROM task_log WHERE task_id = ? AND material_id IN (SELECT id FROM material WHERE type = ?)";

	private static final String GET_FREE_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IS NULL";

	private static final String GET_IN_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 0)";

	private static final String GET_OUT_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 1)";

	private static final String GET_TASK_IN_REDIS_SQL = "SELECT * FROM task WHERE id = ?";

	private static final String GET_MATERIAL_ID_IN_SAME_TASK_SQL = "SELECT * FROM task_log WHERE material_id = ? AND task_id = ?";

	private static final String GET_MATERIAL_BY_ID_SQL = "SELECT * FROM material WHERE id = ?";

	private static final String GET_PACKING_LIST_ITEM_SQL = "SELECT * FROM packing_list_item WHERE task_id = ?";

	private static final String DELETE_TASK_LOG_SQL = "DELETE FROM task_log WHERE task_id = ? AND material_id = ?";

	private static final String GET_TASK_LOG_SQL = "SELECT * FROM task_log WHERE task_id = ? AND material_id = ?";


	public String createIOTask(Integer type, String fileName, String fullFileName) throws Exception {
		String resultString = "添加成功！";
		File file = new File(fullFileName);

		// 如果文件格式不对，则提示检查文件格式
		if (!(fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
			//清空upload目录下的文件
			if (file.exists()) {
				file.delete();
			}

			resultString = "创建任务失败，请检查套料单的文件格式是否正确！";
			return resultString;
		}

		ExcelHelper fileReader = ExcelHelper.from(file);
		List<PackingListItemBO> items = fileReader.unfill(PackingListItemBO.class, 2);
		// 如果套料单表头不对，则提示检查套料单表头
		if (items == null || items.size() == 0) {
			if (file.exists()) {
				file.delete();
			}

			resultString = "创建任务失败，请检查套料单的表头是否正确！";
			return resultString;
		} else {
			synchronized(CREATEIOTASK_LOCK) {
				// 如果已经用该套料单创建过任务，并且该任务没有被作废，则禁止再导入相同文件名的套料单
				if (Task.dao.find(GET_FILE_NAME_SQL, fileName).size() > 0) {
					//清空upload目录下的文件
					if (file.exists()) {
						file.delete();
					}
					resultString = "已经用该套料单创建过任务，请先作废掉原来的套料单任务！或者修改原套料单文件名，如：套料单A-重新入库";
					return resultString;
				}
				// 如果套料单格式正确，则创建一条新的任务记录
				Task task = new Task();
				task.setType(type);
				task.setFileName(fileName);
				task.setState(0);
				task.setCreateTime(new Date());
				task.save();
				// 获取新任务id
				Integer newTaskId = task.getId();

				// 读取excel表格的套料单数据，将数据一条条写入到套料单表
				for (PackingListItemBO item : items) {
					// 检查excel表格内容，避免造成空指针异常或将空格误认为料号或需求数
					if (item.getNo() == null || item.getQuantity() == null || item.getNo().replaceAll(" ", "").equals("") || item.getQuantity().toString().replaceAll(" ", "").equals("")) {
						if (file.exists()) {
							file.delete();
						}
						Db.update(DELETE_PACKING_LIST_ITEM_BY_TASK_ID_SQL, newTaskId);
						Task.dao.deleteById(newTaskId);
						resultString = "创建任务失败，请检查套料单中是否存在非法字符！";
						return resultString;
					}
					// 根据料号找到对应的物料类型id
					MaterialType noDao = MaterialType.dao.findFirst(GET_MATERIAL_TYPE_BY_NO_SQL, item.getNo());
					// 判断物料类型表中是否存在对应的料号且未被禁用，若不存在，则将对应的任务记录删除掉，并提示操作员检查套料单、新增对应的物料类型
					if (noDao == null) {
						if (file.exists()) {
							file.delete();
						}
						Db.update(DELETE_PACKING_LIST_ITEM_BY_TASK_ID_SQL, newTaskId);
						Task.dao.deleteById(newTaskId);
						resultString = "插入套料单失败，料号为" + item.getNo() + "的物料没有记录在物料类型表中或已被禁用！";
						return resultString;
					}
					// 判断套料单中是否存在相同的料号
					if (MaterialType.dao.find(GET_MATERIAL_TYPE_ID_SQL, noDao.getId(), newTaskId).size() > 0) {
						//清空upload目录下的文件
						if (file.exists()) {
							file.delete();
						}
						Db.update(DELETE_PACKING_LIST_ITEM_BY_TASK_ID_SQL, newTaskId);
						Task.dao.deleteById(newTaskId);
						resultString = "插入套料单失败，料号为" + item.getNo() + "的物料在套料单中重复出现！";
						return resultString;
					}

					PackingListItem packingListItem = new PackingListItem();

					// 若物料类型表中存在对应的料号，且该物料未被禁用，则将任务条目插入套料单
					Integer materialTypeId = noDao.getId();
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
				}

				if (file.exists()) {
					file.delete();
				}
				
				}
			}

		return resultString;
	}


	public boolean pass(Integer id) {
		Task task = Task.dao.findById(id);
		task.setState(1);
		return task.update();
	}


	public boolean start(Integer id, Integer window) {
		synchronized(START_LOCK) {
			Task task = Task.dao.findById(id);
			// 设置仓口，并在仓口表绑定该任务id
			task.setWindow(window);
			Window windowDao = Window.dao.findById(window);
			windowDao.setBindTaskId(id);
			windowDao.update();
			// 如果任务类型为出库，则立即将任务条目加载到redis中
			if (task.getType() == 1) {
				// 根据套料单、物料类型表生成任务条目
				List<AGVIOTaskItem> taskItems = new ArrayList<AGVIOTaskItem>();
				List<PackingListItem> items = PackingListItem.dao.find(GET_TASK_ITEMS_SQL, id);
				for (PackingListItem item : items) {
					AGVIOTaskItem a = new AGVIOTaskItem(item);
					taskItems.add(a);
				}
				// 把任务条目均匀插入到队列til中
				TaskItemRedisDAO.addTaskItem(taskItems);
			}
			task.setState(2);
			return task.update();
		}
	}


	public boolean cancel(Integer id) {
		Task task = Task.dao.findById(id);
		int state = task.getState();
		boolean untiedWindowflag = true;
		// 判断任务是否处于进行中状态，若是，则把相关的任务条目从til中剔除;若存在已分配的任务条目，则不解绑任务仓口
		if (state == 2) {
			TaskItemRedisDAO.removeUnAssignedTaskItemByTaskId(id);
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getTaskItems()) {
				// 这里加这条if语句是为了判断任务队列中是否还存在该任务对应的任务条目，因为有可能任务队列中还存在不同任务id的任务条目，因此不能根据任务队列中还有任务条目就直接break
				if (redisTaskItem.getTaskId().intValue() == id) {
					untiedWindowflag = false;
					break;
				}
			}
		}
		// 如果任务状态为进行中且该组任务的全部任务条目都已从redis清空，则将任务绑定的仓口解绑
		if (state == 2 && untiedWindowflag) {
			Window window = Window.dao.findById(task.getWindow());
			window.setBindTaskId(null);
			window.update();
		}
		// 更新任务状态为作废
		task.setState(4);
		return task.update();
	}


	public Object check(Integer id, Integer type, Integer pageSize, Integer pageNo) {
		List<IOTaskDetailVO> ioTaskDetailVOs = new ArrayList<IOTaskDetailVO>();
		// 如果任务类型为出入库
		if (type == 0 || type == 1) {
			// 先进行多表查询，查询出同一个任务id的套料单表的id,物料类型表的料号no,套料单表的计划出入库数量quantity,套料单表对应任务的实际完成时间finish_time
			Page<Record> packingListItems = selectService.select(new String[] {"packing_list_item", "material_type"}, new String[] {"packing_list_item.task_id = " + id.toString(), "material_type.id = packing_list_item.material_type_id"}, pageNo, pageSize, null, null, null);

			// 遍历同一个任务id的套料单数据
			for (Record packingListItem : packingListItems.getList()) {
				// 查询task_log中的material_id,quantity
				List<TaskLog> taskLog = TaskLog.dao.find(GET_TASK_ITEM_DETAILS_SQL, id, packingListItem.get("MaterialType_Id"));
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

		else if (type == 2) {		//如果任务类型为盘点
			return null;
		}

		else if (type == 3) {		//如果任务类型为位置优化
			return null;
		}

		return null;
	}

	
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
			default:
				break;
		}
		return windowIds;
	}


	public Object select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		Page<Record> result = selectService.select("task", pageNo, pageSize, ascBy, descBy, filter);

		List<TaskVO> taskVOs = new ArrayList<TaskVO>();
		for (Record res : result.getList()) {
			TaskVO t = new TaskVO(res.get("id"), res.get("state"), res.get("type"), res.get("file_name"), res.get("create_time"));
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


	public void finish(Integer taskId) {
		// 对于入库任务，每执行完一批任务条目会清一次redis，然后调用该接口将任务设置为已完成并解绑仓口，但是可能还有其它任务条目未被扫描加载进redis，因此这里需要判断是否所有任务条目都已经执行完毕
		List<PackingListItem> packListItem = PackingListItem.dao.find(GET_PACKING_LIST_ITEM_SQL, taskId);
		Task task = Task.dao.findById(taskId);
		for (PackingListItem item : packListItem) {
			if (item.getFinishTime() == null && task.getState() == 2) {
				return ;
			}
		}
		if (task.getState() == 2) {
			task.setState(3);
			task.update();
		}
		// 将仓口解绑(作废任务时，如果还有任务条目没跑完就不会解绑仓口，因此不管任务状态是为进行中还是作废，这里都需要解绑仓口)
	    Window window = Window.dao.findById(task.getWindow());
	    window.setBindTaskId(null);
	    window.update();
	}


	public boolean finishItem(Integer packListItemId, Boolean isForceFinish) {
		if (isForceFinish) {
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getTaskItems()) {
				if (redisTaskItem.getId().intValue() == packListItemId) {
					TaskItemRedisDAO.updateTaskIsForceFinish(redisTaskItem, isForceFinish);
					PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
					packingListItem.setFinishTime(new Date());
					packingListItem.update();
				}
			}
		}
		return true;
	}


	public Object getWindowParkingItem(Integer id) {
		for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getTaskItems()) {
			if(redisTaskItem.getState().intValue() == 2) {
				Task task = Task.dao.findFirst(GET_TASK_IN_REDIS_SQL, redisTaskItem.getTaskId());
				if (task.getWindow() == id) {
					Integer packingListItemId = redisTaskItem.getId();

					// 先进行多表查询，查询出仓口id绑定的正在执行中的任务的套料单表的id,套料单文件名，物料类型表的料号no,套料单表的计划出入库数量quantity
					Page<Record> windowParkingListItems = selectService.select(new String[] {"packing_list_item", "material_type", }, new String[] {"packing_list_item.id = " + packingListItemId, "material_type.id = packing_list_item.material_type_id"}, null, null, null, null, null);

					for (Record windowParkingListItem : windowParkingListItems.getList()) {
						// 查询task_log中的material_id,quantity
						List<TaskLog> taskLogs = TaskLog.dao.find(GET_TASK_ITEM_DETAILS_SQL, redisTaskItem.getTaskId(), redisTaskItem.getMaterialTypeId());
						Integer actualQuantity = 0;
						// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
						for (TaskLog tl : taskLogs) {
							actualQuantity += tl.getQuantity();
						}
						WindowParkingListItemVO wp = new WindowParkingListItemVO(windowParkingListItem.get("PackingListItem_Id"), task.getFileName(),  task.getType(), windowParkingListItem.get("MaterialType_No"), windowParkingListItem.get("PackingListItem_Quantity"), actualQuantity);
						wp.setDetails(taskLogs);
						return wp;
					}
				}
			}
		}

		return null;
	}


	public boolean in(Integer packListItemId, String materialId, Integer quantity, Date productionTime, User user) {
		// 根据套料单id，获取对应的任务记录
		PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
		Task task = Task.dao.findById(packingListItem.getTaskId());
		if (TaskLog.dao.find(GET_MATERIAL_ID_IN_SAME_TASK_SQL, materialId, task.getId()).size() != 0) {
			throw new OperationException("时间戳为" + materialId + "的料盘已在同一个任务中被扫描过，请勿在同一个入库任务中重复扫描同一个料盘！");
		}
		if(Material.dao.find(GET_MATERIAL_BY_ID_SQL, materialId).size() != 0) {
			throw new OperationException("时间戳为" + materialId + "的料盘已入过库，请勿重复入库！");
		}

		/*
		 *  新增物料表记录
		 */
		int boxId = 0;
		for (AGVIOTaskItem item : TaskItemRedisDAO.getTaskItems()) {
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

		// 写入一条入库任务日志
		TaskLog taskLog = new TaskLog();
		taskLog.setTaskId(packingListItem.getTaskId());
		taskLog.setMaterialId(materialId);
		taskLog.setQuantity(quantity);
		taskLog.setOperator(user.getUid());
		// 区分入库操作人工还是机器操作,目前的版本暂时先统一写成机器操作
		taskLog.setAuto(true);
		taskLog.setTime(new Date());
		return taskLog.save();
	}


	public boolean out(Integer packListItemId, String materialId, Integer quantity, User user) {
		// 根据套料单id，获取对应的任务记录
		PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
		Task task = Task.dao.findById(packingListItem.getTaskId());
		// 若在同一个出库任务中重复扫同一个料盘时间戳，则抛出OperationException
		if (TaskLog.dao.find(GET_MATERIAL_ID_IN_SAME_TASK_SQL, materialId, task.getId()).size() != 0) {
			throw new OperationException("时间戳为" + materialId + "的料盘已在同一个任务中被扫描过，请勿在同一个出库任务中重复扫描同一个料盘！");
		}

		// 写入一条出库任务日志
		TaskLog taskLog = new TaskLog();
		taskLog.setTaskId(packingListItem.getTaskId());
		taskLog.setMaterialId(materialId);
		taskLog.setQuantity(quantity);
		taskLog.setOperator(user.getUid());
		// 区分出库操作人工还是机器操作,目前的版本暂时先统一写成机器操作
		taskLog.setAuto(true);
		taskLog.setTime(new Date());
		return taskLog.save();
	}


	public boolean deleteMaterialRecord(Integer packListItemId, String materialId) {
		PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
		int taskId = packingListItem.getTaskId();
		Task task = Task.dao.findById(taskId);
		if (task.getType() == 0) {	// 若是入库，则删除掉入库记录，并删除掉物料实体表记录
			Db.update(DELETE_TASK_LOG_SQL, taskId, materialId);
			return Material.dao.deleteById(materialId);
		} else {	// 若是出库，则将物料实体表记录重新置为有效,并删除掉出库记录
			TaskLog taskLog = TaskLog.dao.findFirst(GET_TASK_LOG_SQL, taskId, materialId);
			Material material = Material.dao.findById(materialId);
			material.setRow(0);
			material.setCol(0);
			material.setRemainderQuantity(taskLog.getQuantity());
			material.update();
			return TaskLog.dao.deleteById(taskLog.getId());
		}
	}


	public void updateOutputQuantity(Integer packListItemId, String materialOutputRecords) {
		if (materialOutputRecords != null) {
			PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
			int taskId = packingListItem.getTaskId();

			JSONArray jsonArray = JSONArray.parseArray(materialOutputRecords);

			for (int i=0; i<jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String materialId = jsonObject.getString("materialId");
				Integer quantity = Integer.parseInt(jsonObject.getString("quantity"));

				// 修改任务日志的出库数量
				TaskLog taskLog = TaskLog.dao.findFirst(GET_TASK_LOG_SQL, taskId, materialId);
				taskLog.setQuantity(quantity).update();
				Material material = Material.dao.findById(materialId);
				// 修改物料实体表对应的料盘剩余数量
				int remainderQuantity = material.getRemainderQuantity() - quantity;
				// 若该料盘没有库存了，则将物料实体表记录置为无效
				if (remainderQuantity <= 0) {
					material.setRow(-1);
					material.setCol(-1);
					material.setRemainderQuantity(0);
					material.update();
					continue;
				} else {
					material.setRemainderQuantity(remainderQuantity);
					material.update();
				}
			}
		}

	}


	public Object getWindowTaskItems(Integer id, Integer pageNo, Integer pageSize) {
		if (id != null) {
			Window window = Window.dao.findById(id);
			// 先进行多表查询，查询出仓口id绑定的正在执行中的任务的套料单表的id,套料单文件名，物料类型表的料号no,套料单表的计划出入库数量quantity
			Page<Record> windowTaskItems = selectService.select(new String[] {"packing_list_item", "material_type", }, new String[] {"packing_list_item.task_id = " + window.getBindTaskId(), "material_type.id = packing_list_item.material_type_id"}, pageNo, pageSize, null, null, null);
			List<WindowTaskItemsVO> windowTaskItemsVOs = new ArrayList<WindowTaskItemsVO>();
			int totalRow = 0;
			for (AGVIOTaskItem redisTaskItem : TaskItemRedisDAO.getTaskItems()) {
				if (redisTaskItem.getTaskId().intValue() == window.getBindTaskId()) {
					totalRow += 1;
					for (Record windowTaskItem : windowTaskItems.getList()) {
						// 查询task_log中的material_id,quantity
						List<TaskLog> taskLogs = TaskLog.dao.find(GET_TASK_ITEM_DETAILS_SQL, redisTaskItem.getTaskId(), redisTaskItem.getMaterialTypeId());
						Integer actualQuantity = 0;
						// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
						for (TaskLog tl : taskLogs) {
							actualQuantity += tl.getQuantity();
						}
						if (windowTaskItem.get("PackingListItem_Id").equals(redisTaskItem.getId())) {
							Task task = Task.dao.findFirst(GET_TASK_IN_REDIS_SQL, window.getBindTaskId());
							WindowTaskItemsVO wt = new WindowTaskItemsVO(windowTaskItem.get("PackingListItem_Id"), task.getFileName(), task.getType(), windowTaskItem.get("MaterialType_No"), windowTaskItem.get("PackingListItem_Quantity"), actualQuantity,  windowTaskItem.get("PackingListItem_FinishTime"));
							wt.setDetails(taskLogs);
							windowTaskItemsVOs.add(wt);
						}
					}
				}
			}
			// 分页，设置页码，每页显示条目等
			PagePaginate pagePaginate = new PagePaginate();
			pagePaginate.setPageSize(pageSize);
			pagePaginate.setPageNumber(pageNo);
			pagePaginate.setTotalRow(totalRow);
			pagePaginate.setList(windowTaskItemsVOs);

			return pagePaginate;
		} else {
			return null;
		}

	}

}