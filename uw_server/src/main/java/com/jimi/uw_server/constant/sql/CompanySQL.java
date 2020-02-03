/**  
*  
*/  
package com.jimi.uw_server.constant.sql;

/**  
 * <p>Title: CompanySql</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月8日
 *
 */
public class CompanySQL {
	
	public static final String GET_COMPANY_SQL = "SELECT * FROM company WHERE enabled = 1";
	
	public static final String GET_COMPANY_BY_NAME_SQL = "SELECT * FROM company WHERE name = ? and enabled = 1";
	
	public static final String GET_COMPANY_BY_NICKNAME_SQL = "SELECT * FROM company WHERE nickname = ? and enabled = 1";

	public static final String GET_COMPANY_BY_CODE_SQL = "SELECT * FROM company WHERE company_code = ? and enabled = 1";

}
