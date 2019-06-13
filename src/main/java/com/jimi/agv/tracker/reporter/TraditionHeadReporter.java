package com.jimi.agv.tracker.reporter;

import java.util.Date;

import com.jimi.agv.tracker.entity.model.TraditionHead;
import com.jimi.agv.tracker.task.AGVIOTask;
import com.jimi.agv.tracker.task.AGVIOTaskItem;
import com.jimi.agv.tracker.task.TraditionAGVIOTask;

import cc.darhao.dautils.api.DateUtil;

public class TraditionHeadReporter implements Reporter{

	private TraditionAGVIOTask task;
	
	
	public TraditionHeadReporter(AGVIOTask task) {
		this.task = (TraditionAGVIOTask) task;
	}


	@Override
	public String getReport() {
		StringBuffer sb = new StringBuffer();
		sb.append("任务模式：传统模式");
		sb.append("报告时间：" + DateUtil.yyyyMMddHHmmss(new Date()) + "\n");
		sb.append("仓口坐标：[" + task.getWindowX() + "," + task.getWindowY() + "]\n");
		for (AGVIOTaskItem item : task.getItems()) {
			sb.append(item.getReporter().getReport());
			sb.append("\n");
		}
		return sb.toString();
	}

	
	@Override
	public void saveToDb() {
		TraditionHead traditionHead = new TraditionHead();
		traditionHead.setTime(new Date());
		traditionHead.setWindow(task.getWindowX() + "," + task.getWindowY());
		traditionHead.save();
		for (AGVIOTaskItem item : task.getItems()) {
			TraditionBodyReporter reporter = (TraditionBodyReporter) item.getReporter();
			reporter.setHeadId(traditionHead.getId());
			reporter.saveToDb();
		}
	}

}
