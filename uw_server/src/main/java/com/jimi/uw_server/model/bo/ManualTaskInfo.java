package com.jimi.uw_server.model.bo;

import java.util.List;


public class ManualTaskInfo {

	private Integer taskId;

	private List<ManualTaskRecord> records;


	public Integer getTaskId() {
		return taskId;
	}


	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}


	public List<ManualTaskRecord> getRecords() {
		return records;
	}


	public void setRecords(List<ManualTaskRecord> records) {
		this.records = records;
	}

}
