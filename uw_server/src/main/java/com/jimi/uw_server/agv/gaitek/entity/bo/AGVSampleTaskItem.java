package com.jimi.uw_server.agv.gaitek.entity.bo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jimi.uw_server.agv.gaitek.entity.bo.base.BaseTaskItem;


/**
 * AGV出入库任务条目 <br>
 * <b>2018年6月15日</b>
 * 
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */

public class AGVSampleTaskItem extends BaseTaskItem {

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


	@JsonIgnore
	public String getGroupId() {
		return boxId + "#" + taskId;
	}

}
