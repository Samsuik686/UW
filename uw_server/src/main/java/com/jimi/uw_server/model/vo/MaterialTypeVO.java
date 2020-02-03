package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;


/**
 * 物料类型表示层对象
 * @author HardyYao
 * @createTime 2018年7月5日 上午11:25:16 
 */
public class MaterialTypeVO{

	private Integer id;
	
	private String no;
	
	private String specification;
	
	private Integer supplier;
	
	private String supplierName;

	private Integer thickness;
	
	private Integer radius;
	
	private Integer type;

	private String designator;
	
	/**
	 * <p>Title<p>
	 * <p>Description<p>
	 */
	public MaterialTypeVO() {
	}
	

	public String getSupplierName() {
		return supplierName;
	}


	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
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

	public Integer getType() {
		return type;
	}


	public void setType(Integer type) {
		this.type = type;
	}
	
	
	public String getDesignator() {
		return designator;
	}


	public void setDesignator(String designator) {
		this.designator = designator;
	}


	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月10日
	 */
	public static List<MaterialTypeVO> fillList(List<Record> records) {
		List<MaterialTypeVO> materialTypeVOs = new ArrayList<MaterialTypeVO>();
		if (!records.isEmpty()) {
			for (Record record : records) {
				MaterialTypeVO materialTypeVO = new MaterialTypeVO();
				materialTypeVO.setId(record.getInt("id"));
				materialTypeVO.setNo(record.getStr("no"));
				materialTypeVO.setSpecification(record.getStr("specification"));
				materialTypeVO.setRadius(record.getInt("radius"));
				materialTypeVO.setThickness(record.getInt("thickness"));
				materialTypeVO.setType(record.getInt("type"));
				materialTypeVO.setSupplier(record.getInt("supplier"));
				materialTypeVO.setSupplierName(record.getStr("supplierName"));
				materialTypeVO.setDesignator(record.getStr("designator"));
				materialTypeVOs.add(materialTypeVO);
			}
		}
		return materialTypeVOs;
	}
	
}
