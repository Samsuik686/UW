package com.jimi.uw_server.controller;

import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.vo.PackingSampleInfoVO;
import com.jimi.uw_server.service.SampleTaskService;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ResultUtil;
import com.jimi.uw_server.util.TokenBox;

/**
 * 
 * @author HardyYao
 * @createTime 2019年6月24日  上午10:56:45
 */

public class SampleTaskController extends Controller{
	
	private static SampleTaskService sampleTaskService = SampleTaskService.me;
	
	public static final String SESSION_KEY_LOGIN_USER = "loginUser";

	
	@Log("创建抽检任务，供应商{supplierId}， 备注{remarks}")
	public void createSampleTask(UploadFile file, Integer supplierId, String remarks) {
		try {
			if (file == null || supplierId == null || remarks == null) {
				throw new OperationException("参数不能为空");
			}
			String result = sampleTaskService.createSampleTask(file.getFile(), supplierId, remarks);
			renderJson(ResultUtil.succeed(result));
		} finally {
			file.getFile().delete();
		}
		
	}
	
	
	@Log("开始抽检任务，任务：{taskId}， 仓口：{windows}")
	public void start(Integer taskId, String windows) {
		
		if (taskId == null || windows == null || windows.trim().equals("")) {
			throw new OperationException("参数不能为空");
		}
		String result = sampleTaskService.start(taskId, windows);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	@Log("作废抽检任务编号为{taskId}的任务")
	public void cancel(Integer taskId) {
		if (taskId ==null) {
			throw new ParameterException("任务id参数不能为空！");
		}
		if(sampleTaskService.cancel(taskId)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}
	
	
	@Log("抽检回库，groupId：{groupId}")
	public void backBox(String groupId) {
		if (groupId == null) {
			throw new OperationException("参数不能为空");
		}
		String result = sampleTaskService.backBox(groupId);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	@Log("异常出库，料盘码：{materialId}， groupId：{groupId}")
	public void outSingular(String materialId, String groupId) {
		if (groupId == null || materialId == null) {
			throw new OperationException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = sampleTaskService.outSingular(materialId, groupId, user);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	@Log("抽检出库，料盘码：{materialId}， groupId：{groupId}")
	public void outRegular(String materialId, String groupId) {
		if (groupId == null || materialId == null) {
			throw new OperationException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = sampleTaskService.outRegular(materialId, groupId, user);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	/**
	 * 获取到站的物料信息
	 * @param windowId
	 */
	public void getPackingSampleMaterialInfo(Integer windowId) {
		if (windowId == null) {
			throw new OperationException("参数不能为空");
		}
		PackingSampleInfoVO result = sampleTaskService.getPackingSampleMaterialInfo(windowId);
		renderJson(ResultUtil.succeed(result));
		
	}
	
	
	/**
	 * 获取抽检任务详情
	 * @param taskId
	 * @param pageSize
	 * @param pageNo
	 */
	public void getSampleTaskDetials(Integer taskId, Integer pageSize, Integer pageNo) {
		if (taskId == null) {
			throw new OperationException("参数不能为空");
		}
		if (pageNo == null || pageSize == null) {
			throw new ParameterException("参数不能为空");
		}
		if (pageNo <= 0 || pageSize <= 0) {
			throw new ParameterException("页码和页容量必须大于0");
		}
		PagePaginate result = sampleTaskService.getSampleTaskDetials(taskId, pageSize, pageNo);
		renderJson(ResultUtil.succeed(result));
		
	}
	
	
	// 查询所有任务
	public void select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(sampleTaskService.select(pageNo, pageSize, ascBy, descBy, filter)));
	}
	

	@Log("设置仓口{windowId}的叉车为{robots}")
	public void setWindowRobots(Integer windowId, String robots) {
		if (windowId == null) {
			throw new ParameterException("参数不能为空！");
		}
		if (robots == null) {
			robots = "";
		}
		String result  = sampleTaskService.setWindowRobots(windowId, robots);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	public void getWindowRobots(Integer windowId) {
		if (windowId == null) {
			throw new ParameterException("参数不能为空！");
		}
		String result  = sampleTaskService.getWindowRobots(windowId);
		renderJson(ResultUtil.succeed(result));
	}


}
