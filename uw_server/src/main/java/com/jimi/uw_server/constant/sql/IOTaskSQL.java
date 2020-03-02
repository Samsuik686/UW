package com.jimi.uw_server.constant.sql;

public class IOTaskSQL {

	public static final String GET_PACKING_LIST_ITEM_BY_TASKID = "SELECT * FROM packing_list_item WHERE packing_list_item.task_id = ?";

	public static final String GET_PACKING_LIST_ITEM_BY_TASKID_AND_NO = "SELECT packing_list_item.* FROM packing_list_item INNER JOIN material_type ON packing_list_item.material_type_id = material_type.id WHERE packing_list_item.task_id = ? AND material_type.`no` = ? AND material_type.type = ? AND material_type.enabled = 1";

	public static final String GET_OLDER_MATERIAL_BY_BOX_AND_TIME = "select * from material where type = ? and production_time < ? and remainder_quantity > 0 and status = ? ORDER BY production_time asc";

	public static final String GET_OLDER_MATERIAL_BY_BOX_AND_TIME_AND_NOTNULLCYCLE = "select * from material where type = ? and production_time < ? and remainder_quantity > 0 and status = ? and cycle is not null ORDER BY production_time asc";

	public static final String GET_PRECIOUS_OUT_MATERIAL_SQL = "SELECT task_log.id, material_id AS materialId, quantity, production_time AS productionTime, is_in_box AS isInBox, remainder_quantity  AS remainderQuantity FROM task_log JOIN material ON  task_log.material_id = material.id WHERE task_log.packing_list_item_id = ? ORDER BY task_log.id DESC";

	public static final String GET_PRECIOUS_IOTASK_INFOS = "SELECT A.*, B.* FROM ( SELECT packing_list_item.material_type_id AS PackingListItem_MaterialTypeId, packing_list_item.id AS PackingListItem_Id, packing_list_item.task_id AS PackingListItem_TaskId, packing_list_item.quantity AS PackingListItem_Quantity, packing_list_item.finish_time AS PackingListItem_FinishTime, material_type.type AS MaterialType_Type, material_type.enabled AS MaterialType_Enabled, material_type.id AS MaterialType_Id, material_type.supplier AS MaterialType_Supplier, material_type. NO AS MaterialType_No, material_type.specification AS MaterialType_Specification, material_type.thickness AS MaterialType_Thickness, material_type.radius AS MaterialType_Radius, material_type.designator AS MaterialType_Designator, supplier. NAME AS Supplier_Name, supplier.enabled AS Supplier_Enabled, supplier.id AS Supplier_Id FROM packing_list_item JOIN material_type JOIN supplier ON packing_list_item.material_type_id = material_type.id AND material_type.supplier = supplier.id WHERE packing_list_item.task_id = ? AND packing_list_item.finish_time IS NULL ) A LEFT JOIN (SELECT task_log.time AS TaskLog_Time, task_log.id AS TaskLog_Id, task_log.packing_list_item_id AS TaskLog_PackingListItemId, task_log.quantity AS TaskLog_Quantity, task_log.destination AS TaskLog_Destination, task_log.material_id AS TaskLog_MaterialId, task_log.operator AS TaskLog_Operator, material.remainder_quantity AS Material_RemainderQuantity, material.production_time AS Material_ProductionTime, material.`status` AS Material_Status  FROM task_log INNER JOIN material INNER JOIN packing_list_item ON task_log.material_id = material.id AND task_log.packing_list_item_id = packing_list_item.id WHERE packing_list_item.task_id = ? ) B ON A.PackingListItem_Id = B.TaskLog_PackingListItemId ORDER BY A.PackingListItem_Id ASC";

	public static final String GET_PRECIOUS_IO_TASK_UW_STORE = "SELECT packing_list_item.id AS PackingListItem_Id, SUM(material.remainder_quantity) AS uwStore FROM packing_list_item JOIN material ON packing_list_item.material_type_id = material.type WHERE packing_list_item.task_id = ? AND material.remainder_quantity > 0 AND material.`status` = 0 GROUP BY material.type";

	public static final String GET_PROBLEM_IOTASK_ITEM_BY_TASK_ID = "SELECT packing_list_item.id AS PackingListItem_Id, packing_list_item.quantity AS PlanQuantity, SUM(task_log.quantity) AS actuallyQuantity FROM packing_list_item LEFT JOIN task_log  ON packing_list_item.id = task_log.packing_list_item_id  WHERE packing_list_item.task_id = ? GROUP BY packing_list_item.id HAVING SUM(task_log.quantity) < packing_list_item.quantity OR SUM(task_log.quantity) IS NULL";

	public static final String GET_CAN_FINSH_IOTASK_ITEM_BY_TASK_ID = "SELECT packing_list_item.* FROM packing_list_item INNER JOIN task_log ON packing_list_item.id = task_log.packing_list_item_id WHERE packing_list_item.task_id = ? AND packing_list_item.finish_time IS NULL GROUP BY packing_list_item.id HAVING SUM(task_log.quantity) = packing_list_item.quantity";

	public static final String GET_UNFINSH_PROBLEM_IOTASK_ITEM_BY_TASK_ID = "SELECT packing_list_item.id AS PackingListItem_Id, packing_list_item.quantity AS PlanQuantity, SUM(task_log.quantity) AS actuallyQuantity FROM packing_list_item LEFT JOIN task_log  ON packing_list_item.id = task_log.packing_list_item_id  WHERE packing_list_item.task_id = ? AND packing_list_item.finish_time IS NULL GROUP BY packing_list_item.id HAVING SUM(task_log.quantity) != packing_list_item.quantity OR SUM(task_log.quantity) IS NULL";

	public static final String GET_CUTTING_PACKING_LIST_ITEM_BY_TASK_ID = "SELECT packing_list_item.* FROM material INNER JOIN task_log INNER JOIN packing_list_item ON task_log.material_id = material.id AND packing_list_item.id = task_log.packing_list_item_id WHERE material.cut_task_log_id IS NOT NULL AND packing_list_item.task_id = ?";
	
	public static final String GET_IN_WRONG_IOTASK_ITEM_BY_TASK_ID = "SELECT packing_list_item.id AS PackingListItem_Id, packing_list_item.quantity AS PlanQuantity, SUM(task_log.quantity) AS actuallyQuantity FROM packing_list_item LEFT JOIN task_log  ON packing_list_item.id = task_log.packing_list_item_id  WHERE packing_list_item.task_id = ? AND packing_list_item.finish_time IS NULL GROUP BY packing_list_item.id HAVING SUM(task_log.quantity) != packing_list_item.quantity";

	public static final String GET_OUT_OVER_PRECIOUS_IOTASK_ITEM_BY_TASKID = "SELECT packing_list_item.id AS PackingListItem_Id, packing_list_item.quantity AS PlanQuantity, SUM(task_log.quantity) AS actuallyQuantity FROM packing_list_item LEFT JOIN task_log  ON packing_list_item.id = task_log.packing_list_item_id  WHERE packing_list_item.task_id = ? AND packing_list_item.finish_time IS NULL HAVING SUM(task_log.quantity) > packing_list_item.quantity";

	public static final String GET_OUT_LACK_PRECIOUS_IOTASK_ITEM_BY_TASKID = "SELECT packing_list_item.id AS PackingListItem_Id, packing_list_item.quantity AS PlanQuantity, SUM(task_log.quantity) AS actuallyQuantity FROM packing_list_item LEFT JOIN task_log  ON packing_list_item.id = task_log.packing_list_item_id  WHERE packing_list_item.task_id = ? AND packing_list_item.finish_time IS NULL HAVING SUM(task_log.quantity) < packing_list_item.quantity AND sum(task_log.quantity) != 0";

	public static final String GET_IOTASK_CUTTING_PACKING_LIST_ITEM = "SELECT packing_list_item.id AS PackingListItem_Id FROM material INNER JOIN packing_list_item INNER JOIN task_log ON task_log.material_id = material.id AND packing_list_item.id = task_log.packing_list_item_id AND task_log.id = material.cut_task_log_id WHERE packing_list_item.task_id = ? AND material.status = 1";

	public static final String GET_IOTASK_CUTTING_PACKING_LIST_ITEM_BY_ID = "SELECT packing_list_item.id AS PackingListItem_Id FROM material INNER JOIN packing_list_item INNER JOIN task_log ON task_log.material_id = material.id AND packing_list_item.id = task_log.packing_list_item_id  AND task_log.id = material.cut_task_log_id WHERE packing_list_item.id = ? AND material.status = 1";

	public static final String GET_DIFFERENCE_QUANTITY_OF_IOTASK = "select material.id, (material.remainder_quantity - task_log.quantity) AS quantity from task_log INNER JOIN material INNER JOIN packing_list_item ON packing_list_item.id = task_log.packing_list_item_id AND task_log.material_id = material.id WHERE packing_list_item.task_id = ? AND packing_list_item.finish_time IS NULL";

	public static final String GET_ALL_UNFINISH_PRECIOUS_IOTASK_ITEM = "SELECT * FROM packing_list_item WHERE packing_list_item.task_id = ? AND packing_list_item.finish_time IS NULL ";

	public static final String GET_PROBLEM_PRECIOUS_IOTASK_ITEM_BY_ID = "SELECT packing_list_item.id AS PackingListItem_Id, packing_list_item.quantity AS PlanQuantity, SUM(task_log.quantity) AS actuallyQuantity FROM packing_list_item LEFT JOIN task_log  ON packing_list_item.id = task_log.packing_list_item_id  WHERE packing_list_item.id = ? HAVING SUM(task_log.quantity) > packing_list_item.quantity";

	public static final String GET_UNFINISH_PRECIOUS_IOTASK_ITEM_BY_TASK_ID = "SELECT packing_list_item.id AS PackingListItem_Id, packing_list_item.quantity AS PlanQuantity, SUM(task_log.quantity) AS actuallyQuantity FROM packing_list_item LEFT JOIN task_log ON packing_list_item.id = task_log.packing_list_item_id WHERE packing_list_item.task_id = ? GROUP BY packing_list_item.id HAVING SUM(task_log.quantity) != packing_list_item.quantity OR SUM(task_log.quantity) IS NULL";

	public static final String GET_MATERIAL_AND_OUTQUANTITY_BY_PACKING_LIST_ITEM_ID = "SELECT material.*, task_log.quantity AS outQuantity FROM material INNER JOIN task_log ON task_log.material_id = material.id WHERE task_log.packing_list_item_id = ?";

	public static final String GET_MATERIAL_BY_PACKING_LIST_ITEM_ID = "SELECT material.* FROM material INNER JOIN task_log ON task_log.material_id = material.id WHERE task_log.packing_list_item_id = ?";
	
	public static final String GET_UNFINISH_PACKING_LIST_ITEM = "SELECT * FROM packing_list_item WHERE packing_list_item.task_id = ? AND packing_list_item.finish_time IS NULL";

	public static final String GET_OLDEST_MATERIAL_UW_STORE = "SELECT material.*, packing_list_item.id AS PackingListItem_Id FROM material INNER JOIN packing_list_item ON packing_list_item.material_type_id = material.type WHERE remainder_quantity > 0 AND packing_list_item.task_id = ? AND status = 0 ORDER BY type, production_time ASC";

	public static final String GET_TASK_BY_NAME = "SELECT * FROM task WHERE task.file_name = ? and task.state != ?";

}
