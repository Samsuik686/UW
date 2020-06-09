package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;


/**
 * 客户实体类
 * 
 * @author HardyYao
 * @createTime 2018年11月22日 下午4:39:50
 */

public class SupplierVO {

	private Integer id;

	private String name;

	private String companyName;

	private Boolean enabled;

	private String enabledString;


	public SupplierVO() {

	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getCompanyName() {
		return companyName;
	}


	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}


	public Boolean getEnabled() {
		return enabled;
	}


	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
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


	public static List<SupplierVO> fillList(List<Record> records) {
		List<SupplierVO> supplierVOs = new ArrayList<SupplierVO>(records.size());
		for (Record record : records) {
			SupplierVO supplierVO = new SupplierVO();
			supplierVO.setId(record.getInt("Supplier_Id"));
			supplierVO.setName(record.getStr("Supplier_Name"));
			supplierVO.setCompanyName(record.getStr("Company_Nickname"));
			supplierVO.setEnabled(record.getBoolean("Supplier_Enabled"));
			supplierVO.setEnabledString(record.getBoolean("Supplier_Enabled"));
			supplierVOs.add(supplierVO);
		}
		return supplierVOs;
	}

}
