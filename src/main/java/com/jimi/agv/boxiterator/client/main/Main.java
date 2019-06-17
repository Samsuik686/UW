package com.jimi.agv.boxiterator.client.main;

import java.io.File;
import java.net.URI;
import java.util.Scanner;

import javax.websocket.ContainerProvider;

import com.jimi.agv.boxiterator.client.constant.Constant;
import com.jimi.agv.boxiterator.client.constant.PackageType;
import com.jimi.agv.boxiterator.client.handler.StatusHandler;
import com.jimi.agv.boxiterator.client.sender.RequestSender;
import com.jimi.agv.boxiterator.client.socket.BoxIteratorProxySocket;
import com.jimi.agv.boxiterator.client.task.BoxIterateTask;
import com.jimi.agv.boxiterator.client.task.BoxIterateTaskItem;
import com.jimi.agv.boxiterator.client.util.PropUtil;

import cc.darhao.pasta.Pasta;

public class Main {
	
	private static BoxIterateTask task;
	
	private static Scanner scanner = new Scanner(System.in);
	
	
	public static void main(String[] args) throws Exception {
		initPastaConfig();
		showMe();
		askForInitTask();
		startTask();
		waitForTaskFinish();
		showEnd();
	}


	private static void initPastaConfig() throws Exception {
		//连接ws
		URI path = new URI(PropUtil.getString(Constant.CONFIG_NAME, Constant.PROXY_WS_STRING));
		ContainerProvider.getWebSocketContainer().connectToServer(BoxIteratorProxySocket.class, path);
		//配置路由器
		Pasta.bindRoute(PackageType.STATUS_0, StatusHandler.class);
		Pasta.bindRoute(PackageType.STATUS_1, StatusHandler.class);
		Pasta.bindRoute(PackageType.STATUS_2, StatusHandler.class);
	}


	private static void showMe() {
		System.out.println("==================================");
		System.out.println("欢迎使用 - Box Iterator 1.0.0 - 料盒迭代器 by Darhao");
		System.out.println("==================================");
	}

	
	private static void askForInitTask() throws Exception {
		try {
			int x = 0, y = 0 , robotId = 0;
			System.out.print("请输入仓口坐标X：");
			x = scanner.nextInt();
			System.out.print("请输入仓口坐标Y：");
			y = scanner.nextInt();
			System.out.print("请输入执行叉车ID：");
			robotId = scanner.nextInt();
			System.out.println("请输入任务单文件路径和文件名，按下回车开始执行任务（支持相对路径）：");
			task = new BoxIterateTask(getExistFilePath(), x, y, robotId);
		} catch (Exception e) {
			System.err.println("错误：" + e.getMessage());
			scanner.next();
			throw e;
		}
	}


	private static void startTask() throws Exception {
		final int SUCCEED = 0;
		final int ROBOTID_ALREADY_EXIST = 1;
		int result = RequestSender.sendLogin(task.getRobotId(), task.getWindowX(), task.getWindowY());
		if(result == SUCCEED) {
			throw new IllegalArgumentException("此robotId已在另一终端登录，请更换另一个robotId");
		}else if(result == ROBOTID_ALREADY_EXIST){
			executeNextItem();
		}
	}
	
	
	public static void executeNextItem() {
		BoxIterateTaskItem item = task.getNextNotStartItem();
		if(item == null) {
			notifyForTaskFinish();
		}else {
			new Thread(()->{ //避免互等ack死锁
				RequestSender.sendGet(item.getKey(), item.getTargetX(), item.getTargetY(), item.getTargetZ());
			}).start();
		}
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


	private static void waitForTaskFinish() throws InterruptedException {
		synchronized (task) {
			task.wait();
		}
	}
	

	private static void showEnd() {
		System.out.println("执行完毕，请关闭程序");
		scanner.next();
	}


	public static void notifyForTaskFinish() {
		synchronized (task) {
			task.notify();
		}
	}


	public static BoxIterateTask getTask() {
		return task;
	}


	public static Scanner getScanner() {
		return scanner;
	}
	

}
