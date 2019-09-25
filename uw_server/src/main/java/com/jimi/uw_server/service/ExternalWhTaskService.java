package com.jimi.uw_server.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Destination;
import com.jimi.uw_server.model.ExternalWhLog;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.bo.TaskItemBO;
import com.jimi.uw_server.model.vo.EWhMaterialDetailVO;
import com.jimi.uw_server.model.vo.ExternalWhInfoVO;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ExcelHelper;
import com.jimi.uw_server.util.ExcelWritter;


/**
 * 
 * @author trjie
 * @createTime 2019年5月8日  上午10:34:10
 */

public class ExternalWhTaskService {

	public static final ExternalWhTaskService me = new ExternalWhTaskService();

	private static final String GET_MATERIAL_TYPE_BY_NO_SQL = "SELECT * FROM material_type WHERE no = ? AND supplier = ? AND enabled = 1";

	private static final String GET_EXTERIALWH_MATERIAL_TYPE_SQL = "SELECT a.*,destination.`name` as wh_name FROM destination INNER JOIN ( SELECT material_type_id AS material_type_id, destination AS wh_id, material_type.`no` as `no`, material_type.specification as `specification`, material_type.supplier as supplier_id, supplier.`name` as supplier_name FROM external_wh_log INNER JOIN material_type INNER JOIN supplier ON external_wh_log.material_type_id = material_type.id AND material_type.supplier = supplier.id WHERE destination != 0 and destination != -1 AND material_type.enabled = 1 GROUP BY material_type_id, destination UNION SELECT material_type_id AS material_type_id, source_wh AS wh_id, material_type.`no` as `no`, material_type.specification as `specification`, material_type.supplier as supplier_id, supplier.`name` as supplier_name FROM external_wh_log INNER JOIN material_type INNER JOIN supplier ON external_wh_log.material_type_id = material_type.id AND material_type.supplier = supplier.id WHERE source_wh != 0 and source_wh != -1 AND material_type.enabled = 1 GROUP BY material_type_id, source_wh ) a ON destination.id = a.wh_id";

	private static final String GET_WEH_MATERIAL_DETAILS_SQL = "SELECT e.*, material_return_record.quantity as return_num FROM((SELECT external_wh_log.*, a.name as `source_wh_name`, b.name as `destination_name`, task.file_name as `task_name`, task.type as `task_type`, task.remarks as remarks, material_type.`no` as `no` FROM external_wh_log INNER JOIN destination a INNER JOIN destination b INNER JOIN task INNER JOIN material_type ON external_wh_log.source_wh = a.id AND external_wh_log.destination = b.id AND material_type_id = material_type.id AND task_id = task.id WHERE external_wh_log.material_type_id = ? and external_wh_log.destination = ?) UNION (SELECT external_wh_log.*, c.name as `source_wh_name`, d.name as `destination_name`, task.file_name as `task_name`, task.type as `task_type`, task.remarks as remarks, material_type.`no` as `no` FROM external_wh_log INNER JOIN destination c INNER JOIN destination d INNER JOIN task INNER JOIN material_type ON external_wh_log.source_wh = c.id AND external_wh_log.destination = d.id AND material_type_id = material_type.id AND task_id = task.id WHERE external_wh_log.material_type_id = ? and external_wh_log.source_wh = ?)) e LEFT JOIN material_return_record ON e.task_id = material_return_record.task_id AND e.material_type_id = material_return_record.material_type_id AND e.source_wh = material_return_record.wh_id ORDER BY e.time ASC";

	private static ExternalWhLogService externalWhLogService = ExternalWhLogService.me;

	public static final MaterialService materialService = MaterialService.me;


	/**
	 * 导入外仓的任务
	 * @param file
	 * @param supplierId
	 * @param sourceHwId
	 * @param destinationHwId
	 * @param user
	 * @return
	 */
	public String importTask(File file, Integer supplierId, Integer sourceHwId, Integer destinationHwId, User user, String remarks) {

		String resultString = "导入成功";
		Date date = new Date();
		String fileName = getTaskName(date);
		ExcelHelper fileReader;
		try {
			fileReader = ExcelHelper.from(file);
			List<TaskItemBO> items = fileReader.unfill(TaskItemBO.class, 0);
			int i = 2;
			List<ExternalWhLog> externalWhLogs = new ArrayList<>();
			Set<Integer> materailTypeIdSet = new HashSet<>();
			if (items == null) {
				throw new OperationException("表格无有效数据或者表格格式不正确！");
			}
			for (TaskItemBO item : items) {
				if (item.getSerialNumber() != null && item.getSerialNumber() > 0) { // 只读取有序号的行数据
					ExternalWhLog externalWhLog = new ExternalWhLog();
					if (item.getNo() == null || item.getQuantity() == null || item.getNo().replaceAll(" ", "").equals("") || item.getQuantity().toString().replaceAll(" ", "").equals("")) {
						resultString = "导入失败，请检查表格第" + i + "行的料号或数量是否填写了准确信息！";
						return resultString;
					}

					if (sourceHwId != 0 && item.getQuantity() <= 0) {
						resultString = "导入失败，表格第" + i + "行的数量为" + item.getQuantity() + "，数量必须大于0,仅当发料地为UW无人仓时数量必须为负！";
						return resultString;
					}
					if (sourceHwId == 0 && item.getQuantity() > 0) {
						resultString = "导入失败，表格第" + i + "行的数量为" + item.getQuantity() + "，当发料地为UW无人仓时，类型为抵扣，数量必须为负！";
						return resultString;
					}

					// 根据料号找到对应的物料类型
					MaterialType mType = MaterialType.dao.findFirst(GET_MATERIAL_TYPE_BY_NO_SQL, item.getNo(), supplierId);
					// 判断物料类型表中是否存在对应的料号且未被禁用，若不存在，则将对应的任务记录删除掉，并提示操作员检查套料单、新增对应的物料类型
					if (mType == null) {
						resultString = "导入失败，料号为" + item.getNo() + "的物料没有记录在物料类型表中或已被禁用，或者是供应商与料号对应不上！";
						return resultString;
					}
					if (materailTypeIdSet.contains(mType.getId())) {
						resultString = "导入失败，料号为" + item.getNo() + "的物料表中存在重复项！";
						return resultString;
					}
					materailTypeIdSet.add(mType.getId());
					if (sourceHwId != 0 && sourceHwId != -1) {
						int storeNum = externalWhLogService.getEWhMaterialQuantity(mType.getId(), sourceHwId);
						if (storeNum < item.getQuantity()) {
							resultString = "导入失败，表格中料号为" + item.getNo() + "的数量大于发料仓库存";
							return resultString;
						}
					}
					externalWhLog.setMaterialTypeId(mType.getId());
					externalWhLog.setQuantity(item.getQuantity());
					if (sourceHwId == 0) {
						int storeNum = externalWhLogService.getEWhMaterialQuantity(mType.getId(), destinationHwId);
						if (storeNum + item.getQuantity() < 0) {
							externalWhLog.setQuantity(0 - storeNum);
						}
					}
					externalWhLog.setSourceWh(sourceHwId);
					externalWhLog.setDestination(destinationHwId);
					externalWhLog.setOperatior(user.getUid());
					externalWhLogs.add(externalWhLog);
					i++;
				} else {
					break;

				}
			}
			Task task = new Task();
			task.setCreateTime(date).setDestination(destinationHwId).setSupplier(supplierId).setFileName(fileName).setType(TaskType.EXTERNAL_IN_OUT).setState(TaskState.FINISHED).setRemarks(remarks);
			;
			if (sourceHwId == 0) {
				task.setType(TaskType.OUT);
			}

			task.save();
			for (ExternalWhLog externalWhLog : externalWhLogs) {
				externalWhLog.setTaskId(task.getId());
				externalWhLog.setTime(new Date());
				externalWhLog.save();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException(e.getMessage());
		} finally {
			file.delete();
		}
		return resultString;
	}


	public String importWastageTask(File file, Integer supplierId, Integer HwId, User user, String remarks) {

		String resultString = "导入成功";
		Date date = new Date();
		String fileName = getTaskName(date);
		ExcelHelper fileReader;
		try {
			fileReader = ExcelHelper.from(file);
			List<TaskItemBO> items = fileReader.unfill(TaskItemBO.class, 0);
			if (items == null) {
				throw new OperationException("表格无有效数据或者表格格式不正确！");
			}
			int i = 0;
			Set<Integer> materailTypeIdSet = new HashSet<>();
			List<ExternalWhLog> externalWhLogs = new ArrayList<>();
			for (TaskItemBO item : items) {
				if (item.getSerialNumber() != null && item.getSerialNumber() > 0) { // 只读取有序号的行数据
					ExternalWhLog externalWhLog = new ExternalWhLog();
					if (item.getNo() == null || item.getQuantity() == null || item.getNo().replaceAll(" ", "").equals("") || item.getQuantity().toString().replaceAll(" ", "").equals("")) {
						resultString = "导入失败，请检查表格第" + i + "行的料号或数量是否填写了准确信息！";
						return resultString;
					}

					if (item.getQuantity() <= 0) {
						resultString = "导入失败，表格第" + i + "行的数量为" + item.getQuantity() + "，数量必须大于0！";
						return resultString;
					}

					// 根据料号找到对应的物料类型
					MaterialType mType = MaterialType.dao.findFirst(GET_MATERIAL_TYPE_BY_NO_SQL, item.getNo(), supplierId);
					// 判断物料类型表中是否存在对应的料号且未被禁用，若不存在，则将对应的任务记录删除掉，并提示操作员检查套料单、新增对应的物料类型
					if (mType == null) {
						resultString = "导入失败，料号为" + item.getNo() + "的物料没有记录在物料类型表中或已被禁用，或者是供应商与料号对应不上！";
						return resultString;
					}
					if (materailTypeIdSet.contains(mType.getId())) {
						resultString = "导入失败，料号为" + item.getNo() + "的物料表中存在重复项！";
						return resultString;
					}
					materailTypeIdSet.add(mType.getId());

					if (HwId != 0) {
						int storeNum = externalWhLogService.getEWhMaterialQuantity(mType.getId(), HwId);
						if (storeNum < item.getQuantity()) {
							resultString = "导入失败，料号为" + item.getNo() + "的损耗物料数量大于库存";
							return resultString;
						}
					}
					externalWhLog.setMaterialTypeId(mType.getId());
					externalWhLog.setQuantity(item.getQuantity());
					externalWhLog.setSourceWh(HwId);
					externalWhLog.setDestination(HwId);
					externalWhLog.setOperatior(user.getUid());
					externalWhLogs.add(externalWhLog);
					i++;
				} else {
					break;

				}
			}
			Task task = new Task();
			task.setCreateTime(date).setDestination(HwId).setSupplier(supplierId).setFileName(fileName).setType(TaskType.WASTAGE).setState(TaskState.FINISHED).setRemarks(remarks);
			task.save();
			for (ExternalWhLog externalWhLog : externalWhLogs) {
				externalWhLog.setTaskId(task.getId());
				externalWhLog.setTime(new Date());
				externalWhLog.save();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException(e.getMessage());
		} finally {
			file.delete();
		}
		return resultString;
	}


	/**
	 * 添加某仓库，某物料的损耗记录
	 * @param materialTypeId
	 * @param HwId
	 * @param quantity
	 * @param user
	 * @return
	 */
	public String addWorstageLog(Integer materialTypeId, Integer HwId, Integer quantity, User user, String remarks) {

		String resultString = "操作成功";
		MaterialType mType = MaterialType.dao.findById(materialTypeId);
		if (mType == null) {
			resultString = "物料类型不存在，请确认系统中存在该物料类型！";
			return resultString;
		}
		Destination destination = Destination.dao.findById(HwId);
		if (destination == null) {
			resultString = "目的仓不存在，请确认系统中存在该物料类型！";
			return resultString;
		}
		if (HwId != 0) {
			int storeNum = externalWhLogService.getEWhMaterialQuantity(mType.getId(), HwId);
			if (storeNum < quantity) {
				resultString = "损耗物料数量不能大于库存";
				return resultString;
			}
		}
		Date date = new Date();
		String fileName = getTaskName(date);
		Task task = new Task();
		task.setDestination(HwId);
		task.setCreateTime(date);
		task.setState(TaskState.FINISHED);
		task.setSupplier(mType.getSupplier());
		task.setType(TaskType.WASTAGE);
		task.setFileName(fileName);
		task.setRemarks(remarks);
		task.save();

		ExternalWhLog externalWhLog = new ExternalWhLog();
		externalWhLog.setMaterialTypeId(mType.getId());
		externalWhLog.setSourceWh(HwId);
		externalWhLog.setDestination(HwId);
		externalWhLog.setQuantity(quantity);
		externalWhLog.setTaskId(task.getId());
		externalWhLog.setOperatior(user.getUid());
		externalWhLog.setTime(new Date());
		externalWhLog.save();
		return resultString;
	}


	public String getTaskName(Date date) {

		String fileName = "workorder_";
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = formatter.format(date);
		fileName = fileName + time;
		return fileName;
	}


	public String deleteExternalWhLog(Integer logId) {

		String resultString = "操作成功";
		ExternalWhLog externalWhLog = ExternalWhLog.dao.findById(logId);
		if (externalWhLog == null) {
			resultString = "删除失败，该日志不存在";
			return resultString;
		}
		externalWhLog.delete();
		return resultString;
	}


	public PagePaginate selectExternalWhInfo(Integer pageNo, Integer pageSize, Integer whId, Integer supplierId, String no, String ascBy, String descBy) {
		SqlPara sqlPara = new SqlPara();
		StringBuffer sql = new StringBuffer();
		List<ExternalWhInfoVO> externalWhInfoVOs = new ArrayList<>();
		sql.append(GET_EXTERIALWH_MATERIAL_TYPE_SQL);
		if (whId != null) {
			sql.append(" WHERE a.wh_id = ? ");
			sqlPara.addPara(whId);
		}
		if (supplierId != null) {
			if (whId == null) {
				sql.append(" WHERE  a.supplier_id = ? ");
				sqlPara.addPara(supplierId);
			} else {
				sql.append(" AND a.supplier_id = ? ");
				sqlPara.addPara(supplierId);
			}
		}
		if (no != null) {
			if (whId == null && supplierId == null) {
				sql.append(" WHERE  a.no like ? ");
				sqlPara.addPara("%" + no + "%");
			} else {
				sql.append(" AND  a.no like ? ");
				sqlPara.addPara("%" + no + "%");
			}
		}
		if ((ascBy == null || ascBy.equals("")) && (descBy == null || descBy.equals(""))) {
			sql.append(" ORDER BY a.wh_id,a.no ASC");
		} else {
			if (ascBy != null && !ascBy.equals("")) {
				sql.append(" ORDER BY " + ascBy.trim() + " ASC");
			}
			if (descBy != null && !descBy.equals("")) {
				sql.append(" ORDER BY " + descBy.trim() + " DESC");
			}
		}

		
		sqlPara.setSql(sql.toString());
		Page<Record> page = Db.paginate(pageNo, pageSize, sqlPara);
		List<Task> inventoryTasks = InventoryTaskService.me.getUnStartInventoryTask(supplierId, WarehouseType.REGULAR);
		for (Record record : page.getList()) {
			ExternalWhInfoVO externalWhInfoVO = new ExternalWhInfoVO();
			externalWhInfoVO.setNo(record.getStr("no"));
			externalWhInfoVO.setSpecification(record.getStr("specification"));
			externalWhInfoVO.setSupplierId(record.getInt("supplier_id"));
			externalWhInfoVO.setMaterialTypeId(record.getInt("material_type_id"));
			externalWhInfoVO.setSupplier(record.getStr("supplier_name"));
			externalWhInfoVO.setWhId(record.getInt("wh_id"));
			externalWhInfoVO.setWareHouse(record.getStr("wh_name"));
			if (inventoryTasks.size() > 0) {
				externalWhInfoVO.setInventoryBeforeQuantity(externalWhLogService.getEWhMaterialQuantity(record.getInt("material_type_id"), record.getInt("wh_id"), inventoryTasks.get(0).getCreateTime()));
				externalWhInfoVO.setInventoryAfterQuantity(externalWhLogService.getEWhMaterialQuantity(record.getInt("material_type_id"), record.getInt("wh_id")) - externalWhLogService.getEWhMaterialQuantity(record.getInt("material_type_id"), record.getInt("wh_id"), inventoryTasks.get(0).getCreateTime()));
			} else {
				externalWhInfoVO.setInventoryBeforeQuantity(externalWhLogService.getEWhMaterialQuantity(record.getInt("material_type_id"), record.getInt("wh_id")));
			}
			externalWhInfoVOs.add(externalWhInfoVO);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageNumber(page.getPageNumber());
		pagePaginate.setPageSize(page.getPageSize());
		pagePaginate.setTotalRow(page.getTotalRow());
		pagePaginate.setTotalPage(page.getTotalPage());
		pagePaginate.setList(externalWhInfoVOs);
		return pagePaginate;
	}


	// 导出物料仓库存报表
	public void exportEWhReport(Integer whId, Integer supplierId, String no, String fileName, OutputStream output) throws IOException {
		SqlPara sqlPara = new SqlPara();
		StringBuffer sql = new StringBuffer();
		sql.append(GET_EXTERIALWH_MATERIAL_TYPE_SQL);
		if (whId != null) {
			sql.append(" WHERE a.wh_id = ? ");
			sqlPara.addPara(whId);
		}
		if (supplierId != null) {
			if (whId == null) {
				sql.append(" WHERE  a.supplier_id = ? ");
				sqlPara.addPara(supplierId);
			} else {
				sql.append(" AND a.supplier_id = ? ");
				sqlPara.addPara(supplierId);
			}
		}
		if (no != null) {
			if (whId == null && supplierId == null) {
				sql.append(" WHERE  a.no like ? ");
				sqlPara.addPara("%" + no + "%");
			} else {
				sql.append(" AND  a.no like ? ");
				sqlPara.addPara("%" + no + "%");
			}
		}
		sql.append(" ORDER BY a.wh_id,a.no ASC");
		sqlPara.setSql(sql.toString());
		List<Record> records = Db.find(sqlPara);
		for (Record record : records) {
			record.set("quantity", externalWhLogService.getEWhMaterialQuantity(record.getInt("material_type_id"), record.getInt("wh_id")));
		}
		String[] field = null;
		String[] head = null;
		field = new String[] {"wh_name", "no", "specification", "supplier_name", "quantity"};
		head = new String[] {"仓库名", "料号", "规格", "供应商", "库存"};
		ExcelWritter writter = ExcelWritter.create(true);
		writter.fill(records, fileName, field, head);
		writter.write(output, true);
	}


	public List<ExternalWhInfoVO> selectExternalWhInfo(Integer whId, Integer supplierId, String no, Task task) {
		SqlPara sqlPara = new SqlPara();
		StringBuffer sql = new StringBuffer();
		List<ExternalWhInfoVO> externalWhInfoVOs = new ArrayList<>();
		sql.append(GET_EXTERIALWH_MATERIAL_TYPE_SQL);
		if (whId != null) {
			sql.append(" WHERE a.wh_id = ? ");
			sqlPara.addPara(whId);
		}
		if (supplierId != null) {
			if (whId == null) {
				sql.append(" WHERE  a.supplier_id = ? ");
				sqlPara.addPara(supplierId);
			} else {
				sql.append(" AND a.supplier_id = ? ");
				sqlPara.addPara(supplierId);
			}
		}
		if (no != null) {
			if (whId == null && supplierId == null) {
				sql.append(" WHERE  a.no like ? ");
				sqlPara.addPara("%" + no + "%");
			} else {
				sql.append(" AND  a.no like ? ");
				sqlPara.addPara("%" + no + "%");
			}
		}
		sql.append(" ORDER BY a.no ASC");
		sqlPara.setSql(sql.toString());
		List<Record> records = Db.find(sqlPara);
		for (Record record : records) {
			ExternalWhInfoVO externalWhInfoVO = new ExternalWhInfoVO();
			externalWhInfoVO.setNo(record.getStr("no"));
			externalWhInfoVO.setSpecification(record.getStr("specification"));
			externalWhInfoVO.setSupplierId(record.getInt("supplier_id"));
			externalWhInfoVO.setMaterialTypeId(record.getInt("material_type_id"));
			externalWhInfoVO.setSupplier(record.getStr("supplier_name"));
			externalWhInfoVO.setWhId(record.getInt("wh_id"));
			externalWhInfoVO.setWareHouse(record.getStr("wh_name"));
			externalWhInfoVO.setQuantity(externalWhLogService.getEWhMaterialQuantity(record.getInt("material_type_id"), record.getInt("wh_id"), task.getCreateTime()));
			externalWhInfoVO.setReturnQuantity(materialService.countMaterialReturnQuantity(record.getInt("material_type_id"), record.getInt("wh_id"), task.getStartTime()));
			externalWhInfoVOs.add(externalWhInfoVO);
		}
		return externalWhInfoVOs;
	}


	public PagePaginate selectEWhMaterialDetails(Integer pageNo, Integer pageSize, Integer materialTypeId, Integer whId) {

		SqlPara sqlPara = new SqlPara();
		sqlPara.setSql(GET_WEH_MATERIAL_DETAILS_SQL);
		sqlPara.addPara(materialTypeId);
		sqlPara.addPara(whId);
		sqlPara.addPara(materialTypeId);
		sqlPara.addPara(whId);
		Page<Record> page = Db.paginate(pageNo, pageSize, sqlPara);
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageNumber(page.getPageNumber());
		pagePaginate.setPageSize(page.getPageSize());
		pagePaginate.setTotalPage(page.getTotalPage());
		pagePaginate.setTotalRow(page.getTotalRow());
		List<EWhMaterialDetailVO> materialDetails = EWhMaterialDetailVO.fillList(page.getList());
		pagePaginate.setList(materialDetails);
		return pagePaginate;
	}

}
