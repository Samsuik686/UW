package com.jimi.uw_server.constant;

/**
 * 任务状态枚举类
 * @author HardyYao
 * @createTime 2018年11月21日  下午4:44:49
 */

public class TaskState {

	public static final int UNREVIEWED = 0;			// 未审核
	public static final int TOBESTARTED = 1;		// 未开始
	public static final int PROCESSING = 2;			// 进行中
	public static final int FINISHED = 3;			// 已完成
	public static final int CANCELED = 4;			// 已作废

}
