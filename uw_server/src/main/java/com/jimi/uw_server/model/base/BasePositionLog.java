package com.jimi.uw_server.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BasePositionLog<M extends BasePositionLog<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Integer id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public M setTaskId(java.lang.Integer taskId) {
		set("task_id", taskId);
		return (M)this;
	}
	
	public java.lang.Integer getTaskId() {
		return getInt("task_id");
	}

	public M setMaterialId(java.lang.String materialId) {
		set("material_id", materialId);
		return (M)this;
	}
	
	public java.lang.String getMaterialId() {
		return getStr("material_id");
	}

	public M setOldArea(java.lang.Integer oldArea) {
		set("old_area", oldArea);
		return (M)this;
	}
	
	public java.lang.Integer getOldArea() {
		return getInt("old_area");
	}

	public M setOldCol(java.lang.Integer oldCol) {
		set("old_col", oldCol);
		return (M)this;
	}
	
	public java.lang.Integer getOldCol() {
		return getInt("old_col");
	}

	public M setOldRow(java.lang.Integer oldRow) {
		set("old_row", oldRow);
		return (M)this;
	}
	
	public java.lang.Integer getOldRow() {
		return getInt("old_row");
	}

	public M setOldHeight(java.lang.Integer oldHeight) {
		set("old_height", oldHeight);
		return (M)this;
	}
	
	public java.lang.Integer getOldHeight() {
		return getInt("old_height");
	}

	public M setNewArea(java.lang.Integer newArea) {
		set("new_area", newArea);
		return (M)this;
	}
	
	public java.lang.Integer getNewArea() {
		return getInt("new_area");
	}

	public M setNewCol(java.lang.Integer newCol) {
		set("new_col", newCol);
		return (M)this;
	}
	
	public java.lang.Integer getNewCol() {
		return getInt("new_col");
	}

	public M setNewRow(java.lang.Integer newRow) {
		set("new_row", newRow);
		return (M)this;
	}
	
	public java.lang.Integer getNewRow() {
		return getInt("new_row");
	}

	public M setNewHeight(java.lang.Integer newHeight) {
		set("new_height", newHeight);
		return (M)this;
	}
	
	public java.lang.Integer getNewHeight() {
		return getInt("new_height");
	}

	public M setTime(java.util.Date time) {
		set("time", time);
		return (M)this;
	}
	
	public java.util.Date getTime() {
		return get("time");
	}

}
