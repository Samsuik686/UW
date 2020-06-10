/**  
*  
*/
package com.jimi.uw_server.service.base;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jimi.uw_server.agv.gaitek.dao.TaskPropertyRedisDAO;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.constant.sql.InventoryTaskSQL;
import com.jimi.uw_server.constant.sql.SQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Destination;
import com.jimi.uw_server.model.InventoryLog;
import com.jimi.uw_server.model.InventoryTaskBaseInfo;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.vo.InventoryTaskVO;
import com.jimi.uw_server.util.ExcelWritter;
import com.jimi.uw_server.util.PagePaginate;

/**
 * <p>
 * Title: BaseInventoryTaskService
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
public class BaseInventoryTaskService {

	private static SelectService selectService = Aop.get(SelectService.class);

	private Integer uwId = 0;


	/**
	 * UW平仓，根据记录ID和任务ID
	 * 
	 * @param id
	 * @param taskId
	 * @param user
	 * @return
	 */
	public String coverUwMaterial(Integer id, Integer taskId, User user) {
		Task task = Task.dao.findById(taskId);
		if (task == null) {
			throw new OperationException("任务不存在！");
		}
		if (!task.getState().equals(TaskState.PROCESSING)) {
			throw new OperationException("盘点任务未处于进行中状态，无法平仓！");
		}
		InventoryLog inventoryLog = InventoryLog.dao.findById(id);
		if (inventoryLog == null) {
			throw new OperationException("该盘点记录不存在，请检查参数！");
		}
		InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID, task.getId(), uwId);
		if (info == null) {
			throw new OperationException("该仓库并未进行盘点！");
		}
		if (info.getCheckTime() == null) {
			throw new OperationException("该仓库盘点任务记录尚未审核，请先审核再平仓！");
		}
		if (info.getFinishTime() != null) {
			throw new OperationException("该仓库盘点任务已完成，无法平仓！");
		}
		if (!inventoryLog.getEnabled()) {
			throw new OperationException("该盘点记录已平或者不需要平仓！");
		}
		inventoryLog.setCoverOperatior(user.getUid());
		inventoryLog.setCoverTime(new Date());
		inventoryLog.setDifferentNum(0);
		inventoryLog.setEnabled(false);
		inventoryLog.update();
		InventoryLog uncoverInventoryLog = InventoryLog.dao.findFirst(InventoryTaskSQL.GET_UNCOVER_INVENTORY_LOG_BY_TASKID, taskId);
		if (uncoverInventoryLog == null) {
			info.setFinishOperator(user.getUid()).setFinishTime(new Date()).update();
		}
		return "操作成功";
	}


	/**
	 * UW仓批量平仓，根据任务ID和物料类型ID
	 * 
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
		InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID, task.getId(), uwId);
		if (info == null) {
			throw new OperationException("该仓库并未进行盘点！");
		}
		if (info.getCheckTime() == null) {
			throw new OperationException("该仓库盘点任务记录尚未审核，请先审核再平仓！");
		}
		if (info.getFinishTime() != null) {
			throw new OperationException("该仓库盘点任务已完成，无法平仓！");
		}
		List<InventoryLog> inventoryLogs = InventoryLog.dao.find(InventoryTaskSQL.GET_UNCOVER_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE, materialTypeId, taskId);
		Date date = new Date();
		for (InventoryLog inventoryLog : inventoryLogs) {
			inventoryLog.setCoverOperatior(user.getUid());
			inventoryLog.setDifferentNum(0);
			inventoryLog.setCoverTime(date);
			inventoryLog.setEnabled(false);
			inventoryLog.update();
		}
		InventoryLog uncoverInventoryLog = InventoryLog.dao.findFirst(InventoryTaskSQL.GET_UNCOVER_INVENTORY_LOG_BY_TASKID, taskId);
		if (uncoverInventoryLog == null) {
			info.setFinishOperator(user.getUid()).setFinishTime(new Date()).update();
		}
		return "操作成功";
	}


	/**
	 * 一键平仓UW
	 * 
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
		InventoryTaskBaseInfo info = InventoryTaskBaseInfo.dao.findFirst(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID, task.getId(), uwId);
		if (info.getCheckTime() == null) {
			throw new OperationException("该仓库盘点任务记录尚未审核，请先审核再平仓！");
		}
		if (info.getFinishTime() != null) {
			throw new OperationException("该仓库盘点任务已完成，无法平仓！");
		}
		List<InventoryLog> inventoryLogs = InventoryLog.dao.find(InventoryTaskSQL.GET_UNCOVER_INVENTORY_LOG_BY_TASKID, taskId);
		Date date = new Date();
		for (InventoryLog inventoryLog : inventoryLogs) {
			inventoryLog.setCoverOperatior(user.getUid());
			inventoryLog.setDifferentNum(0);
			inventoryLog.setCoverTime(date);
			inventoryLog.setEnabled(false);
			inventoryLog.update();
		}
		info.setFinishOperator(user.getUid()).setFinishTime(new Date()).update();
		List<InventoryTaskBaseInfo> infos = InventoryTaskBaseInfo.dao.find(InventoryTaskSQL.GET_INVENTORY_TASK_BASE_INFO_BY_TASKID, task.getId());
		if (infos.size() < 2 && task.getWarehouseType().equals(WarehouseType.REGULAR.getId())) {
			task.setState(TaskState.FINISHED).setEndTime(new Date()).update();
		}
		return "操作成功";
	}


	/**
	 * 
	 * <p>
	 * Description: 作废任务
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月25日
	 */
	public void cancelTask(Integer taskId) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (!task.getState().equals(TaskState.WAIT_START)) {
			throw new OperationException("盘点任务未处于未开始状态，无法作废！");
		}
		task.setState(TaskState.CANCELED).update();
	}


	/**
	 * 根据客户获取盘点任务(下拉框使用)
	 * 
	 * @param supplierId
	 * @return
	 */
	public List<Task> getInventoryTask(Integer supplierId, Integer warehouseType) {

		List<Task> tasks = Task.dao.find(InventoryTaskSQL.GET_INVENTORY_TASK_BY_SUPPLIER, TaskType.COUNT, supplierId, warehouseType);
		return tasks;
	}


	public PagePaginate selectAllInventoryTask(String filter, Integer pageNo, Integer pageSize, String ascBy, String descBy) {
		String[] tables = new String[] { "task", "supplier" };
		String[] refers = new String[] { "task.supplier = supplier.id" };
		if (filter == null || filter.trim().equals("")) {
			filter = "task.type=" + TaskType.COUNT;
		} else {
			filter += "#&#task.type=" + TaskType.COUNT;
		}
		Boolean status;
		if ((ascBy == null || ascBy.equals("")) && (descBy == null || descBy.equals(""))) {
			descBy = "task.state ASC, task.create_time";
		}
		Page<Record> page = selectService.select(tables, refers, pageNo, pageSize, ascBy, descBy, filter);
		List<InventoryTaskVO> taskVOs = new ArrayList<>();
		List<Window> windows = Window.dao.find(SQL.GET_WORKING_WINDOWS);
		Set<Integer> windowBindTaskSet = new HashSet<>();
		if (!windows.isEmpty()) {
			for (Window window : windows) {
				windowBindTaskSet.add(window.getBindTaskId());
			}
		}
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
			status = false;
			if (record.getInt("Task_State").equals(TaskState.PROCESSING) && windowBindTaskSet.contains(record.getInt("Task_Id"))) {
				status = TaskPropertyRedisDAO.getTaskStatus(record.getInt("Task_Id"));
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


	/**
	 * 
	 * <p>
	 * Description: 获取任务目的地：任务ID
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月25日
	 */
	public List<Destination> getInventoryTaskDestination(Integer taskId) {

		List<Destination> destinations = Destination.dao.find(InventoryTaskSQL.GET_INVENTORY_TASK_DESTINATION_BY_TASKID, taskId);
		return destinations;
	}


	/**
	 * 
	 * <p>
	 * Description: 获取任务基础信息：任务ID
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月25日
	 */
	public List<Record> getInventoryTaskBaseInfo(Integer taskId) {

		List<Record> records = Db.find(InventoryTaskSQL.GET_INVENTORY_TASK_BEAS_INFO_BY_TASKID, taskId);
		return records;
	}


	public void exportUwInventoryTask(Integer taskId, String no, String fileName, OutputStream output) throws IOException {
		SqlPara sqlPara = new SqlPara();
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (no == null || no.equals("")) {

			sqlPara.setSql(InventoryTaskSQL.GET_UW_INVENTORY_TASK_INFO);
			sqlPara.addPara(taskId);
			sqlPara.addPara(uwId);
		} else {
			sqlPara.setSql(InventoryTaskSQL.GET_UW_INVENTORY_TASK_INFO_BY_NO);
			sqlPara.addPara(taskId);
			sqlPara.addPara(uwId);
			sqlPara.addPara("%" + no + "%");
		}
		List<Record> inventoryRecords = Db.find(sqlPara);

		String[] field = null;
		String[] head = null;
		field = new String[] { "supplier_name", "no", "before_num", "actural_num", "different_num" };
		head = new String[] { "客户", "料号", "盘前数量", "盘点数量", "盈亏" };
		ExcelWritter writter = ExcelWritter.create(true);
		writter.fill(inventoryRecords, fileName, field, head);
		writter.write(output, true);
	}


	public void exportUwInventoryTaskDetials(Integer taskId, String no, String fileName, OutputStream output) throws IOException {
		SqlPara sqlPara = new SqlPara();
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (no == null || no.equals("")) {

			sqlPara.setSql(InventoryTaskSQL.GET_UW_INVENTORY_TASK_DETIALS_INFO);
			sqlPara.addPara(taskId);
			sqlPara.addPara(uwId);
		} else {
			sqlPara.setSql(InventoryTaskSQL.GET_UW_INVENTORY_TASK_DETIALS_INFO_BY_NO);
			sqlPara.addPara(taskId);
			sqlPara.addPara(uwId);
			sqlPara.addPara("%" + no + "%");
		}
		List<Record> inventoryRecords = Db.find(sqlPara);

		String[] field = null;
		String[] head = null;
		field = new String[] { "supplier_name", "no", "material_id", "material_cycle", "before_num", "actural_num", "different_num" };
		head = new String[] { "客户", "料号", "料盘码", "周期", "盘前数量", "盘点数量", "盈亏" };
		ExcelWritter writter = ExcelWritter.create(true);
		writter.fill(inventoryRecords, fileName, field, head);
		writter.write(output, true);
	}


	/**
	 * 
	 * <p>
	 * Description: 获取任务信息
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月25日
	 */
	public List<Record> getUwInventoryTaskInfo(Integer taskId, String no) {
		SqlPara sqlPara = new SqlPara();
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.COUNT)) {
			throw new OperationException("盘点任务不存在，请检查参数是否正确！");
		}
		if (no == null || no.equals("")) {
			sqlPara.setSql(InventoryTaskSQL.GET_UW_INVENTORY_TASK_INFO);
			sqlPara.addPara(taskId);
			sqlPara.addPara(uwId);
		} else {
			sqlPara.setSql(InventoryTaskSQL.GET_UW_INVENTORY_TASK_INFO_BY_NO);
			sqlPara.addPara(taskId);
			sqlPara.addPara(uwId);
			sqlPara.addPara("%" + no + "%");
		}
		List<Record> inventoryRecords = Db.find(sqlPara);

		return inventoryRecords;
	}


	/**
	 * 
	 * <p>
	 * Description: 生成盘点任务名
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月25日
	 */
	public String getTaskName(Date date, Integer warehouseType) {
		String fileName = "";
		if (warehouseType.equals(WarehouseType.REGULAR.getId())) {
			fileName = "普通仓盘点_";
		} else {
			fileName = "贵重仓盘点_";
		}

		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = formatter.format(date);
		fileName = fileName + time;
		return fileName;
	}


	public List<Task> getUnStartInventoryTask(Integer supplierId, Integer warehouseType, Integer whId) {

		List<Task> tasks = Task.dao.find(InventoryTaskSQL.GET_UNSTART_INVENTORY_TASK_BY_SUPPLIER, supplierId, whId, warehouseType);
		return tasks;
	}


	public Task getOneUnStartInventoryTask(Integer supplierId, Integer warehouseType, Integer whId) {

		Task task = Task.dao.findFirst(InventoryTaskSQL.GET_UNSTART_INVENTORY_TASK_BY_SUPPLIER, supplierId, whId, warehouseType);
		return task;
	}


	public String getTaskName(Integer id) {
		Task task = Task.dao.findById(id);
		if (task != null) {
			return task.getFileName();
		}
		return null;
	}

}
