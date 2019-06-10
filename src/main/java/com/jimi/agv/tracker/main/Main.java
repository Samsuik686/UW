package com.jimi.agv.tracker.main;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import com.jimi.agv.tracker.constant.Constant;
import com.jimi.agv.tracker.entity.bo.AGVIOTask;
import com.jimi.agv.tracker.handler.SwitchHandler;
import com.jimi.agv.tracker.socket.AGVMainSocket;
import com.jimi.agv.tracker.util.PropUtil;

import cc.darhao.dautils.api.TextFileUtil;

public class Main {
	
	private static AGVIOTask task;
	
	private static Scanner scanner = new Scanner(System.in);
	
	public static void main(String[] args) throws Exception {
		connectServer();
		while(true) {
			showMe();
			askForStartTask();
			waitForTaskFinish();
			askForSaveReport();
		}
	}


	private static void connectServer() throws Exception {
		AGVMainSocket.init(PropUtil.getString(Constant.CONFIG_NAME, Constant.MAIN_WS_STRING));
	}


	private static void showMe() {
		System.out.println("==================================");
		System.out.println("欢迎使用 - AGV Tracker 1.2.0 - 轨迹采集器 by Darhao");
		System.out.println("1.1.0更新日志：1.报告输出仓口坐标 2.去掉\"秒\"");
		System.out.println("1.2.0更新日志：1.现在可以输出 [轨迹报告] 了");
		System.out.println("==================================");
	}

	
	private static void askForStartTask() throws Exception {
		System.out.println("请输入任务单文件路径和文件名，按下回车开始执行任务（支持相对路径）：");
		task = new AGVIOTask(getExistFilePath());
		task.executeNextItem();
		SwitchHandler.sendAllStart();
	}


	private static String getExistFilePath() {
		String filePath = null;
		while(true) {
			filePath = scanner.nextLine();
			if(!new File(filePath).exists()) {
				System.err.println("错误：找不到文件，请重新输入：");
			}else {
				break;
			}
		}
		return filePath;
	}


	private static void askForSaveReport() throws IOException {
		System.out.println("请输入 [耗时报告] 文件导出的路径和文件名，按下回车开始生成报告（支持相对路径）：");
		TextFileUtil.writeToFile(getNotExistFilePath(), task.getConsumeReport());
		System.out.println("已生成完毕");
	}
	
	
	private static String getNotExistFilePath() {
		String filePath = null;
		while(true) {
			filePath = scanner.nextLine();
			if(new File(filePath).exists()) {
				System.err.println("警告：文件已存在，确认覆盖吗？（Y/N）");
				if(scanner.nextLine().equalsIgnoreCase("Y")){
					break;
				}else {
					System.out.println("请重新输入新路径和文件名：");
				}
			}else {
				break;
			}
		}
		return filePath;
	}



	private static void waitForTaskFinish() throws InterruptedException {
		synchronized (task) {
			task.wait();
		}
	}


	public static AGVIOTask getTask() {
		return task;
	}

}
