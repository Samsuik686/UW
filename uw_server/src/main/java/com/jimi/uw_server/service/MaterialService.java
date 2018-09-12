package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
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

	private static final String GET_ENABLED_MATERIAL_TYPE_SQL = "SELECT * FROM material_type WHERE enabled = 1";

	private static final String GET_MATERIAL_TYPE_ID_IN_PROCESS_SQL = "SELECT * FROM packing_list_item WHERE material_type_id = ? AND task_id IN"
			+ "(SELECT id FROM task WHERE state <= 2)";

	private static final String COUNT_MATERIAL_SQL = "SELECT SUM(remainder_quantity) as quantity FROM material WHERE type = ?";
	
	private static final String GET_SPECIFIED_POSITION_MATERIAL_TYEP_SQL = "SELECT * FROM material_type WHERE row = ? AND col = ? AND height = ?";

	private	static final String GET_ENTITIES_SQL = "SELECT material.id, material.type, material.row, material.col, "
			+ "material.remainder_quantity as remainderQuantity FROM material, material_type WHERE type = ? "
			+ "AND material_type.id = material.type AND material_type.enabled = 1";

	private static final String GET_MATERIAL_TYPE_BY_NO_SQL = "SELECT * FROM material_type WHERE no = ? AND enabled = 1";

	public Object count(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null ) {
			filter = filter.concat("&enabled=1");
		} else {
			filter = "enabled=1";
		}
		Page<Record> result = selectService.select(new String[] {"material_type"}, null,
				pageNo, pageSize, ascBy, descBy, filter);
		List<MaterialTypeVO> materialTypeVOs = new ArrayList<MaterialTypeVO>();
		for (Record res : result.getList()) {
			MaterialTypeVO m = new MaterialTypeVO(res.get("id"), res.get("no"), res.get("area"),
					res.get("row"), res.get("col"), res.get("height"), res.get("enabled"));
			materialTypeVOs.add(m);
		}

		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(MaterialType.dao.find(GET_ENABLED_MATERIAL_TYPE_SQL).size());
		pagePaginate.setList(materialTypeVOs);

		return pagePaginate;
	}

	public Object getEntities(Integer type, Integer pageNo, Integer pageSize) {
		List<Material> materialEntities = Material.dao.find(GET_ENTITIES_SQL, type);

		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(materialEntities.size());
		pagePaginate.setList(materialEntities);

		return pagePaginate;
	}

	public String add(String no, Integer area, Integer row, Integer col, Integer height) {
		String resultString = "添加成功！";
		if(MaterialType.dao.find(GET_MATERIAL_TYPE_BY_NO_SQL, no).size() != 0) {
			resultString = "该物料已存在，请不要添加重复的物料类型号！";
			return resultString;
		}
		MaterialType materialType = new MaterialType();
		materialType.setNo(no);
		materialType.setArea(area);
		materialType.setRow(row);
		materialType.setCol(col);
		materialType.setHeight(height);
		materialType.setEnabled(true);
		materialType.setIsOnShelf(true);
		materialType.save();
		return resultString;
	}

	public String update(MaterialType materialType) {
		String resultString = "更新成功！";
		if (!materialType.getEnabled()) {
			MaterialType mt = MaterialType.dao.findFirst(COUNT_MATERIAL_SQL, materialType.getId());
			if (mt.get("quantity") != null) {
				Integer quantity = Integer.parseInt(mt.get("quantity").toString());
				if (quantity > 0) {
					resultString = "该物料库存数量大于0，禁止删除！";
					return resultString;
					}
				}
			if (PackingListItem.dao.findFirst(GET_MATERIAL_TYPE_ID_IN_PROCESS_SQL, materialType.getId()) != null) {
				resultString = "当前有某个尚未完成的任务已经绑定了该物料，禁止删除该物料！";
				return resultString;
			}
		}
		materialType.update();
		return resultString;
	}

	/**
	 * 列出同一个坐标盒子的所有物料类型
	 */
	public List<MaterialType> listByXYZ(int x, int y, int z) {
		return MaterialType.dao.find(GET_SPECIFIED_POSITION_MATERIAL_TYEP_SQL, x, y, z);
	}

}