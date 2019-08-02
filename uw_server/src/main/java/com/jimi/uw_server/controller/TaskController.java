package com.jimi.uw_server.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.service.TaskService;
import com.jimi.uw_server.util.ResultUtil;
import com.jimi.uw_server.util.TokenBox;


/**
 * 任务控制层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class TaskController extends Controller {

	private static TaskService taskService = Enhancer.enhance(TaskService.class);

	public static final String SESSION_KEY_LOGIN_USER = "loginUser";


	// 创建出入库/退料任务
	@Log("创建任务类型为{type}的任务，供应商编号为{supplier}")
	public void create(UploadFile file, Integer type, Integer supplier, Integer destination, Boolean isInventoryApply, Integer inventoryTaskId, String remarks) throws Exception {
		if (file == null || type == null || supplier == null || remarks == null || remarks.equals("")) {
			throw new ParameterException("参数不能为空！");
		}
		// 如果是创建「出库、入库或退料任务」，入库type为0，出库type为1，退料type为4，退料清0
		if (type == TaskType.IN || type == TaskType.OUT || type == TaskType.SEND_BACK) {
			file = getFile();
			String fileName = file.getFileName();
			String fullFileName = file.getUploadPath() + File.separator + file.getFileName();
			String resultString = taskService.createIOTask(type, fileName, fullFileName, supplier, destination, isInventoryApply, inventoryTaskId, remarks);

			if (resultString.equals("添加成功！")) {
				renderJson(ResultUtil.succeed());
			} else {
				throw new OperationException(resultString);
			}
		}

		else if (type == TaskType.COUNT) { // 如果是创建「盘点任务」
			renderJson(ResultUtil.failed("该功能尚在开发中！"));
		}

		else if (type == TaskType.POSITION_OPTIZATION) { // 如果是创建「位置优化任务」
			renderJson(ResultUtil.failed("该功能尚在开发中！"));
		}

	}


	// 令指定任务通过审核
	@Log("审核任务编号为{id}的任务")
	public void pass(Integer id) {
		if (id == null) {
			throw new ParameterException("任务id不能为空！");
		}
		if (taskService.pass(id)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 令指定任务开始
	@Log("开始任务编号为{id}的任务，绑定的仓口为{window}")
	public void start(Integer id, Integer window) {
		if (id == null || window == null) {
			throw new ParameterException("任务id或仓口id不能为空！");
		}
		if (taskService.start(id, window)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 作废指定任务
	@Log("作废任务编号为{id}的任务")
	public void cancel(Integer id) {
		if (id == null) {
			throw new ParameterException("任务id参数不能为空！");
		}
		if (taskService.cancel(id)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 查看任务详情
	public void check(Integer id, Integer type, Integer pageSize, Integer pageNo) {
		if (id == null || type == null) {
			throw new ParameterException("任务id或任务类型不能为空！");
		}
		renderJson(ResultUtil.succeed(taskService.check(id, type, pageSize, pageNo)));
	}


	// 查看任务详情
	public void getIOTaskDetails(Integer id, Integer type, Integer pageSize, Integer pageNo) {
		if (id == null || type == null) {
			throw new ParameterException("任务id或任务类型不能为空！");
		}
		renderJson(ResultUtil.succeed(taskService.getIOTaskDetail(id, type, pageSize, pageNo)));
	}


	// 查询指定类型的仓口
	public void getWindows(int type) {
		renderJson(ResultUtil.succeed(taskService.getWindows(type)));
	}


	// 查询所有任务
	public void select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(taskService.select(pageNo, pageSize, ascBy, descBy, filter)));
	}


	// 获取指定仓口任务条目
	public void getWindowTaskItems(Integer id, Integer pageNo, Integer pageSize) {
		renderJson(ResultUtil.succeed(taskService.getWindowTaskItems(id, pageNo, pageSize)));
	}


	// 获取指定仓口停泊条目
	public void getWindowParkingItem(Integer id) {
		ResultUtil result = ResultUtil.succeed(taskService.getWindowParkingItem(id));
		renderJson(result);
	}


	// 物料入库
	@Log("将id号为{packListItemId}的任务条目进行扫码入库，料盘时间戳为{materialId}，入库数量为{quantity}，供应商名为{supplierName}")
	public void in(Integer packListItemId, String materialId, Integer quantity, Date productionTime, String supplierName) {
		if (packListItemId == null || materialId == null || quantity == null || productionTime == null || supplierName == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		Material material = taskService.in(packListItemId, materialId, quantity, productionTime, supplierName, user);
		renderJson(ResultUtil.succeed(material));
	}

	/*
	 * @Log("手工出库，任务ID为{taskId}") public void importOutRecords () { String json =
	 * HttpKit.readData(getRequest()); ManualTaskInfo info =
	 * FastJson.getJson().parse(json, ManualTaskInfo.class); if (info == null) {
	 * throw new ParameterException("参数不能为空！"); } System.out.println(info);
	 * renderNull(); }
	 */


	// 物料出库
	@Log("将id号为{packListItemId}的任务条目进行扫码出库，料盘时间戳为{materialId}，出入库数量为{quantity}，供应商名为{supplierName}")
	public void out(Integer packListItemId, String materialId, Integer quantity, String supplierName) {
		if (packListItemId == null || materialId == null || quantity == null || supplierName == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		if (taskService.out(packListItemId, materialId, quantity, supplierName, user)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 删除错误的料盘记录
	@Log("删除掉料盘时间戳为{materialId}的出入库记录，该料盘绑定的任务条目id为{packListItemId}")
	public void deleteMaterialRecord(Integer packListItemId, String materialId) {
		if (packListItemId == null || materialId == null) {
			throw new ParameterException("参数不能为空！");
		}
		renderJson(ResultUtil.succeed(taskService.deleteMaterialRecord(packListItemId, materialId)));
	}


	@Log("修改任务条目{packListItemId}的出库记录{taskLogId}，料盘码{materialId}的出库数量为{quantity}")
	public void modifyOutQuantity(Integer taskLogId, Integer packListItemId, String materialId, Integer quantity) {

		if (taskLogId == null || packListItemId == null || quantity == null || materialId == null) {
			throw new ParameterException("参数不能为空！");
		}
		if (quantity <= 0) {
			throw new ParameterException("数量必须为正整数！");
		}
		renderJson(ResultUtil.succeed(taskService.modifyOutQuantity(taskLogId, packListItemId, materialId, quantity)));
	}


	// 料盘截料后重新入库
	@Log("料盘时间戳为{materialId}的料盘截料完毕，扫码重新入库，该料盘绑定的任务条目id为{packingListItemId}，料盘剩余数量为{quantity}，供应商名为{supplierName}")
	public void backAfterCutting(Integer packingListItemId, String materialId, Integer quantity, String supplierName) {
		if (packingListItemId == null || materialId == null || quantity == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		Material material = taskService.backAfterCutting(packingListItemId, materialId, quantity, supplierName);
		renderJson(ResultUtil.succeed(material));
	}


	// 设置优先级
	@Log("将任务id为{id}的任务优先级设置为{priority}")
	public void setPriority(Integer id, Integer priority) {
		if (id == null || priority == null) {
			throw new ParameterException("参数不能为空！");
		}
		renderJson(ResultUtil.succeed(taskService.setPriority(id, priority)));
	}


	@Log("编辑任务备注，任务ID{taskId}，备注{remarks}")
	public void editTaskRemarks(Integer taskId, String remarks) {
		if (taskId == null || remarks == null || remarks.equals("")) {
			throw new ParameterException("参数不能为空！");
		}
		String result = taskService.editTaskRemarks(taskId, remarks);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("导出任务未完成条目报表, 任务ID{id}， 任务类型{type}")
	public void exportUnfinishTaskDetails(Integer id, Integer type) {
		OutputStream output = null;
		try {
			// 设置响应，只能在controller层设置，因为getResponse()方法只能在controller层调用
			String fileName = "作废任务_" + taskService.getTaskName(id);
			HttpServletResponse response = getResponse();
			response.reset();
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), "ISO8859-1"));
			response.setContentType("application/vnd.ms-excel");
			output = response.getOutputStream();
			taskService.exportUnfinishTaskDetails(id, type, fileName, output);
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


	@Log("开始/暂停任务{taskId}，Flag{flag}")
	public void switchTask(Integer taskId, Boolean flag) {
		if (taskId == null || flag == null) {
			throw new ParameterException("参数不能为空！");
		}
		taskService.switchTask(taskId, flag);
		renderJson(ResultUtil.succeed());
	}


	/**
	 * 设置任务指定的仓口
	 * @param taskId
	 * @param windowIds
	 */
	@Log("设置任务{taskId}的仓口为{windowIds}")
	public void setTaskWindow(Integer taskId, String windowIds) {

		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		taskService.setTaskWindow(taskId, windowIds);
		renderJson(ResultUtil.succeed());
	}


	/**
	 * 获取任务仓口
	 * @param taskId
	 */
	public void getTaskWindow(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		List<Window> windows = taskService.getTaskWindow(taskId);
		renderJson(ResultUtil.succeed(windows));
	}
}
