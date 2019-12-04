package com.jimi.uw_server.printer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jimi.uw_server.exception.PrinterSocketException;
import com.jimi.uw_server.model.PrinterInfo;


/**
 * 打印机服务端socket
 * @author coke
 */
@ServerEndpoint("/print.ws/{printerIP}")
public class PrintServerSocket {

	// 当前存在的客户端<ip,Session>
	private static Map<String, Session> clients = new HashMap<>();
	// 客户端返回的信息<id,result>
	private static Map<String, String> results = new HashMap<>();


	@OnOpen
	public void onOpen(@PathParam("printerIP") String printerIP, Session session) {
		clients.put(printerIP, session);
	}


	@OnClose
	public void onClose(Session session, @PathParam("printerIP") String printerIP) {
		
		try { session.close(); } catch (IOException e) { throw new
		PrinterSocketException(e.getMessage()); }
		
		clients.remove(printerIP);
	}


	@OnMessage
	public void onMessage(String result) {
		// 存取返回的数据
		try {
			JSONObject jsonObject = JSON.parseObject(result);
			if (results.containsKey(jsonObject.getString("id"))) {
				results.put(jsonObject.getString("id"), result);
			}
		} catch (Exception e) {
			throw new PrinterSocketException(e.getMessage());
		}
	}


	@OnError
	public void onError(@PathParam("printerIP") String printerIP, Session session, Throwable error) {
		try {
			session.close();
		} catch (IOException e) {
			throw new PrinterSocketException(e.getMessage());
		}
		clients.remove(printerIP);
		error.printStackTrace();
	}


	public synchronized static void send(String printerIP, String id, String materialId, String materialNo, String remainingQuantity, String productDate, String user, String supplier, String cycle, String manufacturer, String specification, String designator, Integer type, String printTime) throws IOException {
		results.put(id, null);
		PrinterInfo printerInfo = new PrinterInfo(id, materialId, user, productDate, remainingQuantity, materialNo, supplier, cycle, manufacturer, specification, designator, type, printTime);
		JSONObject jsonObject = (JSONObject) JSONObject.toJSON(printerInfo);
		Session session = clients.get(printerIP);
		session.getBasicRemote().sendText(jsonObject.toJSONString());
	}


	public synchronized static Map<String, Session> getClients() {
		return clients;
	}


	public synchronized static Map<String, String> getResults() {
		return results;
	}
}