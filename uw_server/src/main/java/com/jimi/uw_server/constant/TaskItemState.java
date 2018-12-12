package com.jimi.uw_server.constant;

/**
 * 任务条目状态常量类
 * @author HardyYao
 * @createTime 2018年11月21日  下午4:12:31
 */

public class TaskItemState {

	public static final int FINISH_CUT = -2;		// 已完成截料
	
	public static final int UNASSIGNABLED = -1;		// 暂不可分配
	
	public static final int WAIT_ASSIGN = 0;		// 未分配拣料
	
	public static final int ASSIGNED = 1;			// 已分配拣料
	
	public static final int ARRIVED_WINDOW = 2;		// 已拣料到站
	
	public static final int START_BACK = 3;			// 已分配回库
	
	public static final int FINISH_BACK = 4;		// 已完成回库

	public static final int WAIT_MOVE = 0;			// 叉车接受任务

	public static final int FINISH_SECOND_ACTION = 2;		// 叉车完成第二动作
}
