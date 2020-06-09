package com.jimi.uw_server.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jimi.uw_server.constant.MaterialStatus;
import com.jimi.uw_server.constant.WarehouseType;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.*;
import com.jimi.uw_server.printer.PrintServerSocket;

import java.io.IOException;
import java.text.SimpleDateFormat;


public class PrinterService {

	public static PrinterService me = new PrinterService();
	// 等待反馈的时间
	private static final int TIME_OUT = 10000;


	public synchronized String print(String ip, String materialId, Integer packingListItemId, User user) throws InterruptedException, IOException {
		if (PrintServerSocket.getClients().containsKey(ip)) {

			long startTime = System.currentTimeMillis();
			Long id = PrintServerSocket.getResults().size() + 1 + startTime;
			Material material = Material.dao.findById(materialId);
			if (material == null) {
				throw new OperationException("该料盘不存在，打印失败！");
			}
			if (!material.getStatus().equals(MaterialStatus.CUTTING) || material.getCutTaskLogId() == null) {
				throw new OperationException("该料盘未处于截料状态，打印失败！");
			}
			MaterialType materialType = MaterialType.dao.findById(material.getType());
			Supplier supplier = Supplier.dao.findById(materialType.getSupplier());
			// 发送打印信息
			String cycle = material.getCycle() == null ? "无" : material.getCycle();
			String specification = materialType.getSpecification();
			String manufacturer = material.getManufacturer() == null ? "无" : material.getManufacturer();
			String designator = materialType.getDesignator() == null ? "" : materialType.getDesignator();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String dateString = dateFormat.format(material.getProductionTime());
			String printTimeString = null;
			if (material.getPrintTime() != null) {
				printTimeString = dateFormat.format(material.getPrintTime());
			}
			long startTime2 = System.currentTimeMillis();
			Long id2 = PrintServerSocket.getResults().size() + 1 + startTime2;
			TaskLog taskLog = TaskLog.dao.findById(material.getCutTaskLogId());
			if (taskLog == null || taskLog.getQuantity() == null) {
				throw new OperationException("发送打印信息失败,请检查物料是否处于截料状态！");
			}
			try {
				if (materialType.getType().equals(WarehouseType.REGULAR.getId())) {
					PrintServerSocket.send(ip, id.toString(), materialId, materialType.getNo(), String.valueOf(material.getRemainderQuantity()), dateString, user.getUid(), supplier.getName(), cycle,
							manufacturer, specification, designator, 1, printTimeString, material.getCompanyId());
				} else {
					PrintServerSocket.send(ip, id.toString(), materialId, materialType.getNo(), String.valueOf(material.getRemainderQuantity() - taskLog.getQuantity()), dateString, user.getUid(),
							supplier.getName(), cycle, manufacturer, specification, designator, 1, printTimeString, material.getCompanyId());
				}
			} catch (Exception e) {
				PrintServerSocket.getResults().remove(id.toString());
				PrintServerSocket.getClients().get(ip).close();
				PrintServerSocket.getClients().remove(ip);
				e.printStackTrace();
				throw new OperationException("发送打印信息失败,请检查打印机连接！");
			}

			if (taskLog != null && taskLog.getQuantity() != null && !taskLog.getQuantity().equals(0)) {
				try {
					PrintServerSocket.send(ip, id2.toString(), materialId, materialType.getNo(), String.valueOf(taskLog.getQuantity()), dateString, user.getUid(), supplier.getName(), cycle,
							manufacturer, specification, designator, 0, printTimeString, material.getCompanyId());

				} catch (Exception e) {
					PrintServerSocket.getResults().remove(id.toString());
					PrintServerSocket.getClients().get(ip).close();
					PrintServerSocket.getClients().remove(ip);
					e.printStackTrace();
					throw new OperationException("发送打印信息失败,请检查打印机连接！");
				}
			}

			// 监听客户端返回的信息
			while (System.currentTimeMillis() - startTime < TIME_OUT) {
				Thread.sleep(500);
				if (PrintServerSocket.getResults().get(id.toString()) != null) {
					String returnInfo = PrintServerSocket.getResults().get(id.toString());
					JSONObject jsonObject = JSON.parseObject(returnInfo);
					PrintServerSocket.getResults().remove(id.toString());
					return jsonObject.getString("data");
				} else if (!PrintServerSocket.getClients().containsKey(ip)) {
					PrintServerSocket.getResults().remove(id.toString());
					throw new OperationException("连接失败,请检查打印机连接！");
				}
				if (PrintServerSocket.getResults().get(id2.toString()) != null) {
					String returnInfo = PrintServerSocket.getResults().get(id2.toString());
					JSONObject jsonObject = JSON.parseObject(returnInfo);
					PrintServerSocket.getResults().remove(id2.toString());
					return jsonObject.getString("data");
				} else if (!PrintServerSocket.getClients().containsKey(ip)) {
					PrintServerSocket.getResults().remove(id2.toString());
					throw new OperationException("连接失败,请检查打印机连接！");
				}
			}
			PrintServerSocket.getResults().remove(id.toString());
			PrintServerSocket.getResults().remove(id2.toString());
			PrintServerSocket.getClients().get(ip).close();
			PrintServerSocket.getClients().remove(ip);
			throw new OperationException("打印信息发送超时,请检查打印机连接！");
		} else {
			throw new OperationException("连接失败,请检查打印机连接！");
		}
	}


	public synchronized String printSingle(String ip, String materialId, Integer quantity, User user) throws InterruptedException, IOException {
		if (PrintServerSocket.getClients().containsKey(ip)) {

			long startTime = System.currentTimeMillis();
			Long id = PrintServerSocket.getResults().size() + 1 + startTime;
			Material material = Material.dao.findById(materialId);
			if (material == null) {
				throw new OperationException("该料盘不存在，打印失败！");
			}
			MaterialType materialType = MaterialType.dao.findById(material.getType());
			Supplier supplier = Supplier.dao.findById(materialType.getSupplier());
			// 发送打印信息
			String cycle = material.getCycle() == null ? "无" : material.getCycle();
			String specification = materialType.getSpecification();
			String manufacturer = material.getManufacturer() == null ? "无" : material.getManufacturer();
			String designator = materialType.getDesignator() == null ? "" : materialType.getDesignator();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String dateString = dateFormat.format(material.getProductionTime());
			String printTimeString = null;
			if (material.getPrintTime() != null) {
				printTimeString = dateFormat.format(material.getPrintTime());
			}
			try {
				PrintServerSocket.send(ip, id.toString(), materialId, materialType.getNo(), String.valueOf(quantity), dateString, user.getUid(), supplier.getName(), cycle, manufacturer, specification,
						designator, 0, printTimeString, material.getCompanyId());
			} catch (Exception e) {
				PrintServerSocket.getResults().remove(id.toString());
				PrintServerSocket.getClients().get(ip).close();
				PrintServerSocket.getClients().remove(ip);
				e.printStackTrace();
				throw new OperationException("发送打印信息失败,请检查打印机连接！");
			}
			// 监听客户端返回的信息
			while (System.currentTimeMillis() - startTime < TIME_OUT) {
				Thread.sleep(500);
				if (PrintServerSocket.getResults().get(id.toString()) != null) {
					String returnInfo = PrintServerSocket.getResults().get(id.toString());
					System.out.println(returnInfo);
					JSONObject jsonObject = JSON.parseObject(returnInfo);
					PrintServerSocket.getResults().remove(id.toString());
					return jsonObject.getString("data");
				} else if (!PrintServerSocket.getClients().containsKey(ip)) {
					PrintServerSocket.getResults().remove(id.toString());
					throw new OperationException("连接失败,请检查打印机连接！");
				}
			}
			PrintServerSocket.getResults().remove(id.toString());
			PrintServerSocket.getClients().get(ip).close();
			PrintServerSocket.getClients().remove(ip);
			throw new OperationException("打印信息发送超时,请检查打印机连接！");
		} else {
			throw new OperationException("连接失败,请检查打印机连接！");
		}
	}
}
