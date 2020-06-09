package com.jimi.uw_server.ur.entity;

import com.jimi.uw_server.ur.entity.base.UrBasePackage;


/**
 * <p>
 * Title: ForkliftReachPackage
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2019
 * </p>
 * <p>
 * Company: 惠州市几米物联技术有限公司
 * </p>
 * 
 * @author trjie
 * @date 2019年12月25日
 *
 */
public class ForkliftReachPackage extends UrBasePackage {

	private Integer taskId;

	private Integer boxId;


	/**
	 * <p>
	 * Title
	 * <p>
	 * <p>
	 * Description
	 * <p>
	 */
	public ForkliftReachPackage(Integer taskId, Integer boxId) {
		this.cmdCode = "forklift_reach";
		this.taskId = taskId;
		this.boxId = boxId;
	}


	public Integer getTaskId() {
		return taskId;
	}


	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}


	public Integer getBoxId() {
		return boxId;
	}


	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}


}
