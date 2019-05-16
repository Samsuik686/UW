package com.jimi.uw_server.model.vo;

import java.util.List;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.Supplier;
import com.jfinal.aop.Enhancer;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.service.MaterialService;

/**
 * 仓口停泊条目表示层
 * @author HardyYao
 * @createTime 2018年7月23日 下午4:04:25
 */

public class WindowParkingListItemVO extends TaskLog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static MaterialService materialService = Enhancer.enhance(MaterialService.class);

	private List<?> details;

	private String supplierName;

	private Integer remainderQuantity;
	
	private Integer storeQuantity;

	private String typeString;

	private String specification;
	
	private Integer reelNum;
	
	public WindowParkingListItemVO(Integer packingListItemId, String fileName, Integer type, String materialNo, Integer planQuantity, Integer actualQuantity, Integer materialTypeId, Boolean isForceFinish, Integer storeQuantity) {
		this.set("id", packingListItemId);
		this.set("fileName", fileName);
		this.setTypeString(type);
		this.set("type", getTypeString());
		this.set("materialNo", materialNo);
		this.set("planQuantity", planQuantity);
		this.set("actualQuantity", actualQuantity);
		this.setSupplierName(materialTypeId);
		this.set("supplierName", getSupplierName());
		this.setRemainderQuantity(materialTypeId);
		this.set("remainderQuantity", getRemainderQuantity());
		this.set("isForceFinish", isForceFinish);
		this.setSpecification(materialTypeId);
		this.set("specification", getSpecification());
		this.set("storeQuantity", storeQuantity);
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
		Integer quantity = materialService.countAndReturnRemainderQuantityByMaterialTypeId(materialTypeId);
		this.remainderQuantity = quantity;
	}


	public String getTypeString() {
		return typeString;
	}

	public Integer getStoreQuantity() {
		return storeQuantity;
	}

	public void setStoreQuantity(Integer storeQuantity) {
		this.storeQuantity = storeQuantity;
	}
	
	public void setReelNum(Integer reelNum) {
		this.set("reelNum", reelNum);
	}
	
	public Integer getReelNum() {
		return reelNum;
	}
	
	public void setTypeString(Integer type) {
		switch (type) {
		case TaskType.IN:
			this.typeString = "入库";
			break;
		case TaskType.OUT:
			this.typeString = "出库";
			break;
		case TaskType.COUNT:
			this.typeString = "盘点";
			break;
		case TaskType.POSITION_OPTIZATION:
			this.typeString = "位置优化";
			break;
		case TaskType.SEND_BACK:
			this.typeString = "调拨入库";
			break;
		default:
			this.typeString = "错误类型";
			break;
		}
	}


	public String getSpecification() {
		return specification;
	}


	public void setSpecification(Integer materialTypeId) {
		MaterialType m = MaterialType.dao.findById(materialTypeId);
		this.specification = m.getSpecification();
	}
	
}
