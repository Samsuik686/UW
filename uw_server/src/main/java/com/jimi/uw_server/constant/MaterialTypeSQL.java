package com.jimi.uw_server.constant;

public class MaterialTypeSQL {

	public static final String GET_MATERIAL_TYPE_BY_SUPPLIER_AND_NAME = "SELECT * FROM material_type WHERE no = ? AND supplier = ? AND enabled  = 1";
}
