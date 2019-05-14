package com.jimi.uw_server.interceptor;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jfinal.handler.Handler;
import com.jfinal.kit.StrKit;

/**
 * WebSocket拦截器，需要该拦截器来实现ws
 * <br>
 * <b>2019年3月20日</b>
 * @author 魔法王者安琪拉 
 * 原文：https://my.oschina.net/u/136848/blog/781896
 */
public class WebSocketHandler extends Handler {

	private Pattern filterUrlRegxPattern;

	
	public WebSocketHandler(String filterUrlRegx) {
		if (StrKit.isBlank(filterUrlRegx))
			throw new IllegalArgumentException("The para filterUrlRegx can not be blank.");
		filterUrlRegxPattern = Pattern.compile(filterUrlRegx);
	}

	
	@Override
	public void handle(String target, HttpServletRequest request, HttpServletResponse response, boolean[] isHandled) {
		if (filterUrlRegxPattern.matcher(target).find())
			return;
		else
			next.handle(target, request, response, isHandled);
	}

}
