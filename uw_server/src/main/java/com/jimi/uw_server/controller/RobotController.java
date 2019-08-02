package com.jimi.uw_server.controller;

import com.alibaba.fastjson.JSON;
import com.jfinal.aop.Enhancer;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.service.RobotService;
import com.jimi.uw_server.util.ResultUtil;
import com.jimi.uw_server.util.TokenBox;


/**
 * 叉车控制层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class RobotController extends Controller {

	private static RobotService robotService = Enhancer.enhance(RobotService.class);
	public static final String SESSION_KEY_LOGIN_USER = "loginUser";


	// 查询叉车
	public void select() {
		String string = JSON.toJSONString(ResultUtil.succeed(robotService.select()));
		renderText(string);
	}


	// 启用/禁用叉车
	@ActionKey("/manage/robot/switch")
	public void robotSwitch(String id, Integer enabled) throws Exception {
		robotService.robotSwitch(id, enabled);
		renderJson(ResultUtil.succeed());
	}


	// 启动/暂停叉车
	public void pause(Boolean pause) throws Exception {
		robotService.pause(pause);
		renderJson(ResultUtil.succeed());
	}


	// 令叉车回库，并更新库存
	@Log("发送SL(回库)指令给叉车，该叉车目前绑定的任务条目为{id}")
	public void back(Integer id, Boolean isLater, Integer state) throws Exception {
		if (id == null || isLater == null || state == null) {
			throw new OperationException("参数不能为空");
		}
		String tokenId = getPara(TokenBox.TOKEN_ID_KEY_NAME);
		User user = TokenBox.get(tokenId, SESSION_KEY_LOGIN_USER);
		String resultString = robotService.back(id, isLater, state, user);
		if (resultString.equals("已成功发送回库指令！")) {
			renderJson(ResultUtil.succeed());
		} else if (resultString.equals("料盒中还有其他需要出库的物料，叉车暂时不回库！")) {
			renderJson(ResultUtil.succeed(resultString));
		} else {
			throw new OperationException(resultString);
		}
	}


	// 入库前扫料盘，发LS指令给叉车
	@Log("料号为{no}的物料需要入库，该物料对应的供应商为{supplierName}，发送LS指令让叉车取托盘到仓口{id}")
	public void call(Integer id, String no, String supplierName) throws Exception {
		if (id == null || no == null || supplierName == null) {
			throw new OperationException("参数不能为空");
		}
		String resultString = robotService.call(id, no, supplierName);
		if (resultString.equals("调用成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}
}
