package com.jimi.agv.tracker.robot;

import com.jimi.agv.tracker.entity.bo.AGVRobot;
import com.jimi.agv.tracker.entity.cmd.AGVRobotInfoCmd;

/**
 * 空车计数器
 * <br>
 * <b>2019年6月14日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class FreeRobotCounter {

	private static final int FREE = 0;
	private static final int CHARGING = 4;
	private static final int POWER_THRESHOLD = 60;
	
	
	private static FreeRobotCounter me;
	
	private int num;
	
	
	public synchronized static FreeRobotCounter getInstance() {
		if(me == null) {
			me = new FreeRobotCounter();
		}
		return me;
	}
	
	
	public synchronized void update(AGVRobotInfoCmd cmd) {
		int num = 0;
		for (AGVRobot robot : cmd.getRobotarray()) {
			if(robot.getStatus().intValue() == FREE || (robot.getStatus().intValue() == CHARGING && robot.getBatteryPower().intValue() >= POWER_THRESHOLD)) {
				num++;
			}
		}
		this.num = num;
	}
	
	
	public int getNum() {
		return num;
	}
	
}
