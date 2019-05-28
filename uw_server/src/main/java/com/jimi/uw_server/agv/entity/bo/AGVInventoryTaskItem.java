package com.jimi.uw_server.agv.entity.bo;

import java.io.Serializable;

/**
 * 
 * @author trjie
 * @createTime 2019年5月16日  上午9:38:15
 */
@SuppressWarnings("serial")
public class AGVInventoryTaskItem  implements Serializable  {

		private Integer taskId;

		private Integer robotId;

		private Integer boxId;

		/**
		 * -1：不可分配  0：未分配  1：已分配拣料  2：已拣料到站  3：已分配回库  4：已回库完成
		 */
		private Integer state;

		/**
		 * false：出入库数量尚未满足实际需求	true：出入库数量已满足实际需求
		 */
		private Boolean isForceFinish;

		/**
		 * 任务优先级，取值范围：1-9；数值越大，优先级越高
		 */
		private Integer priority;

		private Integer windowId;

		public AGVInventoryTaskItem() {}

		public AGVInventoryTaskItem(Integer taskId, Integer boxId, Integer state, Integer priority, Integer windowId) {
			this.taskId = taskId;
			this.robotId = 0;
			this.state = state;
			this.boxId = boxId;
			this.isForceFinish = false;
			this.priority = priority;
			this.windowId = windowId;
		}

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

		public Boolean getIsForceFinish() {
			return isForceFinish;
		}

		public void setIsForceFinish(Boolean isForceFinish) {
			this.isForceFinish = isForceFinish;
		}

		public Integer getPriority() {
			return priority;
		}

		public void setPriority(Integer priority) {
			this.priority = priority;
		}

		public String getGroupId() {
			return boxId + "@" + taskId;
		}

		public Integer getWindowId() {
			return windowId;
		}

		public void setWindowId(Integer windowId) {
			this.windowId = windowId;
		}
		
		
}
