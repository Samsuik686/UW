package com.jimi.agv.tracker.handler;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.jimi.agv.tracker.constant.Constant;
import com.jimi.agv.tracker.entity.bo.AGVIOTaskItem;
import com.jimi.agv.tracker.entity.bo.AGVMissionGroup;
import com.jimi.agv.tracker.entity.cmd.AGVMoveCmd;
import com.jimi.agv.tracker.entity.cmd.AGVStatusCmd;
import com.jimi.agv.tracker.main.Main;
import com.jimi.agv.tracker.socket.AGVMainSocket;
import com.jimi.agv.tracker.util.IdCounter;
import com.jimi.agv.tracker.util.PropUtil;

/**
 * 出入库LS、SL命令处理器
 * <br>
 * <b>2018年7月10日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class IOHandler {

	public static void sendSL(AGVIOTaskItem item) throws Exception {
		AGVMainSocket.sendMessage(createSLCmd(item));
	}


	public static void sendLS(AGVIOTaskItem item) throws Exception {
		AGVMainSocket.sendMessage(createLSCmd(item));
	}

	
	/**
	 * 处理Status指令
	 */
	public static void handleStatus(String message) throws Exception {
		AGVStatusCmd statusCmd = JSON.parseObject(message, AGVStatusCmd.class);

		if(statusCmd.getStatus() == 0) {
			handleStatus0(statusCmd);
		}

		if(statusCmd.getStatus() == 1) {
			handleStatus1(statusCmd);
		}
		
		if(statusCmd.getStatus() == 2) {
			handleStatus2(statusCmd);
		}
		
	}


	private static void handleStatus0(AGVStatusCmd statusCmd) throws Exception {
		AGVIOTaskItem item = Main.getTask().getItemByKey(statusCmd.getMissiongroupid());
		if(item.getState() == AGVIOTaskItem.NOT_START) {
			item.start();
		}else if(item.getState() == AGVIOTaskItem.ARRIVED) {
			item.reTurn();
			new Thread(()->{
				try {
					//把下一条任务发送到AGV服务器任务池，避免叉车执行完条目去休息
					Main.getTask().executeNextItem();//另开线程发送避免出现等ack死锁
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
			
		}
	}


	private static void handleStatus1(AGVStatusCmd statusCmd) {
		AGVIOTaskItem item = Main.getTask().getItemByKey(statusCmd.getMissiongroupid());
		if(item.getState() == AGVIOTaskItem.STARTED) {
			item.got();
		}
	}


	private static void handleStatus2(AGVStatusCmd statusCmd) {
		AGVIOTaskItem item = Main.getTask().getItemByKey(statusCmd.getMissiongroupid());
		if(item.getState() == AGVIOTaskItem.GOTTEN) {
			item.arrive();
			new Thread(()->{
				try {
					sendSL(item);//另开线程发送避免出现等ack死锁
				} catch (Exception e) {
					e.printStackTrace();
				}
			}).start();
		}else if(item.getState() == AGVIOTaskItem.RETURNING) {
			item.finish();
			if(Main.getTask().isFinish()) {//如果全部任务条目都完成了就通知主线程
				synchronized (Main.getTask()) {
					Main.getTask().notify();
				}
			}
		}
	}
	

	private static AGVMoveCmd createSLCmd(AGVIOTaskItem item) {
		List<AGVMissionGroup> groups = new ArrayList<>();
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getKey());
		group.setRobotid(PropUtil.getInt(Constant.CONFIG_NAME, Constant.ROBOT_ID_STRING));
		group.setStartx(PropUtil.getInt(Constant.CONFIG_NAME, Constant.WINDOW_X_STRING));
		group.setStarty(PropUtil.getInt(Constant.CONFIG_NAME, Constant.WINDOW_Y_STRING));
		group.setEndx(item.getTargetX());
		group.setEndy(item.getTargetY());
		group.setEndz(item.getTargetZ());
		groups.add(group);
		AGVMoveCmd cmd = new AGVMoveCmd();
		cmd.setCmdcode("SL");
		cmd.setCmdid(IdCounter.getCmdId());
		cmd.setMissiongroups(groups);
		return cmd;
	}


	private static AGVMoveCmd createLSCmd(AGVIOTaskItem item) {
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(item.getKey());
		group.setRobotid(PropUtil.getInt(Constant.CONFIG_NAME, Constant.ROBOT_ID_STRING));
		group.setStartx(item.getTargetX());
		group.setStarty(item.getTargetY());
		group.setStartz(item.getTargetZ());
		group.setEndx(PropUtil.getInt(Constant.CONFIG_NAME, Constant.WINDOW_X_STRING));
		group.setEndy(PropUtil.getInt(Constant.CONFIG_NAME, Constant.WINDOW_Y_STRING));
		List<AGVMissionGroup> groups = new ArrayList<>();
		groups.add(group);
		AGVMoveCmd cmd = new AGVMoveCmd();
		cmd.setCmdcode("LS");
		cmd.setCmdid(IdCounter.getCmdId());
		cmd.setMissiongroups(groups);
		return cmd;
	}

}
