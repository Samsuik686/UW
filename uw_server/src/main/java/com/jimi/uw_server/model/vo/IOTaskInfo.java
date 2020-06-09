package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.constant.MaterialStatus;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.model.Task;


public class IOTaskInfo {

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

	private String cycle;

	public Boolean cutBoolean;

	public String cutString;

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


	public Boolean getCutBoolean() {
		return cutBoolean;
	}


	public void setCutBoolean(Boolean cutBoolean) {
		this.cutBoolean = cutBoolean;
	}


	public String getCutString() {
		return cutString;
	}


	public void setCutString(String cutString) {
		this.cutString = cutString;
	}


	public String getCycle() {
		return cycle;
	}


	public void setCycle(String cycle) {
		this.cycle = cycle;
	}


	public static List<IOTaskInfo> fillList(Task task, List<Record> taskInfoRecords, List<Record> uwStoreRecords, List<Record> oldestMaterialRecords) {
		Map<Integer, IOTaskInfo> map = new HashMap<>();
		for (Record record : taskInfoRecords) {
			IOTaskInfo info = map.get(record.getInt("PackingListItem_Id"));
			if (info == null) {
				info = new IOTaskInfo();
				info.setCutBoolean(false);
				info.setCutString("否");
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
					if (record.getInt("Material_Status") != null && record.getInt("Material_Status").equals(MaterialStatus.CUTTING)) {
						info.setCutBoolean(true);
						info.setCutString("是");
					}
					itemInfos.add(IOTaskItemInfo.fill(record));
					info.setInfos(itemInfos);
					info.setScanNum(1);
					if (task.getType().equals(TaskType.IN) || task.getType().equals(TaskType.SEND_BACK)) {
						info.setLackQuantity(info.getActuallyQuantity() - info.getPlanQuantity());
					} else if (task.getType().equals(TaskType.OUT)) {
						info.setLackQuantity(info.getPlanQuantity() - info.getActuallyQuantity());
					}
				} else {
					info.setActuallyQuantity(0);
					if (task.getType().equals(TaskType.IN) || task.getType().equals(TaskType.SEND_BACK)) {
						info.setLackQuantity(info.getActuallyQuantity() - info.getPlanQuantity());
					} else if (task.getType().equals(TaskType.OUT)) {
						info.setLackQuantity(info.getPlanQuantity() - info.getActuallyQuantity());
					}
					info.setScanNum(0);
				}
				map.put(record.getInt("PackingListItem_Id"), info);
			} else {
				if (record.getInt("TaskLog_Id") != null) {
					if (record.getInt("Material_Status") != null && record.getInt("Material_Status").equals(MaterialStatus.CUTTING)) {
						info.setCutBoolean(true);
						info.setCutString("是");
					}
					info.setActuallyQuantity(record.getInt("TaskLog_Quantity") + info.getActuallyQuantity());
					info.getInfos().add(IOTaskItemInfo.fill(record));
					info.setScanNum(info.getScanNum() + 1);
					if (task.getType().equals(TaskType.IN) || task.getType().equals(TaskType.SEND_BACK)) {
						info.setLackQuantity(info.getActuallyQuantity() - info.getPlanQuantity());
					} else if (task.getType().equals(TaskType.OUT)) {
						info.setLackQuantity(info.getPlanQuantity() - info.getActuallyQuantity());
					}
				}
			}
		}
		for (Record record : oldestMaterialRecords) {
			IOTaskInfo info = map.get(record.getInt("PackingListItem_Id"));
			if (info != null && info.getOldestMaterialDate() == null) {
				info.setOldestMaterialDate(record.getDate("production_time"));
				info.setCycle(record.getStr("cycle"));
			}
		}
		for (Record record : uwStoreRecords) {
			IOTaskInfo info = map.get(record.getInt("PackingListItem_Id"));
			if (info != null) {
				info.setStoreQuantity(record.getInt("uwStore"));
			}
		}
		List<IOTaskInfo> infos = new ArrayList<>(map.values());

		return infos;
	}

}
