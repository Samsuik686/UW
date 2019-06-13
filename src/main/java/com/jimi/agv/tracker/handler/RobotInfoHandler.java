package com.jimi.agv.tracker.handler;

import com.alibaba.fastjson.JSON;
import com.jimi.agv.tracker.constant.Constant;
import com.jimi.agv.tracker.entity.bo.AGVRobot;
import com.jimi.agv.tracker.entity.bo.Position;
import com.jimi.agv.tracker.entity.cmd.AGVRobotInfoCmd;
import com.jimi.agv.tracker.main.Main;
import com.jimi.agv.tracker.task.AGVIOTask;
import com.jimi.agv.tracker.task.AGVIOTaskItem;
import com.jimi.agv.tracker.util.PropUtil;

/**
 * 机器实时状态指令处理器
 * <br>
 * <b>2019年6月10日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class RobotInfoHandler {

	public static void handle(String message) {
		AGVRobotInfoCmd cmd = JSON.parseObject(message, AGVRobotInfoCmd.class);
		for (AGVRobot robot : cmd.getRobotarray()) {
			if(robot.getRobotid().intValue() == PropUtil.getInt(Constant.CONFIG_NAME, Constant.ROBOT_ID_STRING)){
				AGVIOTask task = Main.getTask();
				if(task != null) {
					AGVIOTaskItem executingItem = task.getExecutingItem();
					if(executingItem != null) {
						executingItem.addTrail(new Position(robot.getPosX(), robot.getPosY()));
						return;
					}
				}
				return;
			}
		}
	}
	
}
