package com.jimi.uw_server.service;

import java.util.Collections;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.constant.sql.MaterialBoxSQL;
import com.jimi.uw_server.constant.sql.MaterialSQL;
import com.jimi.uw_server.constant.sql.MaterialTypeSQL;
import com.jimi.uw_server.constant.sql.SupplierSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.FormerSupplier;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.vo.SupplierVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;


/**
 * 客户业务层
 * @author HardyYao
 * @createTime 2018年11月22日  下午4:13:04
 */

public class SupplierService extends SelectService {

	private static SelectService selectService = Aop.get(SelectService.class);

	// 添加客户
	public String add(String name, Integer companyId) {
		String resultString = "添加成功！";
		if (Supplier.dao.findFirst(SupplierSQL.GET_SUPPLIER_BY_NAME_SQL, name.trim()) != null || FormerSupplier.dao.findFirst(SupplierSQL.GET_FORMER_SUPPLIER_SQL, name.trim()) != null) {
			resultString = "该客户已存在或为某客户的曾用名，请勿重复添加！";
			return resultString;
		} else {
			Supplier supplier = new Supplier();
			supplier.setName(name.trim());
			supplier.setEnabled(true);
			supplier.save();
			return resultString;
		}
	}


	// 更新客户的启用/禁用状态
	public void delete(Integer id) {
		Supplier supplier = Supplier.dao.findById(id);
		if (supplier == null) {
			throw new OperationException("客户不存在！");
		}
		MaterialType mt = MaterialType.dao.findFirst(MaterialSQL.GET_MATERIAL_BY_SUPPLIER_SQL, id);
		if (mt != null) {
			throw new OperationException("该客户名下存在物料尚未出库，无法删除！");
		}
		Db.update(MaterialBoxSQL.SET_MATERIAL_BOX_SUPPLIER_COMPANY_NULL_BY_SUPPLIER_SQL, supplier.getId());
		Db.update(MaterialTypeSQL.SET_MATERIAL_TYPE_UNABLED_BY_SUPPLIER_SQL, supplier.getId());
		
		supplier.setEnabled(false);
		supplier.update();
	}


	// 查询所有客户
	public PagePaginate getSuppliers(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null) {
			filter = filter.concat("#&#supplier.enabled=1#&#company.enabled=1");
		} else {
			filter = "#&#supplier.enabled=1#&#company.enabled=1";
		}
		Page<Record> result = selectService.select(new String[] {"supplier", "company"}, new String[] {"supplier.company_id=company.id"}, pageNo, pageSize, ascBy, descBy, filter);
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setTotalRow(result.getTotalRow());
		if (result.getList() != null && !result.getList().isEmpty()) {
			pagePaginate.setList(SupplierVO.fillList(result.getList()));
		}else {
			pagePaginate.setList(Collections.emptyList());
		}
		return pagePaginate;
	}


	// 更新客户的曾用名
	public String changeName(Integer id, String name) {
		String resultString = "更新成功！";
		Supplier supplier = Supplier.dao.findById(id);
		name = name.trim();
		if (!supplier.getName().equals(name.trim())) {
			if (Supplier.dao.findFirst(SupplierSQL.GET_SUPPLIER_BY_NAME_SQL, name) != null || FormerSupplier.dao.findFirst(SupplierSQL.GET_FORMER_SUPPLIER_SQL, name) != null) {
				resultString = "该客户已存在或者为某个客户的曾用名，请勿重复添加！";
				throw new OperationException(resultString);
			}
		}

		String formerName = supplier.getName();
		FormerSupplier formerSupplier = new FormerSupplier();
		formerSupplier.setFormerName(formerName);
		formerSupplier.setSupplierId(supplier.getId());
		formerSupplier.save();
		supplier.setName(name);
		supplier.update();
		return resultString;
	}
	
	
	public Supplier getSupplierById(Integer supplierId) {
		Supplier supplier = Supplier.dao.findById(supplierId);
		if (supplier == null) {
			throw new OperationException("客户不存在！");
		}
		return supplier;
	}
}
