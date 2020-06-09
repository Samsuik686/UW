package com.jimi.uw_server.controller;

import java.io.IOException;

import com.jfinal.core.Controller;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.service.PrinterService;
import com.jimi.uw_server.util.ResultUtil;
import com.jimi.uw_server.util.TokenBox;


public class PrinterController extends Controller {

	public static final String SESSION_KEY_LOGIN_USER = "loginUser";

	private static PrinterService printerService = PrinterService.me;


	@Log("截料远程打印， 打印机IP地址:{ip}，料盘时间戳：{materialId}，任务条目Id：{packingListItemId}")
	public void print(String ip, String materialId, Integer packingListItemId) {
		if (materialId == null || ip == null || packingListItemId == null) {
			throw new ParameterException("参数不能为空");
		}
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		try {
			String reuslt = printerService.print(ip, materialId, packingListItemId, user);
			renderJson(ResultUtil.succeed(reuslt));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	@Log("普通远程打印， 打印机IP地址:{ip}，料盘时间戳：{materialId}，数量：{quantity}")
	public void printSingle(String ip, String materialId, Integer quantity) {
		if (materialId == null || ip == null || quantity == null) {
			throw new ParameterException("参数不能为空");
		}
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		try {
			String reuslt = printerService.printSingle(ip, materialId, quantity, user);
			renderJson(ResultUtil.succeed(reuslt));
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
