package com.jimi.agv.tracker.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jimi.agv.tracker.entity.bo.Position;
import com.jimi.agv.tracker.task.reporter.Reporter;

import cc.darhao.dautils.api.DateUtil;
import cc.darhao.dautils.api.UuidUtil;

/**
 * 任務條目
 * <b>2019年6月6日</b>
 * 
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public abstract class AGVIOTaskItem {
	
	private Reporter reporter;

	private String key;
	
	private int robotId;
	
	public static final int WAIT_ASSIGN = -1;
	public static final int NOT_START = 0;
	public static final int STARTED = 1;
	public static final int GOTTEN = 2;
	public static final int ARRIVED = 3;
	public static final int RETURNING = 4;
	public static final int FINISHED = 5;

	private Date assignTime;
	private Date startTime;
	private Date gotTime;
	private Date arriveTime;
	private Date returnTime;
	private Date finishTime;
	
	private List<Position> gotTrails;
	private List<Position> transportTrails;
	private List<Position> returnTrails;
	
	private int state;
	

	protected AGVIOTaskItem() {
		gotTrails = new ArrayList<>();
		transportTrails = new ArrayList<>();
		returnTrails = new ArrayList<>();
		state = WAIT_ASSIGN;
		key = UuidUtil.get32UUID();
		robotId = 0;
	}


	public abstract String getDescription();
	
	
	public final void assign() {
		state = AGVIOTaskItem.NOT_START;
		assignTime = new Date();
		showState("指令已发送");
	}
	
	
	public final void start() {
		state = AGVIOTaskItem.STARTED;
		startTime = new Date();
		showState("开始执行");
	}
	

	public final void got() {
		state = AGVIOTaskItem.GOTTEN;
		gotTime = new Date();
		showState("已拿到盒子");
	}
	
	
	public final void arrive() {
		state = AGVIOTaskItem.ARRIVED;
		arriveTime = new Date();
		showState("已到达仓口");
	}
	
	
	public final void reTurn() {
		state = AGVIOTaskItem.RETURNING;
		returnTime = new Date();
		showState("开始送回盒子");
	}
	
	
	public final void finish() {
		state = AGVIOTaskItem.FINISHED;
		finishTime = new Date();
		showState("执行完毕");
	}

	
	private final void showState(String message) {
		String robotInfo = "";
		if(robotId != 0) {
			robotInfo = "叉车" + robotId;
		}
		System.out.println(DateUtil.HHmmss(new Date()) + " - 条目动态：" + getDescription() + " - " + robotInfo + message);
	}
	
	
	public final void addTrail(Position trail) {
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

	
	public final int getState() {
		return state;
	}

	public final String getKey() {
		return key;
	}

	public final Date getAssignTime() {
		return assignTime;
	}


	public final Date getStartTime() {
		return startTime;
	}

	public final Date getGotTime() {
		return gotTime;
	}

	public final Date getArriveTime() {
		return arriveTime;
	}

	public final Date getReturnTime() {
		return returnTime;
	}

	public final Date getFinishTime() {
		return finishTime;
	}

	public final Reporter getReporter() {
		return reporter;
	}
	
	public final List<Position> getGotTrails() {
		return gotTrails;
	}

	public final List<Position> getTransportTrails() {
		return transportTrails;
	}

	public final List<Position> getReturnTrails() {
		return returnTrails;
	}

	public final void setReporter(Reporter reporter) {
		this.reporter = reporter;
	}

	public final void setRobotId(int robotId) {
		this.robotId = robotId;
	}

	public final int getRobotId() {
		return robotId;
	}
	
}
