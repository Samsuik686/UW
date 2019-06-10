package com.jimi.agv.tracker.socket;

import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.WebSocketContainer;

import com.jimi.agv.tracker.handler.RobotInfoHandler;

/**
 * 实时接收机器信息的类
 * <br>
 * <b>2018年6月15日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
@ClientEndpoint
public class RobotInfoSocket{
	
	public static void init(String uri) throws Exception {
		connect(uri);
	}


	@OnOpen
	public void onOpen() {
		System.out.println("RobotInfoSocket is Running Now...");
	}


	@OnClose
	public void onClose(CloseReason reason) {
		System.out.println("RobotInfoSocket was close because :" + reason.getCloseCode());
	}
	
	
	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	
	@OnMessage
	public void onMessage(String message) {
		RobotInfoHandler.handle(message);
	}


	private static void connect(String uri) throws Exception {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		container.connectToServer(new RobotInfoSocket(), new URI(uri));
	}

}