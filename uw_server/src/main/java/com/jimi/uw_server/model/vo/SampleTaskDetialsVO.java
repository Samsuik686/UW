package com.jimi.uw_server.model.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

	private Integer storeQuantity;

	private Integer scanQuantity;

	private Integer regularOutQuantity;

	private Integer singularOutQuantity;

	private Integer lostOutQuantity;

	private List<SampleTaskOutMaterialInfoVO> list;


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


	public List<SampleTaskOutMaterialInfoVO> getList() {
		return list;
	}


	public Integer getStoreQuantity() {
		return storeQuantity;
	}


	public void setStoreQuantity(Integer storeQuantity) {
		this.storeQuantity = storeQuantity;
	}


	public Integer getScanQuantity() {
		return scanQuantity;
	}


	public void setScanQuantity(Integer scanQuantity) {
		this.scanQuantity = scanQuantity;
	}


	public Integer getRegularOutQuantity() {
		return regularOutQuantity;
	}


	public void setRegularOutQuantity(Integer regularOutQuantity) {
		this.regularOutQuantity = regularOutQuantity;
	}


	public Integer getSingularOutQuantity() {
		return singularOutQuantity;
	}


	public void setSingularOutQuantity(Integer singularOutQuantity) {
		this.singularOutQuantity = singularOutQuantity;
	}


	public void setList(List<SampleTaskOutMaterialInfoVO> list) {
		this.list = list;
	}


	public Integer getLostOutQuantity() {
		return lostOutQuantity;
	}


	public void setLostOutQuantity(Integer lostOutQuantity) {
		this.lostOutQuantity = lostOutQuantity;
	}


	public static List<SampleTaskDetialsVO> fillList(List<Record> records) {
		List<SampleTaskDetialsVO> sampleTaskDetialsVOs = new ArrayList<>();
		Map<Integer, List<SampleTaskOutMaterialInfoVO>> map = new LinkedHashMap<Integer, List<SampleTaskOutMaterialInfoVO>>();
		for (Record record : records) {
			SampleTaskDetialsVO sampleTaskDetialsVO = new SampleTaskDetialsVO();
			if (map.get(record.get("sample_task_item_id")) == null) {
				sampleTaskDetialsVO.setTaskId(record.getInt("task_id"));
				sampleTaskDetialsVO.setMaterialTypeId(record.getInt("material_type_id"));
				sampleTaskDetialsVO.setSampleTaskItemId(record.getInt("sample_task_item_id"));
				sampleTaskDetialsVO.setNo(record.getStr("no"));
				sampleTaskDetialsVO.setStoreQuantity(record.getInt("store_quantity"));
				sampleTaskDetialsVO.setScanQuantity(record.getInt("scan_quantity"));
				sampleTaskDetialsVO.setRegularOutQuantity(record.getInt("regular_out_quantity"));
				sampleTaskDetialsVO.setSingularOutQuantity(record.getInt("singular_out_quantity"));
				sampleTaskDetialsVO.setLostOutQuantity(record.getInt("lost_out_quantity"));
				sampleTaskDetialsVOs.add(sampleTaskDetialsVO);
				List<SampleTaskOutMaterialInfoVO> infoVOs = new ArrayList<>();
				if (record.getStr("id") != null) {
					SampleTaskOutMaterialInfoVO infoVO = new SampleTaskOutMaterialInfoVO();
					infoVO.setMaterialId(record.getStr("material_id"));
					infoVO.setId(record.getInt("id"));
					infoVO.setQuantity(record.getInt("quantity"));
					infoVO.setOperator(record.getStr("operator"));
					infoVO.setTime(record.getDate("time"));
					infoVO.setOutType(record.getInt("out_type"));
					infoVOs.add(infoVO);
				}
				map.put(record.getInt("sample_task_item_id"), infoVOs);
			} else {
				if (record.getStr("id") != null) {
					List<SampleTaskOutMaterialInfoVO> infoVOs = map.get(record.get("sample_task_item_id"));
					SampleTaskOutMaterialInfoVO infoVO = new SampleTaskOutMaterialInfoVO();
					infoVO.setMaterialId(record.getStr("material_id"));
					infoVO.setId(record.getInt("id"));
					infoVO.setQuantity(record.getInt("quantity"));
					infoVO.setOperator(record.getStr("operator"));
					infoVO.setTime(record.getDate("time"));
					infoVO.setOutType(record.getInt("out_type"));
					infoVOs.add(infoVO);
				}
			}
		}
		for (SampleTaskDetialsVO sampleTaskDetialsVO : sampleTaskDetialsVOs) {
			sampleTaskDetialsVO.setList(map.get(sampleTaskDetialsVO.getSampleTaskItemId()));
		}
		return sampleTaskDetialsVOs;
	}
}
