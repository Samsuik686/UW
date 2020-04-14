package com.jimi.uw_server.constant.sql;

public class SQL {

	// 根据仓口ID查询货位
	public static final String GET_GOODSLOCATION_BY_WINDOWID = "SELECT DISTINCT * FROM goods_location WHERE window_id = ? AND enabled = 1 ORDER BY id ASC";
	// 根据料盒计算料盘数
	public static final String GET_MATERIAL_BY_TYPE_AND_BOX = "SELECT * FROM material WHERE material.type = ? AND material.box = ? AND material.is_in_box = 1 AND material.remainder_quantity > 0 ORDER BY production_time ASC";

    public static final String GET_MATERIAL_BY_BOX = "SELECT * FROM material WHERE material.box = ? AND material.is_in_box = 1 AND material.remainder_quantity > 0 ORDER BY col, row ASC";
    
	public static final String GET_MATERIALS_BY_TIME_AND_BOX = "SELECT * FROM material WHERE material.type = ? AND material.box = ? AND material.is_in_box = 1 AND material.remainder_quantity > 0 AND production_time = ?";
	// 根据任务条目ID查询相应的日志和物料记录（用于停泊条目阶段，此时物料数量暂未清零）
	public static final String GET_PACKING_LIST_ITEM_DETAILS_SQL = "SELECT task_log.id, material_id AS materialId, row, col, quantity, production_time AS productionTime, is_in_box AS isInBox, remainder_quantity  AS remainderQuantity, box AS boxId FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id";

	public static final String GET_OUT_MATERIAL_SQL_BY_BOX = "SELECT task_log.id, material_id AS materialId, quantity, production_time AS productionTime, is_in_box AS isInBox, remainder_quantity  AS remainderQuantity FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id AND material.box = ?";

	public static final String GET_TASK_ITEM_DETAILS_SQL = "SELECT material_id AS materialId, quantity, production_time AS productionTime FROM task_log JOIN material ON task_log.packing_list_item_id = ? AND task_log.material_id = material.id";

	public static final String GET_TASKLOG_INFO_BY_TASK_LOG_ID_SQL = "SELECT material.id AS material_id, material.remainder_quantity, material.production_time, material.box, material.is_in_box, task_log.quantity FROM material INNER JOIN task_log ON material.id = task_log.material_id WHERE task_log.id = ? AND packing_list_item_id = ? AND material_id = ?";

	public static final String GET_TASKLOG_BY_ITEM_ID_SQL = "SELECT material.id AS material_id, material.remainder_quantity, material.production_time, material.box, material.is_in_box, task_log.quantity FROM material INNER JOIN task_log ON material.id = task_log.material_id WHERE packing_list_item_id = ?";

	public static final String GET_NEWEST_MATERIAL_TASKLOG_BY_ITEM_ID_SQL = "SELECT material.id AS material_id, material.production_time FROM material INNER JOIN task_log ON material.id = task_log.material_id WHERE packing_list_item_id = ? ORDER BY material.production_time DESC";

	// 具备时效性，仅使用与出库时，判断那一时刻是否存在截料条目
	public static final String GET_CUT_MATERIAL_RECORD_SQL = "SELECT task_log.* FROM task_log INNER JOIN material ON material.id = task_log.material_id WHERE material.remainder_quantity > 0 AND material.remainder_quantity != task_log.quantity AND task_log.packing_list_item_id = ?";
	// 获取运行中的仓口
	public static final String GET_WORKING_WINDOWS = "SELECT * FROM window WHERE bind_task_id IS NOT NULL";

	public static final String GET_OUT_QUANTITY_BY_PACKINGITEMID = "SELECT sum(quantity) as totalQuantity, operator FROM task_log WHERE packing_list_item_id = ? AND quantity > 0";

	public static final String GET_ALL_TASK_ITEM_BY_TASK_ID = "SELECT * FROM packing_list_item WHERE task_id = ?";

	public static final String GET_REGULAR_UN_OUT_SAMPLE_TAKS_MATERIAL = "SELECT material.id AS id, material.row AS row, material.col AS col, material.type AS material_type_id, material.remainder_quantity AS quantity, material.production_time AS production_time, material_type.`no` AS `no`, material_type.specification AS specification, supplier.id AS supplier_id, supplier.`name` AS supplier_name, sample_task_material_record.is_scaned AS is_scaned FROM material INNER JOIN material_type INNER JOIN supplier INNER JOIN sample_task_item INNER JOIN sample_task_material_record ON sample_task_item.material_type_id = material.type AND sample_task_material_record.task_id = sample_task_item.task_id  AND material.type = material_type.id AND supplier.id = material_type.supplier AND sample_task_material_record.material_id = material.id WHERE material.box = ? AND material.is_in_box = 1 AND remainder_quantity > 0 AND sample_task_item.task_id = ? ORDER BY `sample_task_item`.id, id ASC";

	public static final String GET_PRECIOUS_UN_OUT_SAMPLE_TAKS_MATERIAL = "SELECT material.id AS id, material.row AS row, material.col AS col, material.type AS material_type_id, material.remainder_quantity AS quantity, material.production_time AS production_time, material_type.`no` AS `no`, material_type.specification AS specification, supplier.id AS supplier_id, supplier.`name` AS supplier_name, sample_task_material_record.is_scaned AS is_scaned FROM material INNER JOIN material_type INNER JOIN supplier INNER JOIN sample_task_item INNER JOIN sample_task_material_record ON sample_task_item.material_type_id = material.type AND sample_task_material_record.task_id = sample_task_item.task_id  AND material.type = material_type.id AND supplier.id = material_type.supplier AND sample_task_material_record.material_id = material.id WHERE material.`status` = ? AND remainder_quantity > 0 AND sample_task_item.task_id = ? ORDER BY `sample_task_item`.id, id ASC";

	public static final String GET_REGULAR_OUT_SAMPLE_TASK_MATERIAL = "SELECT sample_out_record.material_id AS id, material.row AS row, material.col AS col, sample_task_item.material_type_id AS material_type_id, sample_out_record.quantity AS quantity,sample_out_record.out_type AS out_type, material.production_time AS production_time, material_type.`no` AS `no`, material_type.specification AS specification, supplier.id AS supplier_id, supplier.`name` AS supplier_name FROM material INNER JOIN material_type INNER JOIN supplier INNER JOIN sample_task_item INNER JOIN sample_out_record ON sample_task_item.material_type_id = material.type AND material.type = material_type.id AND supplier.id = material_type.supplier AND sample_out_record.material_id = material.id AND sample_out_record.sample_task_item_id = sample_task_item.id WHERE sample_out_record.box_id = ? AND sample_task_item.task_id = ? ORDER BY `sample_task_item`.id, id ASC";

	public static final String GET_PRECIOUS_OUT_SAMPLE_TASK_MATERIAL = "SELECT sample_out_record.material_id AS id, material.row AS row, material.col AS col, sample_task_item.material_type_id AS material_type_id, sample_out_record.quantity AS quantity,sample_out_record.out_type AS out_type, material.production_time AS production_time, material_type.`no` AS `no`, material_type.specification AS specification, supplier.id AS supplier_id, supplier.`name` AS supplier_name FROM material INNER JOIN material_type INNER JOIN supplier INNER JOIN sample_task_item INNER JOIN sample_out_record ON sample_task_item.material_type_id = material.type AND material.type = material_type.id AND supplier.id = material_type.supplier AND sample_out_record.material_id = material.id AND sample_out_record.sample_task_item_id = sample_task_item.id WHERE sample_task_item.task_id = ? ORDER BY `sample_task_item`.id, id ASC";

	public static final String GET_WINDOW_BY_TASKID = "SELECT * FROM window WHERE bind_task_id = ?";

	public static final String GET_MATERIAL_WITH_LOCATION_BY_SUPPLIER = "SELECT * FROM material INNER JOIN material_box ON material.box = material_box.id WHERE material.row != -1 AND material.col != -1 AND material_box.supplier = ?";

	public static final String GET_MATERIAL_BY_SUPPLIER = "SELECT * FROM material INNER JOIN  material_box ON material.box = material_box.id WHERE material_box.supplier = ? AND material.remainder_quantity > 0 AND material.id != ? AND material_box.type = ?";

	public static final String GET_GOODSLOCATION_BY_WINDOW = "SELECT * FROM goods_location where window_id = ?";
	
	public static final String GET_WINDOW_BY_TASK = "SELECT * FROM window WHERE bind_task_id = ? ORDER BY id ASC";

}
