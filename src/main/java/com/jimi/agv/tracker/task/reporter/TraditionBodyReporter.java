package com.jimi.agv.tracker.task.reporter;

import com.jimi.agv.tracker.entity.bo.Position;
import com.jimi.agv.tracker.entity.model.TraditionBody;
import com.jimi.agv.tracker.task.AGVIOTaskItem;
import com.jimi.agv.tracker.task.TraditionAGVIOTaskItem;

import cc.darhao.dautils.api.DateUtil;

public class TraditionBodyReporter implements Reporter{

	private TraditionAGVIOTaskItem item;
	
	private long headId;
	
	
	public TraditionBodyReporter(AGVIOTaskItem item) {
		this.item = (TraditionAGVIOTaskItem) item;
	}
	

	@Override
	public String getReport() {
		StringBuffer sb = new StringBuffer();
		sb.append(item.getDescription() + "\n");
		sb.append("执行叉车:" + item.getRobotId() + "\n");
		sb.append("指派时间：" + DateUtil.HHmmss(item.getAssignTime()) + "\n");
		sb.append("开始时间：" + DateUtil.HHmmss(item.getStartTime()) + "\n");
		sb.append("取盒时间：" + DateUtil.HHmmss(item.getGotTime()) + "\n");
		sb.append("到站时间：" + DateUtil.HHmmss(item.getArriveTime()) + "\n");
		sb.append("返回时间：" + DateUtil.HHmmss(item.getReturnTime()) + "\n");
		sb.append("完成时间：" + DateUtil.HHmmss(item.getFinishTime()) + "\n");
		sb.append("开始->取盒耗时："+ (((item.getGotTime().getTime() - item.getStartTime().getTime()) / 1000)) + "\n");
		for (Position position : item.getGotTrails()) {
			sb.append(position.toString()+"\n");
		}
		sb.append("取盒->到站耗时："+ (((item.getArriveTime().getTime() - item.getGotTime().getTime()) / 1000)) + "\n");
		for (Position position : item.getTransportTrails()) {
			sb.append(position.toString()+"\n");
		}
		sb.append("到站->送回耗时："+ (((item.getFinishTime().getTime() - item.getReturnTime().getTime()) / 1000)) + "\n");
		for (Position position : item.getReturnTrails()) {
			sb.append(position.toString()+"\n");
		}
		return sb.toString();
	}

	
	@Override
	public void saveToDb() {
		TraditionBody traditionBody = new TraditionBody();
		traditionBody.setTraditionHead(headId);
		traditionBody.setRobotId(item.getRobotId());
		traditionBody.setAssignTime(item.getAssignTime());
		traditionBody.setStartTime(item.getStartTime());
		traditionBody.setGotTime(item.getGotTime());
		traditionBody.setArriveTime(item.getArriveTime());
		traditionBody.setReturnTime(item.getReturnTime());
		traditionBody.setFinishTime(item.getFinishTime());
		traditionBody.setTargetPosition(item.getDescription());
		traditionBody.setGotConsumeTime((int) ((item.getGotTime().getTime() - item.getStartTime().getTime()) / 1000));
		traditionBody.setTransportConsumeTime((int) ((item.getArriveTime().getTime() - item.getGotTime().getTime()) / 1000));
		traditionBody.setReturnConsumeTime((int) ((item.getFinishTime().getTime() - item.getReturnTime().getTime()) / 1000));
		StringBuffer sb = new StringBuffer();
		for (Position position : item.getGotTrails()) {
			sb.append(position + " ");
		}
		traditionBody.setGotTrails(sb.toString());
		sb = new StringBuffer();
		for (Position position : item.getTransportTrails()) {
			sb.append(position + " ");
		}
		traditionBody.setTransportTrails(sb.toString());
		sb = new StringBuffer();
		for (Position position : item.getReturnTrails()) {
			sb.append(position + " ");
		}
		traditionBody.setReturnTrails(sb.toString());
		traditionBody.save();
	}

	
	public long getHeadId() {
		return headId;
	}


	public void setHeadId(long headId) {
		this.headId = headId;
	}

}
