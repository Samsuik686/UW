package com.jimi.uw_server.service;

import com.alibaba.fastjson.JSONObject;
import com.jimi.uw_server.agv.dao.BuildTaskItemDAO;
import com.jimi.uw_server.agv.entity.bo.AGVBuildTaskItem;
import com.jimi.uw_server.constant.BoxState;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.service.base.SelectService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * 建仓业务层
 * 
 * @author HardyYao
 * @createTime 2018年12月12日 下午3:11:21
 */

public class BuildService extends SelectService {

	public static final String GET_BOX_TYPE_BY_CELL_WIDTH_SQL = "SELECT * FROM box_type WHERE cell_width = ?";

	public static final String GET_BOX_BY_X_Y_Z = "SELECT * FROM material_box WHERE row = ? AND col = ? AND height = ? AND enabled = 1";


	// 建仓
	public String build(String parameters) {
		String resultString = "开始建仓！";
		if (parameters != null) {
			JSONObject Object = JSONObject.parseObject(parameters);
			JSONObject jsonObject = Object.getJSONObject("parameters");
			Integer srcX = jsonObject.getInteger("srcX");
			Integer srcY = jsonObject.getInteger("srcY");
			Integer srcZ = jsonObject.getInteger("srcZ");
			String srcPosition = srcX + ":" + srcY + ":" + srcZ;
			Integer startX = jsonObject.getInteger("startX");
			Integer startY = jsonObject.getInteger("startY");
			Integer startZ = jsonObject.getInteger("startZ");
			Integer endX = jsonObject.getInteger("endX");
			Integer endY = jsonObject.getInteger("endY");
			Integer endZ = jsonObject.getInteger("endZ");
			Integer limitYL = jsonObject.getInteger("limitYL");
			Integer limitYR = jsonObject.getInteger("limitYR");
			String area = jsonObject.getString("area");
			Boolean isStandard = Object.getBoolean("isStandard");
			Integer supplierId = Object.getInteger("supplierId");

			if (isStandard == null || supplierId == null) {
				throw new OperationException("参数格式不正确，请检查");
			}
			createBuildTasks(area, isStandard, supplierId, srcPosition, startX, startY, startZ, endX, endY, endZ, limitYL, limitYR);

		} else {
			resultString = "传递的参数不能为空！";
		}
		return resultString;
	}


	// 根据起始坐标生成“建仓任务”
	private void createBuildTasks(String area, Boolean isStandard, Integer supplierId, String srcPosition, Integer startX, Integer startY, Integer startZ, Integer endX, Integer endY, Integer endZ, Integer limitYL, Integer limitYR) {
		List<AGVBuildTaskItem> buildTaskItems = new ArrayList<AGVBuildTaskItem>();
		Supplier supplier = Supplier.dao.findById(supplierId);
		if (supplier == null) {
			throw new OperationException("客户不存在，请重新选择客户");
		}
		if (isStandard) {
			for (int z = startZ; z < 4; z++) {
				for (int y = startY; y <= limitYR; y++) {
					MaterialBox materialBox = MaterialBox.dao.findFirst(GET_BOX_BY_X_Y_Z, startX, y, z);
					if (materialBox != null) {
						continue;
					}
					materialBox = new MaterialBox();
					materialBox.setArea(area);
					materialBox.setSupplier(supplierId);
					materialBox.setRow(startX);
					materialBox.setCol(y);
					materialBox.setHeight(z);
					materialBox.setIsOnShelf(false);
					materialBox.setType(1);
					materialBox.setEnabled(true);
					materialBox.setStatus(BoxState.EMPTY);
					materialBox.setUpdateTime(new Date());
					materialBox.setCompanyId(supplier.getCompanyId());
					materialBox.save();
					AGVBuildTaskItem bt = new AGVBuildTaskItem(materialBox.getId(), srcPosition);
					buildTaskItems.add(bt);
					if (endX.equals(startX) && endY.equals(y) && endZ.equals(z)) {
						BuildTaskItemDAO.addBuildTaskItem(buildTaskItems);
						return;
					}
					if (y == limitYR) {
						y = limitYL - 1;
					}
				}

			}
		} else {
			for (int z = startZ; z >= endZ; z--) {
				for (int y = startY; y <= limitYR; y++) {
					MaterialBox materialBox = MaterialBox.dao.findFirst(GET_BOX_BY_X_Y_Z, startX, y, z);
					if (materialBox != null) {
						continue;
					}
					materialBox = new MaterialBox();
					materialBox.setArea(area);
					materialBox.setSupplier(supplierId);
					materialBox.setRow(startX);
					materialBox.setCol(y);
					materialBox.setHeight(z);
					materialBox.setIsOnShelf(false);
					materialBox.setType(2);
					materialBox.setEnabled(true);
					materialBox.setStatus(BoxState.EMPTY);
					materialBox.setUpdateTime(new Date());
					materialBox.setCompanyId(supplier.getCompanyId());
					materialBox.save();
					AGVBuildTaskItem bt = new AGVBuildTaskItem(materialBox.getId(), srcPosition);
					buildTaskItems.add(bt);
					if (endX.equals(startX) && endY.equals(y) && endZ.equals(z)) {
						BuildTaskItemDAO.addBuildTaskItem(buildTaskItems);
						return;
					}
					// 需要修改
					if (y == limitYR) {
						y = limitYL - 1;
					}
				}

			}
		}
		BuildTaskItemDAO.addBuildTaskItem(buildTaskItems);

	}

}
