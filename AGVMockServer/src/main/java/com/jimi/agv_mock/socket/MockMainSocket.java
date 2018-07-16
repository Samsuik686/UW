package com.jimi.agv_mock.socket;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.alibaba.fastjson.JSON;
import com.jimi.agv_mock.constant.Constant;
import com.jimi.agv_mock.entity.cmd.base.AGVBaseCmd;
import com.jimi.agv_mock.handle.ACKHandler;
import com.jimi.agv_mock.handle.LSSLHandler;


/**
 * 模拟技田AGV服务器的Mock服务器
 * <br>
 * <b>2018年7月3日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
@ServerEndpoint("/connect")
public class MockMainSocket implements UncaughtExceptionHandler{
	
    private static Session session;
    
	private static int cmdid;

	/**
	 * 发送的CMDID与是否被ACK的关系映射
	 */
	private static Map<Integer, Boolean> sendCmdidAckMap;
    
	/**
	 * 已收到的非ACK的CMDID集合
	 */
	private static Set<Integer> receiveNotAckCmdidSet;
	
	
    @OnOpen
    public void onOpen(Session session){
    	MockMainSocket.session = session;
    	sendCmdidAckMap = new HashMap<>();
    	receiveNotAckCmdidSet = new HashSet<>();
    	sendMessage("AGV 服务当前为Mock模式,服务器类型叉车服务");
    }
    
    
	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		System.out.println("Client was disconnected.");
	}
    

    @OnMessage
    public void onMessage(String message, Session session) {
    	MockMainSocket.session = session;
    	System.out.println("["+ new Date().toString() +"]" + "receiver message:" + message);
    	
    	Thread thread = new Thread(() -> {
			//判断是否是ack指令
			if(message.contains("\"cmdcode\":\"ack\"")) {//ack指令
				ACKHandler.handleACK(message);
			}else if(message.contains("\"cmdcode\"")){//非ack指令
				ACKHandler.handleNOTACK(message);
				
				//交给对应命令处理器：
				//判断是否是ls，sl指令
				if(message.contains("\"cmdcode\":\"LS\"") || message.contains("\"cmdcode\":\"SL\"")) {
					LSSLHandler.handleLSSL(message);
				}
				
				//判断是启用禁用指令
				if(message.contains("\"cmdcode\":\"enable\"") || message.contains("\"cmdcode\":\"disable\"")) {
					
				}
				
				//判断是启用禁用指令
				if(message.contains("\"cmdcode\":\"enable\"") || message.contains("\"cmdcode\":\"disable\"")) {
					
				}
				
				//判断是启用禁用指令
				if(message.contains("\"cmdcode\":\"enable\"") || message.contains("\"cmdcode\":\"disable\"")) {
					
				}
				
			}
		});
		thread.setUncaughtExceptionHandler(this);
		thread.start();
    }

    
    
	/**
	 * 使用websocket发送一条ACK到AGV服务器
	 */
	public static void sendACK(String message) {
		try {
			//模拟延迟
			Thread.sleep(Constant.WAIT_ACK_TIMEOUT + new Random().nextInt() % 500);
			send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * 使用websocket发送一条消息到AGV服务器
	 */
	public static void sendMessage(String message) {
		int cmdid = JSON.parseObject(message, AGVBaseCmd.class).getCmdid();
		try {
			send(message);
			sendCmdidAckMap.put(cmdid, false);
			Thread.sleep(Constant.WAIT_ACK_TIMEOUT);
			while(!sendCmdidAckMap.get(cmdid)) {
				send(message);
				Thread.sleep(Constant.WAIT_ACK_TIMEOUT);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    

	public synchronized static void send(String message) {
        try {
        	System.out.println("["+ new Date().toString() +"]"+"send message: " + message);
			session.getBasicRemote().sendText(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    /**
	 * 获取一个新的CmdId
	 */
	public synchronized static int getCmdId() {
		cmdid%=999999;
		cmdid++;
		return cmdid;
	}


	public static Map<Integer, Boolean> getSendCmdidAckMap() {
		return sendCmdidAckMap;
	}


	public static Set<Integer> getReceiveNotAckCmdidSet() {
		return receiveNotAckCmdidSet;
	}


	@Override
	public void uncaughtException(Thread t, Throwable e) {
		//处理一些错误命令
		e.printStackTrace();
	}

}