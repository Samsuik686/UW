package com.jimi.agv.tracker.controller;

import com.jimi.agv.tracker.entity.cmd.AGVStatusCmd;
import com.jimi.agv.tracker.handler.IOHandler;
import com.jimi.agv.tracker.main.Main;
import com.jimi.agv.tracker.task.AGVIOTaskItem;
import com.jimi.agv.tracker.task.CushionAGVIOTaskItem;

public class CushionController implements Controller{

	@Override
	public void handleStatus0(AGVStatusCmd statusCmd) throws Exception {
		AGVIOTaskItem item = Main.getTask().getItemByKey(statusCmd.getMissiongroupid());
		if(item.getState() == AGVIOTaskItem.NOT_START) {
			item.start();
		}
	}
	

	@Override
	public void handleStatus1(AGVStatusCmd statusCmd) throws Exception {
		AGVIOTaskItem item = Main.getTask().getItemByKey(statusCmd.getMissiongroupid());
		if(item.getState() == AGVIOTaskItem.STARTED) {
			item.got();
			//把下一条任务发送到AGV服务器任务池，避免叉车执行完条目去休息
			Main.getTask().executeNextItem();
		}
	}
	

	@Override
	public void handleStatus2(AGVStatusCmd statusCmd) throws Exception {
		AGVIOTaskItem item = Main.getTask().getItemByKey(statusCmd.getMissiongroupid());
		if(item.getState() == AGVIOTaskItem.GOTTEN) {
			item.finish();
			if(Main.getTask().isFinish()) {//如果全部任务条目都完成了就通知主线程
				Main.notifyForTaskFinish();
			}
		}
	}


	@Override
	public void sendIOCmd(AGVIOTaskItem item) throws Exception {
		IOHandler.sendLL((CushionAGVIOTaskItem) item);
	}

}
