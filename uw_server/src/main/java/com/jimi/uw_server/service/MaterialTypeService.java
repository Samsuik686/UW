package com.jimi.uw_server.service;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.constant.sql.MaterialSQL;
import com.jimi.uw_server.constant.sql.MaterialTypeSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.bo.PreciousMaterialTypeItemBO;
import com.jimi.uw_server.model.bo.RegularMaterialTypeItemBO;
import com.jimi.uw_server.model.vo.MaterialTypeVO;
import com.jimi.uw_server.service.entity.PagePaginate;
import com.jimi.uw_server.util.ExcelHelper;

/**  
 * <p>Title: MaterialTypeService</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月10日
 *
 */
public class MaterialTypeService {
	
	private static final String GET_MATERIAL_TYPE_IN_PROCESS_SQL = "SELECT * FROM packing_list_item WHERE material_type_id = ? AND task_id IN (SELECT id FROM task WHERE state <= 2)";

	private static final String GET_MATERIAL_TYPE_SQL = "SELECT material_type.*, supplier.name AS supplierName FROM material_type INNER JOIN supplier ON material_type.supplier = supplier.id WHERE material_type.enabled = 1 AND supplier.enabled = 1 AND supplier.id = ?";

	private int batchSize = 1500;
	
	private static Object IMPORT_MATERIAL_TYPE_FILE_LOCK = new Object();
	
	
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
						if (item.getNo() == null || item.getSpecification() == null || item.getThickness() == null || item.getRadius() == null || item.getNo().replaceAll(" ", "").equals("") || item.getSpecification().replaceAll(" ", "").equals("") || item.getThickness().toString().replaceAll(" ", "").equals("") || item.getRadius().toString().replaceAll(" ", "").equals("")) {
							resultString = "导入物料类型表失败，请检查单表格第" + i + "行的料号/规格/厚度/直径列是否填写了准确信息！";
							return resultString;
						}

						// 判断厚度和半径是否为正整数
						if (item.getThickness() <= 0 || item.getRadius() <= 0) {
							resultString = "导入物料类型表失败，表格第" + i + "行的厚度/直径列不是正整数！";
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
							if (item.getNo() == null || item.getSpecification() == null || item.getThickness() == null || item.getRadius() == null || item.getNo().replaceAll(" ", "").equals("") || item.getSpecification().replaceAll(" ", "").equals("") || item.getThickness().toString().replaceAll(" ", "").equals("") || item.getRadius().toString().replaceAll(" ", "").equals("")) {
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
							MaterialType tempMaterialType1 = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_SUPPLIER_AND_NAME_SQL, no, supplierId);
							/*
							 * 判断物料类型表中是否存在对应的料号且客户也相同的物料类型记录，并且该物料类型未被禁用； 若存在，则跳过这些记录
							 */
							MaterialType tempMaterialType2 = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_DESIGNATOR_AND_TYPE_SQL, supplierId, deignator, WarehouseType.PRECIOUS.getId());

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
		MaterialType conflictMaterialType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_SUPPLIER_AND_NAME_SQL, no, supplierId);
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
		newMaterialType.setNo(no).setSpecification(specification).setSupplier(supplierId).setThickness(thickness)
		   .setRadius(radius).setEnabled(true).setType(warehouseType).setDesignator(designator).setIsSuperable(isSuperable).save();
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
			MaterialType tempMaterialType = MaterialType.dao.findFirst(MaterialTypeSQL.GET_MATERIAL_TYPE_BY_DESIGNATOR_AND_TYPE_SQL, materialType.getSupplier(), designator, WarehouseType.PRECIOUS.getId());
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
	
	
	public PagePaginate getMaterialTypeVOs(Integer pageNo, Integer pageSize, Integer supplierId, String no, String specification){
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
		}else {
			pagePaginate.setList(Collections.emptyList());
		}
		return pagePaginate;
	}
	
}
