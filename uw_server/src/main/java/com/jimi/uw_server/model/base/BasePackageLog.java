package com.jimi.uw_server.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BasePackageLog<M extends BasePackageLog<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Long id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.Long getId() {
		return getLong("id");
	}

	public M setTime(java.util.Date time) {
		set("time", time);
		return (M)this;
	}
	
	public java.util.Date getTime() {
		return get("time");
	}

	public M setFromId(java.lang.String fromId) {
		set("from_id", fromId);
		return (M)this;
	}
	
	public java.lang.String getFromId() {
		return getStr("from_id");
	}

	public M setToId(java.lang.String toId) {
		set("to_id", toId);
		return (M)this;
	}
	
	public java.lang.String getToId() {
		return getStr("to_id");
	}

	public M setPackageType(java.lang.String packageType) {
		set("package_type", packageType);
		return (M)this;
	}
	
	public java.lang.String getPackageType() {
		return getStr("package_type");
	}

	public M setPackageRequest(java.lang.String packageRequest) {
		set("package_request", packageRequest);
		return (M)this;
	}
	
	public java.lang.String getPackageRequest() {
		return getStr("package_request");
	}

	public M setPackageResponse(java.lang.String packageResponse) {
		set("package_response", packageResponse);
		return (M)this;
	}
	
	public java.lang.String getPackageResponse() {
		return getStr("package_response");
	}

	public M setConsumeTime(java.lang.Integer consumeTime) {
		set("consume_time", consumeTime);
		return (M)this;
	}
	
	public java.lang.Integer getConsumeTime() {
		return getInt("consume_time");
	}

}