package com.jimi.uw_server.ur.handler.business;

import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.AckResponseManager;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;


@Sharable
public class AckPackageHandler extends SimpleChannelInboundHandler<AckPackage> {

	public static final AckPackageHandler me = new AckPackageHandler();


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, AckPackage msg) throws Exception {

		AckResponseManager.countDownAck(msg.getCmdId());

	}

}
