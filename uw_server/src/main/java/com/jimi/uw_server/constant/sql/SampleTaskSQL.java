package com.jimi.uw_server.constant.sql;

public class SampleTaskSQL {

	public static final String GET_SAMPLE_OUT_RECORD_BY_MATERIALID = "SELECT * FROM sample_out_record WHERE material_id = ?";

	public static final String GET_SAMPLE_TASK_DETIALS = "SELECT a.*, sample_out_record.id AS id, sample_out_record.material_id AS material_id, sample_out_record.operator AS operator, sample_out_record.quantity AS quantity, sample_out_record.out_type AS out_type, sample_out_record.time AS time FROM ( SELECT sample_task_item.task_id AS task_id, sample_task_item.id AS sample_task_item_id, sample_task_item.material_type_id AS material_type_id, sample_task_item.store_quantity AS store_quantity, sample_task_item.scan_quantity AS scan_quantity, sample_task_item.regular_out_quantity AS regular_out_quantity, sample_task_item.singular_out_quantity AS singular_out_quantity, sample_task_item.lost_out_quantity AS lost_out_quantity, material_type.`no` AS `no` FROM sample_task_item INNER JOIN material_type ON material_type.id = sample_task_item.material_type_id WHERE sample_task_item.task_id = ? AND material_type.enabled = 1) a LEFT JOIN sample_out_record ON sample_out_record.sample_task_item_id = a.sample_task_item_id ORDER BY a.sample_task_item_id";

	public static final String GET_EXPROT_SAMPLE_TASK_INFO = "SELECT sample_task_item.task_id AS task_id, sample_task_item.id AS sample_task_item_id, sample_task_item.material_type_id AS material_type_id, sample_task_item.store_quantity AS store_quantity, sample_task_item.scan_quantity AS scan_quantity, sample_task_item.regular_out_quantity AS regular_out_quantity, sample_task_item.singular_out_quantity AS singular_out_quantity, material_type.`no` AS `no`, supplier.`name` AS `supplier_name` FROM sample_task_item INNER JOIN material_type INNER JOIN supplier ON material_type.id = sample_task_item.material_type_id AND material_type.supplier = supplier.id WHERE sample_task_item.task_id = ? ORDER BY sample_task_item.id";

	public static final String GET_MATERIAL_BY_SAMPLE_TASK = "SELECT DISTINCT material.* FROM material JOIN sample_task_item JOIN material_type ON sample_task_item.material_type_id = material.type AND material.type = material_type.id WHERE sample_task_item.task_id = ? AND material.remainder_quantity > 0 AND material_type.type = ? AND material.`status` = ?";

	public static final String GET_PROBLEM_MATERIAL_BY_SAMPLE_TASK = "SELECT DISTINCT material.* FROM material JOIN sample_task_item JOIN material_type ON sample_task_item.material_type_id = material.type AND material.type = material_type.id WHERE sample_task_item.task_id = ? AND material.remainder_quantity > 0 AND material_type.type = ? AND material.`status` != ?";

	public static final String GET_SAMPLETASKITEM_BY_TASK = "SELECT * FROM sample_task_item WHERE task_id = ?";

	public static final String GET_UNSCAN_MATERIAL_BY_TASK = "SELECT * FROM sample_task_material_record WHERE task_id = ? AND is_scaned = 0";

	public static final String GET_UNSCAN_MATERIAL_BY_TASK_AND_MATERIAL_TYPE = "SELECT sample_task_material_record.* FROM sample_task_material_record INNER JOIN material ON sample_task_material_record.material_id = material.id WHERE task_id = ? AND material.type = ? AND is_scaned = 0";

	public static final String GET_SAMPLETASKITEM_BY_TASK_AND_TYPE = "SELECT sample_task_item.* FROM sample_task_item INNER JOIN material_type ON material_type.id = sample_task_item.material_type_id WHERE task_id = ? AND material_type_id = ? AND material_type.type = ?";

	public static final String GET_UNSCAN_MATERIAL_BY_MATERIAL = "SELECT * FROM sample_task_material_record WHERE task_id = ? AND material_id = ? AND is_scaned = 0";

	public static final String GET_RECORD_BY_MATERIALID_TASK_BOX = "SELECT * FROM sample_task_material_record WHERE material_id = ? AND task_id = ? AND box_id = ?";

	public static final String GET_RECORD_BY_MATERIALID_TASK = "SELECT * FROM sample_task_material_record WHERE material_id = ? AND task_id = ?";

}
