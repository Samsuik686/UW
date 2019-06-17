package com.jimi.agv.boxiterator.proxy.socket;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.jimi.agv.boxiterator.proxy.container.SessionBox;

import cc.darhao.pasta.Pasta;


/**
 * 生产环境数据通讯websocket
 * <br>
 * <b>2019年3月6日</b>
 * @author 几米物联自动化部-洪达浩
 */
@ServerEndpoint("/connect")
public class BoxIteratorProxyServerSocket {

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("session接入，ID为:"+session.getId());
	}

	
	@OnMessage
	public void onMessage(Session session, String message) {
		try {
			Pasta.receiveMessage(session, message);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}


	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		System.out.println("session关闭，ID为:"+session.getId()+"，原因为:"+closeReason.getReasonPhrase());
		SessionBox.removeSession(session);
	}


	@OnError
	public void onError(Session session, Throwable error) {
		System.out.println("session发生错误，ID为:"+session.getId()+"，信息为:"+error.getMessage());
	}
	
}
