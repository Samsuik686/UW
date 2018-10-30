package com.jimi.uw_server.model.vo;

import com.jimi.uw_server.model.Material;

/**
 * 
 * @author HardyYao
 * @createTime 2018年10月30日  下午3:35:30
 */

public class MaterialVO extends Material {

	private static final long serialVersionUID = 4118106789167442891L;


	public MaterialVO(String id, Integer row, Integer col, Integer remainderQuantity) {
		this.setId(id);
		this.setRow(row);
		this.setCol(col);
		this.set("remainderQuantity", remainderQuantity);
	}
	

}
