package com.jimi.uw_server.ur.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;


public class AckResponseManager {

	static Map<Integer, CountDownLatch> ackMap = new ConcurrentHashMap<Integer, CountDownLatch>();


	public synchronized static void reduce(Integer id) {
		CountDownLatch l = ackMap.get(id);
		if (l != null) {
			l.countDown();
		}
		ackMap.remove(id);
	}


	public synchronized static void putAckResponse(Integer id, CountDownLatch l) {
		ackMap.put(id, l);
	}
}
