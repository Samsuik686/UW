package com.jimi.uw_server.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({ "serial", "unchecked" })
public abstract class BaseInputHelpSocketLog<M extends BaseInputHelpSocketLog<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Long id) {
		set("id", id);
		return (M) this;
	}


	public java.lang.Long getId() {
		return getLong("id");
	}


	public M setPack(java.lang.String pack) {
		set("pack", pack);
		return (M) this;
	}


	public java.lang.String getPack() {
		return getStr("pack");
	}


	public M setTime(java.util.Date time) {
		set("time", time);
		return (M) this;
	}


	public java.util.Date getTime() {
		return get("time");
	}


	public M setIsSend(java.lang.Boolean isSend) {
		set("is_send", isSend);
		return (M) this;
	}


	public java.lang.Boolean getIsSend() {
		return get("is_send");
	}

}
