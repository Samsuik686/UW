package com.jimi.uw_server.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.model.BoxType;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.bo.MaterialTypeItemBO;
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
public class MaterialService extends SelectService{

	private static SelectService selectService = Enhancer.enhance(SelectService.class);

	private static final Object IMPORT_FILE_LOCK = new Object();

	private static final String GET_MATERIAL_TYPE_IN_PROCESS_SQL = "SELECT * FROM packing_list_item WHERE material_type_id = ? AND task_id IN (SELECT id FROM task WHERE state <= 2)";

	private static final String COUNT_MATERIAL_BY_TYPE_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ?";
	
	private static final String COUNT_MATERIAL_BY_BOX_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE box = ?";

	private static final String GET_SPECIFIED_POSITION_MATERIAL_BOX_SQL = "SELECT * FROM material_box WHERE row = ? AND col = ? AND height = ?";

	private static final String GET_ENTITIES_SELECT_SQL = "SELECT material.id AS id, material.type AS type, material.box AS box, material_box.area AS boxArea, material.row AS row, material.col AS col, material.remainder_quantity AS remainderQuantity, material.production_time AS productionTimeString ";

	private	static final String GET_ENTITIES_BY_TYPE_EXCEPT_SELECT_SQL = "FROM material, material_box WHERE material.type = ? AND material.box = material_box.id";

	private	static final String GET_ENTITIES_BY_BOX_EXCEPT_SELECT_SQL = "FROM material, material_box WHERE material.box = ? AND material.box = material_box.id";

	private	static final String GET_ENTITIES_BY_TYPE_AND_BOX_EXCEPT_SELECT_SQL = "FROM material, material_box WHERE material.type = ? and material.box = ? AND material.box = material_box.id";

	private static final String GET_ENABLED_MATERIAL_TYPE_BY_NO_SQL = "SELECT * FROM material_type WHERE no = ? AND supplier = ? AND enabled = 1";

	private static final String GET_ENABLED_MATERIAL_BOX_BY_POSITION_SQL = "SELECT * FROM material_box WHERE area = ? AND row = ? AND col = ? AND height = ? AND enabled = 1";

	public static final String GET_ALL_TASK_LOGS_BY_MATERIAL_TYPE_ID_SQL = "SELECT *,SUM(quantity) AS totalIOQuantity FROM task_log WHERE material_id IN (SELECT id FROM material WHERE material.type = ?) AND (destination = ? OR destination is NULL) GROUP BY packing_list_item_id ORDER BY task_log.time";

	public static final String GET_MATERIAL_REPORT_SQL = "SELECT material_type.id as id, material_type.no as no, material_type.specification as specification, material_box.id AS box, material_box.row as row, material_box.col as col, material_box.height as height, SUM(material.remainder_quantity) AS quantity FROM (material_type LEFT JOIN material ON material_type.id = material.type) LEFT JOIN material_box ON material.box = material_box.id WHERE material_type.supplier = ? AND material_type.enabled = 1 GROUP BY material.box, material.type, material_type.id ORDER BY material_type.id, material_box.id";

	public static final String GET_ENABLED_SUPPLIER_ID_BY_NAME_SQL = "SELECT * FROM supplier WHERE name = ? AND enabled = 1";

	public static final String GET_BOX_TYPE_BY_CELL_WIDTH_SQL = "SELECT * FROM box_type WHERE cell_width = ?";

	private static final String GET_ENABLED_BOX_TYPE_BY_CELL_WIDTH_SQL = "SELECT * FROM box_type WHERE cell_width = ? AND enabled = 1";

	private static final String GET_ENABLED_MATERIAL_BOX_BY_TYPE_SQL = "SELECT * FROM material_box WHERE type = ? AND enabled = 1";

	private static final String GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_SQL = "SELECT * FROM material_type WHERE no = ? AND supplier = ? AND enabled = 1";

	private static final String JUDGE_MATERIAL_BOX_IS_EMPTY_SQL = "SELECT * FROM material_box WHERE enabled = 1";

	private static final String COUNT_MATERIAL_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ?";


	public Object count(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null ) {
			filter = filter.concat("#&#enabled=1");
		} else {
			filter = "enabled=1";
		}
		Page<Record> result = selectService.select(new String[] {"material_type"}, null, pageNo, pageSize, ascBy, descBy, filter);
		List<MaterialTypeVO> materialTypeVOs = new ArrayList<MaterialTypeVO>();
		for (Record res : result.getList()) {
			MaterialTypeVO m = new MaterialTypeVO(res.get("id"), res.get("no"), res.get("specification"), res.get("supplier"), res.get("thickness"), res.get("radius"), res.get("enabled"));
			materialTypeVOs.add(m);
		}

		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(materialTypeVOs);

		return pagePaginate;
	}


	public Page<Record> getEntities(Integer type, Integer box, Integer pageNo, Integer pageSize) {
		Page<Record> materialEntities = new Page<Record>();
		if (type != null && box == null) {
			materialEntities = Db.paginate(pageNo, pageSize, GET_ENTITIES_SELECT_SQL, GET_ENTITIES_BY_TYPE_EXCEPT_SELECT_SQL, type);
		} else if (type == null && box != null) {
			materialEntities = Db.paginate(pageNo, pageSize, GET_ENTITIES_SELECT_SQL, GET_ENTITIES_BY_BOX_EXCEPT_SELECT_SQL, box);
		} else if (type != null && box != null) {
			materialEntities = Db.paginate(pageNo, pageSize, GET_ENTITIES_SELECT_SQL, GET_ENTITIES_BY_TYPE_AND_BOX_EXCEPT_SELECT_SQL, type, box);
		}

		return materialEntities;
	}


	public String addType(String no, String specification, String supplierName, Integer thickness, Integer radius) {
		String resultString = "添加成功！";
		Integer supplier;
		Supplier s = Supplier.dao.findFirst(GET_ENABLED_SUPPLIER_ID_BY_NAME_SQL, supplierName);
		supplier = s.getId();
		if(MaterialType.dao.find(GET_ENABLED_MATERIAL_TYPE_BY_NO_SQL, no, supplier).size() != 0) {
			resultString = "该物料类型已存在，请勿重复添加！";
			return resultString;
		}
		if (no.contains("!") || no.contains("$")) {
			resultString = "请勿往料号中添加非法字符，如“!”或“$”！";
			return resultString;
		}
		MaterialType materialType = new MaterialType();
		materialType.setNo(no);
		materialType.setSpecification(specification);
		materialType.setSupplier(supplier);
		materialType.setThickness(thickness);
		materialType.setRadius(radius);
		materialType.setEnabled(true);
		materialType.save();
		return resultString;
	}


	public String updateType(Integer id, String supplierName, Boolean enabled, Integer thickness, Integer radius) {
		String resultString = "更新成功！";
		if (!enabled) {
			Material m = Material.dao.findFirst(COUNT_MATERIAL_BY_TYPE_SQL,id);
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
		Integer supplier;
		Supplier s = Supplier.dao.findFirst(GET_ENABLED_SUPPLIER_ID_BY_NAME_SQL, supplierName);
		supplier = s.getId();
		MaterialType materialType = MaterialType.dao.findById(id);
		materialType.setSupplier(supplier);
		materialType.setThickness(thickness);
		materialType.setRadius(radius);
		materialType.setEnabled(enabled);
		materialType.update();
		return resultString;
	}


	public Object getBoxes(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null ) {
			filter = filter.concat("#&#enabled=1");
		} else {
			filter = "enabled=1";
		}
		Page<Record> result = selectService.select(new String[] {"material_box"}, null, pageNo, pageSize, ascBy, descBy, filter);
		List<MaterialBoxVO> materialBoxVOs = new ArrayList<MaterialBoxVO>();
		for (Record res : result.getList()) {
			MaterialBoxVO m = new MaterialBoxVO(res.get("id"), res.get("area"), res.get("row"), res.get("col"), res.get("height"), 
					res.get("enabled"), res.get("is_on_shelf"), res.get("type"));
			materialBoxVOs.add(m);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(materialBoxVOs);

		return pagePaginate;
	}


	public String addBox(String area, Integer row, Integer col, Integer height, Integer cellWidth) {
		String resultString = "添加成功！";
		if(MaterialType.dao.find(GET_ENABLED_MATERIAL_BOX_BY_POSITION_SQL, area, row, col, height).size() != 0) {
			resultString = "该位置已有料盒存在，请不要在该位置添加料盒！";
			return resultString;
		}
		BoxType boxType = BoxType.dao.findFirst(GET_BOX_TYPE_BY_CELL_WIDTH_SQL, cellWidth);
		if (boxType == null) {
			resultString = "请填写正确的料盒规格！";
			return resultString;
		}
		MaterialBox materialBox = new MaterialBox();
		materialBox.setArea(area);
		materialBox.setRow(row);
		materialBox.setCol(col);
		materialBox.setHeight(height);
		materialBox.setType(boxType.getId());
		materialBox.setIsOnShelf(true);
		materialBox.setEnabled(true);
		materialBox.save();
		return resultString;
	}


	public Boolean updateBox(Integer id, Boolean isOnShelf) {
		MaterialBox materialBox = MaterialBox.dao.findById(id);
		materialBox.setIsOnShelf(isOnShelf);
		return materialBox.update();
	}


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


	public Object getMaterialRecords(Integer type, Integer destination, Integer pageNo, Integer pageSize) {
		List<RecordItem> recordItemList = new ArrayList<RecordItem>();	// 用于存放完整的物料出入库记录
		recordItemList = getRecordItemList(type, destination);
		List<RecordItem> recordItemSubList = new ArrayList<RecordItem>();	// 用于存放物料出入库记录的子集，以实现分页查询
		int startIndex = (pageNo-1) * pageSize;
		int endIndex = (pageNo-1) * pageSize + pageSize;
		if (startIndex < recordItemList.size()) {
			if (endIndex >= recordItemList.size()) {
				endIndex = recordItemList.size();
			}
			recordItemSubList = recordItemList.subList(startIndex, endIndex);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(recordItemList.size());
		pagePaginate.setList(recordItemSubList);
		return pagePaginate;
	}


	public List<RecordItem> getRecordItemList(int type, int destination) {
		int superIssuedQuantity = 0;		// 累计超发数
		List<RecordItem> recordItemList = new ArrayList<RecordItem>();	// 用于存放完整的物料出入库记录
		List<TaskLog> taskLogList = TaskLog.dao.find(GET_ALL_TASK_LOGS_BY_MATERIAL_TYPE_ID_SQL, type, destination);	// 查询该物料类型的所有出入库任务日志
		for (TaskLog taskLog : taskLogList) {
			PackingListItem pItem = PackingListItem.dao.findById(taskLog.getPackingListItemId());
			Task task = Task.dao.findById(pItem.getTaskId());
			int planQuantity = pItem.getQuantity();
			int actualQuantity = Integer.parseInt(taskLog.get("totalIOQuantity").toString());
			if (task.getType() == 1) {
				// 对于出库任务，计算超发数

				// 1、若超发数大于等于出库计划数，则直接进行抵扣；若此次还有出库，则再次累计超发
				if (superIssuedQuantity >= planQuantity) {
					superIssuedQuantity -= planQuantity;
					superIssuedQuantity += actualQuantity;
				}

				// 2、若有超发记录但超发数小于出库计划数，则抵扣部分超发数
				else if (superIssuedQuantity > 0 && superIssuedQuantity < planQuantity) {
					// 根据公式：超发数 = 实际出库数 - （计划出库数 - 超发数）	计算出新的出库数
					superIssuedQuantity = actualQuantity - (planQuantity - superIssuedQuantity);
				}

				// 3、否则，直接根据实际出库数和计划出库数计算超发数
				else {
					superIssuedQuantity += actualQuantity - planQuantity;
				}

			} else if (task.getType() == 4) {	// 对于退料入库任务，退料后要将超发数清零
				superIssuedQuantity = 0;
			}
			RecordItem ri = new RecordItem(pItem.getMaterialTypeId(), pItem.getQuantity(), task.getFileName(), task.getType(), actualQuantity, superIssuedQuantity, taskLog.getOperator(), taskLog.getTime());
			recordItemList.add(ri);
		}
		return recordItemList;
	}


	public void exportMaterialReport(Integer supplier, String fileName, OutputStream output) throws IOException {
		List<Record> materialRecord = Db.find(GET_MATERIAL_REPORT_SQL, supplier);
		String[] field = null;
		String[] head = null;
		field = new String[] { "id", "no", "specification", "box", "row", "col", "height", "quantity"};
		head =  new String[] { "物料类型号", "料号", "规格号", "盒号", "行号", "列号", "高度", "盒内物料数量"};	
		ExcelWritter writter = ExcelWritter.create(true);
		writter.fill(materialRecord, fileName, field, head);
		writter.write(output, true);
	}


	public Object getBoxTypes(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null ) {
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


	public String addBoxType(Integer cellWidth, Integer cellRows, Integer cellCols) {
		String resultString = "添加成功！";
		if(BoxType.dao.find(GET_ENABLED_BOX_TYPE_BY_CELL_WIDTH_SQL, cellWidth).size() != 0) {
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


	public String importFile(String fileName, String fullFileName, String supplierName) throws Exception {
		String resultString = "导入成功！";
		File file = new File(fullFileName);
		// 如果文件格式不对，则提示检查文件格式
		if (!(fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
			//清空upload目录下的文件
			deleteTempFile(file);
			resultString = "导入物料类型表失败，请检查文件格式是否正确！";
			return resultString;
		}
		ExcelHelper fileReader = ExcelHelper.from(file);
		List<MaterialTypeItemBO> items = fileReader.unfill(MaterialTypeItemBO.class, 0);
		// 如果套料单表头不对，则提示检查套料单表头，同时检查套料单表格中是否有料号记录
		if (items == null || items.size() == 0) {
			deleteTempFile(file);
			resultString = "导入物料类型表失败，请检查表头是否正确！";
			return resultString;
		} else {
			synchronized(IMPORT_FILE_LOCK) {
				// 根据供应商名获取供应商id
				Supplier s = Supplier.dao.findFirst(GET_ENABLED_SUPPLIER_ID_BY_NAME_SQL, supplierName);
				Integer supplier;
				supplier = s.getId();

				// 从电子表格第2行开始有物料记录
				int i = 2;
				for (MaterialTypeItemBO item : items) {
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) {	// 只读取有序号的行数据

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
						MaterialType mType = MaterialType.dao.findFirst(GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_SQL, item.getNo(), supplier);
						/*						
						 判断物料类型表中是否存在对应的料号且供应商也相同的物料类型记录，并且该物料类型未被禁用；
						 若存在，则跳过这些记录
						*/
						if (mType != null) {
							i++;
							continue;
						} else {
							// 若不存在异常数据，则新增一条物料类型表记录
							MaterialType materialType = new MaterialType();
							materialType.setNo(item.getNo());
							materialType.setSpecification(item.getSpecification());
							materialType.setThickness(item.getThickness());
							materialType.setRadius(item.getRadius());
							materialType.setEnabled(true);
							materialType.setSupplier(supplier);
							materialType.save();
						}
						
						i++;
					} else if (i == 2) {	// 若第二行就没有序号，则说明表格一条物料记录也没有
						deleteTempFile(file);
						resultString = "导入物料类型表失败，表格没有任何有效的物料信息记录！";
						return resultString;
					} else {
						break;
					}
				}
				
				deleteTempFile(file);
			}
		}
		return resultString;
	}

	public void deleteTempFile(File file) {
		//清空upload目录下的文件
		if (file.exists()) {
			file.delete();
		}
	}



	/**
	 * 列出同一个坐标盒子的所有物料类型
	 */
	public List<MaterialBox> listByXYZ(int x, int y, int z) {
		return MaterialBox.dao.find(GET_SPECIFIED_POSITION_MATERIAL_BOX_SQL, x, y, z);
	}


	/**
	 * 判断料盒表是否为空
	 */
	public Boolean isMaterialBoxEmpty() {
		int materialSize = MaterialBox.dao.find(JUDGE_MATERIAL_BOX_IS_EMPTY_SQL).size();
		if ( materialSize == 0) {
			return true;
		} else {
			return false;
		}
	}


	/**
	 * 根据物料类型号计算并物料库存数
	 */
	public Integer countAndReturnRemainderQuantityByMaterialTypeId(Integer materialTypeId) {
		Material material = Material.dao.findFirst(COUNT_MATERIAL_SQL, materialTypeId);
		Integer remainderQuantity;
		if (material.get("quantity") == null) {
			remainderQuantity =  0;
		} else {
			remainderQuantity = Integer.parseInt(material.get("quantity").toString());
		}
		return remainderQuantity;
	}


}