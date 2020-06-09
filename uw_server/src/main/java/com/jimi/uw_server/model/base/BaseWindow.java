package com.jimi.uw_server.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({ "serial", "unchecked" })
public abstract class BaseWindow<M extends BaseWindow<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Integer id) {
		set("id", id);
		return (M) this;
	}


	public java.lang.Integer getId() {
		return getInt("id");
	}


	public M setArea(java.lang.Integer area) {
		set("area", area);
		return (M) this;
	}


	public java.lang.Integer getArea() {
		return getInt("area");
	}


	public M setBindTaskId(java.lang.Integer bindTaskId) {
		set("bind_task_id", bindTaskId);
		return (M) this;
	}


	public java.lang.Integer getBindTaskId() {
		return getInt("bind_task_id");
	}


	public M setSize(java.lang.Integer size) {
		set("size", size);
		return (M) this;
	}


	public java.lang.Integer getSize() {
		return getInt("size");
	}


	public M setAuto(java.lang.Boolean auto) {
		set("auto", auto);
		return (M) this;
	}


	public java.lang.Boolean getAuto() {
		return get("auto");
	}

}
