package com.jimi.uw_server.comparator;

import java.util.Comparator;

import com.jimi.uw_server.agv.gaitek.entity.bo.AGVIOTaskItem;


/**
 * 任务条目优先级比较器
 * 
 * @author HardyYao
 * @createTime 2018年11月9日 下午3:35:49
 */

public class IOTaskItemComparator implements Comparator<AGVIOTaskItem> {

	public static IOTaskItemComparator me = new IOTaskItemComparator();


	@Override
	public int compare(AGVIOTaskItem a1, AGVIOTaskItem a2) {
		return a1.getId() - a2.getId();
	}
}
