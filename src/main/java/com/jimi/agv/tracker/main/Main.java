package com.jimi.agv.tracker.main;

import java.io.File;
import java.io.IOException;
import java.util.InputMismatchException;
import java.util.Scanner;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;
import com.jimi.agv.tracker.constant.Constant;
import com.jimi.agv.tracker.entity.model.MysqlMappingKit;
import com.jimi.agv.tracker.handler.SwitchHandler;
import com.jimi.agv.tracker.socket.AGVMainSocket;
import com.jimi.agv.tracker.socket.RobotInfoSocket;
import com.jimi.agv.tracker.task.AGVIOTask;
import com.jimi.agv.tracker.task.CushionAGVIOTask;
import com.jimi.agv.tracker.task.TraditionAGVIOTask;
import com.jimi.agv.tracker.util.PropUtil;

import cc.darhao.dautils.api.TextFileUtil;

public class Main {
	
	private static AGVIOTask task;
	
	private static Scanner scanner = new Scanner(System.in);
	
	
	public static void main(String[] args) throws Exception {
		connectServer();
		initDatabasePool();
		while(true) {
			try {
				Thread.sleep(500);
				showMe();
				askForInitTask();
				startTask();
				waitForTaskFinish();
				callReporter();
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("检测到异常：系统已重启");
			}
			clearTask();
		}
	}


	private static void initDatabasePool() {
		String url = PropUtil.getString(Constant.CONFIG_NAME, Constant.DB_URL);
		String user = PropUtil.getString(Constant.CONFIG_NAME, Constant.DB_USER);
		String password = PropUtil.getString(Constant.CONFIG_NAME, Constant.DB_PASSWORD);
		DruidPlugin dp = new DruidPlugin(url, user, password);
		dp.start();
		ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
	    arp.setDialect(new MysqlDialect());
	    MysqlMappingKit.mapping(arp);
	    arp.start();
	}


	private static void connectServer() throws Exception {
		AGVMainSocket.init(PropUtil.getString(Constant.CONFIG_NAME, Constant.MAIN_WS_STRING));
		RobotInfoSocket.init(PropUtil.getString(Constant.CONFIG_NAME, Constant.ROBOT_WS_STRING));
	}


	private static void showMe() {
		System.out.println("==================================");
		System.out.println("欢迎使用 - AGV Tracker 1.3.0 - 轨迹采集器 by Darhao");
		System.out.println("1.1.0更新日志：1.报告输出仓口坐标 2.去掉\"秒\"");
		System.out.println("1.2.0更新日志：1.现在可以输出 [轨迹报告] 了");
		System.out.println("1.3.0更新日志：1.增加缓冲模式 2.报告支持输出到数据库了哦");
		System.out.println("==================================");
	}

	
	private static void askForInitTask() throws Exception {
		System.out.println("请输入方括号内的编号以选择任务模式：[1]传统模式 [*]缓冲模式");
		if(scanner.next().equals("1")) {
			int x = 0, y = 0;
			try {
				System.out.println("请输入仓口坐标X：");
				x = scanner.nextInt();
				System.out.println("请输入仓口坐标Y：");
				y = scanner.nextInt();
			} catch (InputMismatchException e) {
				System.err.println("错误：无法识别整数");
				scanner.next();
				throw e;
			}
			System.out.println("请输入 [传统模式] 任务单文件路径和文件名，按下回车开始执行任务（支持相对路径）：");
			try {
				task = new TraditionAGVIOTask(getExistFilePath(), x, y);
			} catch (Exception e) {
				System.err.println("错误：解析任务失败，请检查格式");
				throw e;
			}
		}else {
			System.out.println("请输入 [缓冲模式] 任务单文件路径和文件名，按下回车开始执行任务（支持相对路径）：");
			try {
				task = new CushionAGVIOTask(getExistFilePath());
			} catch (Exception e) {
				System.err.println("错误：解析任务失败，请检查格式");
				throw e;
			}
		}
	}


	private static void startTask() throws Exception {
		task.executeNextItem();
		SwitchHandler.sendAllStart();
	}


	private static String getExistFilePath() {
		String filePath = null;
		while(true) {
			filePath = scanner.next();//使用next不使用nextline，否则会把上一次输入的\n算作有效输入而导致方法返回，参考：https://www.cnblogs.com/1020182600HENG/p/6564795.html
			if(!new File(filePath).exists()) {
				System.err.println("错误：找不到文件，请重新输入：");
			}else {
				break;
			}
		}
		return filePath;
	}


	private static void callReporter() throws IOException {
		if(PropUtil.getBoolean(Constant.CONFIG_NAME, Constant.OUT_TO_DB)) {
			System.out.println("正在将报告数据写入数据库，请稍候...");
			task.getReporter().saveToDb();
			System.out.println("###数据写入完毕###");
		}else {
			System.out.println("请输入报告文件导出的路径和文件名，按下回车开始生成报告（支持相对路径）：");
			String path = getNotExistFilePath();
			System.out.println("正在生成报告文件，请稍候...");
			TextFileUtil.writeToFile(path, task.getReporter().getReport());
			System.out.println("###报告文件生成完毕###");
		}
	}


	private static String getNotExistFilePath() {
		String filePath = null;
		while(true) {
			filePath = scanner.next();
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


	private static void clearTask() {
		task = null;
	}
	
	
	private static void waitForTaskFinish() throws InterruptedException {
		synchronized (task) {
			task.wait();
		}
	}
	
	
	public static void notifyForTaskFinish() {
		synchronized (task) {
			task.notify();
		}
	}


	public static AGVIOTask getTask() {
		return task;
	}

}
