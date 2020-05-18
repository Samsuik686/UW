/**  
*  
*/
package com.jimi.uw_server.service.sample;

import java.io.File;
import java.util.Date;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jimi.uw_server.constant.MaterialStatus;
import com.jimi.uw_server.constant.SamplerOutType;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.constant.sql.SampleTaskSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.lock.PreciousTaskLock;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.SampleOutRecord;
import com.jimi.uw_server.model.SampleTaskItem;
import com.jimi.uw_server.model.SampleTaskMaterialRecord;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.service.base.BaseSampleTaskService;

/**
 * <p>
 * Title: PreciousSampleTaskService
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
 * @date 2020年5月26日
 *
 */
public class PreciousSampleTaskService extends BaseSampleTaskService {

	private static MaterialService materialService = Aop.get(MaterialService.class);


	public void create(File file, Integer supplierId, String remarks, Integer warehouseType) {
		synchronized (PreciousTaskLock.CREATE_SAMPLE_LOCK) {
			super.createSampleTask(file, supplierId, remarks, warehouseType);
		}
	}


	public String start(Integer taskId) {
		synchronized (PreciousTaskLock.START_SAMPLE_LOCK) {
			Task task = Task.dao.findById(taskId);
			if (task == null || !task.getType().equals(TaskType.SAMPLE) || !task.getState().equals(TaskState.WAIT_START)) {
				throw new OperationException("抽检任务不存在或并未处于未开始状态，无法开始任务！");
			}
			Material material = Material.dao.findFirst(SampleTaskSQL.GET_PROBLEM_MATERIAL_BY_SAMPLE_TASK, task.getId(), WarehouseType.PRECIOUS.getId(), MaterialStatus.NORMAL);
			if (material != null) {
				throw new OperationException("该抽检任务所抽检的物料类型存在处于出库、入库或截料状态的物料，请检查！");
			}
			List<Material> materials = Material.dao.find(SampleTaskSQL.GET_MATERIAL_BY_SAMPLE_TASK, task.getId(), WarehouseType.PRECIOUS.getId(), MaterialStatus.NORMAL);
			for (Material material2 : materials) {
				SampleTaskMaterialRecord record = new SampleTaskMaterialRecord();
				record.setMaterialId(material2.getId());
				record.setTaskId(task.getId());
				record.save();
			}

			List<SampleTaskItem> sampleTaskItems = SampleTaskItem.dao.find(SampleTaskSQL.GET_SAMPLETASKITEM_BY_TASK, task.getId());
			// 填写本次抽检库存
			boolean flag = true;
			for (SampleTaskItem sampleTaskItem : sampleTaskItems) {
				int store = materialService.countPreciousQuantityByMaterialTypeId(sampleTaskItem.getMaterialTypeId());
				if (store != 0) {
					flag = false;
				}
				sampleTaskItem.setStoreQuantity(store);
				sampleTaskItem.update();
			}

			if (flag) {
				task.setState(TaskState.FINISHED);
				task.setEndTime(new Date());
				task.update();
			} else {
				task.setState(TaskState.PROCESSING);
				task.setStartTime(new Date());
				task.update();
			}
		}
		return "操作成功！";
	}


	// 作废指定任务
	public boolean cancel(Integer id) {
		synchronized (PreciousTaskLock.CANCEL_SAMPLE_LOCK) {
			Task task = Task.dao.findById(id);
			int state = task.getState();
			// 对于已完成的任务，禁止作废
			if (state == TaskState.FINISHED) {
				throw new OperationException("该任务已完成，禁止作废！");
			} else if (state == TaskState.CANCELED) { // 对于已作废过的任务，禁止作废
				throw new OperationException("该任务已作废！");
			} else {
				// 更新任务状态为作废
				task.setState(TaskState.CANCELED).update();
				return true;
			}
		}
	}


	// 作废指定任务
	public boolean finish(Integer id) {
		synchronized (PreciousTaskLock.FINISH_SAMPLE_LOCK) {
			Task task = Task.dao.findById(id);
			int state = task.getState();
			// 对于已完成的任务，禁止完成
			if (state != TaskState.PROCESSING) {
				throw new OperationException("该任务未处于进行中状态，无法完成！");
			}
			// 更新任务状态为完成
			SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(SampleTaskSQL.GET_UNSCAN_MATERIAL_BY_TASK, id);
			if (record != null) {
				throw new OperationException("存在未扫码物料，无法完成任务");
			}
			task.setState(TaskState.FINISHED).update();
			return true;

		}
	}


	public String outSingular(String materialId, Integer taskId, User user) {
		synchronized (Lock.PRECIOUS_SAMPLE_TASK_SCAN_LOCK) {

			Material material = Material.dao.findById(materialId);
			if (material == null || material.getRemainderQuantity() <= 0) {
				throw new OperationException("料盘不存在或者已经出库！");
			}
			if (!material.getStatus().equals(MaterialStatus.NORMAL)) {
				throw new OperationException("该料盘目前不属于正常状态，可能出库、入库或者截料中！");
			}
			SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(SampleTaskSQL.GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, taskId, material.getType(), WarehouseType.PRECIOUS.getId());
			if (sampleTaskItem == null) {
				throw new OperationException("该料盘不在本次抽检范围内！");
			}
			SampleTaskMaterialRecord sampleTaskMaterialRecord = SampleTaskMaterialRecord.dao.findFirst(SampleTaskSQL.GET_UNSCAN_MATERIAL_BY_MATERIAL, taskId, materialId);
			if (sampleTaskMaterialRecord != null) {
				throw new OperationException("该料盘尚未扫码，请先扫码后再出库！");
			}
			SampleOutRecord sampleOutRecord = new SampleOutRecord();
			sampleOutRecord.setMaterialId(materialId);
			sampleOutRecord.setSampleTaskItemId(sampleTaskItem.getId());
			sampleOutRecord.setOutType(SamplerOutType.SINGULAR);
			sampleOutRecord.setQuantity(material.getRemainderQuantity());
			sampleOutRecord.setOperator(user.getUid());
			sampleOutRecord.setTime(new Date());
			sampleOutRecord.save();

			sampleTaskItem.setSingularOutQuantity(sampleTaskItem.getSingularOutQuantity() + material.getRemainderQuantity());
			sampleTaskItem.update();

			material.setRemainderQuantity(0);
			material.update();
		}

		return "操作成功";
	}


	public String outRegular(String materialId, Integer taskId, User user) {
		synchronized (Lock.PRECIOUS_SAMPLE_TASK_SCAN_LOCK) {
			Material material = Material.dao.findById(materialId);
			if (material == null || material.getRemainderQuantity() <= 0) {
				throw new OperationException("料盘不存在或者已经出库！");
			}
			if (!material.getStatus().equals(MaterialStatus.NORMAL)) {
				throw new OperationException("该料盘目前不属于正常状态，可能出库、入库或者截料中！");
			}
			SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(SampleTaskSQL.GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, taskId, material.getType(), WarehouseType.PRECIOUS.getId());
			if (sampleTaskItem == null) {
				throw new OperationException("该料盘不在本次抽检范围内！");
			}
			SampleTaskMaterialRecord sampleTaskMaterialRecord = SampleTaskMaterialRecord.dao.findFirst(SampleTaskSQL.GET_UNSCAN_MATERIAL_BY_MATERIAL, taskId, materialId);
			if (sampleTaskMaterialRecord != null) {
				throw new OperationException("该料盘尚未扫码，请先扫码后再出库！");
			}
			SampleOutRecord sampleOutRecord = new SampleOutRecord();
			sampleOutRecord.setMaterialId(materialId);
			sampleOutRecord.setSampleTaskItemId(sampleTaskItem.getId());
			sampleOutRecord.setOutType(SamplerOutType.REGULAR);
			sampleOutRecord.setQuantity(material.getRemainderQuantity());
			sampleOutRecord.setOperator(user.getUid());
			sampleOutRecord.setTime(new Date());
			sampleOutRecord.save();

			sampleTaskItem.setRegularOutQuantity(sampleTaskItem.getRegularOutQuantity() + material.getRemainderQuantity());
			sampleTaskItem.update();

			material.setRemainderQuantity(0);
			material.setIsRepeated(true);
			material.update();
		}
		return "操作成功";
	}


	public String outLost(String materialId, Integer taskId, User user) {
		synchronized (Lock.PRECIOUS_SAMPLE_TASK_SCAN_LOCK) {
			Material material = Material.dao.findById(materialId);
			if (material == null || material.getRemainderQuantity() <= 0) {
				throw new OperationException("料盘不存在或者已经出库！");
			}
			if (!material.getStatus().equals(MaterialStatus.NORMAL)) {
				throw new OperationException("该料盘目前不属于正常状态，可能出库、入库或者截料中！");
			}
			SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(SampleTaskSQL.GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, taskId, material.getType(), WarehouseType.PRECIOUS.getId());
			if (sampleTaskItem == null) {
				throw new OperationException("该料盘不在本次抽检范围内！");
			}
			SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(SampleTaskSQL.GET_RECORD_BY_MATERIALID_TASK, materialId, taskId);
			if (!record.getIsScaned()) {
				record.setIsScaned(true).update();
				sampleTaskItem.setScanQuantity(sampleTaskItem.getScanQuantity() + material.getRemainderQuantity());
				sampleTaskItem.update();
			} else {
				throw new OperationException("料盘已扫码，不能进行料盘丢失操作！");
			}
			SampleOutRecord sampleOutRecord = new SampleOutRecord();
			sampleOutRecord.setMaterialId(materialId);
			sampleOutRecord.setSampleTaskItemId(sampleTaskItem.getId());
			sampleOutRecord.setOutType(SamplerOutType.LOST);
			sampleOutRecord.setQuantity(material.getRemainderQuantity());
			sampleOutRecord.setOperator(user.getUid());
			sampleOutRecord.setTime(new Date());
			sampleOutRecord.save();

			sampleTaskItem.setLostOutQuantity(sampleTaskItem.getLostOutQuantity() + material.getRemainderQuantity());
			sampleTaskItem.update();

			material.setRemainderQuantity(0);
			material.setIsRepeated(true);
			material.update();
		}
		return "操作成功";
	}


	public void sampleUWMaterial(String materialId, Integer taskId) {
		synchronized (Lock.PRECIOUS_SAMPLE_TASK_SCAN_LOCK) {
			Task task = Task.dao.findById(taskId);
			if (!task.getState().equals(TaskState.PROCESSING)) {
				throw new OperationException("任务未处于进行中状态！");
			}
			Material material = Material.dao.findById(materialId);
			if (material == null) {
				throw new OperationException("料盘不存在！");
			}
			SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(SampleTaskSQL.GET_RECORD_BY_MATERIALID_TASK, materialId, taskId);
			if (record == null) {
				throw new OperationException("本次抽检抽检范围不包含该料盘！");
			}
			if (record.getIsScaned()) {
				throw new OperationException("该料盘已经扫描过，请勿重复扫描！");
			} else {
				SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(SampleTaskSQL.GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, taskId, material.getType(), WarehouseType.PRECIOUS.getId());
				if (sampleTaskItem == null) {
					throw new OperationException("本次抽检抽检范围不包含该料盘！");
				}
				record.setIsScaned(true).update();
				sampleTaskItem.setScanQuantity(sampleTaskItem.getScanQuantity() + material.getRemainderQuantity());
				sampleTaskItem.update();
			}
		}

	}
}
