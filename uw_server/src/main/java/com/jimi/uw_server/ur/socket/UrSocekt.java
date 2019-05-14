package com.jimi.uw_server.ur.socket;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.jimi.uw_server.ur.processor.MessageHandler;
import com.jimi.uw_server.ur.processor.OutPackageHolder;
import com.jimi.uw_server.ur.processor.RequestQueueHolder;
import com.jimi.uw_server.ur.util.ErrorLogger;
import com.jimi.uw_server.util.ErrorLogWritter;

/**
 * 
 * @author trjie
 * @createTime 2019年4月25日  上午9:50:28
 */

@ServerEndpoint("/ur")
public class UrSocekt {
	
	public static MessageHandler handler = null;
	
	public static RequestQueueHolder queueHolder = null;
	
	public static OutPackageHolder outPackageHolder = null;
	
	@OnOpen
	public void onOpen(Session session) {
		System.out.println("UrSocket has been Connected");
		try {
			queueHolder = new RequestQueueHolder(session);
			outPackageHolder = new OutPackageHolder();
			handler = new MessageHandler(session);
//			testTimeout(queueHolder); //单元测试用
		} catch (Exception e) {
			ErrorLogWritter.save(e.getClass().getSimpleName() + ":" + e.getMessage());
			e.printStackTrace();
		}
	}


	@OnClose
	public void onClose(CloseReason reason) {
		handler = null;
		queueHolder = null;
		outPackageHolder = null;
		ErrorLogWritter.save("UrSocket was Stopped because :" + reason.getCloseCode());
	}

	
	@OnMessage
	public void onMessage(String message) {
		try {
			handler.handle(message);
		} catch (Exception e) {
			ErrorLogger.saveErrorToDb(e);
		}
	}
	
	
//	private void testTimeout(RequestQueueHolder queueHolder){
//		//使用断点调试
//		new Thread(()-> {
//			while(true) {
//				ReachInPackage reachInPackage = new ReachInPackage();
//				reachInPackage.setCmdid(1);
//				reachInPackage.setTaskId(1);
//				queueHolder.push(reachInPackage);
//				reachInPackage = new ReachInPackage();
//				reachInPackage.setCmdid(2);
//				reachInPackage.setTaskId(2);
//				queueHolder.push(reachInPackage);
//			}
//		}).start();
//	}
	
}
