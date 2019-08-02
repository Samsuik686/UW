package com.jimi.uw_server.controller;

import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.service.DestinationService;
import com.jimi.uw_server.util.ResultUtil;


/**
 * 发料目的地控制层
 * @author HardyYao
 * @createTime 2019年3月11日  上午11:35:50
 */

public class DestinationController extends Controller {

	private static DestinationService destinationService = Enhancer.enhance(DestinationService.class);


	// 添加发料目的地
	@Log("添加名为{name}的发料目的地")
	public void add(String name) {
		String resultString = destinationService.add(name);
		if (resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}

	// @Log("更新发料目的地号为{id}的信息，传递的发料目的地名参数为{name}")
	// public void update(Integer id, String name) {
	// String resultString = destinationService.update(id, name);
	// if(resultString.equals("更新成功！")) {
	// renderJson(ResultUtil.succeed());
	// } else {
	// throw new OperationException(resultString);
	// }
	// }


	// 删除发料目的地
	@Log("更新发料目的地号为{id}的启/禁用状态，传递的enabeld值为{enabled}(0表示执行删除,1表示不执行删除操作)")
	public void delete(Integer id, Boolean enabled) {
		String resultString = destinationService.delete(id, enabled);
		if (resultString.equals("删除成功！")) {
			renderJson(ResultUtil.succeed());
		} else {
			throw new OperationException(resultString);
		}
	}


	// 查询发料目的地
	public void get(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(destinationService.get(pageNo, pageSize, ascBy, descBy, filter)));
	}

}
