package com.jimi.uw_server.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.vo.InventoryTaskDetailVO;
import com.jimi.uw_server.model.vo.PackingInventoryInfoVO;
import com.jimi.uw_server.service.InventoryTaskService;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ResultUtil;
import com.jimi.uw_server.util.TokenBox;

/**
 * 
 * @author trjie
 * @createTime 2019年5月17日  下午2:30:53
 */

public class InventoryTaskController extends Controller {

	public static final String SESSION_KEY_LOGIN_USER = "loginUser";
	
	private InventoryTaskService inventoryTaskService = InventoryTaskService.me;
	
	@Log("创建盘点任务，供应商编号为{supplierId}")
	public void createInventoryTask(Integer supplierId) {
		
		if (supplierId == null) {
			throw new ParameterException("参数不能为空！");
		}
		String result = inventoryTaskService.createInventoryTask(supplierId);
		
		renderJson(ResultUtil.succeed(result));
	}
	

	/**
	 * 开始盘点任务
	 * @param taskId
	 * @return
	 */
	@Log("开始盘点任务，任务ID为{taskId}，仓口为{windows}")
	public void startInventoryTask(Integer taskId, String windows) {
		if (taskId == null || windows == null || windows.trim().equals("")) {
			throw new ParameterException("参数不能为空！");
		}
		String result = inventoryTaskService.startInventoryTask(taskId, windows);
		renderJson(ResultUtil.succeed(result));
		
	}
	
	
	/**
	 * 让盘点任务的叉车回库
	 * @param taskId
	 * @param boxId
	 * @param windowId
	 * @param user
	 * @return
	 */
	@Log("盘点料盒回库，任务ID为{taskId}，仓口为{windowId}，料盒ID为{boxId}")
	public void backInventoryBox(Integer taskId, Integer boxId, Integer windowId) {
		
		if (taskId == null || boxId == null || windowId == null ) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.backInventoryBox(taskId, boxId, windowId, user);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	/**
	 * 盘点物料
	 * @param materialId
	 * @param boxId
	 * @param taskId
	 * @param acturalNum
	 * @param user
	 * @return
	 */
	@Log("盘点物料uw，料盘码为{materialId}，料盒号为{boxId}，任务ID为{taskId}，盘点数量为{acturalNum}")
	public void inventoryMaterial(String materialId, Integer boxId, Integer taskId, Integer acturalNum) {
		if (materialId == null || taskId == null || boxId == null || acturalNum == null ) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.inventoryMaterial(materialId, boxId, taskId, acturalNum, user);
		renderJson(ResultUtil.succeed(result));
		
	}
	
	
	/**
	 * 平外仓物料
	 * @param id
	 * @param taskId
	 * @param user
	 * @return
	 */
	@Log("平仓EWH，记录ID为{id}，任务ID为{taskId}")
	public void coverEWhMaterial(Integer id, Integer taskId) {
		if (id == null || taskId == null ) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.coverEWhMaterial(id, taskId, user);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	/**
	 * UW平仓，根据记录ID和任务ID
	 * @param id
	 * @param taskId
	 * @param user
	 * @return
	 */
	@Log("平仓UW， 记录ID为{id}，任务ID为{taskId}")
	public void coverMaterial(Integer id, Integer taskId) {
		if (id == null || taskId == null ) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.coverMaterial(id, taskId, user);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	/**
	 * 批量平仓，根据任务ID和物料类型ID
	 * @param materialTypeId
	 * @param taskId
	 * @param user
	 * @return
	 */
	@Log("批量平仓，物料类型ID为{materialTypeId}，任务ID为{taskId}")
	public void coverMaterialByTaskId(Integer materialTypeId, Integer taskId) {
		if (materialTypeId == null || taskId == null ) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.coverMaterialByTaskId(materialTypeId, taskId, user);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	/**
	 * 审核任务
	 * @param taskId
	 * @return
	 */
	@Log("审核任务，任务ID为{taskId}")
	public void checkInventoryTask(Integer taskId) {
		if (taskId == null ) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.checkInventoryTask(taskId, user);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	/**
	 * 导入外仓盘点数据
	 * @param file
	 * @param taskId
	 * @param user
	 * @return
	 */
	@Log("导入物料仓盘点数据，任务ID为{taskId}")
	public void importEWhInventoryRecord(UploadFile file, Integer taskId) {
		
		if (file == null || taskId == null) {
			throw new ParameterException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.importEWhInventoryRecord(file.getFile(), taskId, user);
		if (result.equals("导入成功")) {
			renderJson(ResultUtil.succeed());
		}else {
			renderJson(ResultUtil.failed(result));
		}
	}
	
	
	/**
	 * 获取当前仓口的盘点物料清单
	 * @param windowId
	 * @return
	 */
	public void getPackingInventory(Integer windowId){
		
		if (windowId == null) {
			throw new ParameterException("参数不能为空");
		}
		PackingInventoryInfoVO result = inventoryTaskService.getPackingInventory(windowId);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	/**
	 * 根据供应商获取盘点任务(下拉框使用)
	 * @param supplierId
	 * @return
	 */
	public void getInventoryTask(Integer supplierId) {
		if (supplierId == null) {
			throw new ParameterException("参数不能为空");
		}
		List<Task> result = inventoryTaskService.getInventoryTask(supplierId);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	public void selectAllInventoryTask(String filter ,Integer pageNo, Integer pageSize){
		
		if (pageNo == null || pageSize == null) {
			throw new ParameterException("参数不能为空");
		}
		if (pageNo <= 0 || pageSize <= 0) {
			throw new ParameterException("页码和页容量必须大于0");
		}
		PagePaginate result = inventoryTaskService.selectAllInventoryTask(filter, pageNo, pageSize);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	public void getInventoryTaskInfo(Integer taskId, String no) {

		if (taskId == null) {
			throw new ParameterException("参数不能为空");
		}
		/*if (taskId == null || no == null || pageNo == null || pageSize == null) {
			throw new ParameterException("参数不能为空");
		}
		if (pageNo <= 0 || pageSize <= 0) {
			throw new ParameterException("页码和页容量必须大于0");
		}*/
		List<Record> result = inventoryTaskService.getInventoryTaskInfo(taskId, no);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	public void getInventoryTaskDetails(Integer taskId, Integer materialTypeId) {

		if (taskId == null) {
			throw new ParameterException("参数不能为空");
		}
		/*if (taskId == null || materailTypeId == null || pageNo == null || pageSize == null) {
			throw new ParameterException("参数不能为空");
		}
		if (pageNo <= 0 || pageSize <= 0) {
			throw new ParameterException("页码和页容量必须大于0");
		}*/
		List<InventoryTaskDetailVO> inventoryTaskDetailVOs = inventoryTaskService.getInventoryTaskDetails(taskId, materialTypeId);
		renderJson(ResultUtil.succeed(inventoryTaskDetailVOs));
	}
	
	
	public void getUnStartInventoryTask(Integer supplierId) {
		if (supplierId == null) {
			throw new ParameterException("参数不能为空");
		}
		List<Task> tasks = inventoryTaskService.getUnStartInventoryTask(supplierId);
		renderJson(ResultUtil.succeed(tasks));
	}
	
	
	/**
	 * 完成盘点任务
	 * @param taskId
	 */
	@Log("完成盘点任务，任务ID为{taskId}")
	public void finishInventoryTask(Integer taskId) {
		
		if (taskId == null) {
			throw new ParameterException("参数不能为空");
		}
		String result = inventoryTaskService.finishInventoryTask(taskId);
		renderJson(ResultUtil.succeed(result));
	}
	
	
	@Log("导出盘点报表")
	public void exportEWhReport(Integer taskId, String no) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空");
		}
		String fileName = inventoryTaskService.getInventoryTaskName(taskId);
		if (fileName == null) {
			throw new OperationException("任务不存在，导出失败！");
		}
		fileName = fileName + ".xlsx";
		OutputStream output = null;
		try {
			// 设置响应，只能在controller层设置，因为getResponse()方法只能在controller层调用
			HttpServletResponse response = getResponse();
			response.reset();
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), "ISO8859-1"));
			response.setContentType("application/vnd.ms-excel");
			output = response.getOutputStream();
			inventoryTaskService.exportInventoryTask(taskId, no, fileName, output);
			
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
}
