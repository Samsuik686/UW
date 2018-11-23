package com.jimi.uw_server.controller;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.util.ResultUtil;


/**
 * 物料控制层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class MaterialController extends Controller {

	private static MaterialService materialService = Enhancer.enhance(MaterialService.class);

	public static final String SESSION_KEY_LOGIN_USER = "loginUser";


	// 统计物料类型信息
	public void count(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(materialService.count(pageNo, pageSize, ascBy, descBy, filter)));
	}


	// 获取物料实体
	public void getEntities(Integer type, Integer box, Integer pageNo, Integer pageSize) {
		renderJson(ResultUtil.succeed(materialService.getEntities(type, box, pageNo, pageSize)));
	}


	// 添加物料类型#
	@Log("添加料号为{no}的物料类型，规格号为{specification}，供应商名为{supplier}")
	public void addType(String no, String specification, String supplierName) {
		String resultString = materialService.addType(no, specification, supplierName);
		if(resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	// 更新物料类型#
	@Log("更新物料类型号为{id}的物料类型,传递的enabeld值为{enabled}(0表示执行删除,1表示不执行删除操作)")
	public void updateType(Integer id, String specification, String supplierName, Boolean enabled) {
		String resultString = materialService.updateType(id, specification, supplierName, enabled);
		if(resultString.equals("更新成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	// 统计物料类型信息
	public void getBoxes(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(materialService.getBoxes(pageNo, pageSize, ascBy, descBy, filter)));
	}


	// 添加物料类型#
	@Log("添加新的料盒,区域号为{area},行号为{row},列号为{col},高度为{height}")
	public void addBox(Integer area, Integer row, Integer col, Integer height) {
		String resultString = materialService.addBox(area, row, col, height);
		if(resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	// 更新物料类型#
	@Log("更新料盒号为{id}的料盒信息,传递的enabeld值为{enabled}(0表示执行删除,1表示不执行删除操作)")
	public void updateBox(@Para("") MaterialBox MaterialBox) {
		String resultString = materialService.updateBox(MaterialBox);
		if(resultString.equals("更新成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	public void getMaterialRecords(Integer type, Integer pageNo, Integer pageSize) {
		renderJson(ResultUtil.succeed(materialService.getMaterialRecords(type, pageNo, pageSize)));
	}


	public void exportMaterialReport() {
		OutputStream output = null;
		try {
			// 设置响应，只能在controller层设置，因为getResponse()方法只能在controller层调用
			String fileName = "物料报表";
			HttpServletResponse response = getResponse();
			response.reset();
			response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("utf-8"), "utf-8"));	// iso-8859-1
			response.setContentType("application/vnd.ms-excel");
			output = response.getOutputStream();
			materialService.exportMaterialReport(fileName, output);
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
