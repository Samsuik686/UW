package com.jimi.uw_server.ur.entity;

import com.jimi.uw_server.ur.entity.base.UrBasePackage;

/**
 * ReachOut包
 * <br>
 * <b>2019年5月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class ReachOutPackage extends UrBasePackage{

	private Integer taskId;
	
	
	public ReachOutPackage() {
		cmdcode = "reach_out";
	}
	

	public Integer getTaskId() {
		return taskId;
	}


	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}
	
}
