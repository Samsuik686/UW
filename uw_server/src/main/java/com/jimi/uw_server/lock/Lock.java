package com.jimi.uw_server.lock;

/**
 * 
 * @author HardyYao
 * @createTime 2019年4月23日  上午10:25:27
 */

public class Lock {

	public static Object REDIS_LOCK = new Object();
	
	public static Object ROBOT_TASK_REDIS_LOCK = new Object();
	
	public static Object INVENTORY_WINDOW_LOCK = new Object();
	
	public static Object INVENTORY_REDIS_LOCK = new Object();

	public static Object IMPORT_EWH_INVENTORY_FILE_LOCK = new Object();
}
