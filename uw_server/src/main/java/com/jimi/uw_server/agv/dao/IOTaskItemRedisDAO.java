/**  
*  
*/  
package com.jimi.uw_server.agv.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jfinal.json.Json;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.comparator.PriorityComparator;
import com.jimi.uw_server.constant.TaskItemState;

/**  
 * <p>Title: IOTaskItemRedisDAO</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年5月22日
 *
 */
public class IOTaskItemRedisDAO {

	private static final String UW_IO_TASK_SUFFIX = "UW:IO_TASK_";
	
	private static Cache cache = Redis.use();
	
	/**
	 * 添加出入库任务条目，该方法会把新的任务条目插入到现有的任务列表当中，并把它们按任务优先级轮流排序<br>
	 */
	public synchronized static void addIOTaskItem(Integer taskId, List<AGVIOTaskItem> ioTaskItems) {
		ioTaskItems.sort(new PriorityComparator());
		cache.del(UW_IO_TASK_SUFFIX + taskId);
		for (AGVIOTaskItem item : ioTaskItems) {
			cache.lpush(UW_IO_TASK_SUFFIX + taskId, Json.getJson().toJson(item));
		}
	}


	/**
	 * 删除指定任务id的未分配的条目<br>
	 */
	public synchronized static void removeUnAssignedTaskItemByTaskId(int taskId) {
		for (int i = 0; i < cache.llen(UW_IO_TASK_SUFFIX + taskId); i++) {
			String item = cache.lindex(UW_IO_TASK_SUFFIX + taskId, i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(item, AGVIOTaskItem.class);
			if (agvioTaskItem.getTaskId().intValue() == taskId && (agvioTaskItem.getState().intValue() == TaskItemState.WAIT_SCAN || agvioTaskItem.getState().intValue() == TaskItemState.WAIT_ASSIGN)) {
				cache.lrem(UW_IO_TASK_SUFFIX + taskId, 1, item);
				i--;
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
	 * 删除指定的出入库任务条目<br>
	 */
	public synchronized static void removeTaskItemByPackingListId(Integer taskId, int packingListId) {
		for (int i = 0; i < cache.llen(UW_IO_TASK_SUFFIX + taskId); i++) {
			String item = cache.lindex(UW_IO_TASK_SUFFIX + taskId, i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(item, AGVIOTaskItem.class);
			if (agvioTaskItem.getId().intValue() == packingListId) {
				cache.lrem(UW_IO_TASK_SUFFIX + taskId, 1, item);
				i--;
			}
		}
	}


	/**
	 *  填写指定出入库任务条目的信息
	 */
	public synchronized static void updateIOTaskItemInfo(AGVIOTaskItem taskItem, Integer state, Integer windowId, Integer goodsLocationId, Integer boxId, Integer robotId, Boolean isForceFinish, Boolean isCut, Integer oldWindowId, Integer uwQuantity, Integer deductionQuantity) {
		for (int i = 0; i < cache.llen(UW_IO_TASK_SUFFIX + taskItem.getTaskId()); i++) {
			String item = cache.lindex(UW_IO_TASK_SUFFIX + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
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
				cache.lset(UW_IO_TASK_SUFFIX + taskItem.getTaskId(), i, Json.getJson().toJson(agvioTaskItem));
				break;
			}
		}
	}


	/**
	 * 返回出入库任务条目列表的副本
	 */
	public synchronized static List<AGVIOTaskItem> getIOTaskItems(Integer taskId) {
		return getIOTaskItems(taskId, 0, -1);
	}


	
	@SuppressWarnings("unchecked")
	public synchronized static List<AGVIOTaskItem> getIOTaskItems(Integer taskId, Integer startLine, Integer endLine) {
		List<AGVIOTaskItem> ioTaskItems = new ArrayList<>();
		List<String> items = cache.lrange(UW_IO_TASK_SUFFIX + taskId, startLine, endLine);
		for (String item : items) {
			ioTaskItems.add(Json.getJson().parse(item, AGVIOTaskItem.class));
		}
		return ioTaskItems;
	}
	
	
	public synchronized static Map<Integer, AGVIOTaskItem> getIOTaskItemMap(Integer taskId) {
		List<AGVIOTaskItem> ioTaskItems = getIOTaskItems(taskId, 0, -1);
		Map<Integer, AGVIOTaskItem> map = new HashMap<>();
		if (!ioTaskItems.isEmpty()) {
			for (AGVIOTaskItem agvioTaskItem : ioTaskItems) {
				map.put(agvioTaskItem.getId(), agvioTaskItem);
			}
		}
		return map;
	}
}
