package com.jimi.agv.tracker.socket.handler;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.jimi.agv.tracker.entity.bo.AGVMissionGroup;
import com.jimi.agv.tracker.entity.cmd.AGVMoveCmd;
import com.jimi.agv.tracker.entity.cmd.AGVStatusCmd;
import com.jimi.agv.tracker.main.Main;
import com.jimi.agv.tracker.socket.AGVMainSocket;
import com.jimi.agv.tracker.task.CushionAGVIOTaskItem;
import com.jimi.agv.tracker.task.TraditionAGVIOTaskItem;
import com.jimi.agv.tracker.util.IdCounter;

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
		group.setEndz(item.getTargetZ());
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
		group.setStartz(item.getTargetZ());
		group.setEndx(item.getWindowX());
		group.setEndy(item.getWindowY());
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
		group.setStartz(item.getSourceZ());
		group.setEndx(item.getTargetX());
		group.setEndy(item.getTargetY());
		group.setEndz(item.getTargetZ());
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

		if(statusCmd.getStatus() == 0) {
			Main.getTask().getController().handleStatus0(statusCmd);
		}

		if(statusCmd.getStatus() == 1) {
			Main.getTask().getController().handleStatus1(statusCmd);
		}
		
		if(statusCmd.getStatus() == 2) {
			Main.getTask().getController().handleStatus2(statusCmd);
		}
		
	}

}
