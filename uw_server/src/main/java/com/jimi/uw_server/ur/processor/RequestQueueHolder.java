package com.jimi.uw_server.ur.processor;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.websocket.Session;

import com.alibaba.fastjson.JSON;
import com.jfinal.kit.PropKit;
import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.base.UrBasePackage;
import com.jimi.uw_server.ur.util.ErrorLogger;

/**
 * 请求队列持有者
 * <br>
 * <b>2019年5月10日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class RequestQueueHolder {
	
	private Long waitAckTimeout = PropKit.use("properties.ini").getLong("waitAckTimeout");
	
	private volatile int  waitAckId = 0;
	
	private Session session;
	
	private Thread sendThread;
	
	private Queue<UrBasePackage> sendQueue;
	
	
	public RequestQueueHolder(Session session) {
		this.session = session;
		sendQueue = new LinkedBlockingQueue<>();
		initSendThread();
	}


	public void acceptAck(AckPackage ackPackage) {
		if(ackPackage.getCmdid().equals(waitAckId)) {
			waitAckId = 0;
		}
	}
	
	
	public void push(UrBasePackage basePackage) {
		sendQueue.offer(basePackage);
	}


	private void initSendThread() {
		this.sendThread = new Thread(()-> {
			while(true) {
				if(waitAckId == 0) {
					UrBasePackage head = sendQueue.peek();
					if(head != null) {
						waitAckId = head.getCmdid();
						while(true) {
							try {
								session.getBasicRemote().sendText(JSON.toJSONString(head));
								sendQueue.poll();
								new Thread(()-> {
									try {
										int id = head.getCmdid();
										Thread.sleep(waitAckTimeout);
										if(waitAckId == id) {
											ErrorLogger.saveErrorToDb(new RuntimeException("请求UR超时"));
										}
									} catch (InterruptedException e) {
										e.printStackTrace();
									}
								}).start();
								break;
							} catch (IOException e) {
								try {
									Thread.sleep(5000);
								} catch (InterruptedException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		sendThread.start();
	}

}
