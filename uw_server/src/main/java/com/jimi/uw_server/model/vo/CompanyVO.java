/**  
*  
*/  
package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.List;

import com.jimi.uw_server.model.Company;

/**  
 * <p>Title: CompanyVO</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月8日
 *
 */
public class CompanyVO {

	private Integer id; 
	
	private String companyCode;
	
	private String name;
	
	private String nickname;
	
	private Boolean enabled;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	
	public static List<CompanyVO> fillList(List<Company> companies){
		List<CompanyVO> companyVOs = new ArrayList<CompanyVO>(companies.size());
		for (Company company : companies) {
			CompanyVO companyVO = new CompanyVO();
			companyVO.setId(company.getId());
			companyVO.setCompanyCode(company.getCompanyCode());
			companyVO.setName(company.getName());
			companyVO.setNickname(company.getNickname());
			companyVO.setEnabled(company.getEnabled());
			companyVOs.add(companyVO);
		}
		return companyVOs;
	}
}
