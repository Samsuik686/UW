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

	@Log("添加名为{name}的供应商")
	public void add(String name) {
		String resultString = supplierService.add(name);
		if(resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}

	@Log("更新供应商号为{id}的供应商信息，传递的供应商名参数为{name}，传递的enabeld值为{enabled}(0表示执行删除,1表示不执行删除操作)")
	public void update(Integer id, String name, Boolean enabled) {
		String resultString = supplierService.update(id, name, enabled);
		if(resultString.equals("更新成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}

	public void getSuppliers(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(supplierService.getSuppliers(pageNo, pageSize, ascBy, descBy, filter)));
	}


}
