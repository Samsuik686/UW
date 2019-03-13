package com.jimi.uw_server.constant;

/**
 * 出入库任务条目状态常量类
 * @author HardyYao
 * @createTime 2018年11月21日  下午4:12:31
 */

public class IOTaskItemState {

	public static final int LACK = -3;				// 缺料

	public static final int FINISH_CUT = -2;		// 已完成截料
	
	public static final int WAIT_SCAN = -1;			// 等待扫码(呼叫叉车)
	
	public static final int WAIT_ASSIGN = 0;		// 未分配拣料
	
	public static final int ASSIGNED = 1;			// 已分配拣料
	
	public static final int ARRIVED_WINDOW = 2;		// 已拣料到站
	
	public static final int START_BACK = 3;			// 已分配回库
	
	public static final int FINISH_BACK = 4;		// 已完成回库

}
