package com.jimi.uw_server.ur.handler.business;

import java.util.Date;

import com.jimi.uw_server.ur.constant.Constant;
import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.LoginPackage;
import com.jimi.uw_server.ur.entity.SessionBox;
import com.jimi.uw_server.ur.thread.TestThread;

import cc.darhao.dautils.api.DateUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


/**
 * 登录请求处理器，单例类可被共享
 * <br>
 * <b>2019年9月26日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
@Sharable
public class LoginPackageHandler extends SimpleChannelInboundHandler<LoginPackage> {

	public static final LoginPackageHandler me = new LoginPackageHandler();


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, LoginPackage msg) throws Exception {
		ctx.channel().attr(Constant.LOGIN_NAME).set(msg.getName());
		ctx.writeAndFlush(new AckPackage(msg.getCmdid()));
		SessionBox.add(msg.getName(), ctx);
		System.out.println("[UW机械臂服务器] - " + DateUtil.HHmmssSSS(new Date()) + " - 欢迎，" + msg.getName());

	}

}
