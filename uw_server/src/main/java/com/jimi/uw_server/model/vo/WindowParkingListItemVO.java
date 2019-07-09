package com.jimi.uw_server.model.vo;

import java.util.List;

import com.jimi.uw_server.model.GoodsLocation;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.constant.TaskType;

/**
 * 仓口停泊条目表示层
 * @author HardyYao
 * @createTime 2018年7月23日 下午4:04:25
 */

public class WindowParkingListItemVO {


	private List<PackingListItemDetailsVO> details;

	private Integer id;
	
	private String fileName;
	
	private Integer materialTypeId;
	
	private Integer planQuantity;
	
	private Integer actualQuantity;
	
	private String supplierName;
	
	private Integer uwStoreQuantity;

	private String typeString;

	private String specification;
	
	private Integer reelNum;
	
	private Boolean isForceFinish;
	
	private Integer eWhStoreQuantity;
	
	private String materialNo;

	private Integer windowId;
	
	private Integer goodsLocationId;
	
	private String goodsLocationName;
	
	private Integer BoxId;
	
	public WindowParkingListItemVO(GoodsLocation goodsLocation) {
		this.setGoodsLocationId(goodsLocation.getId());
		this.setGoodsLocationName(goodsLocation.getName());
		this.setWindowId(goodsLocation.getWindowId());;
	}
	
	public List<PackingListItemDetailsVO> getDetails() {
		return details;
	}

	
	public void setDetails(List<PackingListItemDetailsVO> details) {
		this.details = details;
	}

	
	public Integer getId() {
		return id;
	}

	
	public void setId(Integer id) {
		this.id = id;
	}

	
	public String getFileName() {
		return fileName;
	}

	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	
	public Integer getMaterialTypeId() {
		return materialTypeId;
	}

	
	public void setMaterialTypeId(Integer materialTypeId) {
		this.materialTypeId = materialTypeId;
	}

	
	public Integer getPlanQuantity() {
		return planQuantity;
	}

	
	public void setPlanQuantity(Integer planQuantity) {
		this.planQuantity = planQuantity;
	}

	
	public Integer getActualQuantity() {
		return actualQuantity;
	}

	
	public void setActualQuantity(Integer actualQuantity) {
		this.actualQuantity = actualQuantity;
	}

	
	public String getSupplierName() {
		return supplierName;
	}

	
	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	
	public String getTypeString() {
		return typeString;
	}

	
	public void setTypeString(String typeString) {
		this.typeString = typeString;
	}

	
	public Boolean getIsForceFinish() {
		return isForceFinish;
	}

	
	public void setIsForceFinish(Boolean isForceFinish) {
		this.isForceFinish = isForceFinish;
	}

	
	public Integer geteWhStoreQuantity() {
		return eWhStoreQuantity;
	}

	
	public void seteWhStoreQuantity(Integer eWhStoreQuantity) {
		this.eWhStoreQuantity = eWhStoreQuantity;
	}

	
	public void setReelNum(Integer reelNum) {
		this.reelNum = reelNum;
	}

	public Integer getUwStoreQuantity() {
		return uwStoreQuantity;
	}

	public void setUwStoreQuantity(Integer uwStoreQuantity) {
		this.uwStoreQuantity = uwStoreQuantity;
	}


	public Integer getWindowId() {
		return windowId;
	}

	
	public void setWindowId(Integer windowId) {
		this.windowId = windowId;
	}

	
	public Integer getGoodsLocationId() {
		return goodsLocationId;
	}

	
	public void setGoodsLocationId(Integer goodsLocationId) {
		this.goodsLocationId = goodsLocationId;
	}

	
	public String getGoodsLocationName() {
		return goodsLocationName;
	}

	
	public void setGoodsLocationName(String goodsLocationName) {
		this.goodsLocationName = goodsLocationName;
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


	public void setSpecification(String specification) {
		this.specification = specification;
	}
	
	
	public String getMaterialNo() {
		return materialNo;
	}

	
	public void setMaterialNo(String materialNo) {
		this.materialNo = materialNo;
	}

	
	public Integer getBoxId() {
		return BoxId;
	}

	
	public void setBoxId(Integer boxId) {
		BoxId = boxId;
	}

	public WindowParkingListItemVO fill(List<PackingListItemDetailsVO> details, Record record, Integer eWhStoreQuantity, Integer uwStoreQuantity, Integer actualQuantity, Integer reelNum, Boolean isForceFinish, Integer boxId) {
		this.setMaterialTypeId(record.getInt("MaterialType_Id"));
		this.setSpecification(record.getStr("MaterialType_Specification"));
		this.setMaterialNo(record.getStr("MaterialType_No"));
		this.setSupplierName(record.getStr("Supplier_Name"));
		this.setId(record.getInt("PackingListItem_Id"));
		this.setPlanQuantity(record.getInt("PackingListItem_Quantity"));
		this.setTypeString(record.getInt("Task_Type"));
		this.setFileName(record.getStr("Task_FileName"));
		this.setActualQuantity(actualQuantity);
		this.setIsForceFinish(isForceFinish);
		this.setUwStoreQuantity(uwStoreQuantity);
		this.seteWhStoreQuantity(eWhStoreQuantity);
		this.setReelNum(reelNum);
		this.setDetails(details);
		this.setBoxId(boxId);
		return this;
	}
}
