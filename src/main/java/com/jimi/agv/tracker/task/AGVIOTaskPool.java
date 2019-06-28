package com.jimi.agv.tracker.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.jimi.agv.tracker.entity.cmd.AGVStatusCmd;
import com.jimi.agv.tracker.robot.FreeRobotCounter;
import com.jimi.agv.tracker.socket.handler.SwitchHandler;
import com.jimi.agv.tracker.task.comparator.AGVIOTaskPWeightComparator;

import cc.darhao.dautils.api.DateUtil;

public class AGVIOTaskPool {
	
	private List<AGVIOTask> tasks = new ArrayList<>();
	
	private List<AGVIOTaskItem> prepareSendItems = new ArrayList<>();
	

	public void add(AGVIOTask task) {
		tasks.add(task);
	}
	
	
	public void start() throws Exception {
		fillPrepareSendItemsByWeight();
		SwitchHandler.sendAllStart();
		startTask();
	}


	private void fillPrepareSendItemsByWeight() {
		//把任务按照权重倒序排列
		Collections.sort(tasks, new AGVIOTaskPWeightComparator());
		Collections.reverse(tasks);
		//取出最小权重任务的权重
		int minWeight = tasks.get(tasks.size() - 1).getWeight();
		//把每个任务中的前N个子任务放到准备发送列表中，N等于该任务的权重 与 权重最小的任务的权重 的 比 的 整数部分 的 值
		for (AGVIOTask task : tasks) {
			int weightRatio = task.getWeight() / minWeight;
			if(weightRatio > task.getItems().size()) { //避免IndexOE
				for (AGVIOTaskItem item : task.getItems()) {
					prepareSendItems.add(item);
				}
			}else {
				for (AGVIOTaskItem item : task.getItems().subList(0, weightRatio)) {
					prepareSendItems.add(item);
				}
			}
		}
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
			for (AGVIOTaskItem item : prepareSendItems) {
				if(getTaskByItem(item).getController().tryAssign(item)) {
					break;
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


	public void onItemFinish(AGVIOTaskItem item) {
		showProcess();
//		prepareNextItemByTask(getTaskByItem(item));
	}


//	private void prepareNextItemByTask(AGVIOTask task) {
//		for (AGVIOTaskItem item : task.getItems()) {
//			if(item.getState() == AGVIOTaskItem.WAIT_ASSIGN) {
//				//如果是缓冲模式的高优先级指令则直接放到最前面
//				if(item instanceof CushionAGVIOTaskItem && ((CushionAGVIOTaskItem) item).isTopPriority()) {
//					prepareSendItems.add(0, item);
//				}else {
//					prepareSendItems.add(item);
//				}
//			}
//		}
//	}
	
	
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
	
	
	public AGVIOTaskItem getItemByKey(String key) {
		for (AGVIOTask task : tasks) {
			return task.getItemByKey(key);
		}
		return null;
	}
	
	
	public AGVIOTaskItem getExecutingItemByRobotId(int robotId) {
		for (AGVIOTask task : tasks) {
			return task.getExecutingItemByRobotId(robotId);
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
