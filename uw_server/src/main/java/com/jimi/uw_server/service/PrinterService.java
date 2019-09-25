package com.jimi.uw_server.service;

import java.io.IOException;
import java.text.SimpleDateFormat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Material;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.printer.PrintServerSocket;


public class PrinterService {

	public static PrinterService me = new PrinterService();
	// 等待反馈的时间
	private static final int TIME_OUT = 10000;


	public synchronized String print(String ip, String materialId, Integer quantity, User user) throws InterruptedException, IOException {
		if (PrintServerSocket.getClients().containsKey(ip)) {

			long startTime = System.currentTimeMillis();
			Long id = PrintServerSocket.getResults().size() + 1 + startTime;
			Material material = Material.dao.findById(materialId);
			if (material == null) {
				throw new OperationException("该料盘不存在，打印失败");
			}
			MaterialType materialType = MaterialType.dao.findById(material.getType());
			Supplier supplier = Supplier.dao.findById(materialType.getSupplier());
			// 发送打印信息
			String cycle = material.getCycle() == null ? "无" : material.getCycle().toString();
			String specification = materialType.getSpecification();
			String manufacturer = material.getManufacturer() == null ? "无": material.getManufacturer();
			String designator = materialType.getDesignator() == null ? "" : materialType.getDesignator();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String dateString = dateFormat.format(material.getProductionTime());
			try {
				PrintServerSocket.send(ip, id.toString(), materialId, materialType.getNo(), quantity.toString(), dateString, user.getUid(), supplier.getName(), cycle, manufacturer, specification, designator);
			} catch (Exception e) {
				PrintServerSocket.getResults().remove(id.toString());
				PrintServerSocket.getClients().get(ip).close();
				PrintServerSocket.getClients().remove(ip);
				throw new OperationException("发送打印信息失败,请检查打印机连接");
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
					throw new OperationException("连接失败,请检查打印机连接");
				}
			}
			PrintServerSocket.getResults().remove(id.toString());
			PrintServerSocket.getClients().get(ip).close();
			PrintServerSocket.getClients().remove(ip);
			throw new OperationException("打印信息发送超时,请检查打印机连接");
		} else {
			throw new OperationException("连接失败,请检查打印机连接");
		}
	}
}
