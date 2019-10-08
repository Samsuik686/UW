package com.jimi.uw_server.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.jfinal.aop.Aop;
import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.service.IOTaskService;
import com.jimi.uw_server.util.ResultUtil;
import com.jimi.uw_server.util.TokenBox;


/**
 * 任务控制层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class TaskController extends Controller {

	private static IOTaskService taskService = Aop.get(IOTaskService.class);

	public static final String SESSION_KEY_LOGIN_USER = "loginUser";


	// 创建出入库/退料任务
	@Log("创建普通仓任务类型为{type}的任务，供应商编号为{supplier}，目的地{destination}， 是否申补{isInventoryApply}， 申补任务{inventoryTaskId}， 备注{remarks}")
	public void createRegularIOTask(UploadFile file, Integer type, Integer supplier, Integer destination, Boolean isInventoryApply, Integer inventoryTaskId, String remarks) throws Exception {
		if (file == null || type == null || supplier == null || remarks == null || remarks.equals("")) {
			throw new ParameterException("参数不能为空！");
		}
		// 如果是创建「出库、入库或退料任务」，入库type为0，出库type为1，退料type为4，退料清0
		if (type == TaskType.IN || type == TaskType.OUT || type == TaskType.SEND_BACK) {
			file = getFile();
			String fileName = file.getFileName();
			String resultString = taskService.createIOTask(type, fileName, file.getFile(), supplier, destination, isInventoryApply, inventoryTaskId, remarks, WarehouseType.REGULAR);

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


	// 创建出入库/退料任务
	@Log("创建贵重仓任务类型为{type}的任务，供应商编号为{supplier}，目的地{destination}， 是否申补{isInventoryApply}， 申补任务{inventoryTaskId}， 备注{remarks}")
	public void createPreciousIOTask(UploadFile file, Integer type, Integer supplier, Integer destination, Boolean isInventoryApply, Integer inventoryTaskId, String remarks) throws Exception {
		if (file == null || type == null || supplier == null || remarks == null || remarks.equals("")) {
			throw new ParameterException("参数不能为空！");
		}
		// 如果是创建「出库、入库或退料任务」，入库type为0，出库type为1，退料type为4，退料清0
		if (type == TaskType.IN || type == TaskType.OUT || type == TaskType.SEND_BACK) {
			file = getFile();
			String fileName = file.getFileName();
			String resultString = taskService.createIOTask(type, fileName, file.getFile(), supplier, destination, isInventoryApply, inventoryTaskId, remarks, WarehouseType.PRECIOUS);

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
	@Log("审核常规任务编号为{id}的任务")
	public void passRegularIOTask(Integer id) {
		if (id == null) {
			throw new ParameterException("任务id不能为空！");
		}
		if (taskService.passRegularIOTask(id)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 令指定任务通过审核
	@Log("审核贵重仓任务编号为{id}的任务")
	public void passPreciousIOTask(Integer id) {
		if (id == null) {
			throw new ParameterException("任务id不能为空！");
		}
		if (taskService.passPreciousIOTask(id)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 令指定任务开始
	@Log("开始常规任务编号为{id}的任务，绑定的仓口为{window}")
	public void startRegularIOTask(Integer id, Integer window) {
		if (id == null || window == null) {
			throw new ParameterException("任务id或仓口id不能为空！");
		}
		if (taskService.start(id, window)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 令指定任务开始
	@Log("开始贵重仓任务编号为{id}的任务，绑定的仓口为{window}")
	public void startPreciousIOTask(Integer id) {
		if (id == null) {
			throw new ParameterException("任务id不能为空！");
		}
		if (taskService.startPreciousIOTask(id)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 作废指定任务
	@Log("作废常规任务编号为{id}的任务")
	public void cancelRegularIOTask(Integer id) {
		if (id == null) {
			throw new ParameterException("任务id参数不能为空！");
		}
		if (taskService.cancelRegularIOTask(id)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 作废指定任务
	@Log("作废贵重仓任务编号为{id}的任务")
	public void cancelPreciousIOTask(Integer id) {
		if (id == null) {
			throw new ParameterException("任务id参数不能为空！");
		}
		if (taskService.cancelPreciousIOTask(id)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	@Log("完成任务{taskId}的缺料条目")
	public void finishPreciousTaskLackItem(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("任务id参数不能为空！");
		}
		if (taskService.finishPreciousTaskLackItem(taskId)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	@Log("完成{packingListItemId}的任务条目")
	public void finishPreciousTaskItem(Integer packingListItemId) {
		if (packingListItemId == null) {
			throw new ParameterException("参数不能为空！");
		}
		if (taskService.finishPreciousTaskItem(packingListItemId)) {
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


	// 获取任务详情（贵重仓调用）
	public void getIOTaskInfos(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		Object object = taskService.getIOTaskInfos(taskId);
		renderJson(ResultUtil.succeed(object));
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
	@Log("将id号为{packingListItemId}的任务条目进行扫码入库，料盘时间戳为{materialId}，入库数量为{quantity}，供应商名为{supplierName}")
	public void inRegular(Integer packingListItemId, String materialId, Integer quantity, Date productionTime, String supplierName, String cycle, String manufacturer) {
		if (packingListItemId == null || materialId == null || quantity == null || productionTime == null || supplierName == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		if (cycle == null || cycle.trim().equals("")) {
			cycle = "无";
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		Material material = taskService.inRegular(packingListItemId, materialId, quantity, productionTime, supplierName, cycle, manufacturer, user);
		renderJson(ResultUtil.succeed(material));
	}


	// 物料入库
	@Log("贵重仓入库，任务条目ID{packingListItemId}，料盘时间戳为{materialId}，入库数量为{quantity}，供应商名为{supplierName}")
	public void inPrecious(Integer packingListItemId, String materialId, Integer quantity, Date productionTime, String supplierName, String cycle, String manufacturer) {
		if (packingListItemId == null || materialId == null || quantity == null || productionTime == null || supplierName == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		if (cycle == null || cycle.trim().equals("")) {
			cycle = "无";
		}
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		Material material = taskService.inPrecious(packingListItemId, materialId, quantity, productionTime, supplierName, cycle, manufacturer, user);
		renderJson(ResultUtil.succeed(material));
	}


	// 物料出库
	@Log("将id号为{packingListItemId}的任务条目进行扫码出库，料盘时间戳为{materialId}，出入库数量为{quantity}，供应商名为{supplierName}")
	public void outRegular(Integer packingListItemId, String materialId, Integer quantity, String supplierName) {
		if (packingListItemId == null || materialId == null || quantity == null || supplierName == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		if (taskService.outRegular(packingListItemId, materialId, quantity, supplierName, user)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 物料出库
	@Log("贵重仓出库，任务条目ID{packingListItemId}，料盘时间戳为{materialId}，料号为{no}，出入库数量为{quantity}，供应商名为{supplierName}")
	public void outPrecious(Integer packingListItemId, String materialId, Integer quantity, String supplierName) {
		if (packingListItemId == null || materialId == null || quantity == null || supplierName == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		if (taskService.outPrecious(packingListItemId, materialId, quantity, supplierName, user)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 删除错误的料盘记录
	@Log("删除掉普通仓料盘时间戳为{materialId}的出入库记录，该料盘绑定的任务条目id为{packingListItemId}")
	public void deleteRegularMaterialRecord(Integer packingListItemId, String materialId) {
		if (packingListItemId == null || materialId == null) {
			throw new ParameterException("参数不能为空！");
		}
		renderJson(ResultUtil.succeed(taskService.deleteRegularMaterialRecord(packingListItemId, materialId)));
	}


	// 删除错误的料盘记录
	@Log("删除掉贵重仓料盘时间戳为{materialId}的出入库记录，该料盘绑定的任务条目id为{packingListItemId}")
	public void deletePreciousMaterialRecord(Integer packingListItemId, String materialId) {
		if (packingListItemId == null || materialId == null) {
			throw new ParameterException("参数不能为空！");
		}
		renderJson(ResultUtil.succeed(taskService.deletePreciousMaterialRecord(packingListItemId, materialId)));
	}


	@Log("修改任务条目{packingListItemId}的出库记录{taskLogId}，料盘码{materialId}的出库数量为{quantity}")
	public void modifyRegularOutQuantity(Integer taskLogId, Integer packingListItemId, String materialId, Integer quantity) {

		if (taskLogId == null || packingListItemId == null || quantity == null || materialId == null) {
			throw new ParameterException("参数不能为空！");
		}
		if (quantity <= 0) {
			throw new ParameterException("数量必须为正整数！");
		}
		renderJson(ResultUtil.succeed(taskService.modifyRegularOutQuantity(taskLogId, packingListItemId, materialId, quantity)));
	}


	@Log("修改贵重仓任务条目{packingListItemId}的出库记录{taskLogId}，料盘码{materialId}的出库数量为{quantity}")
	public void modifyPreciousOutQuantity(Integer taskLogId, Integer packingListItemId, String materialId, Integer quantity) {

		if (taskLogId == null || packingListItemId == null || quantity == null || materialId == null) {
			throw new ParameterException("参数不能为空！");
		}
		if (quantity <= 0) {
			throw new ParameterException("数量必须为正整数！");
		}
		renderJson(ResultUtil.succeed(taskService.modifyPreciosOutQuantity(taskLogId, packingListItemId, materialId, quantity)));
	}


	// 料盘截料后重新入库
	@Log("料盘时间戳为{materialId}的料盘截料完毕，扫码重新入库，该料盘绑定的任务条目id为{packingListItemId}，料盘剩余数量为{quantity}，供应商名为{supplierName}")
	public void backRegularAfterCutting(Integer packingListItemId, String materialId, Integer quantity, String supplierName) {
		if (packingListItemId == null || materialId == null || quantity == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		Material material = taskService.backRegularAfterCutting(packingListItemId, materialId, quantity, supplierName);
		renderJson(ResultUtil.succeed(material));
	}


	@Log("料盘时间戳为{materialId}的料盘截料完毕，扫码重新入库，该料盘绑定的贵重仓任务条目id为{packingListItemId}，料盘剩余数量为{quantity}，供应商名为{supplierName}")
	public void backPreciousAfterCutting(Integer packingListItemId, String materialId, Integer quantity, String supplierName) {
		if (packingListItemId == null || materialId == null || quantity == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		Material material = taskService.backPreciousAfterCutting(packingListItemId, materialId, quantity, supplierName);
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


	@Log("导出普通仓任务未完成条目报表, 任务ID{id}， 任务类型{type}")
	public void exportUnfinishRegularTaskDetails(Integer id, Integer type) {
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


	@Log("导出贵重仓任务未完成条目报表, 任务ID{id}， 任务类型{type}")
	public void exportUnfinishPreciousTaskDetails(Integer id, Integer type) {
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
