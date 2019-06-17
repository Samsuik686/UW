package com.jimi.agv.boxiterator.client.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;


public class BoxIterateTask {

	private int windowX;
	private int windowY;
	
	private int robotId;
	
	private List<BoxIterateTaskItem> items;
	
	
	public BoxIterateTask(String taskFilePath, int windowX, int windowY, int robootId) throws Exception {
		this.windowX = windowX;
		this.windowY = windowY;
		this.robotId = robootId;
		items = createItems(taskFilePath);
	}
	
	
	private List<BoxIterateTaskItem> createItems(String taskFilePath) throws Exception {
		List<BoxIterateTaskItem> items = new ArrayList<>();
		BufferedReader reader = new BufferedReader(new FileReader(new File(taskFilePath)));
		String item = null;
		while((item = reader.readLine()) != null) {
			String[] position = item.split(",");
			items.add(new BoxIterateTaskItem(Integer.parseInt(position[0]), Integer.parseInt(position[1]), Integer.parseInt(position[2])));
		}
		reader.close();
		return items;
	}


	public final int countFinishItem() {
		int num = 0;
		for (BoxIterateTaskItem item : items) {
			if(item.getState() == BoxIterateTaskItem.FINISHED) {
				num++;
			}
		}
		return num;
	} 
	
	
	public final BoxIterateTaskItem getNextNotStartItem() {
		for (BoxIterateTaskItem item : items) {
			if(item.getState() == BoxIterateTaskItem.NOT_START) {
				return item;
			}
		}
		return null;
	}


	public final BoxIterateTaskItem getItemByKey(String key) {
		for (BoxIterateTaskItem item : items) {
			if(item.getKey().equals(key)) {
				return item;
			}
		}
		return null;
	}
	
	
	public final List<BoxIterateTaskItem> getItems() {
		return items;
	}


	public int getWindowX() {
		return windowX;
	}



	public int getWindowY() {
		return windowY;
	}



	public int getRobotId() {
		return robotId;
	}
	
}
