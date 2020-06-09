/**  
*  
*/
package com.jimi.uw_server.agv.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import com.jfinal.json.Jackson;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jfinal.plugin.redis.RedisPlugin;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.comparator.IOTaskItemComparator;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.util.VisualSerializer;

/**
 * <p>
 * Title: IOTaskItemRedisDAO
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
 * @date 2020年5月22日
 *
 */
public class IOTaskItemRedisDAO {

	private static final String UW_IO_TASK_SUFFIX = "UW:IO_TASK_";

	private static Cache cache = Redis.use();


	/**
	 * 添加出入库任务条目，一个任务仅能添加一次<br>
	 */
	public synchronized static void addIOTaskItem(Integer taskId, List<AGVIOTaskItem> taskItems) {
		cache.del(UW_IO_TASK_SUFFIX + taskId);
		for (AGVIOTaskItem taskItem : taskItems) {
			cache.hset(UW_IO_TASK_SUFFIX + taskId, taskItem.getId(), Jackson.getJson().toJson(taskItem));
		}
	}


	/**
	 * 删除指定任务id的未分配的条目<br>
	 */
	@SuppressWarnings("unchecked")
	public synchronized static void removeUnAssignedTaskItemByTaskId(int taskId) {
		List<String> list = cache.hvals(UW_IO_TASK_SUFFIX + taskId);
		if (list == null || list.isEmpty()) {
			return;
		}
		for (String item : list) {
			AGVIOTaskItem taskItem = Jackson.getJson().parse(item, AGVIOTaskItem.class);
			if (taskItem.getState().intValue() == TaskItemState.WAIT_SCAN || taskItem.getState().intValue() == TaskItemState.WAIT_ASSIGN) {
				cache.hdel(UW_IO_TASK_SUFFIX + taskId, taskItem.getId());

			}
		}
	}


	/**
	 * 删除指定任务id的条目<br>
	 */
	public synchronized static void removeTaskItemByTaskId(int taskId) {
		cache.del(UW_IO_TASK_SUFFIX + taskId);
	}


	/**
	 * 填写指定出入库任务条目的信息
	 */
	public synchronized static void updateIOTaskItemInfo(AGVIOTaskItem taskItem, Integer state, Integer windowId, Integer goodsLocationId, Integer boxId, Integer robotId, Boolean isForceFinish,
			Boolean isCut, Integer oldWindowId, Integer uwQuantity, Integer deductionQuantity) {
		String item = cache.hget(UW_IO_TASK_SUFFIX + taskItem.getTaskId(), taskItem.getId());
		if (item == null || item.trim().equals("")) {
			return;
		}
		AGVIOTaskItem agvioTaskItem = Jackson.getJson().parse(new String(item), AGVIOTaskItem.class);
		if (agvioTaskItem.getId().intValue() == taskItem.getId().intValue()) {
			if (state != null) {
				agvioTaskItem.setState(state);
			}
			if (windowId != null) {
				agvioTaskItem.setWindowId(windowId);
			}
			if (goodsLocationId != null) {
				agvioTaskItem.setGoodsLocationId(goodsLocationId);
			}
			if (boxId != null) {
				agvioTaskItem.setBoxId(boxId);
			}
			if (robotId != null) {
				agvioTaskItem.setRobotId(robotId);
			}
			if (isForceFinish != null) {
				agvioTaskItem.setIsForceFinish(isForceFinish);
			}
			if (isCut != null) {
				agvioTaskItem.setIsCut(isCut);
			}
			if (oldWindowId != null) {
				agvioTaskItem.setOldWindowId(oldWindowId);
			}
			if (uwQuantity != null) {
				agvioTaskItem.setUwQuantity(uwQuantity);
			}
			if (deductionQuantity != null) {
				agvioTaskItem.setDeductionQuantity(deductionQuantity);
			}
		}
		cache.hset(UW_IO_TASK_SUFFIX + taskItem.getTaskId(), taskItem.getId(), Jackson.getJson().toJson(agvioTaskItem));
	}


	/**
	 * 返回出入库任务条目列表的副本
	 */
	public synchronized static List<AGVIOTaskItem> getIOTaskItems(Integer taskId) {
		return getIOTaskItems(taskId, null, null);
	}


	@SuppressWarnings("unchecked")
	public synchronized static List<AGVIOTaskItem> getIOTaskItems(Integer taskId, Integer startLine, Integer endLine) {
		List<String> items = cache.hvals(UW_IO_TASK_SUFFIX + taskId);
		if (items == null || items.isEmpty()) {
			return Collections.emptyList();
		}
		List<AGVIOTaskItem> ioTaskItems = new ArrayList<>(items.size());
		for (String item : items) {
			ioTaskItems.add(Jackson.getJson().parse(item, AGVIOTaskItem.class));
		}
		ioTaskItems.sort(IOTaskItemComparator.me);
		if (startLine == null || endLine == null) {
			return ioTaskItems;
		}
		if (startLine >= endLine) {
			return Collections.emptyList();
		}
		if (endLine > ioTaskItems.size()) {
			endLine = ioTaskItems.size();
		}
		List<AGVIOTaskItem> subAgvioTaskItems = new ArrayList<>(ioTaskItems.subList(startLine, endLine));
		return subAgvioTaskItems;
	}


	/**
	 * 
	 * <p>
	 * Description: 根据任务ID和任务条目ID返回对应的redis条目，返回可能为空
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年6月1日
	 */
	public synchronized static AGVIOTaskItem getIOTaskItem(Integer taskId, Integer taskItemId) {
		String item = cache.hget(UW_IO_TASK_SUFFIX + taskId, taskItemId);
		if (item == null || item.trim().equals("")) {
			return null;
		}
		AGVIOTaskItem taskItem = Jackson.getJson().parse(item, AGVIOTaskItem.class);
		return taskItem;
	}


	@SuppressWarnings("unchecked")
	public synchronized static Map<Integer, AGVIOTaskItem> getIOTaskItemMap(Integer taskId) {
		Map<String, String> map = cache.hgetAll(UW_IO_TASK_SUFFIX + taskId);
		Map<Integer, AGVIOTaskItem> itemMap = new HashMap<>();
		if (!map.isEmpty()) {
			for (Entry<String, String> entry : map.entrySet()) {
				itemMap.put(Integer.valueOf(entry.getKey()), Jackson.getJson().parse(entry.getValue(), AGVIOTaskItem.class));
			}
		}
		return itemMap;
	}


	public static void main(String[] args) {
		RedisPlugin rp = new RedisPlugin("myRedis", "localhost");
		// 与web下唯一区别是需要这里调用一次start()方法
		rp.setSerializer(new VisualSerializer());
		rp.start();
		cache = Redis.use();
		List<AGVIOTaskItem> taskItems = new ArrayList<>();
		taskItems.add(createAGVIOTaskItem(1222, 23));
		taskItems.add(createAGVIOTaskItem(1222, 31));
		taskItems.add(createAGVIOTaskItem(1222, 12));
		taskItems.add(createAGVIOTaskItem(1222, 1));
		taskItems.add(createAGVIOTaskItem(1222, 54));
		addIOTaskItem(1000, taskItems);

		Map<Integer, AGVIOTaskItem> map = getIOTaskItemMap(1000);
		List<AGVIOTaskItem> items = getIOTaskItems(1000);
		AGVIOTaskItem agvioTaskItem = getIOTaskItem(1000, 31);
		System.out.println(map);
		System.out.println(items);
		System.out.println(agvioTaskItem);

	}


	private static AGVIOTaskItem createAGVIOTaskItem(Integer taskId, Integer id) {
		PackingListItem packingListItem = new PackingListItem();
		packingListItem.setId(id);
		packingListItem.setTaskId(taskId);
		packingListItem.setMaterialTypeId(new Random().nextInt(1000));
		packingListItem.setQuantity(new Random().nextInt(10000));
		AGVIOTaskItem agvioTaskItem = new AGVIOTaskItem(packingListItem, 0, 0, false);
		return agvioTaskItem;
	}
}
