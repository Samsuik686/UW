/**  
*  
*/  
package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.List;

import com.jimi.uw_server.agv.dao.PositionTaskRobotInfoRedisDAO;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.model.BoxPositionTask;

/**  
 * <p>Title: BoxPositionTaskVO</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月3日
 *
 */
public class BoxPositionTaskVO {

	private Integer id;
	
	private Integer state;
	
	private String name;
	
	private String stateString;
	
	private Boolean isRunning;
	/**
	 * <p>Title<p>
	 * <p>Description<p>
	 */
	public BoxPositionTaskVO() {
		// TODO Auto-generated constructor stub
	}
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getState() {
		return state;
	}
	public void setState(Integer state) {
		this.state = state;
		switch (state) {
		case TaskState.WAIT_START:
			this.stateString = "未开始";
			break;
		case TaskState.PROCESSING:
			this.stateString = "进行中";
			break;
		case TaskState.FINISHED:
			this.stateString = "已完成";
			break;
		case TaskState.CANCELED:
			this.stateString = "已作废";
			break;
		default:
			break;
		}
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStateString() {
		return stateString;
	}
	public void setStateString(String stateString) {
		this.stateString = stateString;
	}
	public Boolean getIsRunning() {
		return isRunning;
	}
	public void setIsRunning(Boolean isRunning) {
		this.isRunning = isRunning;
	}
	public static List<BoxPositionTaskVO> fillList(List<BoxPositionTask> tasks) {
		if (tasks != null && !tasks.isEmpty()) {
			List<BoxPositionTaskVO> boxPositionTaskVOs = new ArrayList<BoxPositionTaskVO>(tasks.size());
			for (BoxPositionTask boxPositionTask : tasks) {
				BoxPositionTaskVO boxPositionTaskVO = new BoxPositionTaskVO();
				boxPositionTaskVO.setId(boxPositionTask.getId());
				boxPositionTaskVO.setName(boxPositionTask.getName());
				boxPositionTaskVO.setState(boxPositionTask.getState());
				if (boxPositionTask.getState().equals(TaskState.PROCESSING)) {
					boxPositionTaskVO.setIsRunning(PositionTaskRobotInfoRedisDAO.getPositionTaskStatus());
				}
				boxPositionTaskVOs.add(boxPositionTaskVO);
			}
			return boxPositionTaskVOs;
		}
		return null;
	}
	
}
