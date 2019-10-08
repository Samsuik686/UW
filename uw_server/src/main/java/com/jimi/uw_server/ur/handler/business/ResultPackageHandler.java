package com.jimi.uw_server.ur.handler.business;

import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.ResultPackage;
import com.jimi.uw_server.ur.processor.MessageHandler;
import com.jimi.uw_server.ur.thread.TestThread;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


/**
 * 订餐请求处理器，单例类可被共享
 * <br>
 * <b>2019年9月26日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
@Sharable
public class ResultPackageHandler extends SimpleChannelInboundHandler<ResultPackage> {

	public static final ResultPackageHandler me = new ResultPackageHandler();


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ResultPackage msg) throws Exception {

		try {
			System.out.println("结果已收到");
			MessageHandler.me.handleResultPackage(msg);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		AckPackage ackPackage = new AckPackage(msg.getCmdid());
		ctx.channel().writeAndFlush(ackPackage);
	}

}
