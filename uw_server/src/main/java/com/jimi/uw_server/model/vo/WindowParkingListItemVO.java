package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.bo.RecordItem;
import com.jimi.uw_server.service.MaterialService;

/**
 * 
 * @author HardyYao
 * @createTime 2018年7月23日 下午4:04:25
 */
@SuppressWarnings("serial")
public class WindowParkingListItemVO extends TaskLog {

	private static MaterialService materialService = Enhancer.enhance(MaterialService.class);

	private static final String COUNT_MATERIAL_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ?";

	private List<?> details;

	private String supplierName;

	private Integer remainderQuantity;

	private Integer superIssuedQuantity;

	private String typeString;


	public String getType(Integer type) {
		if (type == TaskType.IN) {
			typeString = "入库";
		} else if (type == TaskType.OUT) {
			typeString = "出库";
		} else if (type == TaskType.COUNT) {
			typeString = "盘点";
		}  else if (type == TaskType.POSITION_OPTIZATION) {
			typeString = "位置优化";
		} else if (type == TaskType.SEND_BACK) {
			typeString = "退料入库";
		}
		return typeString;
	}

	public WindowParkingListItemVO(Integer packingListItemId, String fileName, Integer type, String materialNo, Integer planQuantity, Integer actualQuantity, Integer materialTypeId) {
		this.set("id", packingListItemId);
		this.set("fileName", fileName);
		this.set("type", getType(type));
		this.set("materialNo", materialNo);
		this.set("planQuantity", planQuantity);
		this.set("actualQuantity", actualQuantity);
		this.setSupplierName(materialTypeId);
		this.set("supplierName", getSupplierName());
		this.setRemainderQuantity(materialTypeId);
		this.set("remainderQuantity", getRemainderQuantity());
		this.setSuperIssuedQuantity(materialTypeId);
		this.set("superIssuedQuantity", getSuperIssuedQuantity());
	}

	public List<?> getDetails() {
		return details;
	}

	public void setDetails(List<?> details) {
		this.set("details", details);
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(Integer materialTypeId) {
		// 获取物料类型
		MaterialType m = MaterialType.dao.findById(materialTypeId);
		// 根据物料类型获取供应商信息
		Supplier s = Supplier.dao.findById(m.getSupplier());
		this.supplierName = s.getName();
	}

	public Integer getRemainderQuantity() {
		return remainderQuantity;
	}

	public void setRemainderQuantity(Integer materialTypeId) {
		Material material = Material.dao.findFirst(COUNT_MATERIAL_SQL, materialTypeId);
		if (material.get("quantity") == null) {
			this.remainderQuantity = 0;
		} else {
			this.remainderQuantity = Integer.parseInt(material.get("quantity").toString());
		}
	}

	public Integer getSuperIssuedQuantity() {
		return superIssuedQuantity;
	}

	public void setSuperIssuedQuantity(Integer materialTypeId) {
		List<RecordItem> recordItemList = new ArrayList<RecordItem>();	// 用于存放完整的物料出入库记录
		recordItemList = materialService.getRecordItemList(materialTypeId);
		if (recordItemList.size() > 0) {
			RecordItem lastRecord = recordItemList.get(recordItemList.size() - 1);
			this.superIssuedQuantity = lastRecord.getSuperIssuedQuantity();
		} else {
			this.superIssuedQuantity = 0;
		}
	}

}