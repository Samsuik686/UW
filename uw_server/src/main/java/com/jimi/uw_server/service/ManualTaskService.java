package com.jimi.uw_server.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.plugin.activerecord.Db;
import com.jimi.uw_server.constant.DestinationSQL;
import com.jimi.uw_server.constant.SupplierSQL;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Destination;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.bo.MaterialInfoBO;
import com.jimi.uw_server.util.ExcelHelper;


public class ManualTaskService {

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
		task.setFileName(taskName).setSupplier(supplier.getId()).setType(type).setCreateTime(new Date()).save();
		return task.getId();
	}


	/*public String importOutRecords(Integer taskId, File file, User user) {
		ExcelHelper fileReader;
		String resultString = "导入成功！";
		try {
			Task task = Task.dao.findById(taskId);
			if (task == null) {
				resultString = "导入料盘记录表失败，任务不存在！";
				return resultString;
			}
			if (task.getState() != TaskState.WAIT_START) {
				resultString = "任务并未处于已审核状态，无法人工出库！";
				return resultString;
			}
			fileReader = ExcelHelper.from(file);
			List<MaterialInfoBO> items = fileReader.unfill(MaterialInfoBO.class, 0);
			if (items == null || items.size() == 0) {
				resultString = "导入料盘记录表失败，料盘记录表表头错误或者表格中没有任何有效的料盘信息记录！";
				return resultString;
			} else {
				List<Material> materials = new ArrayList<>();
				List<TaskLog> taskLogs = new ArrayList<>();
				Map<Integer, Integer> typeNumMap = new HashMap<>();
				int i = 2;
				for (MaterialInfoBO item : items) {
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) {
						if (item.getMaterialId() == null || item.getNo() == null || item.getSupplier() == null || item.getQuantity() == null || item.getProdutionDate() == null) {
							resultString = "导入料盘记录表失败，请检查单表格第" + i + "行的料盘号/料号/供应商/数量/生产日期列是否填写了准确信息！";
							return resultString;
						}
						
						if (item.getQuantity() <= 0) {
							resultString = "导入料盘记录表失败，请检查表格第" + i + "行的数量是否正确，料盘内物料数量需大于0";
							return resultString;
						}
						Supplier supplier = Supplier.dao.findFirst(GET_SUPPLIER_BY_NAME, item.getSupplier());
						if (supplier == null) {
							resultString = "导入料盘记录表失败，请检查表格第" + i + "行的供应商是否正确，确保在系统中已存在该供应商";
							return resultString;
						}
						MaterialType mType = MaterialType.dao.findFirst(GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_SQL, item.getNo(), supplier.getId());
						if (mType == null) {
							resultString = "导入料盘记录表失败，请检查表格第" + i + "行的料号和供应商是否正确，确保在系统中已存在该供应商和该料号";
							return resultString;
						}
						PackingListItem packingListItem = PackingListItem.dao.findFirst(GET_TASK_ITEMS_BY_TASK_AND_TYPE_SQL, taskId, mType.getId());
						if (packingListItem == null) {
							resultString = "导入料盘记录表失败，请检查表格第" + i + "行的料号是否正确，确保在该任务存在该物料的任务条目";
							return resultString;
						}
						Material material = Material.dao.findFirst(GET_MATERIAL_BY_ID_SQL, item.getMaterialId());
						if (material == null) {
							resultString = "导入料盘记录表失败，请检查表格第" + i + "行的料盘码是否正确，该料盘码不存在系统中";
							return resultString;
						}
						if (material.getBox() != null) {
							resultString = "导入料盘记录表失败，请检查表格第" + i + "行的料盘码是否正确，该料盘码非手动入库料盘，手动出库仅能出手动入库的料盘";
							return resultString;
						}
						if (!material.getRemainderQuantity().equals(item.getQuantity())) {
							resultString = "导入料盘记录表失败，请检查表格第" + i + "行的物料数量是否正确，数量与仓库内该料盘数量不等，手动出库必须出整个料盘";
							return resultString;
						}
						material.setRemainderQuantity(0);
						if (typeNumMap.get(mType.getId()) == null || typeNumMap.get(mType.getId()).equals(0)) {
							typeNumMap.put(mType.getId(), item.getQuantity());
						} else {
							int quantity = typeNumMap.get(mType.getId()) + item.getQuantity();
							typeNumMap.put(mType.getId(), quantity);
						}
						TaskLog taskLog = new TaskLog();
						taskLog.setPackingListItemId(packingListItem.getId());
						taskLog.setMaterialId(item.getMaterialId());
						taskLog.setQuantity(item.getQuantity());
						taskLog.setOperator(user.getUid());
						// 区分入库操作人工还是机器操作,目前的版本暂时先统一写成人工操作
						taskLog.setAuto(false);
						taskLog.setTime(new Date());
						taskLog.setDestination(task.getDestination());
						materials.add(material);
						taskLogs.add(taskLog);

					}
					i++;
				}
				for (Material material : materials) {
					material.update();
				}
				for (TaskLog taskLog : taskLogs) {
					taskLog.save();
				}
				Db.update(SET_PACKING_LIST_ITEM_FINISH, new Date(), taskId);
				task.setState(TaskState.FINISHED).update();

			}
			return resultString;
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException("解析表格失败 + " + e.getStackTrace());
		}

	}*/
}
