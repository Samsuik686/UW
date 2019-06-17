package com.jimi.agv.boxiterator.proxy.socket;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.alibaba.fastjson.JSON;
import com.jimi.agv.boxiterator.proxy.entity.cmd.base.AGVBaseCmd;
import com.jimi.agv.boxiterator.proxy.socket.handle.ACKHandler;
import com.jimi.agv.boxiterator.proxy.socket.proxy.StatusProxy;


/**
 * 与AGV服务器进行通讯的主要类
 * <br>
 * <b>2019年6月15日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
@ClientEndpoint
public class AGVMainSocket {
	
	private static Session session;
	
	/**
	 * 发送的CMDID与是否被ACK的关系映射
	 */
	private static Map<Integer, Boolean> sendCmdidAckMap;
	/**
	 * 已收到的非ACK的CMDID集合
	 */
	private static Set<Integer> receiveNotAckCmdidSet;
	
	
	public static void init(String uri) throws Exception {
		//初始化
		sendCmdidAckMap = new HashMap<>();
		receiveNotAckCmdidSet = new HashSet<>();
		//连接AGV服务器
		connect(uri);
	}


	@OnOpen
	public void onOpen(Session userSession) {
		System.out.println("AGVMainSocket is Running Now...");
		session = userSession;
	}


	@OnClose
	public void onClose(CloseReason reason) {
		System.out.println("AGVMainSocket was close because :" + reason.getCloseCode());
	}
	
	
	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	
	@OnMessage
	public void onMessage(String message) {
		try {
			//判断是否是ack指令
			if(message.contains("\"cmdcode\":\"ack\"")) {//ack指令
				ACKHandler.handleACK(message);
			}else if(message.contains("\"cmdcode\"")){//非ack指令
				if(ACKHandler.handleNOTACK(message)) {
					//判断是否是status指令
					if(message.contains("\"cmdcode\":\"status\"")) {
						StatusProxy.handleStatus(message);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * 使用websocket发送一条ACK到AGV服务器
	 */
	public static void sendACK(AGVBaseCmd message) throws IOException {
		send(message);
	}
	

	/**
	 * 使用websocket发送一条消息到AGV服务器
	 */
	public static void sendMessage(AGVBaseCmd message) {
		new Thread(()-> {
			try {
				send(message);
				sendCmdidAckMap.put(message.getCmdid(), false);
				Thread.sleep(5000);
				while (!sendCmdidAckMap.get(message.getCmdid())) {
					send(message);
					Thread.sleep(5000);
				}
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}).start();
	}
	
	
	public static Map<Integer, Boolean> getSendCmdidAckMap(){
		return sendCmdidAckMap;
	}


	public static Set<Integer> getReceiveNotAckCmdidSet() {
		return receiveNotAckCmdidSet;
	}


	private static void connect(String uri) throws Exception {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		container.connectToServer(new AGVMainSocket(), new URI(uri));
	}


	private static void send(AGVBaseCmd message) throws IOException {
		synchronized (AGVMainSocket.class) {
			session.getBasicRemote().sendText(JSON.toJSONString(message));
		}
	}
	
}