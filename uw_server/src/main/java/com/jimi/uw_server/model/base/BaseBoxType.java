package com.jimi.uw_server.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseBoxType<M extends BaseBoxType<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Integer id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public M setCellWidth(java.lang.Integer cellWidth) {
		set("cell_width", cellWidth);
		return (M)this;
	}
	
	public java.lang.Integer getCellWidth() {
		return getInt("cell_width");
	}

	public M setCellRows(java.lang.Integer cellRows) {
		set("cell_rows", cellRows);
		return (M)this;
	}
	
	public java.lang.Integer getCellRows() {
		return getInt("cell_rows");
	}

	public M setCellCols(java.lang.Integer cellCols) {
		set("cell_cols", cellCols);
		return (M)this;
	}
	
	public java.lang.Integer getCellCols() {
		return getInt("cell_cols");
	}

	public M setEnabled(java.lang.Boolean enabled) {
		set("enabled", enabled);
		return (M)this;
	}
	
	public java.lang.Boolean getEnabled() {
		return get("enabled");
	}

}
