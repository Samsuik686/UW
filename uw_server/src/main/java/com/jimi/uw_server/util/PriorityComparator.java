package com.jimi.uw_server.util;

import java.util.Comparator;

import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;

/**
 * 任务条目优先级比较器
 * 
 * @author HardyYao
 * @createTime 2018年11月9日  下午3:35:49
 */

public class PriorityComparator implements Comparator<AGVIOTaskItem> {

	@Override
	public int compare(AGVIOTaskItem a1, AGVIOTaskItem a2) {
		if (a1.getPriority() > a2.getPriority()) {	// 根据任务条目优先级数值大小，进行降序排序
			return -1;
		} else {
			return 1;
		}
	}

}
