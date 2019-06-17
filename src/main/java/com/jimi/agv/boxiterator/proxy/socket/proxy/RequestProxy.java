package com.jimi.agv.boxiterator.proxy.socket.proxy;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.Session;

import com.alibaba.fastjson.JSONObject;
import com.jimi.agv.boxiterator.proxy.container.SessionBox;
import com.jimi.agv.boxiterator.proxy.entity.bo.AGVMissionGroup;
import com.jimi.agv.boxiterator.proxy.entity.bo.BoxIteratorTaskInfo;
import com.jimi.agv.boxiterator.proxy.entity.cmd.AGVMoveCmd;
import com.jimi.agv.boxiterator.proxy.socket.AGVMainSocket;
import com.jimi.agv.boxiterator.proxy.util.IdCounter;

/**
 * 请求代理
 * <br>
 * <b>2018年7月10日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class RequestProxy {

	public JSONObject login(Session session, int robotId, int windowX, int windowY) {
		JSONObject resp = new JSONObject();
		resp.put("result", SessionBox.addSession(session, new BoxIteratorTaskInfo(robotId, windowX, windowY)));
		return resp;
	}


	public void get(Session session, String key, int targetX, int targetY, int targetZ) throws Exception {
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(key);
		group.setRobotid(SessionBox.getInfoBySession(session).getRobotId());
		group.setStartx(targetX);
		group.setStarty(targetY);
		group.setStartz(targetZ);
		group.setEndx(SessionBox.getInfoBySession(session).getWindowX());
		group.setEndy(SessionBox.getInfoBySession(session).getWindowY());
		List<AGVMissionGroup> groups = new ArrayList<>();
		groups.add(group);
		AGVMoveCmd cmd = new AGVMoveCmd();
		cmd.setCmdcode("LS");
		cmd.setCmdid(IdCounter.getCmdId());
		cmd.setMissiongroups(groups);
		AGVMainSocket.sendMessage(cmd);
	}


	public void reTurn(Session session, String key, int targetX, int targetY, int targetZ) throws Exception {
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(key);
		group.setRobotid(SessionBox.getInfoBySession(session).getRobotId());
		group.setStartx(SessionBox.getInfoBySession(session).getWindowX());
		group.setStarty(SessionBox.getInfoBySession(session).getWindowY());
		group.setEndx(targetX);
		group.setEndy(targetY);
		group.setEndz(targetZ);
		List<AGVMissionGroup> groups = new ArrayList<>();
		groups.add(group);
		AGVMoveCmd cmd = new AGVMoveCmd();
		cmd.setCmdcode("SL");
		cmd.setCmdid(IdCounter.getCmdId());
		cmd.setMissiongroups(groups);
		AGVMainSocket.sendMessage(cmd);
	}

}
