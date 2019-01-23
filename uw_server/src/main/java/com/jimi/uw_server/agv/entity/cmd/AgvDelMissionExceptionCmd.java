package com.jimi.uw_server.agv.entity.cmd;

import com.jimi.uw_server.agv.entity.cmd.base.AGVBaseCmd;

/**
 * 异常任务删除指令
 * @author HardyYao
 * @createTime 2019年1月23日  上午10:56:43
 */

public class AgvDelMissionExceptionCmd extends AGVBaseCmd {

	private String missiongroupid;

	private Integer robotid;

	public String getMissiongroupid() {
		return missiongroupid;
	}

	public void setMissiongroupid(String missiongroupid) {
		this.missiongroupid = missiongroupid;
	}

	public Integer getRobotid() {
		return robotid;
	}

	public void setRobotid(Integer robotid) {
		this.robotid = robotid;
	}

}
