package com.jimi.uw_server.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;

import com.jimi.uw_server.constant.BoxState;
import com.jimi.uw_server.constant.MaterialStatus;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.constant.sql.MaterialTypeSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.BoxType;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialReturnRecord;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.bo.RegularMaterialTypeItemBO;
import com.jimi.uw_server.model.bo.PreciousMaterialTypeItemBO;
import com.jimi.uw_server.model.bo.RecordItem;
import com.jimi.uw_server.model.vo.BoxTypeVO;
import com.jimi.uw_server.model.vo.MaterialBoxVO;
import com.jimi.uw_server.model.vo.MaterialTypeVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ExcelHelper;
import com.jimi.uw_server.util.ExcelWritter;


/**
 * 物料业务层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class MaterialService extends SelectService {

	private static SelectService selectService = Aop.get(SelectService.class);

	private static final Object IMPORT_FILE_LOCK = new Object();

	private static final String GET_MATERIAL_TYPE_IN_PROCESS_SQL = "SELECT * FROM packing_list_item WHERE material_type_id = ? AND task_id IN (SELECT id FROM task WHERE state <= 2)";

	private static final String COUNT_MATERIAL_BY_TYPE_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ?";

	private static final String COUNT_MATERIAL_BY_BOX_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE box = ?";

	private static final String GET_ENTITIES_SELECT_SQL = "SELECT material.id AS id, material.type AS type, material.box AS box, material_box.area AS boxArea, material.row AS row, material.col AS col, material.remainder_quantity AS remainderQuantity, material.production_time AS productionTimeString , material_type.no AS materialNo, material.store_time AS store_time ";

	private static final String GET_ENTITIES_BY_TYPE_EXCEPT_SELECT_SQL = "FROM material join material_box join  material_type ON material_type.id = material.type AND material.box = material_box.id WHERE material.type = ? AND material.remainder_quantity > 0";

	private static final String GET_ENTITIES_BY_BOX_EXCEPT_SELECT_SQL = "FROM material join material_box join  material_type ON material_type.id = material.type AND material.box = material_box.id WHERE material.box = ? AND material.remainder_quantity > 0";

	private static final String GET_ENTITIES_BY_TYPE_AND_BOX_EXCEPT_SELECT_SQL = "FROM material join material_box join  material_type ON material_type.id = material.type AND material.box = material_box.id WHERE material.type = ? and material.box = ? AND material.remainder_quantity > 0";

	private static final String GET_ENABLED_MATERIAL_BOX_BY_POSITION_SQL = "SELECT * FROM material_box WHERE area = ? AND row = ? AND col = ? AND height = ? AND enabled = 1";

	private static final String GET_ALL_TASK_LOGS_BY_MATERIAL_TYPE_ID_SQL = "SELECT a.* FROM((SELECT task.id, task.file_name, task.type, task.destination AS destination, packing_list_item.material_type_id, packing_list_item.quantity AS plan_quantity, SUM(task_log.quantity) AS quantity, task_log.operator, task_log.time FROM task_log INNER JOIN packing_list_item INNER JOIN task ON packing_list_item.task_id = task.id AND task_log.packing_list_item_id = packing_list_item.id WHERE packing_list_item.material_type_id = ? GROUP BY task.id, packing_list_item.material_type_id) union ALL (SELECT task.id, task.file_name, task.type, NULL AS destination, sample_task_item.material_type_id, NULL AS plan_quantity, SUM(sample_out_record.quantity) AS quantity, sample_out_record.operator, sample_out_record.time FROM sample_out_record INNER JOIN sample_task_item INNER JOIN task ON task.id = sample_task_item.task_id AND sample_task_item.id = sample_out_record.sample_task_item_id WHERE sample_task_item.material_type_id = ? GROUP BY task.id, sample_task_item.material_type_id)) a";

	private static final String GET_MATERIAL_REPORT_SQL = "SELECT material_type.id as id, material_type.no as no, material_type.specification as specification, material_box.id AS box, material_box.row as row, material_box.col as col, material_box.height as height, SUM(material.remainder_quantity) AS quantity, material_type.designator as designator FROM (material_type LEFT JOIN material ON material_type.id = material.type) LEFT JOIN material_box ON material.box = material_box.id WHERE material_type.supplier = ? AND material_type.type = ? AND material_type.enabled = 1 GROUP BY material.box, material.type, material_type.id ORDER BY material_type.id, material_box.id";

	private static final String GET_MATERIAL_DETIAL_REPORT_SQL = "SELECT supplier.`name` AS supplier_name, material_type.id AS material_type_id, material_type.`no` AS `no`, material_type.specification AS specification, material_box.id AS box_id, material_box.area AS area, material_box. ROW AS X, material_box.col AS Y, material_box.height AS Z, material.id AS material_id, material.col AS col, material.`row` AS `row`, material.production_time AS production_time, material.remainder_quantity AS quantity FROM supplier INNER JOIN material_type INNER JOIN material_box INNER JOIN material ON supplier.id = material_type.supplier AND supplier.id = material_box.supplier AND material_type.id = material.type AND material.box = material_box.id WHERE material_type.type = ? AND material_type.supplier = ? AND material_type.enabled = 1 AND remainder_quantity > 0 ORDER BY material_type.id, material_box.id";

	private static final String GET_ENABLED_BOX_TYPE_BY_CELL_WIDTH_SQL = "SELECT * FROM box_type WHERE cell_width = ? AND enabled = 1";

	private static final String GET_ENABLED_MATERIAL_BOX_BY_TYPE_SQL = "SELECT * FROM material_box WHERE type = ? AND enabled = 1";

	private static final String JUDGE_MATERIAL_BOX_IS_EMPTY_SQL = "SELECT * FROM material_box WHERE enabled = 1";

	private static final String COUNT_MATERIAL_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ? AND is_in_box = 1";

	private static final String COUNT_PRECIOUS_MATERIAL_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ? AND status = 0";

	private static final String COUNT_MATERIAL_INCLUDE_NOT_IN_BOX_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ?";

	private static final String COUNT_RETURN_QUANTITY = "SELECT SUM(quantity) as quantity FROM material_return_record WHERE material_type_id = ? AND wh_id = ? AND time <= ? AND enabled = 1";

	private static final String GET_OVERDUE_MATERIAL = "SELECT a.* FROM (SELECT DISTINCT material_type.* FROM material_type INNER JOIN material ON material_type.id = material.type WHERE material_type.enabled = 1 AND material.remainder_quantity > 0 AND material.store_time < ? AND material_type.type = ?) a ";

	private static final String GET_PRECIOUS_MATERIAL_ENTITIES = "SELECT material.id AS id, material.type AS type, material_type.designator AS designator, material.remainder_quantity AS remainderQuantity, material.production_time AS productionTimeString , material_type.no AS materialNo, material.store_time AS store_time FROM material INNER JOIN material_type ON material.type = material_type.id WHERE material.type = ? AND material.remainder_quantity > 0 AND material.status = ?";

	public static MaterialService me = new MaterialService();


	// 统计物料类型信息
	public Object count(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null) {
			filter = filter.concat("#&#enabled=1");
		} else {
			filter = "enabled=1";
		}
		Page<Record> result = selectService.select(new String[] {"material_type"}, null, pageNo, pageSize, ascBy, descBy, filter);
		List<MaterialTypeVO> materialTypeVOs = new ArrayList<MaterialTypeVO>();
		for (Record res : result.getList()) {
			MaterialTypeVO m = new MaterialTypeVO(res.get("id"), res.get("no"), res.get("specification"), res.get("supplier"), res.get("thickness"), res.get("radius"), res.get("enabled"), res.getStr("designator"), res.getInt("type"));
			materialTypeVOs.add(m);
		}

		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(materialTypeVOs);

		return pagePaginate;
	}


	// 统计物料类型信息
	public Object getOverdueMaterial(Integer pageNo, Integer pageSize, Integer day, Integer type) {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 0 - day);
		Date date = calendar.getTime();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String dateString = df.format(date);
		SqlPara sqlPara = new SqlPara();
		sqlPara.setSql(GET_OVERDUE_MATERIAL);
		sqlPara.addPara(dateString);
		sqlPara.addPara(type);
		Page<Record> result = null;
		if (pageNo != null && pageSize != null) {
			result = Db.paginate(pageNo, pageSize, sqlPara);
		} else {
			result = Db.paginate(1, PropKit.use("properties.ini").getInt("defaultPageSize"), sqlPara);
		}

		List<MaterialTypeVO> materialTypeVOs = new ArrayList<MaterialTypeVO>();
		for (Record res : result.getList()) {
			MaterialTypeVO m = new MaterialTypeVO(res.get("id"), res.get("no"), res.get("specification"), res.get("supplier"), res.get("thickness"), res.get("radius"), res.get("enabled"), res.getStr("designator"), res.getInt("type"));
			materialTypeVOs.add(m);
		}

		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(materialTypeVOs);

		return pagePaginate;
	}


	// 获取物料详情
	public Page<Record> getEntities(Integer materialTypeId, Integer box, Integer pageNo, Integer pageSize) {
		Page<Record> materialEntities = new Page<Record>();
		if (pageNo == null && pageSize == null) {
			pageNo = 1;
			pageSize = PropKit.use("properties.ini").getInt("defaultPageSize");
		}
		MaterialType materialType = MaterialType.dao.findById(materialTypeId);
		if (materialType == null || materialType.getType().equals(WarehouseType.REGULAR)) {
			if (materialTypeId != null && box == null) {
				materialEntities = Db.paginate(pageNo, pageSize, GET_ENTITIES_SELECT_SQL, GET_ENTITIES_BY_TYPE_EXCEPT_SELECT_SQL, materialTypeId);
			} else if (materialTypeId == null && box != null) {
				materialEntities = Db.paginate(pageNo, pageSize, GET_ENTITIES_SELECT_SQL, GET_ENTITIES_BY_BOX_EXCEPT_SELECT_SQL, box);
			} else if (materialTypeId != null && box != null) {
				materialEntities = Db.paginate(pageNo, pageSize, GET_ENTITIES_SELECT_SQL, GET_ENTITIES_BY_TYPE_AND_BOX_EXCEPT_SELECT_SQL, materialTypeId, box);
			}
			for (Record record : materialEntities.getList()) {
				if (record.getInt("col") != -1 && record.getInt("row") != -1) {
					record.set("col", record.getInt("col") + 1);
					record.set("row", record.getInt("row") + 1);
				}
			}
		} else if (materialType.getType().equals(WarehouseType.PRECIOUS)) {
			SqlPara sqlPara = new SqlPara();
			sqlPara.setSql(GET_PRECIOUS_MATERIAL_ENTITIES);
			sqlPara.addPara(materialTypeId);
			sqlPara.addPara(MaterialStatus.NORMAL);
			materialEntities = Db.paginate(pageNo, pageSize, sqlPara);
		}
		return materialEntities;
	}


	// 新增物料类型
	public String addMaterialType(Integer type, String no, String specification, Integer supplierId, Integer thickness, Integer radius, String designator) {
		String resultString = "添加成功！";
		Supplier s = Supplier.dao.findById(supplierId);
		if (s == null) {
			throw new OperationException("供应商不存在");
		}
		MaterialType materialType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_SUPPLIER_AND_NAME, no.toUpperCase(), supplierId);
		if (materialType != null) {
			if (materialType.getType().equals(type)) {
				resultString = "本仓库该物料类型已存在，请勿重复添加！";
			} else {
				resultString = "其他仓库已存在相同料号的物料类型，请勿重复添加！";
			}
			return resultString;
		}
		if (no.contains("!") || no.contains("$")) {
			resultString = "请勿往料号中添加非法字符，如“!”或“$”！";
			return resultString;
		}
		if (type.equals(WarehouseType.PRECIOUS)) {
			MaterialType tempMaterialType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_DESIGNATOR_AND_TYPE_SQL, supplierId, designator, WarehouseType.PRECIOUS);
			if (tempMaterialType != null) {
				resultString = "存在物料的位号与新添加物料的位号相同，添加失败！";
				return resultString;
			}
		}

		materialType = new MaterialType();
		materialType.setNo(no.toUpperCase());
		materialType.setSpecification(specification);
		materialType.setSupplier(supplierId);
		materialType.setThickness(thickness);
		materialType.setRadius(radius);
		materialType.setEnabled(true);
		materialType.setType(type);
		if (type.equals(WarehouseType.REGULAR)) {
			materialType.setDesignator("无");
		} else {
			materialType.setDesignator(designator);
		}
		materialType.save();
		return resultString;
	}


	// 更新物料类型
	public String updateMaterialType(Integer id, Boolean enabled, Integer thickness, Integer radius, String designator) {
		String resultString = "更新成功！";
		if (!enabled) {
			Material m = Material.dao.findFirst(COUNT_MATERIAL_BY_TYPE_SQL, id);
			if (m.get("quantity") != null) {
				Integer quantity = Integer.parseInt(m.get("quantity").toString());
				if (quantity > 0) {
					resultString = "该物料库存数量大于0，禁止删除！";
					return resultString;
				}
			}
			if (PackingListItem.dao.findFirst(GET_MATERIAL_TYPE_IN_PROCESS_SQL, id) != null) {
				resultString = "当前有某个尚未完成的任务已经绑定了该物料，禁止删除该物料！";
				return resultString;
			}
		}
		MaterialType materialType = MaterialType.dao.findById(id);
		materialType.setThickness(thickness);
		materialType.setRadius(radius);
		materialType.setEnabled(enabled);
		if (materialType.getType().equals(WarehouseType.PRECIOUS)) {
			MaterialType tempMaterialType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_DESIGNATOR_AND_TYPE_SQL, materialType.getSupplier(), designator.trim(), WarehouseType.PRECIOUS);
			if (tempMaterialType != null && !tempMaterialType.getId().equals(id)) {
				resultString = "存在其他物料的位号与新添加物料的位号相同，添加失败！";
				return resultString;
			}
			materialType.setDesignator(designator);
		}
		materialType.update();
		return resultString;
	}


	// 更新物料类型
	public String deleteByIds(String filter) {
		StringBuffer resultString = new StringBuffer();
		String[] ids = filter.split(",");
		List<String> remaindIds = new ArrayList<>();
		List<String> bindIds = new ArrayList<>();
		Boolean flag = true;
		for (String id : ids) {
			Material m = Material.dao.findFirst(COUNT_MATERIAL_BY_TYPE_SQL, Integer.valueOf(id));
			if (m.get("quantity") != null) {
				Integer quantity = Integer.parseInt(m.get("quantity").toString());
				if (quantity > 0) {
					remaindIds.add(id);
					flag = false;
				}
			}
			if (PackingListItem.dao.findFirst(GET_MATERIAL_TYPE_IN_PROCESS_SQL, Integer.valueOf(id)) != null) {
				bindIds.add(id);
				flag = false;
			}
			if (flag.equals(true)) {
				MaterialType materialType = MaterialType.dao.findById(id);
				materialType.setEnabled(false);
				materialType.update();
			}
		}

		if (remaindIds.size() == 0 && bindIds.size() == 0) {
			return "删除成功";
		} else {
			if (remaindIds.size() > 0) {
				resultString.append("料号ID 为  [");
				for (String remaindId : remaindIds) {
					resultString.append(remaindId + ",");
				}
				resultString.deleteCharAt(resultString.lastIndexOf(","));
				resultString.append("] 的物料存在库存，无法删除!");
			}
			if (bindIds.size() > 0) {
				if (resultString.length() != 0) {
					resultString.append("|");
				}
				resultString.append("料号ID 为  [");
				for (String bindId : bindIds) {
					resultString.append(bindId + ",");
				}
				resultString.deleteCharAt(resultString.lastIndexOf(","));
				resultString.append("] 的物料以绑定了任务，无法删除!");
			}
			return resultString.toString();
		}

	}


	// 获取料盒信息
	public Object getBoxes(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null) {
			filter = filter.concat("#&#material_box.enabled=1");
		} else {
			filter = "material_box.enabled=1";
		}
		Page<Record> result = selectService.select(new String[] {"material_box", "supplier"}, new String[] {"material_box.supplier=supplier.id"}, pageNo, pageSize, ascBy, descBy, filter);
		List<MaterialBoxVO> materialBoxVOs = new ArrayList<MaterialBoxVO>();
		for (Record res : result.getList()) {
			MaterialBoxVO m = new MaterialBoxVO(res.getInt("MaterialBox_Id"), res.getStr("MaterialBox_Area"), res.getInt("MaterialBox_Row"), res.getInt("MaterialBox_Col"), res.getInt("MaterialBox_Height"), res.getBoolean("MaterialBox_IsOnShelf"), res.getInt("MaterialBox_Type"), res.getStr("Supplier_Name"));
			materialBoxVOs.add(m);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(materialBoxVOs);

		return pagePaginate;
	}


	// 手动添加料盒
	public String addBox(String area, Integer row, Integer col, Integer height, Integer supplierId, Boolean isStandard) {
		String resultString = "添加成功！";
		if (MaterialType.dao.find(GET_ENABLED_MATERIAL_BOX_BY_POSITION_SQL, area, row, col, height).size() != 0) {
			resultString = "该位置已有料盒存在，请不要在该位置添加料盒！";
			return resultString;
		}
		Supplier supplier = Supplier.dao.findById(supplierId);
		if (supplier == null) {
			resultString = "供应商不存在！";
			return resultString;
		}
		MaterialBox materialBox = new MaterialBox();
		if (isStandard) {
			materialBox.setType(1);
		} else {
			materialBox.setType(2);
		}
		materialBox.setArea(area);
		materialBox.setRow(row);
		materialBox.setCol(col);
		materialBox.setHeight(height);
		materialBox.setIsOnShelf(true);
		materialBox.setEnabled(true);
		materialBox.setSupplier(supplierId);
		materialBox.setStatus(BoxState.EMPTY);
		materialBox.setUpdateTime(new Date());
		materialBox.save();
		return resultString;
	}


	// 更新料盒在架/不在架状态
	public Boolean updateBox(Integer id, Boolean isOnShelf) {
		MaterialBox materialBox = MaterialBox.dao.findById(id);
		materialBox.setIsOnShelf(isOnShelf);
		return materialBox.update();
	}


	// 删除料盒
	public String deleteBox(Integer id, Boolean enabled) {
		String resultString = "更新成功！";
		if (!enabled) {
			Material m = Material.dao.findFirst(COUNT_MATERIAL_BY_BOX_SQL, id);
			if (m.get("quantity") != null) {
				Integer quantity = Integer.parseInt(m.get("quantity").toString());
				if (quantity > 0) {
					resultString = "该料盒中还有物料，禁止删除！";
					return resultString;
				}
			}
		}
		MaterialBox materialBox = MaterialBox.dao.findById(id);
		materialBox.setEnabled(enabled);
		materialBox.update();
		return resultString;
	}


	// 获取物料出入库记录
	public Object getMaterialRecords(Integer type, Integer materialTypeId, Integer destination, String startTime, String endTime, Integer pageNo, Integer pageSize) {
		Page<Record> page = getRecordItemList(type, materialTypeId, destination, startTime, endTime, pageNo, pageSize);
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(page.getPageSize());
		pagePaginate.setPageNumber(page.getPageNumber());
		pagePaginate.setTotalRow(page.getTotalRow());
		pagePaginate.setTotalPage(page.getTotalPage());
		List<RecordItem> recordItems = new ArrayList<>();
		for (Record record : page.getList()) {
			RecordItem recordItem = new RecordItem(record.getInt("material_type_id"), record.getInt("plan_quantity"), record.getStr("file_name"), record.getInt("type"), record.getInt("quantity"), record.getStr("operator"), record.getDate("time"));
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
			sql.append(" ORDER BY a.time ");
		} else if (type == 1) {
			sql.append(" WHERE (a.type = 0 OR a.type = 4) ");
			if (startTime != null && endTime != null) {
				sql.append(" AND (a.time BETWEEN ? AND ?) ");
				sqlPara.addPara(startTime);
				sqlPara.addPara(endTime);
			}
			sql.append(" ORDER BY a.time ");
		} else {
			if (startTime != null && endTime != null) {
				sql.append(" WHERE (a.time BETWEEN ? AND ?) ");
				sqlPara.addPara(startTime);
				sqlPara.addPara(endTime);
			}
			sql.append(" ORDER BY a.time ");
		}
		sqlPara.setSql(sql.toString());
		Page<Record> page = Db.paginate(pageNo, pageSize, sqlPara);
		return page;
	}


	// 导出物料报表
	public void exportMaterialReport(Integer supplier, String fileName, OutputStream output, Integer type) throws IOException {
		List<Record> materialRecord = Db.find(GET_MATERIAL_REPORT_SQL, supplier, type);
		String[] field = null;
		String[] head = null;
		if (type.equals(WarehouseType.REGULAR)) {
			field = new String[] {"id", "no", "specification", "box", "row", "col", "height", "quantity"};
			head = new String[] {"物料类型号", "料号", "规格号", "盒号", "行号", "列号", "高度", "盒内物料数量"};
			ExcelWritter writter = ExcelWritter.create(true);
			writter.fill(materialRecord, fileName, field, head);
			writter.write(output, true);
		} else {
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
		head = new String[] {"供应商", "料号", "物料类型", "规格", "料盘唯一码", "生产日期", "数量", "料盒所在区域", "料盒号", "X", "Y", "Z", "盒内行号", "盒内列号"};
		ExcelWritter writter = ExcelWritter.create(true);
		writter.fill(materialRecord, "物料库存表", field, head);
		writter.write(output, true);
	}


	// 获取料盒类型
	public Object getBoxTypes(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null) {
			filter = filter.concat("#&#enabled=1");
		} else {
			filter = "enabled=1";
		}
		Page<Record> result = selectService.select(new String[] {"box_type"}, null, pageNo, pageSize, ascBy, descBy, filter);
		List<BoxTypeVO> boxTypeVOs = new ArrayList<BoxTypeVO>();
		for (Record res : result.getList()) {
			BoxTypeVO b = new BoxTypeVO(res.get("id"), res.get("cell_width"), res.get("cell_rows"), res.get("cell_cols"), res.get("enabled"));
			boxTypeVOs.add(b);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(boxTypeVOs);

		return pagePaginate;
	}


	// 添加料盒类型
	public String addBoxType(Integer cellWidth, Integer cellRows, Integer cellCols) {
		String resultString = "添加成功！";
		if (BoxType.dao.find(GET_ENABLED_BOX_TYPE_BY_CELL_WIDTH_SQL, cellWidth).size() != 0) {
			resultString = "该料盒类型已存在，请勿重复添加！";
			return resultString;
		}
		BoxType boxType = new BoxType();
		boxType.setCellWidth(cellWidth);
		boxType.setCellRows(cellRows);
		boxType.setCellCols(cellCols);
		boxType.setEnabled(true);
		boxType.save();
		return resultString;
	}


	// 删除料盒类型
	public String deleteBoxType(Integer id, Boolean enabled) {
		String resultString = "更新成功！";
		if (!enabled) {
			MaterialBox mb = MaterialBox.dao.findFirst(GET_ENABLED_MATERIAL_BOX_BY_TYPE_SQL, id);
			if (mb != null) {
				resultString = "该料盒类型已绑定了某个料盒，请先删掉相关的料盒进行删除操作！";
				return resultString;
			}
		}
		BoxType boxType = BoxType.dao.findById(id);
		boxType.setEnabled(enabled);
		boxType.update();
		return resultString;
	}


	// 导入物料类型表
	public String importRegularMaterialTypeFile(String fileName, File file, Integer supplierId) throws Exception {
		String resultString = "导入成功！";
		// 如果文件格式不对，则提示检查文件格式
		if (!(fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
			// 清空upload目录下的文件
			deleteTempFile(file);
			resultString = "导入物料类型表失败，请检查文件格式是否正确！";
			return resultString;
		}
		ExcelHelper fileReader = ExcelHelper.from(file);
		List<RegularMaterialTypeItemBO> items = fileReader.unfill(RegularMaterialTypeItemBO.class, 0);
		// 如果物料类型表头不对或者表格中没有物料信息记录
		if (items == null || items.size() == 0) {
			deleteTempFile(file);
			resultString = "导入物料类型表失败，套料单表头错误或者表格中没有任何有效的物料信息记录！";
			return resultString;
		} else {
			synchronized (IMPORT_FILE_LOCK) {
				// 根据供应商名获取供应商id
				Supplier s = Supplier.dao.findById(supplierId);
				if (s == null) {
					throw new OperationException("供应商不存在！");
				}

				// 从电子表格第2行开始有物料记录
				int i = 2;
				List<MaterialType> list = new ArrayList<>(items.size());
				for (RegularMaterialTypeItemBO item : items) {
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) { // 只读取有序号的行数据

						// 判断各单元格数据类型是否正确以及是否存在多余的空格
						if (item.getNo() == null || item.getSpecification() == null || item.getThickness() == null || item.getRadius() == null || item.getNo().replaceAll(" ", "").equals("") || item.getSpecification().replaceAll(" ", "").equals("") || item.getThickness().toString().replaceAll(" ", "").equals("") || item.getRadius().toString().replaceAll(" ", "").equals("")) {
							deleteTempFile(file);
							resultString = "导入物料类型表失败，请检查单表格第" + i + "行的料号/规格/厚度/半径列是否填写了准确信息！";
							return resultString;
						}

						// 判断厚度和半径是否为正整数
						if (item.getThickness() <= 0 || item.getRadius() <= 0) {
							deleteTempFile(file);
							resultString = "导入物料类型表失败，表格第" + i + "行的厚度/半径列不是正整数！";
							return resultString;
						}

						// 根据料号和供应商找到对应的物料类型
						MaterialType mType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_SUPPLIER_AND_NAME, item.getNo(), supplierId);
						/*
						 * 判断物料类型表中是否存在对应的料号且供应商也相同的物料类型记录，并且该物料类型未被禁用； 若存在，则跳过这些记录
						 */
						if (mType != null) {
							i++;
							continue;
						} else {
							// 若不存在异常数据，则新增一条物料类型表记录
							MaterialType materialType = new MaterialType();
							materialType.setNo(item.getNo().toUpperCase());
							materialType.setSpecification(item.getSpecification());
							materialType.setThickness(item.getThickness());
							materialType.setRadius(item.getRadius());
							materialType.setEnabled(true);
							materialType.setSupplier(supplierId);
							materialType.setType(WarehouseType.REGULAR);
							materialType.setDesignator("无");
							list.add(materialType);
						}

						i++;
					} else if (i == 2) { // 若第二行就没有序号，则说明表格一条物料记录也没有
						deleteTempFile(file);
						resultString = "导入物料类型表失败，表格没有任何有效的物料信息记录！";
						return resultString;
					} else {
						break;
					}
				}
				for (MaterialType materialType : list) {
					materialType.save();
				}
				deleteTempFile(file);
			}
		}
		return resultString;
	}


	// 导入物料类型表
	public String importPreicousMaterialTypeFile(String fileName, File file, Integer supplierId) throws Exception {
		String resultString = "导入成功！";
		// 如果文件格式不对，则提示检查文件格式
		if (!(fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
			// 清空upload目录下的文件
			deleteTempFile(file);
			resultString = "导入物料类型表失败，请检查文件格式是否正确！";
			return resultString;
		}
		ExcelHelper fileReader = ExcelHelper.from(file);
		List<PreciousMaterialTypeItemBO> items = fileReader.unfill(PreciousMaterialTypeItemBO.class, 0);
		List<String> nos = new ArrayList<>();
		List<String> designators = new ArrayList<>();
		// 如果物料类型表头不对或者表格中没有物料信息记录
		if (items == null || items.size() == 0) {
			deleteTempFile(file);
			resultString = "导入物料类型表失败，套料单表头错误或者表格中没有任何有效的物料信息记录！";
			return resultString;
		} else {
			synchronized (IMPORT_FILE_LOCK) {
				// 根据供应商名获取供应商id
				Supplier s = Supplier.dao.findById(supplierId);
				if (s == null) {
					throw new OperationException("供应商不存在！");
				}

				// 从电子表格第2行开始有物料记录
				int i = 2;
				List<MaterialType> list = new ArrayList<>(items.size());
				for (PreciousMaterialTypeItemBO item : items) {
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) { // 只读取有序号的行数据

						// 判断各单元格数据类型是否正确以及是否存在多余的空格
						if (item.getNo() == null || item.getSpecification() == null || item.getThickness() == null || item.getRadius() == null || item.getNo().replaceAll(" ", "").equals("") || item.getSpecification().replaceAll(" ", "").equals("") || item.getThickness().toString().replaceAll(" ", "").equals("") || item.getRadius().toString().replaceAll(" ", "").equals("")) {
							deleteTempFile(file);
							resultString = "导入物料类型表失败，请检查单表格第" + i + "行的料号/规格/厚度/半径列是否填写了准确信息！";
							return resultString;
						}

						// 判断厚度和半径是否为正整数
						if (item.getThickness() <= 0 || item.getRadius() <= 0) {
							deleteTempFile(file);
							resultString = "导入物料类型表失败，表格第" + i + "行的厚度/半径列不是正整数！";
							return resultString;
						}

						if (item.getDesignator() == null || item.getDesignator().trim().equals("")) {
							deleteTempFile(file);
							resultString = "导入物料类型表失败，表格第" + i + "行的位号为空！";
							return resultString;
						}
						// 根据料号和供应商找到对应的物料类型
						MaterialType tempMaterialType1 = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_SUPPLIER_AND_NAME, item.getNo(), supplierId);
						/*
						 * 判断物料类型表中是否存在对应的料号且供应商也相同的物料类型记录，并且该物料类型未被禁用； 若存在，则跳过这些记录
						 */
						MaterialType tempMaterialType2 = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_DESIGNATOR_AND_TYPE_SQL, supplierId, item.getDesignator().trim(), WarehouseType.PRECIOUS);

						if (tempMaterialType1 != null) {
							resultString = "导入物料类型表失败，表格第" + i + "行的料号已存在于系统中！";
							return resultString;
						} else if (tempMaterialType2 != null) {
							resultString = "导入物料类型表失败，表格第" + i + "行的位号已存在于系统中！";
							return resultString;
						} else if (nos.contains(item.getNo().trim())) {
							resultString = "导入物料类型表失败，表格第" + i + "行的料号在表格中重复！";
							return resultString;
						} else if (designators.contains(item.getDesignator().trim())) {
							resultString = "导入物料类型表失败，表格第" + i + "行的位号在表格中重复！";
							return resultString;
						} else {
							// 若不存在异常数据，则新增一条物料类型表记录
							MaterialType materialType = new MaterialType();
							materialType.setNo(item.getNo().toUpperCase());
							materialType.setSpecification(item.getSpecification());
							materialType.setThickness(item.getThickness());
							materialType.setRadius(item.getRadius());
							materialType.setEnabled(true);
							materialType.setSupplier(supplierId);
							materialType.setDesignator(item.getDesignator().trim());
							materialType.setType(WarehouseType.PRECIOUS);
							list.add(materialType);
							nos.add(item.getNo().trim());
							designators.add(item.getDesignator().trim());
						}

						i++;
					} else if (i == 2) { // 若第二行就没有序号，则说明表格一条物料记录也没有
						deleteTempFile(file);
						resultString = "导入物料类型表失败，表格没有任何有效的物料信息记录！";
						return resultString;
					} else {
						break;
					}
				}
				for (MaterialType materialType : list) {
					materialType.save();
				}
				deleteTempFile(file);
			}
		}
		return resultString;
	}


	public void deleteTempFile(File file) {
		// 清空upload目录下的文件
		if (file.exists()) {
			file.delete();
		}
	}


	/**
	 * 判断料盒表是否为空
	 */
	public Boolean isMaterialBoxEmpty() {
		int materialSize = MaterialBox.dao.find(JUDGE_MATERIAL_BOX_IS_EMPTY_SQL).size();
		if (materialSize == 0) {
			return true;
		} else {
			return false;
		}
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