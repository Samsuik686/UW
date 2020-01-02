package com.jimi.uw_server.ur.exception;

/**
 * 
 * @author HardyYao
 * @createTime 2019年4月25日  下午3:50:51
 */

public class ReceiveTimeOutException extends RuntimeException {

	private static final long serialVersionUID = 1L;


	public ReceiveTimeOutException(String message) {
		super(message);
	}

}
