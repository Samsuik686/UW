package com.jimi.uw_server.comparator;

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
		// 根据任务条目优先级数值大小，进行降序排序
		return a2.getPriority() - a1.getPriority();
	}

}
