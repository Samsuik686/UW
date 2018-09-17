package com.jimi.uw_server.model.vo;

import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialType;

/**
 * @author HardyYao
 * @createTime 2018年7月5日 上午11:25:16 
 */
public class MaterialTypeVO extends MaterialType{

	private static final long serialVersionUID = 6512994067269010575L;

	private static final String COUNT_MATERIAL_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ?";

//	private static final String GET_BOX_ID_SQL = "SELECT box FROM material WHERE type = ?";

	private String enabledString;

	private Integer quantity;

	public Integer getQuantity(Integer id) {
		Material material = Material.dao.findFirst(COUNT_MATERIAL_SQL, id);
		if (material.get("quantity") == null) {
			quantity = 0;
		} else {
			quantity = Integer.parseInt(material.get("quantity").toString());
		}
		return quantity;
	}
	
	public String getEnabledString(Boolean enabled) {
		if (enabled) {
			this.enabledString = "是";
		} else {
			this.enabledString = "否";
		}
		return enabledString;
	}

//	public List<Integer> getBoxId(Integer id) {
//		List<Integer> boxIdList = new ArrayList<Integer>();
//		List<Material> material = Material.dao.find(GET_BOX_ID_SQL, id);
//		for (Material m : material) {
//			Integer boxId = m.get("box");
//			boxIdList.add(boxId);
//		}
//		return boxIdList;
//	}

	public MaterialTypeVO(Integer id, String no, String specification, Boolean enabled) {
		this.setId(id);
		this.setNo(no);
		this.setSpecification(specification);
//		this.set("boxIdList", getBoxId(id));
		this.setEnabled(enabled);
		this.set("enabledString", getEnabledString(enabled));
		this.set("quantity", getQuantity(id));
	}

}
 