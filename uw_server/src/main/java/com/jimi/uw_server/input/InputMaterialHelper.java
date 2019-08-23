package com.jimi.uw_server.input;

import com.jimi.InputHelper.send.InputHelper.OnInputListener;
import com.jimi.InputHelper.util.LogCatcher;
import com.jimi.InputHelper.util.PackageLogger;
import com.jimi.uw_server.agv.dao.InputMaterialRedisDAO;
import com.jimi.uw_server.model.ErrorLog;
import com.jimi.uw_server.model.PackageLog;

import java.util.Date;

import com.alibaba.fastjson.JSON;
import com.jimi.InputHelper.bo.PackagePair;
import com.jimi.InputHelper.send.MyInputHelper;


public class InputMaterialHelper {

	private static void initializationListener() {

		MyInputHelper.getInstance().setOnInputListener(new OnInputListener() {

			@Override
			public void onInput(int windowId, int positionNo) {
				int pNo = InputMaterialRedisDAO.getScanStatus(windowId);
				if (pNo != -1) {
					if (pNo == positionNo) {
						String result = MyInputHelper.getInstance().switchAlarm(windowId, false);
						if (result.equals("succeed")) {
							InputMaterialRedisDAO.setScanStatus(windowId, -1);
						} else {
							ErrorLog errorLog = new ErrorLog();
							errorLog.setMessage("Error|ALARM CLOSE FAILED| " + result);
							errorLog.setTime(new Date());
							errorLog.save();
						}
						String result1 = MyInputHelper.getInstance().switchLight(windowId, positionNo, false);
						if (!result1.equals("succeed")) {
							ErrorLog errorLog = new ErrorLog();
							errorLog.setMessage("Error|LIGHT CLOSE FAILED| " + result);
							errorLog.setTime(new Date());
							errorLog.save();
						}
					} else {
						String result = MyInputHelper.getInstance().switchAlarm(windowId, true);
						if (!result.equals("succeed")) {
							ErrorLog errorLog = new ErrorLog();
							errorLog.setMessage("Error|ALARM OPERN FAILED| " + result);
							errorLog.setTime(new Date());
							errorLog.save();
						}
					}
				}
			}
		});
		System.out.println("投料辅助器：位置监听器初始化成功");
	}


	private static void initializationLogger() {

		PackageLogger.getInstance().setLogCatcher(new LogCatcher() {

			@Override
			public void onResponseFinish(PackagePair info) {
				PackageLog log = createLog(info);
				log.setFromId(info.getAddress());
				log.setToId("uw");
				log.setConsumeTime((int) (new Date().getTime() - info.getRequestTime().getTime()));
				log.save();
			}


			@Override
			public void onRequestFinish(PackagePair info) {
				PackageLog log = createLog(info);
				log.setConsumeTime((int) (new Date().getTime() - log.getTime().getTime()));
				log.setFromId("uw");
				log.setToId(info.getAddress());
				log.save();
			}
		});
		System.out.println("投料辅助器，日志记录器初始化成功");
	}


	public static void startInputHepler(int port) {
		initializationListener();
		initializationLogger();
		MyInputHelper.getInstance().start(port);

	}


	private static PackageLog createLog(PackagePair packagePair) {
		PackageLog log = new PackageLog();
		log.setTime(packagePair.getRequestTime());
		log.setPackageType(packagePair.getType());
		try {
			log.setPackageRequest(JSON.toJSONString(packagePair.getRequestPackage()));
		} catch (NullPointerException e) {
		}
		try {
			log.setPackageResponse(JSON.toJSONString(packagePair.getResponsePackage()));
		} catch (NullPointerException e) {
		}
		return log;
	}
}
