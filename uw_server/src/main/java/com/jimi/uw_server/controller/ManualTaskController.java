package com.jimi.uw_server.controller;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.core.Controller;
import com.jfinal.json.Json;
import com.jfinal.kit.HttpKit;
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
		Map<String, Integer> result = new HashMap<>();
		result.put("taskId", taskId);
		renderJson(ResultUtil.succeed(result));
	}


	public void uploadRecord(ManualTaskInfo info) {
		try {
			String json = HttpKit.readData(getRequest());
			info = Json.getJson().parse(json.toString(), ManualTaskInfo.class);
		} catch (NullPointerException e) {
			throw new OperationException("参数不能为空");
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException("参数格式错误");
		}

		if (info == null || info.getTaskId() == null) {
			throw new OperationException("参数不能为空");
		}
		String result = manualTaskService.uploadRecord(info);
		renderJson(ResultUtil.succeed(result));

	}


	public void ping() {
		renderJson(ResultUtil.succeed());
	}
}
