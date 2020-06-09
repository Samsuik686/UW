/**  
*  
*/
package com.jimi.uw_server.ur.entity;

import com.jimi.uw_server.ur.entity.base.UrBasePackage;

/**
 * <p>
 * Title: ScanMaterialExceptionPackage
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2019
 * </p>
 * <p>
 * Company: 惠州市几米物联技术有限公司
 * </p>
 * 
 * @author trjie
 * @date 2019年12月18日
 *
 */
public class ScanMaterialExceptionPackage extends UrBasePackage {

	private Integer taskId;

	private Integer boxId;

	private String materialId;

	private Integer exceptionCode;


	/**
	 * <p>
	 * Title
	 * <p>
	 * <p>
	 * Description
	 * <p>
	 */
	public ScanMaterialExceptionPackage() {
		// TODO Auto-generated constructor stub
	}


	public String getMaterialId() {
		return materialId;
	}


	public void setMaterialId(String materialId) {
		this.materialId = materialId;
	}


	public Integer getExceptionCode() {
		return exceptionCode;
	}


	public void setExceptionCode(Integer exceptionCode) {
		this.exceptionCode = exceptionCode;
	}


	public Integer getTaskId() {
		return taskId;
	}


	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}


	public Integer getBoxId() {
		return boxId;
	}


	public void setBoxId(Integer boxId) {
		this.boxId = boxId;
	}


}
