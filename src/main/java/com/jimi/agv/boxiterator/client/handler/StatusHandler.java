package com.jimi.agv.boxiterator.client.handler;

import java.util.Date;

import com.jimi.agv.boxiterator.client.main.Main;
import com.jimi.agv.boxiterator.client.sender.RequestSender;
import com.jimi.agv.boxiterator.client.task.BoxIterateTask;
import com.jimi.agv.boxiterator.client.task.BoxIterateTaskItem;

import cc.darhao.dautils.api.DateUtil;

/**
 * 状态处理器
 * <br>
 * <b>2019年6月15日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class StatusHandler {
	
	public void status0(String key) throws Exception {
		BoxIterateTaskItem item = Main.getTask().getItemByKey(key);
		if(item.getState() == BoxIterateTaskItem.NOT_START) {
			item.start();
		}else if(item.getState() == BoxIterateTaskItem.ARRIVED) {
			item.reTurn();
		}
	}
	

	public void status1(String key) {
		BoxIterateTaskItem item = Main.getTask().getItemByKey(key);
		if(item.getState() == BoxIterateTaskItem.STARTED) {
			item.got();
		}
	}
	

	public void status2(String key) throws Exception {
		BoxIterateTaskItem item = Main.getTask().getItemByKey(key);
		if(item.getState() == BoxIterateTaskItem.GOTTEN) {
			item.arrive();
			new Thread(()->{
				while(true) {
					System.out.print("输入\"R\"，令叉车回库：");
					if(Main.getScanner().next().equalsIgnoreCase("R")) {
						break;
					}
				}
				RequestSender.sendReturn(item.getKey(), item.getTargetX(), item.getTargetY(), item.getTargetZ());
			}).start();
		}else if(item.getState() == BoxIterateTaskItem.RETURNING) {
			Main.getTask().getItemByKey(key).finish();
			showProcess(Main.getTask());
			Main.executeNextItem();
		}
	}
	
	
	/**
	 * 显示任务完成总进度
	 */
	private void showProcess(BoxIterateTask task) {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("日志时间：" + DateUtil.HHmmss(new Date()));
		System.out.println("迭代进度：" + task.countFinishItem() + "/" + task.getItems().size());
		System.out.println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

}
