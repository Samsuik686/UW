/**  
*  
*/
package com.jimi.uw_server.agv.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

/**
 * <p>
 * Title: EfficiencyRedisDAO
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
 * @date 2019年11月29日
 *
 */
public class EfficiencyRedisDAO {

	public static final String UNDEFINED = "undefined";

	private static Cache cache = Redis.use();

	private static final String TASK_LAST_OPERATION_TIME_MAP = "UW:TASK_LAST_OPERATION_TIME_MAP";

	private static final String USER_LAST_OPERATION_TIME_MAP = "UW:USER_LAST_OPERATION_TIME_MAP";

	private static final String TASK_LAST_OPERATION_USER_MAP = "UW:TASK_LAST_OPERATION_USER_MAP";

	private static final String TASK_BOX_ARRIVED_TIME_MAP = "UW:TASK_BOX_ARRIVED_TIME_MAP";

	private static final String TASK_START_TIME_MAP = "UW:TASK_START_TIME_MAP";

	private static Object TASK_LAST_OPERATION_TIME_LOCK = new Object();

	private static Object USER_LAST_OPERATION_TIME_LOCK = new Object();

	private static Object TASK_LAST_OPERATION_USER_LOCK = new Object();

	private static Object TASK_BOX_ARRIVED_TIME_LOCK = new Object();

	private static Object TASK_START_TIME_MAP_LOCK = new Object();


	public static void putTaskLastOperationTime(Integer taskId, Long time) {
		synchronized (TASK_LAST_OPERATION_TIME_LOCK) {
			cache.hset(TASK_LAST_OPERATION_TIME_MAP, taskId, time);
		}
	}


	public static Long getTaskLastOperationTime(Integer taskId) {
		String timeStr = cache.hget(TASK_LAST_OPERATION_TIME_MAP, taskId);
		if (timeStr != null) {
			Long time = Long.valueOf(timeStr);
			return time;
		}
		return null;
	}


	public static void removeTaskLastOperationTimeByTask(Integer taskId) {
		synchronized (TASK_LAST_OPERATION_TIME_LOCK) {
			cache.hdel(TASK_LAST_OPERATION_TIME_MAP, taskId);
		}
	}


	public static void removeTaskLastOperationTime() {
		synchronized (TASK_LAST_OPERATION_TIME_LOCK) {
			cache.del(TASK_LAST_OPERATION_TIME_MAP);
		}
	}


	public static void putUserLastOperationTime(String uid, Long time) {
		synchronized (USER_LAST_OPERATION_TIME_LOCK) {
			cache.hset(USER_LAST_OPERATION_TIME_MAP, uid, time);
		}
	}


	public static Long getUserLastOperationTime(String uid) {
		String timeStr = cache.hget(USER_LAST_OPERATION_TIME_MAP, uid);
		if (timeStr != null) {
			Long time = Long.valueOf(timeStr);
			return time;
		}
		return null;
	}


	public static void removeUserLastOperationTimeByUser(String uid) {
		synchronized (USER_LAST_OPERATION_TIME_LOCK) {
			cache.hdel(USER_LAST_OPERATION_TIME_MAP, uid);
		}
	}


	public static void removeUserLastOperationTime() {
		synchronized (USER_LAST_OPERATION_TIME_LOCK) {
			cache.del(USER_LAST_OPERATION_TIME_MAP);
		}
	}


	public static void putTaskBoxArrivedTime(Integer taskId, Integer boxId, Long time) {
		synchronized (TASK_BOX_ARRIVED_TIME_LOCK) {
			cache.hset(TASK_BOX_ARRIVED_TIME_MAP, taskId + "_" + boxId, time);
		}
	}


	public static Long getTaskBoxArrivedTime(Integer taskId, Integer boxId) {
		String timeStr = cache.hget(TASK_BOX_ARRIVED_TIME_MAP, taskId + "_" + boxId);
		if (timeStr != null) {
			Long time = Long.valueOf(timeStr);
			return time;
		}
		return null;
	}


	public static void removeTaskBoxArrivedTimeByTaskAndBox(Integer taskId, Integer boxId) {
		synchronized (TASK_BOX_ARRIVED_TIME_LOCK) {
			cache.hdel(TASK_BOX_ARRIVED_TIME_MAP, taskId + "_" + boxId);
		}
	}


	public static void removeTaskBoxArrivedTimeByTask(Integer taskId) {
		synchronized (TASK_BOX_ARRIVED_TIME_LOCK) {
			String suffixKey = taskId + "_";
			@SuppressWarnings("unchecked")
			Map<String, String> map = cache.hgetAll(TASK_BOX_ARRIVED_TIME_MAP);
			List<String> delKeys = new ArrayList<String>();
			if (!map.isEmpty()) {
				for (Entry<String, String> entry : map.entrySet()) {
					if (entry.getKey().startsWith(suffixKey)) {
						delKeys.add(entry.getKey());
					}
				}
			}
			if (!delKeys.isEmpty()) {
				for (String delKey : delKeys) {
					cache.hdel(TASK_BOX_ARRIVED_TIME_MAP, delKey);
				}
			}
		}
	}


	public static void removeTaskBoxArrivedTime() {
		synchronized (TASK_BOX_ARRIVED_TIME_LOCK) {
			cache.del(TASK_BOX_ARRIVED_TIME_MAP);
		}
	}


	public static void putTaskLastOperationUser(Integer taskId, String uid) {
		synchronized (TASK_LAST_OPERATION_USER_LOCK) {
			cache.hset(TASK_LAST_OPERATION_USER_MAP, taskId, uid);
		}
	}


	public static String getTaskLastOperationUser(Integer taskId) {
		String uid = cache.hget(TASK_LAST_OPERATION_USER_MAP, taskId);
		return uid;
	}


	public static void removeTaskLastOperationUserByTask(Integer taskId) {
		synchronized (TASK_LAST_OPERATION_USER_LOCK) {
			cache.hdel(TASK_LAST_OPERATION_USER_MAP, taskId);
		}
	}


	public static void removeTaskLastOperationUser() {
		synchronized (TASK_LAST_OPERATION_USER_LOCK) {
			cache.del(TASK_LAST_OPERATION_USER_MAP);
		}
	}


	public static void putTaskStartTime(Integer taskId, Long time) {
		synchronized (TASK_START_TIME_MAP_LOCK) {
			cache.hset(TASK_START_TIME_MAP, taskId, time);
		}
	}


	public static Long getTaskStartTime(Integer taskId) {
		if (cache.hexists(TASK_START_TIME_MAP, taskId)) {
			String timeStr = cache.hget(TASK_START_TIME_MAP, taskId);
			if (timeStr != null) {
				Long time = Long.valueOf(timeStr);
				return time;
			}
			return null;
		} else {
			return null;
		}

	}


	public static void removeTaskStartTimeByTask(Integer taskId) {
		synchronized (TASK_START_TIME_MAP_LOCK) {
			cache.hdel(TASK_START_TIME_MAP, taskId);
		}
	}


	public static void removeTaskStartTime() {
		synchronized (TASK_START_TIME_MAP_LOCK) {
			cache.del(TASK_START_TIME_MAP);
		}
	}
}
