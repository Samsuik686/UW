package com.jimi.agv_mock.handle;

import com.alibaba.fastjson.JSON;
import com.jimi.agv_mock.entity.cmd.AGVMoveCmd;
import com.jimi.agv_mock.socket.MockMainSocket;

/**
 * LL处理器
 * @author HardyYao
 * @createTime 2018年12月10日  上午10:29:39
 */

public class LLHandler {


	public static void handleLL(String message) {
		// 转换成实体类
		AGVMoveCmd moveCmd = JSON.parseObject(message, AGVMoveCmd.class);
		handleLL(moveCmd);
	}


	public static void handleLL(AGVMoveCmd cmd) {
		//放入任务池
		MockMainSocket.getTaskPool().addLLTask(cmd);
	}


}
