/**  
*  
*/  
package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.constant.BoxState;
import com.jimi.uw_server.constant.MaterialBoxType;
import com.jimi.uw_server.constant.sql.MaterialBoxSQL;
import com.jimi.uw_server.constant.sql.MaterialSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Company;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.vo.MaterialBoxVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;

/**  
 * <p>Title: MaterialBoxService</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月13日
 *
 */
public class MaterialBoxService {

	private static SelectService selectService = Aop.get(SelectService.class);
	
	private static int batchSize = 2000;
	
	
	// 获取料盒信息
	public PagePaginate getMaterialBoxes(Integer companyId, Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null) {
			filter = filter.concat("#&#material_box.enabled=1#&#company.id=" + companyId);
		} else {
			filter = "material_box.enabled=1#&#company.id=" + companyId;
		}
		Page<Record> result = selectService.select(new String[] {"material_box", "supplier", "company"}, new String[] {"material_box.supplier=supplier.id", "material_box.company_id=company.id"}, pageNo, pageSize, ascBy, descBy, filter);
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		if (!result.getList().isEmpty()) {
			pagePaginate.setList(MaterialBoxVO.fillList(result.getList()));
		}else {
			pagePaginate.setList(Collections.emptyList());
		}
		return pagePaginate;
	}


	// 手动添加料盒
	public void addBox(Integer companyId, String area, Integer row, Integer col, Integer height, Integer supplierId, Boolean isStandard) {
		Company company = Company.dao.findById(companyId);
		if (company == null) {
			throw new OperationException("添加失败，公司不存在！");
		}
		Supplier supplier = Supplier.dao.findById(supplierId);
		if (supplier == null) {
			throw new OperationException("添加失败，客户不存在！");
		}
		MaterialBox materialBox = MaterialBox.dao.findFirst(MaterialBoxSQL.GET_ENABLED_MATERIAL_BOX_BY_POSITION_SQL, row, col, height);
		if (materialBox != null) {
			throw new OperationException("添加失败，该位置已有料盒存在！");
		}
		materialBox = new MaterialBox();
		if (isStandard) {
			materialBox.setType(MaterialBoxType.STANDARD);
		} else {
			materialBox.setType(MaterialBoxType.NONSTANDARD);
		}
		materialBox.setArea(area);
		materialBox.setRow(row);
		materialBox.setCol(col);
		materialBox.setHeight(height);
		materialBox.setIsOnShelf(true);
		materialBox.setEnabled(true);
		materialBox.setSupplier(supplierId);
		materialBox.setStatus(BoxState.EMPTY);
		materialBox.setCompanyId(companyId);
		materialBox.setUpdateTime(new Date());
		materialBox.save();
	}


	// 更新料盒在架/不在架状态
	public void updateBox(Integer id, Boolean isOnShelf) {
		MaterialBox materialBox = MaterialBox.dao.findById(id);
		materialBox.setIsOnShelf(isOnShelf);
		materialBox.update();
	}
		
		
	public void editBoxOfSupplier(String ids, Integer supplierId, Integer companyId) {
		String[] idStringArr = ids.split(",");
		List<Integer> idIntegerArr = new ArrayList<>(idStringArr.length);
		List<MaterialBox> materialBoxs = new ArrayList<>(idStringArr.length);
		Supplier supplier = Supplier.dao.findById(supplierId);
		Company company = Company.dao.findById(companyId);
		if (company == null) {
			throw new OperationException("修改失败，公司不存在！");
		}
		if (supplier == null) {
			throw new OperationException("修改失败，客户不存在!");
		}
		try {
			for (String idString : idStringArr) {
				idIntegerArr.add(Integer.valueOf(idString));
			}
			for (Integer idInteger : idIntegerArr) {
				MaterialBox materialBox = MaterialBox.dao.findById(idInteger);
				if (materialBox == null) {
					throw new OperationException("修改失败，料盒号为"+idInteger + "的料盒不存在！");
				}
				Material material = Material.dao.findFirst(MaterialSQL.GET_MATERIAL_BY_BOX_SQL, idInteger);
				if (material != null) {
					throw new OperationException("修改失败，料盒号为"+idInteger + "的料盒存在物料！");
				}
				if (!materialBox.getIsOnShelf()) {
					throw new OperationException("修改失败，料盒号为"+idInteger + "的料盒不在架！");
				}
				materialBox.setSupplier(supplier.getId());
				materialBox.setCompanyId(companyId);
				materialBoxs.add(materialBox);
			}
		} catch (NumberFormatException e) {
			throw new OperationException("修改失败，参数转换出错，存在非数字的ID值！");
		}
		if (!materialBoxs.isEmpty()) {
			Db.batchUpdate(materialBoxs, batchSize);
		}
		
	}
		
		
	public void editBoxOfType(String ids, Integer type) {
		String[] idStringArr = ids.split(",");
		List<Integer> idIntegerArr = new ArrayList<>(idStringArr.length);
		List<MaterialBox> materialBoxs = new ArrayList<>(idStringArr.length);
		if (type != MaterialBoxType.STANDARD && type != MaterialBoxType.NONSTANDARD) {
			throw new OperationException("修改失败，料盒类型仅有标准与非标准！");
		}
		
		try {
			for (String idString : idStringArr) {
				idIntegerArr.add(Integer.valueOf(idString));
			}
			for (Integer idInteger : idIntegerArr) {
				MaterialBox materialBox = MaterialBox.dao.findById(idInteger);
				if (materialBox == null) {
					throw new OperationException("修改失败，料盒号为"+idInteger + "的料盒不存在！");
				}
				Material material = Material.dao.findFirst(MaterialSQL.GET_MATERIAL_BY_BOX_SQL, idInteger);
				if (material != null) {
					throw new OperationException("修改失败，料盒号为"+idInteger + "的料盒存在物料！");
				}
				if (!materialBox.getIsOnShelf()) {
					throw new OperationException("修改失败，料盒号为"+idInteger + "的料盒不在架！");
				}
				materialBox.setType(type);
				materialBoxs.add(materialBox);
			}
		} catch (NumberFormatException e) {
			throw new OperationException("修改失败，参数转换出错，存在非数字的ID值！");
		}
		if (!materialBoxs.isEmpty()) {
			Db.batchUpdate(materialBoxs, batchSize);
		}
		
	}


	// 删除料盒
	public void deleteBox(Integer id) {
		MaterialBox materialBox = MaterialBox.dao.findById(id);
		if (materialBox == null) {
			throw new OperationException("删除失败，料盒号为"+ id + "的料盒不存在！");
		}
		Material material = Material.dao.findFirst(MaterialSQL.GET_MATERIAL_BY_BOX_SQL, id);
		if (material != null) {
			throw new OperationException("删除失败，料盒号为"+ id + "的料盒存在物料！");
		}
		materialBox.setEnabled(false);
		materialBox.update();
	}
	
}
