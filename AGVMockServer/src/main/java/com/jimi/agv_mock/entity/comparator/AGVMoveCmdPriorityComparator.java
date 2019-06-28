package com.jimi.agv_mock.entity.comparator;

import java.util.Comparator;

import com.jimi.agv_mock.entity.cmd.AGVMoveCmd;

public class AGVMoveCmdPriorityComparator implements Comparator<AGVMoveCmd>{

	@Override
	public int compare(AGVMoveCmd o1, AGVMoveCmd o2) {
		if(o1.getMissiongroups().size() == 1 && o2.getMissiongroups().size() == 1) {
			return Integer.parseInt(o1.getMissiongroups().get(0).getPriority()) - Integer.parseInt(o2.getMissiongroups().get(0).getPriority());
		}else {
			return 0;
		}
	}

	
	
}
