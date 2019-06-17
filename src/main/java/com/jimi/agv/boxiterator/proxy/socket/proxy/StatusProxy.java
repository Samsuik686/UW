package com.jimi.agv.boxiterator.proxy.socket.proxy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jimi.agv.boxiterator.proxy.constant.PackageType;
import com.jimi.agv.boxiterator.proxy.container.SessionBox;
import com.jimi.agv.boxiterator.proxy.entity.cmd.AGVStatusCmd;

import cc.darhao.pasta.Pasta;

public class StatusProxy {

	/**
	 * 处理Status指令
	 */
	public static void handleStatus(String message) throws Exception {
		AGVStatusCmd statusCmd = JSON.parseObject(message, AGVStatusCmd.class);
		JSONObject body = new JSONObject();
		body.put("key", statusCmd.getMissiongroupid());
		
		if(statusCmd.getStatus() == 0) {
			Pasta.sendRequest(SessionBox.getSessionByRobotId(statusCmd.getRobotid()), PackageType.STATUS_0, body);
		}

		if(statusCmd.getStatus() == 1) {
			Pasta.sendRequest(SessionBox.getSessionByRobotId(statusCmd.getRobotid()), PackageType.STATUS_1, body);
		}
		
		if(statusCmd.getStatus() == 2) {
			Pasta.sendRequest(SessionBox.getSessionByRobotId(statusCmd.getRobotid()), PackageType.STATUS_2, body);
		}
		
	}
}
