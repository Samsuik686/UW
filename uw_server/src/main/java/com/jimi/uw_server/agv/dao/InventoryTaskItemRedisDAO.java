/**  
*  
*/  
package com.jimi.uw_server.agv.dao;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.json.Json;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jimi.uw_server.agv.entity.bo.AGVInventoryTaskItem;

/**  
 * <p>Title: InventoryTaskItemRedisDAO</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年5月22日
 *
 */
public class InventoryTaskItemRedisDAO {
	
	private static final String UW_INVENTORY_TASK_SUFFIX = "UW:INVENTORY_TASK_";
	
	private static Cache cache = Redis.use();
	
	/**
	 * 添加盘点任务条目，该方法会把新的任务条目插入到现有的任务列表当中<br>
	 */
	public synchronized static void addInventoryTaskItem(Integer taskId, List<AGVInventoryTaskItem> agvInventoryTaskItem) {
		cache.del(UW_INVENTORY_TASK_SUFFIX + taskId);
		for (AGVInventoryTaskItem item : agvInventoryTaskItem) {
			cache.lpush(UW_INVENTORY_TASK_SUFFIX + taskId, Json.getJson().toJson(item));
	}
		
		
	}


	/**
	 * 删除指定任务id的盘点条目<br>
	 */
	public static void removeInventoryTaskItemByTaskId(int taskId) {
		cache.del(UW_INVENTORY_TASK_SUFFIX + taskId);
	}


	/**
	 * 删除指定的盘点任务条目<br>
	 */
	public static void removeInventoryTaskItemById(Integer taskId, Integer boxId) {
		for (int i = 0; i < cache.llen(UW_INVENTORY_TASK_SUFFIX + taskId); i++) {
			String item = cache.lindex(UW_INVENTORY_TASK_SUFFIX + taskId, i);
			AGVInventoryTaskItem agvioTaskItem = Json.getJson().parse(item, AGVInventoryTaskItem.class);
			if (agvioTaskItem.getGroupId().equals(taskId + "@" + boxId)) {
				cache.lrem(UW_INVENTORY_TASK_SUFFIX + taskId, 1, item);
				i--;
			}
		}
	}


	/**
	 *  填写指定盘点任务条目的信息
	 */
	public synchronized static void updateInventoryTaskItemInfo(AGVInventoryTaskItem taskItem, Integer state, Integer windowId, Integer goodsLocationId, Integer robotId, Boolean isForceFinish) {
		for (int i = 0; i < cache.llen(UW_INVENTORY_TASK_SUFFIX + taskItem.getTaskId()); i++) {
			String item = cache.lindex(UW_INVENTORY_TASK_SUFFIX + taskItem.getTaskId(), i);
			AGVInventoryTaskItem agvInventoryTaskItem = Json.getJson().parse(item, AGVInventoryTaskItem.class);
			if (agvInventoryTaskItem.getGroupId().equals(taskItem.getGroupId())) {
				if (state != null) {
					agvInventoryTaskItem.setState(state);
				}
				if (windowId != null) {
					agvInventoryTaskItem.setWindowId(windowId);
				}
				if (goodsLocationId != null) {
					agvInventoryTaskItem.setGoodsLocationId(goodsLocationId);
				}
				if (robotId != null) {
					agvInventoryTaskItem.setRobotId(robotId);
				}
				if (isForceFinish != null) {
					agvInventoryTaskItem.setIsForceFinish(isForceFinish);
				}
				cache.lset(UW_INVENTORY_TASK_SUFFIX + taskItem.getTaskId(), i, Json.getJson().toJson(agvInventoryTaskItem));
				break;
			}
		}
	}


	/**
	 * 返回盘点任务条目列表的副本
	 */
	@SuppressWarnings("unchecked")
	public synchronized static List<AGVInventoryTaskItem> getInventoryTaskItems(Integer taskId) {
		List<AGVInventoryTaskItem> inventoryTaskItems = new ArrayList<>();
		List<String> items = cache.lrange(UW_INVENTORY_TASK_SUFFIX + taskId, 0, -1);
		for (String item : items) {
			inventoryTaskItems.add(Json.getJson().parse(item, AGVInventoryTaskItem.class));
		}
		return inventoryTaskItems;
	}

}
