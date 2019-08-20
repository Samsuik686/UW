package com.jimi.uw_server.constant.sql;

public class MaterialTypeSQL {

	public static final String GET_MATERIAL_TYPE_BY_SUPPLIER_AND_NAME = "SELECT * FROM material_type WHERE no = ? AND supplier = ? AND enabled  = 1";

	public static final String GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_AND_TYPE_SQL = "SELECT * FROM material_type WHERE no = ? AND supplier = ? AND type = ? AND enabled = 1";

}
