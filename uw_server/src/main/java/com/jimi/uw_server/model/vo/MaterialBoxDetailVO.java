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
	
	private Integer x_position;
	
	private Integer y_position;
	
	private Integer status;

	public Integer getBoxId() {
		return boxId;
	}

	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}

	public Integer getX_position() {
		return x_position;
	}

	public void setX_position(Integer x_position) {
		this.x_position = x_position;
	}

	public Integer getY_position() {
		return y_position;
	}

	public void setY_position(Integer y_position) {
		this.y_position = y_position;
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
			materialBoxDetailVO.setX_position(record.getXPosition());
			materialBoxDetailVO.setY_position(record.getYPosition());
			materialBoxDetailVO.setStatus(record.getStatus());
			materialBoxDetailVOs.add(materialBoxDetailVO);
		}
		return materialBoxDetailVOs;
		
	}
}
