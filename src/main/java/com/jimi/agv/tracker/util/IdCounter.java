package com.jimi.agv.tracker.util;

import java.util.concurrent.atomic.AtomicInteger;

public class IdCounter {

	private static final AtomicInteger id = new AtomicInteger();
	
	
	public static int getCmdId() {
		return id.incrementAndGet();
	}
	
}
