package com.jimi.agv.boxiterator.proxy.entity.bo;

public class BoxIteratorTaskInfo {

	private int robotId;
	
	private int windowX;
	private int windowY;
	
	
	public BoxIteratorTaskInfo(int robotId, int windowX, int windowY) {
		this.robotId = robotId;
		this.windowX = windowX;
		this.windowY = windowY;
	}
	
	public int getRobotId() {
		return robotId;
	}
	
	public int getWindowX() {
		return windowX;
	}
	
	public int getWindowY() {
		return windowY;
	}
	
}
