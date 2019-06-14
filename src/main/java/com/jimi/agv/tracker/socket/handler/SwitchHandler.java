package com.jimi.agv.tracker.socket.handler;

import com.jimi.agv.tracker.entity.cmd.base.AGVBaseCmd;
import com.jimi.agv.tracker.socket.AGVMainSocket;
import com.jimi.agv.tracker.util.IdCounter;

/**
 * 启用、禁用、暂停、继续处理器
 * <br>
 * <b>2018年7月13日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class SwitchHandler {

	public static void sendAllStart() throws Exception {
		AGVBaseCmd cmd = new AGVBaseCmd();
		cmd.setCmdid(IdCounter.getCmdId());
		cmd.setCmdcode("allstart");
		AGVMainSocket.sendMessage(cmd);
	}
}
