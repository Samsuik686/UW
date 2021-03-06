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
		return (M)this;
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public M setNo(java.lang.String no) {
		set("no", no);
		return (M)this;
	}
	
	public java.lang.String getNo() {
		return getStr("no");
	}

	public M setArea(java.lang.Integer area) {
		set("area", area);
		return (M)this;
	}
	
	public java.lang.Integer getArea() {
		return getInt("area");
	}

	public M setRow(java.lang.Integer row) {
		set("row", row);
		return (M)this;
	}
	
	public java.lang.Integer getRow() {
		return getInt("row");
	}

	public M setCol(java.lang.Integer col) {
		set("col", col);
		return (M)this;
	}
	
	public java.lang.Integer getCol() {
		return getInt("col");
	}

	public M setHeight(java.lang.Integer height) {
		set("height", height);
		return (M)this;
	}
	
	public java.lang.Integer getHeight() {
		return getInt("height");
	}

	public M setEnabled(java.lang.Boolean enabled) {
		set("enabled", enabled);
		return (M)this;
	}
	
	public java.lang.Boolean getEnabled() {
		return get("enabled");
	}

	public M setIsOnShelf(java.lang.Boolean isOnShelf) {
		set("is_on_shelf", isOnShelf);
		return (M)this;
	}
	
	public java.lang.Boolean getIsOnShelf() {
		return get("is_on_shelf");
	}

}
