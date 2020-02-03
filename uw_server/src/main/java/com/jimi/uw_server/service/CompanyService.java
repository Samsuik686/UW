/**  
*  
*/  
package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.plugin.activerecord.Db;
import com.jimi.uw_server.constant.sql.CompanySQL;
import com.jimi.uw_server.constant.sql.MaterialBoxSQL;
import com.jimi.uw_server.constant.sql.MaterialSQL;
import com.jimi.uw_server.constant.sql.MaterialTypeSQL;
import com.jimi.uw_server.constant.sql.SupplierSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Company;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.vo.CompanyVO;

/**  
 * <p>Title: CompanyService</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月8日
 *
 */
public class CompanyService {

	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月8日
	 */
	public void add(String companyCode, String name, String nickname) {
		Company company = Company.dao.findFirst(CompanySQL.GET_COMPANY_BY_CODE_SQL, companyCode);
		if (company != null) {
			throw new OperationException("公司编码冲突，已存在！");
		}
		company = Company.dao.findFirst(CompanySQL.GET_COMPANY_BY_NAME_SQL, name);
		if (company != null) {
			throw new OperationException("公司名称冲突，已存在！");
		}
		company = Company.dao.findFirst(CompanySQL.GET_COMPANY_BY_NICKNAME_SQL, companyCode);
		if (company != null) {
			throw new OperationException("公司别名冲突，已存在！");
		}
		company = new Company();
		company.setCompanyCode(companyCode).setName(name).setNickname(nickname);
		company.save();
	}
	
	
	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月8日
	 */
	public void delete(Integer id) {
		Company company = Company.dao.findById(id);
		if (company == null) {
			throw new OperationException("公司不存在！");
		}
		Material material = Material.dao.findFirst(MaterialSQL.GET_MATERIAL_BY_COMPANY_SQL, id);
		if (material != null) {
			throw new OperationException("删除失败，该公司在仓库存在物料！");
		}
		Db.update(MaterialBoxSQL.SET_MATERIAL_BOX_SUPPLIER_COMPANY_NULL_BY_COMPANY_SQL, id);
		List<Supplier> suppliers = Supplier.dao.find(SupplierSQL.GET_SUPPLIER_BY_COMPANY_SQL, id);
		if (!suppliers.isEmpty()) {
			for (Supplier supplier : suppliers) {
				Db.update(MaterialTypeSQL.SET_MATERIAL_TYPE_UNABLED_BY_SUPPLIER_SQL, supplier.getId());
				supplier.setEnabled(false).update();
			}
		}
		company.setEnabled(false).update();
	}
	
	
	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月8日
	 */
	public List<CompanyVO> get(String name) {
		String getCompanySql = CompanySQL.GET_COMPANY_SQL;
		if (name != null && !name.trim().equals("")) {
			getCompanySql = getCompanySql + " AND name like '%" + name + "%'";
		}
		List<Company> companies = Company.dao.find(CompanySQL.GET_COMPANY_SQL);
		if (!companies.isEmpty()) {
			return CompanyVO.fillList(companies);
		}else {
			return new ArrayList<CompanyVO>();
		}
	}
	
	
	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年1月8日
	 */
	public void update(Integer id, String companyCode, String name, String nickname) {
		Company company = Company.dao.findById(id);
		if (company == null) {
			throw new OperationException("公司不存在！");
		}
		Company company2 = Company.dao.findFirst(CompanySQL.GET_COMPANY_BY_CODE_SQL, companyCode);
		if (company2 != null && !company2.getId().equals(id)) {
			throw new OperationException("公司编码冲突，已存在！");
		}
		company2 = Company.dao.findFirst(CompanySQL.GET_COMPANY_BY_NAME_SQL, name);
		if (company2 != null && !company2.getId().equals(id)) {
			throw new OperationException("公司名称冲突，已存在！");
		}
		company2 = Company.dao.findFirst(CompanySQL.GET_COMPANY_BY_NICKNAME_SQL, companyCode);
		if (company2 != null && !company2.getId().equals(id)) {
			throw new OperationException("公司别名冲突，已存在！");
		}
		company.setName(name).setNickname(nickname).setCompanyCode(companyCode).update();
	}
}
