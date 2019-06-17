package com.jimi.agv.boxiterator.proxy.util;

import java.util.concurrent.atomic.AtomicInteger;

public class IdCounter {

	private static final AtomicInteger id = new AtomicInteger();
	
	
	public static int getCmdId() {
		return id.incrementAndGet();
	}
	
}
