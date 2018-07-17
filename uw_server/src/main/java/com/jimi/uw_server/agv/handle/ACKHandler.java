package com.jimi.uw_server.agv.handle;

import com.jfinal.json.Json;
import com.jimi.uw_server.agv.entity.AGVBaseCmd;
import com.jimi.uw_server.agv.socket.AGVWebSocket;

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
		AGVBaseCmd baseCmd = Json.getJson().parse(message, AGVBaseCmd.class);
		int cmdid = baseCmd.getCmdid();
		AGVWebSocket.getCmdidAckMap().put(cmdid, true);
	}

	/**
	 * 处理非ACK指令
	 */
	public static void handleNOTACK(String message) {
		AGVBaseCmd baseCmd = Json.getJson().parse(message, AGVBaseCmd.class);
		//判断是否已经ack过
		for (Integer cmdid : AGVWebSocket.getReceiveNotAckCmdidSet()) {
			if(baseCmd.getCmdid() == cmdid) {
				return;
			}
		}
		//添加到已经ACK的非ACK指令CMDID集合
		AGVWebSocket.getReceiveNotAckCmdidSet().add(baseCmd.getCmdid());
		//发送ack
		baseCmd.setCmdcode("ack");
		AGVWebSocket.sendACK(Json.getJson().toJson(baseCmd));
	}
}
	


