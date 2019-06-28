package com.jimi.agv.tracker.task.controller;

import com.jimi.agv.tracker.entity.cmd.AGVStatusCmd;
import com.jimi.agv.tracker.task.AGVIOTaskItem;

/**
 * 任务条目状态控制器
 * <br>
 * <b>2019年6月12日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public abstract class Controller {

	public boolean tryAssign(AGVIOTaskItem item) {
		if(item.getState() == AGVIOTaskItem.WAIT_ASSIGN && isAssignable(item)) {
			try {
				sendIOCmd(item);
				item.assign();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	

	protected abstract boolean isAssignable(AGVIOTaskItem item);

	public abstract void sendIOCmd(AGVIOTaskItem item) throws Exception;

	public abstract void handleStatus0(AGVStatusCmd statusCmd) throws Exception;
	
	public abstract void handleStatus1(AGVStatusCmd statusCmd) throws Exception;
	
	public abstract void handleStatus2(AGVStatusCmd statusCmd) throws Exception;
	
}
