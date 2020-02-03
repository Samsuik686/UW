/**  
*  
*/  
package com.jimi.uw_server.controller;

import com.jfinal.aop.Aop;
import com.jfinal.core.Controller;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.exception.ParameterException;
import com.jimi.uw_server.service.CompanyService;
import com.jimi.uw_server.util.ResultUtil;

/**  
 * <p>Title: CompanyController</p>  
 * <p>Description: 公司控制层</p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月16日
 *
 */
public class CompanyController extends Controller {

	private static CompanyService companyService = Aop.get(CompanyService.class);
	
	/**
	 * <p>Description: 添加公司 <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月8日
	 */
	@Log("添加公司，公司编码：{companyCode}， 公司名称：{name}， 公司简称：{nickname}")
	public void add(String companyCode, String name, String nickname) {
		if (companyCode == null || name == null || nickname == null) {
			throw new ParameterException("参数不能为空！");
		}
		companyService.add(companyCode, name, nickname);
		renderJson(ResultUtil.succeed());
	}
	
	
	/**
	 * <p>Description: 删除公司 <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月8日
	 */
	@Log("删除公司信息")
	public void delete(Integer id) {
		if (id == null) {
			throw new ParameterException("参数不能为空！");
		}
		companyService.delete(id);
		renderJson(ResultUtil.succeed());
	}
	
	
	/**
	 * <p>Description: 获取公司信息<p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月8日
	 */
	public void getCompanies(String name) {
		
		renderJson(ResultUtil.succeed(companyService.get(name)));
	}
	
	
	/**
	 * <p>Description: 修改公司信息(如果不修改，参数请传旧值)<p>
	 * @return
	 * @exception
	 * @author trjie 
	 * @Time 2020年1月8日
	 */
	@Log("修改公司信息，公司ID：{id}， 公司编码：{companyCode}， 公司名称：{name}， 公司简称：{nickname}")
	public void update(Integer id, String companyCode, String name, String nickname) {
		if (id == null || companyCode == null || name == null || nickname == null) {
			throw new ParameterException("参数不能为空！");
		}
		companyService.update(id, companyCode, name, nickname);
		renderJson(ResultUtil.succeed());
	}
}
