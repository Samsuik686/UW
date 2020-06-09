/**  
*  
*/
package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.model.Company;
import com.jimi.uw_server.model.Supplier;

/**
 * <p>
 * Title: Material
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2019
 * </p>
 * <p>
 * Company: 惠州市几米物联技术有限公司
 * </p>
 * 
 * @author trjie
 * @date 2020年1月13日
 *
 */
public class MaterialVO {

	private String materialId;

	private Integer row;

	private Integer col;

	private String no;

	private Integer materialTypeId;

	private Integer boxId;

	private Integer companyId;

	private String companyName;

	private String manufacturer;

	private Integer quantity;

	private Date productionTime;

	private Date storeTime;

	private String designator;

	private Integer supplierId;

	private String SupplierName;

	private String cycle;


	/**
	 * <p>
	 * Title
	 * <p>
	 * <p>
	 * Description
	 * <p>
	 */
	public MaterialVO() {
		// TODO Auto-generated constructor stub
	}


	public String getMaterialId() {
		return materialId;
	}


	public void setMaterialId(String materialId) {
		this.materialId = materialId;
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


	public String getNo() {
		return no;
	}


	public void setNo(String no) {
		this.no = no;
	}


	public Integer getMaterialTypeId() {
		return materialTypeId;
	}


	public void setMaterialTypeId(Integer materialTypeId) {
		this.materialTypeId = materialTypeId;
	}


	public Integer getCompanyId() {
		return companyId;
	}


	public void setCompanyId(Integer companyId) {
		this.companyId = companyId;
	}


	public String getCompanyName() {
		return companyName;
	}


	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}


	public Integer getQuantity() {
		return quantity;
	}


	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}


	public Date getProductionTime() {
		return productionTime;
	}


	public void setProductionTime(Date productionTime) {
		this.productionTime = productionTime;
	}


	public Integer getBoxId() {
		return boxId;
	}


	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}


	public Date getStoreTime() {
		return storeTime;
	}


	public void setStoreTime(Date storeTime) {
		this.storeTime = storeTime;
	}


	public String getManufacturer() {
		return manufacturer;
	}


	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}


	public String getDesignator() {
		return designator;
	}


	public void setDesignator(String designator) {
		this.designator = designator;
	}


	public Integer getSupplierId() {
		return supplierId;
	}


	public void setSupplierId(Integer supplierId) {
		this.supplierId = supplierId;
	}


	public String getSupplierName() {
		return SupplierName;
	}


	public void setSupplierName(String supplierName) {
		SupplierName = supplierName;
	}


	public String getCycle() {
		return cycle;
	}


	public void setCycle(String cycle) {
		this.cycle = cycle;
	}


	public static List<MaterialVO> fillRegualrMaterialVOList(List<Record> records, Company company, Supplier supplier) {
		List<MaterialVO> materialVOs = new ArrayList<MaterialVO>(records.size());
		for (Record record : records) {
			MaterialVO materialVO = new MaterialVO();
			materialVO.setMaterialId(record.getStr("Material_Id"));
			materialVO.setMaterialTypeId(record.getInt("Material_MaterialTypeId"));
			materialVO.setNo(record.getStr("MaterialType_No"));
			materialVO.setBoxId(record.getInt("Material_Box"));
			materialVO.setQuantity(record.getInt("Material_RemainderQuantity"));
			materialVO.setProductionTime(record.getDate("Material_ProductionTime"));
			materialVO.setStoreTime(record.getDate("Material_StoreTime"));
			materialVO.setManufacturer(record.getStr("Material_Manufacturer"));
			materialVO.setCompanyId(company.getId());
			materialVO.setCompanyName(company.getNickname());
			materialVO.setSupplierId(supplier.getId());
			materialVO.setSupplierName(supplier.getName());
			materialVO.setCycle(record.getStr("Material_Cycle"));
			if (record.getInt("Material_Col") != -1 && record.getInt("Material_Row") != -1) {
				materialVO.setRow(record.getInt("Material_Row") + 1);
				materialVO.setCol(record.getInt("Material_Col") + 1);
			} else {
				materialVO.setRow(record.getInt("Material_Row"));
				materialVO.setCol(record.getInt("Material_Col"));
			}
			materialVOs.add(materialVO);
		}
		return materialVOs;
	}


	public static List<MaterialVO> fillPreciousMaterialVOList(List<Record> records, Company company, Supplier supplier) {
		List<MaterialVO> materialVOs = new ArrayList<MaterialVO>(records.size());
		for (Record record : records) {
			MaterialVO materialVO = new MaterialVO();
			materialVO.setMaterialId(record.getStr("Material_Id"));
			materialVO.setMaterialTypeId(record.getInt("Material_MaterialTypeId"));
			materialVO.setNo(record.getStr("MaterialType_No"));
			materialVO.setQuantity(record.getInt("Material_RemainderQuantity"));
			materialVO.setProductionTime(record.getDate("Material_ProductionTime"));
			materialVO.setStoreTime(record.getDate("Material_StoreTime"));
			materialVO.setCompanyId(record.getInt("Company_Id"));
			materialVO.setCompanyName(record.getStr("Company_Nickname"));
			materialVO.setManufacturer(record.getStr("Material_Manufacturer"));
			materialVO.setCycle(record.getStr("Material_Cycle"));
			materialVO.setCompanyId(company.getId());
			materialVO.setCompanyName(company.getNickname());
			materialVO.setSupplierId(supplier.getId());
			materialVO.setSupplierName(supplier.getName());
			materialVOs.add(materialVO);
		}
		return materialVOs;
	}

}
