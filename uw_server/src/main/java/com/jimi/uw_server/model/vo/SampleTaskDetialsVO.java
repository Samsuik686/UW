package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * @author HardyYao
 * @createTime 2019年6月24日  上午9:43:06
 */

public class SampleTaskDetialsVO {
	
	Integer taskId;
	
	Integer sampleTaskItemId;
	
	private Integer materialTypeId;
	
	private String no;
	
	private String materialId;
	
	private String operator;
	
	private Integer quantity;
	
	private Boolean isSingular;
	
	private Date time;
	
	private String isSingularString;

	private Integer id;
	
	public Integer getTaskId() {
		return taskId;
	}

	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}

	public Integer getSampleTaskItemId() {
		return sampleTaskItemId;
	}

	public void setSampleTaskItemId(Integer sampleTaskItemId) {
		this.sampleTaskItemId = sampleTaskItemId;
	}

	public Integer getMaterialTypeId() {
		return materialTypeId;
	}

	public void setMaterialTypeId(Integer materialTypeId) {
		this.materialTypeId = materialTypeId;
	}

	public String getNo() {
		return no;
	}

	public void setNo(String no) {
		this.no = no;
	}

	public String getMaterialId() {
		return materialId;
	}

	public void setMaterialId(String materialId) {
		this.materialId = materialId;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Boolean getIsSingular() {
		return isSingular;
	}

	public void setIsSingular(Boolean isSingular) {
		this.isSingular = isSingular;
		if (isSingular == null){
			this.isSingularString = "正常";
		}else if (isSingular) {
			this.isSingularString = "异常出库";
		}else {
			this.isSingularString = "抽检出库";
		}
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getIsSingularString() {
		return isSingularString;
	}

	public void setIsSingularString(String isSingularString) {
		this.isSingularString = isSingularString;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public static List<SampleTaskDetialsVO> fillList(List<Record> records){
		List<SampleTaskDetialsVO> sampleTaskDetialsVOs = new ArrayList<>();
		for (Record record : records) {
			SampleTaskDetialsVO sampleTaskDetialsVO = new SampleTaskDetialsVO();
			sampleTaskDetialsVO.setId(record.getInt("id"));
			sampleTaskDetialsVO.setTaskId(record.getInt("task_id"));
			sampleTaskDetialsVO.setMaterialTypeId(record.getInt("material_type_id"));
			sampleTaskDetialsVO.setSampleTaskItemId(record.getInt("sample_task_item_id"));
			sampleTaskDetialsVO.setMaterialId(record.getStr("material_id"));
			sampleTaskDetialsVO.setNo(record.getStr("no"));
			sampleTaskDetialsVO.setQuantity(record.getInt("quantity"));
			sampleTaskDetialsVO.setOperator(record.getStr("operator"));
			sampleTaskDetialsVO.setTime(record.getDate("time"));
			sampleTaskDetialsVO.setIsSingular(record.getBoolean("is_singular"));
			sampleTaskDetialsVOs.add(sampleTaskDetialsVO);
		}
		return sampleTaskDetialsVOs;
	}
}
