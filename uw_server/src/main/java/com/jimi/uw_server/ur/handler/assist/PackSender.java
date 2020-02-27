/**  
*  
*/  
package com.jimi.uw_server.ur.handler.assist;

import com.jimi.uw_server.ur.entity.AckResponseManager;
import com.jimi.uw_server.ur.entity.CmdidManager;
import com.jimi.uw_server.ur.entity.SessionBox;
import com.jimi.uw_server.ur.entity.base.UrBasePackage;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**  
 * <p>Title: PackSender</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2019年12月23日
 *
 */
public class PackSender {

	public static Boolean sendPackage(String urName, UrBasePackage pack){
		ChannelHandlerContext ctx = SessionBox.getChannelHandlerContext(urName);
		if (ctx != null) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int cmdId = CmdidManager.getCmdid();
			pack.setCmdId(cmdId);
			CountDownLatch l = new CountDownLatch(1);
			AckResponseManager.putAckResponse(cmdId, l);
			ctx.writeAndFlush(pack);
			try {
				l.await(10000, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return AckResponseManager.GetAndRemove(cmdId);
		}else {
			return false;
		}
		
	}
}
