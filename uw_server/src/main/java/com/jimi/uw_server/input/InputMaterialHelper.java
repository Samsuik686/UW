package com.jimi.uw_server.input;

import com.jimi.InputHelper.send.InputHelper.OnInputListener;
import com.jimi.uw_server.agv.dao.InputMaterialRedisDAO;
import com.jimi.uw_server.model.ErrorLog;
import com.jimi.uw_server.model.InputHelpSocketLog;

import java.util.Date;
import com.jimi.InputHelper.log.Logger;
import com.jimi.InputHelper.log.SocketLogger;
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

		SocketLogger.getInstance().setLogger(new Logger() {

			@Override
			public void onSend(String pack) {

				InputHelpSocketLog log = new InputHelpSocketLog();
				log.setIsSend(true).setPack(pack).setTime(new Date()).save();

			}


			@Override
			public void onReceive(String pack) {
				// TODO Auto-generated method stub
				InputHelpSocketLog log = new InputHelpSocketLog();
				log.setIsSend(false).setPack(pack).setTime(new Date()).save();
			}


			@Override
			public void OnDebug(String pack, boolean isSend) {
				InputHelpSocketLog log = new InputHelpSocketLog();
				log.setIsSend(isSend).setPack(pack).setTime(new Date()).save();
			}
		});
		System.out.println("投料辅助器，日志记录器初始化成功");
	}


	public static void startInputHepler(int port) {
		initializationListener();
		initializationLogger();
		MyInputHelper.getInstance().start(port);

	}
}
