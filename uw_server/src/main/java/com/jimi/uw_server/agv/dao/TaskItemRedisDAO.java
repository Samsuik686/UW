package com.jimi.uw_server.agv.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jfinal.json.Json;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jimi.uw_server.agv.entity.bo.AGVBuildTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.comparator.PriorityComparator;
import com.jimi.uw_server.constant.IOTaskItemState;



/**
 * AGV任务条目Redis数据访问对象
 * <br>
 * <b>2018年6月15日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class TaskItemRedisDAO {

	private static Cache cache = Redis.use();
	
	
	/**
	 * 是否已经停止分配任务
	 */
	public synchronized static int isPauseAssign() {
		try {
			return cache.get("pause");
		} catch (NullPointerException e) {
			cache.set("pause", 0);
			return isPauseAssign();
		}
	}
	
	
	/**
	 * 设置停止分配任务标志位
	 */
	public synchronized static void setPauseAssign(int pause) {
		cache.set("pause", pause);
	}
	
	
	/**
	 * 添加出入库任务条目，该方法会把新的任务条目插入到现有的任务列表当中，并把它们按任务优先级轮流排序<br>
	 */
	public synchronized static void addIOTaskItem(List<AGVIOTaskItem> ioTaskItems) {
		appendIOTaskItems(ioTaskItems);
		ioTaskItems.sort(new PriorityComparator());
		List<byte[]> items = new ArrayList<>();
		for (AGVIOTaskItem item : ioTaskItems) {
			items.add(Json.getJson().toJson(item).getBytes());
		}
		Collections.reverse(ioTaskItems);
		cache.del("til");
		cache.lpush("til", items.toArray());
	}
	
	
	/**
	 * 删除指定任务id的未分配的条目<br>
	 */
	public synchronized static void removeUnAssignedTaskItemByTaskId(int taskId) {
		for (int i = 0; i < cache.llen("til"); i++) {
			byte[] item = cache.lindex("til", i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getTaskId().intValue() == taskId && (agvioTaskItem.getState().intValue() == IOTaskItemState.UNASSIGNABLED || agvioTaskItem.getState().intValue() == IOTaskItemState.WAIT_ASSIGN)){
				cache.lrem("til", 1, item);
				i--;
			}
		}
	}
	
	
	/**
	 * 删除指定任务id的条目<br>
	 */
	public static void removeTaskItemByTaskId(int taskId) {
		for (int i = 0; i < cache.llen("til"); i++) {
			byte[] item = cache.lindex("til", i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getTaskId().intValue() == taskId){
				cache.lrem("til", 1, item);
				i--;
			}
		}
	}


	/**
	 * 更新出入库任务条目执行状态<br>
	 */
	public synchronized static void updateIOTaskItemState(AGVIOTaskItem taskItem, int state) {
		for (int i = 0; i < cache.llen("til"); i++) {
			byte[] item = cache.lindex("til", i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setState(state);
				cache.lset("til", i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 * 更新任务条目为已完成，标识出入库数量已满足实际需求<br>
	 */
	public synchronized static void updateTaskIsForceFinish(AGVIOTaskItem taskItem, boolean isForceFinish) {
		for (int i = 0; i < cache.llen("til"); i++) {
			byte[] item = cache.lindex("til", i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setIsForceFinish(isForceFinish);
				cache.lset("til", i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 * 更新任务条目盒号<br>
	 */
	public synchronized static void updateTaskItemBoxId(AGVIOTaskItem taskItem, int boxId) {
		for (int i = 0; i < cache.llen("til"); i++) {
			byte[] item = cache.lindex("til", i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setBoxId(boxId);
				cache.lset("til", i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 *  填写指定出入库任务条目的执行机器
	 */
	public synchronized static void updateIOTaskItemRobot(AGVIOTaskItem taskItem, int robotid) {
		for (int i = 0; i < cache.llen("til"); i++) {
			byte[] item = cache.lindex("til", i);
			AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(item), AGVIOTaskItem.class);
			if(agvioTaskItem.getId().intValue() == taskItem.getId().intValue()){
				agvioTaskItem.setRobotId(robotid);
				cache.lset("til", i, Json.getJson().toJson(agvioTaskItem).getBytes());
				break;
			}
		}
	}


	/**
	 * 返回出入库任务条目列表的副本
	 */
	public synchronized static List<AGVIOTaskItem> getIOTaskItems() {
		List<AGVIOTaskItem> ioTaskItems = new ArrayList<>();
		return appendIOTaskItems(ioTaskItems);
	}


	/**
	 * 把redis的til内容追加到参数里然后返回
	 */
	public synchronized static List<AGVIOTaskItem> appendIOTaskItems(List<AGVIOTaskItem> ioTaskItems) {
		List<byte[]> items = cache.lrange("til", 0, -1);
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
	 * 删除指定的建仓任务条目<br>
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
	 * 是否需要建仓
	 */
	public synchronized static boolean getIsBuildAssign() {
		try {
			return cache.get("build");
		} catch (NullPointerException e) {
			cache.set("build", false);
			return getIsBuildAssign();
		}
	}
	
	
	/**
	 * 设置建仓任务标志位
	 */
	public synchronized static void setBuildAssign(boolean build) {
		cache.set("build", build);
	}


	/**
	 * 判断建仓任务是否已完成
	 */
	public synchronized static void isBuildFinish() {
		List<byte[]> items = cache.lrange("tilOfBuild", 0, -1);
		if (items.size() == 0) {
			setBuildAssign(false);
		}
	}


}
