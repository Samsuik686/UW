package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.plugin.activerecord.Record;

/**
 * 
 * @author HardyYao
 * @createTime 2019年5月10日  上午9:08:49
 */

public class EWhMaterialDetailVO {
	
	private Integer id;
	
	private Integer materialTypeId;
	
	private String no;
	
	private Integer taskId;
	
	private Integer taskType;
	
	private String taskTypeString;
	
	private String taskName;
	
	private Integer sourceWhId;
	
	private Integer destinationId;
	
	private String sourceWhName;
	
	private String destinationName;
	
	private Integer quantity;
	
	private String operatior;
	
	private Date time;
	
	
	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
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


	public Integer getTaskId() {
		return taskId;
	}


	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}


	public Integer getTaskType() {
		return taskType;
	}


	public void setTaskType(Integer taskType) {
		this.taskType = taskType;
	}


	public String getTaskTypeString() {
		return taskTypeString;
	}


	public void setTaskTypeString(String taskTypeString) {
		this.taskTypeString = taskTypeString;
	}


	public String getTaskName() {
		return taskName;
	}


	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}


	public Integer getSourceWhId() {
		return sourceWhId;
	}


	public void setSourceWhId(Integer sourceWhId) {
		this.sourceWhId = sourceWhId;
	}


	public Integer getDestinationId() {
		return destinationId;
	}


	public void setDestinationId(Integer destinationId) {
		this.destinationId = destinationId;
	}


	public String getSourceWhName() {
		return sourceWhName;
	}


	public void setSourceWhName(String sourceWhName) {
		this.sourceWhName = sourceWhName;
	}


	public String getDestinationName() {
		return destinationName;
	}


	public void setDestinationName(String destinationnName) {
		this.destinationName = destinationnName;
	}


	public Integer getQuantity() {
		return quantity;
	}


	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}


	public String getOperatior() {
		return operatior;
	}


	public void setOperatior(String operatior) {
		this.operatior = operatior;
	}


	public Date getTime() {
		return time;
	}


	public void setTime(Date time) {
		this.time = time;
	}


	public static List<EWhMaterialDetailVO> fillList(List<Record> records){
		
		List<EWhMaterialDetailVO> eWhMaterialDetails = new ArrayList<EWhMaterialDetailVO>();
		for (Record record : records) {
			EWhMaterialDetailVO eWhMaterialDetail = new EWhMaterialDetailVO();
			eWhMaterialDetail.setId(record.getInt("id"));
			eWhMaterialDetail.setMaterialTypeId(record.getInt("material_type_id"));
			eWhMaterialDetail.setTaskId(record.getInt("task_id"));
			eWhMaterialDetail.setQuantity(record.getInt("quantity"));
			eWhMaterialDetail.setSourceWhId(record.getInt("source_wh"));
			eWhMaterialDetail.setDestinationId(record.getInt("destination"));
			eWhMaterialDetail.setOperatior(record.getStr("operatior"));
			eWhMaterialDetail.setTime(record.getDate("time"));
			eWhMaterialDetail.setSourceWhName(record.getStr("source_wh_name"));
			eWhMaterialDetail.setDestinationName(record.getStr("destination_name"));
			eWhMaterialDetail.setTaskName(record.getStr("task_name"));
			eWhMaterialDetail.setTaskType(record.getInt("task_type"));
			eWhMaterialDetail.setNo(record.getStr("no"));
			switch (record.getInt("task_type")) {
			case 0:
				eWhMaterialDetail.setTaskTypeString("入库");
				break;
			case 1:
				eWhMaterialDetail.setTaskTypeString("出库");
				break;
			case 2:
				eWhMaterialDetail.setTaskTypeString("盘点");
				break;
			case 3:
				eWhMaterialDetail.setTaskTypeString("位置优化");
				break;
			case 4:
				eWhMaterialDetail.setTaskTypeString("调拨入库");
				break;
			case 5:
				eWhMaterialDetail.setTaskTypeString("外仓出入库");
				break;
			case 6:
				eWhMaterialDetail.setTaskTypeString("损耗");
				break;
			default:
				break;
			}
			eWhMaterialDetails.add(eWhMaterialDetail);
		}
		return eWhMaterialDetails;
	}

}
