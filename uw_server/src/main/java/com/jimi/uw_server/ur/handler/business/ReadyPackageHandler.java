/**  
*  
*/  
package com.jimi.uw_server.ur.handler.business;


import com.jimi.uw_server.ur.constant.Constant;
import com.jimi.uw_server.ur.dao.UrInvTaskBoxInfoDAO;
import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.ForkliftReachPackage;
import com.jimi.uw_server.ur.entity.ReadyPackage;
import com.jimi.uw_server.ur.entity.SessionBox;
import com.jimi.uw_server.ur.handler.assist.PackSender;
import com.jimi.uw_server.ur.processor.ProcessorExecutor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.SimpleChannelInboundHandler;

/**  
 * <p>Title: ReadyPackageHandler</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2019年12月23日
 *
 */

@Sharable
public class ReadyPackageHandler  extends SimpleChannelInboundHandler<ReadyPackage>{

	public static final ReadyPackageHandler me = new ReadyPackageHandler();

	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2019年12月23日
	 * @param ctx
	 * @param msg
	 * @throws Exception  
	 * @see io.netty.channel.SimpleChannelInboundHandler#channelRead0(io.netty.channel.ChannelHandlerContext, Object)
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ReadyPackage msg) throws Exception {
		
		ctx.writeAndFlush(new AckPackage(msg.getCmdId()));
		String name = ctx.channel().attr(Constant.LOGIN_NAME).get();
		ForkliftReachPackage pack = UrInvTaskBoxInfoDAO.getForkliftReachPackageByUrName(name);
		if (name != null && pack != null) {
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					Boolean flag = PackSender.sendPackage(name, pack);
					if (!flag) {
						SessionBox.remove(name);
					}else {
						UrInvTaskBoxInfoDAO.removeUrTaskBoxArrivedPack(name);
					}
				}
			};
			ProcessorExecutor.me.submit(runnable);

		}
		
	}

}
