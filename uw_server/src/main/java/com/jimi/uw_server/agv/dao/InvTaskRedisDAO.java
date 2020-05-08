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
import com.jimi.uw_server.constant.enums.UwInvTaskProcessFlagEnum;

/**  
 * <p>Title: InvTaskRedisDAO</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年4月28日
 *
 */
public class InvTaskRedisDAO {

	private static final String UW_INV_TASK_PROCESS_SUFFIX = "UW_INV_TASK_PROCESS_";
	
	private static final String UW_INVENTORY_TASK_SUFFIX = "UW_INVENTORY_TASK_";
	
	private static Cache cache = Redis.use();
	/**
	 * 设置UW仓盘点任务的运行状态
	 */
	public synchronized static void setUwInvTaskProcess(int taskId, UwInvTaskProcessFlagEnum flag) {
		cache.set(UW_INV_TASK_PROCESS_SUFFIX + taskId, flag.getCode());
	}


	public synchronized static int getUwInvTaskProcess(int taskId) {
		String flagStr = cache.get(UW_INV_TASK_PROCESS_SUFFIX + taskId);
		int flag = 0;
		if (flagStr != null) {
			flag = Integer.valueOf(flagStr);
			return flag;
		}
		return flag;
	}
	
	
	public synchronized static void delUwInvTaskProcess(int taskId) {
		cache.del(UW_INV_TASK_PROCESS_SUFFIX + taskId);
	}
	
	/**
	 * 添加盘点任务条目，该方法会把新的任务条目插入到现有的任务列表当中<br>
	 */
	public synchronized static void addInventoryTaskItem(Integer taskId, List<AGVInventoryTaskItem> agvInventoryTaskItem) {
		appendInventoryTaskItems(taskId, agvInventoryTaskItem);
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
	public synchronized static void updateInventoryTaskItemInfo(AGVInventoryTaskItem taskItem, Integer state, Integer windowId, Integer goodsLocationId, Integer robotId, Boolean isForceFinish, Boolean isException) {
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
				if (isException != null) {
					agvInventoryTaskItem.setIsException(isException);
				}
				cache.lset(UW_INVENTORY_TASK_SUFFIX + taskItem.getTaskId(), i, Json.getJson().toJson(agvInventoryTaskItem));
				break;
			}
		}
	}


	/**
	 * 返回盘点任务条目列表的副本
	 */
	public synchronized static List<AGVInventoryTaskItem> getInventoryTaskItems(Integer taskId) {
		List<AGVInventoryTaskItem> inventoryTaskItems = new ArrayList<>();
		return appendInventoryTaskItems(taskId, inventoryTaskItems);
	}


	/**
	 * 把redis的inventoryOfTil内容追加到参数里然后返回
	 */
	@SuppressWarnings("unchecked")
	public synchronized static List<AGVInventoryTaskItem> appendInventoryTaskItems(Integer taskId, List<AGVInventoryTaskItem> agvInventoryTaskItem) {
		List<String> items = cache.lrange(UW_INVENTORY_TASK_SUFFIX + taskId, 0, -1);
		for (String item : items) {
			agvInventoryTaskItem.add(Json.getJson().parse(item, AGVInventoryTaskItem.class));
		}
		return agvInventoryTaskItem;
	}
}
