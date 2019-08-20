package com.jimi.uw_server.constant.sql;

public class SupplierSQL {

	public static String GET_SUPPLIER_BY_NAME = "SELECT * FROM supplier WHERE name = ? AND enabled = 1";
}
