package com.jimi.uw_server.ur.handler.assist;

import java.nio.charset.Charset;
import java.util.List;

import com.jfinal.json.Json;
import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.IOPackage;
import com.jimi.uw_server.ur.entity.LoginPackage;
import com.jimi.uw_server.ur.entity.ResultPackage;
import com.jimi.uw_server.ur.entity.base.UrBasePackage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;


/**
 * 类型解码器，负责把所有来自客户端的字节集按照协议类型解码成指令对象
 * <br>
 * <b>2019年9月26日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class TypeDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		// 分割并读取字节集
		String msg = in.toString(Charset.forName("UTF-8"));
		in.skipBytes(in.readableBytes());
		System.err.println("客户端：" + msg);
		try {
			UrBasePackage pack = Json.getJson().parse(msg, UrBasePackage.class);
			switch (pack.getCmdcode()) {
			case "login":
				out.add(Json.getJson().parse(msg, LoginPackage.class));
				break;
			case "in":
				out.add(Json.getJson().parse(msg, IOPackage.class));
				break;
			case "result":
				out.add(Json.getJson().parse(msg, ResultPackage.class));
				break;
			case "ack":
				out.add(Json.getJson().parse(msg, AckPackage.class));
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("解析错误");
		}
		// 跳过已读部分，否则下次decode进来的buf会追加本次的buf

	}
}
