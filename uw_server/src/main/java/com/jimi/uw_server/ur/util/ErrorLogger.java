package com.jimi.uw_server.ur.util;

import java.util.Date;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.jimi.uw_server.model.ErrorLog;

public class ErrorLogger {
	
	private static Logger logger = LogManager.getRootLogger();

	public static void saveErrorToDb(Throwable e) {
		ErrorLog errorLog = new ErrorLog();
		errorLog.setTime(new Date());
		errorLog.setMessage(e.getClass().getSimpleName() + ":" + e.getMessage());
		errorLog.save();
		logger.error(e.getClass().getSimpleName() + ":" + e.getMessage());
	}
	
}
