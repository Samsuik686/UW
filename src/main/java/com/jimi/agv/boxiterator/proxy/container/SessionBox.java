package com.jimi.agv.boxiterator.proxy.container;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.websocket.Session;

import com.jimi.agv.boxiterator.proxy.entity.bo.BoxIteratorTaskInfo;

/**
 * 存放会话的容器
 * <br>
 * <b>2019年4月9日</b>
 * @author 几米物联自动化部-洪达浩
 */
public class SessionBox {
	
	private static final Map<Session, BoxIteratorTaskInfo> sessionMap = new HashMap<>();

	
	public static synchronized BoxIteratorTaskInfo getInfoBySession(Session session) {
		for (Entry<Session, BoxIteratorTaskInfo> sessionEntry : sessionMap.entrySet()) {
			if(sessionEntry.getKey().equals(session)) {
				return sessionEntry.getValue();
			}
		}
		return null;
	}
	
	
	public static synchronized Session getSessionByRobotId(int robotId) {
		for (Entry<Session, BoxIteratorTaskInfo> sessionEntry : sessionMap.entrySet()) {
			if(sessionEntry.getValue().getRobotId() == robotId) {
				return sessionEntry.getKey();
			}
		}
		return null;
	}
	
	
	/**
	 * robotid重复则添加失败，返回false
	 */
	public static synchronized boolean addSession(Session session, BoxIteratorTaskInfo info) {
		if(getSessionByRobotId(info.getRobotId()) != null) {
			return false;
		}else {
			sessionMap.put(session, info);
			return true;
		}
	}
	
	
	public static synchronized void removeSession(Session session) {
		sessionMap.remove(session);
	}
}
