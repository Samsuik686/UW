package com.jimi.uw_server.agv.entity.bo;

/**
 * 
 * @author HardyYao
 * @createTime 2018年12月12日  下午5:24:18
 */

public class AGVBuildTaskItem {

	private Integer robotId;
	
	private Integer state;
	
	private Integer boxId;
	
	private String srcPosition;


	public Integer getRobotId() {
		return robotId;
	}

	public void setRobotId(Integer robotId) {
		this.robotId = robotId;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getBoxId() {
		return boxId;
	}

	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}

	public String getSrcPosition() {
		return srcPosition;
	}

	public void setSrcPosition(String srcPosition) {
		this.srcPosition = srcPosition;
	}

	public String getGroupId() {
		return boxId.toString();
	}


	public AGVBuildTaskItem(Integer boxId, String srcPosition) {
		this.boxId = boxId;
		this.srcPosition = srcPosition;
		this.state = 0;
	}

}
