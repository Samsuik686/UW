package com.jimi.uw_server.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.jfinal.aop.Aop;
import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.service.WindowService;
import com.jimi.uw_server.service.io.PreciousIOTaskService;
import com.jimi.uw_server.service.io.RegularIOTaskService;
import com.jimi.uw_server.util.ResultUtil;
import com.jimi.uw_server.util.TokenBox;


/**
   *  任务控制层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class TaskController extends Controller {

	private static RegularIOTaskService regualrIOTaskService = Aop.get(RegularIOTaskService.class);

	private static PreciousIOTaskService preciousIOTaskService = Aop.get(PreciousIOTaskService.class);

	private static WindowService windowService = Aop.get(WindowService.class);
	public static final String SESSION_KEY_LOGIN_USER = "loginUser";


	// 创建出入库/退料任务
	@Log("创建普通仓任务类型为：{type}的任务，客户编号为：{supplier}，目的地：{destination}， 是否申补：{isInventoryApply}， 申补任务：{inventoryTaskId}， 备注：{remarks}， 是否强制生成：{isForced}")
	public void createRegularIOTask(UploadFile file, Integer type, Integer supplier, Integer destination, Boolean isInventoryApply, Integer inventoryTaskId, String remarks, Boolean isForced) throws Exception {
		try {
			if (file == null || type == null || supplier == null || remarks == null || remarks.equals("")) {
			throw new ParameterException("参数不能为空！");
			}
			if (isForced == null) {
				isForced = false;
			}
			// 如果是创建「出库、入库或退料任务」，入库type为0，出库type为1，退料type为4，退料清0
			if (type == TaskType.IN || type == TaskType.OUT || type == TaskType.SEND_BACK || type == TaskType.EMERGENCY_OUT) {
				file = getFile();
				String fileName = file.getFileName();
				regualrIOTaskService.create(type, fileName, file.getFile(), supplier, destination, isInventoryApply, inventoryTaskId, remarks, isForced);
				renderJson(ResultUtil.succeed());
			}
		} finally {
			if (file != null && file.getFile() != null) {
				file.getFile().delete();
			}
		}
	}


	// 创建出入库/退料任务
	@Log("创建贵重仓任务类型为：{type}的任务，客户编号为：{supplier}，目的地：{destination}， 是否申补：{isInventoryApply}， 申补任务：{inventoryTaskId}， 备注：{remarks}， 是否强制生成：{isForced}")
	public void createPreciousIOTask(UploadFile file, Integer type, Integer supplier, Integer destination, Boolean isInventoryApply, Integer inventoryTaskId, String remarks, Boolean isForced) throws Exception {
		try {
			if (file == null || type == null || supplier == null || remarks == null || remarks.equals("")) {
				throw new ParameterException("参数不能为空！");
			}
			if (isForced == null) {
				isForced = false;
			}
			// 如果是创建「出库、入库或退料任务」，入库type为0，出库type为1，退料type为4，退料清0
			if (type == TaskType.IN || type == TaskType.OUT || type == TaskType.SEND_BACK) {
				file = getFile();
				String fileName = file.getFileName();
				preciousIOTaskService.create(type, fileName, file.getFile(), supplier, destination, isInventoryApply, inventoryTaskId, remarks, WarehouseType.PRECIOUS.getId(), isForced);
				renderJson(ResultUtil.succeed());	
			}
		} finally {
			if (file != null && file.getFile() != null) {
				file.getFile().delete();
			}
		}
		
	}


	// 令指定任务通过审核
	@Log("审核常规任务编号为{id}的任务")
	public void passRegularIOTask(Integer id) {
		if (id == null) {
			throw new ParameterException("任务id不能为空！");
		}
		preciousIOTaskService.pass(id);
		renderJson(ResultUtil.succeed());

	}


	// 令指定任务通过审核
	@Log("审核贵重仓任务编号为{id}的任务")
	public void passPreciousIOTask(Integer id) {
		if (id == null) {
			throw new ParameterException("任务id不能为空！");
		}
		preciousIOTaskService.pass(id);
		renderJson(ResultUtil.succeed());
	}


	// 令指定任务开始
	@Log("开始常规任务编号为{id}的任务，绑定的仓口为{window}, 机械臂仓口为：{urWindowId}")
	public void startRegularIOTask(Integer id, Integer window, Integer urWindowId) {
		if (id == null) {
			throw new ParameterException("任务id或仓口id不能为空！");
		}
		regualrIOTaskService.start(id, window, urWindowId);
		renderJson(ResultUtil.succeed());
	}


	// 令指定任务开始
	@Log("开始贵重仓任务编号为{id}的任务，绑定的仓口为{window}")
	public void startPreciousIOTask(Integer id) {
		if (id == null) {
			throw new ParameterException("任务id不能为空！");
		}
		preciousIOTaskService.start(id);
		renderJson(ResultUtil.succeed());
		
	}


	// 作废指定任务
	@Log("作废常规任务编号为{id}的任务")
	public void cancelRegularIOTask(Integer id) {
		if (id == null) {
			throw new ParameterException("任务id参数不能为空！");
		}
		regualrIOTaskService.cancel(id);
		renderJson(ResultUtil.succeed());
	}


	// 作废指定任务
	@Log("作废贵重仓任务编号为{id}的任务")
	public void cancelPreciousIOTask(Integer id) {
		if (id == null) {
			throw new ParameterException("任务id参数不能为空！");
		}
		if (preciousIOTaskService.cancel(id)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}
	
	
	@Log("完成发料区紧急出库任务：{taskId}")
	public void finishEmergencyRegularTask(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		regualrIOTaskService.finishEmergencyRegularTask(taskId, user);
		renderJson(ResultUtil.succeed());
	}


	@Log("完成贵重仓任务{taskId}的缺料条目")
	public void finishPreciousTaskLackItem(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("任务id参数不能为空！");
		}
		if (preciousIOTaskService.finishTaskLackItem(taskId)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}

	@Log("完成{taskId}的贵重仓任务")
	public void finishPreciousTask(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		String result = preciousIOTaskService.finish(taskId);
		if (result.equals("操作成功")) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed(412, result));
		}
	}


	// 查看任务详情
	public void check(Integer id, Integer type, Integer pageSize, Integer pageNo) {
		if (id == null || type == null) {
			throw new ParameterException("任务id或任务类型不能为空！");
		}
		renderJson(ResultUtil.succeed(regualrIOTaskService.check(id, type, pageSize, pageNo)));
	}


	// 查看任务详情
	public void getIOTaskDetails(Integer id, Integer type, String no, Integer pageSize, Integer pageNo) {
		if (id == null || type == null) {
			throw new ParameterException("任务id或任务类型不能为空！");
		}
		renderJson(ResultUtil.succeed(regualrIOTaskService.getIOTaskDetails(id, type, no, pageSize, pageNo)));
	}


	// 获取任务详情（贵重仓调用）
	public void getIOTaskInfos(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		Object object = preciousIOTaskService.getIOTaskInfos(taskId);
		renderJson(ResultUtil.succeed(object));
	}


	// 查询指定类型的仓口
	public void getWindows(int type) {
        renderJson(ResultUtil.succeed(windowService.getWindows(type, false)));
    }
    
    // 查询指定类型的仓口
    public void getUrWindows() {
        //0代表查询空闲仓口
        renderJson(ResultUtil.succeed(windowService.getWindows(0, true)));
    }


	// 查询所有任务
	public void select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(regualrIOTaskService.select(pageNo, pageSize, ascBy, descBy, filter)));
	}


	// 获取截料中的物料的信息
	public void getCuttingMaterial(Integer taskId) {
		List<Record> records = regualrIOTaskService.getCuttingMaterial(taskId);
		renderJson(ResultUtil.succeed(records));
	}

	// 获取截料中的物料的信息
	public void getCuttingTask() {
		List<Task> records = regualrIOTaskService.getCuttingTask();
		renderJson(ResultUtil.succeed(records));
	}
	
	
	// 获取指定仓口任务条目
	public void getWindowTaskItems(Integer id, Integer pageNo, Integer pageSize) {
		renderJson(ResultUtil.succeed(regualrIOTaskService.getWindowTaskItems(id, pageNo, pageSize)));
	}


	// 获取指定仓口停泊条目
	public void getWindowParkingItem(Integer id) {
		ResultUtil result = ResultUtil.succeed(regualrIOTaskService.getWindowParkingItem(id));
		renderJson(result);
	}


	// 物料入库
	@Log("将id号为{packingListItemId}的任务条目进行扫码入库，料盘时间戳为{materialId}，入库数量为{quantity}，客户名为{supplierName}，生产日期为{productionTime}， 打印时间为{printTime}， 产商为{manufacturer}， 周期是{cycle}")
	public void inRegular(Integer packingListItemId, String materialId, Integer quantity, Date productionTime, String supplierName, String cycle, String manufacturer, Date printTime) {
		if (packingListItemId == null || materialId == null || quantity == null || productionTime == null || supplierName == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		if (cycle == null || cycle.trim().equals("")) {
			cycle = "无";
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		Material material = regualrIOTaskService.in(packingListItemId, materialId, quantity, productionTime, supplierName, cycle, manufacturer, printTime, user);
		renderJson(ResultUtil.succeed(material));
	}


	// 物料入库
	@Log("贵重仓入库，任务条目ID{packingListItemId}，料盘时间戳为{materialId}，入库数量为{quantity}，客户名为{supplierName}，生产日期为{productionTime}， 打印时间为{printTime}， 产商为{manufacturer}， 周期是{cycle}")
	public void inPrecious(Integer packingListItemId, String materialId, Integer quantity, Date productionTime, String supplierName, String cycle, String manufacturer, Date printTime) {
		if (packingListItemId == null || materialId == null || quantity == null || productionTime == null || supplierName == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		if (cycle == null || cycle.trim().equals("")) {
			cycle = "无";
		}
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		Material material = preciousIOTaskService.in(packingListItemId, materialId, quantity, productionTime, supplierName, cycle, manufacturer, printTime, user);
		renderJson(ResultUtil.succeed(material));
	}


	// 物料出库
	@Log("将id号为{packingListItemId}的任务条目进行扫码出库，料盘时间戳为{materialId}，出入库数量为{quantity}，客户名为{supplierName}")
	public void outRegular(Integer packingListItemId, String materialId, Integer quantity, String supplierName) {
		if (packingListItemId == null || materialId == null || quantity == null || supplierName == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		if (regualrIOTaskService.out(packingListItemId, materialId, quantity, supplierName, user)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}
	
	
	// 物料出库
	@Log("发料区紧急出库：将任务id号为{taskId}的任务进行扫码出库，料盘时间戳为{materialId}，出库数量为{quantity}，客户名为{supplierName}， 料号为{no}，生产日期为{productionTime}， 周期为{cycle}， 产商为{manufacturer}， 打印日期为{printTime}")
	public void outEmergencyRegular(String taskId, String no, String materialId, Integer quantity, Date productionTime, String supplierName, String cycle, String manufacturer, Date printTime) {
		if (taskId == null || materialId == null || quantity == null || supplierName == null || productionTime == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		if (regualrIOTaskService.outEmergencyRegular(taskId, no, materialId, quantity, productionTime, supplierName, cycle, manufacturer, printTime, user)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}
	
	// 物料出库
	@Log("贵重仓出库，任务条目ID{packingListItemId}，料盘时间戳为{materialId}，料号为{no}，出入库数量为{quantity}，客户名为{supplierName}, 是否强制出库：{isForced}")
	public void outPrecious(Integer packingListItemId, String materialId, Integer quantity, String supplierName, Boolean isForced) {
		if (packingListItemId == null || materialId == null || quantity == null || supplierName == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		if (preciousIOTaskService.out(packingListItemId, materialId, quantity, supplierName, user, isForced)) {
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
		renderJson(ResultUtil.succeed(regualrIOTaskService.deleteRegularMaterialRecord(packingListItemId, materialId)));
	}
	
	
	// 删除错误的料盘记录
	@Log("删除掉普通仓料盘时间戳为{materialId}的出入库记录，该料盘绑定的task_log条目id为{taskLogId}")
	public void deleteEmergencyRegularMaterialRecord(Integer taskLogId, String materialId) {
		if (taskLogId == null || materialId == null) {
			throw new ParameterException("参数不能为空！");
		}
		regualrIOTaskService.deleteEmergencyRegularMaterialRecord(taskLogId, materialId);
		renderJson(ResultUtil.succeed());
	}


	// 删除错误的料盘记录
	@Log("删除掉贵重仓料盘时间戳为{materialId}的出入库记录，该料盘绑定的任务条目id为{packingListItemId}")
	public void deletePreciousMaterialRecord(Integer packingListItemId, String materialId) {
		if (packingListItemId == null || materialId == null) {
			throw new ParameterException("参数不能为空！");
		}
		renderJson(ResultUtil.succeed(preciousIOTaskService.deleteMaterialRecord(packingListItemId, materialId)));
	}


	@Log("修改任务条目{packingListItemId}的出库记录{taskLogId}，料盘码{materialId}的出库数量为{quantity}")
	public void modifyRegularOutQuantity(Integer taskLogId, Integer packingListItemId, String materialId, Integer quantity) {

		if (taskLogId == null || packingListItemId == null || quantity == null || materialId == null) {
			throw new ParameterException("参数不能为空！");
		}
		if (quantity <= 0) {
			throw new ParameterException("数量必须为正整数！");
		}
		renderJson(ResultUtil.succeed(regualrIOTaskService.modifyOutQuantity(taskLogId, packingListItemId, materialId, quantity)));
	}


	// 料盘截料后重新入库
	@Log("料盘时间戳为{materialId}的料盘截料完毕，扫码重新入库，该料盘绑定的任务条目id为{packingListItemId}，料盘剩余数量为{quantity}，客户名为{supplierName}")
	public void backRegularAfterCutting(Integer packingListItemId, String materialId, Integer quantity, String supplierName) {
		if (packingListItemId == null || materialId == null || quantity == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		Material material = regualrIOTaskService.backAfterCutting(packingListItemId, materialId, quantity, supplierName);
		renderJson(ResultUtil.succeed(material));
	}


	@Log("料盘时间戳为{materialId}的料盘截料完毕，扫码重新入库，该料盘绑定的贵重仓任务条目id为{packingListItemId}，料盘剩余数量为{quantity}，客户名为{supplierName}")
	public void backPreciousAfterCutting(Integer packingListItemId, String materialId, Integer quantity, String supplierName) {
		if (packingListItemId == null || materialId == null || quantity == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		Material material = preciousIOTaskService.backAfterCutting(packingListItemId, materialId, quantity, supplierName);
		renderJson(ResultUtil.succeed(material));
	}


	// 设置优先级
	@Log("将任务id为{id}的任务优先级设置为{priority}")
	public void setPriority(Integer id, Integer priority) {
		if (id == null || priority == null) {
			throw new ParameterException("参数不能为空！");
		}
		renderJson(ResultUtil.succeed(regualrIOTaskService.setPriority(id, priority)));
	}


	@Log("编辑任务备注，任务ID{taskId}，备注{remarks}")
	public void editTaskRemarks(Integer taskId, String remarks) {
		if (taskId == null || remarks == null || remarks.equals("")) {
			throw new ParameterException("参数不能为空！");
		}
		String result = regualrIOTaskService.editTaskRemarks(taskId, remarks);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("导出普通仓任务条目报表, 任务ID{id}， 任务类型{type}")
	public void exportIORegularTaskDetails(Integer id, Integer type) {
		OutputStream output = null;
		try {
			// 设置响应，只能在controller层设置，因为getResponse()方法只能在controller层调用
			String fileName = "任务报表_" + regualrIOTaskService.getTaskName(id);
			HttpServletResponse response = getResponse();
			response.reset();
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), "ISO8859-1"));
			response.setContentType("application/vnd.ms-excel");
			output = response.getOutputStream();
			regualrIOTaskService.exportIOTaskDetails(id, type, fileName, output);
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


	@Log("导出贵重仓任务条目报表, 任务ID{id}， 任务类型{type}")
	public void exportIOPreciousTaskDetails(Integer id, Integer type) {
		OutputStream output = null;
		try {
			// 设置响应，只能在controller层设置，因为getResponse()方法只能在controller层调用
			String fileName = "任务报表_" + preciousIOTaskService.getTaskName(id);
			HttpServletResponse response = getResponse();
			response.reset();
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), "ISO8859-1"));
			response.setContentType("application/vnd.ms-excel");
			output = response.getOutputStream();
			preciousIOTaskService.exportIOTaskDetails(id, type, fileName, output);
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
		regualrIOTaskService.switchTask(taskId, flag);
		renderJson(ResultUtil.succeed());
	}

	
	/**
	 * <p>Description: 获取紧急出库任务列表<p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2019年11月27日
	 */
	public void getEmergencyRegularTasks(){
		List<Task> tasks = regualrIOTaskService.getEmergencyRegularTasks();
		renderJson(ResultUtil.succeed(tasks));
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
		regualrIOTaskService.setTaskWindow(taskId, windowIds);
		renderJson(ResultUtil.succeed());
	}

	/**
	 * <p>Description:强制解绑仓口，仅有作废任务可以解绑 <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2019年11月27日
	 */
	@Log("强制解绑仓口，出入库任务ID：{taskId}")
	public void forceUnbundlingWindow(Integer taskId) {
		
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		regualrIOTaskService.forceUnbundlingWindow(taskId);
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
		List<Window> windows = regualrIOTaskService.getTaskWindow(taskId);
		renderJson(ResultUtil.succeed(windows));
	}
}
