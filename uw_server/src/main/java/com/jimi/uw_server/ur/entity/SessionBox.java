package com.jimi.uw_server.ur.entity;

import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class SessionBox {

	static Map<String, ChannelHandlerContext> map = new ConcurrentHashMap<>();


	public synchronized static void add(String name, ChannelHandlerContext ctx) {

		map.put(name, ctx);
	}


	public synchronized static void remove(String name) {
		ChannelHandlerContext context = map.remove(name);
		if (context != null) {
			context.close();
		}

	}


	public static ChannelHandlerContext getChannelHandlerContext(String name) {
		if (map.get(name) == null || !map.get(name).channel().isActive()) {
			map.remove(name);
			return null;
		}
		return map.get(name);
	}
}
