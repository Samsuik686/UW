package com.jimi.uw_server.ur.handler.assist;

import java.nio.charset.Charset;
import java.util.List;

import com.jfinal.json.Json;
import com.jimi.uw_server.ur.constant.UrCmdType;
import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.AskPostionPackage;
import com.jimi.uw_server.ur.entity.ReadyPackage;
import com.jimi.uw_server.ur.entity.ScanMaterialExceptionPackage;
import com.jimi.uw_server.ur.entity.ForkliftReachPackage;
import com.jimi.uw_server.ur.entity.MaterialPositionInfoPackage;
import com.jimi.uw_server.ur.entity.LoginPackage;
import com.jimi.uw_server.ur.entity.ScanMaterialInfoPackage;
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
		System.out.println("[客户端]：" + msg);
		try {
			UrBasePackage pack = Json.getJson().parse(msg, UrBasePackage.class);
			switch (pack.getCmdCode()) {
			case UrCmdType.LOGIN:
				out.add(Json.getJson().parse(msg, LoginPackage.class));
				break;
			case UrCmdType.READY:
				out.add(Json.getJson().parse(msg, ReadyPackage.class));
				break;
			case UrCmdType.ASK_POSTITION:
				out.add(Json.getJson().parse(msg, AskPostionPackage.class));
				break;
			case UrCmdType.SCAN_MATERIAL_INFO:
				out.add(Json.getJson().parse(msg, ScanMaterialInfoPackage.class));
				break;
			case UrCmdType.SCAN_MATERIAL_EXCEPTION:
				out.add(Json.getJson().parse(msg, ScanMaterialExceptionPackage.class));
				break;
			case UrCmdType.ACK:
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
