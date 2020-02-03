package com.jimi.uw_server.controller;

import com.jfinal.aop.Aop;
import com.jfinal.core.Controller;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.service.SupplierService;
import com.jimi.uw_server.util.ResultUtil;


/**
 * 客户控制层
 * @author HardyYao
 * @createTime 2018年11月22日  下午3:55:50
 */

public class SupplierController extends Controller {

	private static SupplierService supplierService = Aop.get(SupplierService.class);


	// 添加客户
	@Log("添加公司为{companyId},名为{name}的客户")
	public void add(String name, Integer companyId) {
		String resultString = supplierService.add(name, companyId);
		if (resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	// 更新客户的启用/禁用状态
	@Log("删除客户号为{id}的客户")
	public void update(Integer id) {
		supplierService.delete(id);
		renderJson(ResultUtil.succeed());
	}


	// 查询所有客户
	public void getSuppliers(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(supplierService.getSuppliers(pageNo, pageSize, ascBy, descBy, filter)));
	}


	// 更新客户的曾用名
	@Log("更改客户{id}，名字为{name}")
	public void changeName(Integer id, String name) {
		if (id == null || name == null) {
			throw new OperationException("参数不能为空");
		}
		renderJson(ResultUtil.succeed(supplierService.changeName(id, name)));
	}

}
