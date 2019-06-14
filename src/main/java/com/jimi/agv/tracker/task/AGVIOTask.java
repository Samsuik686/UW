package com.jimi.agv.tracker.task;

import java.util.ArrayList;
import java.util.List;

import com.jimi.agv.tracker.task.controller.Controller;
import com.jimi.agv.tracker.task.reporter.Reporter;

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
	
	
	public int countFinishItem() {
		int num = 0;
		for (AGVIOTaskItem item : items) {
			if(item.getState() == AGVIOTaskItem.FINISHED) {
				num++;
			}
		}
		return num;
	} 


	public final AGVIOTaskItem getItemByKey(String key) {
		for (AGVIOTaskItem item : items) {
			if(item.getKey().equals(key)) {
				return item;
			}
		}
		return null;
	}
	
	
	public final AGVIOTaskItem getExecutingItemByRobotId(int robotId) {
		for (AGVIOTaskItem item : items) {
			if(item.getState() > AGVIOTaskItem.NOT_START && item.getState() < AGVIOTaskItem.FINISHED && item.getRobotId() == robotId) {
				return item;
			}
		}
		return null;
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
