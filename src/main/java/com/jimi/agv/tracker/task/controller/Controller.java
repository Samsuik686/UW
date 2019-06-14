package com.jimi.agv.tracker.task.controller;

import java.util.Date;

import com.jimi.agv.tracker.entity.cmd.AGVStatusCmd;
import com.jimi.agv.tracker.main.Main;
import com.jimi.agv.tracker.robot.FreeRobotCounter;
import com.jimi.agv.tracker.socket.handler.SwitchHandler;
import com.jimi.agv.tracker.task.AGVIOTask;
import com.jimi.agv.tracker.task.AGVIOTaskItem;

import cc.darhao.dautils.api.DateUtil;

/**
 * 任务条目状态控制器
 * <br>
 * <b>2019年6月12日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public abstract class Controller {
	
	private AGVIOTask task;
	
	
	protected Controller(AGVIOTask task) {
		this.task = task;
	}
	
	
	public void start() throws Exception {
		new Thread(() -> {
			executeTask();
		}).start();
		SwitchHandler.sendAllStart();
	}


	private void executeTask() {
		while(true) {
			assignTaskToFreeRobots();
			if(task.isFinish()) {
				break;
			}
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
			}
		}
		if(Main.getTask().isFinish()) {//如果全部任务条目都完成了就通知主线程
			Main.notifyForTaskFinish();
		}
	}


	private void assignTaskToFreeRobots() {
		for (int i = 0; i < FreeRobotCounter.getInstance().getNum(); i++) {
			for (AGVIOTaskItem item : task.getItems()) {
				if(tryAssign(item)) {
					break;
				}
			}
		}
	}


	private boolean tryAssign(AGVIOTaskItem item) {
		if(item.getState() == AGVIOTaskItem.WAIT_ASSIGN && isAssignable(item)) {
			try {
				sendIOCmd(item);
				item.assign();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	
	/**
	 * 显示任务完成总进度
	 */
	protected final void showProcess() {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("日志时间：" + DateUtil.HHmmss(new Date()));
		System.out.println("任务总进度：" + task.countFinishItem() + "/" + task.getItems().size());
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}
	

	protected abstract boolean isAssignable(AGVIOTaskItem item);

	public abstract void sendIOCmd(AGVIOTaskItem item) throws Exception;

	public abstract void handleStatus0(AGVStatusCmd statusCmd) throws Exception;
	
	public abstract void handleStatus1(AGVStatusCmd statusCmd) throws Exception;
	
	public abstract void handleStatus2(AGVStatusCmd statusCmd) throws Exception;
	
}
