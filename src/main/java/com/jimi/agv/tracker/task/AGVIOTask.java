package com.jimi.agv.tracker.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jimi.agv.tracker.controller.Controller;
import com.jimi.agv.tracker.reporter.Reporter;

import cc.darhao.dautils.api.DateUtil;

public abstract class AGVIOTask {

	private Reporter reporter;
	
	private Controller controller;
	
	private List<AGVIOTaskItem> items = new ArrayList<>();
	
	
	public final boolean isFinish() {
		for (AGVIOTaskItem item : items) {
			if(item.getState() != AGVIOTaskItem.FINISHED) {
				return false;
			}
		}
		return true;
	}
	
	
	public void executeNextItem() throws Exception {
		for (AGVIOTaskItem item : items) {
			if(item.getState() == AGVIOTaskItem.NOT_START) {
				showProcess(item);
				controller.sendIOCmd(item);
				return;
			}
		}
		showEnd();
	}


	public final AGVIOTaskItem getItemByKey(String key) {
		for (AGVIOTaskItem item : items) {
			if(item.getKey().equals(key)) {
				return item;
			}
		}
		return null;
	}
	
	
	public final AGVIOTaskItem getExecutingItem() {
		for (AGVIOTaskItem item : items) {
			if(item.getState() > AGVIOTaskItem.NOT_START && item.getState() < AGVIOTaskItem.FINISHED) {
				return item;
			}
		}
		return null;
	}


	private final void showEnd() {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("日志时间：" + DateUtil.HHmmss(new Date()));
		System.out.println("任务总进度：" + items.size() + "/" + items.size());
		System.out.println("所有任务条目即将完成");
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}


	private final void showProcess(AGVIOTaskItem item) {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("日志时间：" + DateUtil.HHmmss(new Date()));
		System.out.println("任务总进度：" + items.indexOf(item) + "/" + items.size());
		System.out.println("即将执行任务条目：" + item.getDescription());
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}
	
	
	public final Controller getController() {
		return controller;
	}


	public final Reporter getReporter() {
		return reporter;
	}


	public final void setReporter(Reporter reporter) {
		this.reporter = reporter;
	}


	public final void setController(Controller controller) {
		this.controller = controller;
	}


	public final List<AGVIOTaskItem> getItems() {
		return items;
	}
	
}
