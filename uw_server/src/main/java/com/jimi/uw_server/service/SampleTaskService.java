package com.jimi.uw_server.service;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.jfinal.aop.Aop;
import com.jfinal.aop.Enhancer;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.agv.handle.SamTaskHandler;
import com.jimi.uw_server.constant.SQL;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.SampleSingularRecord;
import com.jimi.uw_server.model.SampleTaskItem;
import com.jimi.uw_server.model.SampleTaskMaterialRecord;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.SampleTaskItemBO;
import com.jimi.uw_server.model.vo.MaterialInfoVO;
import com.jimi.uw_server.model.vo.PackingSampleInfoVO;
import com.jimi.uw_server.model.vo.SampleTaskDetialsVO;
import com.jimi.uw_server.model.vo.TaskVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ExcelHelper;
import com.jimi.uw_server.util.ExcelWritter;

/**
 * 
 * @author trjie
 * @createTime 2019年6月20日  下午5:20:35
 */

public class SampleTaskService {
	
	private static final String GET_MATERIAL_TYPE_BY_NO_SQL = "SELECT * FROM material_type WHERE no = ? AND supplier = ? ";
	
	private static final String GET_SAMPLETASKITEM_BY_TASK = "SELECT * FROM sample_task_item WHERE task_id = ?";
	
	private static final String GET_SAMPLETASKITEM_BY_TASK_AND_TYPE = "SELECT * FROM sample_task_item WHERE task_id = ? AND material_type_id = ?";
	
	private static final String GET_MATERIAL_BOX_BY_SAMPLE_TASK = "SELECT DISTINCT material_box.* FROM material_box JOIN material JOIN sample_task_item ON sample_task_item.material_type_id = material.type AND material_box.id = material.box WHERE sample_task_item.task_id = ? AND material.remainder_quantity > 0 AND material.is_in_box = 1";
	
	private static final String GET_MATERIAL_BY_SAMPLE_TASK = "SELECT DISTINCT material.* FROM material_box JOIN material JOIN sample_task_item ON sample_task_item.material_type_id = material.type AND material_box.id = material.box WHERE sample_task_item.task_id = ? AND material.remainder_quantity > 0 AND material.is_in_box = ?";
		
	private static final String GET_SAMPLE_TASK_DETIALS = "SELECT a.*, sample_singular_record.id AS id, sample_singular_record.material_id AS material_id, sample_singular_record.operator AS operator, sample_singular_record.quantity AS quantity, sample_singular_record.is_singular AS is_singular, sample_singular_record.time AS time FROM ( SELECT sample_task_item.task_id AS task_id, sample_task_item.id AS sample_task_item_id, sample_task_item.material_type_id AS material_type_id, sample_task_item.store_quantity AS store_quantity, sample_task_item.scan_quantity AS scan_quantity, sample_task_item.regular_out_quantity AS regular_out_quantity, sample_task_item.singular_out_quantity AS singular_out_quantity, material_type.`no` AS `no` FROM sample_task_item INNER JOIN material_type ON material_type.id = sample_task_item.material_type_id WHERE sample_task_item.task_id = ? ) a LEFT JOIN sample_singular_record ON sample_singular_record.sample_task_item_id = a.sample_task_item_id ORDER BY a.sample_task_item_id";

	private static final String GET_RECORD_BY_MATERIALID_TASK_BOX = "SELECT * FROM sample_task_material_record WHERE material_id = ? AND task_id = ? AND box_id = ?";
	
	private static final String GET_UNSCAN_MATERIAL = "SELECT * FROM sample_task_material_record WHERE task_id = ? AND box_id = ? AND is_scaned = 0";
	
	private static final String GET_UNSCAN_MATERIAL_BY_MATERIAL = "SELECT * FROM sample_task_material_record WHERE task_id = ? AND box_id = ? AND material_id = ? AND is_scaned = 0";
	
	private static final String GET_EXPROT_SAMPLE_TASK_INFO = "SELECT sample_task_item.task_id AS task_id, sample_task_item.id AS sample_task_item_id, sample_task_item.material_type_id AS material_type_id, sample_task_item.store_quantity AS store_quantity, sample_task_item.scan_quantity AS scan_quantity, sample_task_item.regular_out_quantity AS regular_out_quantity, sample_task_item.singular_out_quantity AS singular_out_quantity, material_type.`no` AS `no`, supplier.`name` AS `supplier_name` FROM sample_task_item INNER JOIN material_type INNER JOIN supplier ON material_type.id = sample_task_item.material_type_id AND material_type.supplier = supplier.id WHERE sample_task_item.task_id = ? ORDER BY sample_task_item.id";
	
	private static SelectService selectService = Enhancer.enhance(SelectService.class);
	
	private static SamTaskHandler samTaskHandler = SamTaskHandler.getInstance();
	
	public static SampleTaskService me = new SampleTaskService();
	
	private static MaterialService materialService = Aop.get(MaterialService.class);
	
	private static Object CANCEL_LOCK = new Object();
	
	public String createSampleTask(File file, Integer supplierId, String remarks) {
		String resultString = "操作成功";
		synchronized (Lock.IMPORT_SAMPLE_TASK_FILE_LOCK) {
			try {
				ExcelHelper excelHelper = ExcelHelper.from(file);
				List<SampleTaskItemBO> sampleTaskItemBOs = excelHelper.unfill(SampleTaskItemBO.class, 0);
				if (sampleTaskItemBOs == null || sampleTaskItemBOs.size() == 0) {
			  		throw new OperationException("创建任务失败，请检查表格的表头是否正确以及表格中是否有效的任务记录！");
				}
				HashSet<Integer> materialTypeSet = new LinkedHashSet<>();
				for (int i = 0 ; i < sampleTaskItemBOs.size(); i++) {
					SampleTaskItemBO item = sampleTaskItemBOs.get(i);
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) {
						if (item.getNo() == null || item.getNo().replaceAll(" ", "").equals("")) {
							throw new OperationException("创建任务失败，请检查套料单表格第" + i + "行的料号是否填写了准确信息！");
						}
		
						// 根据料号找到对应的物料类型
						MaterialType mType = MaterialType.dao.findFirst(GET_MATERIAL_TYPE_BY_NO_SQL, item.getNo(), supplierId);
						// 判断物料类型表中是否存在对应的料号且未被禁用，若不存在，则将对应的任务记录删除掉，并提示操作员检查套料单、新增对应的物料类型
						if (mType == null) {
							throw new OperationException("插入套料单失败，料号为" + item.getNo() + "的物料没有记录在物料类型表中或已被禁用，或者是供应商与料号对应不上！");
						}
						materialTypeSet.add(mType.getId());
					}else {
						break;
					}
				}
				if (materialTypeSet.isEmpty()) {
					throw new OperationException("创建任务失败，请检查表格中是否有效的任务记录！");
				}
				Date date = new Date();
				Task task = new Task();
				task.setFileName(getSampleTaskName(date));
				task.setSupplier(supplierId);
				task.setCreateTime(date);
				task.setRemarks(remarks);
				task.setType(TaskType.SAMPLE);
				task.setState(TaskState.WAIT_START);
				task.save();
				for (Integer materialTypeId : materialTypeSet) {
					SampleTaskItem sampleTaskItem = new SampleTaskItem();
					sampleTaskItem.setMaterialTypeId(materialTypeId);
					sampleTaskItem.setTaskId(task.getId());
					sampleTaskItem.setScanQuantity(0);
					sampleTaskItem.setStoreQuantity(0);
					sampleTaskItem.setRegularOutQuantity(0);
					sampleTaskItem.setSingularOutQuantity(0);
					sampleTaskItem.save();
				}
			} catch (Exception e) {
				
				e.printStackTrace();
				throw new OperationException(e.getMessage());
			}
		}
		return resultString;
		
	}
	
	
	public String start(Integer taskId, String windows) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getType().equals(TaskType.SAMPLE) || !task.getState().equals(TaskState.WAIT_START)) {
			throw new OperationException("抽检任务不存在或并未处于未开始状态，无法开始任务！");
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
		Material material = Material.dao.findFirst(GET_MATERIAL_BY_SAMPLE_TASK, task.getId(), false);
		if (material != null) {
			throw new OperationException("该抽检任务所抽检的物料类型存在不在料盒内的物料，请检查！");
		}
		List<Material> materials = Material.dao.find(GET_MATERIAL_BY_SAMPLE_TASK, task.getId(), true);
		for (Material material2 : materials) {
			SampleTaskMaterialRecord record = new SampleTaskMaterialRecord();
			record.setMaterialId(material2.getId());
			record.setBoxId(material2.getBox());
			record.setTaskId(task.getId());
			record.save();
		}
		List<MaterialBox> materialBoxs = MaterialBox.dao.find(GET_MATERIAL_BOX_BY_SAMPLE_TASK, task.getId());
		List<AGVSampleTaskItem> agvSampleTaskItems = new ArrayList<>();
		for (MaterialBox materialBox : materialBoxs) {
			AGVSampleTaskItem agvSampleTaskItem = new AGVSampleTaskItem(taskId, materialBox.getId());
			agvSampleTaskItems.add(agvSampleTaskItem);
		}
		List<SampleTaskItem> sampleTaskItems = SampleTaskItem.dao.find(GET_SAMPLETASKITEM_BY_TASK, task.getId());
		//填写本次抽检库存
		for (SampleTaskItem sampleTaskItem : sampleTaskItems) {
			sampleTaskItem.setStoreQuantity(materialService.countAndReturnRemainderQuantityByMaterialTypeId(sampleTaskItem.getMaterialTypeId()));
			sampleTaskItem.update();
		}
		task.setState(TaskState.PROCESSING);
		task.setStartTime(new Date());
		task.update();
		//绑定仓口
		if (!agvSampleTaskItems.isEmpty()) {
			TaskItemRedisDAO.addSampleTaskItem(taskId, agvSampleTaskItems);
			for (Window window : wList) {
				window.setBindTaskId(task.getId());
				window.update();
			}
		}else {
			task.setState(TaskState.FINISHED);
			task.setEndTime(new Date());
			task.update();
		}
		
		return "操作成功！";
		
	}
	
	
	// 作废指定任务
	public boolean cancel(Integer id) {
		synchronized (CANCEL_LOCK) {
			Task task = Task.dao.findById(id);
			int state = task.getState();
			// 对于已完成的任务，禁止作废
			if (state == TaskState.FINISHED) {
				throw new OperationException("该任务已完成，禁止作废！");
			} else if (state == TaskState.CANCELED) {	// 对于已作废过的任务，禁止作废
				throw new OperationException("该任务已作废！");
			} else {
				// 判断任务是否处于进行中状态，若是，则把相关的任务条目从til中剔除;若存在已分配的任务条目，则不解绑任务仓口
				if (state == TaskState.PROCESSING) {
					TaskItemRedisDAO.removeUnAssignedSampleTaskItemByTaskId(id);
				}
				// 更新任务状态为作废
				task.setState(TaskState.CANCELED).update();
				samTaskHandler.clearTask(task.getId());
				return true;
			}
		}
	}
	
	public String backBox(String groupId) {
		synchronized (Lock.SAMPLE_TASK_REDIS_LOCK) {
			for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems(Integer.valueOf(groupId.split("#")[1]))) {
				if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
					MaterialBox materialBox = MaterialBox.dao.findById(agvSampleTaskItem.getBoxId());
					SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(GET_UNSCAN_MATERIAL, agvSampleTaskItem.getTaskId(), agvSampleTaskItem.getBoxId());
					if (record != null) {
						throw new OperationException("存在尚未扫描的料盘，回库失败！");
					}
					try {
						GoodsLocation goodsLocation = GoodsLocation.dao.findById(agvSampleTaskItem.getGoodsLocationId());
						if (goodsLocation != null) {
							samTaskHandler.sendBackLL(agvSampleTaskItem, materialBox, goodsLocation);
						}else {
							throw new OperationException("找不到目的货位，仓口：" + agvSampleTaskItem.getWindowId() + "货位：" + agvSampleTaskItem.getGoodsLocationId());
						}
						TaskItemRedisDAO.updateSampleTaskItemInfo(agvSampleTaskItem, TaskItemState.START_BACK, null, null, null, true);
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
		synchronized(Lock.SAMPLE_TASK_OUT_LOCK) {
			for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems(Integer.valueOf(groupId.split("#")[1]))) {
				if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
					Material material = Material.dao.findById(materialId);
					if (material == null || material.getRemainderQuantity() <= 0 || !material.getIsInBox()) {
						throw new OperationException("料盘不存在或者已经出库！");
					}
					SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, agvSampleTaskItem.getTaskId(), material.getType());
					if (sampleTaskItem == null) {
						throw new OperationException("该料盘不在本次抽检范围内！");
					}
					if (!material.getBox().equals(agvSampleTaskItem.getBoxId())) {
						throw new OperationException("该料盘不在当前料盒内！");
					}
					SampleTaskMaterialRecord sampleTaskMaterialRecord = SampleTaskMaterialRecord.dao.findFirst(GET_UNSCAN_MATERIAL_BY_MATERIAL, agvSampleTaskItem.getTaskId(), agvSampleTaskItem.getBoxId(), materialId);
					if (sampleTaskMaterialRecord != null) {
						throw new OperationException("该料盘尚未扫码，请先扫码后再出库！");
					}
					SampleSingularRecord sampleSingularRecord = new SampleSingularRecord();
					sampleSingularRecord.setMaterialId(materialId);
					sampleSingularRecord.setSampleTaskItemId(sampleTaskItem.getId());
					sampleSingularRecord.setIsSingular(true);
					sampleSingularRecord.setQuantity(material.getRemainderQuantity());
					sampleSingularRecord.setBoxId(material.getBox());
					sampleSingularRecord.setOperator(user.getUid());
					sampleSingularRecord.setTime(new Date());
					sampleSingularRecord.save();
					
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
		synchronized(Lock.SAMPLE_TASK_OUT_LOCK) {
			for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems(Integer.valueOf(groupId.split("#")[1]))) {
				if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
					Material material = Material.dao.findById(materialId);
					if (material == null || material.getRemainderQuantity() <= 0 || !material.getIsInBox()) {
						throw new OperationException("料盘不存在或者已经出库！");
					}
					SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, agvSampleTaskItem.getTaskId(), material.getType());
					if (sampleTaskItem == null) {
						throw new OperationException("该料盘不在本次抽检范围内！");
					}
					if (!material.getBox().equals(agvSampleTaskItem.getBoxId())) {
						throw new OperationException("该料盘不在当前料盒内！");
					}
					SampleTaskMaterialRecord sampleTaskMaterialRecord = SampleTaskMaterialRecord.dao.findFirst(GET_UNSCAN_MATERIAL_BY_MATERIAL, agvSampleTaskItem.getTaskId(), agvSampleTaskItem.getBoxId(), materialId);
					if (sampleTaskMaterialRecord != null) {
						throw new OperationException("该料盘尚未扫码，请先扫码后在再出库！");
					}
					SampleSingularRecord sampleSingularRecord = new SampleSingularRecord();
					sampleSingularRecord.setMaterialId(materialId);
					sampleSingularRecord.setSampleTaskItemId(sampleTaskItem.getId());
					sampleSingularRecord.setIsSingular(false);
					sampleSingularRecord.setQuantity(material.getRemainderQuantity());
					sampleSingularRecord.setBoxId(material.getBox());
					sampleSingularRecord.setOperator(user.getUid());
					sampleSingularRecord.setTime(new Date());
					sampleSingularRecord.save();
					
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
	
	
	public void scanMaterial(String materialId, String groupId) {
		
		Integer boxId = Integer.valueOf(groupId.split("#")[0]);
		Integer taskId = Integer.valueOf(groupId.split("#")[1]);
		Material material = Material.dao.findById(materialId);
		if (material == null) {
			throw new OperationException("料盘不存在！");
		}
		if (boxId == null || taskId == null) {
			throw new OperationException("groupId解析失败，获取不到任务ID和料盒号！");
		}
		SampleTaskMaterialRecord record = SampleTaskMaterialRecord.dao.findFirst(GET_RECORD_BY_MATERIALID_TASK_BOX, materialId, taskId, boxId);
		if (record == null) {
			throw new OperationException("本次抽检抽检范围不包含该料盘！");
		}
		if (record.getIsScaned()) {
			throw new OperationException("该料盘已经扫描过，请勿重复扫描！");
		}else {
			record.setIsScaned(true).update();
			SampleTaskItem sampleTaskItem = SampleTaskItem.dao.findFirst(GET_SAMPLETASKITEM_BY_TASK_AND_TYPE, taskId, material.getType());
			sampleTaskItem.setScanQuantity(sampleTaskItem.getScanQuantity() + material.getRemainderQuantity());
			sampleTaskItem.update();
		}
		
	}
	
	
	public List<PackingSampleInfoVO> getPackingSampleMaterialInfo(Integer windowId) {
		Integer boxId = 0;
		String groupId = "";
		Window window = Window.dao.findById(windowId);
		if (window == null || window.getBindTaskId() == null) {
			throw new OperationException("当前仓口没有绑定任务！");
		}
		Map<Integer, PackingSampleInfoVO> map = new LinkedHashMap<>();
		List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOWID, windowId);
		for (GoodsLocation goodsLocation : goodsLocations) {
			map.put(goodsLocation.getId(), new PackingSampleInfoVO(goodsLocation));
		}
		for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems(window.getBindTaskId())) {
			if (agvSampleTaskItem.getState().equals(TaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getWindowId().equals(windowId)) {
				boxId = agvSampleTaskItem.getBoxId();
				groupId = agvSampleTaskItem.getGroupId();
				PackingSampleInfoVO info = map.get(agvSampleTaskItem.getGoodsLocationId());
				if (info == null ) {
					throw new OperationException("仓口 " + windowId + "没有对应货位" + agvSampleTaskItem.getGoodsLocationId());
				}
				if (info.getBoxId() != null) {
					throw new OperationException("仓口 " + windowId + "的货位" + agvSampleTaskItem.getGoodsLocationId() + "有一个以上的到站任务条目，请检查!");
				}
				List<MaterialInfoVO> materialInfoVOs = new ArrayList<>();
				
				List<Record> unOutRecords = Db.find(SQL.GET_UN_OUT_SAMPLE_TAKS_MATERIAL, boxId, window.getBindTaskId());
				Integer totalNum = unOutRecords.size();
				Integer scanNum = 0;
				for (Record record : unOutRecords) {
					MaterialInfoVO materialInfoVO = new MaterialInfoVO();
					materialInfoVO.setMaterailTypeId(record.getInt("material_type_id"));
					materialInfoVO.setMaterialId(record.getStr("id"));
					materialInfoVO.setNo(record.getStr("no"));
					materialInfoVO.setSpecification(record.getStr("specification"));
					materialInfoVO.setStoreNum(record.getInt("quantity"));
					materialInfoVO.setSupplierId(record.getInt("supplier_id"));
					materialInfoVO.setSupplier(record.getStr("supplier_name"));
					materialInfoVO.setProductionTime(record.getDate("production_time"));
					materialInfoVO.setActualNum(0);
					materialInfoVO.setIsScaned(record.getBoolean("is_scaned"));
					materialInfoVO.setIsOuted(false);
					materialInfoVOs.add(materialInfoVO);
					if (record.getBoolean("is_scaned")) {
						scanNum ++;
					}
				}
				List<Record> OutRecords = Db.find(SQL.GET_OUT_SAMPLE_TASK_MATERIAL, boxId, window.getBindTaskId());
				totalNum += OutRecords.size();
				Integer outNum = 0;
				for (Record record : OutRecords) {
					MaterialInfoVO materialInfoVO = new MaterialInfoVO();
					materialInfoVO.setMaterailTypeId(record.getInt("material_type_id"));
					materialInfoVO.setMaterialId(record.getStr("id"));
					materialInfoVO.setNo(record.getStr("no"));
					materialInfoVO.setSpecification(record.getStr("specification"));
					materialInfoVO.setStoreNum(record.getInt("quantity"));
					materialInfoVO.setSupplierId(record.getInt("supplier_id"));
					materialInfoVO.setSupplier(record.getStr("supplier_name"));
					materialInfoVO.setProductionTime(record.getDate("production_time"));
					materialInfoVO.setIsScaned(true);
					materialInfoVO.setIsOuted(true);
					materialInfoVO.setActualNum(0);
					materialInfoVOs.add(materialInfoVO);
					outNum ++;
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
	
	
	public List<SampleTaskDetialsVO> getSampleTaskDetials(Integer taskId) {
		
		SqlPara sqlPara = new SqlPara();
		sqlPara.setSql(GET_SAMPLE_TASK_DETIALS);
		sqlPara.addPara(taskId);
		List<Record> records = Db.find( sqlPara);
		List<SampleTaskDetialsVO> sampleTaskDetialsVOs = SampleTaskDetialsVO.fillList(records);
		return sampleTaskDetialsVOs;
	}
	
	
	// 查询所有任务
	public Object select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		if (filter == null) {
			filter = "task.type=7";
		}else {
			filter = filter +  "#&#task.type=7";
		}
		Page<Record> result = selectService.select("task", pageNo, pageSize, ascBy, descBy, filter);
		List<TaskVO> taskVOs = new ArrayList<TaskVO>();
		Boolean status;
		for (Record res : result.getList()) {
			status  = false;
			if (res.getInt("state").equals(TaskState.PROCESSING)) {
				status = TaskItemRedisDAO.getTaskStatus(res.getInt("id"));
			}
			TaskVO t = new TaskVO(res.get("id"), res.get("state"), res.get("type"), res.get("file_name"), res.get("create_time"), res.get("priority"), res.get("supplier"), res.get("remarks"), status);
			taskVOs.add(t);
		}

		// 分页，设置页码，每页显示条目等
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(taskVOs);

		return pagePaginate;
	}
		
	
	public String getSampleTaskName(Date date) {
		
		String fileName = "抽检_";
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = formatter.format(date);
		fileName = fileName + time;
		return fileName;
	}

	
	public void exportSampleTaskInfo(Integer taskId, String fileName, OutputStream output) {
		try {
			List<Record> records = Db.find(GET_EXPROT_SAMPLE_TASK_INFO, taskId);
			String[] field = null;
			String[] head = null;
			field = new String[] {"supplier_name", "no", "store_quantity", "scan_quantity", "regular_out_quantity", "singular_out_quantity"};
			head =  new String[] {"供应商", "料号", "库存数量", "抽检数量", "抽检出库数量", "异常出库数量"};	
			ExcelWritter writter = ExcelWritter.create(true);
			writter.fill(records, fileName, field, head);
			writter.write(output, true);
		} catch (IOException e) {
			e.printStackTrace();
			throw new OperationException("下载失败" + e.getMessage());
		}
	}
	
	public String getTaskName(Integer taskId) {
		
		Task task = Task.dao.findById(taskId);
		if (task == null) {
			throw new OperationException("任务不存在!");
		}
		return task.getFileName();
	}
}
