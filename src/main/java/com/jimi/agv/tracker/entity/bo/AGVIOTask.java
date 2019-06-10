package com.jimi.agv.tracker.entity.bo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jimi.agv.tracker.constant.Constant;
import com.jimi.agv.tracker.handler.IOHandler;
import com.jimi.agv.tracker.util.PropUtil;

import cc.darhao.dautils.api.DateUtil;

public class AGVIOTask {

	private List<AGVIOTaskItem> items = new ArrayList<>();
	
	public AGVIOTask(String taskFilePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(new File(taskFilePath)));
		String item = null;
		while((item = reader.readLine()) != null) {
			String[] position = item.split(",");
			items.add(new AGVIOTaskItem(Integer.parseInt(position[0]), Integer.parseInt(position[1]), Integer.parseInt(position[2])));
		}
		reader.close();
	}
	
	
	public boolean isFinish() {
		for (AGVIOTaskItem item : items) {
			if(item.getState() != AGVIOTaskItem.FINISHED) {
				return false;
			}
		}
		return true;
	}
	
	
	public void executeNextItem() throws Exception {
		for (AGVIOTaskItem item : items) {
			if(item.getState() == AGVIOTaskItem.NOT_START) {
				showProcess(item);
				IOHandler.sendLS(item);
				return;
			}
		}
		showEnd();
	}


	private void showEnd() {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("日志时间：" + DateUtil.HHmmss(new Date()));
		System.out.println("任务总进度：" + items.size() + "/" + items.size());
		System.out.println("所有任务条目即将完成");
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}


	public AGVIOTaskItem getItemByKey(String key) {
		for (AGVIOTaskItem item : items) {
			if(item.getKey().equals(key)) {
				return item;
			}
		}
		return null;
	}

	private void showProcess(AGVIOTaskItem item) {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("日志时间：" + DateUtil.HHmmss(new Date()));
		System.out.println("任务总进度：" + items.indexOf(item) + "/" + items.size());
		System.out.println("即将执行任务条目：" + item.getPosistionString());
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}


	public String getConsumeReport() {
		System.out.println("正在生成 [耗时报告] 请稍等...");
		StringBuffer sb = new StringBuffer();
		sb.append("报告时间：" + DateUtil.yyyyMMddHHmmss(new Date()) + "\n");
		int windowX = PropUtil.getInt(Constant.CONFIG_NAME, Constant.WINDOW_X_STRING);
		int windowY = PropUtil.getInt(Constant.CONFIG_NAME, Constant.WINDOW_Y_STRING);
		sb.append("仓口坐标：[" + windowX + "," + windowY + "]\n");
		for (AGVIOTaskItem item : items) {
			sb.append(item.getConsumeReport());
			sb.append("\n");
		}
		return sb.toString();
	}
	
}
