package com.jimi.uw_server.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.model.ExternalWhLog;
import com.jimi.uw_server.model.vo.ExternalWhInfoVO;


/**
 * 
 * @author trjie
 * @createTime 2019年5月8日  上午9:40:44
 */

public class ExternalWhLogService {

	public static final ExternalWhLogService me = new ExternalWhLogService();

	private static final String GET_IN_EXTERNALWHLOG_QUANTITY_BY_MATERIALTYPEID = "SELECT sum(external_wh_log.quantity) as in_quantity FROM external_wh_log WHERE external_wh_log.material_type_id = ? and external_wh_log.destination = ? and external_wh_log.source_wh != external_wh_log.destination";

	private static final String GET_OUT_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID = "SELECT sum(external_wh_log.quantity) as out_quantity FROM external_wh_log WHERE external_wh_log.material_type_id = ? and external_wh_log.source_wh = ? and external_wh_log.source_wh != external_wh_log.destination";

	private static final String GET_WASTAGE_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID_AND_TASK_TYPE = "SELECT sum(external_wh_log.quantity) as quantity FROM external_wh_log INNER JOIN task ON external_wh_log.task_id = task.id WHERE external_wh_log.material_type_id = ? and external_wh_log.source_wh = ? and external_wh_log.source_wh = external_wh_log.destination and task.type = ?";

	private static final String GET_IN_EXTERNALWHLOG_QUANTITY_BY_MATERIALTYPEID_AND_TIME = "SELECT sum(external_wh_log.quantity) as in_quantity FROM external_wh_log WHERE external_wh_log.material_type_id = ? and external_wh_log.destination = ? and external_wh_log.source_wh != external_wh_log.destination and external_wh_log.time <= ?";

	private static final String GET_OUT_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID_AND_TIME = "SELECT sum(external_wh_log.quantity) as out_quantity FROM external_wh_log WHERE external_wh_log.material_type_id = ? and external_wh_log.source_wh = ? and external_wh_log.source_wh != external_wh_log.destination and external_wh_log.time <= ?";

	private static final String GET_WASTAGE_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID_AND_TASK_TYPE_AND_TIME = "SELECT sum(external_wh_log.quantity) as quantity FROM external_wh_log INNER JOIN task ON external_wh_log.task_id = task.id WHERE external_wh_log.material_type_id = ? and external_wh_log.source_wh = ? and external_wh_log.source_wh = external_wh_log.destination and task.type = ? and external_wh_log.time <= ?";


	/**
	 * 根据物料类型ID和仓库ID获取该物料类型在该仓库的库存
	 * @param materialTypeId
	 * @param whId
	 * @return
	 */
	public Integer getEWhMaterialQuantity(Integer materialTypeId, Integer whId) {

		ExternalWhLog inExternalWhLog = ExternalWhLog.dao.findFirst(GET_IN_EXTERNALWHLOG_QUANTITY_BY_MATERIALTYPEID, materialTypeId, whId);
		ExternalWhLog outExternalWhLog = ExternalWhLog.dao.findFirst(GET_OUT_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID, materialTypeId, whId);
		ExternalWhLog wastageExternalWhLog = ExternalWhLog.dao.findFirst(GET_WASTAGE_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID_AND_TASK_TYPE, materialTypeId, whId, TaskType.WASTAGE);
		ExternalWhLog inventoryExternalWhLog = ExternalWhLog.dao.findFirst(GET_WASTAGE_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID_AND_TASK_TYPE, materialTypeId, whId, TaskType.COUNT);
		Integer inQuantity = (inExternalWhLog.getInt("in_quantity") == null ? 0 : inExternalWhLog.getInt("in_quantity"));
		Integer outQuantity = (outExternalWhLog.getInt("out_quantity") == null ? 0 : outExternalWhLog.getInt("out_quantity"));
		Integer wastageQuantity = (wastageExternalWhLog.getInt("quantity") == null ? 0 : wastageExternalWhLog.getInt("quantity"));
		Integer inventoryQuantity = (inventoryExternalWhLog.getInt("quantity") == null ? 0 : inventoryExternalWhLog.getInt("quantity"));
		Integer quantity = inQuantity - outQuantity - wastageQuantity + inventoryQuantity;
		return quantity;
	}


	/**
	 * 根据物料类型ID和仓库ID和时间获取该物料类型在该仓库的库存
	 * @param materialTypeId
	 * @param whId
	 * @param time
	 * @return
	 */
	public Integer getEWhMaterialQuantity(Integer materialTypeId, Integer whId, Date time) {

		ExternalWhLog inExternalWhLog = ExternalWhLog.dao.findFirst(GET_IN_EXTERNALWHLOG_QUANTITY_BY_MATERIALTYPEID_AND_TIME, materialTypeId, whId, time);
		ExternalWhLog outExternalWhLog = ExternalWhLog.dao.findFirst(GET_OUT_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID_AND_TIME, materialTypeId, whId, time);
		ExternalWhLog wastageExternalWhLog = ExternalWhLog.dao.findFirst(GET_WASTAGE_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID_AND_TASK_TYPE_AND_TIME, materialTypeId, whId, TaskType.WASTAGE, time);
		ExternalWhLog inventoryExternalWhLog = ExternalWhLog.dao.findFirst(GET_WASTAGE_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID_AND_TASK_TYPE_AND_TIME, materialTypeId, whId, TaskType.COUNT, time);
		Integer inQuantity = (inExternalWhLog.getInt("in_quantity") == null ? 0 : inExternalWhLog.getInt("in_quantity"));
		Integer outQuantity = (outExternalWhLog.getInt("out_quantity") == null ? 0 : outExternalWhLog.getInt("out_quantity"));
		Integer wastageQuantity = (wastageExternalWhLog.getInt("quantity") == null ? 0 : wastageExternalWhLog.getInt("quantity"));
		Integer inventoryQuantity = (inventoryExternalWhLog.getInt("quantity") == null ? 0 : inventoryExternalWhLog.getInt("quantity"));
		Integer quantity = inQuantity - outQuantity - wastageQuantity + inventoryQuantity;
		return quantity;
	}


	/**
	 * 根据同个客户每个周转仓的库存，计算每个物料的总库存
	 * @param materialTypeId
	 * @param whId
	 * @return
	 */
	public Map<Integer, Integer> getEWhMaterialTypeQuantity(List<ExternalWhInfoVO> externalWhInfoVOs) {
		Map<Integer, Integer> materialTypeQuantityMap = new HashMap<>();
		for (ExternalWhInfoVO externalWhInfoVO : externalWhInfoVOs) {
			if (materialTypeQuantityMap.get(externalWhInfoVO.getMaterialTypeId()) == null) {
				materialTypeQuantityMap.put(externalWhInfoVO.getMaterialTypeId(), 0);
			}
			materialTypeQuantityMap.put(externalWhInfoVO.getMaterialTypeId(), materialTypeQuantityMap.get(externalWhInfoVO.getMaterialTypeId()) + externalWhInfoVO.getQuantity());
		}
		return materialTypeQuantityMap;
	}
}
