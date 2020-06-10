package com.jimi.uw_server.agv.gaitek.dao;

import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;

/**
 * AGV任务条目Redis数据访问对象 <br>
 * <b>2018年6月15日</b>
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class TaskPropertyRedisDAO {

	public static final String UNDEFINED = "undefined";

	private static final String UW_LOCATION_SUFFIX = "UW:LOCATION_";

	private static final String UW_AGV_LINK_SWITCH = "UW:AGV_LINK_SWITCH";

	private static final String UW_CMDID = "UW:CMDID";

	private static final String UW_TASK_STATUS_SUFFIX = "UW:TASK_STATUS_";

	private static Cache cache = Redis.use();


	/**
	 * 获取一个新的CmdId
	 */
	public synchronized static int getCmdId() {
		Integer cmdid = 0;
		try {
			String cmdidStr = cache.get(UW_CMDID);
			if (cmdidStr != null) {
				cmdid = Integer.valueOf(cmdidStr);
			}
		} catch (NullPointerException e) {
		}
		cmdid %= 999999;
		cmdid++;
		cache.set(UW_CMDID, cmdid);
		return cmdid;
	}


	/**
	 * 设置agvWebSocket运行状态
	 */
	public synchronized static void setAgvWebSocketStatus(Boolean flag) {
		cache.set(UW_AGV_LINK_SWITCH, flag);
	}


	public synchronized static Boolean getAgvWebSocketStatus() {
		String flagStr = cache.get(UW_AGV_LINK_SWITCH);
		Boolean flag = true;
		if (flagStr != null && flagStr.equals("false")) {
			flag = false;
			return flag;
		}
		cache.set(UW_AGV_LINK_SWITCH, true);
		return flag;
	}


	/**
	 * 任务绑定仓口状态
	 */
	public synchronized static Integer getLocationStatus(Integer windowId, Integer goodsLocId) {
		Integer status = 0;
		String statusStr = cache.get(UW_LOCATION_SUFFIX + windowId + "_" + goodsLocId);
		if (statusStr == null) {
			status = 0;
			cache.set(UW_LOCATION_SUFFIX + windowId + "_" + goodsLocId, 0);
		} else {
			status = Integer.valueOf(statusStr);
		}

		return status;
	}


	/**
	 * 设置位置状态信息（0：空，1：满）
	 */
	public synchronized static void setLocationStatus(Integer windowId, Integer goodsLocId, Integer status) {
		cache.del(UW_LOCATION_SUFFIX + windowId + "_" + goodsLocId);
		cache.set(UW_LOCATION_SUFFIX + windowId + "_" + goodsLocId, status);
	}


	/**
	 * 删除位置状态信息
	 */
	public synchronized static void delLocationStatus(Integer windowId, Integer goodsLocId) {
		cache.del(UW_LOCATION_SUFFIX + windowId + "_" + goodsLocId);
	}


	public synchronized static Boolean getTaskStatus(Integer taskId) {
		String statusStr = cache.get(UW_TASK_STATUS_SUFFIX + taskId);
		Boolean status = true;
		if (statusStr != null && statusStr.equals("false")) {
			status = false;
			return status;
		}
		cache.set(UW_TASK_STATUS_SUFFIX + taskId, true);
		return status;
	}


	public synchronized static void setTaskStatus(Integer taskId, Boolean flag) {
		cache.del(UW_TASK_STATUS_SUFFIX + taskId);
		cache.set(UW_TASK_STATUS_SUFFIX + taskId, flag);
	}


	public synchronized static void delTaskStatus(Integer taskId) {
		cache.del(UW_TASK_STATUS_SUFFIX + taskId);
	}
}
