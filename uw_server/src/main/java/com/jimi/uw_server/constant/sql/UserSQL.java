package com.jimi.uw_server.constant.sql;


public class UserSQL {

	public static final String GET_USER_BY_NAME = "SELECT * FROM user WHERE name = ? AND enabled = 1";
}
