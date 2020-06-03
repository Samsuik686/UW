/**  
*  
*/  
package com.jimi.uw_server.ur.dao;

import com.alibaba.fastjson.JSON;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jimi.uw_server.ur.entity.ForkliftReachPackage;

/**  
 * <p>Title: UrInvTaskBoxInfoDAO</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2019年12月23日
 *
 */
public class UrInvTaskBoxInfoDAO {
	
	public static final String UNDEFINED = "undefined";

	private static Cache cache = Redis.use();
	
	private static final String UW_UR_TASK_BOX_INFO_MAP = "UW:UR_TASK_BOX_INFO_MAP";
	
	private static Object UW_UR_TASK_BOX_INFO_MAP_LOCK = new Object();

	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2019年12月23日
	 */
	public static void putUrTaskBoxArrivedPack(String urName, ForkliftReachPackage pack) {
		synchronized (UW_UR_TASK_BOX_INFO_MAP_LOCK) {
			cache.hset(UW_UR_TASK_BOX_INFO_MAP, urName, JSON.toJSONString(pack));
		}
	}
	
	
	public static void removeUrTaskBoxArrivedPack(String urName) {
		synchronized (UW_UR_TASK_BOX_INFO_MAP_LOCK) {
			cache.hdel(UW_UR_TASK_BOX_INFO_MAP, urName);
		}
	}
	
	
	public static ForkliftReachPackage getForkliftReachPackageByUrName(String urName) {
		String msg = cache.hget(UW_UR_TASK_BOX_INFO_MAP, urName);
		if (msg != null && !msg.equals("")) {
			return JSON.parseObject(msg, ForkliftReachPackage.class);
		}
		return null;
	}

}
