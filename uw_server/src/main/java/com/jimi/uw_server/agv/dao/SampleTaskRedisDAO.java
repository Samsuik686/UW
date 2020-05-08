/**  
*  
*/  
package com.jimi.uw_server.agv.dao;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.json.Json;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jimi.uw_server.agv.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.constant.TaskItemState;

/**  
 * <p>Title: SampleTaskRedisDAO</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年4月29日
 *
 */
public class SampleTaskRedisDAO {

	private static final String UW_SAMPLE_TASK_SUFFIX = "UW_SAMPLE_TASK_";
	
	private static Cache cache = Redis.use();
	
	/**
	 * 添加抽检任务条目，该方法会把新的任务条目插入到现有的任务列表当中<br>
	 */
	public synchronized static void addSampleTaskItem(Integer taskId, List<AGVSampleTaskItem> agvSampleTaskItems) {
		appendSampleTaskItems(taskId, agvSampleTaskItems);
		cache.del(UW_SAMPLE_TASK_SUFFIX + taskId);
		for (AGVSampleTaskItem item : agvSampleTaskItems) {
			cache.lpush(UW_SAMPLE_TASK_SUFFIX + taskId, Json.getJson().toJson(item));
		}
		
	}


	/**
	 * 删除指定任务id的抽检条目<br>
	 */
	public static void removeSampleTaskItemByTaskId(int taskId) {
		cache.del(UW_SAMPLE_TASK_SUFFIX + taskId);
	}


	/**
	 * 删除指定的抽检任务条目<br>
	 */
	public static void removeSampleTaskItemById(Integer taskId, Integer boxId) {
		for (int i = 0; i < cache.llen(UW_SAMPLE_TASK_SUFFIX + taskId); i++) {
			String item = cache.lindex(UW_SAMPLE_TASK_SUFFIX + taskId, i);
			AGVSampleTaskItem agvSampleTaskItem = Json.getJson().parse(item, AGVSampleTaskItem.class);
			if (agvSampleTaskItem.getGroupId().equals(taskId + "#" + boxId)) {
				cache.lrem(UW_SAMPLE_TASK_SUFFIX + taskId, 1, item);
				i--;
			}
		}
	}


	/**
	 *  填写指定出入库任务条目的信息
	 */
	public synchronized static void updateSampleTaskItemInfo(AGVSampleTaskItem taskItem, Integer state, Integer windowId, Integer goodsLocationId, Integer robotId, Boolean isForceFinish) {
		for (int i = 0; i < cache.llen(UW_SAMPLE_TASK_SUFFIX + taskItem.getTaskId()); i++) {
			String item = cache.lindex(UW_SAMPLE_TASK_SUFFIX + taskItem.getTaskId(), i);
			AGVSampleTaskItem agvSampleTaskItem = Json.getJson().parse(item, AGVSampleTaskItem.class);
			if (agvSampleTaskItem.getGroupId().equals(taskItem.getGroupId())) {
				if (state != null) {
					agvSampleTaskItem.setState(state);
				}
				if (windowId != null) {
					agvSampleTaskItem.setWindowId(windowId);
				}
				if (goodsLocationId != null) {
					agvSampleTaskItem.setGoodsLocationId(goodsLocationId);
				}
				if (robotId != null) {
					agvSampleTaskItem.setRobotId(robotId);
				}
				if (isForceFinish != null) {
					agvSampleTaskItem.setIsForceFinish(isForceFinish);
				}
				cache.lset(UW_SAMPLE_TASK_SUFFIX + taskItem.getTaskId(), i, Json.getJson().toJson(agvSampleTaskItem));
				break;
			}
		}
	}


	/**
	 * 返回抽检任务条目列表的副本
	 */
	public synchronized static List<AGVSampleTaskItem> getSampleTaskItems(Integer taskId) {
		List<AGVSampleTaskItem> agvSampleTaskItems = new ArrayList<>();
		return appendSampleTaskItems(taskId, agvSampleTaskItems);
	}


	/**
	 * 把redis的inventoryOfTil内容追加到参数里然后返回
	 */
	@SuppressWarnings("unchecked")
	public synchronized static List<AGVSampleTaskItem> appendSampleTaskItems(Integer taskId, List<AGVSampleTaskItem> agvSampleTaskItems) {
		List<String> items = cache.lrange(UW_SAMPLE_TASK_SUFFIX + taskId, 0, -1);
		for (String item : items) {
			agvSampleTaskItems.add(Json.getJson().parse(new String(item), AGVSampleTaskItem.class));
		}
		return agvSampleTaskItems;
	}


	/**
	 * 删除指定任务id的未分配的条目<br>
	 */
	public synchronized static void removeUnAssignedSampleTaskItemByTaskId(int taskId) {
		for (int i = 0; i < cache.llen(UW_SAMPLE_TASK_SUFFIX + taskId); i++) {
			String item = cache.lindex(UW_SAMPLE_TASK_SUFFIX + taskId, i);
			AGVSampleTaskItem agvSampleTaskItems = Json.getJson().parse(item, AGVSampleTaskItem.class);
			if (agvSampleTaskItems.getTaskId().intValue() == taskId && (agvSampleTaskItems.getState().intValue() == TaskItemState.WAIT_ASSIGN)) {
				cache.lrem(UW_SAMPLE_TASK_SUFFIX + taskId, 1, item);
				i--;
			}
		}
	}

}
