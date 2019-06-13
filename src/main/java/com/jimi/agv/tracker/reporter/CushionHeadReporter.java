package com.jimi.agv.tracker.reporter;

import java.util.Date;

import com.jimi.agv.tracker.entity.model.CushionHead;
import com.jimi.agv.tracker.task.AGVIOTask;
import com.jimi.agv.tracker.task.AGVIOTaskItem;
import com.jimi.agv.tracker.task.CushionAGVIOTask;

import cc.darhao.dautils.api.DateUtil;

public class CushionHeadReporter implements Reporter{

	private CushionAGVIOTask task;
	
	
	public CushionHeadReporter(AGVIOTask task) {
		this.task = (CushionAGVIOTask) task;
	}


	@Override
	public String getReport() {
		StringBuffer sb = new StringBuffer();
		sb.append("任务模式：缓冲模式");
		sb.append("报告时间：" + DateUtil.yyyyMMddHHmmss(new Date()) + "\n");
		for (AGVIOTaskItem item : task.getItems()) {
			sb.append(item.getReporter().getReport());
			sb.append("\n");
		}
		return sb.toString();
	}

	
	@Override
	public void saveToDb() {
		CushionHead cushionHead = new CushionHead();
		cushionHead.setTime(new Date());
		cushionHead.save();
		for (AGVIOTaskItem item : task.getItems()) {
			CushionBodyReporter reporter = (CushionBodyReporter) item.getReporter();
			reporter.setHeadId(cushionHead.getId());
			reporter.saveToDb();
		}
	}


}
