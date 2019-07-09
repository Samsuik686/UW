package com.jimi.uw_server.agv.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jfinal.json.Json;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jimi.uw_server.agv.entity.bo.AGVBuildTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVInventoryTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.comparator.PriorityComparator;
import com.jimi.uw_server.constant.TaskItemState;



/**
 * AGV任务条目Redis数据访问对象
 * <br>
 * <b>2018年6月15日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class TaskItemRedisDAO {
	
	public static final String UNDEFINED = "undefined";
	
	private static Cache cache = Redis.use();
	
	
	/**
	 * 添加出入库任务条目，该方法会把新的任务条目插入到现有的任务列表当中，并把它们按任务优先级轮流排序<br>
	 */
	public synchronized static void addIOTaskItem(Integer taskId, List<AGVIOTaskItem> ioTaskItems) {
		appendIOTaskItems(taskId, ioTaskItems);
		ioTaskItems.sort(new PriorityComparator());
		List<byte[]> items = new ArrayList<>();
		for (AGVIOTaskItem item : ioTaskItems) {
			items.add(Json.getJson().toJson(item).getBytes());
		}
		cache.del("IO_TASK_" + taskId);
		cache.lpush("IO_TASK_" + taskId, items.toArray());
	}
	
	
	/**
	 * 删除指定任务id的未分配的条目<br>
	 */
	public synchronized static void removeUnAssignedTaskItemByTaskId(int taskId) {
		for (int i = 0; i < cache.llen("IO_TASK_" + taskId); i++) {
			byte[] item = cache.lindex("IO_TASK_" + taskId, i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getTaskId().intValue() == taskId && (agvioTaskItem.getState().intValue() == TaskItemState.WAIT_SCAN || agvioTaskItem.getState().intValue() == TaskItemState.WAIT_ASSIGN)){
				cache.lrem("IO_TASK_" + taskId, 1, item);
				i--;
			}
		}
	}
	
	
	/**
	 * 删除指定任务id的条目<br>
	 */
	public synchronized static void removeTaskItemByTaskId(int taskId) {
		cache.del("IO_TASK_" + taskId);
	}


	/**
	 * 删除指定的出入库任务条目<br>
	 */
	public synchronized static void removeTaskItemByPackingListId(Integer taskId, int packingListId) {
		for (int i = 0; i < cache.llen("IO_TASK_" + taskId); i++) {
			byte[] item = cache.lindex("IO_TASK_" + taskId, i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == packingListId){
				cache.lrem("IO_TASK_" + taskId, 1, item);
				i--;
			}
		}
	}


	/**
	 * 更新出入库任务条目执行状态<br>
	 *//*
	public synchronized static void updateIOTaskItemState(AGVIOTaskItem taskItem, int state) {
		for (int i = 0; i < cache.llen("IO_TASK_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("IO_TASK_" + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setState(state);
				cache.lset("IO_TASK_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}

	
	*//**
	 * 更新出入库任务条目是否截料<br>
	 *//*
	public synchronized static void updateIOTaskItemIsCut(AGVIOTaskItem taskItem, Boolean flag) {
		for (int i = 0; i < cache.llen("IO_TASK_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("IO_TASK_" + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setIsCut(flag);
				cache.lset("IO_TASK_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}
	

	*//**
	 * 更新任务条目为已完成，标识出入库数量已满足实际需求<br>
	 *//*
	public synchronized static void updateIOTaskIsForceFinish( AGVIOTaskItem taskItem, boolean isForceFinish) {
		for (int i = 0; i < cache.llen("IO_TASK_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("IO_TASK_" + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setIsForceFinish(isForceFinish);
				cache.lset("IO_TASK_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}


	*//**
	 * 更新任务条目盒号<br>
	 *//*
	public synchronized static void updateIOTaskItemBoxId( AGVIOTaskItem taskItem, int boxId) {
		for (int i = 0; i < cache.llen("IO_TASK_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("IO_TASK_" + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setBoxId(boxId);
				cache.lset("IO_TASK_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}


	*//**
	 *  填写指定出入库任务条目的执行机器
	 *//*
	public synchronized static void updateIOTaskItemRobot(AGVIOTaskItem taskItem, int robotid) {
		for (int i = 0; i < cache.llen("IO_TASK_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("IO_TASK_" + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setRobotId(robotid);
				cache.lset("IO_TASK_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}*/

	
	/**
	 *  填写指定出入库任务条目的信息
	 */
	public synchronized static void updateIOTaskItemInfo(AGVIOTaskItem taskItem, Integer state, Integer windowId, Integer goodsLocationId, Integer boxId, Integer robotId, Boolean isForceFinish, Boolean isCut) {
		for (int i = 0; i < cache.llen("IO_TASK_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("IO_TASK_" + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
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
				cache.lset("IO_TASK_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}

	/**
	 * 返回出入库任务条目列表的副本
	 */
	public synchronized static List<AGVIOTaskItem> getIOTaskItems(Integer taskId) {
		List<AGVIOTaskItem> ioTaskItems = new ArrayList<>();
		return appendIOTaskItems(taskId, ioTaskItems);
	}


	/**
	 * 把redis的til内容追加到参数里然后返回
	 */
	@SuppressWarnings("unchecked")
	public synchronized static List<AGVIOTaskItem> appendIOTaskItems(Integer taskId, List<AGVIOTaskItem> ioTaskItems) {
		List<byte[]> items = cache.lrange("IO_TASK_" + taskId, 0, -1);
		for (byte[] item : items) {
			ioTaskItems.add(Json.getJson().parse(new String(item), AGVIOTaskItem.class));
		}
		return ioTaskItems;
	} 

	
	/**
	 * 获取一个新的CmdId
	 */
	public synchronized static int getCmdId() {
		int cmdid = 0;
		try {
			cmdid = cache.get("cmdid");
		} catch (NullPointerException e) {
		}
		cmdid%=999999;
		cmdid++;
		cache.set("cmdid", cmdid);
		return cmdid;
	}


	/**
	 * 添加建仓任务条目，该方法会把新的任务条目插入到现有的任务列表当中<br>
	 */
	public synchronized static void addBuildTaskItem(List<AGVBuildTaskItem> buildTaskItems) {
		appendBuildTaskItems(buildTaskItems);
		List<byte[]> items = new ArrayList<>();
		for (AGVBuildTaskItem item : buildTaskItems) {
			items.add(Json.getJson().toJson(item).getBytes());
		}
		Collections.reverse(buildTaskItems);
		cache.del("tilOfBuild");
		cache.lpush("tilOfBuild", items.toArray());
	}


	/**
	 * 把redis的tilOfBuild内容追加到参数里然后返回
	 */
	@SuppressWarnings("unchecked")
	public synchronized static List<AGVBuildTaskItem> appendBuildTaskItems(List<AGVBuildTaskItem> buildTaskItems) {
		List<byte[]> items = cache.lrange("tilOfBuild", 0, -1);
		for (byte[] item : items) {
			buildTaskItems.add(Json.getJson().parse(new String(item), AGVBuildTaskItem.class));
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
		for (int i = 0; i < cache.llen("tilOfBuild"); i++) {
			byte[] item = cache.lindex("tilOfBuild", i);
			AGVBuildTaskItem agvBuildTaskItem = Json.getJson().parse(new String(item), AGVBuildTaskItem.class);
			if(agvBuildTaskItem.getSrcPosition().equals(srcPosition)){
				cache.lrem("tilOfBuild", 1, item);
				i--;
			}
		}
	}


	/**
	 * 删除某条指定的建仓任务条目<br>
	 */
	public static void removeBuildTaskItemByBoxId(int boxId) {
		for (int i = 0; i < cache.llen("tilOfBuild"); i++) {
			byte[] item = cache.lindex("tilOfBuild", i);
			AGVBuildTaskItem agvBuildTaskItem = Json.getJson().parse(new String(item), AGVBuildTaskItem.class);
			if(agvBuildTaskItem.getBoxId().intValue() == boxId){
				cache.lrem("tilOfBuild", 1, item);
				i--;
			}
		}
	}


	/**
	 *  填写指定建仓任务条目的执行机器
	 */
	public synchronized static void updateBuildTaskItemRobot(AGVBuildTaskItem buildTaskItem, int robotid) {
		for (int i = 0; i < cache.llen("tilOfBuild"); i++) {
			byte[] item = cache.lindex("tilOfBuild", i);
			AGVBuildTaskItem agvBuildTaskItem = Json.getJson().parse(new String(item), AGVBuildTaskItem.class);
			if(agvBuildTaskItem.getBoxId().intValue() == buildTaskItem.getBoxId().intValue()){
				agvBuildTaskItem.setRobotId(robotid);
				cache.lset("tilOfBuild", i, Json.getJson().toJson(agvBuildTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 * 更新建仓任务条目执行状态<br>
	 */
	public synchronized static void updateBuildTaskItemState(AGVBuildTaskItem buildTaskItem, int state) {
		for (int i = 0; i < cache.llen("tilOfBuild"); i++) {
			byte[] item = cache.lindex("tilOfBuild", i);
			AGVBuildTaskItem agvBuildTaskItem = Json.getJson().parse(new String(item), AGVBuildTaskItem.class);
			if(agvBuildTaskItem.getBoxId().intValue() == buildTaskItem.getBoxId().intValue()){
				agvBuildTaskItem.setState(state);
				cache.lset("tilOfBuild", i, Json.getJson().toJson(agvBuildTaskItem).getBytes());
				break;
			}
		}
	}
	
	
	/**
	 * 设置agvWebSocket运行状态
	 */
	public synchronized static void setAgvWebSocketStatus(Boolean flag) {
		cache.set("agvWebSocketStatus", flag);
	}
	
	
	public synchronized static Boolean getAgvWebSocketStatus() {
		Boolean flag = cache.get("agvWebSocketStatus");
		if (flag == null) {
			flag = true;
			cache.set("agvWebSocketStatus", true);
		}
		return flag;
	}

	
	/**
	 * 添加盘点任务条目，该方法会把新的任务条目插入到现有的任务列表当中<br>
	 */
	public synchronized static void addInventoryTaskItem(Integer taskId, List<AGVInventoryTaskItem> agvInventoryTaskItem) {
		appendInventoryTaskItems(taskId, agvInventoryTaskItem);
		List<byte[]> items = new ArrayList<>();
		for (AGVInventoryTaskItem item : agvInventoryTaskItem) {
			items.add(Json.getJson().toJson(item).getBytes());
		}
		cache.del("InvTask_" + taskId);
		cache.lpush("InvTask_" + taskId, items.toArray());
	}
	
	
	
	/**
	 * 删除指定任务id的盘点条目<br>
	 */
	public static void removeInventoryTaskItemByTaskId(int taskId) {
		cache.del("InvTask_" + taskId);
	}


	/**
	 * 删除指定的盘点任务条目<br>
	 */
	public static void removeInventoryTaskItemById(Integer taskId, Integer boxId) {
		for (int i = 0; i < cache.llen("InvTask_" + taskId); i++) {
			byte[] item = cache.lindex("InvTask_" + taskId, i);
			AGVInventoryTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVInventoryTaskItem.class);
			if(agvioTaskItem.getGroupId().equals(taskId + "@" + boxId)){
				cache.lrem("InvTask_" + taskId, 1, item);
				i--;
			}
		}
	}

	/**
	 *  填写指定盘点任务条目的信息
	 */
	public synchronized static void updateInventoryTaskItemInfo(AGVInventoryTaskItem taskItem, Integer state, Integer windowId, Integer goodsLocationId, Integer robotId, Boolean isForceFinish) {
		for (int i = 0; i < cache.llen("InvTask_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("InvTask_" + taskItem.getTaskId(), i);
			AGVInventoryTaskItem agvInventoryTaskItem = Json.getJson().parse(new String(item), AGVInventoryTaskItem.class);
			if(agvInventoryTaskItem.getGroupId().equals(taskItem.getGroupId())){
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
				cache.lset("InvTask_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvInventoryTaskItem).getBytes());
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
		List<byte[]> items = cache.lrange("InvTask_" + taskId, 0, -1);
		for (byte[] item : items) {
			agvInventoryTaskItem.add(Json.getJson().parse(new String(item), AGVInventoryTaskItem.class));
		}
		return agvInventoryTaskItem;
	} 

	
	/**
	 * 添加抽检任务条目，该方法会把新的任务条目插入到现有的任务列表当中<br>
	 */
	public synchronized static void addSampleTaskItem(Integer taskId, List<AGVSampleTaskItem> agvSampleTaskItems) {
		appendSampleTaskItems(taskId, agvSampleTaskItems);
		List<byte[]> items = new ArrayList<>();
		for (AGVSampleTaskItem item : agvSampleTaskItems) {
			items.add(Json.getJson().toJson(item).getBytes());
		}
		cache.del("SamTask_" + taskId);
		cache.lpush("SamTask_" + taskId, items.toArray());
	}
	
	
	
	/**
	 * 删除指定任务id的抽检条目<br>
	 */
	public static void removeSampleTaskItemByTaskId(int taskId) {
		cache.del("SamTask_" + taskId);
	}


	/**
	 * 删除指定的抽检任务条目<br>
	 */
	public static void removeSampleTaskItemById(Integer taskId, Integer boxId) {
		for (int i = 0; i < cache.llen("SamTask_" + taskId); i++) {
			byte[] item = cache.lindex("SamTask_" + taskId, i);
			AGVSampleTaskItem agvSampleTaskItem = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItem.getGroupId().equals(taskId + "#" + boxId)){
				cache.lrem("SamTask_" + taskId, 1, item);
				i--;
			}
		}
	}


	/**
	 * 更新抽检任务条目执行状态<br>
	 *//*
	public synchronized static void updateSampleTaskItemState(AGVSampleTaskItem taskItem, int state) {
		for (int i = 0; i < cache.llen("SamTask_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("SamTask_" + taskItem.getTaskId(), i);
			AGVSampleTaskItem agvSampleTaskItem = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItem.getGroupId().equals(taskItem.getGroupId())){
				agvSampleTaskItem.setState(state);
				cache.lset("SamTask_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvSampleTaskItem).getBytes());
				break;
			}
		}
	}


	*//**
	 * 更新抽检任务条目为已完成<br>
	 *//*
	public synchronized static void updateSampleTaskIsForceFinish(AGVSampleTaskItem taskItem, boolean isForceFinish) {
		for (int i = 0; i < cache.llen("SamTask_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("SamTask_" + taskItem.getTaskId(), i);
			AGVSampleTaskItem agvSampleTaskItem = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItem.getGroupId().equals(taskItem.getGroupId())){
				agvSampleTaskItem.setIsForceFinish(isForceFinish);
				cache.lset("SamTask_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvSampleTaskItem).getBytes());
				break;
			}
		}
	}


	*//**
	 *  填写指定抽检任务条目的执行机器
	 *//*
	public synchronized static void updateSampleTaskItemRobot(AGVSampleTaskItem taskItem, int robotid) {
		for (int i = 0; i < cache.llen("SamTask_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("SamTask_" + taskItem.getTaskId(), i);
			AGVSampleTaskItem agvSampleTaskItem = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItem.getGroupId().equals(taskItem.getGroupId())){
				agvSampleTaskItem.setRobotId(robotid);
				cache.lset("SamTask_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvSampleTaskItem).getBytes());
				break;
			}
		}
	}*/

	/**
	 *  填写指定出入库任务条目的信息
	 */
	public synchronized static void updateSampleTaskItemInfo(AGVSampleTaskItem taskItem, Integer state, Integer windowId, Integer goodsLocationId, Integer robotId, Boolean isForceFinish) {
		for (int i = 0; i < cache.llen("SamTask_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("SamTask_" + taskItem.getTaskId(), i);
			AGVSampleTaskItem agvSampleTaskItem = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItem.getGroupId().equals(taskItem.getGroupId())){
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
				cache.lset("SamTask_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvSampleTaskItem).getBytes());
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
	public synchronized static List<AGVSampleTaskItem> appendSampleTaskItems(Integer taskId,List<AGVSampleTaskItem> agvSampleTaskItems) {
		List<byte[]> items = cache.lrange("SamTask_" + taskId, 0, -1);
		for (byte[] item : items) {
			agvSampleTaskItems.add(Json.getJson().parse(new String(item), AGVSampleTaskItem.class));
		}
		return agvSampleTaskItems;
	} 

	
	/**
	 *  填写指定抽检任务条目的仓口
	 *//*
	public synchronized static void updateSampleTaskItemLocation(AGVSampleTaskItem taskItem, int windowId,  int goodsLocationId) {
		for (int i = 0; i < cache.llen("SamTask_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("SamTask_" + taskItem.getTaskId(), i);
			AGVSampleTaskItem agvSampleTaskItems = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItems.getGroupId().equals(taskItem.getGroupId())){
				agvSampleTaskItems.setWindowId(windowId);
				agvSampleTaskItems.setGoodsLocationId(goodsLocationId);
				cache.lset("SamTask_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvSampleTaskItems).getBytes());
				break;
			}
		}
	}
	*/
	
	/**
	 * 删除指定任务id的未分配的条目<br>
	 */
	public synchronized static void removeUnAssignedSampleTaskItemByTaskId(int taskId) {
		for (int i = 0; i < cache.llen("SamTask_" + taskId); i++) {
			byte[] item = cache.lindex("SamTask_" + taskId, i);
			AGVSampleTaskItem agvSampleTaskItems = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItems.getTaskId().intValue() == taskId && (agvSampleTaskItems.getState().intValue() == TaskItemState.WAIT_ASSIGN)){
				cache.lrem("SamTask_" + taskId, 1, item);
				i--;
			}
		}
	}
	
	
	/**
	 * 任务绑定仓口状态
	 */
	public synchronized static Integer getLocationStatus(Integer windowId, Integer goodsLocId) {
		Integer status = 0;
		status =  cache.get("Location_"+windowId + "_" + goodsLocId);
		if (status == null) {
			status = 0;
			cache.set("Location_"+windowId + "_" + goodsLocId, 0);
			
		}
		return status;
	}
	
	
	/**
	 * 设置位置状态信息（0：空，1：满）
	 */
	public synchronized static void setLocationStatus(Integer windowId, Integer goodsLocId, Integer status) {
		cache.del("Location_"+windowId + "_" + goodsLocId);
		cache.set("Location_"+windowId + "_" + goodsLocId, status);
	}
	
	
	/**
	 * 删除位置状态信息
	 */
	public synchronized static void delLocationStatus(Integer windowId, Integer goodsLocId) {
		cache.del("Location_"+windowId + "_" + goodsLocId);
	}

	public synchronized static Boolean getTaskStatus(Integer taskId) {
		Boolean status = false;
		status =  cache.get("Status_"+ taskId);
		if (status == null) {
			status = true;
			cache.set("Status_"+ taskId, true);
		}
		return status;
	}
	
	public synchronized static void setTaskStatus(Integer taskId, Boolean flag) {
		cache.del("Status_"+ taskId);
		cache.set("Status_"+ taskId, flag);
	}
	
	
	public synchronized static void delTaskStatus(Integer taskId) {
		cache.del("Status_"+ taskId);
	}
}
