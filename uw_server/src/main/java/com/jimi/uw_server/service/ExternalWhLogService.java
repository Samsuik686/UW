package com.jimi.uw_server.service;

import com.jimi.uw_server.model.ExternalWhLog;

/**
 * 
 * @author trjie
 * @createTime 2019年5月8日  上午9:40:44
 */

public class ExternalWhLogService {

	
	private static final String GET_IN_EXTERNALWHLOG_QUANTITY_BY_MATERIALTYPEID = "SELECT sum(external_wh_log.quantity) as in_quantity FROM external_wh_log WHERE external_wh_log.material_type_id = ? and external_wh_log.destination = ? and external_wh_log.source_wh != external_wh_log.destination";
	
	private static final String GET_OUT_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID = "SELECT sum(external_wh_log.quantity) as out_quantity FROM external_wh_log WHERE external_wh_log.material_type_id = ? and external_wh_log.source_wh = ? and external_wh_log.source_wh != external_wh_log.destination";
	
	private static final String GET_WASTAGE_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID = "SELECT sum(external_wh_log.quantity) as quantity FROM external_wh_log WHERE external_wh_log.material_type_id = ? and external_wh_log.source_wh = ? and external_wh_log.source_wh = external_wh_log.destination";
	
	
	/**
	 * 根据物料类型ID和仓库ID获取该物料类型在该仓库的库存
	 * @param materialTypeId
	 * @param whId
	 * @return
	 */
	public Integer getEWhMaterialQuantity(Integer materialTypeId, Integer whId) {
		
		ExternalWhLog inExternalWhLog = ExternalWhLog.dao.findFirst(GET_IN_EXTERNALWHLOG_QUANTITY_BY_MATERIALTYPEID, materialTypeId, whId);
		ExternalWhLog outExternalWhLog = ExternalWhLog.dao.findFirst(GET_OUT_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID, materialTypeId, whId);
		ExternalWhLog wastageExternalWhLog = ExternalWhLog.dao.findFirst(GET_WASTAGE_EXTERNALWULOG_QUANTITY_BY_MATERIALTYPEID, materialTypeId, whId);
		Integer inQuantity = (inExternalWhLog.getInt("in_quantity") == null? 0: inExternalWhLog.getInt("in_quantity"));
		Integer outQuantity = (outExternalWhLog.getInt("out_quantity") == null? 0: outExternalWhLog.getInt("out_quantity"));
		Integer wastageQuantity = (wastageExternalWhLog.getInt("quantity") == null? 0: wastageExternalWhLog.getInt("quantity"));
		Integer quantity = inQuantity - outQuantity - wastageQuantity;
		return quantity;
	}
}
