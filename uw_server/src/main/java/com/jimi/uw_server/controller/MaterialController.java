package com.jimi.uw_server.controller;

import com.jfinal.aop.Aop;
import com.jfinal.core.Controller;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.service.SupplierService;
import com.jimi.uw_server.util.ResultUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;


/**
 * 物料控制层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class MaterialController extends Controller {

	private static MaterialService materialService = Aop.get(MaterialService.class);

	private static SupplierService supplierService = Aop.get(SupplierService.class);

	// 统计物料类型信息
	public void countMaterials(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		if ((pageNo != null && pageSize == null) || (pageNo == null && pageSize != null)) {
			throw new ParameterException("页码和页数只能同时为空或同时不为空！");
		}
		if (pageNo != null && pageSize != null && (pageNo <= 0 || pageSize <= 0)) {
			throw new ParameterException("页码和页数必须为正整数！");
		}
		renderJson(ResultUtil.succeed(materialService.countMaterials(pageNo, pageSize, ascBy, descBy, filter)));
	}


	// 统计超过期限的物料类型信息
	public void getOverdueMaterial(Integer pageNo, Integer pageSize, Integer day, Integer type, Integer companyId) {
		if (type == null || companyId == null) {
			throw new ParameterException("参数不能为空！");
		}
		if ((pageNo != null && pageSize == null) || (pageNo == null && pageSize != null)) {
			throw new ParameterException("页码和页数只能同时为空或同时不为空！");
		}
		if (pageNo != null && pageSize != null && (pageNo <= 0 || pageSize <= 0)) {
			throw new ParameterException("页码和页数必须为正整数！");
		}
		if (day == null || day < 0) {
			throw new ParameterException("天数必须为非负整数！");
		}
		renderJson(ResultUtil.succeed(materialService.getOverdueMaterial(pageNo, pageSize, day, type, companyId)));
	}


	public void getMaterialsByBox(Integer boxId, Integer pageNo, Integer pageSize) {
		if (boxId == null) {
			throw new ParameterException("参数不能为空！");
		}
		if ((pageNo != null && pageSize == null) || (pageNo == null && pageSize != null)) {
			throw new ParameterException("页码和页数只能同时为空或同时不为空！");
		}
		if (pageNo != null && pageSize != null && (pageNo <= 0 || pageSize <= 0)) {
			throw new ParameterException("页码和页数必须为正整数！");
		}
		renderJson(ResultUtil.succeed(materialService.getMaterialsByBox(boxId, pageNo, pageSize)));
	}

	
	public void getMaterialsByMaterialType(Integer materialTypeId, Integer pageNo, Integer pageSize) {
		if (materialTypeId == null) {
			throw new ParameterException("参数不能为空！");
		}
		if ((pageNo != null && pageSize == null) || (pageNo == null && pageSize != null)) {
			throw new ParameterException("页码和页数只能同时为空或同时不为空！");
		}
		if (pageNo != null && pageSize != null && (pageNo <= 0 || pageSize <= 0)) {
			throw new ParameterException("页码和页数必须为正整数！");
		}
		renderJson(ResultUtil.succeed(materialService.getMaterialsByMaterialType(materialTypeId, pageNo, pageSize)));
	}


	

	// 获取物料出入库记录
	public void getMaterialIOTaskRecords(Integer type, Integer materialTypeId, Integer destination, Integer pageNo, Integer pageSize, String startTime, String endTime) {
		if (pageNo == null || pageSize == 0) {
			throw new OperationException("页码和页容量不能为空！");
		}
		if (pageNo <= 0 || pageSize <= 0) {
			throw new OperationException("页码和页容量必须为正整数！");
		}
		if ((startTime != null && endTime == null) || (endTime != null && startTime == null)) {
			throw new OperationException("开始时间和结束时间仅可同时为空或同时不为空！");
		}
		if (startTime != null && endTime != null) {
			if (!isDate(startTime) || !isDate(endTime)) {
				throw new OperationException("开始时间和结束时间的日期格式不正确！");
			}
		}
		renderJson(ResultUtil.succeed(materialService.getMaterialIOTaskRecords(type, materialTypeId, destination, startTime, endTime, pageNo, pageSize)));
	}


	// 导出物料报表
	public void exportMaterialReport(Integer supplier, Integer type) {
		OutputStream output = null;
		if (supplier == null) {
			throw new ParameterException("参数不能为空！");
		}
		Supplier s = supplierService.getSupplierById(supplier);
		String supplierName = s.getName();
		try {
			// 设置响应，只能在controller层设置，因为getResponse()方法只能在controller层调用
			String fileName = supplierName + "物料报表.xlsx";
			HttpServletResponse response = getResponse();
			response.reset();
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), "ISO8859-1"));
			response.setContentType("application/vnd.ms-excel");
			output = response.getOutputStream();
			materialService.exportMaterialReport(s, fileName, output, type);
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


	// 导出物料报表
	public void exportMaterialDetialReport(Integer supplier, Integer type) {
		OutputStream output = null;
		if (supplier == null) {
			throw new ParameterException("参数不能为空！");
		}
		Supplier s = supplierService.getSupplierById(supplier);
		String supplierName = s.getName();
		if (type == null) {
			type = 0;
		}
		try {
			// 设置响应，只能在controller层设置，因为getResponse()方法只能在controller层调用
			String fileName = supplierName + "物料库存报表.xlsx";
			HttpServletResponse response = getResponse();
			response.reset();
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), "ISO8859-1"));
			response.setContentType("application/vnd.ms-excel");
			output = response.getOutputStream();
			materialService.exportMaterialDetialsReport(supplier, output, type);
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

	
	/**
	 * 判断是否符合日期格式
	 * @param time
	 * @return
	 */
	private boolean isDate(String time) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			format.parse(time);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
