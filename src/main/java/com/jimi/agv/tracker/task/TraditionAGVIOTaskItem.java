package com.jimi.agv.tracker.task;

import com.jimi.agv.tracker.reporter.TraditionBodyReporter;

/**
 * 传统模式任务条目
 * <b>2019年6月6日</b>
 * 
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class TraditionAGVIOTaskItem extends AGVIOTaskItem {
	
	private int windowX;
	private int windowY;
	
	private int targetX;
	private int targetY;
	private int targetZ;


	protected TraditionAGVIOTaskItem(int windowX, int windowY, int targetX, int targetY, int targetZ) {
		this.windowX = windowX;
		this.windowY = windowY;
		this.targetX = targetX;
		this.targetY = targetY;
		this.targetZ = targetZ;
		setReporter(new TraditionBodyReporter(this));
	}
	
	
	@Override
	public String getDescription() {
		return "[" + targetX + "," + targetY + "," + targetZ + "]";
	}
	
	
	public int getWindowX() {
		return windowX;
	}

	public int getWindowY() {
		return windowY;
	}

	public int getTargetZ() {
		return targetZ;
	}

	public int getTargetY() {
		return targetY;
	}

	public int getTargetX() {
		return targetX;
	}

}
