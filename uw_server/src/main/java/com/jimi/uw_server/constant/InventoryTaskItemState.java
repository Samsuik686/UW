package com.jimi.uw_server.constant;

/**
 * 
 * @author HardyYao
 * @createTime 2019年5月17日  上午10:07:33
 */

public class InventoryTaskItemState {

	public static final int WAIT_ASSIGN = 0;		// 未分配拣料
	
	public static final int ASSIGNED = 1;			// 已分配拣料
	
	public static final int ARRIVED_WINDOW = 2;		// 已拣料到站
	
	public static final int START_BACK = 3;			// 已分配回库
	
	public static final int FINISH_BACK = 4;		// 已完成回库
}
