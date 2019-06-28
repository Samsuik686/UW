package com.jimi.agv.tracker.task.comparator;

import java.util.Comparator;

import com.jimi.agv.tracker.task.AGVIOTask;

public class AGVIOTaskPWeightComparator implements Comparator<AGVIOTask> {

	@Override
	public int compare(AGVIOTask o1, AGVIOTask o2) {
		return o1.getWeight() - o2.getWeight();
	}

}
