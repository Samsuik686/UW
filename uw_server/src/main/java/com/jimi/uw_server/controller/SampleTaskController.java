package com.jimi.uw_server.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.constant.enums.WarehouseTypeEnum;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.vo.PackingSampleInfoVO;
import com.jimi.uw_server.model.vo.SampleTaskDetialsVO;
import com.jimi.uw_server.service.SampleTaskService;
import com.jimi.uw_server.util.ResultUtil;
import com.jimi.uw_server.util.TokenBox;


/**
 * 
 * @author HardyYao
 * @createTime 2019年6月24日  上午10:56:45
 */

public class SampleTaskController extends Controller {

	private static SampleTaskService sampleTaskService = SampleTaskService.me;

	public static final String SESSION_KEY_LOGIN_USER = "loginUser";


	@Log("创建普通仓抽检任务，客户{supplierId}， 备注{remarks}")
	public void createRegularSampleTask(UploadFile file, Integer supplierId, String remarks) {
		try {
			if (file == null || supplierId == null || remarks == null) {
				throw new OperationException("参数不能为空");
			}
			String result = sampleTaskService.createSampleTask(file.getFile(), supplierId, remarks, WarehouseTypeEnum.REGULAR.getId());
			renderJson(ResultUtil.succeed(result));
		} finally {
			file.getFile().delete();
		}

	}


	@Log("创建贵重仓抽检任务，客户{supplierId}， 备注{remarks}")
	public void createPreciousSampleTask(UploadFile file, Integer supplierId, String remarks) {
		try {
			if (file == null || supplierId == null || remarks == null) {
				throw new OperationException("参数不能为空");
			}
			String result = sampleTaskService.createSampleTask(file.getFile(), supplierId, remarks, WarehouseTypeEnum.PRECIOUS.getId());
			renderJson(ResultUtil.succeed(result));
		} finally {
			file.getFile().delete();
		}

	}


	@Log("开始普通仓抽检任务，任务：{taskId}， 仓口：{windows}")
	public void startRegularTask(Integer taskId, String windows) {

		if (taskId == null || windows == null || windows.trim().equals("")) {
			throw new OperationException("参数不能为空");
		}
		String result = sampleTaskService.startRegularTask(taskId, windows);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("开始贵重仓抽检任务，任务：{taskId}")
	public void startPreciousTask(Integer taskId) {

		if (taskId == null) {
			throw new OperationException("参数不能为空");
		}
		String result = sampleTaskService.startPreciousTask(taskId);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("作废普通仓抽检任务编号为{taskId}的任务")
	public void cancelRegularTask(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("任务id参数不能为空！");
		}
		if (sampleTaskService.cancelRegularTask(taskId)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	@Log("作废贵重仓抽检任务编号为{taskId}的任务")
	public void cancelPreciousTask(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("任务id参数不能为空！");
		}
		if (sampleTaskService.cancelPreciousTask(taskId)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	@Log("完成贵重仓抽检任务编号为{taskId}的任务")
	public void finishPreciousTask(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("任务id参数不能为空！");
		}
		if (sampleTaskService.finishPreciousTask(taskId)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	@Log("普通仓抽检回库，groupId：{groupId}")
	public void backRegularUWBox(String groupId) {
		if (groupId == null) {
			throw new OperationException("参数不能为空");
		}
		String result = sampleTaskService.backRegularUWBox(groupId);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("普通仓异常出库，料盘码：{materialId}， groupId：{groupId}")
	public void outRegularTaskSingular(String materialId, String groupId) {
		if (groupId == null || materialId == null) {
			throw new OperationException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = sampleTaskService.outRegularTaskSingular(materialId, groupId, user);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("贵重仓异常出库，料盘码：{materialId}， 任务ID：{taskId}")
	public void outPreciousTaskSingular(String materialId, Integer taskId) {
		if (taskId == null || materialId == null) {
			throw new OperationException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = sampleTaskService.outPreciousTaskSingular(materialId, taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("贵重仓抽检出库，料盘码：{materialId}， groupId：{groupId}")
	public void outRegularTaskRegular(String materialId, String groupId) {
		if (groupId == null || materialId == null) {
			throw new OperationException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = sampleTaskService.outRegularTaskRegular(materialId, groupId, user);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("贵重仓抽检出库，料盘码：{materialId}，任务ID：{taskId}")
	public void outPreciousTaskRegular(String materialId, Integer taskId) {
		if (taskId == null || materialId == null) {
			throw new OperationException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = sampleTaskService.outPreciousTaskRegular(materialId, taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("普通仓料盘丢失，料盘码：{materialId}， groupId：{groupId}")
	public void outRegularTaskLost(String materialId, String groupId) {
		if (groupId == null || materialId == null) {
			throw new OperationException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = sampleTaskService.outRegularTaskLost(materialId, groupId, user);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("贵重仓料盘丢失，料盘码：{materialId}，任务ID：{taskId}")
	public void outPreciousTaskLost(String materialId, Integer taskId) {
		if (taskId == null || materialId == null) {
			throw new OperationException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = sampleTaskService.outPreciousTaskLost(materialId, taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("普通仓扫码料盘，料盘码：{materialId}， groupId：{groupId}")
	public void sampleRegularUWMaterial(String materialId, String groupId) {
		if (materialId == null || materialId.trim().equals("") || groupId == null || groupId.trim().equals("")) {
			throw new OperationException("参数不能为空");
		}
		sampleTaskService.sampleRegularUWMaterial(materialId, groupId);
		renderJson(ResultUtil.succeed());
	}


	@Log("贵重仓仓扫码料盘，料盘码：{materialId}，任务ID：{taskId}")
	public void samplePreciousUWMaterial(String materialId, Integer taskId) {
		if (materialId == null || materialId.trim().equals("") || taskId == null) {
			throw new OperationException("参数不能为空");
		}
		sampleTaskService.samplePreciousUWMaterial(materialId, taskId);
		renderJson(ResultUtil.succeed());
	}


	/**
	 * 获取到站的物料信息
	 * @param windowId
	 */
	public void getPackingSampleMaterialInfo(Integer windowId) {
		if (windowId == null) {
			throw new OperationException("参数不能为空");
		}
		List<PackingSampleInfoVO> result = sampleTaskService.getPackingSampleMaterialInfo(windowId);
		renderJson(ResultUtil.succeed(result));

	}


	public void getSampleTaskMaterialInfo(Integer taskId) {
		if (taskId == null) {
			throw new OperationException("参数不能为空");
		}
		PackingSampleInfoVO result = sampleTaskService.getSampleTaskMaterialInfo(taskId);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 获取抽检任务详情
	 * @param taskId
	 * @param pageSize
	 * @param pageNo
	 */
	public void getSampleTaskDetials(Integer taskId) {
		if (taskId == null) {
			throw new OperationException("参数不能为空");
		}
		List<SampleTaskDetialsVO> result = sampleTaskService.getSampleTaskDetials(taskId);
		renderJson(ResultUtil.succeed(result));

	}


	// 获取进行中的贵重仓抽检任务（PDA用）
	public void getWorkingPreciousTask() {
		String filter = "warehouse_type=1#&#state=2";
		;
		Object result = sampleTaskService.select(null, null, null, null, filter);
		renderJson(ResultUtil.succeed(result));
	}


	// 查询所有任务
	public void select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(sampleTaskService.select(pageNo, pageSize, ascBy, descBy, filter)));
	}


	@Log("导出抽检务报表, 任务ID{taskId}")
	public void exportSampleTaskInfo(Integer taskId, Integer type) {
		OutputStream output = null;
		try {
			// 设置响应，只能在controller层设置，因为getResponse()方法只能在controller层调用
			String fileName = "报表_" + sampleTaskService.getTaskName(taskId) + ".xlsx";
			HttpServletResponse response = getResponse();
			response.reset();
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), "ISO8859-1"));
			response.setContentType("application/vnd.ms-excel");
			output = response.getOutputStream();
			sampleTaskService.exportSampleTaskInfo(taskId, fileName, output);
		} catch (Exception e) {
			renderJson(ResultUtil.failed());
		} finally {
			try {
				if (output != null) {
					output.close();
				}
			} catch (IOException e) {
				renderJson(ResultUtil.failed());
			}
		}
		renderNull();
	}
	
	
	/**
	 * <p>Description:强制解绑仓口，仅有作废任务可以解绑 <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2019年11月27日
	 */
	@Log("强制解绑仓口，抽检任务ID：{taskId}")
	public void forceUnbundlingWindow(Integer taskId) {
		
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		sampleTaskService.forceUnbundlingWindow(taskId);
		renderJson(ResultUtil.succeed());
	}

}
