/**  
*  
*/  
package com.jimi.uw_server.model.bo;

import com.jimi.uw_server.util.ExcelHelper.Excel;

/**  
 * <p>Title: BoxPositonMoveTaskItemBo</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月2日
 *
 */
public class BoxPositonMoveTaskItemBO {
	
	@Excel(col = 0, head = "X1")
	private Integer x1;

	@Excel(col = 1, head = "Y1")
	private Integer y1;

	@Excel(col = 2, head = "Z1")
	private Integer z1;
	
	@Excel(col = 3, head = "X2")
	private Integer x2;

	@Excel(col = 4, head = "Y2")
	private Integer y2;

	@Excel(col = 5, head = "Z2")
	private Integer z2;

	public Integer getX1() {
		return x1;
	}

	public void setX1(Integer x1) {
		this.x1 = x1;
	}

	public Integer getY1() {
		return y1;
	}

	public void setY1(Integer y1) {
		this.y1 = y1;
	}

	public Integer getZ1() {
		return z1;
	}

	public void setZ1(Integer z1) {
		this.z1 = z1;
	}

	public Integer getX2() {
		return x2;
	}

	public void setX2(Integer x2) {
		this.x2 = x2;
	}

	public Integer getY2() {
		return y2;
	}

	public void setY2(Integer y2) {
		this.y2 = y2;
	}

	public Integer getZ2() {
		return z2;
	}

	public void setZ2(Integer z2) {
		this.z2 = z2;
	}

	
}
