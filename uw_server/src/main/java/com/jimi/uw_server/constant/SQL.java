package com.jimi.uw_server.constant;


public class SQL {
	
	//根据仓口ID查询货位
	public static final String GET_GOODSLOCATION_BY_WINDOWID = "SELECT DISTINCT * FROM goods_location WHERE window_id = ? ORDER BY id ASC";
	//根据料盒计算料盘数
	public static final String GET_REELNUM_BY_BOX = "SELECT COUNT(1) AS reelNum FROM material WHERE material.type = ? AND material.box = ? AND material.is_in_box = 1 AND material.remainder_quantity > 0";

	//根据任务条目ID查询相应的日志和物料记录（用于停泊条目阶段，此时物料数量暂未清零）
	public static final String GET_PACKING_LIST_ITEM_DETAILS_SQL = "SELECT task_log.id, material_id AS materialId, quantity, production_time AS productionTime, is_in_box AS isInBox, remainder_quantity  AS remainderQuantity FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id";

	public static final String GET_OUT_MATERIAL_SQL_BY_BOX = "SELECT task_log.id, material_id AS materialId, quantity, production_time AS productionTime, is_in_box AS isInBox, remainder_quantity  AS remainderQuantity FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id AND material.box = ?";
	
	public static final String GET_TASK_ITEM_DETAILS_SQL = "SELECT material_id AS materialId, quantity, production_time AS productionTime FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id";

	public static final String GET_TASKLOG_INFO_BY_TASK_LOG_ID_SQL = "SELECT material.id AS material_id, material.remainder_quantity, material.production_time, material.box, material.is_in_box, task_log.quantity FROM material INNER JOIN task_log ON material.id = task_log.material_id WHERE task_log.id = ? AND packing_list_item_id = ? AND material_id = ?";
	
	public static final String GET_TASKLOG_BY_ITEM_ID_SQL = "SELECT material.id AS material_id, material.remainder_quantity, material.production_time, material.box, material.is_in_box, task_log.quantity FROM material INNER JOIN task_log ON material.id = task_log.material_id WHERE packing_list_item_id = ?";
	
	//具备时效性，仅使用与出库时，判断那一时刻是否存在截料条目
	public static final String GET_CUT_MATERIAL_RECORD_SQL = "SELECT task_log.* FROM task_log INNER JOIN material ON material.id = task_log.material_id WHERE material.remainder_quantity > 0 AND material.remainder_quantity != task_log.quantity AND task_log.packing_list_item_id = ?";
	//获取运行中的仓口
	public static final String GET_WORKING_WINDOWS = "SELECT * FROM window WHERE bind_task_id IS NOT NULL";
	
	public static final String GET_OUT_QUANTITY_BY_PACKINGITEMID = "SELECT sum(quantity) as totalQuantity, operator FROM task_log WHERE packing_list_item_id = ? AND quantity > 0";

	public static final String GET_ALL_TASK_ITEM_BY_TASK_ID = "SELECT * FROM packing_list_item WHERE task_id = ?";
	
	public static final String GET_UN_OUT_SAMPLE_TAKS_MATERIAL = "SELECT material.id AS id, material.type AS material_type_id, material.remainder_quantity AS quantity, material.production_time AS production_time, material_type.`no` AS `no`, material_type.specification AS specification, supplier.id AS supplier_id, supplier.`name` AS supplier_name, sample_task_material_record.is_scaned AS is_scaned FROM material INNER JOIN material_type INNER JOIN supplier INNER JOIN sample_task_item INNER JOIN sample_task_material_record ON sample_task_item.material_type_id = material.type AND sample_task_material_record.task_id = sample_task_item.task_id  AND material.type = material_type.id AND supplier.id = material_type.supplier AND sample_task_material_record.material_id = material.id WHERE material.box = ? AND material.is_in_box = 1 AND remainder_quantity > 0 AND sample_task_item.task_id = ? ORDER BY `sample_task_item`.id, id ASC";

	public static final String GET_OUT_SAMPLE_TASK_MATERIAL = "SELECT sample_singular_record.material_id AS id, sample_task_item.material_type_id AS material_type_id, sample_singular_record.quantity AS quantity, material.production_time AS production_time, material_type.`no` AS `no`, material_type.specification AS specification, supplier.id AS supplier_id, supplier.`name` AS supplier_name FROM material INNER JOIN material_type INNER JOIN supplier INNER JOIN sample_task_item INNER JOIN sample_singular_record ON sample_task_item.material_type_id = material.type AND material.type = material_type.id AND supplier.id = material_type.supplier AND sample_singular_record.material_id = material.id AND sample_singular_record.sample_task_item_id = sample_task_item.id WHERE sample_singular_record.box_id = ? AND sample_task_item.task_id = ? ORDER BY `sample_task_item`.id, id ASC";

	public static final String GET_WINDOW_BY_TASKID = "SELECT * FROM window WHERE bind_task_id = ?";

}
