package com.jimi.uw_server.ur.handler.business;

import java.util.Date;

import com.jimi.uw_server.ur.constant.Constant;
import com.jimi.uw_server.ur.entity.SessionBox;

import cc.darhao.dautils.api.DateUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;


@Sharable
public class ConnectionHandler extends ChannelInboundHandlerAdapter {

	public static final ConnectionHandler me = new ConnectionHandler();


	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("[UW机械臂服务端] - " + DateUtil.HHmmssSSS(new Date()) + " - 监听到一个连接");
	}


	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		if (ctx.channel().attr(Constant.LOGIN_NAME).get() != null) {
			SessionBox.remove(ctx.channel().attr(Constant.LOGIN_NAME).get());
		}
		System.out.println("[UW机械臂服务端] - " + DateUtil.HHmmssSSS(new Date()) + " - 一个连接已断开");
	}

}
