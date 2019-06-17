package com.jimi.agv.tracker.task.controller;

import com.jimi.agv.tracker.entity.cmd.AGVStatusCmd;
import com.jimi.agv.tracker.main.Main;
import com.jimi.agv.tracker.socket.handler.IOHandler;
import com.jimi.agv.tracker.space.SpaceManager;
import com.jimi.agv.tracker.task.AGVIOTask;
import com.jimi.agv.tracker.task.AGVIOTaskItem;
import com.jimi.agv.tracker.task.TraditionAGVIOTaskItem;

public class TraditionController extends Controller{

	public TraditionController(AGVIOTask task) {
		super(task);
	}
	
	
	@Override
	public void handleStatus0(AGVStatusCmd statusCmd) throws Exception {
		AGVIOTaskItem item = Main.getTask().getItemByKey(statusCmd.getMissiongroupid());
		if(item.getState() == AGVIOTaskItem.NOT_START) {
			item.setRobotId(statusCmd.getRobotid());
			item.start();
		}else if(item.getState() == AGVIOTaskItem.ARRIVED) {
			item.reTurn();
		}
	}
	

	@Override
	public void handleStatus1(AGVStatusCmd statusCmd) {
		TraditionAGVIOTaskItem item = (TraditionAGVIOTaskItem) Main.getTask().getItemByKey(statusCmd.getMissiongroupid());
		if(item.getState() == AGVIOTaskItem.STARTED) {
			item.got();
		}
	}
	

	@Override
	public void handleStatus2(AGVStatusCmd statusCmd) throws Exception {
		TraditionAGVIOTaskItem item = (TraditionAGVIOTaskItem) Main.getTask().getItemByKey(statusCmd.getMissiongroupid());
		if(item.getState() == AGVIOTaskItem.GOTTEN) {
			item.arrive();
			IOHandler.sendSL((TraditionAGVIOTaskItem) item);
		}else if(item.getState() == AGVIOTaskItem.RETURNING) {
			item.finish();
			showProcess();
			SpaceManager.getInstance().fill(item.getTargetX(), item.getTargetY(), item.getTargetZ());
		}
	}


	@Override
	public void sendIOCmd(AGVIOTaskItem item) throws Exception {
		IOHandler.sendLS((TraditionAGVIOTaskItem) item);
	}


	@Override
	protected boolean isAssignable(AGVIOTaskItem item) {
		return SpaceManager.getInstance().tryLock((TraditionAGVIOTaskItem)item);
	}

}
