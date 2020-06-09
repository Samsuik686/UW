package com.jimi.uw_server.constant.sql;

public class DestinationSQL {

	public static final String GET_DESTINATION_SQL = "SELECT * FROM destination WHERE enabled = 1";

	public static final String GET_SHARE_DESTINATION_SQL = "SELECT * FROM destination WHERE enabled = 1 AND company_id IS NULL";

	public static final String GET_DESTINATION_BY_NAME_AND_COMPANY_SQL = "SELECT * FROM destination WHERE name = ? AND company_id = ? AND enabled = 1";

	public static final String GET_ENABLED_DESTINATION_BY_NAME_AND_COMPANY_SQL = "SELECT * FROM destination WHERE name = ? AND company_id = ? AND enabled = 1";

	public static final String GET_DESTINATION_BY_COMPANY_SQL = "SELECT * FROM destination WHERE company_id = ? AND enabled = 1";

	public static final String GET_DESTINATION_BY_ID_SQL = "SELECT * FROM destination WHERE id = ? AND enabled = 1";

	public static final String GET_UW_DESTINATION_SQL = "SELECT * FROM destination WHERE enabled = 1 AND id = 0";

}
