package com.jimi.uw_server.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.constant.sql.DestinationSQL;
import com.jimi.uw_server.constant.sql.IOTaskSQL;
import com.jimi.uw_server.constant.sql.MaterialSQL;
import com.jimi.uw_server.constant.sql.MaterialTypeSQL;
import com.jimi.uw_server.constant.sql.SupplierSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Destination;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.bo.PreciousMaterialTypeItemBO;
import com.jimi.uw_server.model.bo.RegularMaterialTypeItemBO;
import com.jimi.uw_server.model.vo.MaterialStockDetailVO;
import com.jimi.uw_server.model.vo.MaterialTypeVO;
import com.jimi.uw_server.model.vo.WarehouseStockDetailVO;
import com.jimi.uw_server.model.vo.WarehouseStockVO;
import com.jimi.uw_server.util.ExcelHelper;
import com.jimi.uw_server.util.PagePaginate;

/**
 * <p>
 * Title: MaterialTypeService
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
 * @date 2020年1月10日
 *
 */
public class MaterialTypeService {

	private static final String GET_MATERIAL_TYPE_IN_PROCESS_SQL = "SELECT * FROM packing_list_item WHERE material_type_id = ? AND task_id IN (SELECT id FROM task WHERE state <= 2)";

	private static final String GET_MATERIAL_TYPE_SQL = "SELECT material_type.*, supplier.name AS supplierName FROM material_type INNER JOIN supplier ON material_type.supplier = supplier.id WHERE material_type.enabled = 1 AND supplier.enabled = 1 AND supplier.id = ?";

	private int batchSize = 1500;

	private static Object IMPORT_MATERIAL_TYPE_FILE_LOCK = new Object();

	private ExternalWhLogService externalWhLogService = Aop.get(ExternalWhLogService.class);


	// 导入物料类型表
	public String importRegularMaterialTypeFile(String fileName, File file, Integer supplierId) throws Exception {
		String resultString = "导入成功！";
		// 如果文件格式不对，则提示检查文件格式
		if (!(fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
			// 清空upload目录下的文件
			resultString = "导入物料类型表失败，请检查文件格式是否正确！";
			return resultString;
		}
		ExcelHelper fileReader = ExcelHelper.from(file);
		List<RegularMaterialTypeItemBO> items = fileReader.unfill(RegularMaterialTypeItemBO.class, 0);
		// 如果物料类型表头不对或者表格中没有物料信息记录
		if (items == null || items.isEmpty()) {
			resultString = "导入物料类型表失败，套料单表头错误或者表格中没有任何有效的物料信息记录！";
			return resultString;
		} else {
			synchronized (IMPORT_MATERIAL_TYPE_FILE_LOCK) {
				// 根据客户名获取客户id
				Supplier s = Supplier.dao.findById(supplierId);
				if (s == null) {
					throw new OperationException("客户不存在！");
				}

				// 从电子表格第2行开始有物料记录
				int i = 2;
				List<MaterialType> list = new ArrayList<>(items.size());
				Set<String> noSet = new HashSet<>();
				for (RegularMaterialTypeItemBO item : items) {
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) { // 只读取有序号的行数据

						// 判断各单元格数据类型是否正确以及是否存在多余的空格
						if (item.getNo() == null || item.getSpecification() == null || item.getThickness() == null || item.getRadius() == null || item.getNo().replaceAll(" ", "").equals("")
								|| item.getSpecification().replaceAll(" ", "").equals("") || item.getThickness().toString().replaceAll(" ", "").equals("")
								|| item.getRadius().toString().replaceAll(" ", "").equals("") || item.getIsSuperabled() == null || item.getIsSuperabled().trim().equals("")) {
							resultString = "导入物料类型表失败，请检查单表格第" + i + "行的料号/规格/厚度/直径/是否超发列是否填写了准确信息！";
						}

						// 判断厚度和半径是否为正整数
						if (item.getThickness() <= 0 || item.getRadius() <= 0) {
							resultString = "导入物料类型表失败，表格第" + i + "行的厚度/直径列不是正整数！";
							return resultString;
						}
						boolean isSuperable = false;
						if (item.getIsSuperabled().trim().equals("是")) {
							isSuperable = true;
						} else if (item.getIsSuperabled().trim().equals("否")) {
							isSuperable = false;
						} else {
							resultString = "导入物料类型表失败，表格第" + i + "行的是否超发列格式不正确，请填写”是“或.”否“！";
							return resultString;
						}
						// 根据料号和客户找到对应的物料类型
						MaterialType mType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_SQL, item.getNo().trim().toUpperCase(), supplierId);
						/*
						 * 判断物料类型表中是否存在对应的料号且客户也相同的物料类型记录，并且该物料类型未被禁用； 若存在，则跳过这些记录
						 */
						if (mType != null) {
							i++;
							continue;
						} else {
							// 若不存在异常数据，则新增一条物料类型表记录
							if (noSet.contains(item.getNo().toUpperCase().trim())) {
								continue;
							}
							noSet.add(item.getNo().trim().toUpperCase());
							MaterialType materialType = new MaterialType();
							materialType.setNo(item.getNo().toUpperCase().trim());
							materialType.setSpecification(item.getSpecification().trim());
							materialType.setThickness(item.getThickness());
							materialType.setRadius(item.getRadius());
							materialType.setEnabled(true);
							materialType.setSupplier(supplierId);
							materialType.setType(WarehouseType.REGULAR.getId());
							materialType.setIsSuperable(isSuperable);
							list.add(materialType);
						}

						i++;
					} else if (i == 2) { // 若第二行就没有序号，则说明表格一条物料记录也没有
						resultString = "导入物料类型表失败，表格没有任何有效的物料信息记录！";
						return resultString;
					} else {
						break;
					}
				}
				Db.batchSave(list, batchSize);
			}
		}
		return resultString;
	}


	// 导入物料类型表
	public String importPreciousMaterialTypeFile(String fileName, File file, Integer supplierId) throws Exception {
		String resultString = "导入成功！";
		// 如果文件格式不对，则提示检查文件格式
		if (!(fileName.endsWith(".xls") || fileName.endsWith(".xlsx"))) {
			// 清空upload目录下的文件
			resultString = "导入物料类型表失败，请检查文件格式是否正确！";
			return resultString;
		}
		ExcelHelper fileReader = ExcelHelper.from(file);
		List<PreciousMaterialTypeItemBO> items = fileReader.unfill(PreciousMaterialTypeItemBO.class, 0);
		List<String> nos = new ArrayList<>();
		List<String> designators = new ArrayList<>();
		// 如果物料类型表头不对或者表格中没有物料信息记录
		if (items == null || items.size() == 0) {
			resultString = "导入物料类型表失败，套料单表头错误或者表格中没有任何有效的物料信息记录！";
			return resultString;
		} else {
			synchronized (IMPORT_MATERIAL_TYPE_FILE_LOCK) {
				// 根据客户名获取客户id
				Supplier s = Supplier.dao.findById(supplierId);
				if (s == null) {
					throw new OperationException("客户不存在！");
				}

				// 从电子表格第2行开始有物料记录
				int i = 2;
				List<MaterialType> list = new ArrayList<>(items.size());
				for (PreciousMaterialTypeItemBO item : items) {
					if (item.getSerialNumber() != null && item.getSerialNumber() > 0) { // 只读取有序号的行数据

						// 判断各单元格数据类型是否正确以及是否存在多余的空格
						if (item.getNo() == null || item.getSpecification() == null || item.getThickness() == null || item.getRadius() == null || item.getNo().replaceAll(" ", "").equals("")
								|| item.getSpecification().replaceAll(" ", "").equals("") || item.getThickness().toString().replaceAll(" ", "").equals("")
								|| item.getRadius().toString().replaceAll(" ", "").equals("")) {
							resultString = "导入物料类型表失败，请检查单表格第" + i + "行的料号/规格/厚度/半径列是否填写了准确信息！";
							return resultString;
						}

						// 判断厚度和半径是否为正整数
						if (item.getThickness() <= 0 || item.getRadius() <= 0) {
							resultString = "导入物料类型表失败，表格第" + i + "行的厚度/半径列不是正整数！";
							return resultString;
						}

						if (item.getDesignator() == null || item.getDesignator().trim().equals("")) {
							resultString = "导入物料类型表失败，表格第" + i + "行的位号为空！";
							return resultString;
						}
						String no = item.getNo().trim().toUpperCase();
						String deignator = item.getDesignator().trim().toUpperCase();
						// 根据料号和客户找到对应的物料类型
						MaterialType tempMaterialType1 = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_SQL, no, supplierId);
						/*
						 * 判断物料类型表中是否存在对应的料号且客户也相同的物料类型记录，并且该物料类型未被禁用； 若存在，则跳过这些记录
						 */
						MaterialType tempMaterialType2 = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_DESIGNATOR_AND_TYPE_SQL, supplierId, deignator,
								WarehouseType.PRECIOUS.getId());

						if (tempMaterialType1 != null) {
							resultString = "导入物料类型表失败，表格第" + i + "行的料号已存在于系统中！";
							return resultString;
						} else if (tempMaterialType2 != null) {
							resultString = "导入物料类型表失败，表格第" + i + "行的位号已存在于系统中！";
							return resultString;
						} else if (nos.contains(no)) {
							resultString = "导入物料类型表失败，表格第" + i + "行的料号在表格中重复！";
							return resultString;
						} else if (designators.contains(deignator)) {
							resultString = "导入物料类型表失败，表格第" + i + "行的位号在表格中重复！";
							return resultString;
						} else {
							// 若不存在异常数据，则新增一条物料类型表记录
							MaterialType materialType = new MaterialType();
							materialType.setNo(no);
							materialType.setSpecification(item.getSpecification());
							materialType.setThickness(item.getThickness());
							materialType.setRadius(item.getRadius());
							materialType.setEnabled(true);
							materialType.setSupplier(supplierId);
							materialType.setDesignator(deignator);
							materialType.setType(WarehouseType.PRECIOUS.getId());
							list.add(materialType);
							nos.add(no);
							designators.add(deignator);
						}

						i++;
					} else if (i == 2) { // 若第二行就没有序号，则说明表格一条物料记录也没有
						resultString = "导入物料类型表失败，表格没有任何有效的物料信息记录！";
						return resultString;
					} else {
						break;
					}
				}
				for (MaterialType materialType : list) {
					materialType.save();
				}
			}
		}
		return resultString;
	}


	// 新增物料类型
	public String addMaterialType(String no, String specification, Integer supplierId, Integer thickness, Integer radius, Integer warehouseType, String designator, Boolean isSuperable) {
		String resultString = "添加成功！";
		if (no.contains("!") || no.contains("$")) {
			throw new OperationException("请勿往料号中添加非法字符，如“!”或“$”！");
		}
		no = no.trim().toUpperCase();
		designator = (designator == null || designator.trim().equals("")) ? null : designator.trim().toUpperCase();
		Supplier s = Supplier.dao.findById(supplierId);
		if (s == null) {
			throw new OperationException("客户不存在！");
		}
		MaterialType conflictMaterialType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_SQL, no, supplierId);
		if (conflictMaterialType != null) {
			resultString = WarehouseType.getDescribeById(conflictMaterialType.getType()) + "已存在该物料类型，请勿重复添加！";
			return resultString;
		}
		if (warehouseType.equals(WarehouseType.PRECIOUS.getId())) {
			if (designator == null) {
				throw new OperationException("贵重仓物料类型需添加位号！");
			}
			MaterialType tempMaterialType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_DESIGNATOR_AND_TYPE_SQL, supplierId, designator, WarehouseType.PRECIOUS.getId());
			if (tempMaterialType != null) {
				resultString = "存在物料的位号与新添加物料的位号相同，添加失败！";
				return resultString;
			}
		}
		MaterialType newMaterialType = new MaterialType();
		newMaterialType.setNo(no).setSpecification(specification).setSupplier(supplierId).setThickness(thickness).setRadius(radius).setEnabled(true).setType(warehouseType).setDesignator(designator)
				.setIsSuperable(isSuperable).save();
		return resultString;
	}


	// 更新物料类型
	public String updateMaterialType(Integer id, Integer thickness, Integer radius, String designator, String specification, Integer warehouseType, Boolean isSuperable) {
		String resultString = "更新成功！";
		MaterialType materialType = MaterialType.dao.findById(id);
		if (materialType == null) {
			throw new OperationException("物料类型不存在！");
		}
		designator = (designator == null || designator.trim().equals("")) ? null : designator.trim().toUpperCase();
		if (materialType.getType().equals(WarehouseType.PRECIOUS.getId())) {
			if (designator == null) {
				throw new OperationException("贵重仓物料类型需添加位号！");
			}
			MaterialType tempMaterialType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_DESIGNATOR_AND_TYPE_SQL, materialType.getSupplier(), designator,
					WarehouseType.PRECIOUS.getId());
			if (tempMaterialType != null && !tempMaterialType.getId().equals(id)) {
				resultString = "存在其他物料的位号与新添加物料的位号相同，添加失败！";
				return resultString;
			}
		}
		materialType.setThickness(thickness).setRadius(radius).setSpecification(specification).setDesignator(designator).setIsSuperable(isSuperable).update();
		return resultString;
	}


	// 更新物料类型
	public String deleteByIds(String filter) {
		StringBuffer resultString = new StringBuffer();
		String[] ids = filter.split(",");
		List<String> remaindIds = new ArrayList<>();
		List<String> bindIds = new ArrayList<>();
		Boolean flag = true;
		for (String id : ids) {
			Material m = Material.dao.findFirst(MaterialSQL.GET_MATERIAL_BY_MATERIAL_TYPE_AND_COMPANY_SQL, Integer.valueOf(id));
			if (m != null) {
				remaindIds.add(id);
				flag = false;
			}
			if (PackingListItem.dao.findFirst(GET_MATERIAL_TYPE_IN_PROCESS_SQL, Integer.valueOf(id)) != null) {
				bindIds.add(id);
				flag = false;
			}
			if (flag.equals(true)) {
				MaterialType materialType = MaterialType.dao.findById(id);
				materialType.setEnabled(false);
				materialType.update();
			}
		}

		if (remaindIds.size() == 0 && bindIds.size() == 0) {
			return "删除成功";
		} else {
			if (remaindIds.size() > 0) {
				resultString.append("料号ID 为  [");
				for (String remaindId : remaindIds) {
					resultString.append(remaindId + ",");
				}
				resultString.deleteCharAt(resultString.lastIndexOf(","));
				resultString.append("] 的物料存在库存，无法删除!");
			}
			if (bindIds.size() > 0) {
				if (resultString.length() != 0) {
					resultString.append("|");
				}
				resultString.append("料号ID 为  [");
				for (String bindId : bindIds) {
					resultString.append(bindId + ",");
				}
				resultString.deleteCharAt(resultString.lastIndexOf(","));
				resultString.append("] 的物料以绑定了任务，无法删除!");
			}
			return resultString.toString();
		}

	}


	public PagePaginate getMaterialTypeVOs(Integer pageNo, Integer pageSize, Integer supplierId, String no, String specification) {
		SqlPara sqlPara = new SqlPara();
		String sql = GET_MATERIAL_TYPE_SQL;
		sqlPara.addPara(supplierId);
		if (no != null) {
			sql += " AND no like ?";
			sqlPara.addPara("%" + no + "%");
		}
		if (specification != null) {
			sql += " AND specification like ?";
			sqlPara.addPara("%" + specification + "%");
		}
		sqlPara.setSql(sql);
		Page<Record> materialTypePages = Db.paginate(pageNo, pageSize, sqlPara);
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageNumber(materialTypePages.getPageNumber());
		pagePaginate.setPageSize(materialTypePages.getPageSize());
		pagePaginate.setTotalPage(materialTypePages.getTotalPage());
		pagePaginate.setTotalRow(materialTypePages.getTotalRow());

		if (materialTypePages.getList() != null) {
			pagePaginate.setList(MaterialTypeVO.fillList(materialTypePages.getList()));
		} else {
			pagePaginate.setList(Collections.emptyList());
		}
		return pagePaginate;
	}


	/**
	 * 
	 * <p>
	 * Description: 物料明细表
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年5月20日
	 */
	public PagePaginate getMaterialStockDetails(String no, String supplierId, Integer warehouseType, Integer whId, Integer pageNo, Integer pageSize, Date startTime, Date endTime) {
		// 获取客户所有仓库
		Supplier supplier = Supplier.dao.findFirst(SupplierSQL.GET_SUPPLIER_BY_ID_SQL, supplierId);
		String warehouseString = WarehouseType.getDescribeById(warehouseType);
		List<Destination> warehouses = getWarehouseBySupplier(supplier, warehouseType, whId);
		// 获取客户所有物料类型
		SqlPara sqlPara = new SqlPara();
		if (no == null || no.trim().equals("")) {
			sqlPara.setSql(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_SUPPLIER_AND_TYPE_SQL);
			sqlPara.addPara(supplier.getId()).addPara(warehouseType);
		} else {
			sqlPara.setSql(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_NO_AND_SUPPLIER_AND_TYPE_SQL);
			sqlPara.addPara(no).addPara(supplier.getId()).addPara(warehouseType);
		}
		Page<Record> materialTypesRecords = Db.paginate(pageNo, pageSize, sqlPara);
		// 物料库存明细记录
		PagePaginate page = new PagePaginate();
		page.setPageNumber(materialTypesRecords.getPageNumber());
		page.setPageSize(materialTypesRecords.getPageSize());
		page.setTotalPage(materialTypesRecords.getTotalPage());
		page.setTotalRow(materialTypesRecords.getTotalRow());
		List<MaterialStockDetailVO> materialStockDetailVOs = new ArrayList<>();
		if (materialTypesRecords != null && materialTypesRecords.getList() != null && !materialTypesRecords.getList().isEmpty()) {
			// 遍历所有物料类型
			for (Record record : materialTypesRecords.getList()) {

				MaterialStockDetailVO materialStockDetailVO = new MaterialStockDetailVO();
				materialStockDetailVO.setId(record.getInt("id"));
				materialStockDetailVO.setNo(record.getStr("no"));
				materialStockDetailVO.setSpecification(record.getStr("specification"));
				materialStockDetailVO.setSupplierName(supplier.getName());
				materialStockDetailVO.setWarehouse(warehouseString);
				List<WarehouseStockVO> warehouseStockVOs = new ArrayList<>();
				// 全部仓库的入库数和出库数
				Integer allWarehosueInNum = 0;
				Integer allWarehosueOutNum = 0;
				Integer allOldBalance = 0;
				Integer allCurrentBalance = 0;
				// 遍历所有仓库
				for (Destination warehouse : warehouses) {
					WarehouseStockVO warehouseStockVO = new WarehouseStockVO();
					warehouseStockVO.setWarehouse(warehouse.getName());
					List<WarehouseStockDetailVO> warehouseStockDetailVOs = new ArrayList<>();
					// 每个仓库内部的入库数和出库数
					Integer inStockNum = 0;
					Integer outStockNum = 0;
					Integer oldBalance = 0;
					if (warehouse.getId().equals(0)) {
						List<Record> ioRecords = Db.find(IOTaskSQL.GET_ALL_SUM_OF_TASK_LOG_BY_TIME_AND_MATERIALTYPE_SQL, startTime, endTime, record.getInt("id"), startTime, endTime,
								record.getInt("id"), startTime, endTime, record.getInt("id"));
						// 遍历所有日志记录（同一任务同一物料类型入库，出库数量已统计）
						if (!ioRecords.isEmpty()) {
							for (Record ioRecord : ioRecords) {
								Integer taskType = ioRecord.getInt("Task_Type");
								if (taskType == null) {
									continue;
								}
								Integer ioQuantity = ioRecord.getInt("IO_Quantity") == null ? 0 : ioRecord.getInt("IO_Quantity");
								WarehouseStockDetailVO warehouseStockDetailVO = new WarehouseStockDetailVO();
								switch (taskType) {
								// 入库
								case TaskType.IN:
									warehouseStockDetailVO.setOperationType("入库");
									warehouseStockDetailVO.setNumberInStock(ioQuantity);
									inStockNum += ioQuantity;
									break;
								// 出库
								case TaskType.OUT:
									warehouseStockDetailVO.setOperationType("出库");
									warehouseStockDetailVO.setNumberOutStock(ioQuantity);
									outStockNum += ioQuantity;
									break;
								// 调拨入库
								case TaskType.SEND_BACK:
									warehouseStockDetailVO.setOperationType("入库");
									warehouseStockDetailVO.setNumberInStock(ioQuantity);
									inStockNum += ioQuantity;
									break;
								// 紧急出库（紧急出库伴随着一个入库任务生成，紧急入库任务数量包含在入库任务中计算，此处仅计算紧急出库）
								case TaskType.EMERGENCY_OUT:
									warehouseStockDetailVO.setOperationType("出库");
									warehouseStockDetailVO.setNumberOutStock(ioQuantity);
									outStockNum += ioQuantity;
									break;
								// 盘点
								case TaskType.COUNT:
									if (ioQuantity > 0) {
										warehouseStockDetailVO.setOperationType("入库");
										warehouseStockDetailVO.setNumberInStock(ioQuantity);
										inStockNum += ioQuantity;
									} else {
										warehouseStockDetailVO.setOperationType("出库");
										warehouseStockDetailVO.setNumberOutStock(0 - ioQuantity);
										outStockNum += (0 - ioQuantity);
									}
									break;
								// 抽检
								case TaskType.SAMPLE:
									warehouseStockDetailVO.setOperationType("出库");
									warehouseStockDetailVO.setNumberOutStock(ioQuantity);
									outStockNum += ioQuantity;
									break;
								}
								warehouseStockDetailVOs.add(warehouseStockDetailVO);
							}
						}

						oldBalance = getUwStockByTimeAndMaterialType(startTime, record.getInt("id"));
					} else {
						List<Record> ioRecords = Db.find(IOTaskSQL.GET_ALL_EXTERNAL_WH_IO_LOG_BY_TIME_AND_MATERIALTYPE_SQL, warehouse.getId(), startTime, endTime, record.getInt("id"),
								warehouse.getId(), startTime, endTime, record.getInt("id"), warehouse.getId(), startTime, endTime, record.getInt("id"));
						if (!ioRecords.isEmpty()) {
							for (Record ioRecord : ioRecords) {
								Integer taskType = ioRecord.getInt("Task_Type");
								if (taskType == null) {
									continue;
								}
								Integer ioQuantity = ioRecord.getInt("IO_Quantity") == null ? 0 : ioRecord.getInt("IO_Quantity");
								WarehouseStockDetailVO warehouseStockDetailVO = new WarehouseStockDetailVO();
								switch (taskType) {
								// 出库
								case TaskType.OUT:
									if (ioQuantity > 0) {
										warehouseStockDetailVO.setOperationType("入库");
										warehouseStockDetailVO.setNumberInStock(ioQuantity);
										inStockNum += ioQuantity;
									} else {
										warehouseStockDetailVO.setOperationType("出库");
										warehouseStockDetailVO.setNumberOutStock(ioQuantity);
										outStockNum += (0 - ioQuantity);
									}
									break;
								// 紧急出库（紧急出库伴随着一个入库任务生成，紧急入库任务数量包含在入库任务中计算，此处仅计算紧急出库）
								case TaskType.EMERGENCY_OUT:
									if (ioQuantity > 0) {
										warehouseStockDetailVO.setOperationType("入库");
										warehouseStockDetailVO.setNumberInStock(ioQuantity);
										inStockNum += ioQuantity;
									} else {
										warehouseStockDetailVO.setOperationType("出库");
										warehouseStockDetailVO.setNumberOutStock(ioQuantity);
										outStockNum += (0 - ioQuantity);
									}
									break;
								// 调拨入库
								case TaskType.SEND_BACK:
									warehouseStockDetailVO.setOperationType("出库");
									warehouseStockDetailVO.setNumberOutStock(ioQuantity);
									outStockNum += ioQuantity;
									break;
								// 盘点
								case TaskType.COUNT:
									if (ioQuantity > 0) {
										warehouseStockDetailVO.setOperationType("入库");
										warehouseStockDetailVO.setNumberInStock(ioQuantity);
										inStockNum += ioQuantity;
									} else {
										warehouseStockDetailVO.setOperationType("出库");
										warehouseStockDetailVO.setNumberOutStock(0 - ioQuantity);
										outStockNum += (0 - ioQuantity);
									}
									break;
								// 抽检
								case TaskType.EXTERNAL_IN_OUT:
									if (ioQuantity > 0) {
										warehouseStockDetailVO.setOperationType("入库");
										warehouseStockDetailVO.setNumberInStock(ioQuantity);
										inStockNum += ioQuantity;
									} else {
										warehouseStockDetailVO.setOperationType("出库");
										warehouseStockDetailVO.setNumberOutStock(0 - ioQuantity);
										outStockNum += (0 - ioQuantity);
									}

									break;
								}
								warehouseStockDetailVOs.add(warehouseStockDetailVO);
							}
						}
						oldBalance = externalWhLogService.getRuntimeEWhMaterialQuantity(record.getInt("id"), warehouse.getId(), startTime);
					}
					warehouseStockVO.setNumberInStock(inStockNum);
					warehouseStockVO.setNumberOutStock(outStockNum);
					warehouseStockVO.setOldBalance(oldBalance);
					warehouseStockVO.setCurrentBalance(oldBalance + inStockNum - outStockNum);
					warehouseStockVO.setDetails(warehouseStockDetailVOs);
					warehouseStockVOs.add(warehouseStockVO);
					allWarehosueInNum += inStockNum;
					allWarehosueOutNum += outStockNum;
					allOldBalance += oldBalance;
					allCurrentBalance += warehouseStockVO.getCurrentBalance();
				}
				materialStockDetailVO.setNumberInStock(allWarehosueInNum);
				materialStockDetailVO.setNumberOutStock(allWarehosueOutNum);
				materialStockDetailVO.setOldBalance(allOldBalance);
				materialStockDetailVO.setCurrentBalance(allCurrentBalance);
				materialStockDetailVO.setList(warehouseStockVOs);
				materialStockDetailVOs.add(materialStockDetailVO);
			}
		}
		page.setList(materialStockDetailVOs);
		return page;
	}


	private List<Destination> getWarehouseBySupplier(Supplier supplier, Integer warehouseType, Integer whId) {
		List<Destination> warehouses = new ArrayList<Destination>();
		if (WarehouseType.REGULAR.getId().equals(warehouseType)) {
			if (whId != null) {
				warehouses = Destination.dao.find(DestinationSQL.GET_DESTINATION_BY_ID_SQL, whId);
			} else {
				warehouses = Destination.dao.find(DestinationSQL.GET_UW_DESTINATION_SQL);
				List<Destination> whOfCompany = Destination.dao.find(DestinationSQL.GET_DESTINATION_BY_COMPANY_SQL, supplier.getCompanyId());
				warehouses.addAll(whOfCompany);
			}
		} else {
			warehouses = Destination.dao.find(DestinationSQL.GET_UW_DESTINATION_SQL);
		}
		return warehouses;
	}


	private Integer getUwStockByTimeAndMaterialType(Date time, Integer materialTypeId) {
		Record uwStockRecord = Db.findFirst(IOTaskSQL.GET_UW_STOCK_BY_TIME_AND_MATERIALTYPE_SQL, time, materialTypeId, time, materialTypeId, time, materialTypeId, time, materialTypeId);
		if (uwStockRecord == null || uwStockRecord.getInt("IO_Quantity") == null) {
			return 0;
		} else {
			return uwStockRecord.getInt("IO_Quantity");
		}
	}
}
