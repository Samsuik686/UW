package com.jimi.uw_server.ur.handler.business;

import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.MaterialPositionInfoPackage;
import com.jimi.uw_server.ur.entity.ScanMaterialInfoPackage;
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
public class ScanMaterialInfoPackageHandler extends SimpleChannelInboundHandler<ScanMaterialInfoPackage> {

	public static final ScanMaterialInfoPackageHandler me = new ScanMaterialInfoPackageHandler();


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ScanMaterialInfoPackage msg) throws Exception {
		AckPackage ackPackage = new AckPackage(msg.getCmdId());
		ctx.channel().writeAndFlush(ackPackage);
		try {
			System.out.println("扫描物料包已收到");
			MessageHandler.me.handleScanMaterialInfoPackage(msg);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

	}

}
