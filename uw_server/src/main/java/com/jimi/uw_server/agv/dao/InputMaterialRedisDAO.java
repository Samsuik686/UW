package com.jimi.uw_server.agv.dao;

import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;


public class InputMaterialRedisDAO {

	public static final String UNDEFINED = "undefined";

	private static Cache cache = Redis.use();


	/**
	 * 任务绑定仓口状态
	 */
	public synchronized static Integer getScanStatus(Integer windowId) {
		Integer status = 0;
		status = cache.get("Scan_" + windowId);
		if (status == null) {
			status = 0;
			cache.set("Scan_" + windowId, -1);

		}
		return status;
	}


	/**
	 * 设置位置状态信息（0：空，1：满）
	 */
	public synchronized static void setScanStatus(Integer windowId, Integer status) {
		cache.del("Scan_" + windowId);
		cache.set("Scan_" + windowId, status);
	}
}
