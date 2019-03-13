package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVBuildTaskItem;
import com.jimi.uw_server.model.BoxType;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.service.base.SelectService;

/**
 * 建仓业务层
 * @author HardyYao
 * @createTime 2018年12月12日  下午3:11:21
 */

public class BuildService extends SelectService {

	public static final String GET_BOX_TYPE_BY_CELL_WIDTH_SQL = "SELECT * FROM box_type WHERE cell_width = ?";

	public String build(String parameters) {
		String resultString = "开始建仓！";
		if (parameters != null) {
			JSONArray jsonArray = JSONArray.parseArray(parameters);
			for (int i=0; i<jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				Integer boxType = Integer.parseInt(jsonObject.getString("boxType"));
				Integer srcX = Integer.parseInt(jsonObject.getString("srcX"));
				Integer srcY = Integer.parseInt(jsonObject.getString("srcY"));
				Integer srcZ = Integer.parseInt(jsonObject.getString("srcZ"));
				String dst = jsonObject.getString("dst");
				JSONArray dstArray = JSONArray.parseArray(dst);
				String srcPosition = srcX + ":" + srcY + ":" + srcZ;
				BoxType bType = new BoxType();
				bType.setCellWidth(boxType);
				bType.setCellRows(20);
				bType.setCellCols(2);
				bType.setEnabled(true);
				bType.save();
				for (int j=0; j<dstArray.size(); j++) {
					JSONObject jsObj = dstArray.getJSONObject(j);
					Integer startX = Integer.parseInt(jsObj.getString("startX"));
					Integer startY = Integer.parseInt(jsObj.getString("startY"));
					Integer startZ = Integer.parseInt(jsObj.getString("startZ"));
					Integer endX = Integer.parseInt(jsObj.getString("endX"));
					Integer endY = Integer.parseInt(jsObj.getString("endY"));
					Integer endZ = Integer.parseInt(jsObj.getString("endZ"));
					createBuildTasks(bType.getId(), srcPosition, startX, startY, startZ, endX, endY, endZ);
				}
			}

		} else {
			resultString = "传递的参数不能为空！";
		}
		return resultString;
	}


	private void createBuildTasks(Integer boxTypeId, String srcPosition, Integer startX, Integer startY, Integer startZ, Integer endX, Integer endY, Integer endZ) {
		List<AGVBuildTaskItem> buildTaskItems = new ArrayList<AGVBuildTaskItem>();
		for (int x=startX; x<=endX; x++) {
			for (int y=startY; y<=endY; y++) {
				for (int z=startZ; z<=endZ; z++) {
					MaterialBox materialBox = new MaterialBox();
					materialBox.setArea("A");
					materialBox.setRow(x);
					materialBox.setCol(y);
					materialBox.setHeight(z);
					materialBox.setIsOnShelf(false);
					materialBox.setType(boxTypeId);
					materialBox.setEnabled(true);
					materialBox.save();
					AGVBuildTaskItem bt = new AGVBuildTaskItem(materialBox.getId(), srcPosition);
					buildTaskItems.add(bt);
				}
			}
		}
		TaskItemRedisDAO.addBuildTaskItem(buildTaskItems);
	}

}
