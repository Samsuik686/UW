/**  
*  
*/
package com.jimi.uw_server.controller;

import java.util.Date;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.vo.IOTaskDetailVO;
import com.jimi.uw_server.model.vo.TaskVO;
import com.jimi.uw_server.service.PdaClientService;
import com.jimi.uw_server.util.ResultUtil;
import com.jimi.uw_server.util.TokenBox;

/**
 * <p>
 * Title: PdaClientController
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2019
 * </p>
 * <p>
 * Company: 惠州市几米物联技术有限公司
 * </p>
 * 
 * @author trjie
 * @date 2020年5月28日
 *
 */
public class PdaClientController extends Controller {

	private PdaClientService pdaClientService = Aop.get(PdaClientService.class);

	private static final String SESSION_KEY_LOGIN_USER = "loginUser";


	public void getWorkingEmergencyRegularTasks() {
		List<TaskVO> taskVOs = pdaClientService.getWorkingEmergencyRegularTasks();
		renderJson(ResultUtil.succeed(taskVOs));
	}


	public void getEmergencyRegularTaskInfo(Integer taskId, String no) {
		if (taskId == null || StrKit.isBlank(no)) {
			throw new ParameterException("参数不能为空！");
		}
		List<IOTaskDetailVO> ioTaskDetailVOs = pdaClientService.getEmergencyRegularTaskInfo(taskId, no);
		renderJson(ResultUtil.succeed(ioTaskDetailVOs));
	}


	/**
	 * 
	 * <p>
	 * Description: 紧急出库料盘
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月28日
	 */
	public void outEmergencyRegular(String taskId, String no, String materialId, Integer quantity, Date productionTime, String supplierName, String cycle, String manufacturer, Date printTime) {

		if (taskId == null || materialId == null || quantity == null || supplierName == null || productionTime == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		pdaClientService.outEmergencyRegular(taskId, no, materialId, quantity, productionTime, supplierName, cycle, manufacturer, printTime, user);
		renderJson(ResultUtil.succeed());
	}


	/**
	 * <p>
	 * Description: 获取进行中的贵重仓盘点任务列表
	 * <p>
	 * 
	 * @return
	 * @exception @Time 2019年11月27日
	 */
	public void getWorkingPreciousInventoryTasks() {
		List<TaskVO> taskVOs = pdaClientService.getWorkingPreciousInventoryTasks();
		renderJson(ResultUtil.succeed(taskVOs));
	}


	/**
	 * <p>
	 * Description: 贵重仓盘点料盘
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月28日
	 */
	public void inventoryPreciousUWMaterial(String materialId, Integer taskId, Integer acturalNum) {
		if (taskId == null || materialId == null || acturalNum == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		pdaClientService.inventoryPreciousUWMaterial(materialId, taskId, acturalNum, user);
		renderJson(ResultUtil.succeed());
	}


	/**
	 * 
	 * <p>
	 * Description: 获取未盘点的料盘数
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月28日
	 */
	public void getUnScanInventoryTaskMaterial(Integer taskId, String no, Integer supplierId) {
		if (taskId == null || supplierId == null || StrKit.isBlank(no)) {
			throw new ParameterException("参数不能为空！");
		}
		Integer size = pdaClientService.getUnScanInventoryTaskMaterial(taskId, no, supplierId);
		renderJson(ResultUtil.succeed(size));
	}


	/**
	 * <p>
	 * Description: 获取进行中的贵重仓抽检任务列表
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2019年11月27日
	 */
	public void getWorkingPreciousSampleTasks() {
		List<TaskVO> taskVOs = pdaClientService.getWorkingPreciousSampleTasks();
		renderJson(ResultUtil.succeed(taskVOs));
	}


	/**
	 * <p>
	 * Description: 贵重仓抽检料盘
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月28日
	 */
	public void samplePreciousUWMaterial(String materialId, Integer taskId) {
		if (taskId == null || materialId == null) {
			throw new ParameterException("参数不能为空，请检查料盘二维码格式！");
		}
		pdaClientService.samplePreciousUWMaterial(materialId, taskId);
		renderJson(ResultUtil.succeed());
	}


	/**
	 * 
	 * <p>
	 * Description: 获取未抽检的料盘数
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月28日
	 */
	public void getUnScanSampleTaskMaterial(Integer taskId, String no, Integer supplierId) {
		if (taskId == null || supplierId == null || StrKit.isBlank(no)) {
			throw new ParameterException("参数不能为空！");
		}
		Integer size = pdaClientService.getUnScanSampleTaskMaterial(taskId, no, supplierId);
		renderJson(ResultUtil.succeed(size));
	}
}
