package com.jimi.uw_server.constant;

/**
 * 任务条目状态常量类
 * @author HardyYao
 * @createTime 2018年11月21日  下午4:12:31
 */

public class TaskItemState {

	public static final int LACK = -3; // 缺料 （仅出入库任务）

	public static final int FINISH_CUT = -2; // 已完成截料（仅出入库任务）

	public static final int WAIT_SCAN = -1; // 等待扫码(呼叫叉车)（仅出入库任务）

	public static final int WAIT_ASSIGN = 0; // 未分配拣料（仅出入库任务）

	public static final int ASSIGNED = 1; // 已分配拣料

	public static final int SEND_BOX = 2; // 运送料盒（取）

	public static final int ARRIVED_WINDOW = 3; // 已拣料到站

	public static final int START_BACK = 4; // 已分配回库

	public static final int BACK_BOX = 5; // 运送料盒（回）

	public static final int FINISH_BACK = 6; // 已完成回库

}
