/**  
*  
*/
package com.jimi.uw_server.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <p>
 * Title: ProcessorExcuteFactory
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2019
 * </p>
 * <p>
 * Company: 惠州市几米物联技术有限公司
 * </p>
 * 
 * @author trjie
 * @date 2020年5月11日
 *
 */
public class UwProcessorExcutor {

	public static UwProcessorExcutor me = new UwProcessorExcutor();

	private AtomicInteger atomicInteger = new AtomicInteger(0);

	private ExecutorService pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1, 50, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(100), new ThreadFactory() {
		@Override
		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setName("UW_PRO_HANDLER" + atomicInteger.incrementAndGet());
			return thread;
		}
	}, new ThreadPoolExecutor.AbortPolicy());


	private UwProcessorExcutor() {
	}


	public void execute(Runnable r) {
		pool.execute(r);
	}


	public void stopAfterExecuteTask() {
		pool.shutdown();
	}


	public void stopNow() {
		pool.shutdownNow();
	}
}
