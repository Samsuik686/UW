package com.jimi.agv.tracker.handler;


import com.alibaba.fastjson.JSON;
import com.jimi.agv.tracker.entity.cmd.base.AGVBaseCmd;
import com.jimi.agv.tracker.socket.AGVMainSocket;

/**
 * ACK
 * <br>
 * <b>2018年7月10日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class ACKHandler {

	/**
	 * 处理ACK指令
	 */
	public static void handleACK(String message) {
		//在Map修改该CMDID对应的值为已收到ACK
		AGVBaseCmd baseCmd = JSON.parseObject(message, AGVBaseCmd.class);
		int cmdid = baseCmd.getCmdid();
		AGVMainSocket.getSendCmdidAckMap().put(cmdid, true);
	}

	/**
	 * 处理非ACK指令
	 * @throws Exception 
	 */
	public static boolean handleNOTACK(String message) throws Exception {
		AGVBaseCmd baseCmd = JSON.parseObject(message, AGVBaseCmd.class);
		//判断是否已经ack过
		for (Integer cmdid : AGVMainSocket.getReceiveNotAckCmdidSet()) {
			if(baseCmd.getCmdid() == cmdid) {
				return false;
			}
		}
		//发送ack
		baseCmd.setCmdcode("ack");
		AGVMainSocket.sendACK(baseCmd);
		//添加到已经ACK的非ACK指令CMDID集合
		AGVMainSocket.getReceiveNotAckCmdidSet().add(baseCmd.getCmdid());
		return true;
	}
}
	



