/**  
*  
*/
package com.jimi.uw_server.service.base;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jimi.uw_server.agv.gaitek.dao.SampleTaskItemRedisDAO;
import com.jimi.uw_server.agv.gaitek.dao.TaskPropertyRedisDAO;
import com.jimi.uw_server.constant.MaterialStatus;
import com.jimi.uw_server.constant.TaskState;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.constant.sql.MaterialTypeSQL;
import com.jimi.uw_server.constant.sql.SQL;
import com.jimi.uw_server.constant.sql.SampleTaskSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.SampleTaskItem;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.SampleTaskItemBO;
import com.jimi.uw_server.model.vo.MaterialDetialsVO;
import com.jimi.uw_server.model.vo.PackingSampleInfoVO;
import com.jimi.uw_server.model.vo.SampleTaskDetialsVO;
import com.jimi.uw_server.model.vo.TaskVO;
import com.jimi.uw_server.util.ExcelHelper;
import com.jimi.uw_server.util.ExcelWritter;
import com.jimi.uw_server.util.PagePaginate;

/**
 * <p>
 * Title: BaseSampleTaskService
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
public class BaseSampleTaskService {

	private static SelectService selectService = Aop.get(SelectService.class);


	public void createSampleTask(File file, Integer supplierId, String remarks, Integer warehouseType) {

		Supplier supplier = Supplier.dao.findById(supplierId);
		if (supplier == null) {
			throw new OperationException("客户不存在！");
		}
		try {
			ExcelHelper excelHelper = ExcelHelper.from(file);
			List<SampleTaskItemBO> sampleTaskItemBOs = excelHelper.unfill(SampleTaskItemBO.class, 0);
			if (sampleTaskItemBOs == null || sampleTaskItemBOs.size() == 0) {
				throw new OperationException("创建任务失败，请检查表格的表头是否正确以及表格中是否有效的任务记录！");
			}
			HashSet<Integer> materialTypeSet = new LinkedHashSet<>();
			for (int i = 0; i < sampleTaskItemBOs.size(); i++) {
				SampleTaskItemBO item = sampleTaskItemBOs.get(i);
				if (item.getSerialNumber() != null && item.getSerialNumber() > 0) {
					if (item.getNo() == null || item.getNo().replaceAll(" ", "").equals("")) {
						throw new OperationException("创建任务失败，请检查套料单表格第" + i + "行的料号是否填写了准确信息！");
					}
					String no = item.getNo().trim().toUpperCase();
					// 根据料号找到对应的物料类型
					MaterialType mType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_AND_TYPE_SQL, no, supplierId, warehouseType);
					// 判断物料类型表中是否存在对应的料号且未被禁用，若不存在，则将对应的任务记录删除掉，并提示操作员检查套料单、新增对应的物料类型
					if (mType == null) {
						throw new OperationException("插入套料单失败，料号为" + item.getNo() + "的物料没有记录在物料类型表中或已被禁用，或者是客户与料号对应不上！");
					}
					materialTypeSet.add(mType.getId());
				} else {
					break;
				}
			}
			if (materialTypeSet.isEmpty()) {
				throw new OperationException("创建任务失败，请检查表格中是否有效的任务记录！");
			}
			Date date = new Date();
			Task task = new Task();
			task.setFileName(getSampleTaskName(date, warehouseType, supplier.getName()));
			task.setSupplier(supplierId);
			task.setCreateTime(date);
			task.setRemarks(remarks);
			task.setType(TaskType.SAMPLE);
			task.setState(TaskState.WAIT_START);
			task.setWarehouseType(warehouseType);
			task.save();
			for (Integer materialTypeId : materialTypeSet) {
				SampleTaskItem sampleTaskItem = new SampleTaskItem();
				sampleTaskItem.setMaterialTypeId(materialTypeId);
				sampleTaskItem.setTaskId(task.getId());
				sampleTaskItem.setScanQuantity(0);
				sampleTaskItem.setStoreQuantity(0);
				sampleTaskItem.setRegularOutQuantity(0);
				sampleTaskItem.setSingularOutQuantity(0);
				sampleTaskItem.setLostOutQuantity(0);
				sampleTaskItem.save();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new OperationException(e.getMessage());
		}
	}


	public PackingSampleInfoVO getSampleTaskMaterialInfo(Integer taskId) {
		PackingSampleInfoVO infoVO = new PackingSampleInfoVO();
		List<MaterialDetialsVO> materialInfoVOs = new ArrayList<>();
		List<Record> unOutRecords = Db.find(SQL.GET_PRECIOUS_UN_OUT_SAMPLE_TAKS_MATERIAL, MaterialStatus.NORMAL, taskId);
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
		List<Record> OutRecords = Db.find(SQL.GET_OUT_SAMPLE_TASK_MATERIAL, taskId);
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

		infoVO.setTaskId(taskId);
		infoVO.setTotalNum(totalNum);
		infoVO.setScanNum(scanNum);
		infoVO.setOutNum(outNum);
		infoVO.setList(materialInfoVOs);
		return infoVO;

	}


	public List<SampleTaskDetialsVO> getSampleTaskDetials(Integer taskId) {

		SqlPara sqlPara = new SqlPara();
		sqlPara.setSql(SampleTaskSQL.GET_SAMPLE_TASK_DETIALS);
		sqlPara.addPara(taskId);
		List<Record> records = Db.find(sqlPara);
		List<SampleTaskDetialsVO> sampleTaskDetialsVOs = SampleTaskDetialsVO.fillList(records);
		return sampleTaskDetialsVOs;
	}


	// 查询所有任务
	public Object select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		if (filter == null) {
			filter = "task.type=7";
		} else {
			filter = filter + "#&#task.type=7";
		}
		Page<Record> result = selectService.select(new String[] { "task", "supplier" }, new String[] { "task.supplier=supplier.id" }, pageNo, pageSize, ascBy, descBy, filter);
		List<Window> windows = Window.dao.find(SQL.GET_WORKING_WINDOWS);
		Set<Integer> windowBindTaskSet = new HashSet<>();
		if (!windows.isEmpty()) {
			for (Window window : windows) {
				windowBindTaskSet.add(window.getBindTaskId());
			}
		}
		List<TaskVO> taskVOs = TaskVO.fillList(result.getList(), windowBindTaskSet);
		// 分页，设置页码，每页显示条目等
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(taskVOs);

		return pagePaginate;
	}


	public void exportSampleTaskInfo(Integer taskId, String fileName, OutputStream output) {
		try {
			List<Record> records = Db.find(SampleTaskSQL.GET_EXPROT_SAMPLE_TASK_INFO, taskId);
			String[] field = null;
			String[] head = null;
			field = new String[] { "supplier_name", "no", "store_quantity", "scan_quantity", "regular_out_quantity", "singular_out_quantity" };
			head = new String[] { "客户", "料号", "库存数量", "抽检数量", "抽检出库数量", "异常出库数量" };
			ExcelWritter writter = ExcelWritter.create(true);
			writter.fill(records, fileName, field, head);
			writter.write(output, true);
		} catch (IOException e) {
			e.printStackTrace();
			throw new OperationException("下载失败" + e.getMessage());
		}
	}


	/**
	 * <p>
	 * Description:强制解绑仓口，仅有作废任务可以解绑
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2019年11月27日
	 */
	public void forceUnbundlingWindow(Integer taskId) {
		Task task = Task.dao.findById(taskId);
		if (task == null || !task.getState().equals(TaskState.CANCELED)) {
			throw new OperationException("仓口绑定任务未处于作废状态，无法解绑！");
		}
		synchronized (Lock.WINDOW_LOCK) {
			List<Window> windows = Window.dao.find(SQL.GET_WINDOW_BY_TASK, task.getId());
			if (windows == null || windows.isEmpty()) {
				throw new OperationException("任务并未绑定仓口，无需解绑！");
			}
			SampleTaskItemRedisDAO.removeSampleTaskItemByTaskId(task.getId());
			for (Window window : windows) {
				List<GoodsLocation> goodsLocations = GoodsLocation.dao.find(SQL.GET_GOODSLOCATION_BY_WINDOW, window.getId());
				if (!goodsLocations.isEmpty()) {
					for (GoodsLocation goodsLocation : goodsLocations) {
						TaskPropertyRedisDAO.delLocationStatus(window.getId(), goodsLocation.getId());
					}
				}
				window.setBindTaskId(null).update();
			}
		}

	}


	public String getTaskName(Integer taskId) {

		Task task = Task.dao.findById(taskId);
		if (task == null) {
			throw new OperationException("任务不存在!");
		}
		return task.getFileName();
	}


	public String getSampleTaskName(Date date, Integer warehouseType, String supplierName) {
		String fileName = "";
		fileName = supplierName + WarehouseType.getDescribeById(warehouseType) + "抽检_";
		DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String time = formatter.format(date);
		fileName = fileName + time;
		return fileName;
	}
}
