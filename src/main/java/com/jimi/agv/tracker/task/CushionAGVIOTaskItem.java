package com.jimi.agv.tracker.task;

import com.jimi.agv.tracker.task.reporter.CushionBodyReporter;

/**
 * 缓冲模式任务条目
 * <b>2019年6月6日</b>
 * 
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class CushionAGVIOTaskItem extends AGVIOTaskItem{
	
	private int sourceX;
	private int sourceY;
	private int sourceZ;
	
	private int targetX;
	private int targetY;
	private int targetZ;
	

	protected CushionAGVIOTaskItem(int sourceX, int sourceY, int sourceZ, int targetX, int targetY, int targetZ) {
		this.sourceX = sourceX;
		this.sourceY = sourceY;
		this.sourceZ = sourceZ;
		this.targetX = targetX;
		this.targetY = targetY;
		this.targetZ = targetZ;
		setReporter(new CushionBodyReporter(this));
	}
	
	
	@Override
	public String getDescription() {
		return "[" + sourceX + "," + sourceY + "," + sourceZ + "]->[" + targetX + "," + targetY + "," + targetZ + "]";
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

	public int getSourceX() {
		return sourceX;
	}

	public int getSourceY() {
		return sourceY;
	}

	public int getSourceZ() {
		return sourceZ;
	}
	
}
