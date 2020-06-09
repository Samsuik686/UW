package com.jimi.uw_server.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({ "serial", "unchecked" })
public abstract class BaseFormerSupplier<M extends BaseFormerSupplier<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Integer id) {
		set("id", id);
		return (M) this;
	}


	public java.lang.Integer getId() {
		return getInt("id");
	}


	public M setFormerName(java.lang.String formerName) {
		set("former_name", formerName);
		return (M) this;
	}


	public java.lang.String getFormerName() {
		return getStr("former_name");
	}


	public M setSupplierId(java.lang.Integer supplierId) {
		set("supplier_id", supplierId);
		return (M) this;
	}


	public java.lang.Integer getSupplierId() {
		return getInt("supplier_id");
	}

}
