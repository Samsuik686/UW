/**  
*  
*/  
package com.jimi.uw_server.controller;

import java.util.Date;

import com.jfinal.aop.Aop;
import com.jfinal.core.Controller;
import com.jfinal.upload.UploadFile;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.service.MaterialTypeService;
import com.jimi.uw_server.util.PagePaginate;
import com.jimi.uw_server.util.ResultUtil;

/**  
 * <p>Title: MaterialTypeController</p>  
 * <p>Description: 物料类型控制层</p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月16日
 *
 */
public class MaterialTypeController extends Controller {

	private static MaterialTypeService materialTypeService = Aop.get(MaterialTypeService.class);
	
	@Log("添加普通仓料号为{no}的物料类型，规格号为{specification}，客户ID为{supplierId}，厚度为{thickness}，直径为{radius}，超发为{isSuperable}")
	public void addRegularMaterialType(String no, String specification, Integer supplierId, Integer thickness, Integer radius, Boolean isSuperable) {
		if (no == null || no.equals("") || specification == null || specification.equals("") || supplierId == null || thickness == null || radius == null || isSuperable == null) {
			throw new ParameterException("参数不能为空！");
		}
		String resultString = materialTypeService.addMaterialType(no, specification, supplierId, thickness, radius, WarehouseType.REGULAR.getId(), null, isSuperable);
		if (resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	// 添加物料类型#
	@Log("添加贵重仓料号为{no}的物料类型，规格号为{specification}，客户ID为{supplierId}，厚度为{thickness}，直径为{radius}， 位号为{designator}")
	public void addPreciousMaterialType(String no, String specification, Integer supplierId, Integer thickness, Integer radius, String designator) {
		if (no == null || no.equals("") || specification == null || specification.equals("") || supplierId == null || thickness == null || radius == null || designator == null || designator.equals("")) {
			throw new ParameterException("参数不能为空！");
		}
		String resultString = materialTypeService.addMaterialType(no, specification, supplierId, thickness, radius, WarehouseType.PRECIOUS.getId(), designator, false);
		if (resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	// 更新物料类型#
	@Log("更新普通仓物料类型号为{id}的物料类型, 厚度为{thickness}，直径为{radius}， 规格为{specification}，超发为{isSuperable}")
	public void updateRegularMaterialType(Integer id, Boolean enabled, Integer thickness, Integer radius, String specification, Boolean isSuperable) {
		String resultString = materialTypeService.updateMaterialType(id, thickness, radius, null, specification, WarehouseType.REGULAR.getId(), isSuperable);
		if (resultString.equals("更新成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	@Log("更新贵重仓物料类型号为{id}的物料类型，厚度为{thickness}，直径为{radius}， 行号为{designator}， 规格为{specification}")
	public void updatePreciousMaterialType(Integer id, Integer thickness, Integer radius, String designator, String specification) {
		String resultString = materialTypeService.updateMaterialType(id, thickness, radius, designator, specification, WarehouseType.PRECIOUS.getId(), false);
		if (resultString.equals("更新成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	@Log("批量删除普通仓物料类型号为[{filter}]的物料类型")
	public void deleteRegularMaterialByIds(String ids) {
		if (ids == null || ids.trim().equals("")) {
			throw new OperationException("参数不能为空");
		}
		String resultString = materialTypeService.deleteByIds(ids);
		if (resultString.equals("删除成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.succeed(resultString));
		}
	}


	@Log("批量删除贵重仓物料类型号为[{filter}]的物料类型")
	public void deletePreciousMaterialByIds(String ids) {
		if (ids == null || ids.trim().equals("")) {
			throw new OperationException("参数不能为空");
		}
		String resultString = materialTypeService.deleteByIds(ids);
		if (resultString.equals("删除成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			renderJson(ResultUtil.succeed(resultString));
		}
	}
	
	
	// 导入物料类型表#
	@Log("导入普通仓物料类型表，导入的物料对应的客户为：{supplierId}")
	public void importRegularMaterialTypeFile(UploadFile file, Integer supplierId) throws Exception {
		String fileName = file.getFileName();
		String resultString = materialTypeService.importRegularMaterialTypeFile(fileName, file.getFile(), supplierId);
		if (resultString.equals("导入成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}

	
	@Log("导入物料类型表，导入的物料对应的客户为：{supplierId}")
	public void importPreciousMaterialTypeFile(UploadFile file, Integer supplierId) throws Exception {
		String fileName = file.getFileName();
		String resultString = materialTypeService.importPreciousMaterialTypeFile(fileName, file.getFile(), supplierId);
		if (resultString.equals("导入成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}
	
	
	public void getMaterialTypeVOs(Integer pageNo, Integer pageSize, Integer supplierId, String no, String specification){
		if (supplierId == null) {
			throw new OperationException("参数不能为空");
		}
		if (pageNo == null || pageSize == null) {
			throw new OperationException("页码和页容量不能为空！");
		}
		if (pageNo <= 0 || pageSize <= 0) {
			throw new OperationException("页码和页容量必须为正整数！");
		}
		renderJson(ResultUtil.succeed(materialTypeService.getMaterialTypeVOs(pageNo, pageSize, supplierId, no, specification)));
	}
	
	
	public void getMaterialStockDetails(String no, String supplierId, Integer warehouseType, Integer whId, Integer pageNo, Integer pageSize, Date startTime,  Date endTime) {
		if (supplierId == null || warehouseType == null || startTime == null || endTime == null) {
			throw new OperationException("参数不能为空");
		}
		if (pageNo == null || pageSize == null) {
			throw new OperationException("页码和页容量不能为空！");
		}
		if (pageNo <= 0 || pageSize <= 0) {
			throw new OperationException("页码和页容量必须为正整数！");
		}
		if (startTime != null && endTime != null) {
			if (!endTime.after(startTime)) {
				throw new OperationException("开始时间需小于结束时间！");
			}
		}
		PagePaginate page = materialTypeService.getMaterialStockDetails(no, supplierId, warehouseType, whId, pageNo, pageSize, startTime, endTime);
		renderJson(ResultUtil.succeed(page));
 
	}
}
