package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.service.MaterialService;


/**
 * 物料类型表示层对象
 * @author HardyYao
 * @createTime 2018年7月5日 上午11:25:16 
 */
public class MaterialInfoVO{

	private static MaterialService materialService = Aop.get(MaterialService.class);

	private Integer id;
	
	private String no;
	
	private String specification;
	
	private Integer supplier;
	
	private String supplierName;

	private Integer thickness;
	
	private Integer radius;
	
	private String designator;
	
	private Boolean enabled;
	
	private Integer type;
	
	private String enabledString;

	private Integer quantity;


	/**
	 * <p>Title<p>
	 * <p>Description<p>
	 */
	public MaterialInfoVO() {
		// TODO Auto-generated constructor stub
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
		if (type.equals(WarehouseType.REGULAR.getId())) {
			remainderQuantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(id);
		} else if (type.equals(WarehouseType.PRECIOUS.getId())) {
			remainderQuantity = materialService.countPreciousQuantityByMaterialTypeId(id);
		}

		this.quantity = remainderQuantity;
	}


	public String getSupplierName() {
		return supplierName;
	}


	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}


	public void setEnabledString(String enabledString) {
		this.enabledString = enabledString;
	}

	
	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public String getNo() {
		return no;
	}


	public void setNo(String no) {
		this.no = no;
	}


	public String getSpecification() {
		return specification;
	}


	public void setSpecification(String specification) {
		this.specification = specification;
	}


	public Integer getSupplier() {
		return supplier;
	}


	public void setSupplier(Integer supplier) {
		this.supplier = supplier;
	}


	public Integer getThickness() {
		return thickness;
	}


	public void setThickness(Integer thickness) {
		this.thickness = thickness;
	}


	public Integer getRadius() {
		return radius;
	}


	public void setRadius(Integer radius) {
		this.radius = radius;
	}


	public String getDesignator() {
		return designator;
	}


	public void setDesignator(String designator) {
		this.designator = designator;
	}


	public Boolean getEnabled() {
		return enabled;
	}


	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}


	public Integer getType() {
		return type;
	}


	public void setType(Integer type) {
		this.type = type;
	}
	
	
	public static List<MaterialInfoVO> fillList(List<Record> records){
		List<MaterialInfoVO> materialInfoVOs = new ArrayList<MaterialInfoVO>(records.size());
		for (Record record : records) {
			MaterialInfoVO m = new MaterialInfoVO();
			m.setId(record.getInt("MaterialType_Id"));
			m.setNo(record.getStr("MaterialType_No"));
			m.setSpecification(record.getStr("MaterialType_Specification"));
			m.setSupplier(record.getInt("MaterialType_Supplier"));
			m.setSupplierName(record.getStr("Supplier_Name"));
			m.setThickness(record.getInt("MaterialType_Thickness"));
			m.setRadius(record.getInt("MaterialType_Radius"));
			m.setEnabled(record.getBoolean("MaterialType_Enabled"));
			m.setEnabledString(record.getBoolean("MaterialType_Enabled"));
			m.setType(record.getInt("MaterialType_Type"));
			m.setQuantity(record.getInt("MaterialType_Id"), record.getInt("MaterialType_Type"));
			m.setDesignator(record.getStr("MaterialType_Designator"));
			materialInfoVOs.add(m);
		}
		return materialInfoVOs;
	}

}
