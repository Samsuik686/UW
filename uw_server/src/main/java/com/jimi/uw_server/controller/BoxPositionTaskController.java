/**  
*  
*/  
package com.jimi.uw_server.controller;

import java.io.File;

import com.jfinal.aop.Aop;
import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.service.MaterialBoxMoveService;
import com.jimi.uw_server.util.ResultUtil;

/**  
 * <p>Title: BoxPositionTaskController</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月3日
 *
 */
public class BoxPositionTaskController extends Controller {
	
	private static MaterialBoxMoveService boxMoveService = Aop.get(MaterialBoxMoveService.class);

	/**
	 * 
	 * <p>Description: 创建任务<p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月3日
	 */
	@Log("创建料盒搬运任务")
	public void createMoveTask(UploadFile uploadFile) {
		try {
			uploadFile = getFile();
			
			File file = uploadFile.getFile();
			if (!file.getName().endsWith(".xls") && !file.getName().endsWith(".xlsx")) {
				throw new ParameterException("文件格式错误");
			}
			boxMoveService.createMoveTask(file);
			renderJson(ResultUtil.succeed());
		} finally {
			if(uploadFile.getFile() != null) {
				uploadFile.getFile().delete();
			}
		}
		

		
	}
	
	
	/**
	 * <p>Description: 开始<p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月3日
	 */	
	@Log("开始料盒搬运任务，ID：{taskId}")
	public void start(Integer taskId) {
		
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		boxMoveService.start(taskId);
		renderJson(ResultUtil.succeed());

	}
	
	
	/**
	 * 
	 * <p>Description: 暂停/开始任务<p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月3日
	 */
	@Log("暂停/开始料盒搬运任务，ID：{taskId}， Flag：{flag}")
	public void switchTask(Integer taskId, Boolean flag) {
		if (taskId == null || flag == null) {
			throw new ParameterException("参数不能为空！");
		}
		boxMoveService.switchTask(taskId, flag);
		renderJson(ResultUtil.succeed());
	}

	
	/**
	 * 
	 * <p>Description: 作废任务<p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月3日
	 */
	@Log("作废料盒搬运任务，ID：{taskId}")
	public void cancelTask(Integer taskId) {
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		boxMoveService.cancelTask(taskId);
		renderJson(ResultUtil.succeed());
	}
	
	
	/**
	 * 
	 * <p>Description:获取所有料盒搬运任务 <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月3日
	 */
	public void listBoxPositionTask(){
		renderJson(ResultUtil.succeed(boxMoveService.listBoxPositionTask()));
	}
	
	
	/**
	 * 
	 * <p>Description: 根据料盒搬运任务ID 获取条目详情<p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月3日
	 */
	public void listBoxPositionTaskItem(Integer taskId){
		if (taskId == null) {
			throw new ParameterException("参数不能为空！");
		}
		renderJson(ResultUtil.succeed(boxMoveService.listBoxPositionTaskItem(taskId)));

	}
}
