package com.jimi.uw_server.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.bo.RecordItem;
import com.jimi.uw_server.model.vo.MaterialBoxVO;
import com.jimi.uw_server.model.vo.MaterialTypeVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ExcelHelper;

/**
 * 物料业务层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class MaterialService extends SelectService{

	private static SelectService selectService = Enhancer.enhance(SelectService.class);

	private static final String GET_MATERIAL_TYPE_IN_PROCESS_SQL = "SELECT * FROM packing_list_item WHERE material_type_id = ? AND task_id IN (SELECT id FROM task WHERE state <= 2)";

	private static final String COUNT_MATERIAL_BY_TYPE_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ?";
	
	private static final String COUNT_MATERIAL_BY_BOX_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE box = ?";

	private static final String GET_SPECIFIED_POSITION_MATERIAL_BOX_SQL = "SELECT * FROM material_box WHERE row = ? AND col = ? AND height = ?";

	private static final String GET_ENTITIES_SELECT_SQL = "SELECT id, type, box, row, col, remainder_quantity as remainderQuantity, production_time as productionTimeString ";

	private	static final String GET_ENTITIES_BY_TYPE_EXCEPT_SELECT_SQL = "FROM material WHERE type = ?";

	private	static final String GET_ENTITIES_BY_BOX_EXCEPT_SELECT_SQL = "FROM material WHERE box = ?";

	private	static final String GET_ENTITIES_BY_TYPE_AND_BOX_EXCEPT_SELECT_SQL = "FROM material WHERE type = ? and box = ?";

	private static final String GET_ENABLED_MATERIAL_TYPE_BY_NO_SQL = "SELECT * FROM material_type WHERE no = ? AND enabled = 1";

	private static final String GET_ENABLED_MATERIAL_BOX_BY_POSITION_SQL = "SELECT * FROM material_box WHERE area = ? AND row = ? AND col = ? AND height = ? AND enabled = 1";

	public static final String GET_ALL_TASK_LOGS_BY_MATERIAL_TYPE_ID_SQL = "SELECT *,SUM(quantity) AS totalIOQuantity FROM task_log WHERE material_id IN (SELECT id FROM material WHERE material.type = ?) GROUP BY packing_list_item_id ORDER BY task_log.time";

	public static final String GET_TASK_LOGS_BY_PACKING_LIST_ITEM_ID_SQL = "SELECT * FROM task_log WHERE packing_list_item_id = ? ORDER BY task_log.time";


	public Object count(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null ) {
			filter = filter.concat("&enabled=1");
		} else {
			filter = "enabled=1";
		}
		Page<Record> result = selectService.select(new String[] {"material_type"}, null, pageNo, pageSize, ascBy, descBy, filter);
		List<MaterialTypeVO> materialTypeVOs = new ArrayList<MaterialTypeVO>();
		for (Record res : result.getList()) {
			MaterialTypeVO m = new MaterialTypeVO(res.get("id"), res.get("no"), res.get("specification"), res.get("enabled"));
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


	public String addType(String no, String specification) {
		String resultString = "添加成功！";
		if(MaterialType.dao.find(GET_ENABLED_MATERIAL_TYPE_BY_NO_SQL, no).size() != 0) {
			resultString = "该物料已存在，请不要添加重复的物料类型号！";
			return resultString;
		}
		MaterialType materialType = new MaterialType();
		materialType.setNo(no);
		materialType.setSpecification(specification);
		materialType.setEnabled(true);
		materialType.save();
		return resultString;
	}


	public String updateType(MaterialType materialType) {
		String resultString = "更新成功！";
		if (!materialType.getEnabled()) {
			Material m = Material.dao.findFirst(COUNT_MATERIAL_BY_TYPE_SQL, materialType.getId());
			if (m.get("quantity") != null) {
				Integer quantity = Integer.parseInt(m.get("quantity").toString());
				if (quantity > 0) {
					resultString = "该物料库存数量大于0，禁止删除！";
					return resultString;
					}
				}
			if (PackingListItem.dao.findFirst(GET_MATERIAL_TYPE_IN_PROCESS_SQL, materialType.getId()) != null) {
				resultString = "当前有某个尚未完成的任务已经绑定了该物料，禁止删除该物料！";
				return resultString;
			}
		}
		materialType.update();
		return resultString;
	}


	public Object getBoxes(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null ) {
			filter = filter.concat("&enabled=1");
		} else {
			filter = "enabled=1";
		}
		Page<Record> result = selectService.select(new String[] {"material_box"}, null, pageNo, pageSize, ascBy, descBy, filter);
		List<MaterialBoxVO> MaterialBoxVOs = new ArrayList<MaterialBoxVO>();
		for (Record res : result.getList()) {
			MaterialBoxVO m = new MaterialBoxVO(res.get("id"), res.get("area"), res.get("row"), res.get("col"), res.get("height"), res.get("enabled"));
			MaterialBoxVOs.add(m);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(MaterialBoxVOs);

		return pagePaginate;
	}


	public String addBox(Integer area, Integer row, Integer col, Integer height) {
		String resultString = "添加成功！";
		if(MaterialType.dao.find(GET_ENABLED_MATERIAL_BOX_BY_POSITION_SQL, area, row, col, height).size() != 0) {
			resultString = "该位置已有料盒存在，请不要在该位置添加料盒！";
			return resultString;
		}
		MaterialBox materialBox = new MaterialBox();
		materialBox.setArea(area);
		materialBox.setRow(row);
		materialBox.setCol(col);
		materialBox.setHeight(height);
		materialBox.setIsOnShelf(true);
		materialBox.setEnabled(true);
		materialBox.save();
		return resultString;
	}


	public String updateBox(MaterialBox materialBox) {
		String resultString = "更新成功！";
		if (!materialBox.getEnabled()) {
			Material m = Material.dao.findFirst(COUNT_MATERIAL_BY_BOX_SQL, materialBox.getId());
			if (m.get("quantity") != null) {
				Integer quantity = Integer.parseInt(m.get("quantity").toString());
				if (quantity > 0) {
					resultString = "该料盒中还有物料，禁止删除！";
					return resultString;
					}
				}
		}
		materialBox.update();
		return resultString;
	}


	public Object getMaterialRecords(Integer type, Integer pageNo, Integer pageSize, String operator) {
		List<RecordItem> recordItemList = new ArrayList<RecordItem>();	// 用于存放完整的物料出入库记录
		List<TaskLog> taskLogList = TaskLog.dao.find(GET_ALL_TASK_LOGS_BY_MATERIAL_TYPE_ID_SQL, type);	// 查询该物料类型的所有出入库任务日志
		int remainderQuantity = 0;		// 结余数
		int superIssuedQuantity = 0;		// 累计超发数
		int lossQuantity = 0;		// 累计破损数
		for (TaskLog taskLog : taskLogList) {
			PackingListItem pItem = PackingListItem.dao.findById(taskLog.getPackingListItemId());
			Task task = Task.dao.findById(pItem.getTaskId());
			int actualQuantity = Integer.parseInt(taskLog.get("totalIOQuantity").toString());
			if (task.getType() == 0) {	// 对于入库任务，仅仅需要将入库数量累加到库存结余数即可
				remainderQuantity += actualQuantity;
			}  else if (task.getType() == 1) {	// 对于出库任务，要在库存结余数中减去出库数量，若有超发，还要记录超发数量
				remainderQuantity -= actualQuantity;
				superIssuedQuantity += actualQuantity - pItem.getQuantity();
			} else if (task.getType() == 4) {	// 对于退料入库任务，要将入库数量累加到库存结余数，并记录损耗数量，最后将超发数清零
				remainderQuantity += actualQuantity;
				lossQuantity += superIssuedQuantity - actualQuantity;
				superIssuedQuantity = 0;
			}
			RecordItem ri = new RecordItem(pItem, task.getFileName(), task.getType(), actualQuantity, remainderQuantity, superIssuedQuantity, lossQuantity, operator, taskLog.getTime());
			recordItemList.add(ri);
		}
		List<RecordItem> recordItemSubList = new ArrayList<RecordItem>();	// 用于存放物料出入库记录的子集，以实现分页查询
		int startIndex = (pageNo-1) * pageSize;
		int endIndex = (pageNo-1) * pageSize + pageSize;
		// 不用 endIndex 作为数组结尾是为了避免数组越界
		for (int i=startIndex; i<taskLogList.size(); i++) {
			recordItemSubList.add(recordItemList.get(i));
			if (i == endIndex-1) {
				break;
			}
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(recordItemList.size());
		pagePaginate.setList(recordItemSubList);
		return pagePaginate;
	}


	public void exportMaterialReport(String filaName, OutputStream output) throws IOException {
		List<MaterialType> materialList = MaterialType.dao.find("SELECT material_type.id as id, material_type.no as no, material_type.specification as specification, material_box.id AS box, material_box.row as row, material_box.col as col, material_box.height as height, SUM(material.remainder_quantity) AS quantity FROM (material_type LEFT JOIN material ON material_type.id = material.type) LEFT JOIN material_box ON material.box = material_box.id GROUP BY material.box, material.type, material_type.id ORDER BY material_type.id, material_box.id");
		String[] field = null;
		String[] head = null;
		field = new String[] { "id", "no", "specification", "box", "row", "col", "height", "quantity"};
		head =  new String[] { "物料类型号", "料号", "规格号", "盒号", "行号", "列号", "高度", "盒内物料数量"};	
		ExcelHelper helper = ExcelHelper.create(true);
		helper.fill(materialList, filaName, field, head);
		helper.write(output, true);
	}


	/**
	 * 列出同一个坐标盒子的所有物料类型
	 */
	public List<MaterialBox> listByXYZ(int x, int y, int z) {
		return MaterialBox.dao.find(GET_SPECIFIED_POSITION_MATERIAL_BOX_SQL, x, y, z);
	}

}