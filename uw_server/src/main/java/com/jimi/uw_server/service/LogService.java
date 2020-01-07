package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.vo.ActionLogVO;
import com.jimi.uw_server.model.vo.TaskLogVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;


/**
 * 日志业务层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class LogService extends SelectService {

	private static SelectService selectService = Aop.get(SelectService.class);


	// 查询接口调用日志
	public Object selectActionLog(String table, Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		Page<Record> result = selectService.select(new String[] {table}, null, pageNo, pageSize, ascBy, descBy, filter);
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
		Page<Record> result = selectService.select(new String[] {"task_log", "packing_list_item", "material_type", "material", "user"}, new String[] {"task_log.packing_list_item_id = packing_list_item.id", "task_log.material_id = material.id", "material_type.id = material.type", "task_log.operator = user.uid"}, pageNo, pageSize, ascBy, descBy, filter);
		List<TaskLogVO> taskLogVOs = new ArrayList<TaskLogVO>();
		for (Record res : result.getList()) {
			PackingListItem packingListItem = PackingListItem.dao.findById(Integer.parseInt(res.get("TaskLog_PackingListItemId").toString()));
			Task task = Task.dao.findById(packingListItem.getTaskId());
			TaskLogVO t = new TaskLogVO(res.get("TaskLog_Id"), res.get("TaskLog_PackingListItemId"), task.getType(), res.get("TaskLog_MaterialId"), res.get("MaterialType_No"), res.get("TaskLog_Quantity"), res.get("User_Uid"), res.get("TaskLog_Auto"), res.get("TaskLog_Time"));
			taskLogVOs.add(t);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(taskLogVOs);

		return pagePaginate;
	}

}
