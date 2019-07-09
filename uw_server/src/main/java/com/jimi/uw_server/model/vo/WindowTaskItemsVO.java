package com.jimi.uw_server.model.vo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.TaskType;

/**
 * 仓口任务条目表示层
 * @author HardyYao
 * @createTime 2018年7月23日 下午4:04:25
 */
public class WindowTaskItemsVO{

	private Integer id;
	
	private String fileName;
	
	private Integer type;
	
	private String materialNo;
	
	private Integer planQuantity;
	
	private Integer actualQuantity;
	
	private String finishTime;
	
	private Integer state;

	private String typeString;

	private String stateString;

	private Integer boxId;
	
	private Integer goodsLocationId;
	
	private String goodsLocationName;
	
	private Integer robotId;
	
	private List<?> details;
	
	public WindowTaskItemsVO(Integer packingListItemId, String fileName, Integer type, String materialNo, Integer planQuantity, Integer actualQuantity, Date finishTime, Integer state, Integer boxId, Integer goodsLocationId, String goodsLocaitonName, Integer robotId) {
		this.setId(packingListItemId);
		this.setFileName(fileName);
		this.setType(type);
		this.setTypeString(type);
		this.setMaterialNo(materialNo);
		this.setPlanQuantity(planQuantity);
		this.setActualQuantity(actualQuantity);
		if (finishTime == null) {
			this.setFinishTime("no");
		} else {
			SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			this.setFinishTime(sFormat.format(finishTime));
		}
		this.setState(state);
		this.setStateString(state);
		this.setBoxId(boxId);
		this.setGoodsLocationId(goodsLocationId);
		this.setGoodsLocationName(goodsLocaitonName);
		this.setRobotId(robotId);
	}


	


	public void setStateString(Integer state) {
		switch (state) {
		case TaskItemState.LACK:
			this.stateString = "缺料";
			break;
		case TaskItemState.FINISH_CUT:
			this.stateString = "等待截料返库";
			break;
		case TaskItemState.WAIT_SCAN:
			this.stateString = "等待扫码";
			break;
		case TaskItemState.WAIT_ASSIGN:
			this.stateString = "未分配给叉车";	
			break;
		case TaskItemState.ASSIGNED:
			this.stateString = "已分配拣料";
			break;
		case TaskItemState.SEND_BOX:
			this.stateString = "运送料盒（取）";
			break;
		case TaskItemState.ARRIVED_WINDOW:
			this.stateString = "已拣料到站";
			break;
		case TaskItemState.START_BACK:
			this.stateString = "已分配回库";
			break;
		case TaskItemState.BACK_BOX:
			this.stateString = "运送料盒（回）";
			break;
		case TaskItemState.FINISH_BACK:
			this.stateString = "已回库完成";
			break;
		default:
			this.stateString = "异常状态";
			break;
		}
	}


	public String getTypeString() {
		return typeString;
	}


	public void setTypeString(Integer type) {
		switch (type) {
		case TaskType.IN:
			this.typeString = "入库";
			break;
		case TaskType.OUT:
			this.typeString = "出库";
			break;
		case TaskType.COUNT:
			this.typeString = "盘点";
			break;
		case TaskType.POSITION_OPTIZATION:
			this.typeString = "位置优化";
			break;
		case TaskType.SEND_BACK:
			this.typeString = "调拨入库";
			break;
		default:
			this.typeString = "错误类型";
			break;
		}
	}


	
	public Integer getBoxId() {
		return boxId;
	}


	
	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}


	
	public Integer getGoodsLocationId() {
		return goodsLocationId;
	}


	
	public void setGoodsLocationId(Integer goodsLocationId) {
		this.goodsLocationId = goodsLocationId;
	}


	
	public String getGoodsLocationName() {
		return goodsLocationName;
	}


	
	public void setGoodsLocationName(String goodsLocationName) {
		this.goodsLocationName = goodsLocationName;
	}


	
	public void setTypeString(String typeString) {
		this.typeString = typeString;
	}


	
	public void setStateString(String stateString) {
		this.stateString = stateString;
	}

	
	public void setId(Integer id) {
		this.id = id;
	}
	
	
	public Integer getId() {
		return id;
	}

	
	public String getFileName() {
		return fileName;
	}
	
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public Integer getType() {
		return type;
	}


	public void setType(Integer type) {
		this.type = type;
	}

	
	public String getMaterialNo() {
		return materialNo;
	}


	public void setMaterialNo(String materialNo) {
		this.materialNo = materialNo;
	}


	public Integer getPlanQuantity() {
		return planQuantity;
	}


	public void setPlanQuantity(Integer planQuantity) {
		this.planQuantity = planQuantity;
	}


	public Integer getActualQuantity() {
		return actualQuantity;
	}


	public void setActualQuantity(Integer actualQuantity) {
		this.actualQuantity = actualQuantity;
	}


	public String getFinishTime() {
		return finishTime;
	}


	public void setFinishTime(String finishTime) {
		this.finishTime = finishTime;
	}


	public Integer getState() {
		return state;
	}


	public void setState(Integer state) {
		this.state = state;
	}

	
	public List<?> getDetails() {
		return details;
	}


	public void setDetails(List<?> details) {
		this.details = details;
	}


	public String getStateString() {
		return stateString;
	}

	
	public Integer getRobotId() {
		return robotId;
	}


	public void setRobotId(Integer robotId) {
		this.robotId = robotId;
	}
	

}
