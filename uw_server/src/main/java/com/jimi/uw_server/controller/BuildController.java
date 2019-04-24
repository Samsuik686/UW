package com.jimi.uw_server.controller;

import com.jfinal.aop.Enhancer;
import com.jfinal.core.ActionKey;
import com.jfinal.core.Controller;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.service.BuildService;
import com.jimi.uw_server.util.ResultUtil;

/**
 * 建仓控制层
 * @author HardyYao
 * @createTime 2018年12月12日  下午3:10:34
 */

public class BuildController extends Controller {

	private static BuildService buildService = Enhancer.enhance(BuildService.class);


	// 建仓接口
	@ActionKey("/build")
	@Log("开始建仓，传递的参数为{parameters}")
	public void build(String parameters) {
		if (parameters == null) {
			throw new OperationException("参数不能为空");
		}
		String resultString = buildService.build(parameters);
		if (resultString.equals("开始建仓！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}




}
