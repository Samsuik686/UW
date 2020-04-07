/**  
*  
*/  
package com.jimi.uw_server.constant.sql;

/**  
 * <p>Title: MaterialBoxSQL</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月8日
 *
 */
public class MaterialBoxSQL {


	public static final String SET_MATERIAL_BOX_SUPPLIER_NULL_BY_SUPPLIER_SQL = "UPDATE material_box SET material_box.supplier = null WHERE material_box.supplier = ?"; 

	public static final String SET_MATERIAL_BOX_SUPPLIER_COMPANY_NULL_BY_COMPANY_SQL = "UPDATE material_box SET material_box.supplier = null, material_box.company_id = null WHERE material_box.company_id = ?"; 

	public static final String SET_MATERIAL_BOX_SUPPLIER_COMPANY_NULL_BY_SUPPLIER_AND_COMPANY_SQL = "UPDATE material_box SET material_box.supplier = null, material_box.company_id = null WHERE material_box.supplier = ? AND company_id = ?"; 
	
	public static final String GET_ENABLED_MATERIAL_BOX_BY_POSITION_SQL = "SELECT * FROM material_box WHERE row = ? AND col = ? AND height = ? AND enabled = 1";

	public static final String GET_ALL_NOT_EMPTY_BOX_BY_SUPPLIER_SQL = "SELECT DISTINCT material_box.* FROM material_box INNER JOIN material ON material_box.id = material.box WHERE material.remainder_quantity > 0 AND material_box.supplier = ? AND material_box.enabled = 1";

	public static final String GET_NOT_ON_SHELF_BOX_BY_SUPPLIER_SQL = "SELECT * FROM material_box WHERE material_box.supplier = ? AND material_box.is_on_shelf = 0 AND material_box.enabled = 1";

}
