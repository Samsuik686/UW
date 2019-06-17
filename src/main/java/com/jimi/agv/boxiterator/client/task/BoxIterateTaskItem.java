package com.jimi.agv.boxiterator.client.task;

import java.util.Date;

import cc.darhao.dautils.api.DateUtil;
import cc.darhao.dautils.api.UuidUtil;

/**
 * 任務條目
 * <b>2019年6月6日</b>
 * 
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class BoxIterateTaskItem {
	
	private String key;
	
	private int targetX;
	private int targetY;
	private int targetZ;
	
	public static final int NOT_START = 0;
	public static final int STARTED = 1;
	public static final int GOTTEN = 2;
	public static final int ARRIVED = 3;
	public static final int RETURNING = 4;
	public static final int FINISHED = 5;

	private int state;
	

	public BoxIterateTaskItem(int targetX, int targetY, int targetZ) {
		this.targetX = targetX;
		this.targetY = targetY;
		this.targetZ = targetZ;
		state = NOT_START;
		key = UuidUtil.get32UUID();
	}


	public String getDescription() {
		return "[" + targetX + "," + targetY + "," + targetZ + "]";
	}
	
	
	public final void start() {
		state = BoxIterateTaskItem.STARTED;
		showState("开始执行");
	}
	

	public final void got() {
		state = BoxIterateTaskItem.GOTTEN;
		showState("已拿到盒子");
	}
	
	
	public final void arrive() {
		state = BoxIterateTaskItem.ARRIVED;
		showState("已到达仓口");
	}
	
	
	public final void reTurn() {
		state = BoxIterateTaskItem.RETURNING;
		showState("开始送回盒子");
	}
	
	
	public final void finish() {
		state = BoxIterateTaskItem.FINISHED;
		showState("执行完毕");
	}

	
	private final void showState(String message) {
		System.out.println(DateUtil.HHmmss(new Date()) + " - 条目动态：" + getDescription() + message);
	}
	
	
	public final int getState() {
		return state;
	}

	public final String getKey() {
		return key;
	}


	public int getTargetX() {
		return targetX;
	}


	public int getTargetY() {
		return targetY;
	}


	public int getTargetZ() {
		return targetZ;
	}


	public static int getNotStart() {
		return NOT_START;
	}


	public static int getStarted() {
		return STARTED;
	}


	public static int getGotten() {
		return GOTTEN;
	}


	public static int getArrived() {
		return ARRIVED;
	}


	public static int getReturning() {
		return RETURNING;
	}


	public static int getFinished() {
		return FINISHED;
	}
	
}
