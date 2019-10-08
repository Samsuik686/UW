package com.jimi.uw_server.ur.handler.business;

import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.IOPackage;
import com.jimi.uw_server.ur.processor.MessageHandler;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


/**
 * 异常信息处理器，单例类可被共享
 * <br>
 * <b>2019年9月27日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
@Sharable
public class IOPackageHandler extends SimpleChannelInboundHandler<IOPackage> {

	public static final IOPackageHandler me = new IOPackageHandler();


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IOPackage msg) throws Exception {
		AckPackage ackPackage = new AckPackage(msg.getCmdid());
		ctx.channel().writeAndFlush(ackPackage);
		try {
			System.out.println("入库包已收到");
			MessageHandler.me.handleInPackage(msg);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

	}

}
