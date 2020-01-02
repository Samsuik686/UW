package com.jimi.uw_server.ur.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;


public class SessionBox {

	static Map<String, ChannelHandlerContext> map = new ConcurrentHashMap<>();


	public static void add(String name, ChannelHandlerContext ctx) {

		map.put(name, ctx);
	}


	public static void remove(String name) {
		map.remove(name);
	}


	public static ChannelHandlerContext getChannelHandlerContext(String name) {

		return map.get(name);
	}
}
