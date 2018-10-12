package com.jimi.uw_server.controller;

import com.jfinal.aop.Enhancer;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.util.ResultUtil;

/**
 * 物料控制层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class MaterialController extends Controller {

	private static MaterialService materialService = Enhancer.enhance(MaterialService.class);

	// 统计物料类型信息
	public void count(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		renderJson(ResultUtil.succeed(materialService.count(pageNo, pageSize, ascBy, descBy, filter)));
	}

	// 获取物料实体
	public void getEntities(Integer type, Integer pageNo, Integer pageSize) {
		renderJson(ResultUtil.succeed(materialService.getEntities(type, pageNo, pageSize)));
	}

	// 添加物料类型#
	@Log("添加料号为{no}的物料类型,物料具体位置为: 区域号{area},行号{row},列号{col},高度{height}")
	public void add(String no, Integer area, Integer row, Integer col, Integer height) {
		String resultString = materialService.add(no, area, row, col, height);
		if(resultString.equals("添加成功！")) {
			renderJson(ResultUtil.succeed());
		}else {
			throw new OperationException(resultString);
		}
	}

    // 更新物料类型#
	@Log("更新料号为{no}的物料类型,更新后的物料具体位置为: 区域号{area},行号{row},列号{col},高度{height},传递的enabeld值为{enabled}(0表示禁用,1表示启用)")
	public void update(@Para("") MaterialType materialType) {
		String resultString = materialService.update(materialType);
		if(resultString.equals("更新成功！")) {
			renderJson(ResultUtil.succeed());
		}else {
			throw new OperationException(resultString);
		}
	}
	
}
