package com.jimi.agv.tracker.entity.bo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cc.darhao.dautils.api.DateUtil;
import cc.darhao.dautils.api.UuidUtil;

/**
 * 任務條目
 * <b>2019年6月6日</b>
 * 
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class AGVIOTaskItem {

	private String key;
	
	public static final int NOT_START = 0;
	public static final int STARTED = 1;
	public static final int GOTTEN = 2;
	public static final int ARRIVED = 3;
	public static final int RETURNING = 4;
	public static final int FINISHED = 5;

	private Date startTime;
	private Date gotTime;
	private Date arriveTime;
	private Date returnTime;
	private Date finishTime;
	
	private List<Position> gotTrails;
	private List<Position> transportTrails;
	private List<Position> returnTrails;
	
	private int targetX;
	private int targetY;
	private int targetZ;

	private int state;

	public AGVIOTaskItem(int targetX, int targetY, int targetZ) {
		this.targetX = targetX;
		this.targetY = targetY;
		this.targetZ = targetZ;
		gotTrails = new ArrayList<>();
		transportTrails = new ArrayList<>();
		returnTrails = new ArrayList<>();
		state = NOT_START;
		key = UuidUtil.get32UUID();
	}
	

	public String getReport() {
		StringBuffer sb = new StringBuffer();
		sb.append(getTargetString() + "\n");
		sb.append("开始->取盒耗时："+ (((gotTime.getTime() - startTime.getTime()) / 1000)) + "\n");
		for (Position position : gotTrails) {
			sb.append(position.toString()+"\n");
		}
		sb.append("取盒->到站耗时："+ (((arriveTime.getTime() - gotTime.getTime()) / 1000)) + "\n");
		for (Position position : transportTrails) {
			sb.append(position.toString()+"\n");
		}
		sb.append("到站->送回耗时："+ (((finishTime.getTime() - returnTime.getTime()) / 1000)) + "\n");
		for (Position position : returnTrails) {
			sb.append(position.toString()+"\n");
		}
		return sb.toString();
	}
	
	
	public String getTargetString() {
		return "[ " + targetX + ", " + targetY + ", " + targetZ + " ]";
	}
	
	
	public void start() {
		state = AGVIOTaskItem.STARTED;
		startTime = new Date();
		showState("开始执行");
	}
	

	public void got() {
		state = AGVIOTaskItem.GOTTEN;
		gotTime = new Date();
		showState("已拿到盒子");
	}
	
	
	public void arrive() {
		state = AGVIOTaskItem.ARRIVED;
		arriveTime = new Date();
		showState("已到达仓口");
	}
	
	
	public void reTurn() {
		state = AGVIOTaskItem.RETURNING;
		returnTime = new Date();
		showState("开始送回盒子");
	}
	
	
	public void finish() {
		state = AGVIOTaskItem.FINISHED;
		finishTime = new Date();
		showState("已送回盒子");
	}

	
	private void showState(String message) {
		System.out.println(DateUtil.HHmmss(new Date()) + " - 条目动态：" + getTargetString() + " - " + message);
	}
	
	
	public void addTrail(Position trail) {
		switch (state) {
		case STARTED:
			gotTrails.add(trail);
			break;
		case GOTTEN:
			transportTrails.add(trail);
			break;
		case RETURNING:
			returnTrails.add(trail);
			break;
		default:
			break;
		}
	}

	
	public int getState() {
		return state;
	}
	
	public int getTargetZ() {
		return targetZ;
	}

	public int getTargetY() {
		return targetY;
	}

	public int getTargetX() {
		return targetX;
	}

	public String getKey() {
		return key;
	}

	public Date getStartTime() {
		return startTime;
	}

	public Date getGotTime() {
		return gotTime;
	}

	public Date getArriveTime() {
		return arriveTime;
	}

	public Date getReturnTime() {
		return returnTime;
	}

	public Date getFinishTime() {
		return finishTime;
	}

}