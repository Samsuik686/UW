package com.jimi.uw_server.controller;

import java.io.IOException;

import com.jfinal.core.Controller;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.service.PrinterService;
import com.jimi.uw_server.util.ResultUtil;
import com.jimi.uw_server.util.TokenBox;


public class PrinterController extends Controller {

	public static final String SESSION_KEY_LOGIN_USER = "loginUser";

	private static PrinterService printerService = PrinterService.me;


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
