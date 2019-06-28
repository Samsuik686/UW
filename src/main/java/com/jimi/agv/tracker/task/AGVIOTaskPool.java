package com.jimi.agv.tracker.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.jimi.agv.tracker.entity.cmd.AGVStatusCmd;
import com.jimi.agv.tracker.robot.FreeRobotCounter;
import com.jimi.agv.tracker.socket.handler.SwitchHandler;
import com.jimi.agv.tracker.space.SpaceManager;
import com.jimi.agv.tracker.task.comparator.AGVIOTaskPWeightComparator;

import cc.darhao.dautils.api.DateUtil;

public class AGVIOTaskPool {
	
	private List<AGVIOTask> tasks = new ArrayList<>();
	

	public AGVIOTaskPool(List<AGVIOTask> tasks) {
		this.tasks = tasks;
	}

	
	public void start() throws Exception {
		Collections.sort(tasks, new AGVIOTaskPWeightComparator());
		SwitchHandler.sendAllStart();
		startTask();
	}


	private void startTask() throws InterruptedException {
		while(true) {
			if(isFinish()) {
				break;
			}
			assignTaskToFreeRobots();
			Thread.sleep(3000);
		}
	}


	private boolean isFinish() {
		for (AGVIOTask task : tasks) {
			for (AGVIOTaskItem item : task.getItems()) {
				if(item.getState() != AGVIOTaskItem.FINISHED) {
					return false;
				}
			}
		}
		return true;
	}


	private void assignTaskToFreeRobots() {
		for (int i = 0; i < FreeRobotCounter.getInstance().getNum(); i++) {
			synchronized (SpaceManager.getInstance()) { //严格按顺序发条目
				assignTask();
			}
		}
	}


	private void assignTask() {
		for (AGVIOTask task : tasks) {
			for (AGVIOTaskItem item : task.getItems()) {
				if(task.getController().tryAssign(item)) {
					return;
				}
			}
		}
	}
	
	
	private AGVIOTask getTaskByItem(AGVIOTaskItem item) {
		for (AGVIOTask task : tasks) {
			for (AGVIOTaskItem item2 : task.getItems()) {
				if(item2.getKey().equals(item.getKey())) {
					return task;
				}
			}
		}
		return null;
	}


	/**
	 * 显示任务完成总进度
	 */
	private void showProcess() {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("日志时间：" + DateUtil.HHmmss(new Date()));
		System.out.println("任务总进度：" + countFinishItem() + "/" + getItemsSize());
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}
	
	
	private int getItemsSize() {
		int sum = 0;
		for (AGVIOTask task : tasks) {
			sum += task.getItems().size();
		}
		return sum;
	}


	private int countFinishItem() {
		int sum = 0;
		for (AGVIOTask task : tasks) {
			sum += task.countFinishItem();
		}
		return sum;
	}
	
	
	public void onItemFinish(AGVIOTaskItem item) {
		showProcess();
	}


	public AGVIOTaskItem getItemByKey(String key) {
		for (AGVIOTask task : tasks) {
			AGVIOTaskItem item = task.getItemByKey(key);
			if(item != null) {
				return item;
			}else {
				continue;
			}
		}
		return null;
	}
	
	
	public AGVIOTaskItem getExecutingItemByRobotId(int robotId) {
		for (AGVIOTask task : tasks) {
			AGVIOTaskItem item = task.getExecutingItemByRobotId(robotId);
			if(item != null) {
				return item;
			}else {
				continue;
			}
		}
		return null;
	}


	public List<AGVIOTask> getTasks() {
		return tasks;
	}


	public void handleStatus(AGVStatusCmd statusCmd) throws Exception {
		if(statusCmd.getStatus() == 0) {
			getTaskByItem(getItemByKey(statusCmd.getMissiongroupid())).getController().handleStatus0(statusCmd);
		}

		if(statusCmd.getStatus() == 1) {
			getTaskByItem(getItemByKey(statusCmd.getMissiongroupid())).getController().handleStatus1(statusCmd);
		}
		
		if(statusCmd.getStatus() == 2) {
			getTaskByItem(getItemByKey(statusCmd.getMissiongroupid())).getController().handleStatus2(statusCmd);
		}
	}
}
