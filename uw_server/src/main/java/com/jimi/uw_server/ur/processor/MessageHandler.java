package com.jimi.uw_server.ur.processor;

import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.service.InventoryTaskService;
import com.jimi.uw_server.ur.constant.Constant;
import com.jimi.uw_server.ur.dao.UrInvTaskInfoDAO;
import com.jimi.uw_server.ur.dao.UrOperationMaterialInfoDAO;
import com.jimi.uw_server.ur.entity.*;
import com.jimi.uw_server.ur.handler.assist.PackSender;

import java.util.List;

public class MessageHandler {

    public static MessageHandler me = new MessageHandler();

    /**
     * 处理扫描物料包
     * @param scanMaterialInfoPackage
     */
    public void handleScanMaterialInfoPackage(ScanMaterialInfoPackage scanMaterialInfoPackage) {
        Task task = Task.dao.findById(scanMaterialInfoPackage.getTaskId());
        if (task.getType().equals(TaskType.COUNT)) {
            List<UrMaterialInfo> urMaterialInfos = UrInvTaskInfoDAO.getUrMaterialInfos(scanMaterialInfoPackage.getTaskId(), scanMaterialInfoPackage.getBoxId());
            if (!urMaterialInfos.isEmpty() && urMaterialInfos.size() > 0) {
                boolean isScanFlag = false;
                boolean isAllFinishFlag = true;
                for (UrMaterialInfo urMaterialInfo : urMaterialInfos) {
                    if (urMaterialInfo.getIsScaned()) {
                        continue;
                    }
                    if (!urMaterialInfo.getMaterialId().equals(scanMaterialInfoPackage.getMaterialId())){
                        isAllFinishFlag = false;
                        continue;
                    }
                    urMaterialInfo.setIsScaned(true);
                    isScanFlag = true;
                }
                if (isScanFlag) {
                    UrInvTaskInfoDAO.putUrMaterialInfos(scanMaterialInfoPackage.getTaskId(), scanMaterialInfoPackage.getBoxId(), urMaterialInfos);
                    InventoryTaskService.me.inventoryUrRegularUWMaterial(scanMaterialInfoPackage.getMaterialId(), scanMaterialInfoPackage.getBoxId(), scanMaterialInfoPackage.getTaskId());
                    UrOperationMaterialInfoDAO.removeUrTaskBoxArrivedPack("robot1");
                }
                if (isAllFinishFlag) {
                   InventoryTaskService.me.backUrInventoryRegularUWBox(scanMaterialInfoPackage.getTaskId(), scanMaterialInfoPackage.getBoxId(),  "robot1");
                    UrInvTaskInfoDAO.removeUrMaterialInfosByTaskAndBox(scanMaterialInfoPackage.getTaskId(), scanMaterialInfoPackage.getBoxId());
                }
            }
        }

    }

    /**
     * 处理机械臂请求夹取位置包
     * @param askPostionPackage
     */
    public void handleAskPostionPackage(AskPostionPackage askPostionPackage){
        Task task = Task.dao.findById(askPostionPackage.getTaskId());
        if (task.getType().equals(TaskType.COUNT)) {
            List<UrMaterialInfo> urMaterialInfos = UrInvTaskInfoDAO.getUrMaterialInfos(askPostionPackage.getTaskId(), askPostionPackage.getBoxId());
            for (UrMaterialInfo urMaterialInfo : urMaterialInfos) {
                if (urMaterialInfo.getIsScaned()) {
                    continue;
                }
                MaterialPositionInfoPackage materialPositionInfoPackage = new MaterialPositionInfoPackage();
                materialPositionInfoPackage.setBoxId(urMaterialInfo.getBoxId());
                materialPositionInfoPackage.setTaskId(urMaterialInfo.getTaskId());
                materialPositionInfoPackage.setMaterialId(urMaterialInfo.getMaterialId());
                materialPositionInfoPackage.setType(Constant.UR_INVENTORY_TASK);
                materialPositionInfoPackage.setxPosition(urMaterialInfo.getxPosition());
                materialPositionInfoPackage.setyPosition(urMaterialInfo.getyPosition());
                materialPositionInfoPackage.setQuantity(urMaterialInfo.getQuantity());
                UrOperationMaterialInfoDAO.putUrTaskBoxArrivedPack("robot1", urMaterialInfo);
                Runnable runnable = new Runnable() {

					@Override
					public void run() {
						PackSender.sendPackage("robot1", materialPositionInfoPackage);
					}
				};
                ProcessorExecutor.me.execute(runnable);
                break;
            }
        }
    }

    /**
     * 处理扫描物料异常包
     * @param scanMaterialExceptionPackage
     */
    public void handleScanMaterialExceptionPackage(ScanMaterialExceptionPackage scanMaterialExceptionPackage){
        Task task = Task.dao.findById(scanMaterialExceptionPackage.getTaskId());
        if (task.getType().equals(TaskType.COUNT)) {
            List<UrMaterialInfo> urMaterialInfos = UrInvTaskInfoDAO.getUrMaterialInfos(scanMaterialExceptionPackage.getTaskId(), scanMaterialExceptionPackage.getBoxId());
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
                UrInvTaskInfoDAO.putUrMaterialInfos(scanMaterialExceptionPackage.getTaskId(), scanMaterialExceptionPackage.getBoxId(), urMaterialInfos);
            }
        }
    }
}
