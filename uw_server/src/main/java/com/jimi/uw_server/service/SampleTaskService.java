package com.jimi.uw_server.service;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVSampleTaskItem;
import com.jimi.uw_server.agv.handle.IOHandler;
import com.jimi.uw_server.annotation.Log;
import com.jimi.uw_server.constant.SampleTaskItemState;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.SampleSingularRecord;
import com.jimi.uw_server.model.SampleTaskItem;
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

/**
 * 
 * @author trjie
 * @createTime 2019年6月20日  下午5:20:35
 */

public class SampleTaskService {
	
	private  static final String GET_MATERIAL_TYPE_BY_NO_SQL = "SELECT * FROM material_type WHERE no = ? AND supplier = ? ";
	
	private  static final String GET_SAMPLETASKITEM_BY_TASK = "SELECT * FROM sample_task_item WHERE task_id = ?";
	
	private  static final String GET_SAMPLETASKITEM_BY_TASK_AND_TYPE = "SELECT * FROM sample_task_item WHERE task_id = ? AND material_type_id = ?";
	
	private  static final String GET_MATERIAL_BOX_BY_SAMPLE_TASK = "SELECT DISTINCT material_box.* FROM material_box JOIN material JOIN sample_task_item ON sample_task_item.material_type_id = material.type AND material_box.id = material.box WHERE sample_task_item.task_id = ? AND material.remainder_quantity > 0 AND material.is_in_box = 1";
	
	private static final String GET_PACKING_SAMPLE_ITEM_BY_BOX_AND_TASKID = "SELECT material.id as id, material.type as material_type_id, material.remainder_quantity as quantity, material.production_time as production_time, material_type.`no` as `no`, material_type.specification as specification, supplier.id as supplier_id, supplier.`name` as supplier_name FROM material INNER JOIN material_type INNER JOIN supplier INNER JOIN sample_task_item ON sample_task_item.material_type_id = material.type AND material.type = material_type.id AND supplier.id = material_type.supplier WHERE material.box = ? AND material.is_in_box = 1 AND remainder_quantity > 0 AND sample_task_item.task_id = ?";

	private static final String GET_WORKING_WINDOWS = "SELECT * FROM window WHERE bind_task_id IS NOT NULL";
	
	private static final String GET_SAMPLE_TASK_DETIALS = "SELECT sample_singular_record.id AS id, sample_task_item.task_id AS task_id, sample_task_item.id AS sample_task_item_id, sample_task_item.material_type_id AS material_type_id, sample_singular_record.material_id AS material_id, sample_singular_record.operator AS operator, sample_singular_record.quantity AS quantity, sample_singular_record.is_singular AS is_singular, sample_singular_record.time AS time, material_type.`no` AS `no` FROM sample_task_item INNER JOIN sample_singular_record INNER JOIN material_type ON sample_singular_record.sample_task_item_id = sample_task_item.id AND material_type.id = sample_task_item.material_type_id WHERE sample_task_item.task_id = ?";

	private static SelectService selectService = Enhancer.enhance(SelectService.class);
	
	public static SampleTaskService me = new SampleTaskService();
	
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
				HashSet<Integer> materialTypeSet = new HashSet<>();
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
		List<MaterialBox> materialBoxs = MaterialBox.dao.find(GET_MATERIAL_BOX_BY_SAMPLE_TASK, task.getId());
		List<AGVSampleTaskItem> agvSampleTaskItems = new ArrayList<>();
		for (MaterialBox materialBox : materialBoxs) {
			AGVSampleTaskItem agvSampleTaskItem = new AGVSampleTaskItem(taskId, materialBox.getId());
			agvSampleTaskItems.add(agvSampleTaskItem);
		}
		task.setState(TaskState.PROCESSING);
		task.setStartTime(new Date());
		task.update();
		if (!agvSampleTaskItems.isEmpty()) {
			TaskItemRedisDAO.addSampleTaskItem(agvSampleTaskItems);
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
					IOHandler.clearSampleTask(id);
					return true;
				}
			}

		}
	
	public String backBox(String groupId) {
		synchronized (Lock.SAMPLE_TASK_REDIS_LOCK) {
			for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems()) {
				if (agvSampleTaskItem.getState().equals(SampleTaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
					MaterialBox materialBox = MaterialBox.dao.findById(agvSampleTaskItem.getBoxId());
					Window window = Window.dao.findById(agvSampleTaskItem.getWindowId());
					try {
						IOHandler.sendSL(agvSampleTaskItem, materialBox, window);
						TaskItemRedisDAO.updateSampleTaskIsForceFinish(agvSampleTaskItem, true);
						TaskItemRedisDAO.updateSampleTaskItemState(agvSampleTaskItem, SampleTaskItemState.START_BACK);
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
			for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems()) {
				if (agvSampleTaskItem.getState().equals(SampleTaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
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
					SampleSingularRecord sampleSingularRecord = new SampleSingularRecord();
					sampleSingularRecord.setMaterialId(materialId);
					sampleSingularRecord.setSampleTaskItemId(sampleTaskItem.getId());
					sampleSingularRecord.setIsSingular(true);
					sampleSingularRecord.setQuantity(material.getRemainderQuantity());
					sampleSingularRecord.setOperator(user.getUid());
					sampleSingularRecord.setTime(new Date());
					sampleSingularRecord.save();
					
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
			for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems()) {
				if (agvSampleTaskItem.getState().equals(SampleTaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getGroupId().equals(groupId)) {
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
					SampleSingularRecord sampleSingularRecord = new SampleSingularRecord();
					sampleSingularRecord.setMaterialId(materialId);
					sampleSingularRecord.setSampleTaskItemId(sampleTaskItem.getId());
					sampleSingularRecord.setIsSingular(false);
					sampleSingularRecord.setQuantity(material.getRemainderQuantity());
					sampleSingularRecord.setOperator(user.getUid());
					sampleSingularRecord.setTime(new Date());
					sampleSingularRecord.save();
					
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
	
	
	public PackingSampleInfoVO getPackingSampleMaterialInfo(Integer windowId) {
		Integer boxId = 0;
		String groupId = "";
		Window window = Window.dao.findById(windowId);
		for (AGVSampleTaskItem agvSampleTaskItem : TaskItemRedisDAO.getSampleTaskItems()) {
			if (agvSampleTaskItem.getState().equals(SampleTaskItemState.ARRIVED_WINDOW) && agvSampleTaskItem.getWindowId().equals(windowId) && agvSampleTaskItem.getTaskId().equals(window.getBindTaskId())) {
				boxId = agvSampleTaskItem.getBoxId();
				groupId = agvSampleTaskItem.getGroupId();
				break;
			}
		}
		if (boxId == 0) {
			return null;
		}
		List<MaterialInfoVO> materialInfoVOs = new ArrayList<>();
		List<Record> records = Db.find(GET_PACKING_SAMPLE_ITEM_BY_BOX_AND_TASKID, boxId, window.getBindTaskId());
		for (Record record : records) {
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
			materialInfoVOs.add(materialInfoVO);
		}
		PackingSampleInfoVO info = new PackingSampleInfoVO();
		info.setBoxId(boxId);
		info.setTaskId(window.getBindTaskId());
		info.setWindowId(windowId);
		info.setGroupId(groupId);
		info.setList(materialInfoVOs);
		return info;
		
	}
	
	
	public PagePaginate getSampleTaskDetials(Integer taskId, Integer pageSize, Integer pageNo) {
		
		SqlPara sqlPara = new SqlPara();
		sqlPara.setSql(GET_SAMPLE_TASK_DETIALS);
		sqlPara.addPara(taskId);
		Page<Record> page = null;
		if (pageNo == null && pageSize == null) {
			page = Db.paginate(1, PropKit.use("properties.ini").getInt("defaultPageSize"), sqlPara);
		}else {
			page = Db.paginate(pageNo, pageSize, sqlPara);
		}
		List<SampleTaskDetialsVO> sampleTaskDetialsVOs = SampleTaskDetialsVO.fillList(page.getList());
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageNumber(page.getPageNumber());
		pagePaginate.setPageSize(page.getPageSize());
		pagePaginate.setTotalPage(page.getTotalPage());
		pagePaginate.setTotalRow(page.getTotalRow());
		pagePaginate.setList(sampleTaskDetialsVOs);
		return pagePaginate;
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
		for (Record res : result.getList()) {
			TaskVO t = new TaskVO(res.get("id"), res.get("state"), res.get("type"), res.get("file_name"), res.get("create_time"), res.get("priority"), res.get("supplier"), res.get("remarks"));
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

	@Log("设置仓口{windowId}所绑定的叉车{robots}")
	public String setWindowRobots(Integer windowId, String robots) {
		Window window = Window.dao.findById(windowId);
		if (window == null || window.getBindTaskId() == null) {
			throw new OperationException("当前仓口无任务运行！");
		}
		List<String> robotTempList =  new ArrayList<>();
		String result = "";
		if (robots != null && !robots.trim().equals("")) {
			String[] robotArr = robots.split(",");
			List<Window> windows = Window.dao.find(GET_WORKING_WINDOWS);
			//查找冲突的叉车
			synchronized (Lock.ROBOT_TASK_REDIS_LOCK) {
			
				for (Window windowTemp : windows) {
					if (windowTemp.getId().equals(window.getId())) {
						continue;
					}
					String rbTemp = TaskItemRedisDAO.getWindowTaskInfo(windowTemp.getId(), windowTemp.getBindTaskId());
					if (rbTemp != null && !rbTemp.equals(IOHandler.UNDEFINED) && !rbTemp.trim().equals("")) {
						String[] rbTempArr = rbTemp.split(",");
						for (String rbTempStr : rbTempArr) {
							for (String robotStr : robotArr) {
								if (rbTempStr.trim().equals(robotStr.trim())) {
									if (!result.equals("")) {
										result += "，";
									}
									robotTempList.add(robotStr.trim());
									result += "叉车" + rbTempStr + "已被仓口" + windowTemp.getId() + "使用，请前往对应仓口解绑";
	
								}
							}
						}
					}
					
				}
				List<String> robotArrList = new ArrayList<>();
				for (String robotStr : robotArr) {
					robotArrList.add(robotStr.trim());
				}
				if (robotTempList.size() == 0) {
					TaskItemRedisDAO.setWindowTaskInfo(windowId, window.getBindTaskId(), robots);
				}else {
					for (String sameRobot : robotTempList) {
						if (robotArrList.contains(sameRobot)) {
							robotArrList.remove(sameRobot);
						}
					}
					String insertRobots = "";
					for (String string : robotArrList) {
						if (!insertRobots.equals("")) {
							insertRobots += ",";
						}
						insertRobots += string;
					}
					if (!insertRobots.equals("")) {
						TaskItemRedisDAO.setWindowTaskInfo(windowId, window.getBindTaskId(), insertRobots);
					}
				}
			}	
			
		}else {
			TaskItemRedisDAO.delWindowTaskInfo(windowId, window.getBindTaskId());
		}
		if (!result.equals("")) {
			return result;
		}
		return "操作成功";
	}
	
	
	public String getWindowRobots(Integer windowId) {
		String result = "undefined";
		Window window = Window.dao.findById(windowId);
		Task task = Task.dao.findById(window.getBindTaskId());
		if (task != null) {
			synchronized (Lock.ROBOT_TASK_REDIS_LOCK) {
				result = TaskItemRedisDAO.getWindowTaskInfo(windowId, window.getBindTaskId());
			}
		}
		return result;
	}
}
