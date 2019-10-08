package com.jimi.uw_server.ur.entity;

import java.util.concurrent.atomic.AtomicInteger;


public class CmdidManager {

	private static AtomicInteger cmdid = new AtomicInteger();


	public synchronized static int getCmdid() {
		int id = cmdid.incrementAndGet();
		if (id == 65536) {
			cmdid.set(0);
		}
		return cmdid.incrementAndGet();
	}

}
