package com.jimi.uw_server.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;


/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseMaterialReturnRecord<M extends BaseMaterialReturnRecord<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Integer id) {
		set("id", id);
		return (M) this;
	}


	public java.lang.Integer getId() {
		return getInt("id");
	}


	public M setTaskId(java.lang.Integer taskId) {
		set("task_id", taskId);
		return (M) this;
	}


	public java.lang.Integer getTaskId() {
		return getInt("task_id");
	}


	public M setWhId(java.lang.Integer whId) {
		set("wh_id", whId);
		return (M) this;
	}


	public java.lang.Integer getWhId() {
		return getInt("wh_id");
	}


	public M setMaterialTypeId(java.lang.Integer materialTypeId) {
		set("material_type_id", materialTypeId);
		return (M) this;
	}


	public java.lang.Integer getMaterialTypeId() {
		return getInt("material_type_id");
	}


	public M setQuantity(java.lang.Integer quantity) {
		set("quantity", quantity);
		return (M) this;
	}


	public java.lang.Integer getQuantity() {
		return getInt("quantity");
	}


	public M setTime(java.util.Date time) {
		set("time", time);
		return (M) this;
	}


	public java.util.Date getTime() {
		return get("time");
	}


	public M setEnabled(java.lang.Boolean enabled) {
		set("enabled", enabled);
		return (M) this;
	}


	public java.lang.Boolean getEnabled() {
		return get("enabled");
	}

}
