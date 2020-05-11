/**  
*  
*/  
package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.List;

import com.jimi.uw_server.model.MaterialBoxDetail;

/**  
 * <p>Title: MaterialBoxDetailVO</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年4月30日
 *
 */
public class MaterialBoxDetailVO {

	private Integer boxId;
	
	private Integer row;
	
	private Integer col;
	
	private Integer status;

	public Integer getBoxId() {
		return boxId;
	}

	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}

	public Integer getRow() {
		return row;
	}

	public void setRow(Integer row) {
		this.row = row;
	}

	public Integer getCol() {
		return col;
	}

	public void setCol(Integer col) {
		this.col = col;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public static List<MaterialBoxDetailVO> fillList(List<MaterialBoxDetail> records){
		List<MaterialBoxDetailVO> materialBoxDetailVOs = new ArrayList<MaterialBoxDetailVO>(records.size());
		for (MaterialBoxDetail record : records) {
			
			MaterialBoxDetailVO materialBoxDetailVO = new MaterialBoxDetailVO();
			materialBoxDetailVO.setBoxId(record.getBoxId());
			materialBoxDetailVO.setRow(record.getXPosition());
			materialBoxDetailVO.setCol(record.getYPosition());
			materialBoxDetailVO.setStatus(record.getStatus());
			materialBoxDetailVOs.add(materialBoxDetailVO);
		}
		return materialBoxDetailVOs;
		
	}
}
