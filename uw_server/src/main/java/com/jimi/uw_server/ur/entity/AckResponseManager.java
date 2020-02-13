package com.jimi.uw_server.ur.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;


public class AckResponseManager {

	public static AckResponseManager manager = new AckResponseManager();
	
	class AckResponseInfo{
		
		private Boolean flag;
		
		private CountDownLatch countDownLatch;
		
		/**
		 * <p>Title<p>
		 * <p>Description<p>
		 */
		public AckResponseInfo(CountDownLatch l) {
			this.flag = false;
			this.countDownLatch = l;
		}

		public Boolean getFlag() {
			return flag;
		}

		public void setFlag(Boolean flag) {
			this.flag = flag;
		}

		public CountDownLatch getCountDownLatch() {
			return countDownLatch;
		}

		public void setCountDownLatch(CountDownLatch countDownLatch) {
			this.countDownLatch = countDownLatch;
		}
		
		
	}
	
	static Map<Integer, AckResponseInfo> ackMap = new ConcurrentHashMap<Integer, AckResponseInfo>();

	public synchronized static void countDownAck(Integer id) {
		AckResponseInfo ackResponseInfo = ackMap.get(id);
		if (ackResponseInfo != null && ackResponseInfo.getCountDownLatch() != null) {
			ackResponseInfo.setFlag(true);
			ackResponseInfo.getCountDownLatch().countDown();
		}
	}
	

	public synchronized static Boolean GetAndreduce(Integer id) {
		AckResponseInfo ackResponseInfo = ackMap.get(id);
		ackMap.remove(id);
		if (ackResponseInfo != null) {
			return ackResponseInfo.getFlag();
		}
		return false;
		
	}


	public synchronized static void putAckResponse(Integer id, CountDownLatch l) {
		ackMap.put(id, manager.new AckResponseInfo(l));
	}
}
