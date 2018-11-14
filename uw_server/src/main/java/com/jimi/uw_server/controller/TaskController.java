package com.jimi.uw_server.controller;

import java.io.File;
import java.util.Date;

import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.User;
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


	// 创建任务
	@Log("创建任务类型为{type}的任务")
	public void create(UploadFile file, Integer type) throws Exception {
		// 如果是创建「出入库任务」，入库type为0，出库type为1
		if (type.equals(0) || type.equals(1)) {
			file = getFile();
			String fileName = file.getFileName();
			String fullFileName = file.getUploadPath() + File.separator + file.getFileName();
			String resultString = taskService.createIOTask(type, fileName, fullFileName);

			if(resultString.equals("添加成功！")) {
				renderJson(ResultUtil.succeed());
				} 
			else {
				throw new OperationException(resultString);
				}

		}
		
		else if (type.equals(2) ) {	//如果是创建「盘点任务」
			renderJson(ResultUtil.failed("该功能尚在开发中！"));
		}
		
		else if (type.equals(3)) {	//如果是创建「位置优化任务」
			renderJson(ResultUtil.failed("该功能尚在开发中！"));
		}
		
	}


	// 令指定任务通过审核
	@Log("审核通过任务编号为{id}的任务")
	public void pass(Integer id) {
		if(taskService.pass(id)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 令指定任务开始
	@Log("开始任务编号为{id}的任务")
	public void start(Integer id, Integer window) {
		if(taskService.start(id, window)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}
	

	// 作废指定任务
	@Log("作废任务编号为{id}的任务")
	public void cancel(Integer id) {
		if(taskService.cancel(id)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 查看任务详情
	public void check(Integer id, Integer type, Integer pageSize, Integer pageNo) {
		renderJson(ResultUtil.succeed(taskService.check(id, type, pageSize, pageNo)));
	}
	

	// 查询指定类型的仓口
	public void getWindows(int type) {
		renderJson(ResultUtil.succeed(taskService.getWindows(type)));
	}


	// 查询所有任务
	public void select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter){
		renderJson(ResultUtil.succeed(taskService.select(pageNo, pageSize, ascBy, descBy, filter)));
	}


	// 获取指定仓口任务条目
	public void getWindowTaskItems(Integer id, Integer pageNo, Integer pageSize) {
		renderJson(ResultUtil.succeed(taskService.getWindowTaskItems(id, pageNo, pageSize)));
	}

	// 获取指定仓口停泊条目
	public void getWindowParkingItem(Integer id) {
		renderJson(ResultUtil.succeed(taskService.getWindowParkingItem(id)));
	}


	// 物料入库
	@Log("将id号为{packListItemId}的任务条目进行扫码入库，料盘时间戳为{materialId}，入库数量为{quantity}")
	public void in(Integer packListItemId, String materialId, Integer quantity, Date productionTime) {
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		if (taskService.in(packListItemId, materialId, quantity, productionTime, user)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 物料出库
	@Log("将id号为{packListItemId}的任务条目进行扫码出库，料盘时间戳为{materialId}，出入库数量为{quantity}")
	public void out(Integer packListItemId, String materialId, Integer quantity) {
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		if (taskService.out(packListItemId, materialId, quantity, user)) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed());
		}
	}


	// 删除错误的料盘记录
	@Log("删除掉料盘时间戳为{materialId}的出入库记录，该料盘绑定的任务条目id为{packListItemId}")
	public void deleteMaterialRecord(Integer packListItemId, String materialId) {
		renderJson(ResultUtil.succeed(taskService.deleteMaterialRecord(packListItemId, materialId)));
	}


	// 完成任务条目
	@Log("id号为{packListItemId}的任务条目出入库数量与计划数量不相符，叉车回库后，该任务条目的完成状态为{isFinish}(true表示已完成，false表示未完成)")
	public void finishItem(Integer packListItemId, Boolean isFinish) {
		renderJson(ResultUtil.succeed(taskService.finishItem(packListItemId, isFinish)));
	}


	// 料盘截料后重新入库
	@Log("料盘时间戳为{materialId}的料盘截料完毕，扫码重新入库，该料盘绑定的任务条目id为{packingListItemId}")
	public void backAfterCutting(Integer packingListItemId, String materialId) {
		String resultString = taskService.backAfterCutting(packingListItemId, materialId);
		if(resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	// 料盘截料后重新入库
	@Log("将任务id为{id}的任务优先级设置为{priority}")
	public void setPriority(Integer id, Integer priority) {
		renderJson(ResultUtil.succeed(taskService.setPriority(id, priority)));
	}

}
