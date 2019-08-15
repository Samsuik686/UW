package com.jimi.uw_server.controller;

import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.service.SupplierService;
import com.jimi.uw_server.util.ResultUtil;


/**
 * 供应商控制层
 * @author HardyYao
 * @createTime 2018年11月22日  下午3:55:50
 */

public class SupplierController extends Controller {

	private static SupplierService supplierService = Enhancer.enhance(SupplierService.class);


	// 添加供应商
	@Log("添加名为{name}的供应商")
	public void add(String name) {
		String resultString = supplierService.add(name);
		if (resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	// 更新供应商的启用/禁用状态
	@Log("删除供应商号为{id}的供应商")
	public void update(Integer id, Boolean enabled) {
		String resultString = supplierService.update(id, enabled);
		if (resultString.equals("更新成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	// 查询所有供应商
	public void getSuppliers(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(supplierService.getSuppliers(pageNo, pageSize, ascBy, descBy, filter)));
	}


	// 更新供应商的曾用名
	@Log("更改供应商{id}，名字为{name}")
	public void changeName(Integer id, String name) {
		if (id == null || name == null) {
			throw new OperationException("参数不能为空");
		}
		renderJson(ResultUtil.succeed(supplierService.changeName(id, name)));
	}

}