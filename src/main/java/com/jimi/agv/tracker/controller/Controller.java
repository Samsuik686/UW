package com.jimi.agv.tracker.controller;

import com.jimi.agv.tracker.entity.cmd.AGVStatusCmd;
import com.jimi.agv.tracker.task.AGVIOTaskItem;

/**
 * 任务条目状态控制器
 * <br>
 * <b>2019年6月12日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public interface Controller {
	
	void sendIOCmd(AGVIOTaskItem item) throws Exception;

	void handleStatus0(AGVStatusCmd statusCmd) throws Exception;
	
	void handleStatus1(AGVStatusCmd statusCmd) throws Exception;
	
	void handleStatus2(AGVStatusCmd statusCmd) throws Exception;
	
}
