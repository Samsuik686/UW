package com.jimi.agv.tracker.task.controller;

import com.jimi.agv.tracker.entity.cmd.AGVStatusCmd;
import com.jimi.agv.tracker.main.Main;
import com.jimi.agv.tracker.socket.handler.IOHandler;
import com.jimi.agv.tracker.space.SpaceManager;
import com.jimi.agv.tracker.task.AGVIOTask;
import com.jimi.agv.tracker.task.AGVIOTaskItem;
import com.jimi.agv.tracker.task.CushionAGVIOTaskItem;

public class CushionController extends Controller{
	
	public CushionController(AGVIOTask task) {
		super(task);
	}
	

	@Override
	public void handleStatus0(AGVStatusCmd statusCmd) throws Exception {
		AGVIOTaskItem item = Main.getTask().getItemByKey(statusCmd.getMissiongroupid());
		if(item.getState() == AGVIOTaskItem.NOT_START) {
			item.setRobotId(statusCmd.getRobotid());
			item.start();
		}
	}
	

	@Override
	public void handleStatus1(AGVStatusCmd statusCmd) throws Exception {
		CushionAGVIOTaskItem item = (CushionAGVIOTaskItem) Main.getTask().getItemByKey(statusCmd.getMissiongroupid());
		if(item.getState() == AGVIOTaskItem.STARTED) {
			item.got();
			SpaceManager.getInstance().empty(item.getSourceX(), item.getSourceY(), item.getSourceZ());
		}
	}
	

	@Override
	public void handleStatus2(AGVStatusCmd statusCmd) throws Exception {
		CushionAGVIOTaskItem item = (CushionAGVIOTaskItem) Main.getTask().getItemByKey(statusCmd.getMissiongroupid());
		if(item.getState() == AGVIOTaskItem.GOTTEN) {
			item.finish();
			showProcess();
			SpaceManager.getInstance().fill(item.getTargetX(), item.getTargetY(), item.getTargetZ());
		}
	}


	@Override
	public void sendIOCmd(AGVIOTaskItem item) throws Exception {
		IOHandler.sendLL((CushionAGVIOTaskItem) item);
	}


	@Override
	protected boolean isAssignable(AGVIOTaskItem item) {
		return SpaceManager.getInstance().tryLock((CushionAGVIOTaskItem) item);
	}

}
