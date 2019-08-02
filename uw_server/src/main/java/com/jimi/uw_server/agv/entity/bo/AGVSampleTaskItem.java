package com.jimi.uw_server.agv.entity.bo;

import java.io.Serializable;

import com.jimi.uw_server.agv.entity.bo.base.BaseTaskItem;


/**
 * AGV出入库任务条目 <br>
 * <b>2018年6月15日</b>
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
@SuppressWarnings("serial")
public class AGVSampleTaskItem extends BaseTaskItem implements Serializable {

	public AGVSampleTaskItem() {
	}


	public AGVSampleTaskItem(Integer taskId, Integer boxId) {
		this.robotId = 0;
		this.state = 0;
		this.boxId = boxId;
		this.windowId = 0;
		this.taskId = taskId;
		this.isForceFinish = false;
		this.goodsLocationId = 0;
	}


	public String getGroupId() {
		return boxId + "#" + taskId;
	}

}
