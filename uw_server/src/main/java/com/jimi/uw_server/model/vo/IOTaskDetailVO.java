package com.jimi.uw_server.model.vo;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.Null;

import com.jimi.uw_server.model.TaskLog;

/** 
 * 出入库任务详情表示层对象
 * @author HardyYao
 * @createTime 2018年7月13日 上午8:29:22
 */
@SuppressWarnings("serial")
public class IOTaskDetailVO extends TaskLog {

	private List<?> details;
	
	private Integer uwStoreNum;
	
	private Integer whStoreNum;
	
	private String status;

	public IOTaskDetailVO(Integer packingListItemId, String materialNo, Integer planQuantity, Integer actualQuantity, Integer deductQuantity,Date finishTime) {
		this.set("id", packingListItemId);
		this.set("materialNo", materialNo);
		this.set("planQuantity", planQuantity);
		this.set("actualQuantity", actualQuantity);
		if (finishTime == null) {
			this.set("finishTime", "no");
		} else {
			this.set("finishTime", finishTime);
		}
		this.set("deductQuantity", deductQuantity);
		if (deductQuantity != 0) {
			int lackNum = planQuantity - actualQuantity + deductQuantity;
			if (lackNum < 0) {
				this.set("itemStatus", "欠料");
				this.set("lackNum", (0-lackNum));
			}
		} else {
			this.set("itemStatus", "正常");
			this.set("lackNum", null);
		}
	}

	public List<?> getDetails() {
		return details;
	}

	public void setDetails(List<?> details) {
		this.set("details", details);
	}

	public Integer getUwStoreNum() {
		return uwStoreNum;
	}

	public void setUwStoreNum(Integer uwStoreNum) {
		this.set("uwStoreNum", uwStoreNum);
	}

	public Integer getWhStoreNum() {
		return whStoreNum;
	}

	public void setWhStoreNum(Integer whStoreNum) {
		this.set("whStoreNum", whStoreNum);
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(Integer uwStoreNum, Integer whStoreNum, Integer planQuantity ) {
		
		if ((planQuantity - uwStoreNum - whStoreNum) > 0) {
			this.set("status", "缺料");
		}else if ((planQuantity - uwStoreNum - whStoreNum) < 0 && whStoreNum > 0) {
			this.set("status", "超发");
		}else if ((planQuantity - uwStoreNum - whStoreNum) < 0 && whStoreNum < 0) {
			this.set("status", "欠料");
		}else {
			this.set("status", "正常");
		}
		
	}


}
