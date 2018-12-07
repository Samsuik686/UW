package com.jimi.uw_server.service;

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
import com.jimi.uw_server.model.vo.MaterialBoxVO;
import com.jimi.uw_server.model.vo.MaterialTypeVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;

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


	public Object count(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null ) {
			filter = filter.concat("#&#enabled=1");
		} else {
			filter = "enabled=1";
		}
		Page<Record> result = selectService.select(new String[] {"material_type"}, null,
				pageNo, pageSize, ascBy, descBy, filter);
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
			resultString = "该物料已存在，请不要添加重复的料号！";
			return resultString;
		}
		if (no.contains("!") || no.contains("$")) {
			resultString = "请勿往料号中添加非法字符，如“!”或“$”！";
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
			filter = filter.concat("#&#enabled=1");
		} else {
			filter = "enabled=1";
		}
		Page<Record> result = selectService.select(new String[] {"material_box"}, null, pageNo, pageSize, ascBy, descBy, filter);
		List<MaterialBoxVO> MaterialBoxVOs = new ArrayList<MaterialBoxVO>();
		for (Record res : result.getList()) {
			MaterialBoxVO m = new MaterialBoxVO(res.get("id"), res.get("area"), res.get("row"), res.get("col"), res.get("height"), 
					res.get("enabled"), res.get("is_on_shelf"));
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
		if(MaterialType.dao.find(GET_ENABLED_MATERIAL_BOX_BY_POSITION_SQL, materialBox.getArea(), materialBox.getRow(), materialBox.getCol(), materialBox.getHeight()).size() != 0) {
			resultString = "该位置已有料盒存在，请不要在该位置添加料盒！";
			return resultString;
		}
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


	/**
	 * 列出同一个坐标盒子的所有物料类型
	 */
	public List<MaterialBox> listByXYZ(int x, int y, int z) {
		return MaterialBox.dao.find(GET_SPECIFIED_POSITION_MATERIAL_BOX_SQL, x, y, z);
	}

}