/**  
*  
*/  
package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.List;

import com.jimi.uw_server.constant.PositionTaskItemState;
import com.jimi.uw_server.model.BoxPositionTaskItem;

/**  
 * <p>Title: BoxPositionTaskItemVO</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月3日
 *
 */
public class BoxPositionTaskItemVO {

	private Integer id;
	
	private Integer x1;
	
	private Integer y1;
	
	private Integer z1;
	
	private Integer x2;
	
	private Integer y2;
	
	private Integer z2;
	
	private Integer bindId;
	
	private Integer status;
	
	private String statusString;

	private Integer robotId;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getX1() {
		return x1;
	}

	public void setX1(Integer x1) {
		this.x1 = x1;
	}

	public Integer getY1() {
		return y1;
	}

	public void setY1(Integer y1) {
		this.y1 = y1;
	}

	public Integer getZ1() {
		return z1;
	}

	public void setZ1(Integer z1) {
		this.z1 = z1;
	}

	public Integer getX2() {
		return x2;
	}

	public void setX2(Integer x2) {
		this.x2 = x2;
	}

	public Integer getY2() {
		return y2;
	}

	public void setY2(Integer y2) {
		this.y2 = y2;
	}

	public Integer getZ2() {
		return z2;
	}

	public void setZ2(Integer z2) {
		this.z2 = z2;
	}

	public Integer getBindId() {
		return bindId;
	}

	public void setBindId(Integer bindId) {
		this.bindId = bindId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
		
		switch (status) {
		case PositionTaskItemState.WAIT:
			this.statusString = "等待中";
			break;
		case PositionTaskItemState.ASSIGN:
			this.statusString = "已发送";
			break;
		case PositionTaskItemState.ACCEPT:
			this.statusString = "已接收";
			break;
		case PositionTaskItemState.CATCH:
			this.statusString = "已取料";
			break;
		case PositionTaskItemState.FINISH:
			this.statusString = "完成";
			break;
		default:
			break;
		}
	}

	public String getStatusString() {
		return statusString;
	}

	public void setStatusString(String statusString) {
		this.statusString = statusString;
	}
	
	
	public Integer getRobotId() {
		return robotId;
	}

	public void setRobotId(Integer robotId) {
		this.robotId = robotId;
	}

	public static List<BoxPositionTaskItemVO> fillList(List<BoxPositionTaskItem> items) {
		if (items != null && !items.isEmpty()) {
			List<BoxPositionTaskItemVO> itemVOs = new ArrayList<BoxPositionTaskItemVO>(items.size());
			for (BoxPositionTaskItem item : items) {
				BoxPositionTaskItemVO itemVO = new BoxPositionTaskItemVO();
				itemVO.setId(item.getId());
				itemVO.setStatus(item.getStatus());
				itemVO.setX1(item.getX1());
				itemVO.setY1(item.getY1());
				itemVO.setZ1(item.getZ1());
				itemVO.setX2(item.getX2());
				itemVO.setY2(item.getY2());
				itemVO.setZ2(item.getZ2());
				itemVO.setBindId(item.getBindId());
				itemVO.setRobotId(item.getRobotId());
				itemVOs.add(itemVO);
			}
			return itemVOs;
		}
		return null;
	}
}
