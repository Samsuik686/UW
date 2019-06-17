package com.jimi.agv.boxiterator.client.sender;

import com.alibaba.fastjson.JSONObject;
import com.jimi.agv.boxiterator.client.constant.PackageType;
import com.jimi.agv.boxiterator.client.socket.BoxIteratorProxySocket;

import cc.darhao.pasta.Pasta;

/**
 * 请求发送者
 * <br>
 * <b>2019年6月15日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class RequestSender {

	public static int sendLogin(int robotId, int windowX, int windowY) {
		JSONObject body = new JSONObject();
		body.put("robotId", robotId);
		body.put("windowX", windowX);
		body.put("windowY", windowY);
		try {
			return Pasta.sendRequest(BoxIteratorProxySocket.getSession(), PackageType.LOGIN, body).getInteger("result");
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}


	public static void sendGet(String key, int targetX, int targetY, int targetZ) {
		JSONObject body = createMoveCmdBody(key, targetX, targetY, targetZ);
		try {
			Pasta.sendRequest(BoxIteratorProxySocket.getSession(), PackageType.GET, body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void sendReturn(String key, int targetX, int targetY, int targetZ) {
		JSONObject body = createMoveCmdBody(key, targetX, targetY, targetZ);
		try {
			Pasta.sendRequest(BoxIteratorProxySocket.getSession(), PackageType.RETURN, body);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private static JSONObject createMoveCmdBody(String key, int targetX, int targetY, int targetZ) {
		JSONObject body = new JSONObject();
		body.put("key", key);
		body.put("targetX", targetX);
		body.put("targetY", targetY);
		body.put("targetZ", targetZ);
		return body;
	}
	
}
