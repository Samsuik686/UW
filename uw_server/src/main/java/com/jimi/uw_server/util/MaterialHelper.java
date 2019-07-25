package com.jimi.uw_server.util;

import java.util.List;

import com.jfinal.kit.PropKit;
import com.jimi.uw_server.model.Material;

public class MaterialHelper {
	
	private static Integer ROW = PropKit.use("properties.ini").getInt("materialBoxRow");
	
	private static Integer COL = PropKit.use("properties.ini").getInt("materialBoxCol");
	
	private static boolean[][] materialStatus = new boolean[PropKit.use("properties.ini").getInt("materialBoxCol")][PropKit.use("properties.ini").getInt("materialBoxRow")];
	
	private static final String GET_MATERIAL_WITH_LOCATION_BY_BOX = "SELECT * FROM material WHERE row != -1 AND col != -1 AND box = ?";	
	/**
	 * 分配物料位置
	 * @param material
	 * @param isForced 是否强制分配
	 */
	public synchronized static void getMaterialLocation(Material material,  Boolean isForced) {
		try {
			List<Material> materials = Material.dao.find(GET_MATERIAL_WITH_LOCATION_BY_BOX, material.getBox());
			if (materials.isEmpty() && !isForced) {
				return;
			}
			for (Material material2 : materials) {
				materialStatus[material2.getCol()][material2.getRow()] = true;
			}
			for (int i = 0; i < COL; i++) {
				for (int j = 0; j < ROW; j++) {
					if (!materialStatus[i][j])  {
						material.setCol(i).setRow(j).update();
						return ;
					}
				}
			}
			return ;
		} finally {
			for (int i = 0; i < COL; i++) {
				for (int j = 0; j < ROW; j++) {
					materialStatus[i][j] = false;
				}
			}
		}
		
	}
}
