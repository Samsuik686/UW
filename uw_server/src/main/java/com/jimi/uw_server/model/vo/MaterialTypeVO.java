package com.jimi.uw_server.model.vo;

import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.Supplier;

/**
 * 物料类型表示层对象
 * @author HardyYao
 * @createTime 2018年7月5日 上午11:25:16 
 */
@SuppressWarnings("serial")
public class MaterialTypeVO extends MaterialType{

	private static final String COUNT_MATERIAL_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ?";

	private String enabledString;

	private Integer quantity;

	private String supplierName;


	public MaterialTypeVO(Integer id, String no, String specification, Integer supplier, Boolean enabled, Integer thickness, Integer radius) {
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
		this.set("enabledString", getEnabledString());
		this.setQuantity(id);
		this.set("quantity", getQuantity());
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

	public void setQuantity(Integer id) {
		Material material = Material.dao.findFirst(COUNT_MATERIAL_SQL, id);
		if (material.get("quantity") == null) {
			this.quantity = 0;
		} else {
			this.quantity = Integer.parseInt(material.get("quantity").toString());
		}
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
 