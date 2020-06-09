package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.vo.ActionLogVO;
import com.jimi.uw_server.model.vo.TaskLogVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.util.PagePaginate;


/**
 * 日志业务层
 * 
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class LogService extends SelectService {

	private static final String GET_TASK_LOG_INFO_BY_IDS = "SELECT task_log.time AS TaskLog_Time, task_log.id AS TaskLog_Id, task_log.material_id AS TaskLog_MaterialId, task_log.operator AS TaskLog_Operator, task_log.auto AS TaskLog_Auto, task_log.quantity AS TaskLog_Quantity, task_log.destination AS TaskLog_Destination, task_log.packing_list_item_id AS TaskLog_PackingListItemId, packing_list_item.task_id AS PackingListItem_TaskId, material_type.type AS MaterialType_Type, material_type. NO AS MaterialType_No, `user`.uid AS User_Uid, `user`.`name` AS User_Name FROM packing_list_item INNER JOIN material_type INNER JOIN `user` INNER JOIN task_log ON task_log.packing_list_item_id = packing_list_item.id AND material_type.id = packing_list_item.material_type_id AND task_log.operator = `user`.uid WHERE task_log.id IN ";
	private static SelectService selectService = Aop.get(SelectService.class);


	// 查询接口调用日志
	public Object selectActionLog(String table, Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		Page<Record> result = selectService.select(new String[] { table }, null, pageNo, pageSize, ascBy, descBy, filter);
		List<ActionLogVO> actionLogVOs = new ArrayList<ActionLogVO>();
		for (Record res : result.getList()) {
			ActionLogVO a = new ActionLogVO(res.get("id"), res.get("ip"), res.get("uid"), res.get("action"), res.get("time"), res.get("result_code"));
			actionLogVOs.add(a);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());

		pagePaginate.setList(actionLogVOs);

		return pagePaginate;
	}


	// 查询任务日志
	public Object selectTaskLog(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		Page<Record> result = selectService.select("task_log", pageNo, pageSize, ascBy, descBy, filter);
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		if (result.getList() == null || result.getList().isEmpty()) {
			pagePaginate.setList(Collections.emptyList());
			return pagePaginate;
		}
		List<TaskLogVO> taskLogVOs = new ArrayList<TaskLogVO>();
		StringBuilder sb = new StringBuilder();
		for (Record record : result.getList()) {
			sb.append(record.getInt("id"));
			sb.append(", ");
		}
		sb.deleteCharAt(sb.lastIndexOf(","));
		List<Record> records = Db.find(GET_TASK_LOG_INFO_BY_IDS + " ( " + sb.toString() + " )");
		for (Record record : records) {
			Task task = Task.dao.findById(record.getInt("PackingListItem_TaskId"));
			TaskLogVO t = new TaskLogVO(record.get("TaskLog_Id"), record.get("TaskLog_PackingListItemId"), task.getType(), record.get("TaskLog_MaterialId"), record.get("MaterialType_No"),
					record.get("TaskLog_Quantity"), record.get("User_Uid"), record.getStr("User_Name"), record.get("TaskLog_Auto"), record.get("TaskLog_Time"));
			taskLogVOs.add(t);
		}
		pagePaginate.setList(taskLogVOs);

		return pagePaginate;
	}

}
