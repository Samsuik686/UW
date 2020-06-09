package com.jimi.uw_server.ur.processor;

import com.jfinal.aop.Aop;
import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.service.RobotService;
import com.jimi.uw_server.service.inventory.RegularInventoryTaskService;
import com.jimi.uw_server.service.io.RegularIOTaskService;
import com.jimi.uw_server.ur.constant.Constant;
import com.jimi.uw_server.ur.dao.UrTaskInfoDAO;
import com.jimi.uw_server.ur.dao.UrOperationMaterialInfoDAO;
import com.jimi.uw_server.ur.entity.*;
import com.jimi.uw_server.ur.handler.assist.PackSender;

import java.util.List;

public class MessageHandler {

	public static MessageHandler me = new MessageHandler();

	public static RegularIOTaskService ioTaskService = Aop.get(RegularIOTaskService.class);

	public static RobotService robotService = Aop.get(RobotService.class);


	/**
	 * 处理扫描物料包
	 * 
	 * @param scanMaterialInfoPackage
	 */
	public void handleScanMaterialInfoPackage(ScanMaterialInfoPackage scanMaterialInfoPackage) {
		Task task = Task.dao.findById(scanMaterialInfoPackage.getTaskId());
		if (task.getType().equals(TaskType.COUNT)) {
			synchronized (Lock.UR_INV_TASK_LOCK) {
				List<UrMaterialInfo> urMaterialInfos = UrTaskInfoDAO.getUrMaterialInfos(scanMaterialInfoPackage.getTaskId(), scanMaterialInfoPackage.getBoxId());
				if (!urMaterialInfos.isEmpty() && urMaterialInfos.size() > 0) {
					boolean isScanFlag = false;
					boolean isAllFinishFlag = true;
					for (UrMaterialInfo urMaterialInfo : urMaterialInfos) {
						if (urMaterialInfo.getIsScaned()) {
							continue;
						}
						if (!urMaterialInfo.getMaterialId().equals(scanMaterialInfoPackage.getMaterialId())) {
							isAllFinishFlag = false;
							continue;
						}
						urMaterialInfo.setIsScaned(true);
						isScanFlag = true;
					}
					if (isScanFlag) {
						UrTaskInfoDAO.putUrMaterialInfos(scanMaterialInfoPackage.getTaskId(), scanMaterialInfoPackage.getBoxId(), urMaterialInfos);
						RegularInventoryTaskService.me.inventoryUrUWMaterial(scanMaterialInfoPackage.getMaterialId(), scanMaterialInfoPackage.getBoxId(), scanMaterialInfoPackage.getTaskId());
						UrOperationMaterialInfoDAO.removeUrTaskBoxArrivedPack("robot1");
					}
					if (isAllFinishFlag) {
						RegularInventoryTaskService.me.backUrUWBox(scanMaterialInfoPackage.getTaskId(), scanMaterialInfoPackage.getBoxId(), "robot1");
						UrTaskInfoDAO.removeUrMaterialInfosByTaskAndBox(scanMaterialInfoPackage.getTaskId(), scanMaterialInfoPackage.getBoxId());
					}
				}
			}
		} else if (task.getType().equals(TaskType.OUT)) {
			List<UrMaterialInfo> urMaterialInfos = UrTaskInfoDAO.getUrMaterialInfos(scanMaterialInfoPackage.getTaskId(), scanMaterialInfoPackage.getBoxId());
			UrMaterialInfo presentInfo = null;
			if (!urMaterialInfos.isEmpty() && urMaterialInfos.size() > 0) {
				boolean isScanFlag = false;
				boolean isAllFinishFlag = true;
				for (UrMaterialInfo urMaterialInfo : urMaterialInfos) {
					if (urMaterialInfo.getIsScaned()) {
						continue;
					}
					if (!urMaterialInfo.getMaterialId().equals(scanMaterialInfoPackage.getMaterialId())) {
						isAllFinishFlag = false;
						continue;
					}
					ioTaskService.urOut(urMaterialInfo);
					urMaterialInfo.setIsScaned(true);
					presentInfo = urMaterialInfo;
					isScanFlag = true;
				}
				if (isScanFlag) {

					UrTaskInfoDAO.putUrMaterialInfos(scanMaterialInfoPackage.getTaskId(), scanMaterialInfoPackage.getBoxId(), urMaterialInfos);
					UrOperationMaterialInfoDAO.removeUrTaskBoxArrivedPack("robot1");
				}
				if (isAllFinishFlag) {
					try {
						robotService.urOutTaskBack(presentInfo.getIoItemId(), null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}


	/**
	 * 处理机械臂请求夹取位置包
	 * 
	 * @param askPostionPackage
	 */
	public void handleAskPostionPackage(AskPostionPackage askPostionPackage) {
		Task task = Task.dao.findById(askPostionPackage.getTaskId());
		Boolean flag = false;
		List<UrMaterialInfo> urMaterialInfos = UrTaskInfoDAO.getUrMaterialInfos(askPostionPackage.getTaskId(), askPostionPackage.getBoxId());

		MaterialPositionInfoPackage materialPositionInfoPackage = new MaterialPositionInfoPackage();
		materialPositionInfoPackage.setBoxId(askPostionPackage.getBoxId());
		materialPositionInfoPackage.setTaskId(askPostionPackage.getTaskId());
		if (task.getType().equals(TaskType.COUNT)) {
			materialPositionInfoPackage.setType(Constant.UR_INVENTORY_TASK);
		} else if (task.getType().equals(TaskType.OUT)) {
			materialPositionInfoPackage.setType(Constant.UR_OUT_TASK);
		}
		for (UrMaterialInfo urMaterialInfo : urMaterialInfos) {
			if (urMaterialInfo.getIsScaned()) {
				continue;
			}
			flag = true;
			materialPositionInfoPackage.setMaterialId(urMaterialInfo.getMaterialId());
			materialPositionInfoPackage.setxPosition(urMaterialInfo.getxPosition());
			materialPositionInfoPackage.setyPosition(urMaterialInfo.getyPosition());
			materialPositionInfoPackage.setQuantity(urMaterialInfo.getQuantity());
			UrOperationMaterialInfoDAO.putUrTaskBoxArrivedPack("robot1", urMaterialInfo);

			break;
		}
		if (!flag) {
			materialPositionInfoPackage.setMaterialId("null");
			materialPositionInfoPackage.setxPosition(9999);
			materialPositionInfoPackage.setyPosition(9999);
			materialPositionInfoPackage.setQuantity(9999);
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				PackSender.sendPackage("robot1", materialPositionInfoPackage);
			}
		};
		ProcessorExecutor.me.execute(runnable);

	}


	/**
	 * 处理扫描物料异常包
	 * 
	 * @param scanMaterialExceptionPackage
	 */
	public void handleScanMaterialExceptionPackage(ScanMaterialExceptionPackage scanMaterialExceptionPackage) {
		Task task = Task.dao.findById(scanMaterialExceptionPackage.getTaskId());
		if (task.getType().equals(TaskType.COUNT)) {
			synchronized (Lock.UR_INV_TASK_LOCK) {
				List<UrMaterialInfo> urMaterialInfos = UrTaskInfoDAO.getUrMaterialInfos(scanMaterialExceptionPackage.getTaskId(), scanMaterialExceptionPackage.getBoxId());
				boolean flag = false;
				for (UrMaterialInfo urMaterialInfo : urMaterialInfos) {
					if (urMaterialInfo.getIsScaned() || !urMaterialInfo.getMaterialId().equals(scanMaterialExceptionPackage.getMaterialId())) {
						continue;
					}
					flag = true;
					urMaterialInfo.setExceptionCode(scanMaterialExceptionPackage.getExceptionCode());
					UrMaterialInfo urMaterialInfoTemp = UrOperationMaterialInfoDAO.getUrOperationMaterialInfoByUrName("robot1");
					if (urMaterialInfoTemp.getMaterialId().equals(scanMaterialExceptionPackage.getMaterialId())) {
						UrOperationMaterialInfoDAO.putUrTaskBoxArrivedPack("robot1", urMaterialInfo);
					}
				}
				if (flag) {
					UrTaskInfoDAO.putUrMaterialInfos(scanMaterialExceptionPackage.getTaskId(), scanMaterialExceptionPackage.getBoxId(), urMaterialInfos);
				}
			}
		} else if (task.getType().equals(TaskType.OUT)) {
			synchronized (Lock.UR_OUT_TASK_LOCK) {
				List<UrMaterialInfo> urMaterialInfos = UrTaskInfoDAO.getUrMaterialInfos(scanMaterialExceptionPackage.getTaskId(), scanMaterialExceptionPackage.getBoxId());
				boolean flag = false;
				for (UrMaterialInfo urMaterialInfo : urMaterialInfos) {
					if (urMaterialInfo.getIsScaned() || !urMaterialInfo.getMaterialId().equals(scanMaterialExceptionPackage.getMaterialId())) {
						continue;
					}
					flag = true;
					urMaterialInfo.setExceptionCode(scanMaterialExceptionPackage.getExceptionCode());
					UrMaterialInfo urMaterialInfoTemp = UrOperationMaterialInfoDAO.getUrOperationMaterialInfoByUrName("robot1");
					if (urMaterialInfoTemp.getMaterialId().equals(scanMaterialExceptionPackage.getMaterialId())) {
						UrOperationMaterialInfoDAO.putUrTaskBoxArrivedPack("robot1", urMaterialInfo);
					}
				}
				if (flag) {
					UrTaskInfoDAO.putUrMaterialInfos(scanMaterialExceptionPackage.getTaskId(), scanMaterialExceptionPackage.getBoxId(), urMaterialInfos);
				}
			}
		}
	}
}
