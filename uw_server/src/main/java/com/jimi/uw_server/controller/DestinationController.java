package com.jimi.uw_server.controller;

import com.jfinal.aop.Aop;
import com.jfinal.core.Controller;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.service.DestinationService;
import com.jimi.uw_server.util.ResultUtil;


/**
 * 发料目的地控制层
 * 
 * @author HardyYao
 * @createTime 2019年3月11日 上午11:35:50
 */

public class DestinationController extends Controller {

	private static DestinationService destinationService = Aop.get(DestinationService.class);


	// 添加发料目的地
	@Log("添加名为{name}, 公司ID为{companyId}的发料目的地")
	public void add(String name, Integer companyId) {
		if (name == null || companyId == null) {
			throw new ParameterException("参数不能为空！");
		}
		destinationService.add(name, companyId);
		renderJson(ResultUtil.succeed());

	}


	// 删除发料目的地
	@Log("删除发料目的地号为{id}")
	public void delete(Integer id) {
		if (id == null) {
			throw new ParameterException("参数不能为空！");
		}
		destinationService.delete(id);
		renderJson(ResultUtil.succeed());
	}


	@Log("修改发料目的地名称，ID：{id}， 名称：{name}")
	public void update(Integer id, String name) {
		if (id == null || name == null) {
			throw new ParameterException("参数不能为空！");
		}
		destinationService.update(id, name);
	}


	// 查询发料目的地
	public void getDestinations(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(destinationService.select(pageNo, pageSize, ascBy, descBy, filter)));
	}

}
