package com.jimi.uw_server.lock;

/**
 * 
 * @author HardyYao
 * @createTime 2019年4月23日 上午10:25:27
 */

public class Lock {

	public static Object IO_TASK_REDIS_LOCK = new Object();

	public static Object ROBOT_TASK_REDIS_LOCK = new Object();

	public static Object SAMPLE_TASK_REDIS_LOCK = new Object();

	public static Object WINDOW_LOCK = new Object();

	public static Object INVENTORY_REDIS_LOCK = new Object();

	public static Object TASK_REDIS_LOCK = new Object();

	public static Object UR_INV_TASK_LOCK = new Object();

	public static Object UR_OUT_TASK_LOCK = new Object();
}
