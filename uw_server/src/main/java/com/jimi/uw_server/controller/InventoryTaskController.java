package com.jimi.uw_server.controller;

import com.jfinal.core.Controller;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.Destination;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.vo.InventoryTaskDetailVO;
import com.jimi.uw_server.model.vo.PackingInventoryInfoVO;
import com.jimi.uw_server.service.InventoryTaskService;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ResultUtil;
import com.jimi.uw_server.util.TokenBox;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;


/**
 * 
 * @author trjie
 * @createTime 2019年5月17日  下午2:30:53
 */

public class InventoryTaskController extends Controller {

	public static final String SESSION_KEY_LOGIN_USER = "loginUser";

	private InventoryTaskService inventoryTaskService = InventoryTaskService.me;


	@Log("创建普通仓盘点任务，客户编号为{supplierId}， 盘点仓库{destinationIds}")
	public void createRegularTask(Integer supplierId, String destinationIds) {

		if (supplierId == null) {
			throw new ParameterException("参数不能为空！");
		}
		inventoryTaskService.createRegularTask(supplierId, destinationIds);
		renderJson(ResultUtil.succeed());
	}


	@Log("创建贵重仓盘点任务，客户编号为{supplierId}")
	public void createPreciousTask( Integer supplierId) {

		if (supplierId == null) {
			throw new ParameterException("参数不能为空！");
		}
		inventoryTaskService.createPreciousTask(supplierId);
		renderJson(ResultUtil.succeed());
	}


	/**
	 * 开始盘点任务
	 * @param taskId
	 * @return
	 */
	@Log("开始普通仓盘点任务，任务ID为{taskId}，仓口为{windows}")
	public void startRegularTask(Integer taskId, String windows) {
		if (taskId == null || windows == null || windows.trim().equals("")) {
			throw new ParameterException("参数不能为空！");
		}
		String result = inventoryTaskService.startRegularTask(taskId, windows);
		renderJson(ResultUtil.succeed(result));

	}


	/**
	 * 开始盘点任务
	 * @param taskId
	 * @return
	 */
	@Log("开始贵重仓盘点任务，任务ID为{taskId}")
	public void startPreciousTask(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		String result = inventoryTaskService.startPreciousTask(taskId);
		renderJson(ResultUtil.succeed(result));

	}

	
	@Log("作废普通仓盘点任务，任务ID为{taskId}")
	public void cancelRegularTask(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		inventoryTaskService.cancelTask(taskId);
		renderJson(ResultUtil.succeed());
	}
	
	
	@Log("作废贵重仓盘点任务，任务ID为{taskId}")
	public void cancelPreciousTask(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		inventoryTaskService.cancelTask(taskId);
		renderJson(ResultUtil.succeed());
	}
	
	
	/**
	 * 让盘点任务的叉车回库
	 * @param taskId
	 * @param boxId
	 * @param windowId
	 * @return
	 */
	@Log("盘点料盒回库，任务ID为{taskId}，仓口为{windowId}，料盒ID为{boxId}")
	public void backInventoryRegularUWBox(Integer taskId, Integer boxId, Integer windowId) {

		if (taskId == null || boxId == null || windowId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.backInventoryRegularUWBox(taskId, boxId, windowId, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 盘点UW物料
	 * @param materialId
	 * @param boxId
	 * @param taskId
	 * @param acturalNum
	 * @return
	 */
	@Log("盘点普通仓物料uw，料盘码为{materialId}，料盒号为{boxId}，任务ID为{taskId}，盘点数量为{acturalNum}")
	public void inventoryRegularUWMaterial(String materialId, Integer boxId, Integer taskId, Integer acturalNum) {
		if (materialId == null || taskId == null || boxId == null || acturalNum == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		Material result = inventoryTaskService.inventoryRegularUWMaterial(materialId, boxId, taskId, acturalNum, user);
		renderJson(ResultUtil.succeed(result));

	}


	/**
	 * 盘点UW物料
	 * @param materialId
	 * @param taskId
	 * @param acturalNum
	 * @return
	 */
	@Log("盘点贵重仓物料uw，料盘码为{materialId}，料盒号为{boxId}，任务ID为{taskId}，盘点数量为{acturalNum}")
	public void inventoryPreciousUWMaterial(String materialId, Integer taskId, Integer acturalNum) {
		if (materialId == null || taskId == null || acturalNum == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		inventoryTaskService.inventoryPreciousUWMaterial(materialId, taskId, acturalNum, user);
		renderJson(ResultUtil.succeed());

	}


	/**
	 * 平外仓物料
	 * @param id
	 * @param taskId
	 * @return
	 */
	@Log("普通仓平仓EWH，记录ID为{id}，任务ID为{taskId}")
	public void coverRegularEWhMaterial(Integer id, Integer taskId) {
		if (id == null || taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.coverRegularEWhMaterial(id, taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * UW平仓，根据记录ID和任务ID
	 * @param id
	 * @param taskId
	 * @return
	 */
	@Log("普通仓平仓UW， 记录ID为{id}，任务ID为{taskId}")
	public void coverRegularUWMaterial(Integer id, Integer taskId) {
		if (id == null || taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.coverUwMaterial(id, taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * UW平仓，根据记录ID和任务ID
	 * @param id
	 * @param taskId
	 * @return
	 */
	@Log("贵重仓平仓UW， 记录ID为{id}，任务ID为{taskId}")
	public void coverPreciousUWMaterial(Integer id, Integer taskId) {
		if (id == null || taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.coverUwMaterial(id, taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 批量平仓，根据任务ID和物料类型ID
	 * @param materialTypeId
	 * @param taskId
	 * @return
	 */
	@Log("普通仓UW批量平仓，物料类型ID为{materialTypeId}，任务ID为{taskId}")
	public void coverRegularUWMaterialByTaskId(Integer materialTypeId, Integer taskId) {
		if (materialTypeId == null || taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.coverUwMaterialByTaskId(materialTypeId, taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 批量平仓，根据任务ID和物料类型ID
	 * @param materialTypeId
	 * @param taskId
	 * @return
	 */
	@Log("贵重仓UW批量平仓，物料类型ID为{materialTypeId}，任务ID为{taskId}")
	public void coverPreciousUWMaterialByTaskId(Integer materialTypeId, Integer taskId) {
		if (materialTypeId == null || taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.coverUwMaterialByTaskId(materialTypeId, taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 批量平仓，根据任务ID和物料类型ID
	 * @param materialTypeId
	 * @param taskId
	 * @return
	 */
	@Log("普通仓外仓批量平仓，物料类型ID为{materialTypeId}，任务ID为{taskId}, 目的仓库ID为{whId}")
	public void coverRegularEwhMaterialByTaskId(Integer materialTypeId, Integer taskId, Integer whId) {
		if (materialTypeId == null || taskId == null || whId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.coverEwhMaterialByTaskId(materialTypeId, taskId, whId, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 审核任务
	 * @param taskId
	 * @return
	 */
	@Log("审核UW普通仓盘点任务，任务ID为{taskId}")
	public void checkRegularTask(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.checkUwRegularTask(taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("审核UW贵重仓盘点任务，任务ID为{taskId}")
	public void checkPreciousTask(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.checkUwPreciousTask(taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("审核物料仓盘点任务，任务ID为{taskId}, 目的仓库ID为{whId}")
	public void checkEwhInventoryTask(Integer taskId, Integer whId) {
		if (taskId == null || whId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.checkEwhInventoryTask(taskId, whId, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 导入外仓盘点数据
	 * @param file
	 * @param taskId
	 * @return
	 */
	@Log("导入物料仓盘点数据，任务ID为{taskId}, 目的仓库ID为{whId}")
	public void importEWhInventoryRecord(UploadFile file, Integer taskId, Integer whId) {

		if (file == null || taskId == null || whId == null) {
			throw new ParameterException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.importEWhInventoryRecord(file.getFile(), taskId,whId, user);
		if (result.equals("导入成功")) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed(result));
		}
	}


	/**
	 * 获取当前仓口的盘点物料清单
	 * @param windowId
	 * @return
	 */
	public void getPackingInventory(Integer windowId) {

		if (windowId == null) {
			throw new ParameterException("参数不能为空");
		}
		List<PackingInventoryInfoVO> result = inventoryTaskService.getPackingInventory(windowId);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 根据客户获取盘点任务(下拉框使用)
	 * @param supplierId
	 * @return
	 */
	public void getInventoryTask(Integer supplierId, Integer warehouseType) {
		if (supplierId == null || warehouseType == null) {
			throw new ParameterException("参数不能为空");
		}
		List<Task> result = inventoryTaskService.getInventoryTask(supplierId, warehouseType);
		renderJson(ResultUtil.succeed(result));
	}


	// 获取进行中的贵重仓盘点任务（PDA用）
	public void getWorkingPreciousTask() {
		String filter = "warehouse_type=1#&#state=2";
		PagePaginate result = inventoryTaskService.selectAllInventoryTask(filter, null, null, null, null);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 查询所有盘点任务（简单信息）
	 * @param filter
	 * @param pageNo
	 * @param pageSize
	 */
	public void selectAllInventoryTask(String filter, Integer pageNo, Integer pageSize, String ascBy, String descBy) {

		if (pageNo == null || pageSize == null) {
			throw new ParameterException("参数不能为空");
		}
		if (pageNo <= 0 || pageSize <= 0) {
			throw new ParameterException("页码和页容量必须大于0");
		}
		PagePaginate result = inventoryTaskService.selectAllInventoryTask(filter, pageNo, pageSize, ascBy, descBy);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 根据任务ID和料号查询UW仓盘点任务信息（较为详细，粒度：料号）
	 * @param taskId
	 * @param no
	 */
	public void getUwInventoryTaskInfo(Integer taskId, String no) {

		if (taskId == null) {
			throw new ParameterException("参数不能为空");
		}
		List<Record> result = inventoryTaskService.getUwInventoryTaskInfo(taskId, no);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 根据任务ID和料号查询物料仓盘点任务信息（较为详细，粒度：料号）
	 * @param taskId
	 * @param no
	 */
	public void getEwhInventoryTaskInfo(Integer taskId, Integer whId, String no) {

		if (taskId == null || whId == null) {
			throw new ParameterException("参数不能为空");
		}
		List<Record> result = inventoryTaskService.getEwhInventoryTaskInfo(taskId, whId, no);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 根据任务ID和物料ID查询UW仓盘点任务信息(详细，粒度：料盘）
	 * @param taskId
	 * @param materialTypeId
	 */
	public void getUwInventoryTaskDetails(Integer taskId, Integer materialTypeId) {

		if (taskId == null) {
			throw new ParameterException("参数不能为空");
		}
		List<InventoryTaskDetailVO> inventoryTaskDetailVOs = inventoryTaskService.getUwInventoryTaskDetails(taskId, materialTypeId);
		renderJson(ResultUtil.succeed(inventoryTaskDetailVOs));
	}


	/**
	 * 根据任务ID和物料ID查询物料仓盘点任务信息(详细，粒度：仓库，料号）
	 * @param taskId
	 * @param materialTypeId
	 */
	public void getEwhInventoryTaskDetails(Integer taskId, Integer materialTypeId, Integer whId) {

		if (taskId == null || whId == null) {
			throw new ParameterException("参数不能为空");
		}
		List<InventoryTaskDetailVO> inventoryTaskDetailVOs = inventoryTaskService.getEwhInventoryTaskDetails(taskId, materialTypeId, whId);
		renderJson(ResultUtil.succeed(inventoryTaskDetailVOs));
	}

	/**
	 * 获取未开始的盘点任务
	 * @param supplierId
	 * @param warehouseType
	 * @param whId
	 */
	public void getUnStartInventoryTask(Integer supplierId, Integer warehouseType, Integer whId) {
		if (supplierId == null || whId == null) {
			throw new ParameterException("参数不能为空");
		}
		List<Task> tasks = inventoryTaskService.getUnStartInventoryTask(supplierId, warehouseType, whId);
		renderJson(ResultUtil.succeed(tasks));
	}

	
	/**
	 * 获取盘点任务绑定的目的仓库
	 * @param taskId
	 */
	public void getInventoryTaskDestination(Integer taskId) {
		
		if (taskId == null) {
			throw new ParameterException("参数不能为空");
		}
		List<Destination> destinations = inventoryTaskService.getInventoryTaskDestination(taskId);
		renderJson(ResultUtil.succeed(destinations));
	}
	
	
	/**
	 * 获取盘点任务基础信息，如每个仓库的审核时间，完成时间等
	 * @param taskId
	 */
	public void getInventoryTaskBaseInfo(Integer taskId) {
		
		if (taskId == null) {
			throw new ParameterException("参数不能为空");
		}
		List<Record> records = inventoryTaskService.getInventoryTaskBaseInfo(taskId);
		renderJson(ResultUtil.succeed(records));
	}

	/**
	 * 完成盘点任务
	 * @param taskId
	 */
	@Log("完成盘点任务，任务ID为{taskId}, 目的仓库ID为{whId}")
	public void finishRegularTask(Integer taskId, Integer whId) {

		if (taskId == null || whId == null) {
			throw new ParameterException("参数不能为空");
		}
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.finishRegularTask(taskId, whId, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 完成盘点任务
	 * @param taskId
	 */
	@Log("完成盘点任务，任务ID为{taskId}")
	public void finishPreciousTask(Integer taskId) {

		if (taskId == null) {
			throw new ParameterException("参数不能为空");
		}
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.finishPreciousTask(taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 修改盘点记录的盘点数量和退料盘盈
	 * @param id
	 * @param acturalNum
	 * @param returnNum
	 * @return
	 */
	@Log("修改盘点记录ID为{id}的盘点数量为{acturalNum}，退料盘盈数为{returnNum}")
	public void editEwhInventoryLog(Integer id, Integer acturalNum, Integer returnNum) {
		if (id == null || acturalNum == null || returnNum == null) {
			throw new OperationException("参数不能为空！");
		}
		if (acturalNum < 0 || returnNum < 0) {
			throw new OperationException("数量不能小于0");
		}
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.editEwhInventoryLog(id, acturalNum, returnNum, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 一键平仓UW
	 * @param taskId
	 * @return
	 */
	@Log("一键平普通UW仓，任务ID{taskId}")
	public void coverRegularUWMaterialOneKey(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.coverUwMaterialOneKey(taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 一键平仓UW
	 * @param taskId
	 * @return
	 */
	@Log("一键平贵重UW仓，任务ID{taskId}")
	public void coverPreciousUWMaterialOneKey(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.coverUwMaterialOneKey(taskId, user);
		renderJson(ResultUtil.succeed(result));
	}


	/**
	 * 物料仓一键批量平仓
	 * @param taskId
	 * @return
	 */
	@Log("一键平物料仓，任务ID{taskId}, 目的仓库ID为{whId}")
	public void coverEwhMaterialOneKey(Integer taskId, Integer whId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = inventoryTaskService.coverEwhMaterialOneKey(taskId, whId, user);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("导出盘点报表,任务ID为{taskI}， 目的仓库ID为{whId}")
	public void exportEWhReport(Integer taskId, String no, Integer whId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空");
		}
		String fileName = inventoryTaskService.getTaskName(taskId);
		if (fileName == null) {
			throw new OperationException("任务不存在，导出失败！");
		}
		fileName = fileName + "物料仓_.xlsx";
		OutputStream output = null;
		try {
			// 设置响应，只能在controller层设置，因为getResponse()方法只能在controller层调用
			HttpServletResponse response = getResponse();
			response.reset();
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), "ISO8859-1"));
			response.setContentType("application/vnd.ms-excel");
			output = response.getOutputStream();
			inventoryTaskService.exportEwhInventoryTask(taskId, no, whId, fileName, output);

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


	@Log("导出UW仓盘点报表 {taskId},料号{no}")
	public void exportUwReport(Integer taskId, String no) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空");
		}
		String fileName = inventoryTaskService.getTaskName(taskId);
		if (fileName == null) {
			throw new OperationException("任务不存在，导出失败！");
		}
		fileName = fileName + "_UW.xlsx";
		OutputStream output = null;
		try {
			// 设置响应，只能在controller层设置，因为getResponse()方法只能在controller层调用
			HttpServletResponse response = getResponse();
			response.reset();
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), "ISO8859-1"));
			response.setContentType("application/vnd.ms-excel");
			output = response.getOutputStream();
			inventoryTaskService.exportUwInventoryTask(taskId, no, fileName, output);

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
