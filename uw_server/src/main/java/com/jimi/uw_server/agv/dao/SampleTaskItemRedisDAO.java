/**  
*  
*/
package com.jimi.uw_server.agv.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jfinal.json.Jackson;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jimi.uw_server.agv.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.comparator.SampleTaskItemComparator;
import com.jimi.uw_server.constant.TaskItemState;

/**
 * <p>
 * Title: SampleTaskItemRedisDAO
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
public class SampleTaskItemRedisDAO {

	private static final String UW_SAMPLE_TASK_SUFFIX = "UW:SAMPLE_TASK_";

	private static Cache cache = Redis.use();


	/**
	 * 添加抽检任务条目，该方法会把新的任务条目插入到现有的任务列表当中<br>
	 */
	public synchronized static void addSampleTaskItem(Integer taskId, List<AGVSampleTaskItem> agvSampleTaskItems) {
		cache.del(UW_SAMPLE_TASK_SUFFIX + taskId);
		for (AGVSampleTaskItem item : agvSampleTaskItems) {
			cache.hset(UW_SAMPLE_TASK_SUFFIX + taskId, item.getBoxId(), Jackson.getJson().toJson(item));
		}

	}


	/**
	 * 删除指定任务id的抽检条目<br>
	 */
	public static void removeSampleTaskItemByTaskId(int taskId) {
		cache.del(UW_SAMPLE_TASK_SUFFIX + taskId);
	}


	/**
	 * 填写指定出入库任务条目的信息
	 */
	public synchronized static void updateSampleTaskItemInfo(AGVSampleTaskItem taskItem, Integer state, Integer windowId, Integer goodsLocationId, Integer robotId, Boolean isForceFinish) {
		String item = cache.hget(UW_SAMPLE_TASK_SUFFIX + taskItem.getTaskId(), taskItem.getBoxId());
		if (item == null || item.trim().equals("")) {
			return;
		}
		AGVSampleTaskItem agvSampleTaskItem = Jackson.getJson().parse(item, AGVSampleTaskItem.class);
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
		cache.hset(UW_SAMPLE_TASK_SUFFIX + taskItem.getTaskId(), taskItem.getBoxId(), Jackson.getJson().toJson(agvSampleTaskItem));
	}


	/**
	 * 返回抽检任务条目列表的副本
	 */
	public synchronized static List<AGVSampleTaskItem> getSampleTaskItems(Integer taskId) {
		List<AGVSampleTaskItem> agvSampleTaskItems = getSampleTaskItems(taskId, null, null);
		return agvSampleTaskItems;
	}


	@SuppressWarnings("unchecked")
	public synchronized static List<AGVSampleTaskItem> getSampleTaskItems(Integer taskId, Integer startLine, Integer endLine) {
		List<String> items = cache.hvals(UW_SAMPLE_TASK_SUFFIX + taskId);
		if (items == null || items.isEmpty()) {
			return Collections.emptyList();
		}
		List<AGVSampleTaskItem> agvSampleTaskItems = new ArrayList<>(items.size());

		for (String item : items) {
			agvSampleTaskItems.add(Jackson.getJson().parse(item, AGVSampleTaskItem.class));
		}
		agvSampleTaskItems.sort(SampleTaskItemComparator.me);
		if (startLine == null || endLine == null) {
			return agvSampleTaskItems;
		}
		if (startLine >= endLine) {
			return Collections.emptyList();
		}
		if (endLine > agvSampleTaskItems.size()) {
			endLine = agvSampleTaskItems.size();
		}
		List<AGVSampleTaskItem> subAGVSampleTaskItems = new ArrayList<>(agvSampleTaskItems.subList(startLine, endLine));
		return subAGVSampleTaskItems;
	}


	/**
	 * 删除指定任务id的未分配的条目<br>
	 */
	@SuppressWarnings("unchecked")
	public synchronized static void removeUnAssignedSampleTaskItemByTaskId(int taskId) {
		List<String> items = cache.hvals(UW_SAMPLE_TASK_SUFFIX + taskId);
		if (items == null || items.isEmpty()) {
			return;
		}
		List<AGVSampleTaskItem> agvSampleTaskItems = new ArrayList<>(items.size());

		for (String item : items) {
			agvSampleTaskItems.add(Jackson.getJson().parse(item, AGVSampleTaskItem.class));
		}
		for (AGVSampleTaskItem agvSampleTaskItem : agvSampleTaskItems) {
			if ((agvSampleTaskItem.getState().intValue() == TaskItemState.WAIT_ASSIGN)) {
				cache.hdel(UW_SAMPLE_TASK_SUFFIX + taskId, agvSampleTaskItem.getBoxId());
			}
		}
	}


	/**
	 * <p>
	 * Description:
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年6月2日
	 */
	public static AGVSampleTaskItem getSampleTaskItem(Integer taskId, Integer boxId) {
		String item = cache.hget(UW_SAMPLE_TASK_SUFFIX + taskId, boxId);
		if (item == null || item.trim().equals("")) {
			return null;
		}
		AGVSampleTaskItem agvSampleTaskItem = Jackson.getJson().parse(item, AGVSampleTaskItem.class);
		return agvSampleTaskItem;
	}

}
