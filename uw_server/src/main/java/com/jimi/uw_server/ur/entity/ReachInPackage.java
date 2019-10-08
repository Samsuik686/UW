package com.jimi.uw_server.ur.entity;

import com.jimi.uw_server.ur.entity.base.UrBasePackage;


/**
 * ReachIn包
 * <br>
 * <b>2019年5月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class ReachInPackage extends UrBasePackage {

	private Integer taskId;


	public ReachInPackage() {
		cmdcode = "reach_in";
	}


	public Integer getTaskId() {
		return taskId;
	}


	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

}
