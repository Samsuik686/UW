package com.jimi.uw_server.ur.entity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;


public class AckResponseManager {
	
	static class AckResponseInfo{
		
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
	

	public synchronized static Boolean GetAndRemove(Integer id) {
		AckResponseInfo ackResponseInfo = ackMap.get(id);
		ackMap.remove(id);
		if (ackResponseInfo != null) {
			if (ackResponseInfo.getFlag()) {
				return true;
			}else {
				System.out.println("获取回复包失败：" + id);
			}
			
		}
		return false;
		
	}


	public synchronized static void putAckResponse(Integer id, CountDownLatch l) {
		ackMap.put(id, new AckResponseInfo(l));
	}
}
