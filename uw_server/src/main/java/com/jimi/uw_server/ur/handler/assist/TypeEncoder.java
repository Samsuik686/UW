package com.jimi.uw_server.ur.handler.assist;

import java.nio.charset.Charset;

import com.jfinal.json.Json;
import com.jimi.uw_server.ur.constant.Constant;
import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.AckResponseManager;
import com.jimi.uw_server.ur.entity.CmdidManager;
import com.jimi.uw_server.ur.entity.IOPackage;
import com.jimi.uw_server.ur.entity.ReachInPackage;
import com.jimi.uw_server.ur.entity.ReachOutPackage;
import com.jimi.uw_server.ur.entity.base.UrBasePackage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


/**
 * 类型编码器，负责把指令对象按照协议类型编码成指令字节集
 * <br>
 * <b>2019年9月26日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class TypeEncoder extends MessageToByteEncoder<Object> {

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		UrBasePackage pack = (UrBasePackage) msg;
		if (pack.getCmdid() == null) {
			pack.setCmdid(CmdidManager.getCmdid());
		}
		if (msg instanceof AckPackage) {
			out.writeBytes((Json.getJson().toJson((AckPackage) pack)).getBytes(Charset.forName("UTF-8")));
		} else if (msg instanceof ReachInPackage) {
			out.writeBytes((Json.getJson().toJson((ReachInPackage) pack)).getBytes(Charset.forName("UTF-8")));
		} else if (msg instanceof ReachOutPackage) {
			out.writeBytes((Json.getJson().toJson((ReachOutPackage) pack)).getBytes(Charset.forName("UTF-8")));
		} else if (msg instanceof IOPackage) {
			out.writeBytes((Json.getJson().toJson((IOPackage) pack)).getBytes(Charset.forName("UTF-8")));
		}
		// 别忘了编码的时候也按照协议来加上结束符
		out.writeBytes(Constant.CMD_END_FLAG.getBytes(Charset.forName("UTF-8")));
		System.out.println("服务器：" + out.toString(Charset.forName("UTF-8")));
	}

}
