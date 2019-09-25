package com.jimi.uw_server.controller;

import com.jfinal.aop.Aop;
import com.jfinal.core.Controller;
import com.jimi.uw_server.service.LogService;
import com.jimi.uw_server.util.ResultUtil;


/**
 * 日志控制层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class LogController extends Controller {

	private static LogService logService =  Aop.get(LogService.class);


	// 根据表名查询日志
	public void select(String table, Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		if (table.equals("action_log")) { // 查询「接口调用日志」
			renderJson(ResultUtil.succeed(logService.selectActionLog(table, pageNo, pageSize, ascBy, descBy, filter)));
		} else if (table.equals("task_log")) { // 查询「任务日志」b
			renderJson(ResultUtil.succeed(logService.selectTaskLog(pageNo, pageSize, ascBy, descBy, filter)));
		} else if (table.equals("position_log")) { // 查询「物料位置转移日志」
			renderJson(ResultUtil.succeed(logService.selectPositionLog(table, pageNo, pageSize, ascBy, descBy, filter)));
		}

	}

}
