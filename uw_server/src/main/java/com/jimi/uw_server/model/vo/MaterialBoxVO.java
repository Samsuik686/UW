package com.jimi.uw_server.model.vo;

import com.jimi.uw_server.model.MaterialBox;

/**
 * 料盒表示层对象
 * @author HardyYao
 * @createTime 2018年9月7日  上午10:56:49
 */

@SuppressWarnings("serial")
public class MaterialBoxVO extends MaterialBox {

	private String enabledString;

	private String isOnShelfString;

	public MaterialBoxVO(Integer id, Integer area, Integer row, Integer col, Integer height, Boolean enabled, Boolean isOnShelf) {
		this.setId(id);
		this.setArea(area);
		this.setRow(row);
		this.setCol(col);
		this.setHeight(height);
		this.setEnabled(enabled);
		this.setEnabledString(enabled);
		this.set("enabledString", getEnabledString());
		this.set("isOnShelf", isOnShelf);
		this.setIsOnShelfString(isOnShelf);
		this.set("isOnShelfString", getIsOnShelfString());
	}

	public String getEnabledString() {
		return enabledString;
	}

	public void setEnabledString(Boolean enabled) {
		if (enabled) {
			this.enabledString = "是";
		} else {
			this.enabledString = "否";
		}
	}

	public String getIsOnShelfString() {
		return isOnShelfString;
	}

	public void setIsOnShelfString(Boolean isOnShelf) {
		if (isOnShelf) {
			this.isOnShelfString = "是";
		} else {
			this.isOnShelfString = "否";
		}
	}

}
