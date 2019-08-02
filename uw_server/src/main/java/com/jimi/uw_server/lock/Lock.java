package com.jimi.uw_server.lock;

/**
 * 
 * @author HardyYao
 * @createTime 2019年4月23日  上午10:25:27
 */

public class Lock {

	public static Object IO_TASK_REDIS_LOCK = new Object();

	public static Object ROBOT_TASK_REDIS_LOCK = new Object();

	public static Object SAMPLE_TASK_REDIS_LOCK = new Object();

	public static Object WINDOW_LOCK = new Object();

	public static Object INVENTORY_REDIS_LOCK = new Object();

	public static Object IMPORT_EWH_INVENTORY_FILE_LOCK = new Object();

	public static Object IMPORT_SAMPLE_TASK_FILE_LOCK = new Object();

	public static Object TASK_REDIS_LOCK = new Object();

	public static Object SAMPLE_TASK_OUT_LOCK = new Object();

	public static Object IO_TASK_BACK_LOCK = new Object();

	public static Object IO_TASK_CALL_LOCK = new Object();

	public static Object INV_TASK_BACK_LOCK = new Object();

	public static Object INV_TASK_CALL_LOCK = new Object();
}
