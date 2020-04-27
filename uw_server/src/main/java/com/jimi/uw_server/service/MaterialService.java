package com.jimi.uw_server.service;

import com.jfinal.aop.Aop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jimi.uw_server.constant.MaterialStatus;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.constant.sql.MaterialSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.*;
import com.jimi.uw_server.model.bo.IOTaskRecord;
import com.jimi.uw_server.model.vo.MaterialInfoVO;
import com.jimi.uw_server.model.vo.MaterialVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ExcelWritter;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 物料业务层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class MaterialService extends SelectService {

	private static SelectService selectService = Aop.get(SelectService.class);
	
	private static final String GET_ALL_TASK_LOGS_BY_MATERIAL_TYPE_ID_SQL = "SELECT a.* FROM((SELECT task.id, task.file_name, task.type, task.destination AS destination, packing_list_item.material_type_id, packing_list_item.quantity AS plan_quantity, SUM(task_log.quantity) AS quantity, task_log.operator, task_log.time FROM task_log INNER JOIN packing_list_item INNER JOIN task ON packing_list_item.task_id = task.id AND task_log.packing_list_item_id = packing_list_item.id WHERE packing_list_item.material_type_id = ? GROUP BY task.id, packing_list_item.material_type_id) union ALL (SELECT task.id, task.file_name, task.type, NULL AS destination, sample_task_item.material_type_id, NULL AS plan_quantity, SUM(sample_out_record.quantity) AS quantity, sample_out_record.operator, sample_out_record.time FROM sample_out_record INNER JOIN sample_task_item INNER JOIN task ON task.id = sample_task_item.task_id AND sample_task_item.id = sample_out_record.sample_task_item_id WHERE sample_task_item.material_type_id = ? GROUP BY task.id, sample_task_item.material_type_id)) a";

	private static final String GET_MATERIAL_DETIAL_REPORT_SQL = "SELECT supplier.`name` AS supplier_name, material_type.id AS material_type_id, material_type.`no` AS `no`, material_type.specification AS specification, material_box.id AS box_id, material_box.area AS area, material_box. ROW AS X, material_box.col AS Y, material_box.height AS Z, material.id AS material_id, material.col AS col, material.`row` AS `row`, material.production_time AS production_time, material.remainder_quantity AS quantity FROM supplier INNER JOIN material_type INNER JOIN material_box INNER JOIN material ON supplier.id = material_type.supplier AND supplier.id = material_box.supplier AND material_type.id = material.type AND material.box = material_box.id WHERE material_type.type = ? AND material_type.supplier = ? AND material_type.enabled = 1 AND remainder_quantity > 0 ORDER BY material_type.id, material_box.id";

	private static final String COUNT_MATERIAL_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ? AND is_in_box = 1";

	private static final String COUNT_PRECIOUS_MATERIAL_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ? AND status = 0";

	private static final String COUNT_MATERIAL_INCLUDE_NOT_IN_BOX_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ?";

	private static final String COUNT_RETURN_QUANTITY = "SELECT SUM(quantity) as quantity FROM material_return_record WHERE material_type_id = ? AND wh_id = ? AND time <= ? AND enabled = 1";

	private static final String GET_OVERDUE_MATERIAL = "SELECT a.* FROM (SELECT DISTINCT material_type.type AS MaterialType_Type,material_type.enabled AS MaterialType_Enabled,material_type.id AS MaterialType_Id,material_type.supplier AS MaterialType_Supplier,material_type.no AS MaterialType_No,material_type.specification AS MaterialType_Specification,material_type.thickness AS MaterialType_Thickness,material_type.radius AS MaterialType_Radius,material_type.designator AS MaterialType_Designator, material_type.is_superable AS MaterialType_IsSuperable, supplier.name AS Supplier_Name,supplier.enabled AS Supplier_Enabled,supplier.id AS Supplier_Id,supplier.company_id AS Supplier_CompanyId FROM supplier INNER JOIN material_type INNER JOIN material ON material_type.id = material.type AND supplier.id = material_type.supplier WHERE material_type.enabled = 1 AND material.remainder_quantity > 0 AND material.store_time < ? AND material_type.type = ? AND supplier.company_id = ?) a ";

	public static MaterialService me = new MaterialService();
	
	// 统计物料类型信息
	public PagePaginate countMaterials(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null) {
			filter = filter.concat("#&#material_type.enabled=1#&#supplier.enabled=1");
		} else {
			filter = "material_type.enabled=1#&#supplier.enabled=1";
		}
		Page<Record> result = selectService.select(new String[] {"material_type", "supplier"}, new String[] {"material_type.supplier=supplier.id"}, pageNo, pageSize, ascBy, descBy, filter);
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		if (result.getList() != null && !result.getList().isEmpty()) {
			pagePaginate.setList(MaterialInfoVO.fillList(result.getList()));
		}else {
			pagePaginate.setList(Collections.emptyList());
		}

		return pagePaginate;
	}


	// 统计物料类型信息
	public Object getOverdueMaterial(Integer pageNo, Integer pageSize, Integer day, Integer type, Integer companyId) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 0 - day);
		Date date = calendar.getTime();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String dateString = df.format(date);
		SqlPara sqlPara = new SqlPara();
		sqlPara.setSql(GET_OVERDUE_MATERIAL);
		sqlPara.addPara(dateString);
		sqlPara.addPara(type);
		sqlPara.addPara(companyId);
		Page<Record> result = null;
		if (pageNo != null && pageSize != null) {
			result = Db.paginate(pageNo, pageSize, sqlPara);
		} else {
			result = Db.paginate(1, PropKit.use("properties.ini").getInt("defaultPageSize"), sqlPara);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		if (result.getList() != null && !result.getList().isEmpty()) {
			pagePaginate.setList(MaterialInfoVO.fillList(result.getList()));
		}else {
			pagePaginate.setList(Collections.emptyList());
		}
		
		return pagePaginate;
	}


	/**
	 * 
	 * <p>Description: 通过公司和料盒获取物料信息<p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月13日
	 */
	public PagePaginate getMaterialsByBox(Integer boxId, Integer pageNo, Integer pageSize) {
		if (pageNo == null && pageSize == null) {
			pageNo = 1;
			pageSize = PropKit.use("properties.ini").getInt("defaultPageSize");
		}
		MaterialBox materialBox = MaterialBox.dao.findById(boxId);
		if (materialBox == null) {
			throw new OperationException("料盒不存在！");
		}
		Supplier supplier = Supplier.dao.findById(materialBox.getSupplier());
		Company company = Company.dao.findById(materialBox.getCompanyId());
		if (!supplier.getCompanyId().equals(company.getId())) {
			throw new OperationException("料盒所属客户的公司与料盒所属公司冲突！");
		}
		Page<Record> page = Db.paginate(pageNo, pageSize, MaterialSQL.GET_ENTITIES_SELECT_SQL, MaterialSQL.GET_ENTITIES_BY_BOX_EXCEPT_SELECT_SQL, boxId, supplier.getId());
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageNumber(page.getPageNumber());
		pagePaginate.setPageSize(page.getPageSize());
		pagePaginate.setTotalPage(page.getTotalPage());
		pagePaginate.setTotalRow(page.getTotalRow());
		if (page.getList() != null && !page.getList().isEmpty()) {
			pagePaginate.setList(MaterialVO.fillRegualrMaterialVOList(page.getList(), company, supplier));
		}else {
			pagePaginate.setList(Collections.emptyList());
		}
		return pagePaginate;
	}
	
	
	/**
	 * 
	 * <p>Description: 通过公司和物料类型获取物料信息<p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月13日
	 */
	public PagePaginate getMaterialsByMaterialType(Integer materialTypeId, Integer pageNo, Integer pageSize) {
		if (pageNo == null && pageSize == null) {
			pageNo = 1;
			pageSize = PropKit.use("properties.ini").getInt("defaultPageSize");
		}
		MaterialType materialType = MaterialType.dao.findById(materialTypeId);
		if (materialType == null) {
			throw new OperationException("物料类型不存在！");
		}
		Supplier supplier = Supplier.dao.findById(materialType.getSupplier());
		Company company = Company.dao.findById(supplier.getCompanyId());
		PagePaginate pagePaginate = new PagePaginate();
		Page<Record> page = null;
		if (materialType.getType().equals(WarehouseType.REGULAR.getId())) {
			page = Db.paginate(pageNo, pageSize, MaterialSQL.GET_ENTITIES_SELECT_SQL, MaterialSQL.GET_ENTITIES_BY_TYPE_EXCEPT_SELECT_SQL, materialTypeId, materialType.getSupplier());
			if (page.getList() != null && !page.getList().isEmpty()) {
				pagePaginate.setList(MaterialVO.fillRegualrMaterialVOList(page.getList(), company, supplier));
			}else {
				pagePaginate.setList(Collections.emptyList());
			}
		}else {
			SqlPara sqlPara = new SqlPara();
			sqlPara.setSql(MaterialSQL.GET_PRECIOUS_MATERIAL_ENTITIES_SQL);
			sqlPara.addPara(materialTypeId);
			sqlPara.addPara(MaterialStatus.NORMAL);
			page = Db.paginate(pageNo, pageSize, sqlPara);
			if (page.getList() != null && !page.getList().isEmpty()) {
				pagePaginate.setList(MaterialVO.fillPreciousMaterialVOList(page.getList(), company, supplier));
			}else {
				pagePaginate.setList(Collections.emptyList());
			}
		}
		pagePaginate.setPageNumber(page.getPageNumber());
		pagePaginate.setPageSize(page.getPageSize());
		pagePaginate.setTotalPage(page.getTotalPage());
		pagePaginate.setTotalRow(page.getTotalRow());
		
		return pagePaginate;
	}
	
	
	// 获取物料出入库记录
	public Object getMaterialIOTaskRecords(Integer type, Integer materialTypeId, Integer destination, String startTime, String endTime, Integer pageNo, Integer pageSize) {
		Page<Record> page = getRecordItemList(type, materialTypeId, destination, startTime, endTime, pageNo, pageSize);
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(page.getPageSize());
		pagePaginate.setPageNumber(page.getPageNumber());
		pagePaginate.setTotalRow(page.getTotalRow());
		pagePaginate.setTotalPage(page.getTotalPage());
		List<IOTaskRecord> recordItems = new ArrayList<>();
		for (Record record : page.getList()) {
			IOTaskRecord recordItem = new IOTaskRecord(record.getInt("material_type_id"), record.getInt("plan_quantity"), record.getStr("file_name"), record.getInt("type"), record.getInt("quantity"), record.getStr("operator"), record.getDate("time"));
			recordItems.add(recordItem);
		}
		pagePaginate.setList(recordItems);
		return pagePaginate;
	}


	// 获取物料出入库记录子方法
	public Page<Record> getRecordItemList(Integer type, Integer materialTypeId, Integer destination, String startTime, String endTime, Integer pageNo, Integer pageSize) {
		SqlPara sqlPara = new SqlPara();
		StringBuffer sql = new StringBuffer(GET_ALL_TASK_LOGS_BY_MATERIAL_TYPE_ID_SQL);
		sqlPara.addPara(materialTypeId);
		sqlPara.addPara(materialTypeId);
		if (type == 0) {
			if (destination == null) {
				sql.append(" WHERE (a.type = 1 OR a.type = 7) ");
			} else {
				sql.append(" WHERE (destination = ? AND a.type = 1) ");
				sqlPara.addPara(destination);
			}
			if (startTime != null && endTime != null) {
				sql.append(" AND (a.time BETWEEN ? AND ?) ");
				sqlPara.addPara(startTime);
				sqlPara.addPara(endTime);
			}
			sql.append(" ORDER BY a.time DESC ");
		} else if (type == 1) {
			sql.append(" WHERE (a.type = 0 OR a.type = 4) ");
			if (startTime != null && endTime != null) {
				sql.append(" AND (a.time BETWEEN ? AND ?) ");
				sqlPara.addPara(startTime);
				sqlPara.addPara(endTime);
			}
			sql.append(" ORDER BY a.time DESC ");
		} else {
			if (startTime != null && endTime != null) {
				sql.append(" WHERE (a.time BETWEEN ? AND ?) ");
				sqlPara.addPara(startTime);
				sqlPara.addPara(endTime);
			}
			sql.append(" ORDER BY a.time DESC ");
		}
		sqlPara.setSql(sql.toString());
		Page<Record> page = Db.paginate(pageNo, pageSize, sqlPara);
		return page;
	}


	// 导出物料报表
	public void exportMaterialReport(Supplier supplier, String fileName, OutputStream output, Integer warehouseType) throws IOException {
		String[] field = null;
		String[] head = null;
		if (warehouseType.equals(WarehouseType.REGULAR.getId())) {
			List<Record> materialRecord = Db.find(MaterialSQL.GET_REGUALR_MATERIAL_REPORT_SQL, warehouseType, supplier.getCompanyId(), supplier.getId());
			field = new String[] {"id", "no", "specification",  "quantity"};
			head = new String[] {"物料类型号", "料号", "规格号", "物料数量"};
			ExcelWritter writter = ExcelWritter.create(true);
			writter.fill(materialRecord, fileName, field, head);
			writter.write(output, true);
		} else {
			List<Record> materialRecord = Db.find(MaterialSQL.GET_PRECIOUS_MATERIAL_REPORT_SQL, warehouseType, supplier.getCompanyId(), supplier.getId());
			field = new String[] {"id", "no", "specification", "designator", "quantity"};
			head = new String[] {"物料类型号", "料号", "规格号", "位号", "物料数量"};
			ExcelWritter writter = ExcelWritter.create(true);
			writter.fill(materialRecord, fileName, field, head);
			writter.write(output, true);
		}

	}


	// 导出物料详细报表
	public void exportMaterialDetialsReport(Integer supplier, OutputStream output, Integer type) throws IOException {
		List<Record> materialRecord = Db.find(GET_MATERIAL_DETIAL_REPORT_SQL, type, supplier);
		String[] field = null;
		String[] head = null;
		field = new String[] {"supplier_name", "no", "material_type_id", "specification", "material_id", "production_time", "quantity", "area", "box_id", "X", "Y", "Z", "row", "col"};
		head = new String[] {"客户", "料号", "物料类型", "规格", "料盘唯一码", "生产日期", "数量", "料盒所在区域", "料盒号", "X", "Y", "Z", "盒内行号", "盒内列号"};
		ExcelWritter writter = ExcelWritter.create(true);
		writter.fill(materialRecord, "物料库存表", field, head);
		writter.write(output, true);
	}


	/**
	 * 根据物料类型号计算物料库存数并返回，不计算不在料盒内的物料
	 */
	public Integer countAndReturnRemainderQuantityByMaterialTypeId(Integer materialTypeId) {
		Material material = Material.dao.findFirst(COUNT_MATERIAL_SQL, materialTypeId);
		Integer remainderQuantity;
		if (material.get("quantity") == null) {
			remainderQuantity = 0;
		} else {
			remainderQuantity = Integer.parseInt(material.get("quantity").toString());
		}
		return remainderQuantity;
	}


	/**
	 * 根据物料类型号计算物料库存数并返回，有计算不在料盒内的物料，在 TaskPool类 的 sendIOCmds方法 中用得上 
	 */
	public Integer countMaterialIncludeNotInBox(Integer materialTypeId) {
		Material material = Material.dao.findFirst(COUNT_MATERIAL_INCLUDE_NOT_IN_BOX_SQL, materialTypeId);
		Integer remainderQuantity;
		if (material.get("quantity") == null) {
			remainderQuantity = 0;
		} else {
			remainderQuantity = Integer.parseInt(material.get("quantity").toString());
		}
		return remainderQuantity;
	}


	/**
	 * 根据物料类型号计算物料库存数并返回，不计算不在料盒内的物料
	 */
	public Integer countPreciousQuantityByMaterialTypeId(Integer materialTypeId) {
		Material material = Material.dao.findFirst(COUNT_PRECIOUS_MATERIAL_SQL, materialTypeId);
		Integer remainderQuantity;
		if (material.get("quantity") == null) {
			remainderQuantity = 0;
		} else {
			remainderQuantity = Integer.parseInt(material.get("quantity").toString());
		}
		return remainderQuantity;
	}


	public Integer countMaterialReturnQuantity(Integer material_type_id, Integer wh_id, Date time) {
		MaterialReturnRecord materialReturnRecord = MaterialReturnRecord.dao.findFirst(COUNT_RETURN_QUANTITY, material_type_id, wh_id, time);
		if (materialReturnRecord.getQuantity() == null) {
			return 0;
		}
		return materialReturnRecord.getQuantity();
	}

}