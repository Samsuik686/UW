package com.jimi.uw_server.ur.handler.assist;

import java.nio.charset.Charset;

import com.jfinal.json.Json;
import com.jimi.uw_server.ur.constant.Constant;
import com.jimi.uw_server.ur.constant.UrCmdType;
import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.MaterialPositionInfoPackage;
import com.jimi.uw_server.ur.entity.ForkliftReachPackage;
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
		switch (pack.getCmdCode()) {
		
		case UrCmdType.FORKLIFT_REACH:
			out.writeBytes((Json.getJson().toJson((ForkliftReachPackage) pack)).getBytes(Charset.forName("UTF-8")));
			break;
		case UrCmdType.MATERIAL_POSITION_INFO:
			out.writeBytes((Json.getJson().toJson((MaterialPositionInfoPackage) pack)).getBytes(Charset.forName("UTF-8")));
			break;
		case UrCmdType.ACK:
			out.writeBytes((Json.getJson().toJson((AckPackage) pack)).getBytes(Charset.forName("UTF-8")));
			break;
		default:
			break;
		}
		// 别忘了编码的时候也按照协议来加上结束符
		out.writeBytes(Constant.CMD_END_FLAG.getBytes(Charset.forName("UTF-8")));
		System.out.println("[服务器]：" + out.toString(Charset.forName("UTF-8")));
	}

}
