package com.jimi.uw_server.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;


/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseMaterialType<M extends BaseMaterialType<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Integer id) {
		set("id", id);
		return (M) this;
	}


	public java.lang.Integer getId() {
		return getInt("id");
	}


	public M setNo(java.lang.String no) {
		set("no", no);
		return (M) this;
	}


	public java.lang.String getNo() {
		return getStr("no");
	}


	public M setSpecification(java.lang.String specification) {
		set("specification", specification);
		return (M) this;
	}


	public java.lang.String getSpecification() {
		return getStr("specification");
	}


	public M setEnabled(java.lang.Boolean enabled) {
		set("enabled", enabled);
		return (M) this;
	}


	public java.lang.Boolean getEnabled() {
		return get("enabled");
	}


	public M setSupplier(java.lang.Integer supplier) {
		set("supplier", supplier);
		return (M) this;
	}


	public java.lang.Integer getSupplier() {
		return getInt("supplier");
	}


	public M setThickness(java.lang.Integer thickness) {
		set("thickness", thickness);
		return (M) this;
	}


	public java.lang.Integer getThickness() {
		return getInt("thickness");
	}


	public M setRadius(java.lang.Integer radius) {
		set("radius", radius);
		return (M) this;
	}


	public java.lang.Integer getRadius() {
		return getInt("radius");
	}

}
