package com.jimi.uw_server.ur.processor;

import java.text.ParseException;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.handle.IOTaskHandler;
import com.jimi.uw_server.constant.TaskItemState;
import com.jimi.uw_server.model.GoodsLocation;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.service.IOTaskService;
import com.jimi.uw_server.ur.entity.IOPackage;
import com.jimi.uw_server.ur.entity.ResultPackage;
import com.jimi.uw_server.ur.entity.base.UrMaterialInfo;

import cc.darhao.dautils.api.DateUtil;


/**
 * UrSocket消息处理器
 * <br>
 * <b>2019年5月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class MessageHandler {

	private IOTaskService taskService = Aop.get(IOTaskService.class);

	public static MessageHandler me = new MessageHandler();


	/**
	 * 处理入库结果包
	 * @throws Exception 
	 */
	public void handleInPackage(IOPackage inPackage) throws Exception {
		// 判断是否存在空值
		if (inPackage.containsNullFields()) {
			throw new IllegalArgumentException("接受的包存在空属性");
		}

		updateDb(inPackage);
		updateRedis(inPackage);
		returnBox(inPackage);
	}


	/**
	 * 处理出库结果包
	 * @throws Exception 
	 */
	public void handleResultPackage(ResultPackage resultPackage) throws Exception {
		// 判断是否存在空值
		if (resultPackage.isContainsNullFields()) {
			throw new IllegalArgumentException("接受的包存在空属性");
		}

		// 根据aim获取out包
		IOPackage outPackage = OutPackageHolder.me.getByAim(resultPackage.getAimid());
		if (outPackage == null) {
			throw new IllegalArgumentException("找不到aimid对应的数据");
		}

		updateDb(outPackage);
		updateRedis(outPackage);
		returnBox(outPackage);
	}


	private void updateDb(IOPackage ioPackage) throws ParseException {
		// 库存更新和记录入库日志
		List<UrMaterialInfo> urMaterialInfos = ioPackage.getList();
		if (urMaterialInfos == null || urMaterialInfos.size() == 0) {
			throw new IllegalArgumentException("list不能为空");
		}
		for (UrMaterialInfo urMaterialInfo : urMaterialInfos) {
			if (urMaterialInfo.containsNullFields()) {
				throw new IllegalArgumentException("list的内容不能存在空属性");
			}
			if (ioPackage.getCmdcode().equals("out")) {
				taskService.out(ioPackage.getTaskId(), urMaterialInfo.getMaterialNo(), urMaterialInfo.getMaterialTypeId(), urMaterialInfo.getQuantity(), ioPackage.getSupplier());
			} else {
				taskService.in(ioPackage.getTaskId(), urMaterialInfo.getMaterialNo(), urMaterialInfo.getRow(), urMaterialInfo.getCol(), urMaterialInfo.getMaterialTypeId(), urMaterialInfo.getQuantity(), ioPackage.getSupplier(), DateUtil.yyyyMMdd(urMaterialInfo.getProductionTime()));
			}
		}
	}


	private void returnBox(IOPackage ioPackage) throws Exception {
		// 令叉车把盒子还回库中
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(ioPackage.getTaskId())) {
			if (item.getTaskId().equals(ioPackage.getTaskId())) {
				MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
				GoodsLocation goodsLocation = GoodsLocation.dao.findById(item.getGoodsLocationId());
				IOTaskHandler.getInstance().sendBackLL(item, materialBox, goodsLocation, 0);
				break;
			}
		}
	}


	private void updateRedis(IOPackage ioPackage) {
		// redis里记录所有条目为已分配回库
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems(ioPackage.getTaskId())) {
			if (item.getTaskId().equals(ioPackage.getTaskId())) {
				TaskItemRedisDAO.updateIOTaskItemInfo(item, TaskItemState.START_BACK, null, null, null, null, true, null);
			}
		}
	}
}
