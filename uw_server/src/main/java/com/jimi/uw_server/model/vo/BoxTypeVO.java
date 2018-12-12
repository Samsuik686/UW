package com.jimi.uw_server.model.vo;

import com.jimi.uw_server.model.BoxType;

/**
 * 料盒类型表示层对象
 * @author HardyYao
 * @createTime 2018年12月12日  上午11:07:12
 */

@SuppressWarnings("serial")
public class BoxTypeVO extends BoxType {

	private String enabledString;


	public BoxTypeVO(Integer id, Integer cellWidth, Integer cellRows, Integer cellCols, Boolean enabled) {
		this.setId(id);
		this.set("cellWidth", cellWidth);
		this.set("cellRows", cellRows);
		this.set("cellCols", cellCols);
		this.setEnabled(enabled);
		this.setEnabledString(enabled);
		this.set("enabledString", getEnabledString());
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

}
