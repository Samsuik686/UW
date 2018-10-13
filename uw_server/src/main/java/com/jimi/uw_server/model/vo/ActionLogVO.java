package com.jimi.uw_server.model.vo;

import java.util.Date;

import com.jimi.uw_server.model.ActionLog;

/**
 * 
 * @author HardyYao
 * @createTime 2018年10月13日  上午10:01:54
 */

public class ActionLogVO extends ActionLog {

	private static final long serialVersionUID = -7728273290422993327L;

	private String resultString;


	public String getResultString(Integer resultCode) {
		switch (resultCode) {
		case 200:
			this.resultString = "操作成功";
			break;
		case 401:
			this.resultString = "权限不足";	
			break;
		case 400:
			this.resultString = "参数错误";
			break;
		case 412:
			this.resultString = "操作错误";
			break;
		default:
			this.resultString = "服务器异常";
			break;
		}
		return resultString;
	}


	public ActionLogVO(Integer id, String ip, String uid, String action, Date time, Integer resultCode) {
		this.setId(id);
		this.setIp(ip);
		this.setUid(uid);
		this.setAction(action);
		this.setTime(time);
		this.set("resultCode", resultCode);
		this.set("resultString", getResultString(resultCode));
	}

}
