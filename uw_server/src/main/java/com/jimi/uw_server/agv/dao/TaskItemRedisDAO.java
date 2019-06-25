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
import com.jimi.uw_server.constant.IOTaskItemState;



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
		Collections.reverse(ioTaskItems);
		cache.del("til_" + taskId);
		cache.lpush("til_" + taskId, items.toArray());
	}
	
	
	/**
	 * 删除指定任务id的未分配的条目<br>
	 */
	public synchronized static void removeUnAssignedTaskItemByTaskId(int taskId) {
		for (int i = 0; i < cache.llen("til_" + taskId); i++) {
			byte[] item = cache.lindex("til_" + taskId, i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getTaskId().intValue() == taskId && (agvioTaskItem.getState().intValue() == IOTaskItemState.WAIT_SCAN || agvioTaskItem.getState().intValue() == IOTaskItemState.WAIT_ASSIGN)){
				cache.lrem("til_" + taskId, 1, item);
				i--;
			}
		}
	}
	
	
	/**
	 * 删除指定任务id的条目<br>
	 */
	public static void removeTaskItemByTaskId(int taskId) {
		cache.del("til_" + taskId);
	}


	/**
	 * 删除指定的出入库任务条目<br>
	 */
	public static void removeTaskItemByPackingListId(Integer taskId, int packingListId) {
		for (int i = 0; i < cache.llen("til_" + taskId); i++) {
			byte[] item = cache.lindex("til_" + taskId, i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == packingListId){
				cache.lrem("til_" + taskId, 1, item);
				i--;
			}
		}
	}


	/**
	 * 更新出入库任务条目执行状态<br>
	 */
	public synchronized static void updateIOTaskItemState(AGVIOTaskItem taskItem, int state) {
		for (int i = 0; i < cache.llen("til_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("til_" + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setState(state);
				cache.lset("til_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}

	
	/**
	 * 更新出入库任务条目是否截料<br>
	 */
	public synchronized static void updateIOTaskItemIsCut(AGVIOTaskItem taskItem, Boolean flag) {
		for (int i = 0; i < cache.llen("til_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("til_" + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setIsCut(flag);
				cache.lset("til_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}
	

	/**
	 * 更新任务条目为已完成，标识出入库数量已满足实际需求<br>
	 */
	public synchronized static void updateTaskIsForceFinish( AGVIOTaskItem taskItem, boolean isForceFinish) {
		for (int i = 0; i < cache.llen("til_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("til_" + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setIsForceFinish(isForceFinish);
				cache.lset("til_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 * 更新任务条目盒号<br>
	 */
	public synchronized static void updateTaskItemBoxId( AGVIOTaskItem taskItem, int boxId) {
		for (int i = 0; i < cache.llen("til_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("til_" + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setBoxId(boxId);
				cache.lset("til_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 *  填写指定出入库任务条目的执行机器
	 */
	public synchronized static void updateIOTaskItemRobot(AGVIOTaskItem taskItem, int robotid) {
		for (int i = 0; i < cache.llen("til_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("til_" + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setRobotId(robotid);
				cache.lset("til_" + taskItem.getTaskId(), i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}

	
	/**
	 *  填写指定出入库任务条目的仓口
	 */
	public synchronized static void updateIOTaskItemWindow(AGVIOTaskItem taskItem, int windowId) {
		for (int i = 0; i < cache.llen("til_" + taskItem.getTaskId()); i++) {
			byte[] item = cache.lindex("til_" + taskItem.getTaskId(), i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getGroupId().equals(taskItem.getGroupId())){
				agvioTaskItem.setWindowId(windowId);;
				cache.lset("inventoryOfTil", i, Json.getJson().toJson(agvioTaskItem).getBytes());
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
		List<byte[]> items = cache.lrange("til_" + taskId, 0, -1);
		for (byte[] item : items) {
			ioTaskItems.add(Json.getJson().parse(new String(item), AGVIOTaskItem.class));
		}
		ioTaskItems.sort(new PriorityComparator());
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
	 * 获取叉车绑定的任务
	 */
	public synchronized static Integer getRobotTask(Integer robotId) {
		try {
			return cache.get("robot_" + robotId);
		} catch (NullPointerException e) {
			
			return null;
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
	
	
/*	*//**
	 * 获取叉车的指令
	 */
	public synchronized static String getRobotOrder(Integer robotId) {
		try {
			return cache.get("rb_order_" + robotId);
		} catch (NullPointerException e) {
			
			return null;
		}
	}
	
	
	/**
	 * 设置机器任务执行的指令
	 */
	public synchronized static void setRobotOrder(Integer robotId, String id) {
		cache.set("rb_order_" + robotId, id);
	}
	
	
	public synchronized static void delRobotOrder(Integer robotId) {
		cache.del("rb_order_" + robotId);
	}
	

	
	/**
	 * 添加盘点任务条目，该方法会把新的任务条目插入到现有的任务列表当中<br>
	 */
	public synchronized static void addInventoryTaskItem(List<AGVInventoryTaskItem> agvInventoryTaskItem) {
		appendInventoryTaskItems(agvInventoryTaskItem);
		List<byte[]> items = new ArrayList<>();
		for (AGVInventoryTaskItem item : agvInventoryTaskItem) {
			items.add(Json.getJson().toJson(item).getBytes());
		}
		Collections.reverse(agvInventoryTaskItem);
		cache.del("inventoryOfTil");
		cache.lpush("inventoryOfTil", items.toArray());
	}
	
	
	
	/**
	 * 删除指定任务id的盘点条目<br>
	 */
	public static void removeInventoryTaskItemByTaskId(int taskId) {
		for (int i = 0; i < cache.llen("inventoryOfTil"); i++) {
			byte[] item = cache.lindex("inventoryOfTil", i);
			AGVInventoryTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVInventoryTaskItem.class);
			if(agvioTaskItem.getTaskId().intValue() == taskId){
				cache.lrem("inventoryOfTil", 1, item);
				i--;
			}
		}
	}


	/**
	 * 删除指定的盘点任务条目<br>
	 */
	public static void removeInventoryTaskItemById(Integer taskId, Integer boxId) {
		for (int i = 0; i < cache.llen("inventoryOfTil"); i++) {
			byte[] item = cache.lindex("inventoryOfTil", i);
			AGVInventoryTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVInventoryTaskItem.class);
			if(agvioTaskItem.getGroupId().equals(taskId + "@" + boxId)){
				cache.lrem("inventoryOfTil", 1, item);
				i--;
			}
		}
	}


	/**
	 * 更新盘点任务条目执行状态<br>
	 */
	public synchronized static void updateInventoryTaskItemState(AGVInventoryTaskItem taskItem, int state) {
		for (int i = 0; i < cache.llen("inventoryOfTil"); i++) {
			byte[] item = cache.lindex("inventoryOfTil", i);
			AGVInventoryTaskItem agvInventoryTaskItem = Json.getJson().parse(new String(item), AGVInventoryTaskItem.class);
			if(agvInventoryTaskItem.getGroupId().equals(taskItem.getGroupId())){
				agvInventoryTaskItem.setState(state);
				cache.lset("inventoryOfTil", i, Json.getJson().toJson(agvInventoryTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 * 更新盘点任务条目为已完成<br>
	 */
	public synchronized static void updateInventoryTaskIsForceFinish(AGVInventoryTaskItem taskItem, boolean isForceFinish) {
		for (int i = 0; i < cache.llen("inventoryOfTil"); i++) {
			byte[] item = cache.lindex("inventoryOfTil", i);
			AGVInventoryTaskItem agvInventoryTaskItem = Json.getJson().parse(new String(item), AGVInventoryTaskItem.class);
			if(agvInventoryTaskItem.getGroupId().equals(taskItem.getGroupId())){
				agvInventoryTaskItem.setIsForceFinish(isForceFinish);
				cache.lset("inventoryOfTil", i, Json.getJson().toJson(agvInventoryTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 *  填写指定盘点任务条目的执行机器
	 */
	public synchronized static void updateInventoryTaskItemRobot(AGVInventoryTaskItem taskItem, int robotid) {
		for (int i = 0; i < cache.llen("inventoryOfTil"); i++) {
			byte[] item = cache.lindex("inventoryOfTil", i);
			AGVInventoryTaskItem agvInventoryTaskItem = Json.getJson().parse(new String(item), AGVInventoryTaskItem.class);
			if(agvInventoryTaskItem.getGroupId().equals(taskItem.getGroupId())){
				agvInventoryTaskItem.setRobotId(robotid);
				cache.lset("inventoryOfTil", i, Json.getJson().toJson(agvInventoryTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 * 返回盘点任务条目列表的副本
	 */
	public synchronized static List<AGVInventoryTaskItem> getInventoryTaskItems() {
		List<AGVInventoryTaskItem> inventoryTaskItems = new ArrayList<>();
		return appendInventoryTaskItems(inventoryTaskItems);
	}


	/**
	 * 把redis的inventoryOfTil内容追加到参数里然后返回
	 */
	@SuppressWarnings("unchecked")
	public synchronized static List<AGVInventoryTaskItem> appendInventoryTaskItems(List<AGVInventoryTaskItem> agvInventoryTaskItem) {
		List<byte[]> items = cache.lrange("inventoryOfTil", 0, -1);
		for (byte[] item : items) {
			agvInventoryTaskItem.add(Json.getJson().parse(new String(item), AGVInventoryTaskItem.class));
		}
		return agvInventoryTaskItem;
	} 

	
	/**
	 *  填写指定盘点任务条目的仓口
	 */
	public synchronized static void updateInventoryTaskItemWindow(AGVInventoryTaskItem taskItem, int windowId) {
		for (int i = 0; i < cache.llen("inventoryOfTil"); i++) {
			byte[] item = cache.lindex("inventoryOfTil", i);
			AGVInventoryTaskItem agvInventoryTaskItem = Json.getJson().parse(new String(item), AGVInventoryTaskItem.class);
			if(agvInventoryTaskItem.getGroupId().equals(taskItem.getGroupId())){
				agvInventoryTaskItem.setWindowId(windowId);;
				cache.lset("inventoryOfTil", i, Json.getJson().toJson(agvInventoryTaskItem).getBytes());
				break;
			}
		}
	}

	
	/**
	 * 添加抽检任务条目，该方法会把新的任务条目插入到现有的任务列表当中<br>
	 */
	public synchronized static void addSampleTaskItem(List<AGVSampleTaskItem> agvSampleTaskItems) {
		appendSampleTaskItems(agvSampleTaskItems);
		List<byte[]> items = new ArrayList<>();
		for (AGVSampleTaskItem item : agvSampleTaskItems) {
			items.add(Json.getJson().toJson(item).getBytes());
		}
		Collections.reverse(agvSampleTaskItems);
		cache.del("sampleOfTil");
		cache.lpush("sampleOfTil", items.toArray());
	}
	
	
	
	/**
	 * 删除指定任务id的抽检条目<br>
	 */
	public static void removeSampleTaskItemByTaskId(int taskId) {
		for (int i = 0; i < cache.llen("sampleOfTil"); i++) {
			byte[] item = cache.lindex("sampleOfTil", i);
			AGVSampleTaskItem agvSampleTaskItem = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItem.getTaskId().intValue() == taskId){
				cache.lrem("sampleOfTil", 1, item);
				i--;
			}
		}
	}


	/**
	 * 删除指定的抽检任务条目<br>
	 */
	public static void removeSampleTaskItemById(Integer taskId, Integer boxId) {
		for (int i = 0; i < cache.llen("sampleOfTil"); i++) {
			byte[] item = cache.lindex("sampleOfTil", i);
			AGVSampleTaskItem agvSampleTaskItem = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItem.getGroupId().equals(taskId + "#" + boxId)){
				cache.lrem("sampleOfTil", 1, item);
				i--;
			}
		}
	}


	/**
	 * 更新抽检任务条目执行状态<br>
	 */
	public synchronized static void updateSampleTaskItemState(AGVSampleTaskItem taskItem, int state) {
		for (int i = 0; i < cache.llen("sampleOfTil"); i++) {
			byte[] item = cache.lindex("sampleOfTil", i);
			AGVSampleTaskItem agvSampleTaskItem = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItem.getGroupId().equals(taskItem.getGroupId())){
				agvSampleTaskItem.setState(state);
				cache.lset("sampleOfTil", i, Json.getJson().toJson(agvSampleTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 * 更新抽检任务条目为已完成<br>
	 */
	public synchronized static void updateSampleTaskIsForceFinish(AGVSampleTaskItem taskItem, boolean isForceFinish) {
		for (int i = 0; i < cache.llen("sampleOfTil"); i++) {
			byte[] item = cache.lindex("sampleOfTil", i);
			AGVSampleTaskItem agvSampleTaskItem = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItem.getGroupId().equals(taskItem.getGroupId())){
				agvSampleTaskItem.setIsForceFinish(isForceFinish);
				cache.lset("sampleOfTil", i, Json.getJson().toJson(agvSampleTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 *  填写指定抽检任务条目的执行机器
	 */
	public synchronized static void updateSampleTaskItemRobot(AGVSampleTaskItem taskItem, int robotid) {
		for (int i = 0; i < cache.llen("sampleOfTil"); i++) {
			byte[] item = cache.lindex("sampleOfTil", i);
			AGVSampleTaskItem agvSampleTaskItem = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItem.getGroupId().equals(taskItem.getGroupId())){
				agvSampleTaskItem.setRobotId(robotid);
				cache.lset("sampleOfTil", i, Json.getJson().toJson(agvSampleTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 * 返回抽检任务条目列表的副本
	 */
	public synchronized static List<AGVSampleTaskItem> getSampleTaskItems() {
		List<AGVSampleTaskItem> agvSampleTaskItems = new ArrayList<>();
		return appendSampleTaskItems(agvSampleTaskItems);
	}


	/**
	 * 把redis的inventoryOfTil内容追加到参数里然后返回
	 */
	@SuppressWarnings("unchecked")
	public synchronized static List<AGVSampleTaskItem> appendSampleTaskItems(List<AGVSampleTaskItem> agvSampleTaskItems) {
		List<byte[]> items = cache.lrange("sampleOfTil", 0, -1);
		for (byte[] item : items) {
			agvSampleTaskItems.add(Json.getJson().parse(new String(item), AGVSampleTaskItem.class));
		}
		return agvSampleTaskItems;
	} 

	
	/**
	 *  填写指定抽检任务条目的仓口
	 */
	public synchronized static void updateSampleTaskItemWindow(AGVSampleTaskItem taskItem, int windowId) {
		for (int i = 0; i < cache.llen("sampleOfTil"); i++) {
			byte[] item = cache.lindex("sampleOfTil", i);
			AGVSampleTaskItem agvSampleTaskItems = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItems.getGroupId().equals(taskItem.getGroupId())){
				agvSampleTaskItems.setWindowId(windowId);;
				cache.lset("sampleOfTil", i, Json.getJson().toJson(agvSampleTaskItems).getBytes());
				break;
			}
		}
	}
	
	
	/**
	 * 删除指定任务id的未分配的条目<br>
	 */
	public synchronized static void removeUnAssignedSampleTaskItemByTaskId(int taskId) {
		for (int i = 0; i < cache.llen("sampleOfTil"); i++) {
			byte[] item = cache.lindex("sampleOfTil", i);
			AGVSampleTaskItem agvSampleTaskItems = Json.getJson().parse(new String(item), AGVSampleTaskItem.class);
			if(agvSampleTaskItems.getTaskId().intValue() == taskId && (agvSampleTaskItems.getState().intValue() == IOTaskItemState.WAIT_ASSIGN)){
				cache.lrem("sampleOfTil" + taskId, 1, item);
				i--;
			}
		}
	}
	
	
	/**
	 * 任务绑定仓口状态
	 */
	public synchronized static String getWindowTaskInfo(Integer windowId, Integer taskId) {
		try {
			return cache.get("Window_"+windowId + "_" + taskId);
		} catch (NullPointerException e) {
			cache.set("Window_"+windowId + "_" + taskId, UNDEFINED);
			return null;
		}
	}
	
	
	/**
	 * 设置任务绑定仓口状态
	 */
	public synchronized static void setWindowTaskInfo(Integer windowId, Integer taskId, String robots) {
		cache.del("Window_"+windowId + "_" + taskId);
		cache.set("Window_"+windowId + "_" + taskId, robots);
	}
	
	
	/**
	 * 删除任务绑定仓口状态
	 */
	public synchronized static void delWindowTaskInfo(Integer windowId, Integer taskId) {
		cache.del("Window_"+windowId + "_" + taskId);
	}

}
