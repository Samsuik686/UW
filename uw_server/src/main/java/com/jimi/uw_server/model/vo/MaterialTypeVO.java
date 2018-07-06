package com.jimi.uw_server.model.vo;

import com.jimi.uw_server.model.MaterialType;

/**
 * @author HardyYao
 * @createTime 2018年7月5日 上午11:25:16 
 */
public class MaterialTypeVO extends MaterialType {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2988989565944159643L;

	private String enabledString;
	
	private Integer quantity;
	
	
	
	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getQuantity() {
		return quantity;
	}
	
	public String getEnabledString() {
		if (this.getEnabled()) {
			this.enabledString = "是";
		} else {
			this.enabledString = "否";
		}
		return enabledString;
	}

	public MaterialTypeVO(Integer id, String no, Integer area, Integer row, Integer col, Integer height, 
			boolean enabled, Integer quantity) {
		this.setId(id);
		this.setNo(no);
		this.setArea(area);
		this.setRow(row);
		this.setCol(col);
		this.setHeight(height);
		this.setEnabled(enabled);
		this.set("enabledString", getEnabledString());
//		this.setIsOnShelf(isOnShelf);
		this.set("quantity", quantity);
	}

}
 