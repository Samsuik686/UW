package com.jimi.uw_server.agv.gaitek.entity.cmd;

import java.util.List;

import com.jimi.uw_server.agv.gaitek.entity.bo.AGVMissionGroup;
import com.jimi.uw_server.agv.gaitek.entity.cmd.base.AGVBaseCmd;


/**
 * AGV移动、拣料、回库等相关指令类 <br>
 * <b>2018年6月15日</b>
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class AGVMoveCmd extends AGVBaseCmd {

	private List<AGVMissionGroup> missiongroups;


	public List<AGVMissionGroup> getMissiongroups() {
		return missiongroups;
	}


	public void setMissiongroups(List<AGVMissionGroup> missiongroups) {
		this.missiongroups = missiongroups;
	}

}
