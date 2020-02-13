package com.jimi.uw_server.ur.processor;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ProcessorExecutor {

    public static ProcessorExecutor me = new ProcessorExecutor();

    private AtomicInteger atomicInteger = new AtomicInteger(0);

    private ExecutorService pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors() + 1, 50,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(100), new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName("UR_HANDLER" + atomicInteger.incrementAndGet());
            return null;
        }
    }, new ThreadPoolExecutor.AbortPolicy());

    private ProcessorExecutor(){}

    public Future submit(Runnable r) {
       Future future = pool.submit(r);
       return future;
    }

    public void execute(Runnable r) {
        pool.execute(r);
    }

    public void stopAfterExecuteTask(){
        pool.shutdown();
    }

    public void stopNow(){
        pool.shutdownNow();
    }

}
