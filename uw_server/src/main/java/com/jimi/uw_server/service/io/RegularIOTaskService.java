package com.jimi.uw_server.service.io;

import com.jfinal.aop.Aop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.agv.gaitek.dao.EfficiencyRedisDAO;
import com.jimi.uw_server.agv.gaitek.dao.IOTaskItemRedisDAO;
import com.jimi.uw_server.agv.gaitek.dao.InventoryTaskItemRedisDAO;
import com.jimi.uw_server.agv.gaitek.dao.SampleTaskItemRedisDAO;
import com.jimi.uw_server.agv.gaitek.dao.TaskPropertyRedisDAO;
import com.jimi.uw_server.agv.gaitek.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.gaitek.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.agv.gaitek.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.agv.gaitek.handle.IOTaskHandler;
import com.jimi.uw_server.constant.*;
import com.jimi.uw_server.constant.sql.*;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.lock.RegularTaskLock;
import com.jimi.uw_server.model.*;
import com.jimi.uw_server.model.vo.*;
import com.jimi.uw_server.service.EfficiencyService;
import com.jimi.uw_server.service.ExternalWhLogService;
import com.jimi.uw_server.service.base.BaseIOTaskService;
import com.jimi.uw_server.service.inventory.RegularInventoryTaskService;
import com.jimi.uw_server.ur.dao.UrOperationMaterialInfoDAO;
import com.jimi.uw_server.ur.dao.UrTaskInfoDAO;
import com.jimi.uw_server.ur.entity.UrMaterialInfo;
import com.jimi.uw_server.util.MaterialHelper;
import com.jimi.uw_server.util.PagePaginate;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * 任务业务层
 * 
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class RegularIOTaskService extends BaseIOTaskService {

	private static EfficiencyService efficiencyService = Aop.get(EfficiencyService.class);

	private static ExternalWhLogService externalWhLogService = Aop.get(ExternalWhLogService.class);

	private static final Object UPDATEOUTQUANTITYANDMATERIALINFO_LOCK = new Object();

	private static final String GET_TASK_LOG_BY_TASK_ID_SQL = "SELECT task_log.* FROM task_log INNER JOIN packing_list_item ON task_log.packing_list_item_id = packing_list_item.id WHERE packing_list_item.task_id = ? AND task_log.material_id IS NOT NULL";

	private static final String GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_SQL = "SELECT * FROM task_log WHERE packing_list_item_id = ? AND material_id IS NOT NULL";

	private static final String GET_MATERIAL_BY_BOX_ID = "SELECT * FROM material where box = ? and is_in_box = ? and remainder_quantity > 0";

	private static final String GET_MATERIAL_BY_BOX_SQL = "SELECT * FROM material WHERE box = ? AND remainder_quantity > 0";

	private static final String GET_TASKLOG_BY_PACKINGITEMID = "SELECT * FROM task_log WHERE packing_list_item_id = ? AND quantity > 0";

	private static final String GET_OLDER_MATERIAL_BY_BOX_AND_TIME = "select * from material where box = ? and type = ? and production_time < ? and remainder_quantity > 0 and is_in_box = 1 ORDER BY production_time asc";

	private static final String GET_OLDER_MATERIAL_BY_NO_BOX_AND_TIME = "select * from material where box != ? and type = ? and production_time < ? and remainder_quantity > 0 and is_in_box = 1 ORDER BY production_time asc";

	private static final String GET_FORMER_SUPPLIER_SQL = "SELECT * FROM former_supplier WHERE former_name = ? and supplier_id = ?";

	private static final String GET_TASK_ITEMS_SQL = "SELECT packing_list_item.*, material_type.is_superable FROM packing_list_item INNER JOIN material_type ON material_type.id = packing_list_item.material_type_id WHERE packing_list_item.task_id = ? and material_type.enabled = 1";

	private IOTaskHandler ioTaskHandler = IOTaskHandler.getInstance();

	private static final int UW_ID = 0;

	private static final int EWH_DEDUCT_QUANTITY_LIMIT = 5000;


	// 创建出入库/退料任务
	public void create(Integer type, String fileName, File file, Integer supplier, Integer destination, Boolean isInventoryApply, Integer inventoryTaskId, String remarks, Boolean isForced,
			Boolean isDeducted) throws Exception {
		synchronized (RegularTaskLock.CREATE_IO_LOCK) {
			super.createTask(type, fileName, file, supplier, destination, isInventoryApply, inventoryTaskId, remarks, WarehouseType.REGULAR.getId(), isForced, isDeducted);
		}
	}


	// 令指定任务通过审核
	@Override
	public void pass(Integer id) {
		synchronized (RegularTaskLock.PASS_IO_LOCK) {
			super.pass(id);
		}
	}


	// 令指定任务开始
	public boolean start(Integer id, Integer window, Integer urWindowId) {
		synchronized (RegularTaskLock.START_IO_LOCK) {
			// 根据仓口id查找对应的仓口记录
			Task task = Task.dao.findById(id);
			// 任务仓库为普通UW仓
			if (task.getWarehouseType().equals(WarehouseType.REGULAR.getId()) && !task.getType().equals(TaskType.EMERGENCY_OUT)) {
				if (window != null) {
					Task inventoryTask = Task.dao.findFirst(InventoryTaskSQL.GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER, task.getSupplier(), WarehouseType.REGULAR.getId());
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
					}
					synchronized (Lock.WINDOW_LOCK) {
						List<Window> windows = new ArrayList<Window>(2);
						Window NonUrWindow = Window.dao.findById(window);
						if (NonUrWindow.getBindTaskId() != null) {
							throw new OperationException("该仓口已被占用，请选择其它仓口！");
						}
						windows.add(NonUrWindow);
						// 判断仓口是否被占用
						if (urWindowId != null) {
							Window urWindow = Window.dao.findById(urWindowId);
							if (urWindow.getBindTaskId() != null || !urWindow.getAuto()) {
								throw new OperationException("该仓口已被占用，请选择其它仓口！");
							}
							windows.add(urWindow);
						}
						// 设置仓口，并在仓口表绑定该任务id
						for (Window bingWindow : windows) {
							bingWindow.setBindTaskId(id);
							bingWindow.update();
						}
					}
					// 根据套料单、物料类型表生成任务条目
					List<AGVIOTaskItem> taskItems = new ArrayList<AGVIOTaskItem>();
					List<PackingListItem> items = PackingListItem.dao.find(GET_TASK_ITEMS_SQL, id);
					// 如果任务类型为入库或退料入库，则将任务条目加载到redis中，将任务条目状态设置为不可分配
					if (task.getType() == TaskType.IN || task.getType() == TaskType.SEND_BACK) {
						for (PackingListItem item : items) {
							AGVIOTaskItem a = new AGVIOTaskItem(item, TaskItemState.WAIT_SCAN, 0, item.getBoolean("is_superable"));
							taskItems.add(a);
						}
					} else if (task.getType() == TaskType.OUT) { // 如果任务类型为出库，则将任务条目加载到redis中，将任务条目状态设置为未分配
						for (PackingListItem item : items) {
							AGVIOTaskItem a = new AGVIOTaskItem(item, TaskItemState.WAIT_ASSIGN, 0, item.getBoolean("is_superable"));
							taskItems.add(a);
						}
					}
					// 把任务条目均匀插入到队列til中
					IOTaskItemRedisDAO.addIOTaskItem(task.getId(), taskItems);
					// 将任务状态设置为进行中
					task.setStartTime(new Date()).setState(TaskState.PROCESSING);
					return task.update();
				}
			} else {
				if (task.getType().equals(TaskType.EMERGENCY_OUT)) {
					task.setStartTime(new Date()).setState(TaskState.PROCESSING);
					return task.update();
				}
			}

		}
		return false;
	}


	// 作废指定任务
	public boolean cancel(Integer id) {
		synchronized (RegularTaskLock.CANCEL_IO_LOCK) {
			Task task = Task.dao.findById(id);
			int state = task.getState();
			// 对于已完成的任务，禁止作废
			if (state == TaskState.FINISHED) {
				throw new OperationException("该任务已完成，禁止作废！");
			}
			if (state == TaskState.CANCELED) { // 对于已作废过的任务，禁止作废
				throw new OperationException("该任务已作废！");
			}
			if (task.getStartTime() == null) {
				task.setStartTime(new Date());
			}
			if (task.getWarehouseType().equals(WarehouseType.REGULAR.getId()) && !task.getType().equals(TaskType.EMERGENCY_OUT)) {

				boolean untiedWindowflag = true;
				// 判断任务是否处于进行中状态，若是，则把相关的任务条目从til中剔除;若存在已分配的任务条目，则不解绑任务仓口
				if (state == TaskState.PROCESSING) {
					IOTaskItemRedisDAO.removeUnAssignedTaskItemByTaskId(id);
					List<AGVIOTaskItem> redisTaskItems = IOTaskItemRedisDAO.getIOTaskItems(task.getId());
					if (redisTaskItems != null && redisTaskItems.size() > 0) {
						untiedWindowflag = false;
					}
				}
				// 如果任务状态为进行中且该组任务的全部任务条目都已从redis清空，则将任务绑定的仓口解绑
				if (state == TaskState.PROCESSING && untiedWindowflag) {
					synchronized (Lock.WINDOW_LOCK) {
						List<Window> windows = Window.dao.find(WindowSQL.GET_WINDOW_BY_TASK, task.getId());
						for (Window window : windows) {
							List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, window.getId());
							for (GoodsLocation goodsLocation : goodsLocations) {
								TaskPropertyRedisDAO.delLocationStatus(window.getId(), goodsLocation.getId());
							}
							window.setBindTaskId(null).update();
						}
					}

				}
				// 更新任务状态为作废
				ioTaskHandler.clearTask(task.getId(), true);
				task.setEndTime(new Date()).setState(TaskState.CANCELED).update();
				return true;
			} else if (task.getWarehouseType().equals(WarehouseType.REGULAR.getId()) && task.getType().equals(TaskType.EMERGENCY_OUT)) {
				List<TaskLog> taskLogs = TaskLog.dao.find(GET_TASK_LOG_BY_TASK_ID_SQL, task.getId());
				List<Material> materials = new ArrayList<>();
				if (!taskLogs.isEmpty()) {
					for (TaskLog taskLog : taskLogs) {
						Material material = Material.dao.findById(taskLog.getMaterialId());
						materials.add(material);
						taskLog.delete();
					}
				}
				if (!materials.isEmpty()) {
					for (Material material : materials) {
						material.delete();
					}
				}
				// 更新任务状态为作废
				task.setEndTime(new Date()).setState(TaskState.CANCELED).update();
				return true;
			}
			return false;
		}


	}


	public void exportIOTaskDetails(Integer id, Integer type, String fileName, OutputStream output) throws IOException {
		// 如果任务类型为出入库
		super.exportIOTaskDetails(id, type, fileName, output);
	}


	public PagePaginate check(Integer id, Integer type, Integer pageSize, Integer pageNo) {
		return super.check(id, type, pageSize, pageNo);
	}


	// 查询所有任务
	public PagePaginate select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		return super.select(pageNo, pageSize, ascBy, descBy, filter);
	}


	// 完成任务
	public void finish(Integer taskId) {
		Task task = Task.dao.findById(taskId);
		List<PackingListItem> unfinishItems = PackingListItem.dao.find(IOTaskSQL.GET_UNFINISH_PACKING_LIST_ITEM, task.getId());
		setIOTaskMaterialEmpty(task, unfinishItems);
		if (task.getType().equals(TaskType.OUT)) {
			countAndSetDiffQuantityIOTaskItem(task, unfinishItems, true, true);
		}
		Boolean isLack = false;

		List<PackingListItem> packingListItems = PackingListItem.dao.find(SQL.GET_ALL_TASK_ITEM_BY_TASK_ID, task.getId());
		for (PackingListItem packingListItem : packingListItems) {
			TaskLog taskLog = TaskLog.dao.findFirst(SQL.GET_OUT_QUANTITY_BY_PACKINGITEMID, packingListItem.getId());
			Integer uwQuantity = taskLog.getInt("totalQuantity") == null ? 0 : taskLog.getInt("totalQuantity");
			Integer deductQuantity = externalWhLogService.getDeductEwhMaterialQuantityByOutTask(task.getId(), packingListItem.getMaterialTypeId());
			if (deductQuantity <= 0 && packingListItem.getQuantity() > (uwQuantity - deductQuantity)) {
				isLack = true;
				break;
			}
		}
		if (task.getState() == TaskState.PROCESSING && !isLack) {
			task.setEndTime(new Date()).setState(TaskState.FINISHED);
			task.update();
		} else if (task.getState() == TaskState.PROCESSING && isLack) {
			task.setEndTime(new Date()).setState(TaskState.EXIST_LACK);
			task.update();
		} else {
			task.setEndTime(new Date()).update();
		}
		// 将仓口解绑(作废任务时，如果还有任务条目没跑完就不会解绑仓口，因此不管任务状态是为进行中还是作废，这里都需要解绑仓口)
		unbindWindow(task);
	}


	// 作废任务处理
	public void cancelRegualrTask(Integer taskId) {
		Task task = Task.dao.findById(taskId);
		List<PackingListItem> unfinishItems = PackingListItem.dao.find(IOTaskSQL.GET_UNFINISH_PACKING_LIST_ITEM, task.getId());
		setIOTaskMaterialEmpty(task, unfinishItems);
		countAndSetDiffQuantityIOTaskItem(task, unfinishItems, true, false);
		task.setState(TaskState.CANCELED).setEndTime(new Date()).update();
		unbindWindow(task);
		// 将仓口解绑(作废任务时，如果还有任务条目没跑完就不会解绑仓口，因此不管任务状态是为进行中还是作废，这里都需要解绑仓口)
	}


	public List<Task> getEmergencyRegularTasks() {
		List<Task> tasks = Task.dao.find(IOTaskSQL.GET_TASK_BY_TYPE, TaskType.EMERGENCY_OUT, TaskState.PROCESSING, WarehouseType.REGULAR.getId());
		return tasks;
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
		for (AGVIOTaskItem redisTaskItem : IOTaskItemRedisDAO.getIOTaskItems(window.getBindTaskId())) {
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
				Page<Record> windowParkingListItems = selectService.select(new String[] { "packing_list_item", "material_type", "supplier", "task" },
						new String[] { "packing_list_item.id = " + packingListItemId, "material_type.id = packing_list_item.material_type_id", "material_type.supplier = supplier.id",
								"packing_list_item.task_id = task.id" },
						null, null, null, null, null);
				if (!windowParkingListItems.getList().isEmpty() && windowParkingListItems.getList().size() == 1) {
					Record windowParkingListItem = windowParkingListItems.getList().get(0);
					Integer actualQuantity = 0;
					List<PackingListItemDetailsVO> packingListItemDetailsVOs = new ArrayList<PackingListItemDetailsVO>();
					for (TaskLog tl : taskLogs) {
						// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
						actualQuantity += tl.getQuantity();
						// 封装任务条目详情数据
						PackingListItemDetailsVO pl = new PackingListItemDetailsVO(tl.getId(), tl.get("materialId"), tl.getQuantity(), tl.get("remainderQuantity"), tl.get("productionTime"),
								tl.get("isInBox"), tl.getInt("row"), tl.getInt("col"), tl.getInt("boxId"));
						packingListItemDetailsVOs.add(pl);
					}
					Integer eWhStoreQuantity = 0;
					if (task.getIsInventoryApply() != null && task.getIsInventoryApply()) {
						Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
						eWhStoreQuantity = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination(), inventoryTask.getCreateTime());
					} else {
						if (!task.getType().equals(TaskType.IN)) {
							Task inventoryTask = inventoryTaskService.getOneUnStartInventoryTask(task.getSupplier(), task.getWarehouseType(), task.getDestination());
							if (inventoryTask != null) {
								eWhStoreQuantity = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination())
										- externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination(), inventoryTask.getCreateTime());
							} else {
								eWhStoreQuantity = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination());
							}
						} else {
							eWhStoreQuantity = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination());
						}
					}
					List<Material> materials1 = Material.dao.find(SQL.GET_MATERIAL_BY_TYPE_AND_BOX, redisTaskItem.getMaterialTypeId(), redisTaskItem.getBoxId());
					Integer reelNum = materials1.size();
					List<Material> materials2 = null;
					if (reelNum > 0 && task.getType().equals(TaskType.OUT)) {
						Material materialTemp2 = Material.dao.findFirst(GET_OLDER_MATERIAL_BY_NO_BOX_AND_TIME, redisTaskItem.getBoxId(), redisTaskItem.getMaterialTypeId(),
								materials1.get(0).getProductionTime());
						if (materialTemp2 == null) {
							materials2 = Material.dao.find(SQL.GET_MATERIALS_BY_TIME_AND_BOX, redisTaskItem.getMaterialTypeId(), redisTaskItem.getBoxId(), materials1.get(0).getProductionTime());
						}
					}
					Integer uwStoreQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(redisTaskItem.getMaterialTypeId());
					windowParkingListItemVO.fill(packingListItemDetailsVOs, windowParkingListItem, eWhStoreQuantity, uwStoreQuantity, actualQuantity, reelNum, redisTaskItem.getIsForceFinish(),
							redisTaskItem.getBoxId(), materials2);

				}
			}

		}
		if (map.isEmpty()) {
			return null;
		}

		return new ArrayList<>(map.values());
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
			Map<Integer, String> map = new HashMap<>();
			List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, id);
			for (GoodsLocation goodsLocation : goodsLocations) {
				map.put(goodsLocation.getId(), goodsLocation.getName());
			}
			map.put(0, "无");
			// 先进行多表查询，查询出仓口id绑定的正在执行中的任务的套料单表的id，套料单文件名，物料类型表的料号no，套料单表的计划出入库数量quantity
			Page<Record> windowTaskItems = selectService.select(new String[] { "packing_list_item", "material_type", },
					new String[] { "packing_list_item.task_id = " + window.getBindTaskId(), "material_type.id = packing_list_item.material_type_id" }, null, null, null, null, null);

			List<WindowTaskItemsVO> windowTaskItemVOs = new ArrayList<WindowTaskItemsVO>();
			Task bindInventoryTask = null;
			if (task.getIsInventoryApply() != null && task.getIsInventoryApply()) {
				bindInventoryTask = Task.dao.findById(task.getInventoryTaskId());
			}
			Task unstartInventoryTask = inventoryTaskService.getOneUnStartInventoryTask(task.getSupplier(), task.getWarehouseType(), task.getDestination());
			int startLine = (pageNo - 1) * pageSize;
			int endLine = (pageNo * pageSize) - 1;
			List<AGVIOTaskItem> items = IOTaskItemRedisDAO.getIOTaskItems(window.getBindTaskId(), startLine, endLine);
			if (items.isEmpty()) {
				return null;
			}
			Map<Integer, Record> taskItemMap = new HashMap<Integer, Record>();
			for (Record windowTaskItem : windowTaskItems.getList()) {
				taskItemMap.put(windowTaskItem.getInt("PackingListItem_Id"), windowTaskItem);
			}
			for (AGVIOTaskItem redisTaskItem : items) {
				// 查询task_log中的material_id,quantity
				List<TaskLog> taskLogs = TaskLog.dao.find(IOTaskSQL.GET_TASK_ITEM_DETAILS_SQL, redisTaskItem.getId());
				Integer actualQuantity = 0;
				// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
				for (TaskLog tl : taskLogs) {
					actualQuantity += tl.getQuantity();
				}
				Integer eWhStoreQuantity = 0;
				if (task.getType().equals(TaskType.OUT)) {
					if (bindInventoryTask != null) {
						eWhStoreQuantity = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination(), bindInventoryTask.getCreateTime());
					} else {
						if (unstartInventoryTask != null) {
							eWhStoreQuantity = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination())
									- externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination(), unstartInventoryTask.getCreateTime());
						} else {
							eWhStoreQuantity = externalWhLogService.getEWhMaterialQuantity(redisTaskItem.getMaterialTypeId(), task.getDestination());
						}
					}
				}
				Record windowTaskItem = taskItemMap.get(redisTaskItem.getId());
				WindowTaskItemsVO wt = new WindowTaskItemsVO(windowTaskItem.getInt("PackingListItem_Id"), task.getFileName(), task.getType(), windowTaskItem.getStr("MaterialType_No"),
						windowTaskItem.getInt("PackingListItem_Quantity"), actualQuantity, windowTaskItem.getDate("PackingListItem_FinishTime"), redisTaskItem.getState(), redisTaskItem.getBoxId(),
						redisTaskItem.getGoodsLocationId(), map.get(redisTaskItem.getGoodsLocationId()), redisTaskItem.getRobotId(), eWhStoreQuantity);
				wt.setDetails(taskLogs);
				windowTaskItemVOs.add(wt);
			}
			// 分页，设置页码，每页显示条目等
			PagePaginate pagePaginate = new PagePaginate();
			pagePaginate.setPageSize(pageSize);
			pagePaginate.setPageNumber(pageNo);
			pagePaginate.setTotalRow(windowTaskItems.getTotalRow());
			pagePaginate.setList(windowTaskItemVOs);
			return pagePaginate;
		} else {
			return null;
		}

	}


	// 新增入库料盘记录并写入库任务日志记录
	public Material in(Integer packListItemId, String materialId, Integer quantity, Date productionTime, String supplierName, String cycle, String manufacturer, Date printTime, User user) {
		synchronized (RegularTaskLock.IN_SCAN_IO_LOCK) {
			// 通过任务条目id获取套料单记录
			PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
			if (packingListItem == null) {
				throw new OperationException("扫码错误，任务条目不存在");
			}
			Task task = Task.dao.findById(packingListItem.getTaskId());
			if (!TaskPropertyRedisDAO.getTaskStatus(task.getId())) {
				throw new OperationException("扫码无效，任务处于暂停状态！");
			}
			// 通过套料单记录获取物料类型id
			MaterialType materialType = MaterialType.dao.findById(packingListItem.getMaterialTypeId());
			// 通过物料类型获取对应的客户id
			Integer supplierId = materialType.getSupplier();
			// 通过客户id获取客户名
			Supplier supplier = Supplier.dao.findById(supplierId);
			String sName = supplier.getName();
			int materialBoxCapacity = PropKit.use("properties.ini").getInt("materialBoxCapacity");
			FormerSupplier formerSupplier = FormerSupplier.dao.findFirst(GET_FORMER_SUPPLIER_SQL, supplierName, materialType.getSupplier());
			if (!supplierName.equals(sName) && formerSupplier == null) {
				throw new OperationException("扫码错误，客户 " + supplierName + " 对应的任务目前没有在本仓口进行任务，" + "本仓口已绑定 " + sName + " 的任务单！");
			}
			if (TaskLog.dao.find(IOTaskSQL.GET_MATERIAL_ID_IN_SAME_TASK_SQL, materialId, packListItemId).size() != 0) {
				throw new OperationException("时间戳为" + materialId + "的料盘已在同一个任务中被扫描过，请勿在同一个入库任务中重复扫描同一个料盘！");
			}
			Material material = Material.dao.findById(materialId);
			if (material != null && !material.getIsRepeated()) {
				throw new OperationException("时间戳为" + materialId + "的料盘已入过库，请勿重复入库！");
			}
			// 新增物料表记录
			AGVIOTaskItem item = IOTaskItemRedisDAO.getIOTaskItem(packingListItem.getTaskId(), packingListItem.getId());
			if (item == null || item.getState() != TaskItemState.ARRIVED_WINDOW) {
				throw new OperationException("任务条目不存在或者尚未到站！");
			}
			int boxId = item.getBoxId().intValue();
			TaskLog taskLog = TaskLog.dao.findFirst(SQL.GET_OUT_QUANTITY_BY_PACKINGITEMID, packingListItem.getId());
			if (taskLog != null && taskLog.getInt("totalQuantity") != null && taskLog.getInt("totalQuantity") >= packingListItem.getQuantity()) {
				throw new OperationException("扫描数量足够！");
			}
			Integer reelNum = 0;
			if (materialType.getRadius().equals(7)) {
				List<Material> materials = Material.dao.find(GET_MATERIAL_BY_BOX_SQL, boxId);
				reelNum = materials.size();
				if (reelNum == materialBoxCapacity) {
					throw new OperationException("当前料盒已满，请停止扫码！");
				}
			}
			material = putInMaterialToDb(material, materialId, boxId, quantity, productionTime, printTime, materialType.getId(), cycle, manufacturer, MaterialStatus.NORMAL, supplier.getCompanyId());
			if (materialType.getRadius().equals(7)) {

				if (reelNum > 0) {
					MaterialHelper.getMaterialLocation(material, false);
				} else {
					Material material2 = Material.dao.findFirst(SQL.GET_MATERIAL_WITH_LOCATION_BY_SUPPLIER, supplierId);
					Material material3 = Material.dao.findFirst(SQL.GET_MATERIAL_BY_SUPPLIER, supplierId, material.getId(), MaterialBoxType.STANDARD);
					if (material2 != null || material3 == null) {
						MaterialHelper.getMaterialLocation(material, true);
					} else {
						MaterialHelper.getMaterialLocation(material, false);
					}
				}
			}
			Integer efficiencySwitch = PropKit.use("properties.ini").getInt("efficiencySwitch");
			if (efficiencySwitch != null && efficiencySwitch == 1) {
				int inOperationType = 0;
				int boxType = 0;
				Long userLastOperationTime = efficiencyService.getUserLastOperationTime(task.getId(), boxId, user.getUid());
				if (materialType.getRadius().equals(7)) {
					putEfficiencyTimeToDb(userLastOperationTime, boxType, user, inOperationType, task.getId(), boxId);
				} else {
					boxType = 1;
					putEfficiencyTimeToDb(userLastOperationTime, boxType, user, inOperationType, task.getId(), boxId);
				}
			}
			createTaskLog(packListItemId, materialId, quantity, user, task);
			return material;
		}
	}


	// 写出库任务日志
	public boolean out(Integer packListItemId, String materialId, Integer quantity, String supplierName, User user) {
		synchronized (RegularTaskLock.OUT_SCAN_IO_LOCK) {
			// 通过任务条目id获取套料单记录
			PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
			if (packingListItem == null) {
				throw new OperationException("扫码错误，任务条目不存在");
			}
			Task task = Task.dao.findById(packingListItem.getTaskId());
			if (!TaskPropertyRedisDAO.getTaskStatus(task.getId())) {
				throw new OperationException("扫码无效，任务处于暂停状态！");
			}
			// 通过套料单记录获取物料类型id
			MaterialType materialType = MaterialType.dao.findById(packingListItem.getMaterialTypeId());
			// 通过物料类型获取对应的客户id
			Integer supplierId = materialType.getSupplier();
			// 通过客户id获取客户名
			Supplier supplier = Supplier.dao.findById(supplierId);
			String sName = supplier.getName();
			FormerSupplier formerSupplier = FormerSupplier.dao.findFirst(GET_FORMER_SUPPLIER_SQL, supplierName, materialType.getSupplier());
			if (!supplierName.equals(sName) && formerSupplier == null) {
				throw new OperationException("扫码错误，客户 " + supplierName + " 对应的任务目前没有在本仓口进行任务，" + "本仓口已绑定 " + sName + " 的任务单！");
			}
			Material material = Material.dao.findById(materialId);
			// 若扫描的料盘记录不存在于数据库中或不在盒内，则抛出OperationException
			if (material == null || !material.getIsInBox()) {
				throw new OperationException("时间戳为" + materialId + "的料盘没有入过库或者不在盒内，不能对其进行出库操作！");
			}
			// 对于不在已到站料盒的物料，禁止对其进行操作
			if (!packingListItem.getMaterialTypeId().equals(material.getType())) {
				throw new OperationException("时间戳为" + materialId + "的料盘并非当前出库料号，不能对其进行出库操作！");
			}
			AGVIOTaskItem item = IOTaskItemRedisDAO.getIOTaskItem(packingListItem.getTaskId(), packingListItem.getId());
			if (item == null || item.getState() != TaskItemState.ARRIVED_WINDOW) {
				throw new OperationException("任务条目不存在或者尚未到站！");
			}
			if (item.getBoxId().intValue() != material.getBox().intValue()) {
				throw new OperationException("时间戳为" + materialId + "的料盘不在该料盒中，无法对其进行出库操作！");
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
				if (materialTemp1.getCycle() == null || materialTemp1.getCycle().trim().equals("")) {
					throw new OperationException("当前料盒存在更旧的物料，日期是 " + materialTemp1.getPrintTime() + "！");
				}
				throw new OperationException("当前料盒存在更旧的物料，周期是 " + materialTemp1.getCycle() + "！");
			}
			// 若在同一个出库任务中重复扫同一个料盘时间戳，则抛出OperationException
			if (TaskLog.dao.find(IOTaskSQL.GET_MATERIAL_ID_IN_SAME_TASK_SQL, materialId, packListItemId).size() != 0) {
				throw new OperationException("时间戳为" + materialId + "的料盘已在同一个任务中被扫描过，请勿在同一个出库任务中重复扫描同一个料盘！");
			}

			// 判断物料二维码中包含的料盘数量信息是否与数据库中的料盘剩余数相匹配
			Integer remainderQuantity = Material.dao.findById(materialId).getRemainderQuantity();
			if (remainderQuantity.intValue() != quantity) {
				throw new OperationException("时间戳为" + materialId + "的料盘数量与数据库中记录的料盘剩余数量不一致，请扫描正确的料盘二维码！");
			}
			Window window = Window.dao.findById(item.getWindowId());
			if (window.getAuto()) {
				// 手动扫描本应机械臂扫描的物料
				List<UrMaterialInfo> urMaterialInfos = UrTaskInfoDAO.getUrMaterialInfos(item.getTaskId(), item.getBoxId());
				if (!urMaterialInfos.isEmpty() && urMaterialInfos.size() > 0) {
					boolean isScanFlag = false;
					for (UrMaterialInfo urMaterialInfo : urMaterialInfos) {
						if (urMaterialInfo.getIsScaned()) {
							continue;
						}
						if (!urMaterialInfo.getMaterialId().equals(materialId)) {
							continue;
						}

						urMaterialInfo.setIsScaned(true);
						urMaterialInfo.setExceptionCode(0);
						isScanFlag = true;
					}
					if (isScanFlag) {
						UrTaskInfoDAO.putUrMaterialInfos(item.getTaskId(), item.getBoxId(), urMaterialInfos);
						UrOperationMaterialInfoDAO.removeUrTaskBoxArrivedPack("robot1");
					}
				}

			} else {
				Integer efficiencySwitch = PropKit.use("properties.ini").getInt("efficiencySwitch");
				if (efficiencySwitch != null && efficiencySwitch == 1) {
					Long userLastOperationTime = efficiencyService.getUserLastOperationTime(task.getId(), material.getBox(), user.getUid());
					System.out.println("end:" + userLastOperationTime);
					int outOperationType = 1;
					int boxType = 0;
					if (materialType.getRadius().equals(7)) {
						putEfficiencyTimeToDb(userLastOperationTime, boxType, user, outOperationType, task.getId(), material.getBox());
					} else {
						boxType = 1;
						putEfficiencyTimeToDb(userLastOperationTime, boxType, user, outOperationType, task.getId(), material.getBox());
					}
				}
			}


			// 扫码出库后，将料盘设置为不在盒内
			material.setIsInBox(false).setStatus(MaterialStatus.OUTTING).update();
			createTaskLog(packListItemId, materialId, quantity, user, task);
			return true;
		}
	}


	public void urOut(UrMaterialInfo urMaterialInfo) {
		synchronized (RegularTaskLock.OUT_SCAN_IO_LOCK) {
			Task task = Task.dao.findById(urMaterialInfo.getTaskId());
			Material material = Material.dao.findById(urMaterialInfo.getMaterialId());
			// 若扫描的料盘记录不存在于数据库中或不在盒内，则抛出OperationException
			if (material == null || !material.getIsInBox()) {
				throw new OperationException("时间戳为" + material.getId() + "的料盘没有入过库或者不在盒内，不能对其进行出库操作！");
			}

			// 扫码出库后，将料盘设置为不在盒内
			material.setIsInBox(false).setStatus(MaterialStatus.OUTTING).update();
			createAutoTaskLog(urMaterialInfo.getIoItemId(), material.getId(), material.getRemainderQuantity(), "robot1 ", task);
		}
	}


	// 写发料区紧急出库任务日志
	public boolean outEmergencyRegular(String taskId, String no, String materialId, Integer quantity, Date productionTime, String supplierName, String cycle, String manufacturer, Date printTime,
			User user) {
		synchronized (RegularTaskLock.OUT_SCAN_IO_LOCK) {
			Task task = Task.dao.findById(taskId);
			if (task == null || !task.getType().equals(TaskType.EMERGENCY_OUT)) {
				throw new OperationException("任务不存在，或者任务非紧急手动出库任务！");
			}
			MaterialType materialType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_AND_TYPE_SQL, no.trim().toUpperCase(), task.getSupplier(),
					WarehouseType.REGULAR.getId());
			if (materialType == null) {
				throw new OperationException("普通仓不存在该物料类型");
			}
			// 通过任务条目id获取套料单记录
			PackingListItem packingListItem = PackingListItem.dao.findFirst(IOTaskSQL.GET_PACKING_LIST_ITEM_BY_TASKID_AND_NO, taskId, no.trim().toUpperCase(), WarehouseType.REGULAR.getId());
			if (packingListItem == null) {
				throw new OperationException("任务条目不存在，该物料不需要出库");
			}
			// 通过物料类型获取对应的客户id
			Integer supplierId = materialType.getSupplier();
			// 通过客户id获取客户名
			String sName = Supplier.dao.findById(supplierId).getName();
			FormerSupplier formerSupplier = FormerSupplier.dao.findFirst(GET_FORMER_SUPPLIER_SQL, supplierName, materialType.getSupplier());
			if (!supplierName.equals(sName) && formerSupplier == null) {
				throw new OperationException("扫码错误，客户 " + supplierName + " 非该任务所属的客户！");
			}
			Material material = Material.dao.findById(materialId);
			if (material != null) {
				throw new OperationException("料盘已存在，该物料无法紧急出库，请走常规流程！");
			}
			material = new Material();

			// 扫码出库后，将料盘设置为不在盒内
			material.setId(materialId).setType(materialType.getId()).setStoreTime(new Date()).setRemainderQuantity(0).setCycle(cycle).setManufacturer(manufacturer).setProductionTime(productionTime)
					.setPrintTime(printTime).setIsInBox(false).setStatus(MaterialStatus.NORMAL).save();

			// 写入一条出库任务日志
			TaskLog taskLog = new TaskLog();
			taskLog.setPackingListItemId(packingListItem.getId());
			taskLog.setMaterialId(material.getId());
			taskLog.setQuantity(quantity);
			taskLog.setOperator(user.getUid());
			// 区分出库操作人工还是机器操作,目前的版本暂时先统一写成人工操作
			taskLog.setAuto(false);
			taskLog.setTime(new Date());
			taskLog.setDestination(task.getDestination());
			return taskLog.save();
		}
	}


	public void finishEmergencyRegularTask(Integer taskId, User user) {

		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.EMERGENCY_OUT)) {
			throw new OperationException("任务不存在，或者任务非紧急手动出库任务！");
		}
		String taskName = task.getFileName() + "_入库";
		Task newTask = new Task();
		newTask.setState(TaskState.PROCESSING).setSupplier(task.getSupplier()).setRemarks(task.getRemarks()).setStartTime(task.getStartTime()).setType(TaskType.IN).setFileName(taskName)
				.setCreateTime(task.getCreateTime()).setWarehouseType(WarehouseType.REGULAR.getId()).save();
		List<PackingListItem> packingListItems = PackingListItem.dao.find(IOTaskSQL.GET_PACKING_LIST_ITEM_BY_TASKID, taskId);
		if (!packingListItems.isEmpty()) {
			for (PackingListItem packingListItem : packingListItems) {
				PackingListItem newPackingListItem = new PackingListItem();
				newPackingListItem.setQuantity(packingListItem.getQuantity()).setTaskId(newTask.getId()).setMaterialTypeId(packingListItem.getMaterialTypeId())
						.setFinishTime(packingListItem.getFinishTime()).save();
				Integer newPackingListItemId = newPackingListItem.getId();
				List<TaskLog> taskLogs = TaskLog.dao.find(GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_SQL, packingListItem.getId());
				int quantity = 0;
				if (!taskLogs.isEmpty()) {
					for (TaskLog taskLog : taskLogs) {
						TaskLog newTaskLog = new TaskLog();
						quantity = quantity + taskLog.getQuantity();
						newTaskLog.setPackingListItemId(newPackingListItemId).setAuto(taskLog.getAuto()).setQuantity(taskLog.getQuantity()).setMaterialId(taskLog.getMaterialId())
								.setOperator(taskLog.getOperator()).setTime(taskLog.getTime()).save();
					}
				}
				packingListItem.setFinishTime(new Date()).update();
				newPackingListItem.setQuantity(quantity).setFinishTime(new Date()).update();
				if (!packingListItem.getQuantity().equals(quantity)) {
					if (packingListItem.getQuantity() < quantity) {
						// 超发
						ExternalWhLog externalWhLog = new ExternalWhLog();
						externalWhLog.setMaterialTypeId(packingListItem.getMaterialTypeId());
						externalWhLog.setDestination(task.getDestination());
						externalWhLog.setSourceWh(UW_ID);
						externalWhLog.setTaskId(task.getId());
						externalWhLog.setQuantity(quantity - packingListItem.getQuantity());
						if (task.getIsInventoryApply()) {
							Task inventoryTask = Task.dao.findById(task.getInventoryTaskId());
							externalWhLog.setTime(inventoryTask.getCreateTime());
						} else {
							externalWhLog.setTime(new Date());
						}
						externalWhLog.setOperationTime(new Date());
						externalWhLog.setOperatior(user.getUid());
						externalWhLog.save();
					} else if (packingListItem.getQuantity() > quantity) {
						// 欠发
						if (externalWhLogService.getEWhMaterialQuantity(packingListItem.getMaterialTypeId(), task.getDestination()) > 0) {
							ExternalWhLog externalWhLog = new ExternalWhLog();
							externalWhLog.setMaterialTypeId(packingListItem.getMaterialTypeId());
							externalWhLog.setDestination(task.getDestination());
							externalWhLog.setSourceWh(UW_ID);
							externalWhLog.setTaskId(task.getId());
							int lackNum = packingListItem.getQuantity() - quantity;

							int storeNum = 0;
							List<Task> inventoryTasks = RegularInventoryTaskService.me.getUnStartInventoryTask(task.getSupplier(), WarehouseType.REGULAR.getId(), task.getDestination());
							if (inventoryTasks.size() > 0) {
								storeNum = externalWhLogService.getEWhMaterialQuantity(packingListItem.getMaterialTypeId(), task.getDestination())
										- externalWhLogService.getEWhMaterialQuantity(packingListItem.getMaterialTypeId(), task.getDestination(), inventoryTasks.get(0).getCreateTime());

							} else {
								storeNum = externalWhLogService.getEWhMaterialQuantity(packingListItem.getMaterialTypeId(), task.getDestination());
							}
							if (storeNum > lackNum) {
								externalWhLog.setQuantity(0 - lackNum);
							} else if (storeNum <= 0) {
								externalWhLog.setQuantity(0);
							} else {
								externalWhLog.setQuantity(0 - storeNum);
							}
							externalWhLog.setTime(new Date());
							externalWhLog.setOperationTime(new Date());
							externalWhLog.setOperatior(user.getUid());
							externalWhLog.save();
						}
					}
				}
			}
		}
		task.setEndTime(new Date()).setState(TaskState.FINISHED).update();
		newTask.setEndTime(new Date()).setState(TaskState.FINISHED).update();

	}


	// 删除错误的料盘记录
	public Material deleteRegularMaterialRecord(Integer packListItemId, String materialId) {
		PackingListItem packingListItem = PackingListItem.dao.findById(packListItemId);
		int taskId = packingListItem.getTaskId();
		Task task = Task.dao.findById(taskId);
		Material material = Material.dao.findById(materialId);
		// 对于不在已到站料盒的物料，禁止对其进行操作
		AGVIOTaskItem agvioTaskItem = IOTaskItemRedisDAO.getIOTaskItem(taskId, packingListItem.getId());
		if (agvioTaskItem == null || agvioTaskItem.getState() != TaskItemState.ARRIVED_WINDOW) {
			throw new OperationException("任务条目不存在或者尚未到站！");
		}
		if (agvioTaskItem.getBoxId().intValue() != material.getBox().intValue()) {
			throw new OperationException("时间戳为" + materialId + "的料盘不在该料盒中，禁止删除！");
		}
		if (task.getType() == TaskType.IN || task.getType() == TaskType.SEND_BACK) { // 若是入库或退料入库任务，则删除掉入库记录，并删除掉物料实体表记录
			TaskLog taskLog = TaskLog.dao.findFirst(IOTaskSQL.GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, packListItemId, materialId);
			if (!material.getIsInBox() || !material.getRemainderQuantity().equals(taskLog.getQuantity())) {
				throw new OperationException("时间戳为" + materialId + "的料盘已被出库，禁止删除！");
			}
			SampleOutRecord sampleOutRecord = SampleOutRecord.dao.findFirst(SampleTaskSQL.GET_SAMPLE_OUT_RECORD_BY_MATERIALID, materialId);
			if (sampleOutRecord != null) {
				throw new OperationException("抽检出库或者丢失的料盘入库后，禁止删除！");
			}
			MaterialBox materialBox = MaterialBox.dao.findById(material.getBox());
			Db.update(IOTaskSQL.DELETE_TASK_LOG_SQL, packListItemId, materialId);
			Integer efficiencySwitch = PropKit.use("properties.ini").getInt("efficiencySwitch");
			if (efficiencySwitch != null && efficiencySwitch == 1) {
				int boxType = 0;
				int operationType = 0;
				if (materialBox.getType().equals(MaterialBoxType.NONSTANDARD)) {
					boxType = 1;
				}
				putAndReduceDishNumToDb(taskId, material.getBox(), boxType, taskLog.getOperator(), operationType);
			}
			Material.dao.deleteById(materialId);

			return material;
		} else { // 若是出库任务，删除掉出库记录；若已经执行过删除操作，则将物料实体表对应的料盘记录还原
			TaskLog taskLog = TaskLog.dao.findFirst(IOTaskSQL.GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, packListItemId, materialId);
			Record record = Db.findFirst(SQL.GET_NEWEST_MATERIAL_TASKLOG_BY_ITEM_ID_SQL, packingListItem);
			if (record != null && !record.getDate("production_time").equals(material.getProductionTime())) {
				throw new OperationException("时间戳为" + materialId + "的料盘并非当前出库记录中最新的料盘，禁止删除！");
			}
			MaterialBox materialBox = MaterialBox.dao.findById(material.getBox());
			material.setIsInBox(true).setStatus(MaterialStatus.NORMAL).setCutTaskLogId(null).update();
			Integer efficiencySwitch = PropKit.use("properties.ini").getInt("efficiencySwitch");
			if (efficiencySwitch != null && efficiencySwitch == 1) {
				int boxType = 0;
				int operationType = 1;
				if (materialBox.getType().equals(MaterialBoxType.NONSTANDARD)) {
					boxType = 1;
				}
				putAndReduceDishNumToDb(taskId, material.getBox(), boxType, taskLog.getOperator(), operationType);
			}
			TaskLog.dao.deleteById(taskLog.getId());
			// 判断删除的是否是截料的那一盘，是的话，消除其截料状态
			record = Db.findFirst(SQL.GET_CUT_MATERIAL_RECORD_SQL, packListItemId);
			if (record == null) {
				IOTaskItemRedisDAO.updateIOTaskItemInfo(agvioTaskItem, null, null, null, null, null, null, false, null, null, null);
			}
			return material;
		}
	}


	// 删除错误的料盘记录
	public void deleteEmergencyRegularMaterialRecord(Integer taskLogId, String materialId) {
		TaskLog taskLog = TaskLog.dao.findById(taskLogId);

		if (taskLog == null) {
			throw new OperationException("该物料并未出库，请检查！");
		}
		PackingListItem packingListItem = PackingListItem.dao.findById(taskLog.getPackingListItemId());
		Task task = Task.dao.findById(packingListItem.getTaskId());
		if (task == null || !task.getType().equals(TaskType.EMERGENCY_OUT)) {
			throw new OperationException("任务不存在，或者任务非紧急手动出库任务！");
		}
		if (!taskLog.getMaterialId().equals(materialId.trim())) {
			throw new OperationException("该物料记录ID与料盘码未匹配，请检查！");
		}
		Material material = Material.dao.findById(materialId);
		taskLog.delete();
		material.delete();

	}


	// 更新标准料盘出库数量以及料盘信息
	public void updateOutQuantityAndMaterialInfo(AGVIOTaskItem item, Boolean afterCut, PackingListItem packingListItem, User user) {
		synchronized (UPDATEOUTQUANTITYANDMATERIALINFO_LOCK) {
			int acturallyNum = 0;
			List<Record> records = Db.find(SQL.GET_TASKLOG_BY_ITEM_ID_SQL, item.getId());
			if (!records.isEmpty()) {
				for (Record record : records) {
					acturallyNum += record.getInt("quantity");
					int remainderQuantity = record.getInt("remainder_quantity") - record.getInt("quantity");
					Material material = Material.dao.findById(record.getStr("material_id"));
					if (!afterCut && item.getIsForceFinish()) {
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
					if (material.getStatus().equals(MaterialStatus.OUTTING)) {
						material.setStatus(MaterialStatus.NORMAL).update();
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
						externalWhLog.setOperationTime(new Date());
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
								List<Task> inventoryTasks = RegularInventoryTaskService.me.getUnStartInventoryTask(task.getSupplier(), WarehouseType.REGULAR.getId(), task.getDestination());
								if (inventoryTasks.size() > 0) {
									storeNum = externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination())
											- externalWhLogService.getEWhMaterialQuantity(item.getMaterialTypeId(), task.getDestination(), inventoryTasks.get(0).getCreateTime());

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
							externalWhLog.setOperationTime(new Date());
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
		AGVIOTaskItem redisTaskItem = IOTaskItemRedisDAO.getIOTaskItem(taskId, packingListItem.getId());
		if (redisTaskItem == null || redisTaskItem.getState() != TaskItemState.ARRIVED_WINDOW) {
			throw new OperationException("任务条目不存在或者尚未到站！");
		}
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
			IOTaskItemRedisDAO.updateIOTaskItemInfo(redisTaskItem, null, null, null, null, null, null, true, null, null, null);
			material.setStatus(MaterialStatus.CUTTING).setCutTaskLogId(taskLogId).update();

		} else {
			IOTaskItemRedisDAO.updateIOTaskItemInfo(redisTaskItem, null, null, null, null, null, null, false, null, null, null);
			material.setStatus(MaterialStatus.OUTTING).setCutTaskLogId(null).update();
		}
		result = "操作成功";
		return result;

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
		FormerSupplier formerSupplier = FormerSupplier.dao.findFirst(GET_FORMER_SUPPLIER_SQL, supplierName, materialType.getSupplier());
		TaskLog taskLog = TaskLog.dao.findFirst(IOTaskSQL.GET_TASK_LOG_BY_PACKING_LIST_ITEM_ID_AND_MATERIAL_ID_SQL, packingListItemId, materialId);
		Material material = Material.dao.findById(materialId);
		if (!supplierName.equals(sName) && formerSupplier == null) {
			throw new OperationException("扫码错误，客户 " + supplierName + " 对应的任务目前没有在本仓口进行任务，" + "本仓口已绑定 " + sName + " 的任务单！");
		} else if (taskLog == null) {
			throw new OperationException("扫错料盘，该料盘不需要放回该料盒!");
		} else if (material.getRemainderQuantity().intValue() != quantity) {
			throw new OperationException("请扫描修改出库数时所打印出的新料盘二维码!");
		} else if (material.getRemainderQuantity() == 0) {
			throw new OperationException("该料盘已全部出库!");
		} else if (material.getIsInBox()) {
			throw new OperationException("该料盘已设置为在盒内，请将料盘放入料盒内!");
		} else {
			material.setIsInBox(true).setStatus(MaterialStatus.OUTTING).setCutTaskLogId(null).update();
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
	 * 
	 * @param taskId
	 * @param windowIds
	 */
	public synchronized void setTaskWindow(Integer taskId, String windowIds) {
		Task task = Task.dao.findById(taskId);
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("任务未处于进行中状态，无法指定仓口！");
		}
		if (TaskPropertyRedisDAO.getTaskStatus(taskId)) {
			throw new OperationException("任务未处于暂停状态，无法指定或更改仓口！");
		}
		List<Window> windows = Window.dao.find(SQL.GET_WINDOW_BY_TASK, task.getId());
		if (task.getType().equals(TaskType.COUNT)) {
			List<InventoryLog> logs = InventoryLog.dao.find(InventoryTaskSQL.GET_UN_INVENTORY_LOG_BY_TASKID, taskId);
			if (logs.size() == 0 && windows.isEmpty()) {
				throw new OperationException("盘点任务UW仓盘点阶段已结束，无法指定或更改仓口！");
			}
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
				for (AGVIOTaskItem agvioTaskItem : IOTaskItemRedisDAO.getIOTaskItems(taskId)) {
					if (agvioTaskItem.getState() >= TaskItemState.ASSIGNED && agvioTaskItem.getState() < TaskItemState.BACK_BOX) {
						throw new OperationException("仓口" + agvioTaskItem.getWindowId() + "存在已分配叉车或已到站的任务条目，无法解绑或重新设置该任务的仓口");
					}
				}
			} else if (task.getType().equals(TaskType.COUNT)) {
				for (AGVInventoryTaskItem agvInventoryTaskItem : InventoryTaskItemRedisDAO.getInventoryTaskItems(taskId)) {
					if (agvInventoryTaskItem.getState() >= TaskItemState.ASSIGNED && agvInventoryTaskItem.getState() < TaskItemState.BACK_BOX) {
						throw new OperationException("仓口" + agvInventoryTaskItem.getWindowId() + "存在已分配叉车或已到站的任务条目，无法解绑或重新设置该任务的仓口");
					}
				}
			} else if (task.getType().equals(TaskType.SAMPLE)) {
				for (AGVSampleTaskItem agvSampleTaskItem : SampleTaskItemRedisDAO.getSampleTaskItems(taskId)) {
					if (agvSampleTaskItem.getState() >= TaskItemState.ASSIGNED && agvSampleTaskItem.getState() < TaskItemState.BACK_BOX) {
						throw new OperationException("仓口" + agvSampleTaskItem.getWindowId() + "存在已分配叉车或已到站的任务条目，无法解绑或重新设置该任务的仓口");
					}
				}
			}

		}
		synchronized (Lock.WINDOW_LOCK) {
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

	}


	/**
	 * 
	 * <p>
	 * Description: 开始/暂停任务
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2019年11月27日
	 */
	public String switchTask(Integer taskId, Boolean flag) {
		Task task = Task.dao.findById(taskId);
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("任务并未处于进行中状态，无法开始或暂停！");
		}
		if (flag) {
			Window window = Window.dao.findFirst(WindowSQL.GET_WINDOW_BY_TASK, taskId);
			if (window == null) {
				throw new OperationException("任务没有绑定仓口，任务无法开始！");
			}
			EfficiencyRedisDAO.putTaskStartTime(taskId, new Date().getTime());
		}
		TaskPropertyRedisDAO.setTaskStatus(taskId, flag);
		return "操作成功";
	}


	/**
	 * <p>
	 * Description:强制解绑仓口，仅有作废任务可以解绑
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2019年11月27日
	 */
	public void forceUnbundlingWindow(Integer taskId) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getState().equals(TaskState.CANCELED)) {
			throw new OperationException("仓口绑定任务未处于作废状态，无法解绑！");
		}
		synchronized (Lock.WINDOW_LOCK) {
			List<Window> windows = getTaskWindow(taskId);
			if (windows == null || windows.isEmpty()) {
				throw new OperationException("任务并未绑定仓口，无需解绑！");
			}
			IOTaskItemRedisDAO.removeTaskItemByTaskId(task.getId());
			for (Window window : windows) {
				List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOW, window.getId());
				if (!goodsLocations.isEmpty()) {
					for (GoodsLocation goodsLocation : goodsLocations) {
						TaskPropertyRedisDAO.delLocationStatus(window.getId(), goodsLocation.getId());
					}
				}
				window.setBindTaskId(null).update();
			}
		}

		EfficiencyRedisDAO.removeTaskBoxArrivedTimeByTask(taskId);
		EfficiencyRedisDAO.removeTaskLastOperationUserByTask(taskId);
		EfficiencyRedisDAO.removeTaskLastOperationTimeByTask(taskId);
		EfficiencyRedisDAO.removeTaskStartTimeByTask(taskId);
	}


	private void setIOTaskMaterialEmpty(Task task, List<PackingListItem> unfinishItems) {
		// 查询所有未完成任务条目
		if (unfinishItems.isEmpty()) {
			return;
		}
		for (PackingListItem packingListItem : unfinishItems) {
			packingListItem.setFinishTime(new Date());
			packingListItem.update();
			if (task.getType().equals(TaskType.OUT)) {
				String operatior = "";
				List<TaskLog> taskLogs = TaskLog.dao.find(GET_TASKLOG_BY_PACKINGITEMID, packingListItem.getId());
				if (!taskLogs.isEmpty()) {
					for (TaskLog taskLog : taskLogs) {
						if (operatior.equals("")) {
							operatior = taskLog.getOperator();
						}
						Material material = Material.dao.findById(taskLog.getMaterialId());
						int remainderQuantity = material.getRemainderQuantity() - taskLog.getQuantity();
						// 若该料盘没有库存了，则将物料实体表记录置为无效
						if (remainderQuantity <= 0) {
							material.setRow(-1);
							material.setCol(-1);
							material.setRemainderQuantity(0);
							material.setIsInBox(false);

						} else {
							material.setRemainderQuantity(remainderQuantity);
							material.setIsInBox(true);
						}
						material.update();
					}
				} else {
					createTaskLog(packingListItem.getId(), null, 0, null, task);
				}
			}
		}
	}


	private void countAndSetDiffQuantityIOTaskItem(Task task, List<PackingListItem> unfinishItems, Boolean isNeedSuperRecoed, Boolean isNeedDeductRecord) {

		if (unfinishItems.isEmpty()) {
			return;
		}
		Task inventoryTask = null;
		if (task.getIsInventoryApply()) {
			inventoryTask = Task.dao.findById(task.getInventoryTaskId());
		} else {
			inventoryTask = RegularInventoryTaskService.me.getOneUnStartInventoryTask(task.getSupplier(), WarehouseType.REGULAR.getId(), task.getDestination());
		}
		for (PackingListItem item : unfinishItems) {
			AGVIOTaskItem agvioTaskItem = IOTaskItemRedisDAO.getIOTaskItem(task.getId(), item.getId());
			if (agvioTaskItem == null) {
				continue;
			}
			Record record = Db.findFirst(IOTaskSQL.GET_OUT_QUANTIYT_IOTASK_ITEM_BY_ID, item.getId());
			int actualQuantity = 0;
			String operator = "";
			if (record != null) {
				actualQuantity = record.getInt("ActualQuantity") == null ? 0 : record.getInt("ActualQuantity");
				operator = record.getStr("Operator") == null ? "" : record.getStr("Operator");
			}
			int totalOutQuantity = actualQuantity + Math.abs(agvioTaskItem.getDeductionQuantity());
			if (!item.getQuantity().equals(totalOutQuantity)) {
				ExternalWhLog externalWhLog = new ExternalWhLog();
				externalWhLog.setMaterialTypeId(item.getMaterialTypeId());
				externalWhLog.setDestination(task.getDestination());
				externalWhLog.setSourceWh(UW_ID);
				externalWhLog.setTaskId(task.getId());
				externalWhLog.setOperationTime(new Date());
				externalWhLog.setOperatior(operator);
				externalWhLog.setTime(inventoryTask == null ? new Date() : inventoryTask.getCreateTime());
				if (item.getQuantity() < totalOutQuantity && isNeedSuperRecoed) {
					externalWhLog.setQuantity(totalOutQuantity - item.getQuantity());
				} else if (item.getQuantity() > totalOutQuantity && isNeedDeductRecord) {
					// 盘点已经过一次抵扣的缺料数
					Integer lackQuantity = item.getQuantity() - totalOutQuantity;
					Integer eWhStoreQuantity = externalWhLogService.getEwhMaterialQuantityByOutTask(task, inventoryTask, item.getMaterialTypeId(), task.getDestination());
					if (eWhStoreQuantity > lackQuantity) {
						externalWhLog.setQuantity(0 - lackQuantity);
					} else {
						externalWhLog.setQuantity(0 - eWhStoreQuantity);
					}
				}
				externalWhLog.save();
			}
		}
	}


	public void unbindWindow(Task task) {
		synchronized (Lock.WINDOW_LOCK) {
			List<Window> windows = Window.dao.find(WindowSQL.GET_WINDOW_BY_TASK, task.getId());
			for (Window window : windows) {
				List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, window.getId());
				for (GoodsLocation goodsLocation : goodsLocations) {
					TaskPropertyRedisDAO.delLocationStatus(window.getId(), goodsLocation.getId());
				}
				window.setBindTaskId(null).update();
			}
		}
	}


	private void putEfficiencyTimeToDb(Long time, Integer boxType, User user, Integer operationType, Integer taskId, Integer boxId) {
		Calendar calendar = Calendar.getInstance();
		System.out.println("now:" + calendar.getTime());
		if (time != null && !time.equals((long) 0)) {
			Efficiency efficiency = Efficiency.dao.findFirst("SELECT * FROM efficiency WHERE year = ? AND month = ? AND uid = ? AND box_type = ? AND operation_type = ?", calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH) + 1, user.getUid(), boxType, operationType);
			if (efficiency != null) {
				if (time == null || time == -1 || time == -2) {
					efficiency.setDishNum(efficiency.getDishNum() + 1).update();
				} else {
					if (calendar.getTimeInMillis() - time < 0) {
						efficiency.setDishNum(efficiency.getDishNum() + 1).update();
					} else {
						efficiency.setDeltaTime(efficiency.getDeltaTime() + calendar.getTimeInMillis() - time).setDishNum(efficiency.getDishNum() + 1).update();
					}
				}
			} else {
				efficiency = new Efficiency();
				efficiency.setYear(calendar.get(Calendar.YEAR)).setUid(user.getUid()).setMonth(calendar.get(Calendar.MONTH) + 1).setOperationType(operationType).setBoxType(boxType);
				if (time == null || time == -1 || time == -2) {
					efficiency.setDishNum(1).save();
				} else {
					if (calendar.getTimeInMillis() - time < 0) {
						efficiency.setDishNum(1).save();
					} else {
						efficiency.setDeltaTime(calendar.getTimeInMillis() - time).setDishNum(1).save();
					}
				}
			}
		}

		EfficiencyRedisDAO.putTaskLastOperationTime(taskId, calendar.getTimeInMillis());
		EfficiencyRedisDAO.putTaskLastOperationUser(taskId, user.getUid());
		EfficiencyRedisDAO.putUserLastOperationTime(user.getUid(), calendar.getTimeInMillis());
	}


	private void putAndReduceDishNumToDb(Integer taskId, Integer boxId, Integer boxType, String userId, Integer operationType) {
		Calendar calendar = Calendar.getInstance();
		Efficiency efficiency = Efficiency.dao.findFirst("SELECT * FROM efficiency WHERE year = ? AND month = ? AND uid = ? AND box_type = ? AND operation_type = ?", calendar.get(Calendar.YEAR),
				calendar.get(Calendar.MONTH) + 1, userId, boxType, operationType);
		if (efficiency != null) {
			if (EfficiencyRedisDAO.getTaskBoxArrivedTime(taskId, boxId) != null && efficiency.getDishNum() > 0) {
				efficiency.setDishNum(efficiency.getDishNum() - 1).update();
			}
		}
	}


	public void putUrOutTaskMaterialInfoToRedis(AGVIOTaskItem item, Task task) {

		Task inventoryTask = null;
		if (task.getType().equals(TaskType.OUT) && task.getIsInventoryApply()) {
			inventoryTask = Task.dao.findById(task.getInventoryTaskId());
		} else if (task.getType().equals(TaskType.OUT) && !task.getIsInventoryApply()) {
			inventoryTask = RegularInventoryTaskService.me.getOneUnStartInventoryTask(task.getSupplier(), WarehouseType.REGULAR.getId(), task.getDestination());
		}
		Integer eWhStoreQuantity = externalWhLogService.getEwhMaterialQuantityByOutTask(task, inventoryTask, item.getMaterialTypeId(), task.getDestination());
		Integer outQuantity = getActualIOQuantity(item.getId());
		Integer actualQuantity = 0;
		// 计算实际UW仓需出库物料数，以及抵扣数
		if (task.getIsDeducted()) {
			if (outQuantity == 0 && eWhStoreQuantity > EWH_DEDUCT_QUANTITY_LIMIT) {
				ExternalWhLog externalWhLog = new ExternalWhLog();
				externalWhLog.setMaterialTypeId(item.getMaterialTypeId());
				externalWhLog.setDestination(task.getDestination());
				externalWhLog.setSourceWh(UW_ID);
				externalWhLog.setTaskId(task.getId());
				externalWhLog.setTime(inventoryTask == null ? new Date() : inventoryTask.getCreateTime());
				if (eWhStoreQuantity - item.getPlanQuantity() > EWH_DEDUCT_QUANTITY_LIMIT) {
					externalWhLog.setQuantity(0 - item.getPlanQuantity());
					actualQuantity = 0;
					item.setDeductionQuantity(0 - item.getPlanQuantity());
				} else {
					externalWhLog.setQuantity(0 - eWhStoreQuantity + EWH_DEDUCT_QUANTITY_LIMIT);
					actualQuantity = item.getPlanQuantity() - (eWhStoreQuantity - EWH_DEDUCT_QUANTITY_LIMIT);
				}
				item.setDeductionQuantity(0 - eWhStoreQuantity + EWH_DEDUCT_QUANTITY_LIMIT);
				item.setUwQuantity(actualQuantity);
				externalWhLog.setOperatior("robot1");
				externalWhLog.setOperationTime(new Date());
				externalWhLog.save();
			} else if (outQuantity == 0 && eWhStoreQuantity <= EWH_DEDUCT_QUANTITY_LIMIT) {
				actualQuantity = item.getPlanQuantity() + EWH_DEDUCT_QUANTITY_LIMIT;
				item.setUwQuantity(actualQuantity);
			} else {
				actualQuantity = item.getUwQuantity() - outQuantity;
			}
		} else {
			if (outQuantity == 0) {
				actualQuantity = item.getPlanQuantity();
				item.setUwQuantity(actualQuantity);
			} else {
				actualQuantity = item.getUwQuantity() - outQuantity;
			}
		}

		// 获取机械臂出库物料
		List<UrMaterialInfo> urMaterialInfos = getUrOutTaskMaterialInfos(item, actualQuantity);
		if (urMaterialInfos != null) {
			UrTaskInfoDAO.putUrMaterialInfos(item.getTaskId(), item.getBoxId(), urMaterialInfos);
		}

	}


	/**
	 * 获取任务条目实际出入库数量
	 */
	public Integer getActualIOQuantity(Integer packingListItemId) {
		// 查询task_log中的material_id,quantity
		List<TaskLog> taskLogs = TaskLog.dao.find(IOTaskSQL.GET_TASK_ITEM_DETAILS_SQL, packingListItemId);
		Integer actualQuantity = 0;
		// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
		for (TaskLog tl : taskLogs) {
			actualQuantity += tl.getQuantity();
		}
		return actualQuantity;
	}


	public List<UrMaterialInfo> getUrOutTaskMaterialInfos(AGVIOTaskItem item, Integer limitQuantity) {
		Material oldestMaterial = Material.dao.findFirst(MaterialSQL.GET_MATERIAL_BY_MATERIAL_TYPE_AND_NOT_BOX_ORDER_TIME_SQL, item.getMaterialTypeId(), item.getBoxId());
		List<Material> materials = Material.dao.find(MaterialSQL.GET_MATERIAL_BY_TYPE_AND_BOX_ORDER_TIME_SQL, item.getMaterialTypeId(), item.getBoxId());
		Integer tempQuantiy = 0;
		List<UrMaterialInfo> urMaterialInfos = new ArrayList<UrMaterialInfo>();
		if (!materials.isEmpty()) {
			for (Material material : materials) {
				if (oldestMaterial != null) {
					if (material.getProductionTime().getTime() <= oldestMaterial.getProductionTime().getTime()) {
						tempQuantiy += material.getRemainderQuantity();
						UrMaterialInfo info = new UrMaterialInfo(material.getId(), material.getRow(), material.getCol(), item.getTaskId(), item.getBoxId(), item.getWindowId(),
								item.getGoodsLocationId(), false, 0, material.getRemainderQuantity(), item.getId());
						urMaterialInfos.add(info);
						if (tempQuantiy > limitQuantity) {
							break;
						}
					}
				} else {
					tempQuantiy += material.getRemainderQuantity();
					UrMaterialInfo info = new UrMaterialInfo(material.getId(), material.getRow(), material.getCol(), item.getTaskId(), item.getBoxId(), item.getWindowId(), item.getGoodsLocationId(),
							false, 0, material.getRemainderQuantity(), item.getId());
					urMaterialInfos.add(info);
					if (tempQuantiy > limitQuantity) {
						break;
					}
				}

			}
		}
		return urMaterialInfos;
	}

}