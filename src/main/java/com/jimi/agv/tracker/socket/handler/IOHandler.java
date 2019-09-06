package com.jimi.agv.tracker.socket.handler;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.jimi.agv.tracker.constant.Constant;
import com.jimi.agv.tracker.entity.bo.AGVMissionGroup;
import com.jimi.agv.tracker.entity.cmd.AGVMoveCmd;
import com.jimi.agv.tracker.entity.cmd.AGVStatusCmd;
import com.jimi.agv.tracker.main.Main;
import com.jimi.agv.tracker.socket.AGVMainSocket;
import com.jimi.agv.tracker.task.CushionAGVIOTaskItem;
import com.jimi.agv.tracker.task.TraditionAGVIOTaskItem;
import com.jimi.agv.tracker.util.IdCounter;
import com.jimi.agv.tracker.util.PropUtil;

/**
 * 出入库LS、SL命令处理器
 * <br>
 * <b>2018年7月10日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class IOHandler {

	public static void sendSL(TraditionAGVIOTaskItem item) throws Exception {
		List<AGVMissionGroup> groups = new ArrayList<>();
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getKey());
		group.setRobotid(item.getRobotId());
		group.setStartx(item.getWindowX());
		group.setStarty(item.getWindowY());
		group.setEndx(item.getTargetX());
		group.setEndy(item.getTargetY());
		group.setEndz(formatZ(item.getTargetZ()));
		group.setPriority("5");
		groups.add(group);
		AGVMoveCmd cmd = new AGVMoveCmd();
		cmd.setCmdcode("SL");
		cmd.setCmdid(IdCounter.getCmdId());
		cmd.setMissiongroups(groups);
		AGVMainSocket.sendMessage(cmd);
	}


	public static void sendLS(TraditionAGVIOTaskItem item) throws Exception {
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getKey());
		group.setRobotid(item.getRobotId());
		group.setStartx(item.getTargetX());
		group.setStarty(item.getTargetY());
		group.setStartz(formatZ(item.getTargetZ()));
		group.setEndx(item.getWindowX());
		group.setEndy(item.getWindowY());
		group.setPriority("5");
		List<AGVMissionGroup> groups = new ArrayList<>();
		groups.add(group);
		AGVMoveCmd cmd = new AGVMoveCmd();
		cmd.setCmdcode("LS");
		cmd.setCmdid(IdCounter.getCmdId());
		cmd.setMissiongroups(groups);
		AGVMainSocket.sendMessage(cmd);
	}

	
	public static void sendLL(CushionAGVIOTaskItem item) throws Exception {
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getKey());
		group.setRobotid(0);
		group.setStartx(item.getSourceX());
		group.setStarty(item.getSourceY());
		group.setStartz(formatZ(item.getSourceZ()));
		group.setEndx(item.getTargetX());
		group.setEndy(item.getTargetY());
		group.setEndz(formatZ(item.getTargetZ()));
		group.setPriority("5");
		List<AGVMissionGroup> groups = new ArrayList<>();
		groups.add(group);
		AGVMoveCmd cmd = new AGVMoveCmd();
		cmd.setCmdcode("LL");
		cmd.setCmdid(IdCounter.getCmdId());
		cmd.setMissiongroups(groups);
		AGVMainSocket.sendMessage(cmd);
	}


	/**
	 * 处理Status指令
	 */
	public static void handleStatus(String message) throws Exception {
		AGVStatusCmd statusCmd = JSON.parseObject(message, AGVStatusCmd.class);
		Main.getTaskPool().handleStatus(statusCmd);
	}
	
	
	private static Integer formatZ(Integer z) {
		if(z == null) {
			return null;
		}
		if(PropUtil.getBoolean(Constant.CONFIG_NAME, Constant.USE_ABSOLUTE_Z)) {
			switch (z) {
			case 1:
				return PropUtil.getInt(Constant.CONFIG_NAME, Constant.Z1);
			case 2:
				return PropUtil.getInt(Constant.CONFIG_NAME, Constant.Z2);
			case 3:
				return PropUtil.getInt(Constant.CONFIG_NAME, Constant.Z3);
			case 4:
				return PropUtil.getInt(Constant.CONFIG_NAME, Constant.Z4);
			default:
				throw new IllegalArgumentException("无法转成绝对高度的Z值：" + z);
			}
		}else {
			return z;
		}
	}

}
