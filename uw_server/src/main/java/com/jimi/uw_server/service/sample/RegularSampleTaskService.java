package com.jimi.uw_server.service.sample;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.agv.dao.SampleTaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.agv.handle.SampleTaskHandler;
import com.jimi.uw_server.constant.*;
import com.jimi.uw_server.constant.sql.InventoryTaskSQL;
import com.jimi.uw_server.constant.sql.SQL;
import com.jimi.uw_server.constant.sql.SampleTaskSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.lock.RegularTaskLock;
import com.jimi.uw_server.model.*;
import com.jimi.uw_server.model.vo.MaterialDetialsVO;
import com.jimi.uw_server.model.vo.PackingSampleInfoVO;
import com.jimi.uw_server.service.MaterialService;
import com.jimi.uw_server.service.base.BaseSampleTaskService;

import java.io.File;
import java.util.*;

/**
 * 
 * @author trjie
 * @createTime 2019年6月20日 下午5:20:35
 */

public class RegularSampleTaskService extends BaseSampleTaskService {

	private static final String GET_MATERIAL_BOX_BY_SAMPLE_TASK = "SELECT DISTINCT material_box.* FROM material_box JOIN material JOIN sample_task_item ON sample_task_item.material_type_id = material.type AND material_box.id = material.box WHERE sample_task_item.task_id = ? AND material.remainder_quantity > 0 AND material.is_in_box = 1";

	private static final String GET_REGULAR_MATERIAL_BY_SAMPLE_TASK = "SELECT DISTINCT material.* FROM material_box JOIN material JOIN sample_task_item JOIN material_type ON sample_task_item.material_type_id = material.type AND material.type = material_type.id AND material_box.id = material.box WHERE sample_task_item.task_id = ? AND material.remainder_quantity > 0 AND material_type.type = ? AND material.is_in_box = ?";

	private static final String GET_UNSCAN_MATERIAL_BY_TASK_AND_BOX = "SELECT * FROM sample_task_material_record WHERE task_id = ? AND box_id = ? AND is_scaned = 0";

	private static final String GET_REGULAR_UNSCAN_MATERIAL_BY_MATERIAL = "SELECT * FROM sample_task_material_record WHERE task_id = ? AND box_id = ? AND material_id = ? AND is_scaned = 0";

	private static SampleTaskHandler samTaskHandler = SampleTaskHandler.getInstance();

	public static RegularSampleTaskService me = new RegularSampleTaskService();

	private static MaterialService materialService = Aop.get(MaterialService.class);

	private Integer batchSize = 2000;

	
	public void create(File file, Integer supplierId, String remarks, Integer warehouseType) {
		synchronized (RegularTaskLock.CREATE_SAMPLE_LOCK) {
			super.createSampleTask(file, supplierId, remarks, warehouseType);
		}
	}

	
	public String start(Integer taskId, String windows) {
		synchronized (RegularTaskLock.START_SAMPLE_LOCK) {
			Task task = Task.dao.findById(taskId);
			if (task == null || !task.getType().equals(TaskType.SAMPLE) || !task.getState().equals(TaskState.WAIT_START)) {
				throw new OperationException("抽检任务不存在或并未处于未开始状态，无法开始任务！");
			}
			Task inventoryTask = Task.dao.findFirst(InventoryTaskSQL.GET_RUNNING_INVENTORY_TASK_BY_SUPPLIER, task.getSupplier(), WarehouseType.REGULAR.getId());
			InventoryLog inventoryLog = null;
			if (inventoryTask != null) {
				inventoryLog = InventoryLog.dao.findFirst(InventoryTaskSQL.GET_UNCOVER_INVENTORY_LOG_BY_TASKID, inventoryTask.getId());
			}
			if (inventoryTask != null && inventoryLog != null) {
				throw new OperationException("当前客户存在进行中的UW仓盘点任务，请等待任务结束后再开抽检任务!");
			}
			List<Window> wList = new ArrayList<>();
			String[] windowArr = windows.split(",");
			synchronized (Lock.WINDOW_LOCK) {

				for (String w : windowArr) {
					Integer windowId = 0;
					try {
						windowId = Integer.valueOf(w.trim());
					} catch (Exception e) {
						throw new OperationException("仓口参数解析错误，请检查参数格式");
					}
					Window window = Window.dao.findById(windowId);
					if (window.getBindTaskId() != null) {
						throw new OperationException("仓口" + windowId + "已经被其他任务绑定");
					}
					wList.add(window);
				}
				if (wList.size() == 0) {
					throw new OperationException("仓口参数不能为空，请检查参数及其格式");
				}
			}
			Material material = Material.dao.findFirst(GET_REGULAR_MATERIAL_BY_SAMPLE_TASK, task.getId(), WarehouseType.REGULAR.getId(), false);
			if (material != null) {
				throw new OperationException("该抽检任务所抽检的物料类型存在不在料盒内的物料，请检查！");
			}
			List<Material> materials = Material.dao.find(GET_REGULAR_MATERIAL_BY_SAMPLE_TASK, task.getId(), WarehouseType.REGULAR.getId(), true);
			List<SampleTaskMaterialRecord> sampleTaskMaterialRecords = new ArrayList<>();
			for (Material material2 : materials) {
				SampleTaskMaterialRecord record = new SampleTaskMaterialRecord();
				record.setMaterialId(material2.getId());
				record.setBoxId(material2.getBox());
				record.setTaskId(task.getId());
				sampleTaskMaterialRecords.add(record);
			}
			Db.batchSave(sampleTaskMaterialRecords, batchSize);
			List<MaterialBox> materialBoxs = MaterialBox.dao.find(GET_MATERIAL_BOX_BY_SAMPLE_TASK, task.getId());
			List<AGVSampleTaskItem> agvSampleTaskItems = new ArrayList<>();
			for (MaterialBox materialBox : materialBoxs) {
				AGVSampleTaskItem agvSampleTaskItem = new AGVSampleTaskItem(taskId, materialBox.getId());
				agvSampleTaskItems.add(agvSampleTaskItem);
			}
			List<SampleTaskItem> sampleTaskItems = SampleTaskItem.dao.find(SampleTaskSQL.GET_SAMPLETASKITEM_BY_TASK, task.getId());
			// 填写本次抽检库存
			for (SampleTaskItem sampleTaskItem : sampleTaskItems) {
				sampleTaskItem.setStoreQuantity(materialService.countAndReturnRemainderQuantityByMaterialTypeId(sampleTaskItem.getMaterialTypeId()));
				sampleTaskItem.update();
			}
			task.setState(TaskState.PROCESSING);
			task.setStartTime(new Date());
			task.update();
			// 绑定仓口
			if (!agvSampleTaskItems.isEmpty()) {
				SampleTaskItemRedisDAO.addSampleTaskItem(taskId, agvSampleTaskItems);
				for (Window window : wList) {
					window.setBindTaskId(task.getId());
					window.update();
				}
			} else {
				task.setState(TaskState.FINISHED);
				task.setEndTime(new Date());
				task.update();
			}
		}

		return "操作成功！";
	}

	
	// 作废指定任务
	public boolean cancelRegularTask(Integer id) {
		synchronized (RegularTaskLock.CANCEL_SAMPLE_LOCK) {
			Task task = Task.dao.findById(id);
			int state = task.getState();
			// 对于已完成的任务，禁止作废
			if (state == TaskState.FINISHED) {
				throw new OperationException("该任务已完成，禁止作废！");
			} else if (state == TaskState.CANCELED) { // 对于已作废过的任务，禁止作废
				throw new OperationException("该任务已作废！");
			} else {
				// 判断任务是否处于进行中状态，若是，则把相关的任务条目从til中剔除;若存在已分配的任务条目，则不解绑任务仓口
				if (state == TaskState.PROCESSING) {
					SampleTaskItemRedisDAO.removeUnAssignedSampleTaskItemByTaskId(id);
				}
				// 更新任务状态为作废
				task.setState(TaskState.CANCELED).update();
				samTaskHandler.clearTask(task.getId());
				return true;
			}
		}
	}

	
	public String backRegularUWBox(String groupId) {
		synchronized (Lock.SAMPLE_TASK_REDIS_LOCK) {
			Task task = Task.dao.findById(Integer.valueOf(groupId.split("#")[1]));
			if (task == null) {
				throw new OperationException("任务不存在，回库失败！");
			}
			for (AGVSampleTaskItem agvSampleTaskItem : SampleTaskItemRedisDAO.getSampleTaskItems(Integer.valueOf(groupId.split("#")[1]))) {
				if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
					MaterialBox materialBox = MaterialBox.dao.findById(agvSampleTaskItem.getBoxId());
					SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(GET_UNSCAN_MATERIAL_BY_TASK_AND_BOX, agvSampleTaskItem.getTaskId(), agvSampleTaskItem.getBoxId());
					if (record != null) {
						throw new OperationException("存在尚未扫描的料盘，回库失败！");
					}
					try {
						GoodsLocation goodsLocation = GoodsLocation.dao.findById(agvSampleTaskItem.getGoodsLocationId());
						if (goodsLocation != null) {
							samTaskHandler.sendBackLL(agvSampleTaskItem, materialBox, goodsLocation, task.getPriority());
						} else {
							throw new OperationException("找不到目的货位，仓口：" + agvSampleTaskItem.getWindowId() + "货位：" + agvSampleTaskItem.getGoodsLocationId());
						}
						SampleTaskItemRedisDAO.updateSampleTaskItemInfo(agvSampleTaskItem, TaskItemState.START_BACK, null, null, null, true);
					} catch (Exception e) {
						e.printStackTrace();
						throw new OperationException("发送回库指令失败，+ " + e.getMessage());
					}
					break;
				}
			}
		}
		return "操作完成";
	}

	
	public String outSingular(String materialId, String groupId, User user) {
		synchronized (Lock.REGULAR_SAMPLE_TASK_SCAN_LOCK) {
			for (AGVSampleTaskItem agvSampleTaskItem : SampleTaskItemRedisDAO.getSampleTaskItems(Integer.valueOf(groupId.split("#")[1]))) {
				if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
					Material material = Material.dao.findById(materialId);
					if (material == null || material.getRemainderQuantity() <= 0 || !material.getIsInBox()) {
						throw new OperationException("料盘不存在或者已经出库！");
					}
					SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(SampleTaskSQL.GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, agvSampleTaskItem.getTaskId(), material.getType(),
							WarehouseType.REGULAR.getId());
					if (sampleTaskItem == null) {
						throw new OperationException("该料盘不在本次抽检范围内！");
					}
					if (!material.getBox().equals(agvSampleTaskItem.getBoxId())) {
						throw new OperationException("该料盘不在当前料盒内！");
					}
					SampleTaskMaterialRecord sampleTaskMaterialRecord = SampleTaskMaterialRecord.dao.findFirst(GET_REGULAR_UNSCAN_MATERIAL_BY_MATERIAL, agvSampleTaskItem.getTaskId(),
							agvSampleTaskItem.getBoxId(), materialId);
					if (sampleTaskMaterialRecord != null) {
						throw new OperationException("该料盘尚未扫码，请先扫码后再出库！");
					}
					SampleOutRecord sampleOutRecord = new SampleOutRecord();
					sampleOutRecord.setMaterialId(materialId);
					sampleOutRecord.setSampleTaskItemId(sampleTaskItem.getId());
					sampleOutRecord.setOutType(SamplerOutType.SINGULAR);
					sampleOutRecord.setQuantity(material.getRemainderQuantity());
					sampleOutRecord.setBoxId(material.getBox());
					sampleOutRecord.setOperator(user.getUid());
					sampleOutRecord.setTime(new Date());
					sampleOutRecord.save();

					sampleTaskItem.setSingularOutQuantity(sampleTaskItem.getSingularOutQuantity() + material.getRemainderQuantity());
					sampleTaskItem.update();

					material.setRemainderQuantity(0);
					material.setCol(-1);
					material.setRow(-1);
					material.setIsInBox(false);
					material.update();
				}
			}
		}
		return "操作成功";
	}
	

	public String outRegular(String materialId, String groupId, User user) {
		synchronized (Lock.REGULAR_SAMPLE_TASK_SCAN_LOCK) {
			for (AGVSampleTaskItem agvSampleTaskItem : SampleTaskItemRedisDAO.getSampleTaskItems(Integer.valueOf(groupId.split("#")[1]))) {
				if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
					Material material = Material.dao.findById(materialId);
					if (material == null || material.getRemainderQuantity() <= 0 || !material.getIsInBox()) {
						throw new OperationException("料盘不存在或者已经出库！");
					}
					SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(SampleTaskSQL.GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, agvSampleTaskItem.getTaskId(), material.getType(),
							WarehouseType.REGULAR.getId());
					if (sampleTaskItem == null) {
						throw new OperationException("该料盘不在本次抽检范围内！");
					}
					if (!material.getBox().equals(agvSampleTaskItem.getBoxId())) {
						throw new OperationException("该料盘不在当前料盒内！");
					}
					SampleTaskMaterialRecord sampleTaskMaterialRecord = SampleTaskMaterialRecord.dao.findFirst(GET_REGULAR_UNSCAN_MATERIAL_BY_MATERIAL, agvSampleTaskItem.getTaskId(),
							agvSampleTaskItem.getBoxId(), materialId);
					if (sampleTaskMaterialRecord != null) {
						throw new OperationException("该料盘尚未扫码，请先扫码后再出库！");
					}
					SampleOutRecord sampleOutRecord = new SampleOutRecord();
					sampleOutRecord.setMaterialId(materialId);
					sampleOutRecord.setSampleTaskItemId(sampleTaskItem.getId());
					sampleOutRecord.setOutType(SamplerOutType.REGULAR);
					sampleOutRecord.setQuantity(material.getRemainderQuantity());
					sampleOutRecord.setBoxId(material.getBox());
					sampleOutRecord.setOperator(user.getUid());
					sampleOutRecord.setTime(new Date());
					sampleOutRecord.save();

					sampleTaskItem.setRegularOutQuantity(sampleTaskItem.getRegularOutQuantity() + material.getRemainderQuantity());
					sampleTaskItem.update();

					material.setRemainderQuantity(0);
					material.setCol(-1);
					material.setRow(-1);
					material.setIsInBox(false);
					material.setIsRepeated(true);
					material.update();
				}
			}
		}
		return "操作成功";
	}

	
	public String outLost(String materialId, String groupId, User user) {
		synchronized (Lock.REGULAR_SAMPLE_TASK_SCAN_LOCK) {
			for (AGVSampleTaskItem agvSampleTaskItem : SampleTaskItemRedisDAO.getSampleTaskItems(Integer.valueOf(groupId.split("#")[1]))) {
				if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
					Material material = Material.dao.findById(materialId);
					if (material == null || material.getRemainderQuantity() <= 0 || !material.getIsInBox()) {
						throw new OperationException("料盘不存在或者已经出库！");
					}
					SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(SampleTaskSQL.GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, agvSampleTaskItem.getTaskId(), material.getType(),
							WarehouseType.REGULAR.getId());
					if (sampleTaskItem == null) {
						throw new OperationException("该料盘不在本次抽检范围内！");
					}
					if (!material.getBox().equals(agvSampleTaskItem.getBoxId())) {
						throw new OperationException("该料盘不在当前料盒内！");
					}
					SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(SampleTaskSQL.GET_RECORD_BY_MATERIALID_TASK_BOX, materialId, agvSampleTaskItem.getTaskId(),
							agvSampleTaskItem.getBoxId());
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
					sampleOutRecord.setBoxId(material.getBox());
					sampleOutRecord.setOperator(user.getUid());
					sampleOutRecord.setTime(new Date());
					sampleOutRecord.save();

					sampleTaskItem.setLostOutQuantity(sampleTaskItem.getLostOutQuantity() + material.getRemainderQuantity());
					sampleTaskItem.update();

					material.setRemainderQuantity(0);
					material.setCol(-1);
					material.setRow(-1);
					material.setIsInBox(false);
					material.setIsRepeated(true);
					material.update();
				}
			}
		}
		return "操作成功";
	}

	
	public void sampleUWMaterial(String materialId, String groupId) {
		synchronized (Lock.REGULAR_SAMPLE_TASK_SCAN_LOCK) {
			Integer boxId = Integer.valueOf(groupId.split("#")[0]);
			Integer taskId = Integer.valueOf(groupId.split("#")[1]);
			Material material = Material.dao.findById(materialId);
			if (material == null) {
				throw new OperationException("料盘不存在！");
			}
			if (boxId == null || taskId == null) {
				throw new OperationException("groupId解析失败，获取不到任务ID和料盒号！");
			}
			SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(SampleTaskSQL.GET_RECORD_BY_MATERIALID_TASK_BOX, materialId, taskId, boxId);
			if (record == null) {
				throw new OperationException("本次抽检抽检范围不包含该料盘！");
			}
			if (record.getIsScaned()) {
				throw new OperationException("该料盘已经扫描过，请勿重复扫描！");
			} else {

				SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(SampleTaskSQL.GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, taskId, material.getType(), WarehouseType.REGULAR.getId());
				if (sampleTaskItem == null) {
					throw new OperationException("本次抽检抽检范围不包含该料盘！");
				}
				record.setIsScaned(true).update();
				sampleTaskItem.setScanQuantity(sampleTaskItem.getScanQuantity() + material.getRemainderQuantity());
				sampleTaskItem.update();
			}
		}

	}
	

	public List<PackingSampleInfoVO> getParkingSampleMaterialInfo(Integer windowId) {
		Integer boxId = 0;
		String groupId = "";
		Window window = Window.dao.findById(windowId);
		if (window == null || window.getBindTaskId() == null) {
			throw new OperationException("仓口不存在任务");
		}
		Map<Integer, PackingSampleInfoVO> map = new LinkedHashMap<>();
		List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, windowId);
		for (GoodsLocation goodsLocation : goodsLocations) {
			map.put(goodsLocation.getId(), new PackingSampleInfoVO(goodsLocation));
		}
		for (AGVSampleTaskItem agvSampleTaskItem : SampleTaskItemRedisDAO.getSampleTaskItems(window.getBindTaskId())) {
			if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getWindowId().equals(windowId)) {
				boxId = agvSampleTaskItem.getBoxId();
				groupId = agvSampleTaskItem.getGroupId();
				PackingSampleInfoVO info = map.get(agvSampleTaskItem.getGoodsLocationId());
				if (info == null) {
					throw new OperationException("仓口 " + windowId + "没有对应货位" + agvSampleTaskItem.getGoodsLocationId());
				}
				if (info.getBoxId() != null) {
					throw new OperationException("仓口 " + windowId + "的货位" + agvSampleTaskItem.getGoodsLocationId() + "有一个以上的到站任务条目，请检查!");
				}
				List<MaterialDetialsVO> materialInfoVOs = new ArrayList<>();

				List<Record> unOutRecords = Db.find(SQL.GET_REGULAR_UN_OUT_SAMPLE_TAKS_MATERIAL, boxId, window.getBindTaskId());
				Integer totalNum = unOutRecords.size();
				Integer scanNum = 0;
				for (Record record : unOutRecords) {
					MaterialDetialsVO materialInfoVO = new MaterialDetialsVO();
					materialInfoVO.setMaterialTypeId(record.getInt("material_type_id"));
					materialInfoVO.setMaterialId(record.getStr("id"));
					materialInfoVO.setNo(record.getStr("no"));
					materialInfoVO.setSpecification(record.getStr("specification"));
					materialInfoVO.setStoreNum(record.getInt("quantity"));
					materialInfoVO.setSupplierId(record.getInt("supplier_id"));
					materialInfoVO.setSupplierName(record.getStr("supplier_name"));
					materialInfoVO.setProductionTime(record.getDate("production_time"));
					materialInfoVO.setActualNum(0);
					materialInfoVO.setIsScaned(record.getBoolean("is_scaned"));
					materialInfoVO.setCol(record.getInt("col"));
					materialInfoVO.setRow(record.getInt("row"));
					materialInfoVO.setIsOuted(-1);
					materialInfoVOs.add(materialInfoVO);
					if (record.getBoolean("is_scaned")) {
						scanNum++;
					}
				}
				List<Record> OutRecords = Db.find(SQL.GET_REGULAR_OUT_SAMPLE_TASK_MATERIAL, boxId, window.getBindTaskId());
				totalNum += OutRecords.size();
				Integer outNum = 0;
				for (Record record : OutRecords) {
					MaterialDetialsVO materialInfoVO = new MaterialDetialsVO();
					materialInfoVO.setMaterialTypeId(record.getInt("material_type_id"));
					materialInfoVO.setMaterialId(record.getStr("id"));
					materialInfoVO.setNo(record.getStr("no"));
					materialInfoVO.setSpecification(record.getStr("specification"));
					materialInfoVO.setStoreNum(record.getInt("quantity"));
					materialInfoVO.setSupplierId(record.getInt("supplier_id"));
					materialInfoVO.setSupplierName(record.getStr("supplier_name"));
					materialInfoVO.setProductionTime(record.getDate("production_time"));
					materialInfoVO.setIsScaned(true);
					materialInfoVO.setIsOuted(record.getInt("out_type"));
					materialInfoVO.setActualNum(0);
					materialInfoVO.setCol(record.getInt("col"));
					materialInfoVO.setRow(record.getInt("row"));
					materialInfoVOs.add(materialInfoVO);
					outNum++;
				}
				scanNum = scanNum + outNum;
				info.setBoxId(boxId);
				info.setTaskId(window.getBindTaskId());
				info.setWindowId(windowId);
				info.setGroupId(groupId);
				info.setTotalNum(totalNum);
				info.setScanNum(scanNum);
				info.setOutNum(outNum);
				info.setList(materialInfoVOs);
			}
		}
		return new ArrayList<>(map.values());

	}

}
