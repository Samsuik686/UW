package com.jimi.uw_server.constant;


public class DestinationSQL {
	
	public static String GET_DESTINATION_BY_NAME = "SELECT * FROM destination WHERE name = ? AND enabled = 1";
}
