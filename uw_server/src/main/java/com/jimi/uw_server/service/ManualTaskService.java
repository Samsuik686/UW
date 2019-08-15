package com.jimi.uw_server.service;

import java.util.Date;
import java.util.List;

import com.jimi.uw_server.constant.DestinationSQL;
import com.jimi.uw_server.constant.MaterialTypeSQL;
import com.jimi.uw_server.constant.SupplierSQL;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Destination;
import com.jimi.uw_server.model.ExternalWhLog;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.bo.ManualTaskInfo;
import com.jimi.uw_server.model.bo.ManualTaskRecord;
import com.jimi.uw_server.model.bo.MaterialReel;


public class ManualTaskService {

	private static final int UW_ID = 0;

	public static final ManualTaskService me = new ManualTaskService();


	public Integer create(String supplierName, Integer type, String destinationName) {

		Supplier supplier = Supplier.dao.findFirst(SupplierSQL.GET_SUPPLIER_BY_NAME, supplierName);
		if (supplier == null) {
			throw new OperationException("供应商不存在！");
		}
		Task task = new Task();
		if (type == 1) {
			Destination destination = Destination.dao.findFirst(DestinationSQL.GET_DESTINATION_BY_NAME, destinationName);
			if (destination == null) {
				throw new OperationException("目的仓库不存在！");
			}
			task.setDestination(destination.getId());
		}
		String taskName = supplierName + "_紧急手动任务";
		task.setFileName(taskName).setSupplier(supplier.getId()).setType(type).setCreateTime(new Date()).setState(TaskState.WAIT_START).save();
		return task.getId();
	}


	public String uploadRecord(ManualTaskInfo info) {
		String resultString = "导入成功！";

		Task task = Task.dao.findById(info.getTaskId());
		if (task == null) {
			resultString = "导入料盘记录表失败，任务不存在！";
			return resultString;
		}
		if (task.getState() != TaskState.WAIT_START) {
			resultString = "任务并未处于已审核状态，无法人工出库！";
			return resultString;
		}

		List<ManualTaskRecord> records = info.getRecords();
		if (!records.isEmpty()) {
			for (ManualTaskRecord record : records) {
				Supplier supplier = Supplier.dao.findFirst(SupplierSQL.GET_SUPPLIER_BY_NAME, record.getSupplierName());
				if (supplier == null) {
					resultString += "[供应商 " + record.getSupplierName() + " 不存在]";
					return resultString;
				}
				MaterialType materialType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_SUPPLIER_AND_NAME, record.getNo(), supplier.getId());
				if (materialType == null) {
					resultString += "[物料类型 " + record.getNo() + " 不存在]";
					return resultString;
				}
				PackingListItem packingListItem = new PackingListItem().setTaskId(task.getId()).setQuantity(record.getPlanQuantity()).setMaterialTypeId(materialType.getId()).setFinishTime(new Date());
				packingListItem.save();
				if (packingListItem.getId() <= 0) {
					resultString += "[料号" + record.getNo() + "的出库条目保存失败]";
					return resultString;
				}
				List<MaterialReel> materialReels = record.getMaterialReels();
				int actualQuantity = 0;
				String operator = "";
				if (!materialReels.isEmpty()) {
					for (MaterialReel materialReel : materialReels) {
						Material material = Material.dao.findById(materialReel.getMaterialId());
						if (material == null || !material.getIsInBox()) {
							resultString += "[料盘码" + materialReel.getMaterialId() + "的出库条目保存失败,料盘不存在或者不在盒内]";
							continue;
						}
						if (!material.getRemainderQuantity().equals(materialReel.getQuantity())) {
							resultString += "[料盘码" + materialReel.getMaterialId() + "的出库条目保存失败,料盘剩余数量与出库数量不符]";
							continue;
						}
						operator = materialReel.getOperator();
						TaskLog taskLog = new TaskLog().setAuto(false).setMaterialId(materialReel.getMaterialId()).setOperator(materialReel.getOperator()).setQuantity(material.getRemainderQuantity()).setPackingListItemId(packingListItem.getId()).setDestination(task.getDestination()).setTime(new Date());
						material.setRemainderQuantity(0).setCol(-1).setRow(-1).setIsInBox(false).update();
						actualQuantity += materialReel.getQuantity();
						taskLog.save();
					}
					task.setState(TaskState.FINISHED);
				}
				if (actualQuantity != 0) {
					if (packingListItem.getQuantity() < actualQuantity) {
						// 超发
						ExternalWhLog externalWhLog = new ExternalWhLog();
						externalWhLog.setMaterialTypeId(packingListItem.getMaterialTypeId());
						externalWhLog.setDestination(task.getDestination());
						externalWhLog.setSourceWh(UW_ID);
						externalWhLog.setTaskId(task.getId());
						externalWhLog.setQuantity(actualQuantity - packingListItem.getQuantity());
						externalWhLog.setTime(new Date());
						externalWhLog.setOperatior(operator);
						externalWhLog.save();
					}
				}
			}
		}
		return resultString;
	}
	
}