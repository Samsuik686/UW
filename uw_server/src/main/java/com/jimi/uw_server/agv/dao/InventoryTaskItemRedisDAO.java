/**  
*  
*/  
package com.jimi.uw_server.agv.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import com.jfinal.json.Jackson;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.comparator.InventoryTaskItemComparator;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.util.VisualSerializer;

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
			cache.hset(UW_INVENTORY_TASK_SUFFIX + taskId, item.getBoxId(), Jackson.getJson().toJson(item));
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
		cache.hdel(UW_INVENTORY_TASK_SUFFIX + taskId, boxId);
	}


	/**
	 *  填写指定盘点任务条目的信息
	 */
	public synchronized static void updateInventoryTaskItemInfo(AGVInventoryTaskItem taskItem, Integer state, Integer windowId, Integer goodsLocationId, Integer robotId, Boolean isForceFinish) {
		String item = cache.hget(UW_INVENTORY_TASK_SUFFIX + taskItem.getTaskId(), taskItem.getBoxId());
		if (item == null || item.trim().equals("")) {
			return ;
		}
		AGVInventoryTaskItem agvInventoryTaskItem = Jackson.getJson().parse(new String(item), AGVInventoryTaskItem.class);
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
		}
		cache.hset(UW_INVENTORY_TASK_SUFFIX + taskItem.getTaskId(), taskItem.getBoxId(), Jackson.getJson().toJson(agvInventoryTaskItem));
	}

	
	@SuppressWarnings("unchecked")
	public synchronized static Map<Integer, AGVInventoryTaskItem> getInventoryTaskItemMap(Integer taskId) {
		Map<String, String> map = cache.hgetAll(UW_INVENTORY_TASK_SUFFIX + taskId);
		Map<Integer, AGVInventoryTaskItem> itemMap = new HashMap<>();
		if (!map.isEmpty()) {
			for (Entry<String, String> entry : map.entrySet()) {
				itemMap.put(Integer.valueOf(entry.getKey()), Jackson.getJson().parse(entry.getValue(), AGVInventoryTaskItem.class));
			}
		}
		return itemMap;
	}
	
	
	public synchronized static AGVInventoryTaskItem getInventoryTaskItem(Integer taskId, Integer boxId) {
		String item = cache.hget(UW_INVENTORY_TASK_SUFFIX + taskId, boxId);
		if (item == null || item.trim().equals("")) {
			return null;
		}
		AGVInventoryTaskItem agvInventoryTaskItem = Jackson.getJson().parse(new String(item), AGVInventoryTaskItem.class);
		return agvInventoryTaskItem;
	}
	
	/**
	 * 返回盘点任务条目列表的副本
	 */
	public synchronized static List<AGVInventoryTaskItem> getInventoryTaskItems(Integer taskId) {
		List<AGVInventoryTaskItem> agvInventoryTaskItems = getInventoryTaskItems(taskId, null, null);
		return agvInventoryTaskItems;
	}
	

	/**
	 * 返回盘点任务条目列表的副本
	 */
	@SuppressWarnings("unchecked")
	public synchronized static List<AGVInventoryTaskItem> getInventoryTaskItems(Integer taskId, Integer startLine, Integer endLine) {
		List<String> items = cache.hvals(UW_INVENTORY_TASK_SUFFIX + taskId);
		if (items == null || items.isEmpty()) {
			return Collections.emptyList();
		}
		List<AGVInventoryTaskItem> agvInventoryTaskItems = new ArrayList<>(items.size());

		for (String item : items) {
			agvInventoryTaskItems.add(Jackson.getJson().parse(item, AGVInventoryTaskItem.class));
		}
		agvInventoryTaskItems.sort(InventoryTaskItemComparator.me);
		if (startLine == null || endLine == null) {
			return agvInventoryTaskItems;
		}
		if (startLine >= endLine) {
			return Collections.emptyList();
		}
		if (endLine > agvInventoryTaskItems.size()) {
			endLine = agvInventoryTaskItems.size();
		}
		List<AGVInventoryTaskItem> subAgvInventoryTaskItems = new ArrayList<>(agvInventoryTaskItems.subList(startLine, endLine));
		return subAgvInventoryTaskItems;
	}

	
	public static void main(String[] args) {
	    RedisPlugin rp = new RedisPlugin("myRedis", "localhost");
	    // 与web下唯一区别是需要这里调用一次start()方法
	    rp.setSerializer(new VisualSerializer());
	    rp.start();
	    cache = Redis.use();
	    List<AGVInventoryTaskItem> taskItems = new ArrayList<>();
	    taskItems.add(createAGVInventoryTaskItem(1222, 23));
	    taskItems.add(createAGVInventoryTaskItem(1222, 31));
	    taskItems.add(createAGVInventoryTaskItem(1222, 12));
	    taskItems.add(createAGVInventoryTaskItem(1222, 1));
	    taskItems.add(createAGVInventoryTaskItem(1222, 54));
	    addInventoryTaskItem(1000, taskItems);
	    
	    List<AGVInventoryTaskItem> items = getInventoryTaskItems(1000);
	    List<AGVInventoryTaskItem> items2 = getInventoryTaskItems(1000, 0, 1);
	    System.out.println(items);
	    System.out.println(items2);

	  }
	
	private static AGVInventoryTaskItem createAGVInventoryTaskItem(Integer taskId, Integer boxId) {
	    AGVInventoryTaskItem agvioTaskItem = new AGVInventoryTaskItem(taskId, boxId, 0, 0, 0, 1);
	    return agvioTaskItem;
	}
}
