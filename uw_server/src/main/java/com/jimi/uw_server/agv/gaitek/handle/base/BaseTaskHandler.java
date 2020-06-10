package com.jimi.uw_server.agv.gaitek.handle.base;

import java.util.ArrayList;
import java.util.List;

import com.jimi.uw_server.agv.gaitek.dao.TaskPropertyRedisDAO;
import com.jimi.uw_server.agv.gaitek.entity.CmdStatus;
import com.jimi.uw_server.agv.gaitek.entity.bo.AGVMissionGroup;
import com.jimi.uw_server.agv.gaitek.entity.bo.base.BaseTaskItem;
import com.jimi.uw_server.agv.gaitek.entity.cmd.AGVMoveCmd;
import com.jimi.uw_server.agv.gaitek.entity.cmd.AGVStatusCmd;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.MaterialBox;


public abstract class BaseTaskHandler {

	public abstract void sendSendLL(BaseTaskItem item, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority) throws Exception;


	public abstract void sendBackLL(BaseTaskItem item, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority) throws Exception;


	/**
	 * 处理Status指令
	 */
	public void handleStatus(AGVStatusCmd statusCmd) throws Exception {
		// 判断是否是开始执行任务
		switch (statusCmd.getStatus()) {
		case CmdStatus.START:
			handleStartStatus(statusCmd);
			break;
		case CmdStatus.PROCESS:
			handleProcessStatus(statusCmd);
			break;
		case CmdStatus.FINISH:
			handleFinishStatus(statusCmd);
			break;
		default:
			break;
		}

	}


	protected abstract void handleStartStatus(AGVStatusCmd statusCmd) throws Exception;


	protected abstract void handleProcessStatus(AGVStatusCmd statusCmd) throws Exception;


	protected abstract void handleFinishStatus(AGVStatusCmd statusCmd) throws Exception;


	public AGVMoveCmd createBackLLCmd(String groupId, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority) {
		List<AGVMissionGroup> groups = new ArrayList<>();
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(groupId + "_B");
		group.setRobotid(0);
		group.setStartx(goodsLocation.getXPosition());// 起点X为货位X
		group.setStarty(goodsLocation.getYPosition());// 起点Y为货位Y
		group.setStartz(goodsLocation.getZPosition());// 起点Z为货位Z
		group.setEndx(materialBox.getRow());// 设置X
		group.setEndy(materialBox.getCol());// 设置Y
		group.setEndz(materialBox.getHeight());// 设置Z
		group.setPriority(String.valueOf(priority));
		groups.add(group);
		AGVMoveCmd moveCmd = new AGVMoveCmd();
		moveCmd.setCmdcode("LL");
		moveCmd.setCmdid(TaskPropertyRedisDAO.getCmdId());
		moveCmd.setMissiongroups(groups);
		return moveCmd;
	}


	public AGVMoveCmd createSendLLCmd(String groupId, MaterialBox materialBox, GoodsLocation goodsLocation, Integer priority) {
		List<AGVMissionGroup> groups = new ArrayList<>();
		AGVMissionGroup group = new AGVMissionGroup();
		group.setMissiongroupid(groupId + "_S");
		group.setRobotid(0);
		group.setEndx(goodsLocation.getXPosition());// 终点X为货位X
		group.setEndy(goodsLocation.getYPosition());// 终点Y为货位Y
		group.setEndz(goodsLocation.getZPosition());// 终点Z为货位Z
		group.setStartx(materialBox.getRow());// 设置X
		group.setStarty(materialBox.getCol());// 设置Y
		group.setStartz(materialBox.getHeight());// 设置Z
		group.setPriority(String.valueOf(priority));
		groups.add(group);
		AGVMoveCmd moveCmd = new AGVMoveCmd();
		moveCmd.setCmdcode("LL");
		moveCmd.setCmdid(TaskPropertyRedisDAO.getCmdId());
		moveCmd.setMissiongroups(groups);
		return moveCmd;
	}

}
