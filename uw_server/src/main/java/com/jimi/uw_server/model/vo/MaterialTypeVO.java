package com.jimi.uw_server.model.vo;

import com.jfinal.aop.Aop;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.service.MaterialService;


/**
 * 物料类型表示层对象
 * @author HardyYao
 * @createTime 2018年7月5日 上午11:25:16 
 */
@SuppressWarnings("serial")
public class MaterialTypeVO extends MaterialType {

	private static MaterialService materialService = Aop.get(MaterialService.class);

	private String enabledString;

	private Integer quantity;

	private String supplierName;


	public MaterialTypeVO(Integer id, String no, String specification, Integer supplier, Integer thickness, Integer radius, Boolean enabled, String designator, Integer type) {
		this.setId(id);
		this.setNo(no);
		this.setSpecification(specification);
		this.setSupplier(supplier);
		this.setSupplierName(supplier);
		this.set("supplierName", getSupplierName());
		this.setThickness(thickness);
		this.setRadius(radius);
		this.setEnabled(enabled);
		this.setEnabledString(enabled);
		this.setType(type);
		this.set("enabledString", getEnabledString());
		this.setQuantity(id, type);
		this.set("quantity", getQuantity());
		this.set("designator", designator);
	}


	public String getEnabledString() {
		return enabledString;
	}


	public void setEnabledString(Boolean enabled) {
		if (enabled) {
			this.enabledString = "是";
		} else {
			this.enabledString = "否";
		}
	}


	public Integer getQuantity() {
		return quantity;
	}


	public void setQuantity(Integer id, Integer type) {
		Integer remainderQuantity = 0;
		if (type.equals(WarehouseType.REGULAR)) {
			remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(id);
		} else if (type.equals(WarehouseType.PRECIOUS)) {
			remainderQuantity = materialService.countPreciousQuantityByMaterialTypeId(id);
		}

		this.quantity = remainderQuantity;
	}


	public String getSupplierName() {
		return supplierName;
	}


	public void setSupplierName(Integer supplier) {
		Supplier s = Supplier.dao.findById(supplier);
		String name = s.getName();
		this.supplierName = name;
	}

}
