package com.jimi.agv.tracker.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import com.jimi.agv.tracker.task.controller.TraditionController;
import com.jimi.agv.tracker.task.reporter.TraditionHeadReporter;

/**
 * 传统任务
 * <br>
 * <b>2019年6月12日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class TraditionAGVIOTask extends AGVIOTask{

	private int windowX;
	private int windowY;
	
	
	public TraditionAGVIOTask(String taskFilePath, int windowX, int windowY) throws IOException {
		this.windowX = windowX;
		this.windowY = windowY;
		
		BufferedReader reader = new BufferedReader(new FileReader(new File(taskFilePath)));
		setWeight(Integer.parseInt(reader.readLine()));
		
		String item = null;
		while((item = reader.readLine()) != null) {
			String[] position = item.split(",");
			getItems().add(new TraditionAGVIOTaskItem(windowX, windowY, Integer.parseInt(position[0]), Integer.parseInt(position[1]), Integer.parseInt(position[2])));
		}
		reader.close();
		
		setReporter(new TraditionHeadReporter(this));
		setController(new TraditionController());
	}
	

	public final int getWindowX() {
		return windowX;
	}


	public final void setWindowX(int windowX) {
		this.windowX = windowX;
	}


	public final int getWindowY() {
		return windowY;
	}


	public final void setWindowY(int windowY) {
		this.windowY = windowY;
	}
	
	
	
	
}
