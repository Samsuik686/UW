package com.jimi.uw_server.ur.constant;

import io.netty.util.AttributeKey;


public class Constant {

	public final static AttributeKey<String> LOGIN_NAME = AttributeKey.newInstance("LOGIN_NAME");

	public final static String CMD_END_FLAG = "#";

	public final static int CMD_MAX_BYTES_LENGTH = 4096;
}
