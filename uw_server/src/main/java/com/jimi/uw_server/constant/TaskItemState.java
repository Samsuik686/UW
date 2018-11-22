package com.jimi.uw_server.constant;

/**
 * 任务条目状态枚举类
 * @author HardyYao
 * @createTime 2018年11月21日  下午4:12:31
 */

public class TaskItemState {

	public static final int UNASSIGNABLED = -1;		// 暂不可分配
	public static final int UNALLOCATED = 0;		// 未分配
	public static final int ASSIGNED = 1;			// 已分配
	public static final int ARRIVED = 2;			// 已到站
	public static final int BACKSHELF = 3;			// 已回库
	public static final int COMPLETED = 4;			// 已完成	

}
