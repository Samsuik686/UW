package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;

/**
 * 料盒表示层对象
 * @author HardyYao
 * @createTime 2018年9月7日  上午10:56:49
 */

public class MaterialBoxVO {

	private Integer id;

	private String area;

	private Integer row;

	private Integer col;

	private Integer height;

	private String isOnShelfString;

	private String supplierName;

	private String typeName;
	
	private String companyName;


	public MaterialBoxVO() {
	}


	public String getIsOnShelfString() {
		return isOnShelfString;
	}


	public void setIsOnShelfString(Boolean isOnShelf) {
		if (isOnShelf) {
			this.isOnShelfString = "是";
		} else {
			this.isOnShelfString = "否";
		}
	}


	public String getSupplierName() {
		return supplierName;
	}


	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}


	public String getTypeName() {
		return typeName;
	}


	public void setTypeName(Integer type) {
		if (type.equals(1)) {
			this.typeName = "标准";
		} else {
			this.typeName = "非标准";
		}

	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public String getArea() {
		return area;
	}


	public void setArea(String area) {
		this.area = area;
	}


	public Integer getRow() {
		return row;
	}


	public void setRow(Integer row) {
		this.row = row;
	}


	public Integer getCol() {
		return col;
	}


	public void setCol(Integer col) {
		this.col = col;
	}


	public Integer getHeight() {
		return height;
	}


	public void setHeight(Integer height) {
		this.height = height;
	}


	public String getCompanyName() {
		return companyName;
	}


	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}
	
	
	public static List<MaterialBoxVO> fillList(List<Record> records){
		List<MaterialBoxVO> materialBoxVOs = new ArrayList<MaterialBoxVO>(records.size());
		for (Record record : records) {
			MaterialBoxVO materialBoxVO = new MaterialBoxVO();
			materialBoxVO.setId(record.getInt("MaterialBox_Id"));
			materialBoxVO.setArea(record.getStr("MaterialBox_Area"));
			materialBoxVO.setCol(record.getInt("MaterialBox_Col"));
			materialBoxVO.setRow(record.getInt("MaterialBox_Row"));
			materialBoxVO.setHeight(record.getInt("MaterialBox_Height"));
			materialBoxVO.setSupplierName(record.getStr("Supplier_Name"));
			materialBoxVO.setCompanyName(record.getStr("Company_Nickname"));
			materialBoxVO.setTypeName(record.getInt("MaterialBox_Type"));
			materialBoxVO.setIsOnShelfString(record.getBoolean("MaterialBox_IsOnShelf"));
			materialBoxVOs.add(materialBoxVO);
		}
		return materialBoxVOs;
	}

}