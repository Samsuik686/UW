package com.jimi.uw_server.controller;

import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.service.ExternalWhTaskService;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ResultUtil;
import com.jimi.uw_server.util.TokenBox;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletResponse;

import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;


/**
 * 
 * @author trjie
 * @createTime 2019年5月10日  下午2:16:15
 */

public class ExternalWhController extends Controller {

	public static final String SESSION_KEY_LOGIN_USER = "loginUser";

	private static ExternalWhTaskService externalWhTaskService = Enhancer.enhance(ExternalWhTaskService.class);


	@Log("导入物料仓任务，供应商ID为{supplierId}，源物料仓ID为{souceWhId}，目的物料仓ID为{destinationwhId}, 备注{remarks}")
	public void importTask(UploadFile file, Integer supplierId, Integer sourceWhId, Integer destinationwhId, String remarks) {

		if (file == null || supplierId == null || sourceWhId == null || destinationwhId == null || remarks == null || remarks.equals("")) {
			throw new ParameterException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = externalWhTaskService.importTask(file.getFile(), supplierId, sourceWhId, destinationwhId, user, remarks);
		if (result.equals("导入成功")) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed(result));
		}
	}


	@Log("导入物料仓损耗任务，供应商ID为{supplierId}，物料仓ID为{whId}, 备注{remarks}")
	public void importWastageTask(UploadFile file, Integer supplierId, Integer whId, String remarks) {

		if (file == null || supplierId == null || whId == null) {
			throw new ParameterException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = externalWhTaskService.importWastageTask(file.getFile(), supplierId, whId, user, remarks);
		if (result.equals("导入成功")) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed(result));
		}
	}


	/**
	 * 添加某仓库，某物料的损耗记录
	 * @param materialTypeId
	 * @param whId
	 * @param quantity
	 * @param user
	 * @return
	 */
	@Log("添加物料仓损耗记录，物料类型ID为{materialTypeId}，物料仓ID为{whId}，数量为{quantity}, 备注{remarks}")
	public void addWorstageLog(Integer materialTypeId, Integer whId, Integer quantity, String remarks) {
		if (materialTypeId == null || whId == null || quantity == null) {
			throw new ParameterException("参数不能为空");
		}
		// 获取当前使用系统的用户，以便获取操作员uid
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String result = externalWhTaskService.addWorstageLog(materialTypeId, whId, quantity, user, remarks);
		if (result.equals("操作成功")) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed(result));
		}

	}


	@Log("删除物料仓记录，记录ID为{logId}")
	public void deleteExternalWhLog(Integer logId) {
		if (logId == null) {
			throw new ParameterException("参数不能为空");
		}
		String result = externalWhTaskService.deleteExternalWhLog(logId);
		if (result.equals("操作成功")) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.failed(result));
		}

	}


	@Log("查询物料仓记录，页码为{pageNo}， 页容量为{pageSize}，物料仓ID为{whId}，供应商ID为{supplierId}，料号为{no}")
	public void selectExternalWhInfo(Integer pageNo, Integer pageSize, Integer whId, Integer supplierId, String no, String ascBy, String descBy) {
		if (pageNo == null || pageSize == null) {
			throw new ParameterException("参数不能为空");
		}
		if (pageNo <= 0 || pageSize <= 0) {
			throw new ParameterException("页码和页容量必须大于0");
		}
		PagePaginate result = externalWhTaskService.selectExternalWhInfo(pageNo, pageSize, whId, supplierId, no, ascBy, descBy);
		renderJson(ResultUtil.succeed(result));
	}


	@Log("查询物料仓详细记录，页码为{pageNo}， 页容量为{pageSize}，物料仓ID为{whId}，物料类型Id为{materialTypeId}")
	public void selectEWhMaterialDetails(Integer pageNo, Integer pageSize, Integer materialTypeId, Integer whId) {
		if (pageNo == null || pageSize == null || whId == null || materialTypeId == null) {
			throw new ParameterException("参数不能为空");
		}
		if (pageNo <= 0 || pageSize <= 0) {
			throw new ParameterException("页码和页容量必须大于0");
		}
		PagePaginate result = externalWhTaskService.selectEWhMaterialDetails(pageNo, pageSize, materialTypeId, whId);
		renderJson(ResultUtil.succeed(result));

	}


	@Log("导出物料仓库存报表")
	public void exportEWhReport(Integer whId, Integer supplierId, String no) {
		OutputStream output = null;
		try {
			// 设置响应，只能在controller层设置，因为getResponse()方法只能在controller层调用
			String fileName = getFileName(new Date()) + ".xlsx";
			HttpServletResponse response = getResponse();
			response.reset();
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), "ISO8859-1"));
			response.setContentType("application/vnd.ms-excel");
			output = response.getOutputStream();
			externalWhTaskService.exportEWhReport(whId, supplierId, no, fileName, output);
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


	public String getFileName(Date date) {

		String fileName = "物料仓报表_";
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = formatter.format(date);
		fileName = fileName + time;
		return fileName;
	}

}
