package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.FormerSupplier;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.vo.SupplierVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;


/**
 * 供应商业务层
 * @author HardyYao
 * @createTime 2018年11月22日  下午4:13:04
 */

public class SupplierService extends SelectService {

	private static SelectService selectService = Aop.get(SelectService.class);

	private static final String GET_ENABLED_SUPPLIER_BY_NAME_SQL = "SELECT * FROM supplier WHERE name = ? AND enabled = 1";

	private static final String GET_MATERIAL_BY_SUPPLIER = "SELECT * FROM material WHERE material.remainder_quantity > 0 AND material.box IN (SELECT id FROM material_box WHERE material_box.supplier = ? AND enabled = 1)";

	private static final String SET_BOX_SUPPLIER_NULL_BY_SUPPLIER = "UPDATE material_box SET material_box.supplier = null WHERE material_box.supplier = ?";

	private static final String GET_FORMER_SUPPLIER_SQL = "SELECT * FROM former_supplier WHERE former_name = ?";

	public static final String GET_SUPPLIER_BY_MATERIAL_TYPE_ID = "SELECT * FROM material_type JOIN supplier ON material_type.supplier = supplier.id WHERE material_type.id = ?";


	// 添加供应商
	public String add(String name) {
		String resultString = "添加成功！";
		if (Supplier.dao.findFirst(GET_ENABLED_SUPPLIER_BY_NAME_SQL, name.trim()) != null || FormerSupplier.dao.findFirst(GET_FORMER_SUPPLIER_SQL, name.trim()) != null) {
			resultString = "该供应商已存在或为某供应商的曾用名，请勿重复添加！";
			return resultString;
		} else {
			Supplier supplier = new Supplier();
			supplier.setName(name.trim());
			supplier.setEnabled(true);
			supplier.save();
			return resultString;
		}
	}


	// 更新供应商的启用/禁用状态
	public String update(Integer id, Boolean enabled) {
		String resultString = "更新成功！";
		Supplier supplier = Supplier.dao.findById(id);
		// if(!supplier.getName().equals(name)) {
		// if (Supplier.dao.find(GET_ENABLED_SUPPLIER_BY_NAME_SQL, name).size() != 0) {
		// resultString = "该供应商已存在，请勿重复添加！";
		// return resultString;
		// }
		// }
		if (!enabled) {
			MaterialType mt = MaterialType.dao.findFirst(GET_MATERIAL_BY_SUPPLIER, id);
			if (mt != null) {
				resultString = "该供应商名下存在物料尚未出库，无法删除！";
				return resultString;
			}
			Db.update(SET_BOX_SUPPLIER_NULL_BY_SUPPLIER, supplier.getId());

		}
		supplier.setEnabled(enabled);
		supplier.update();
		return resultString;
	}


	// 查询所有供应商
	public Object getSuppliers(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null) {
			filter = filter.concat("#&#enabled=1");
		} else {
			filter = "enabled=1";
		}
		Page<Record> result = selectService.select("supplier", pageNo, pageSize, ascBy, descBy, filter);
		List<SupplierVO> supplierVOs = new ArrayList<SupplierVO>();
		for (Record res : result.getList()) {
			SupplierVO s = new SupplierVO(res.get("id"), res.get("name"), res.get("enabled"));
			supplierVOs.add(s);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(supplierVOs);
		return pagePaginate;
	}


	// 更新供应商的曾用名
	public String changeName(Integer id, String name) {
		String resultString = "更新成功！";
		Supplier supplier = Supplier.dao.findById(id);
		if (!supplier.getName().equals(name.trim())) {
			if (Supplier.dao.findFirst(GET_ENABLED_SUPPLIER_BY_NAME_SQL, name) != null || FormerSupplier.dao.findFirst(GET_FORMER_SUPPLIER_SQL, name.trim()) != null) {
				resultString = "该供应商已存在或者为某个供应商的曾用名，请勿重复添加！";
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
}
