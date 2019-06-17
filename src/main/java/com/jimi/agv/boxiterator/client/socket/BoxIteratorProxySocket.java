package com.jimi.agv.boxiterator.client.socket;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import cc.darhao.pasta.Pasta;


/**
 * 料盒迭代器代理的ws终端
 * <br>
 * <b>2019年6月15日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
@ClientEndpoint
public class BoxIteratorProxySocket {
	
	private static Session session;
	

	@OnOpen
	public void onOpen(Session session) {
		System.out.println("BoxIteratorProxySocket has Connected...");
		BoxIteratorProxySocket.session = session;
	}


	@OnClose
	public void onClose(CloseReason reason) {
		System.out.println("BoxIteratorProxySocket was close because :" + reason.getCloseCode());
	}
	
	
	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	
	@OnMessage
	public void onMessage(Session session, String message) {
		try {
			Pasta.receiveMessage(session, message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static Session getSession() {
		return session;
	}
	
}