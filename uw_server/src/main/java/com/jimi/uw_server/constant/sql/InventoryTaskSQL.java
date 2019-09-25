package com.jimi.uw_server.constant.sql;

public class InventoryTaskSQL {

	public static final String GET_UN_INVENTORY_LOG_BY_TASKID = "SELECT inventory_log.* FROM inventory_log INNER JOIN material ON inventory_log.material_id = material.id WHERE inventory_log.task_id = ? AND inventory_log.inventory_time is null AND inventory_log.enabled = 1";

	public static final String GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER = "SELECT * FROM task where state = 2 and type = 2 and supplier = ? AND warehouse_type = ?";

}
