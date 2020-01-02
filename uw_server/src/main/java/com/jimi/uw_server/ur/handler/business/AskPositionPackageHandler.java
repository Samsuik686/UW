/**  
*  
*/  
package com.jimi.uw_server.ur.handler.business;

import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.AskPostionPackage;
import com.jimi.uw_server.ur.processor.MessageHandler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;

/**  
 * <p>Title: AskMaterialPositionPackageHandler</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2019年12月23日
 *
 */
@Sharable
public class AskPositionPackageHandler extends SimpleChannelInboundHandler<AskPostionPackage>{

	public static final AskPositionPackageHandler me = new AskPositionPackageHandler();
	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2019年12月23日
	 * @param ctx
	 * @param msg
	 * @throws Exception  
	 * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext, java.lang.Object)  
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, AskPostionPackage msg) throws Exception {
		ctx.writeAndFlush(new AckPackage(msg.getCmdId()));
		
		MessageHandler.me.handleAskPostionPackage(msg);
	}

}
