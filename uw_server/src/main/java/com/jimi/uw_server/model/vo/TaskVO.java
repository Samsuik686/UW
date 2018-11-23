package com.jimi.uw_server.model.vo;

import java.util.Date;

import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;

@SuppressWarnings("serial")
public class TaskVO extends Task{

	private static final String GET_PACKING_LIST_ITEM_BY_TASK_ID_SQL = "SELECT * FROM packing_list_item WHERE task_id = ?";

	private String typeString;
	
	private String stateString;

	private String supplierName;

	public String getTypeString(Integer type) {
		if (type == 0) {
			this.typeString = "入库";
		} else if (type == 1) {
			this.typeString = "出库";
		} else if (type == 2) {
			this.typeString = "盘点";
		}  else if (type == 3) {
			this.typeString = "位置优化";
		} else if (type == 4) {
			this.typeString = "退料入库";
		}
		return typeString;
	}

	public String getStateString(Integer state) {
		if (state == 0) {
			this.stateString = "未审核";
		} else if (state == 1) {
			this.stateString = "未开始";
		} else if (state == 2) {
			this.stateString = "进行中";
		}  else if (state == 3) {
			this.stateString = "已完成";
		} else if (state == 4) {
			this.stateString = "已作废";
		}
		return stateString;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(Integer taskId) {
		PackingListItem packingListItem = PackingListItem.dao.findFirst(GET_PACKING_LIST_ITEM_BY_TASK_ID_SQL, taskId);
		MaterialType m = MaterialType.dao.findById(packingListItem.getMaterialTypeId());
		Supplier s = Supplier.dao.findById(m.getSupplier());
		String name = s.getName();
		this.supplierName = name;
	}

	public TaskVO(Integer id, Integer state, Integer type, String fileName, Date createTime) {
		this.setId(id);
		this.setState(state);
		this.set("stateString", getStateString(state));
		this.setType(type);
		this.set("typeString", getTypeString(type));
		this.set("fileName", fileName);
		this.setSupplierName(id);
		this.set("supplierName", getSupplierName());
		this.set("createTimeString", createTime);
	}

	
}
