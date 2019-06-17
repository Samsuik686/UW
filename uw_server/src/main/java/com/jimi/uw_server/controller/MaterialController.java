package com.jimi.uw_server.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletResponse;

import com.jfinal.aop.Enhancer;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.util.ResultUtil;


/**
 * 物料控制层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class MaterialController extends Controller {

	private static MaterialService materialService = Enhancer.enhance(MaterialService.class);


	// 统计物料类型信息
	public void count(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(materialService.count(pageNo, pageSize, ascBy, descBy, filter)));
	}

	
	// 统计超过期限的物料类型信息
		public void getOverdueMaterial(Integer pageNo, Integer pageSize, Integer day) {
			if ((pageNo != null && pageSize == null) || (pageNo == null && pageSize != null)) {
				throw new ParameterException("页码和页数只能同时为空或同时不为空！");
			}
			if (pageNo != null && pageSize != null && (pageNo <= 0 || pageSize <= 0 )) {
				throw new ParameterException("页码和页数必须为正整数！");
			}
			if (day == null || day < 0) {
				throw new ParameterException("天数必须为非负整数！");
			}
			renderJson(ResultUtil.succeed(materialService.getOverdueMaterial(pageNo, pageSize, day)));
	}
		
		
	// 获取物料实体
	public void getEntities(Integer type, Integer box, Integer pageNo, Integer pageSize) {
		renderJson(ResultUtil.succeed(materialService.getEntities(type, box, pageNo, pageSize)));
	}


	// 添加物料类型#
	@Log("添加料号为{no}的物料类型，规格号为{specification}，供应商名为{supplier}，厚度为{thickness}，直径为{radius}")
	public void addType(String no, String specification, String supplier, Integer thickness, Integer radius) {
		String resultString = materialService.addType(no, specification, supplier, thickness, radius);
		if(resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	// 更新物料类型#
	@Log("更新物料类型号为{id}的物料类型,传递的enabeld值为{enabled}(0表示执行删除,1表示不执行删除操作)，厚度为{thickness}，半径为{radius}")
	public void updateType(Integer id, Boolean enabled, Integer thickness, Integer radius) {
		String resultString = materialService.updateType(id, enabled, thickness, radius);
		if(resultString.equals("更新成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}

	@Log("批量删除物料类型号为[{filter}]的物料类型")
	public void deleteByIds(String filter) {
		if (filter == null || filter.equals("")) {
			throw new OperationException("参数不能为空");
		}
		String resultString = materialService.deleteByIds(filter);
		if(resultString.equals("删除成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.succeed(resultString));
		}
	}

	// 获取料盒信息
	public void getBoxes(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(materialService.getBoxes(pageNo, pageSize, ascBy, descBy, filter)));
	}

	// 添加料盒#
	@Log("添加新的料盒，料盒的具体位置为：区域号{area}，行号{row}，列号{col}，高度{height}，供应商{supplierId}，是否标准料盒{isStandard}")
	public void addBox(String area, Integer row, Integer col, Integer height,  Integer supplierId, Boolean isStandard) {
		String resultString = materialService.addBox(area, row, col, height, supplierId, isStandard);
		if(resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		}else {
			throw new OperationException(resultString);
		}
	}


	// 更新料盒在架情况#
	@Log("更新料盒号为{id}的料盒在架情况，传递的isOnShelf值为：{isOnShelf}(0表示标记为不在架，1表示标记为在架)")
	public void updateBox(Integer id, Boolean isOnShelf) {
		if(materialService.updateBox(id, isOnShelf)) {
			renderJson(ResultUtil.succeed());
		}else {
			renderJson(ResultUtil.failed());
		}
	}


	// 更新料盒的启/禁用状态#
	@Log("更新料盒号为{id}的料盒的启/禁用状态，传递的enabeld值为：{enabled}(0表示标记为删除，1表示不标记为删除)")
	public void deleteBox(Integer id, Boolean enabled) {
		String resultString = materialService.deleteBox(id, enabled);
		if(resultString.equals("更新成功！")) {
			renderJson(ResultUtil.succeed());
		}else {
			throw new OperationException(resultString);
		}
	}


	// 获取物料出入库记录
	public void getMaterialRecords(Integer type, Integer materialTypeId, Integer destination, Integer pageNo, Integer pageSize, String startTime, String endTime) {
		if ((startTime != null && endTime == null) || (endTime != null && startTime == null)) {
			throw new OperationException("开始时间和结束时间仅可同时为空或同时不为空！");
		}
		if (startTime != null && endTime != null) {
			if (!isDate(startTime) || !isDate(endTime)) {
				throw new OperationException("开始时间和结束时间的日期格式不正确！");
			}
		}
		renderJson(ResultUtil.succeed(materialService.getMaterialRecords(type, materialTypeId, destination, startTime, endTime, pageNo, pageSize)));
	}


	// 导出物料报表
	public void exportMaterialReport(Integer supplier) {
		OutputStream output = null;
		Supplier s = Supplier.dao.findById(supplier);
		String supplierName = s.getName();
		try {
			// 设置响应，只能在controller层设置，因为getResponse()方法只能在controller层调用
			String fileName = supplierName + "物料报表.xlsx";
			HttpServletResponse response = getResponse();
			response.reset();
			response.setHeader("Content-Disposition", "attachment; filename=" + new String(fileName.getBytes(), "ISO8859-1"));
			response.setContentType("application/vnd.ms-excel");
			output = response.getOutputStream();
			materialService.exportMaterialReport(supplier, fileName, output);
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
	

	// 获取料盒类型信息
	public void getBoxTypes(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(materialService.getBoxTypes(pageNo, pageSize, ascBy, descBy, filter)));
	}

	
	// 添加料盒#
	@Log("添加新的料盒类型，规格为{cellWidth}，总行数{cellRows}，总列数{cellCols}")
	public void addBoxType(Integer cellWidth, Integer cellRows, Integer cellCols) {
		String resultString = materialService.addBoxType(cellWidth, cellRows, cellCols);
		if(resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		}else {
			throw new OperationException(resultString);
		}
	}


	// 更新料盒信息#
	@Log("更新料盒类型号为{id}的料盒类型，传递的enabeld值为：{enabled}(0表示标记为删除，1表示不标记为删除)")
	public void deleteBoxType(Integer id, Boolean enabled) {
		String resultString = materialService.deleteBoxType(id, enabled);
		if(resultString.equals("更新成功！")) {
			renderJson(ResultUtil.succeed());
		}else {
			throw new OperationException(resultString);
		}
	}


	// 导入物料类型表#
	@ActionKey("/manage/material/import")
	@Log("导入物料类型表，导入的物料对应的供应商为：{supplierName}")
	public void importFile(UploadFile file, String supplierName) throws Exception {
		String fileName = file.getFileName();
		String fullFileName = file.getUploadPath() + File.separator + file.getFileName();
		String resultString = materialService.importFile(fileName, fullFileName, supplierName);
		if(resultString.equals("导入成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
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
