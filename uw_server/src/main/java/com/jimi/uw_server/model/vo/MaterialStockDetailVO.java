/**  
*  
*/
package com.jimi.uw_server.model.vo;

import java.util.List;

/**
 * <p>
 * Title: MaterialStockDetailVO
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
 * @date 2020年4月13日
 *
 */
public class MaterialStockDetailVO {
	private int id;

	private String no;

	private String specification;

	private String supplierName;

	private String warehouse;

	private int oldBalance;

	private int currentBalance;

	private int numberInStock;

	private int numberOutStock;

	private List<WarehouseStockVO> list;


	public void setId(int id) {
		this.id = id;
	}


	public int getId() {
		return this.id;
	}


	public void setNo(String no) {
		this.no = no;
	}


	public String getNo() {
		return this.no;
	}


	public void setSpecification(String specification) {
		this.specification = specification;
	}


	public String getSpecification() {
		return this.specification;
	}


	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}


	public String getSupplierName() {
		return this.supplierName;
	}


	public void setWarehouse(String warehouse) {
		this.warehouse = warehouse;
	}


	public String getWarehouse() {
		return this.warehouse;
	}


	public void setOldBalance(int oldBalance) {
		this.oldBalance = oldBalance;
	}


	public int getOldBalance() {
		return this.oldBalance;
	}


	public void setCurrentBalance(int currentBalance) {
		this.currentBalance = currentBalance;
	}


	public int getCurrentBalance() {
		return this.currentBalance;
	}


	public void setNumberInStock(int numberInStock) {
		this.numberInStock = numberInStock;
	}


	public int getNumberInStock() {
		return this.numberInStock;
	}


	public void setNumberOutStock(int numberOutStock) {
		this.numberOutStock = numberOutStock;
	}


	public int getNumberOutStock() {
		return this.numberOutStock;
	}


	public void setList(List<WarehouseStockVO> list) {
		this.list = list;
	}


	public List<WarehouseStockVO> getList() {
		return this.list;
	}
}
