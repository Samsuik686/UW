package com.jimi.uw_server.ur.processor;

import com.jimi.uw_server.lock.Lock;
import com.jimi.uw_server.service.InventoryTaskService;
import com.jimi.uw_server.ur.entity.*;


public class MessageHandler {

    public static MessageHandler me = new MessageHandler();

    /**
     * 处理扫描物料包
     * @param scanMaterialInfoPackage
     */
    public void handleInvMaterialScanInfoPackage(InvMaterialScanInfoPackage invMaterialScanInfoPackage) {
        
        synchronized (Lock.UR_INV_TASK_LOCK) {
        		
            InventoryTaskService.me.inventoryUrRegularUWMaterial(invMaterialScanInfoPackage.getMaterialId(), invMaterialScanInfoPackage.getBoxId(), invMaterialScanInfoPackage.getTaskId(), invMaterialScanInfoPackage.getxPosition(), invMaterialScanInfoPackage.getyPosition(), invMaterialScanInfoPackage.getQuantity(), invMaterialScanInfoPackage.getResult());
            
            if (invMaterialScanInfoPackage.getEndFlag() == 1) {
                InventoryTaskService.me.backUrInventoryRegularUWBox(invMaterialScanInfoPackage.getTaskId(), invMaterialScanInfoPackage.getBoxId(), "robot1");
            }
        }

    }

	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年4月30日
	 */
	public void handleScanMaterialExceptionPackage(ScanMaterialExceptionPackage msg) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * <p>Description: <p>
	 * @return
	 * @exception
	 * @author trjie
	 * @Time 2020年4月30日
	 */
	public void handleAskPostionPackage(AskPostionPackage msg) {
		// TODO Auto-generated method stub
		
	}

}
