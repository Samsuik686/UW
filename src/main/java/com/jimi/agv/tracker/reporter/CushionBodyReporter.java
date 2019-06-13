package com.jimi.agv.tracker.reporter;

import com.jimi.agv.tracker.entity.bo.Position;
import com.jimi.agv.tracker.entity.model.CushionBody;
import com.jimi.agv.tracker.task.AGVIOTaskItem;
import com.jimi.agv.tracker.task.CushionAGVIOTaskItem;

public class CushionBodyReporter implements Reporter{

	private CushionAGVIOTaskItem item;
	
	private long headId;
	
	
	public CushionBodyReporter(AGVIOTaskItem item) {
		this.item = (CushionAGVIOTaskItem) item;
	}
	

	@Override
	public String getReport() {
		StringBuffer sb = new StringBuffer();
		sb.append(item.getDescription() + "\n");
		sb.append("开始->取盒耗时："+ (((item.getGotTime().getTime() - item.getStartTime().getTime()) / 1000)) + "\n");
		for (Position position : item.getGotTrails()) {
			sb.append(position.toString()+"\n");
		}
		sb.append("取盒->到站耗时："+ (((item.getFinishTime().getTime() - item.getGotTime().getTime()) / 1000)) + "\n");
		for (Position position : item.getTransportTrails()) {
			sb.append(position.toString()+"\n");
		}
		return sb.toString();
	}

	
	@Override
	public void saveToDb() {
		CushionBody cushionBody = new CushionBody();
		cushionBody.setCushionHead(headId);
		cushionBody.setSourcePosition("[" + item.getSourceX() + "," + item.getSourceY() + "," + item.getSourceZ() + "]");
		cushionBody.setTargetPosition("[" + item.getTargetX() + "," + item.getTargetY() + "," + item.getTargetZ() + "]");
		cushionBody.setGotConsumeTime((int) ((item.getGotTime().getTime() - item.getStartTime().getTime()) / 1000));
		cushionBody.setTransportConsumeTime((int) ((item.getFinishTime().getTime() - item.getGotTime().getTime()) / 1000));
		StringBuffer sb = new StringBuffer();
		for (Position position : item.getGotTrails()) {
			sb.append(position + " ");
		}
		cushionBody.setGotTrails(sb.toString());
		sb = new StringBuffer();
		for (Position position : item.getTransportTrails()) {
			sb.append(position + " ");
		}
		cushionBody.setTransportTrails(sb.toString());
		cushionBody.save();
	}
	
	
	public long getHeadId() {
		return headId;
	}


	public void setHeadId(long headId) {
		this.headId = headId;
	}
	
}
