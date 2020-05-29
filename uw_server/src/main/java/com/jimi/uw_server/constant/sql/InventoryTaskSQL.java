package com.jimi.uw_server.constant.sql;

public class InventoryTaskSQL {
	
	public static final String GET_INVENTORY_TASK_BY_SUPPLIER = "SELECT * FROM task WHERE type = ? AND supplier = ? AND state != 1 AND state != 4 AND warehouse_type = ? ORDER BY create_time DESC";

	public static final String GET_USEFUL_TASK_BY_TYPE_SUPPLIER = "select * from task where task.type = ? and task.state < 3 and task.supplier = ? and warehouse_type = ?";

	public static final String GET_UNSTART_INVENTORY_TASK_BY_SUPPLIER = "SELECT * FROM task INNER JOIN inventory_task_base_info ON task.id = inventory_task_base_info.task_id where state = 1 and type = 2 and supplier = ? and inventory_task_base_info.destination_id = ? and warehouse_type = ? order by create_time desc";

	public static final String GET_RUNNING_TASK_BY_SUPPLIER = "SELECT * FROM task where state = 2 and supplier = ? and warehouse_type = ?";

	public static final String GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER = "SELECT * FROM task where state = 2 and type = 2 and supplier = ? AND warehouse_type = ?";

	public static final String GET_UNCOVER_INVENTORY_LOG_BY_TASKID = "SELECT * FROM inventory_log WHERE task_id = ? AND enabled = 1";

	public static final String GET_INVENTORY_TASK_BASE_INFO_BY_TASKID = "SELECT * FROM inventory_task_base_info WHERE task_id = ?";
	
	public static final String GET_INVENTORY_TASK_BASE_INFO_BY_TASKID_AND_WHID = "SELECT * FROM inventory_task_base_info WHERE task_id = ? AND destination_id = ?";

	public static final String UPDATE_MATERIAL_RETURN_RECORD_UNENABLED = "UPDATE material_return_record SET enabled = 0 WHERE time <= ? AND enabled = 1 AND wh_id = ? AND material_type_id IN (SELECT id from material_type WHERE material_type.supplier = ? AND material_type.enabled = 1 AND material_type.type = ?)";

	public static final String GET_INVENTORY_LOG_BY_TASKID_AND_MATERIALID = "SELECT * FROM inventory_log WHERE inventory_log.task_id = ? AND inventory_log.material_id = ?";

	public static final String UPDATE_INVENTORY_LOG_UNCOVER = "UPDATE inventory_log SET enabled = 0 WHERE different_num = 0 AND task_id = ?";

	public static final String GET_UN_INVENTORY_LOG_BY_TASKID = "SELECT inventory_log.*,material.type AS material_type_id FROM inventory_log INNER JOIN material ON inventory_log.material_id = material.id WHERE inventory_log.task_id = ? AND inventory_log.inventory_time is null AND inventory_log.enabled = 1 GROUP BY material.type";
	
	public static final String GET_UN_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE = "SELECT inventory_log.*,material.type AS material_type_id FROM inventory_log INNER JOIN material ON inventory_log.material_id = material.id WHERE inventory_log.task_id = ? AND inventory_log.inventory_time is null AND inventory_log.enabled = 1 AND material.type = ?";

	public static final String GET_INVENTORY_LOG_BY_TASKID = "SELECT * FROM inventory_log WHERE inventory_log.task_id = ?";

	public static final String GET_UNCOVER_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE = "SELECT inventory_log.* FROM inventory_log INNER JOIN material ON inventory_log.material_id = material.id WHERE material.type = ? AND inventory_log.task_id = ? AND inventory_log.enabled = 1";

	public static final String GET_INVENTORY_TASK_DESTINATION_BY_TASKID = "SELECT destination.* FROM inventory_task_base_info INNER JOIN destination ON destination.id = inventory_task_base_info.destination_id where inventory_task_base_info.task_id = ? and inventory_task_base_info.destination_id > 0"; 
	
	public static final String GET_INVENTORY_TASK_BEAS_INFO_BY_TASKID = "SELECT destination.`name` AS destinationName, inventory_task_base_info.check_operator AS checkOperator, inventory_task_base_info.check_time AS checkTime, inventory_task_base_info.finish_time AS finishTime, inventory_task_base_info.finish_operator AS finishOperator FROM destination INNER JOIN inventory_task_base_info INNER JOIN task ON destination.id = inventory_task_base_info.destination_id AND task.id = inventory_task_base_info.task_id WHERE task.id = ?"; 

	public static final String GET_UW_INVENTORY_TASK_DETIALS_INFO = "SELECT material_type.id AS material_type_id, material_type.`no`, material_type.specification, material_type.designator, material.id AS material_id, material.cycle AS material_cycle, inventory_log.task_id, inventory_task_base_info.check_time AS check_time, inventory_task_base_info.check_operator AS check_operator, before_num AS before_num, actural_num AS actural_num, different_num AS different_num, supplier.id AS supplier_id, supplier.`name` AS supplier_name, inventory_log.inventory_operatior AS inventory_operatior FROM inventory_log INNER JOIN inventory_task_base_info INNER JOIN material INNER JOIN material_type INNER JOIN supplier ON inventory_log.task_id = inventory_task_base_info.task_id AND inventory_log.material_id = material.id AND material_type.id = material.type AND material_type.supplier = supplier.id WHERE inventory_log.task_id = ? AND inventory_task_base_info.destination_id = ? ORDER BY material_type.`no` DESC";

	public static final String GET_UW_INVENTORY_TASK_DETIALS_INFO_BY_NO = "SELECT material_type.id AS material_type_id, material_type.`no`, material_type.specification, material_type.designator, material.id AS material_id, material.cycle AS material_cycle, inventory_log.task_id, inventory_task_base_info.check_time AS check_time, inventory_task_base_info.check_operator AS check_operator, before_num AS before_num, actural_num AS actural_num, different_num AS different_num, supplier.id AS supplier_id, supplier.`name` AS supplier_name, inventory_log.inventory_operatior AS inventory_operatior FROM inventory_log INNER JOIN inventory_task_base_info INNER JOIN material INNER JOIN material_type INNER JOIN supplier ON inventory_log.task_id = inventory_task_base_info.task_id AND inventory_log.material_id = material.id AND material_type.id = material.type AND material_type.supplier = supplier.id WHERE inventory_log.task_id = ? AND inventory_task_base_info.destination_id = ? AND material_type.`no` LIKE ? ORDER BY material_type.`no` DESC";

	public static final String GET_UW_INVENTORY_TASK_INFO = "SELECT material_type.id AS material_type_id, material_type.`no`, material_type.specification, material_type.designator, inventory_log.task_id, inventory_task_base_info.check_time AS check_time, inventory_task_base_info.check_operator AS check_operator, SUM(before_num) AS before_num, SUM(actural_num) AS actural_num, SUM(different_num) AS different_num, supplier.id AS supplier_id, supplier.`name` AS supplier_name, inventory_log.inventory_operatior AS inventory_operatior FROM inventory_log INNER JOIN inventory_task_base_info INNER JOIN material INNER JOIN material_type INNER JOIN supplier ON inventory_log.task_id = inventory_task_base_info.task_id AND inventory_log.material_id = material.id AND material_type.id = material.type AND material_type.supplier = supplier.id WHERE inventory_log.task_id = ? AND inventory_task_base_info.destination_id = ? GROUP BY material.type ORDER BY material_type.`no` DESC";

	public static final String GET_UW_INVENTORY_TASK_INFO_BY_NO = "SELECT material_type.id AS material_type_id, material_type.`no`, material_type.specification, material_type.designator, inventory_log.task_id, inventory_task_base_info.check_time AS check_time, inventory_task_base_info.check_operator AS check_operator, SUM(before_num) AS before_num, SUM(actural_num) AS actural_num, SUM(different_num) AS different_num, supplier.id AS supplier_id, supplier.`name` AS supplier_name, inventory_log.inventory_operatior AS inventory_operatior FROM inventory_log INNER JOIN inventory_task_base_info INNER JOIN material INNER JOIN material_type INNER JOIN supplier ON inventory_log.task_id = inventory_task_base_info.task_id AND inventory_log.material_id = material.id AND material_type.id = material.type AND material_type.supplier = supplier.id WHERE inventory_log.task_id = ? AND inventory_task_base_info.destination_id = ? AND material_type.`no` like ? GROUP BY material.type ORDER BY material_type.`no` DESC";

}
