/**  
*  
*/  
package com.jimi.uw_server.controller;

import com.jfinal.aop.Aop;
import com.jfinal.core.Controller;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.service.MaterialBoxService;
import com.jimi.uw_server.util.ResultUtil;

/**  
 * <p>Title: MaterialBoxController</p>  
 * <p>Description: 料盒控制层</p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月16日
 *
 */
public class MaterialBoxController extends Controller {

	MaterialBoxService materialBoxService = Aop.get(MaterialBoxService.class);
	
	// 获取料盒信息
	public void getBoxes(Integer companyId, Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		if (companyId == null) {
			throw new ParameterException("公司ID不能为空！");
		}
		if (pageNo == null || pageSize == null) {
			throw new OperationException("页码和页容量不能为空！");
		}
		if (pageNo <= 0 || pageSize <= 0) {
			throw new OperationException("页码和页容量必须为正整数！");
		}
		renderJson(ResultUtil.succeed(materialBoxService.getMaterialBoxes(companyId, pageNo, pageSize, ascBy, descBy, filter)));
	}


	// 添加料盒#
	@Log("添加新的料盒，料盒的具体信息为：区域号{area}，行号{row}，列号{col}，高度{height}，客户{supplierId}，是否标准料盒{isStandard}")
	public void addBox(String area, Integer row, Integer col, Integer height, Integer supplierId, Boolean isStandard) {
		if (area == null || row == null || col == null || height == null || supplierId == null || isStandard == null) {
			throw new ParameterException("参数不能为空！");
		}
		materialBoxService.addBox( area, row, col, height, supplierId, isStandard);;
		renderJson(ResultUtil.succeed());
	}


	// 更新料盒在架情况#
	@Log("更新料盒号为{id}的料盒在架情况，传递的isOnShelf值为：{isOnShelf}(0表示标记为不在架，1表示标记为在架)")
	public void updateBox(Integer id, Boolean isOnShelf) {
		materialBoxService.updateBox(id, isOnShelf);
		renderJson(ResultUtil.succeed());
	}

	@Log("更新料盒号为[{ids}]信息，客户为{supplierId}")
	public void editBoxOfSupplier(String ids, Integer supplierId) {
		if (supplierId == null || ids == null || ids.trim().equals("")) {
			throw new OperationException("参数不能为空");
		}
		materialBoxService.editBoxOfSupplier(ids, supplierId);
		renderJson(ResultUtil.succeed());
	}
	
	@Log("更新料盒号为[{ids}]信息，料盒类型为{materialBoxTypeId}")
	public void editBoxOfType(String ids, Integer materialBoxTypeId) {
		if (materialBoxTypeId == null || ids == null || ids.trim().equals("")) {
			throw new OperationException("参数不能为空");
		}
		materialBoxService.editBoxOfType(ids, materialBoxTypeId);
		renderJson(ResultUtil.succeed());
	}


	// 更新料盒的启/禁用状态#
	@Log("删除料盒号为{id}的料盒")
	public void deleteBox(Integer id) {
		materialBoxService.deleteBox(id);
		renderJson(ResultUtil.succeed());
	}
}
