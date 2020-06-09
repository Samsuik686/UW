package com.jimi.uw_server.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({ "serial", "unchecked" })
public abstract class BaseMaterial<M extends BaseMaterial<M>> extends Model<M> implements IBean {

	public M setId(java.lang.String id) {
		set("id", id);
		return (M) this;
	}


	public java.lang.String getId() {
		return getStr("id");
	}


	public M setType(java.lang.Integer type) {
		set("type", type);
		return (M) this;
	}


	public java.lang.Integer getType() {
		return getInt("type");
	}


	public M setBox(java.lang.Integer box) {
		set("box", box);
		return (M) this;
	}


	public java.lang.Integer getBox() {
		return getInt("box");
	}


	public M setRow(java.lang.Integer row) {
		set("row", row);
		return (M) this;
	}


	public java.lang.Integer getRow() {
		return getInt("row");
	}


	public M setCol(java.lang.Integer col) {
		set("col", col);
		return (M) this;
	}


	public java.lang.Integer getCol() {
		return getInt("col");
	}


	public M setRemainderQuantity(java.lang.Integer remainderQuantity) {
		set("remainder_quantity", remainderQuantity);
		return (M) this;
	}


	public java.lang.Integer getRemainderQuantity() {
		return getInt("remainder_quantity");
	}


	public M setProductionTime(java.util.Date productionTime) {
		set("production_time", productionTime);
		return (M) this;
	}


	public java.util.Date getProductionTime() {
		return get("production_time");
	}


	public M setStoreTime(java.util.Date storeTime) {
		set("store_time", storeTime);
		return (M) this;
	}


	public java.util.Date getStoreTime() {
		return get("store_time");
	}


	public M setIsInBox(java.lang.Boolean isInBox) {
		set("is_in_box", isInBox);
		return (M) this;
	}


	public java.lang.Boolean getIsInBox() {
		return get("is_in_box");
	}


	public M setIsRepeated(java.lang.Boolean isRepeated) {
		set("is_repeated", isRepeated);
		return (M) this;
	}


	public java.lang.Boolean getIsRepeated() {
		return get("is_repeated");
	}


	public M setManufacturer(java.lang.String manufacturer) {
		set("manufacturer", manufacturer);
		return (M) this;
	}


	public java.lang.String getManufacturer() {
		return getStr("manufacturer");
	}


	public M setCycle(java.lang.String cycle) {
		set("cycle", cycle);
		return (M) this;
	}


	public java.lang.String getCycle() {
		return getStr("cycle");
	}


	public M setStatus(java.lang.Integer status) {
		set("status", status);
		return (M) this;
	}


	public java.lang.Integer getStatus() {
		return getInt("status");
	}


	public M setPrintTime(java.util.Date printTime) {
		set("print_time", printTime);
		return (M) this;
	}


	public java.util.Date getPrintTime() {
		return get("print_time");
	}


	public M setCutTaskLogId(java.lang.Integer cutTaskLogId) {
		set("cut_task_log_id", cutTaskLogId);
		return (M) this;
	}


	public java.lang.Integer getCutTaskLogId() {
		return getInt("cut_task_log_id");
	}


	public M setCompanyId(java.lang.Integer companyId) {
		set("company_id", companyId);
		return (M) this;
	}


	public java.lang.Integer getCompanyId() {
		return getInt("company_id");
	}

}
