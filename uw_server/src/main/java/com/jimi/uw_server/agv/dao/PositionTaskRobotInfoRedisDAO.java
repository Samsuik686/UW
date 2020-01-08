package com.jimi.uw_server.agv.dao;

import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;


/**
 * AGV任务条目Redis数据访问对象
 * <br>
 * <b>2018年6月15日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class PositionTaskRobotInfoRedisDAO {

	private static Cache cache = Redis.use();


	/**
	 * 更新机器实时数据到Redis
	 */
	public synchronized static void setRobotStatus(Integer robotId, Boolean flag) {
		cache.del("Robot_" + robotId);
		cache.set("Robot_" + robotId, String.valueOf(flag));
	}
	
	public synchronized static Boolean getRobotStatus(Integer robotId) {
		String statusStr = cache.get("Robot_" + robotId);
		Boolean status = false;
		if (statusStr != null && statusStr.equals("true")) {
			status = true;
			return status;
		}
		cache.set("Robot_" + robotId, false);
		return status;
	}

	
	public synchronized static void delRobotStatus(Integer robotId) {
		cache.del("Robot_" + robotId);
	}
	
	
	
	/**
	 * 更新机器实时数据到Redis
	 */
	public synchronized static void setPositionTaskStatus(Boolean flag) {
		cache.del("PositionTask");
		cache.set("PositionTask", String.valueOf(flag));
	}
	
	public synchronized static Boolean getPositionTaskStatus() {
		String statusStr = cache.get("PositionTask");
		Boolean status = false;
		if (statusStr != null && statusStr.equals("true")) {
			status = true;
			return status;
		}
		cache.set("PositionTask", false);
		return status;
	}

	
	public synchronized static void delPositionTaskStatus() {
		cache.del("PositionTask");
	}
}
