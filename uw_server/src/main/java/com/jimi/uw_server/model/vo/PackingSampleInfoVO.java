package com.jimi.uw_server.model.vo;

import java.util.List;

/**
 * 
 * @author HardyYao
 * @createTime 2019年6月21日  下午4:22:53
 */

public class PackingSampleInfoVO {

	public String groupId;
	
	private Integer taskId;
	
	private Integer boxId;
	
	private Integer windowId;
	
	private List<MaterialInfoVO> list;

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

	public Integer getWindowId() {
		return windowId;
	}

	public void setWindowId(Integer windowId) {
		this.windowId = windowId;
	}

	public List<MaterialInfoVO> getList() {
		return list;
	}

	public void setList(List<MaterialInfoVO> list) {
		this.list = list;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	
}
