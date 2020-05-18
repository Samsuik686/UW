/**  
*  
*/  
package com.jimi.uw_server.constant.sql;

/**  
 * <p>Title: MaterialSQL</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月8日
 *
 */
public class MaterialSQL {

	public static final String GET_MATERIAL_BY_BOX_SQL = "SELECT material.* FROM material WHERE remainder_quantity > 0 AND box = ?";

	public static final String GET_MATERIAL_BY_COMPANY_SQL = "SELECT material.* FROM material WHERE remainder_quantity > 0 AND company_id = ?";
	
	public static final String GET_MATERIAL_BY_SUPPLIER_SQL = "SELECT material.* FROM material INNER JOIN material_type ON material_type.id = material.type WHERE remainder_quantity > 0 AND material_type.supplier = ?";

	public static final String GET_MATERIAL_BY_MATERIAL_TYPE_AND_COMPANY_SQL = "SELECT material.* FROM material INNER JOIN material_type ON material_type.id = material.type WHERE remainder_quantity > 0 AND material_type.id = ?";

	//获取过期物料
	public static final String GET_OVERDUE_MATERIAL_SQL = "SELECT a.* FROM (SELECT DISTINCT material_type.*, supplier.name AS supplier_name, company.nickname AS company_nickname FROM material_type INNER JOIN material INNER JOIN supplier INNER JOIN company ON material_type.id = material.type AND material_type.supplier = supplier.id AND supplier.company_id = company.id WHERE material_type.enabled = 1 AND material.remainder_quantity > 0 AND material.store_time < ? AND material_type.type = ?) a ";

	public static final String GET_ENTITIES_SELECT_SQL = "SELECT material.id AS Material_Id,material.company_id AS Material_CompanyId,material.type AS Material_Type,material.box AS Material_Box,material.row AS Material_Row,material.col AS Material_Col,material.store_time AS Material_StoreTime,material.is_in_box AS Material_IsInBox,material.is_repeated AS Material_IsRepeated,material.manufacturer AS Material_Manufacturer,material.cycle AS Material_Cycle,material.status AS Material_Status,material.print_time AS Material_PrintTime,material.cut_task_log_id AS Material_CutTaskLogId,material.remainder_quantity AS Material_RemainderQuantity,material.production_time AS Material_ProductionTime, material_type.id AS MaterialType_Id,material_type.enabled AS MaterialType_Enabled,material_type.type AS MaterialType_Type,material_type.supplier AS MaterialType_Supplier,material_type.no AS MaterialType_No,material_type.specification AS MaterialType_Specification,material_type.thickness AS MaterialType_Thickness,material_type.radius AS MaterialType_Radius,material_type.designator AS MaterialType_Designator," + 
			"material_box.id AS MaterialBox_Id,material_box.enabled AS MaterialBox_Enabled,material_box.company_id AS MaterialBox_CompanyId,material_box.type AS MaterialBox_Type,material_box.row AS MaterialBox_Row,material_box.col AS MaterialBox_Col,material_box.status AS MaterialBox_Status,material_box.area AS MaterialBox_Area,material_box.height AS MaterialBox_Height,material_box.is_on_shelf AS MaterialBox_IsOnShelf,material_box.supplier AS MaterialBox_Supplier,material_box.update_time AS MaterialBox_UpdateTime";
	
	public static final String GET_ENTITIES_BY_TYPE_EXCEPT_SELECT_SQL = " FROM material INNER JOIN material_box INNER JOIN material_type ON material_type.id = material.type AND material.box = material_box.id WHERE material.type = ? AND material.remainder_quantity > 0  AND material_box.enabled = 1 AND material_type.enabled = 1 AND material_type.supplier = ?";

	public static final String GET_ENTITIES_BY_BOX_EXCEPT_SELECT_SQL = " FROM material INNER JOIN material_box INNER JOIN material_type ON material_type.id = material.type AND material.box = material_box.id WHERE material.box = ? AND material.remainder_quantity > 0 AND material_box.enabled = 1 AND material_type.enabled = 1 AND material_box.supplier = ?";

	public static final String GET_ENTITIES_BY_TYPE_AND_BOX_EXCEPT_SELECT_SQL = " FROM material join material_box join  material_type ON material_type.id = material.type AND material.box = material_box.id WHERE material.type = ? and material.box = ? AND material.remainder_quantity > 0 AND material.company_id = ? AND material_box.enabled = 1";

	//根据物料类型获取贵重仓物料详情
	public static final String GET_PRECIOUS_MATERIAL_ENTITIES_SQL = "SELECT material.id AS Material_Id,material.company_id AS Material_CompanyId,material.type AS Material_Type,material.box AS Material_Box,material.row AS Material_Row,material.col AS Material_Col,material.store_time AS Material_StoreTime,material.is_in_box AS Material_IsInBox,material.is_repeated AS Material_IsRepeated,material.manufacturer AS Material_Manufacturer,material.cycle AS Material_Cycle,material.status AS Material_Status,material.print_time AS Material_PrintTime,material.cut_task_log_id AS Material_CutTaskLogId,material.remainder_quantity AS Material_RemainderQuantity,material.production_time AS Material_ProductionTime,material_type.id AS MaterialType_Id,material_type.enabled AS MaterialType_Enabled,material_type.type AS MaterialType_Type,material_type.supplier AS MaterialType_Supplier,material_type.no AS MaterialType_No,material_type.specification AS MaterialType_Specification,material_type.thickness AS MaterialType_Thickness,material_type.radius AS MaterialType_Radius,material_type.designator AS MaterialType_Designator FROM material INNER JOIN material_type ON material.type = material_type.id WHERE material.type = ? AND material.remainder_quantity > 0 AND material. STATUS = ?";

	//获取普通仓物料报表
	public static final String GET_REGUALR_MATERIAL_REPORT_SQL = "SELECT material_type.id as id, material_type.`no` as `no`, material_type.specification as specification,  SUM(material.remainder_quantity) AS quantity FROM material INNER JOIN material_type ON material_type.id = material.type WHERE material_type.type = ? AND material.company_id = ? AND material_type.supplier = ? GROUP BY material.type ORDER BY material.type ASC";
	//获取贵重仓物料报表
	public static final String GET_PRECIOUS_MATERIAL_REPORT_SQL = "SELECT material_type.id as id, material_type.`no` as `no`, material_type.specification as specification, SUM(material.remainder_quantity) AS quantity, material_type.designator FROM material INNER JOIN material_type ON  material_type.id = material.type WHERE material_type.type = ? AND material.company_id = ? AND material_type.supplier = ? GROUP BY material.type ORDER BY  material.type ASC";

	//获取物料详细报表
	public static final String GET_MATERIAL_DETIAL_REPORT_SQL = "SELECT supplier.`name` AS supplier_name, material_type.id AS material_type_id, material_type.`no` AS `no`, material_type.specification AS specification, material_box.id AS box_id, material_box.area AS area, material_box. ROW AS X, material_box.col AS Y, material_box.height AS Z, material.id AS material_id, material.col AS col, material.`row` AS `row`, material.production_time AS production_time, material.remainder_quantity AS quantity FROM supplier INNER JOIN material_type INNER JOIN material_box INNER JOIN material ON supplier.id = material_type.supplier AND supplier.id = material_box.supplier AND material_type.id = material.type AND material.box = material_box.id WHERE material_type.type = ? AND material_type.supplier = ? AND material.company_id = ? AND material_type.enabled = 1 AND remainder_quantity > 0 ORDER BY material_type.id, material_box.id";
	//获取不在盒内物料
	public static final String GET_NOT_IN_BOX_MATERIAL_SQL = "SELECT material.id FROM material INNER JOIN material_box ON material.box = material_box.id WHERE material.remainder_quantity > 0 AND material_box.supplier = ? AND material_box.enabled = 1 AND material.is_in_box = 0";
	
	//获取所有普通仓物料，通过客户
	public static final String GET_ALL_REGULAR_MATERIAL_BY_SUPPLIER_SQL = "SELECT material.* FROM material_box INNER JOIN material ON material_box.id = material.box WHERE material.remainder_quantity > 0 AND material_box.supplier = ? AND material_box.enabled = 1";

	//获取所有贵重仓物料，通过客户
	public static final String GET_ALL_MATERIAL_BY_SUPPLIER_AND_WAREHOUSE_SQL = "SELECT material.* FROM material_type INNER JOIN material ON material_type.id = material.type WHERE material.remainder_quantity > 0 AND material_type.supplier = ? AND material_type.enabled = 1 AND material_type.type = ? AND material.status = ?";
	//获取物料类型和物料信息，通过料盒号
	public static final String GET_MATERIAL_INFO_BY_BOX = "SELECT material.id as id, material.row as row, material.col as col, material.type as material_type_id, material.remainder_quantity as quantity, material.production_time as production_time, material_type.`no` as `no`, material_type.specification as specification, supplier.id as supplier_id, supplier.`name` as supplier_name FROM material INNER JOIN material_type INNER JOIN supplier ON material.type = material_type.id AND supplier.id = material_type.supplier WHERE material.box = ? AND material.is_in_box = 1 AND remainder_quantity > 0";

	public static final String GET_MATERIAL_BY_MATERIAL_TYPE_ORDER_TIME_SQL = "SELECT * FROM material WHERE remainder_quantity > 0 AND type = ? AND is_in_box = b'1' ORDER BY production_time, id ASC";

	public static final String GET_MATERIAL_BY_MATERIAL_TYPE_AND_NOT_BOX_ORDER_TIME_SQL = "SELECT * FROM material WHERE remainder_quantity > 0 AND type = ? AND is_in_box = b'1' AND box != ? ORDER BY production_time, id ASC";

	public static final String GET_MATERIAL_BY_TYPE_AND_BOX_ORDER_TIME_SQL = "SELECT material.* FROM material WHERE type = ? AND box = ? AND remainder_quantity > 0 AND is_in_box = 1 ORDER BY production_time ASC";
	
}
