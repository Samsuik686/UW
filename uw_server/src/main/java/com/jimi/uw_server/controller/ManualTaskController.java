package com.jimi.uw_server.controller;

import java.util.HashMap;

import com.jfinal.core.Controller;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.bo.ManualTaskInfo;
import com.jimi.uw_server.service.ManualTaskService;
import com.jimi.uw_server.util.ResultUtil;


public class ManualTaskController extends Controller {

	private static final ManualTaskService manualTaskService = ManualTaskService.me;


	public void create(String supplierName, Integer type, String destinationName) {

		if (supplierName == null || type == null || destinationName == null) {
			throw new OperationException("参数不能为空");
		}
		Integer taskId = manualTaskService.create(supplierName, type, destinationName);
		renderJson(ResultUtil.succeed(new HashMap<>().put("taskId", taskId)));
	}


	public void uploadRecord(ManualTaskInfo info) {
		if (info == null || info.getTaskId() == null) {
			throw new OperationException("参数不能为空");
		}
		String result = manualTaskService.uploadRecord(info);
		if (result.equals("导入成功")) {
			renderJson(ResultUtil.succeed(result));
		} else {
			renderJson(ResultUtil.failed(result));
		}
	}
}
