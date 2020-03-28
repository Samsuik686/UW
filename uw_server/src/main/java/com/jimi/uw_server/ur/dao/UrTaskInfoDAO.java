/**  
*  
*/  
package com.jimi.uw_server.ur.dao;

import com.alibaba.fastjson.JSON;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jimi.uw_server.ur.entity.UrMaterialInfo;
import com.jimi.uw_server.util.VisualSerializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**  
 * <p>Title: UrInvTaskInfoDAO</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2019年12月19日
 *
 */
public class UrTaskInfoDAO {
	
	public static final String UNDEFINED = "undefined";

	private static Cache cache = Redis.use();
	
	private static final String UW_UR_TASK_MATERIAL_INFO_MAP = "UW_UR_TASK_MATERIAL_INFO_MAP";
	
	private static Object UW_UR_TASK_MATERIAL_INFO_MAP_LOCK = new Object();
	
	
	public static void putUrMaterialInfos(Integer taskId, Integer boxId, List<UrMaterialInfo> urMaterialInfos) {
		synchronized (UW_UR_TASK_MATERIAL_INFO_MAP_LOCK) {
			if (!urMaterialInfos.isEmpty()) {
				cache.hset(UW_UR_TASK_MATERIAL_INFO_MAP, taskId + "_" + boxId, JSON.toJSONString(urMaterialInfos));
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public static List<UrMaterialInfo> getAllUrMaterialInfos() {
		List<String> urMaterialInfosStringList = cache.hvals(UW_UR_TASK_MATERIAL_INFO_MAP);
		if (urMaterialInfosStringList != null && !urMaterialInfosStringList.isEmpty()) {
			List<UrMaterialInfo> urMaterialInfos = new ArrayList<UrMaterialInfo>(urMaterialInfosStringList.size());
			for (String urMaterialInfosString : urMaterialInfosStringList) {
				urMaterialInfos.addAll(JSON.parseArray(urMaterialInfosString, UrMaterialInfo.class));
			}
			return urMaterialInfos;
		}
		return null;
	}
	
	
	public static List<UrMaterialInfo> getUrMaterialInfos(Integer taskId, Integer boxId) {
		String urMaterialInfosString = cache.hget(UW_UR_TASK_MATERIAL_INFO_MAP, taskId + "_" + boxId);
		if (urMaterialInfosString != null && !urMaterialInfosString.equals("")) {
			List<UrMaterialInfo> urMaterialInfos = JSON.parseArray(urMaterialInfosString, UrMaterialInfo.class);
			return urMaterialInfos;
		}
		return Collections.emptyList();
	}
	
	
	public static Integer getUrTask() {
		Set<Object> keySet = cache.hkeys(UW_UR_TASK_MATERIAL_INFO_MAP);
		if (keySet != null && !keySet.isEmpty()) {
			for (Object object : keySet) {
				String key = String.valueOf(object);
				return Integer.valueOf(key.split("_")[0]);
			}
		}
		return null;
	}
	
	
	public static void removeUrMaterialInfosByTaskAndBox(Integer taskId, Integer boxId) {
		synchronized (UW_UR_TASK_MATERIAL_INFO_MAP_LOCK) {
			cache.hdel(UW_UR_TASK_MATERIAL_INFO_MAP, taskId + "_" + boxId);
		}
	}
	
	
	public static void removeUrMaterialInfos() {
		synchronized (UW_UR_TASK_MATERIAL_INFO_MAP_LOCK) {
			cache.del(UW_UR_TASK_MATERIAL_INFO_MAP);
		}
	}
	
	
	
	
	
	public static void main(String[] args) {
		RedisPlugin rp = new RedisPlugin("uw", "10.10.11.90", 6379, "jimiuw");
	    // 与web下唯一区别是需要这里调用一次start()方法
		rp.setSerializer(new VisualSerializer());
	    rp.start();
	    cache = Redis.use();
	    UrMaterialInfo urMaterialInfo1 = new UrMaterialInfo("123", 1, 2,4,5,1,1, true,1,100,0);
	    UrMaterialInfo urMaterialInfo2 = new UrMaterialInfo("124", 2, 2,3,4,4,1,true,1,100,0);
	    List<UrMaterialInfo> urMaterialInfos = new ArrayList<>();
	    urMaterialInfos.add(urMaterialInfo1);
	    urMaterialInfos.add(urMaterialInfo2);
	    putUrMaterialInfos(12, 1, urMaterialInfos);
	    List<UrMaterialInfo> a = getAllUrMaterialInfos();
	    System.out.println(a);
	}
}
