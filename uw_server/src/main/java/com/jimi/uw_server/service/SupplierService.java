package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
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

	private static SelectService selectService = Enhancer.enhance(SelectService.class);

	private static final String GET_ENABLED_SUPPLIER_BY_NAME_SQL = "SELECT * FROM supplier WHERE name = ? AND enabled = 1";

	private static final String GET_ENABLED_MATERIAL_TYPE_BY_SUPPLIER_ID_SQL = "SELECT * FROM material_type WHERE supplier = ? AND enabled = 1";


	public String add(String name) {
		String resultString = "添加成功！";
		if (Supplier.dao.find(GET_ENABLED_SUPPLIER_BY_NAME_SQL, name).size() != 0) {
			resultString = "该供应商已存在，请勿重复添加！";
			return resultString;
		} else {
			Supplier supplier = new Supplier();
			supplier.setName(name);
			supplier.setEnabled(true);
			supplier.save();
			return resultString;
		}
	}

	public String update(Integer id, String name, Boolean enabled) {
		String resultString = "更新成功！";
		Supplier supplier = Supplier.dao.findById(id);
		if(!supplier.getName().equals(name)) {
			if (Supplier.dao.find(GET_ENABLED_SUPPLIER_BY_NAME_SQL, name).size() != 0) {
				resultString = "该供应商已存在，请勿重复添加！";
				return resultString;
			}
		}
		if (!enabled) {
			MaterialType mt = MaterialType.dao.findFirst(GET_ENABLED_MATERIAL_TYPE_BY_SUPPLIER_ID_SQL, id);
			if (mt != null) {
				resultString = "该供应商已绑定了某个物料类型，请先删掉相关的物料类型再进行删除操作！";
				return resultString;
			}
		}
		supplier.setName(name);
		supplier.setEnabled(enabled);
		supplier.update();
		return resultString;
	}

	public Object getSuppliers(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null ) {
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

}
