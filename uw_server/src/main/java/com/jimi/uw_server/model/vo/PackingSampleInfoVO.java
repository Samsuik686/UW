package com.jimi.uw_server.model.vo;

import java.util.List;

import com.jimi.uw_server.model.GoodsLocation;


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

	private String goodsLocationName;

	private Integer goodsLocationId;

	private Integer scanNum;

	private Integer outNum;

	private Integer totalNum;

	private List<MaterialDetialsVO> list;


	public PackingSampleInfoVO() {

	}


	public PackingSampleInfoVO(GoodsLocation goodsLocation) {
		this.setGoodsLocationId(goodsLocation.getId());
		this.setGoodsLocationName(goodsLocation.getName());
		this.setWindowId(goodsLocation.getWindowId());
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


	public Integer getWindowId() {
		return windowId;
	}


	public void setWindowId(Integer windowId) {
		this.windowId = windowId;
	}


	public List<MaterialDetialsVO> getList() {
		return list;
	}


	public void setList(List<MaterialDetialsVO> list) {
		this.list = list;
	}


	public String getGroupId() {
		return groupId;
	}


	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}


	public String getGoodsLocationName() {
		return goodsLocationName;
	}


	public void setGoodsLocationName(String goodsLocationName) {
		this.goodsLocationName = goodsLocationName;
	}


	public Integer getGoodsLocationId() {
		return goodsLocationId;
	}


	public void setGoodsLocationId(Integer goodsLocationId) {
		this.goodsLocationId = goodsLocationId;
	}


	public Integer getScanNum() {
		return scanNum;
	}


	public void setScanNum(Integer scanNum) {
		this.scanNum = scanNum;
	}


	public Integer getOutNum() {
		return outNum;
	}


	public void setOutNum(Integer outNum) {
		this.outNum = outNum;
	}


	public Integer getTotalNum() {
		return totalNum;
	}


	public void setTotalNum(Integer totalNum) {
		this.totalNum = totalNum;
	}

}
