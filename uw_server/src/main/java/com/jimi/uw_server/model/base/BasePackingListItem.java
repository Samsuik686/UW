package com.jimi.uw_server.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BasePackingListItem<M extends BasePackingListItem<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Integer id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public M setMaterialTypeId(java.lang.Integer materialTypeId) {
		set("material_type_id", materialTypeId);
		return (M)this;
	}
	
	public java.lang.Integer getMaterialTypeId() {
		return getInt("material_type_id");
	}

	public M setQuantity(java.lang.Integer quantity) {
		set("quantity", quantity);
		return (M)this;
	}
	
	public java.lang.Integer getQuantity() {
		return getInt("quantity");
	}

	public M setTaskId(java.lang.Integer taskId) {
		set("task_id", taskId);
		return (M)this;
	}
	
	public java.lang.Integer getTaskId() {
		return getInt("task_id");
	}

	public M setFinishTime(java.util.Date finishTime) {
		set("finish_time", finishTime);
		return (M)this;
	}
	
	public java.util.Date getFinishTime() {
		return get("finish_time");
	}

}
