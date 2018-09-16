package com.jimi.uw_server.model.vo;

import com.jimi.uw_server.model.MaterialBox;

/**
 * 
 * 
 * @author HardyYao
 * @createTime 2018年9月7日  上午10:56:49
 */

public class MaterialBoxVO extends MaterialBox {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3689388827559340799L;

	private String enabledString;


	public String getEnabledString(Boolean enabled) {
		if (enabled) {
			this.enabledString = "是";
		} else {
			this.enabledString = "否";
		}
		return enabledString;
	}


	public MaterialBoxVO(Integer id, Integer area, Integer row, Integer col, Integer height, Boolean enabled) {
		this.setId(id);
		this.setArea(area);
		this.setRow(row);
		this.setCol(col);
		this.setHeight(height);
		this.setEnabled(enabled);
		this.set("enabledString", getEnabledString(enabled));
	}


}
