package com.jimi.uw_server.model.vo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.agv.dao.TaskPropertyRedisDAO;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;


/**
 * 任务表示层对象
 * @author HardyYao
 * @createTime 2018年10月13日  上午10:01:54
 */

public class TaskVO{

	private Integer id;
	
	private Integer state;
	
	private String stateString;
	
	private Integer type;
	
	private String typeString;
	
	private String fileName;
	
	private Integer supplier;
	
	private String supplierName;

	private String createTimeString;

	private Integer priority;
	
	private Boolean status;
	
	private String remarks;
	


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public Integer getState() {
		return state;
	}


	public void setState(Integer state) {
		this.state = state;
	}


	public String getStateString() {
		return stateString;
	}


	public void setStateString(String stateString) {
		this.stateString = stateString;
	}


	public Integer getType() {
		return type;
	}


	public void setType(Integer type) {
		this.type = type;
	}


	public String getTypeString() {
		return typeString;
	}


	public void setTypeString(String typeString) {
		this.typeString = typeString;
	}


	public String getFileName() {
		return fileName;
	}


	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	public Integer getSupplier() {
		return supplier;
	}


	public void setSupplier(Integer supplier) {
		this.supplier = supplier;
	}


	public String getSupplierName() {
		return supplierName;
	}


	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}


	public String getCreateTimeString() {
		return createTimeString;
	}


	public void setCreateTimeString(String createTimeString) {
		this.createTimeString = createTimeString;
	}


	public Integer getPriority() {
		return priority;
	}


	public void setPriority(Integer priority) {
		this.priority = priority;
	}


	public Boolean getStatus() {
		return status;
	}


	public void setStatus(Boolean status) {
		this.status = status;
	}


	public String getRemarks() {
		return remarks;
	}


	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}


	public static List<TaskVO> fillList(List<Record> records, Set<Integer> taskIdSet){
		if (records.isEmpty()) {
			return Collections.emptyList();
		}
		List<TaskVO> taskVOs = new ArrayList<TaskVO>(records.size());
		DateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (Record record : records) {
			TaskVO taskVO = new TaskVO();
			taskVO.setId(record.getInt("Task_Id"));
			taskVO.setState(record.getInt("Task_State"));
			taskVO.setType(record.getInt("Task_Type"));
			taskVO.setFileName(record.getStr("Task_FileName"));
			taskVO.setPriority(record.getInt("Task_Priority"));
			taskVO.setSupplier(record.getInt("Task_Supplier"));
			taskVO.setSupplierName(record.getStr("Supplier_Name"));
			taskVO.setRemarks(record.getStr("Task_Remarks"));
			if (record.getDate("Task_CreateTime") != null) {
				taskVO.setCreateTimeString(formater.format(record.getDate("Task_CreateTime")));
			}
			if (record.getInt("Task_State") != null) {
				switch (record.getInt("Task_State")) {
				case TaskState.WAIT_REVIEW:
					taskVO.setStateString("未审核");
					break;
				case TaskState.WAIT_START:
					taskVO.setStateString("未开始");
					break;
				case TaskState.PROCESSING:
					taskVO.setStateString("进行中");
					break;
				case TaskState.FINISHED:
					taskVO.setStateString("已完成");
					break;
				case TaskState.CANCELED:
					taskVO.setStateString("已作废");
					break;
				case TaskState.EXIST_LACK:
					taskVO.setStateString("存在缺料");
					break;
				default:
					break;
				}
			}
			if (record.getInt("Task_Type") != null) {
				switch (record.getInt("Task_Type")) {
				case TaskType.IN:
					taskVO.setTypeString("入库");
					break;
				case TaskType.OUT:
					taskVO.setTypeString("出库");
					break;
				case TaskType.COUNT:
					taskVO.setTypeString("盘点");
					break;
				case TaskType.POSITION_OPTIZATION:
					taskVO.setTypeString("位置优化");
					break;
				case TaskType.SEND_BACK:
					taskVO.setTypeString("调拨入库");
					break;
				case TaskType.SAMPLE:
					taskVO.setTypeString("抽检");
					break;
				case TaskType.EMERGENCY_OUT:
					taskVO.setTypeString("出库-紧急");
					break;
				case TaskType.EXTERNAL_IN_OUT:
					taskVO.setTypeString("外仓调拨");
					break;
				case TaskType.WASTAGE:
					taskVO.setTypeString("损耗");
					break;
				default:
					break;
				}
			}
			if (taskIdSet != null && !taskIdSet.isEmpty()) {
				if (record.getInt("Task_State").equals(TaskState.PROCESSING) && taskIdSet.contains(record.getInt("Task_Id"))) {
					taskVO.setStatus(TaskPropertyRedisDAO.getTaskStatus(record.getInt("Task_Id")));
				}
			}
			taskVOs.add(taskVO);
		}
		return taskVOs;
	}
}
