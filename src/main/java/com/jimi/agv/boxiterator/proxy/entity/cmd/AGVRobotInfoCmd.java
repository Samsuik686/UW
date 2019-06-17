package com.jimi.agv.boxiterator.proxy.entity.cmd;

import java.util.List;

import com.jimi.agv.boxiterator.proxy.entity.bo.AGVRobot;
import com.jimi.agv.boxiterator.proxy.entity.cmd.base.AGVBaseCmd;

/**
 * AGV机器实时信息类
 * <br>
 * <b>2018年7月13日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class AGVRobotInfoCmd extends AGVBaseCmd{
	
	private List<AGVRobot> robotarray;

	public List<AGVRobot> getRobotarray() {
		return robotarray;
	}

	public void setRobotarray(List<AGVRobot> robotarray) {
		this.robotarray = robotarray;
	}
	
}
