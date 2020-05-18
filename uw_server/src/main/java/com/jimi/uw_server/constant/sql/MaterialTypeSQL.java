package com.jimi.uw_server.constant.sql;

public class MaterialTypeSQL {

	public static final String GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_SQL = "SELECT * FROM material_type WHERE no = ? AND supplier = ? AND enabled  = 1";

	public static final String GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_AND_TYPE_SQL = "SELECT * FROM material_type WHERE no = ? AND supplier = ? AND type = ? AND enabled = 1";
	
	public static final String GET_MATERIAL_STORE_BY_MATERIAL_TYPE_AND_STATUS_SQL = "SELECT material.type, SUM(material.remainder_quantity) AS quantity FROM material WHERE material.remainder_quantity > 0 AND material.`status` = ? AND material.type = ? GROUP BY material.type";

	public static final String GET_MATERIAL_TYPE_BY_DESIGNATOR_AND_TYPE_SQL = "SELECT * FROM material_type WHERE supplier = ? AND designator = ? AND type = ? AND enabled = 1";

	public static final String SET_MATERIAL_TYPE_UNABLED_BY_SUPPLIER_SQL = "UPDATE material_type SET material_type.enabled = 0 WHERE material_type.supplier = ?";
   
	public static final String GET_MATERIAL_TYPE_BY_SUPPLIER_AND_TYPE_SQL = "SELECT * FROM material_type WHERE supplier = ? AND type = ? AND enabled = 1";
}
