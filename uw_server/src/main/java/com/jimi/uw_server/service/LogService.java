package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.vo.ActionLogVO;
import com.jimi.uw_server.model.vo.PositionLogVO;
import com.jimi.uw_server.model.vo.TaskLogVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;

/**
 * 日志业务层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class LogService extends SelectService {

	private static SelectService selectService = Enhancer.enhance(SelectService.class);


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


	public Object selectTaskLog(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		Page<Record> result = selectService.select(new String[] {"task_log", "packing_list_item", "material_type", "material", "user"}, new String[] {"task_log.packing_list_item_id = packing_list_item.id", "task_log.material_id = material.id", "material_type.id = material.type", "task_log.operator = user.uid"}, pageNo, pageSize, ascBy, descBy, filter);
		List<TaskLogVO> taskLogVOs = new ArrayList<TaskLogVO>();
		for (Record res : result.getList()) {
			PackingListItem packingListItem = PackingListItem.dao.findById(Integer.parseInt(res.get("TaskLog_PackingListItemId").toString()));
			Task task = Task.dao.findById(packingListItem.getTaskId());
			TaskLogVO t = new TaskLogVO(res.get("TaskLog_Id"), res.get("TaskLog_PackingListItemId"), task.getType(), res.get("TaskLog_MaterialId"), res.get("MaterialType_No"), res.get("TaskLog_Quantity"), res.get("User_Uid"), res.get("TaskLog_Auto"), res.get("TaskLog_Time"));taskLogVOs.add(t);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(taskLogVOs);

		return pagePaginate;
	}


	public Object selectPositionLog(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		Page<Record> result = selectService.select(new String[] {"position_log", "material_type", "material"}, new String[] {"position_log.material_id = material.id", "material.type = material_type.id"}, pageNo, pageSize, ascBy, descBy, filter);
		List<PositionLogVO> positionLogVOs = new ArrayList<PositionLogVO>();
		for (Record res : result.getList()) {
			PositionLogVO p = new PositionLogVO(res.get("PositionLog_Id"), res.get("PositionLog_TaskId"), res.get("PositionLog_MaterialId"), res.get("MaterialType_No"), res.get("PositionLog_OldArea"), res.get("PositionLog_OldRow"), res.get("PositionLog_OldCol"), res.get("PositionLog_OldHeight"), res.get("PositionLog_NewArea"), res.get("PositionLog_NewRow"), res.get("PositionLog_NewCol"), res.get("PositionLog_NewHeight"), res.get("PositionLog_Time"));
			positionLogVOs.add(p);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(positionLogVOs);

		return pagePaginate;
	}


}
