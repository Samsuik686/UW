package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Record;


public class PreciousIOTaskInfo {

	public String no;

	public Integer packingListItemId;

	public Integer planQuantity;

	public Integer storeQuantity;

	public Integer actuallyQuantity;

	public Integer lackQuantity;

	public String specification;

	public String supplier;

	public String designator;

	public Integer scanNum;

	public Date oldestMaterialDate;

	List<IOTaskItemInfo> infos;


	public String getNo() {
		return no;
	}


	public void setNo(String no) {
		this.no = no;
	}


	public Integer getPackingListItemId() {
		return packingListItemId;
	}


	public void setPackingListItemId(Integer packingListItemId) {
		this.packingListItemId = packingListItemId;
	}


	public Integer getPlanQuantity() {
		return planQuantity;
	}


	public void setPlanQuantity(Integer planQuantity) {
		this.planQuantity = planQuantity;
	}


	public Integer getStoreQuantity() {
		return storeQuantity;
	}


	public void setStoreQuantity(Integer storeQuantity) {
		this.storeQuantity = storeQuantity;
	}


	public Integer getActuallyQuantity() {
		return actuallyQuantity;
	}


	public void setActuallyQuantity(Integer actuallyQuantity) {
		this.actuallyQuantity = actuallyQuantity;
	}


	public Integer getLackQuantity() {
		return lackQuantity;
	}


	public void setLackQuantity(Integer lackQuantity) {
		this.lackQuantity = lackQuantity;
	}


	public String getSpecification() {
		return specification;
	}


	public void setSpecification(String specification) {
		this.specification = specification;
	}


	public String getSupplier() {
		return supplier;
	}


	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}


	public String getDesignator() {
		return designator;
	}


	public void setDesignator(String designator) {
		this.designator = designator;
	}


	public Integer getScanNum() {
		return scanNum;
	}


	public void setScanNum(Integer scanNum) {
		this.scanNum = scanNum;
	}


	public List<IOTaskItemInfo> getInfos() {
		return infos;
	}


	public void setInfos(List<IOTaskItemInfo> infos) {
		this.infos = infos;
	}


	public Date getOldestMaterialDate() {
		return oldestMaterialDate;
	}


	public void setOldestMaterialDate(Date oldestMaterialDate) {
		this.oldestMaterialDate = oldestMaterialDate;
	}


	public static List<PreciousIOTaskInfo> fillList(List<Record> taskInfoRecords, List<Record> uwStoreRecords, List<Record> oldestMaterialRecordsWithNotCycle, List<Record> oldestMaterialRecordsWithCycle) {
		Map<Integer, PreciousIOTaskInfo> map = new HashMap<>();
		for (Record record : taskInfoRecords) {
			PreciousIOTaskInfo info = map.get(record.getInt("PackingListItem_Id"));
			if (info == null) {
				info = new PreciousIOTaskInfo();
				info.setPackingListItemId(record.getInt("PackingListItem_Id"));
				info.setPlanQuantity(record.getInt("PackingListItem_Quantity"));
				info.setDesignator(record.getStr("MaterialType_Designator"));
				info.setNo(record.getStr("MaterialType_No"));
				info.setSpecification(record.getStr("MaterialType_Specification"));
				info.setSupplier(record.getStr("Supplier_Name"));
				info.setStoreQuantity(0);
				if (record.getInt("TaskLog_Id") != null) {
					info.setActuallyQuantity(record.getInt("TaskLog_Quantity"));
					List<IOTaskItemInfo> itemInfos = new ArrayList<>();
					itemInfos.add(IOTaskItemInfo.fill(record));
					info.setInfos(itemInfos);
					info.setScanNum(1);
					info.setLackQuantity(record.getInt("PackingListItem_Quantity") - record.getInt("TaskLog_Quantity"));
				} else {
					info.setLackQuantity(record.getInt("PackingListItem_Quantity"));
					info.setActuallyQuantity(0);
					info.setScanNum(0);
				}
				map.put(record.getInt("PackingListItem_Id"), info);
			} else {
				if (record.getInt("TaskLog_Id") != null) {
					info.setActuallyQuantity(record.getInt("TaskLog_Quantity") + info.getActuallyQuantity());
					info.getInfos().add(IOTaskItemInfo.fill(record));
					info.setScanNum(info.getScanNum() + 1);
					info.setLackQuantity(record.getInt("PackingListItem_Quantity") - info.getActuallyQuantity());
				}
			}
		}
		for (Record record : oldestMaterialRecordsWithNotCycle) {
			PreciousIOTaskInfo info = map.get(record.getInt("PackingListItem_Id"));
			if (info != null) {
				info.setOldestMaterialDate(record.getDate("production_time"));
			}
		}
		for (Record record : oldestMaterialRecordsWithCycle) {
			PreciousIOTaskInfo info = map.get(record.getInt("PackingListItem_Id"));
			if (info != null && info.getOldestMaterialDate() == null) {
				info.setOldestMaterialDate(record.getDate("production_time"));
			}
		}
		for (Record record : uwStoreRecords) {
			PreciousIOTaskInfo info = map.get(record.getInt("PackingListItem_Id"));
			if (info != null) {
				info.setStoreQuantity(record.getInt("uwStore"));
			}
		}
		List<PreciousIOTaskInfo> infos = new ArrayList<>(map.values());

		return infos;
	}

}
