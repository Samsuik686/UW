package com.jimi.uw_server.constant.sql;

public class SupplierSQL {

	public static final String GET_SUPPLIER_BY_NAME_SQL = "SELECT * FROM supplier WHERE name = ? AND enabled = 1";
	
	public static final String GET_SUPPLIER_BY_COMPANY_SQL = "SELECT * FROM supplier WHERE company_id = ? AND enabled = 1";

	public static final String GET_MATERIAL_BY_SUPPLIER_SQL= "SELECT * FROM material INNER JOIN material_type ON material.type = material_type.id WHERE remainder_quantity > 0 AND material_type.supplier = ? AND material_type.enabled = 1";

	public static final String GET_FORMER_SUPPLIER_SQL = "SELECT * FROM former_supplier WHERE former_name = ?";
}
