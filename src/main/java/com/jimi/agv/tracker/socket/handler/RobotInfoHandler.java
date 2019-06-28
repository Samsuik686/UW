package com.jimi.agv.tracker.socket.handler;

import com.alibaba.fastjson.JSON;
import com.jimi.agv.tracker.entity.bo.AGVRobot;
import com.jimi.agv.tracker.entity.bo.Position;
import com.jimi.agv.tracker.entity.cmd.AGVRobotInfoCmd;
import com.jimi.agv.tracker.main.Main;
import com.jimi.agv.tracker.robot.FreeRobotCounter;
import com.jimi.agv.tracker.task.AGVIOTaskItem;

/**
 * 机器实时状态指令处理器
 * <br>
 * <b>2019年6月10日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class RobotInfoHandler {

	public static void handle(String message) {
		AGVRobotInfoCmd cmd = JSON.parseObject(message, AGVRobotInfoCmd.class);
		updateFreeRobotCounter(cmd);
		addTrails(cmd);
	}
	

	private static void updateFreeRobotCounter(AGVRobotInfoCmd cmd) {
		FreeRobotCounter.getInstance().update(cmd);
	}
	

	private static void addTrails(AGVRobotInfoCmd cmd) {
		for (AGVRobot robot : cmd.getRobotarray()) {
			if(Main.getTaskPool() != null) {
				AGVIOTaskItem executingItem = Main.getTaskPool().getExecutingItemByRobotId(robot.getRobotid());
				if(executingItem != null) {
					executingItem.addTrail(new Position(robot.getPosX(), robot.getPosY()));
				}
			}
		}
	}
	
}
