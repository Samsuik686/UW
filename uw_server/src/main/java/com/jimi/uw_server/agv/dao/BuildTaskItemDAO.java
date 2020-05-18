/**  
*  
*/  
package com.jimi.uw_server.agv.dao;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.json.Json;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jimi.uw_server.agv.entity.bo.AGVBuildTaskItem;

/**  
 * <p>Title: BuildTaskItemDAO</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年5月22日
 *
 */
public class BuildTaskItemDAO {

	private static final String UW_BUILD_TASK_SUFFIX = "UW:BUILD_TASK_";
	
	private static Cache cache = Redis.use();
	
	
	/**
	 * 添加建仓任务条目，该方法会把新的任务条目插入到现有的任务列表当中<br>
	 */
	public synchronized static void addBuildTaskItem(List<AGVBuildTaskItem> buildTaskItems) {
		cache.del(UW_BUILD_TASK_SUFFIX);
		for (AGVBuildTaskItem item : buildTaskItems) {
			cache.lpush(UW_BUILD_TASK_SUFFIX, Json.getJson().toJson(item));
		}
		
		
		
	}


	/**
	 * 把redis的tilOfBuild内容追加到参数里然后返回
	 */
	@SuppressWarnings("unchecked")
	public synchronized static List<AGVBuildTaskItem> appendBuildTaskItems(List<AGVBuildTaskItem> buildTaskItems) {
		List<String> items = cache.lrange(UW_BUILD_TASK_SUFFIX, 0, -1);
		for (String item : items) {
			buildTaskItems.add(Json.getJson().parse(item, AGVBuildTaskItem.class));
		}
		return buildTaskItems;
	}


	/**
	 * 返回建仓任务条目列表的副本
	 */
	public synchronized static List<AGVBuildTaskItem> getBuildTaskItems() {
		List<AGVBuildTaskItem> buildTaskItems = new ArrayList<>();
		return appendBuildTaskItems(buildTaskItems);
	}


	/**
	 * 批量删除指定的建仓任务条目<br>
	 */
	public static void removeBuildTaskItemBySrcPosition(String srcPosition) {
		for (int i = 0; i < cache.llen(UW_BUILD_TASK_SUFFIX); i++) {
			String item = cache.lindex(UW_BUILD_TASK_SUFFIX, i);
			AGVBuildTaskItem agvBuildTaskItem = Json.getJson().parse(item, AGVBuildTaskItem.class);
			if (agvBuildTaskItem.getSrcPosition().equals(srcPosition)) {
				cache.lrem(UW_BUILD_TASK_SUFFIX, 1, item);
				i--;
			}
		}
	}


	/**
	 * 删除某条指定的建仓任务条目<br>
	 */
	public static void removeBuildTaskItemByBoxId(int boxId) {
		for (int i = 0; i < cache.llen(UW_BUILD_TASK_SUFFIX); i++) {
			String item = cache.lindex(UW_BUILD_TASK_SUFFIX, i);
			AGVBuildTaskItem agvBuildTaskItem = Json.getJson().parse(item, AGVBuildTaskItem.class);
			if (agvBuildTaskItem.getBoxId().intValue() == boxId) {
				cache.lrem(UW_BUILD_TASK_SUFFIX, 1, item);
				i--;
			}
		}
	}


	/**
	 *  填写指定建仓任务条目的执行机器
	 */
	public synchronized static void updateBuildTaskItemRobot(AGVBuildTaskItem buildTaskItem, int robotid) {
		for (int i = 0; i < cache.llen(UW_BUILD_TASK_SUFFIX); i++) {
			String item = cache.lindex(UW_BUILD_TASK_SUFFIX, i);
			AGVBuildTaskItem agvBuildTaskItem = Json.getJson().parse(item, AGVBuildTaskItem.class);
			if (agvBuildTaskItem.getBoxId().intValue() == buildTaskItem.getBoxId().intValue()) {
				agvBuildTaskItem.setRobotId(robotid);
				cache.lset(UW_BUILD_TASK_SUFFIX, i, Json.getJson().toJson(agvBuildTaskItem));
				break;
			}
		}
	}


	/**
	 * 更新建仓任务条目执行状态<br>
	 */
	public synchronized static void updateBuildTaskItemState(AGVBuildTaskItem buildTaskItem, int state) {
		for (int i = 0; i < cache.llen(UW_BUILD_TASK_SUFFIX); i++) {
			String item = cache.lindex(UW_BUILD_TASK_SUFFIX, i);
			AGVBuildTaskItem agvBuildTaskItem = Json.getJson().parse(item, AGVBuildTaskItem.class);
			if (agvBuildTaskItem.getBoxId().intValue() == buildTaskItem.getBoxId().intValue()) {
				agvBuildTaskItem.setState(state);
				cache.lset(UW_BUILD_TASK_SUFFIX, i, Json.getJson().toJson(agvBuildTaskItem));
				break;
			}
		}
	}

}
