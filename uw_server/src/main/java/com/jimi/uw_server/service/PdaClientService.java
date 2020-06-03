package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.constant.sql.IOTaskSQL;
import com.jimi.uw_server.constant.sql.InventoryTaskSQL;
import com.jimi.uw_server.constant.sql.MaterialTypeSQL;
import com.jimi.uw_server.constant.sql.SampleTaskSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.InventoryLog;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.SampleTaskMaterialRecord;
import com.jimi.uw_server.model.TaskLog;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.vo.IOTaskDetailVO;
import com.jimi.uw_server.model.vo.TaskVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.inventory.PreciousInventoryTaskService;
import com.jimi.uw_server.service.io.RegularIOTaskService;
import com.jimi.uw_server.service.sample.PreciousSampleTaskService;

/**
 * <p>
 * Title: PdaClientService
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
 * @date 2020年5月28日
 *
 */
public class PdaClientService {

	private PreciousInventoryTaskService preciousInventoryTaskService = Aop.get(PreciousInventoryTaskService.class);

	private PreciousSampleTaskService preciousSampleTaskService = Aop.get(PreciousSampleTaskService.class);

	private RegularIOTaskService regularIOTaskService = Aop.get(RegularIOTaskService.class);

	private SelectService selectService = Aop.get(SelectService.class);

	private ExternalWhLogService externalWhLogService = Aop.get(ExternalWhLogService.class);


	/**
	 * <p>
	 * Description: 获取进行中紧急出库任务列表
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2019年11月27日
	 */
	public List<TaskVO> getWorkingEmergencyRegularTasks() {
		String filter = "task.type=8#&#task.state=2#&#supplier.enabled=1#&#task.warehouse_type=0";
		Page<Record> result = selectService.select(new String[] { "task", "supplier" }, new String[] { "task.supplier=supplier.id" }, null, null, null, null, filter);
		List<TaskVO> taskVOs = TaskVO.fillList(result.getList(), null);
		return taskVOs;
	}


	public List<IOTaskDetailVO> getEmergencyRegularTaskInfo(Integer taskId, String no) {
		List<IOTaskDetailVO> ioTaskDetailVOs = new ArrayList<IOTaskDetailVO>();
		// 如果任务类型为出入库
		String filter = null;
		if (no != null && !no.trim().equals("")) {
			filter = "packing_list_item.task_id=" + taskId + "#&#material_type.no=" + no;
		}
		// 先进行多表查询，查询出同一个任务id的套料单表的id,物料类型表的料号no,套料单表的计划出入库数量quantity,套料单表对应任务的实际完成时间finish_time
		Page<Record> packingListItems = selectService.select(new String[] { "packing_list_item", "material_type" },
				new String[] {"material_type.id = packing_list_item.material_type_id" }, null, null, null, null, filter);

		// 遍历同一个任务id的套料单数据
		for (Record packingListItem : packingListItems.getList()) {
			// 查询task_log中的material_id,quantity
			List<TaskLog> taskLog = TaskLog.dao.find(IOTaskSQL.GET_TASK_ITEM_DETAILS_SQL, Integer.parseInt(packingListItem.get("PackingListItem_Id").toString()));
			Integer actualQuantity = 0;
			// 实际出入库数量要根据task_log中的出入库数量记录进行累加得到
			for (TaskLog tl : taskLog) {
				actualQuantity += tl.getQuantity();
			}
			Integer deductQuantity = externalWhLogService.getDeductEwhMaterialQuantityByOutTask(taskId, packingListItem.getInt("PackingListItem_MaterialTypeId"));
			IOTaskDetailVO io = new IOTaskDetailVO(packingListItem.get("PackingListItem_Id"), packingListItem.get("MaterialType_No"), packingListItem.get("PackingListItem_Quantity"), actualQuantity,
					deductQuantity, packingListItem.get("PackingListItem_FinishTime"), 8);
			io.setDetails(taskLog);
			ioTaskDetailVOs.add(io);
		}
		return ioTaskDetailVOs;
	}


	/**
	 * 
	 * <p>
	 * Description: 紧急出库料盘
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月28日
	 */
	public void outEmergencyRegular(String taskId, String no, String materialId, Integer quantity, Date productionTime, String supplierName, String cycle, String manufacturer, Date printTime,
			User user) {
		regularIOTaskService.outEmergencyRegular(taskId, no, materialId, quantity, productionTime, supplierName, cycle, manufacturer, printTime, user);
	}


	/**
	 * <p>
	 * Description: 获取进行中的贵重仓盘点任务列表
	 * <p>
	 * 
	 * @return
	 * @exception @Time 2019年11月27日
	 */
	public List<TaskVO> getWorkingPreciousInventoryTasks() {
		String filter = "task.type=2#&#task.state=2#&#supplier.enabled=1#&#task.warehouse_type=1";
		Page<Record> result = selectService.select(new String[] { "task", "supplier" }, new String[] { "task.supplier=supplier.id" }, null, null, null, null, filter);
		List<TaskVO> taskVOs = TaskVO.fillList(result.getList(), null);
		return taskVOs;
	}


	/**
	 * <p>
	 * Description: 贵重仓盘点料盘
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月28日
	 */
	public void inventoryPreciousUWMaterial(String materialId, Integer taskId, Integer acturalNum, User user) {
		preciousInventoryTaskService.inventoryUWMaterial(materialId, taskId, acturalNum, user);
	}


	/**
	 * 
	 * <p>Description: 获取未盘点的料盘数<p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年5月28日
	 */
	public Integer getUnScanInventoryTaskMaterial(Integer taskId, String no, Integer supplierId) {
		MaterialType materialType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_SQL, no, supplierId);
		if (materialType == null) {
			throw new OperationException("物料类型不存在！");
		}
		List<InventoryLog> inventoryLogs = InventoryLog.dao.find(InventoryTaskSQL.GET_UN_INVENTORY_LOG_BY_TASKID_AND_MATERIAL_TYPE, taskId, materialType.getId() );
		if (inventoryLogs == null) {
			return 0;
		}
		return inventoryLogs.size();
	}
	
	
	/**
	 * <p>
	 * Description: 获取进行中的贵重仓抽检任务列表
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2019年11月27日
	 */
	public List<TaskVO> getWorkingPreciousSampleTasks() {
		String filter = "task.type=7#&#task.state=2#&#supplier.enabled=1#&#task.warehouse_type=1";
		Page<Record> result = selectService.select(new String[] { "task", "supplier" }, new String[] { "task.supplier=supplier.id" }, null, null, null, null, filter);
		List<TaskVO> taskVOs = TaskVO.fillList(result.getList(), null);
		return taskVOs;
	}


	/**
	 * <p>
	 * Description: 贵重仓抽检料盘
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月28日
	 */
	public void samplePreciousUWMaterial(String materialId, Integer taskId) {
		preciousSampleTaskService.sampleUWMaterial(materialId, taskId);
	}


	/**
	 * 
	 * <p>Description: 获取未抽检的料盘数<p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年5月28日
	 */
	public Integer getUnScanSampleTaskMaterial(Integer taskId, String no, Integer supplierId) {
		MaterialType materialType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_SQL, no, supplierId);
		if (materialType == null) {
			throw new OperationException("物料类型不存在！");
		}
		List<SampleTaskMaterialRecord> sampleTaskMaterialRecords = SampleTaskMaterialRecord.dao.find(SampleTaskSQL.GET_UNSCAN_MATERIAL_BY_TASK_AND_MATERIAL_TYPE, taskId, materialType.getId() );
		if (sampleTaskMaterialRecords == null) {
			return 0;
		}
		return sampleTaskMaterialRecords.size();
	}
}
