package com.jimi.uw_server.agv.socket;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.jfinal.json.Json;
import com.jfinal.kit.PropKit;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.cmd.AGVStatusCmd;
import com.jimi.uw_server.agv.entity.cmd.base.AGVBaseCmd;
import com.jimi.uw_server.agv.handle.ACKHandler;
import com.jimi.uw_server.agv.handle.BuildHandler;
import com.jimi.uw_server.agv.handle.ExceptionHandler;
import com.jimi.uw_server.agv.handle.IOTaskHandler;
import com.jimi.uw_server.agv.handle.InvTaskHandler;
import com.jimi.uw_server.agv.handle.SamTaskHandler;
import com.jimi.uw_server.model.SocketLog;
import com.jimi.uw_server.util.ErrorLogWritter;


/**
 * 与AGV服务器进行通讯的主要类
 * <br>
 * <b>2018年6月15日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
@ClientEndpoint
public class AGVMainSocket {

	private static Session session;

	private static String uri;

	/**
	 * 发送的CMDID与是否被ACK的关系映射
	 */
	private static Map<Integer, Boolean> sendCmdidAckMap;
	/**
	 * 已收到的非ACK的CMDID集合
	 */
	private static Set<Integer> receiveNotAckCmdidSet;

	private static IOTaskHandler ioTaskHandler = IOTaskHandler.getInstance();

	private static InvTaskHandler invTaskHandler = InvTaskHandler.getInstance();

	private static SamTaskHandler samTaskHandler = SamTaskHandler.getInstance();


	public static void init(String uri) throws Exception {
		// 初始化
		sendCmdidAckMap = new HashMap<>();
		receiveNotAckCmdidSet = new HashSet<>();
		// 连接AGV服务器
		AGVMainSocket.uri = uri;
		connect(AGVMainSocket.uri);
	}


	@OnOpen
	public void onOpen(Session userSession) {
		System.out.println("AGVMainSocket is Running Now...");
		session = userSession;
		TaskItemRedisDAO.setAgvWebSocketStatus(true);
	}


	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		ErrorLogWritter.save("AGVMainSocket was Stopped because :" + reason.getCloseCode());
		TaskItemRedisDAO.setAgvWebSocketStatus(false);
		connect(AGVMainSocket.uri);
	}


	@OnMessage
	public void onMessage(String message, Session session) {
		AGVMainSocket.session = session;
		try {
			log(false, message);
			// 判断是否是ack指令
			if (message.contains("\"cmdcode\":\"ack\"")) {// ack指令
				ACKHandler.handleACK(message);
			} else if (message.contains("\"cmdcode\"")) {// 非ack指令
				if (ACKHandler.handleNOTACK(message)) {
					// 判断是否是status指令
					if (message.contains("\"cmdcode\":\"status\"")) {
						// 转换成实体类
						AGVStatusCmd statusCmd = Json.getJson().parse(message, AGVStatusCmd.class);
						// missiongroupid 包含“:”表示为出入库任务
						if (statusCmd.getMissiongroupid().contains(":")) {
							// 判断是否是开始执行任务
							ioTaskHandler.handleStatus(statusCmd);
						} else if (statusCmd.getMissiongroupid().contains("@")) {
							invTaskHandler.handleStatus(statusCmd);
						} else if (statusCmd.getMissiongroupid().contains("#")) {
							// 抽检任务
							samTaskHandler.handleStatus(statusCmd);
						} else {
							BuildHandler.handleStatus(message);
						}
					} else if (message.contains("\"cmdcode\":\"loadexception\"")) {// 判断是否是loadexception指令
						ExceptionHandler.handleLoadException(message);
					} else if (message.contains("\"cmdcode\":\"agv_del_mission\"")) {// 判断是否是agv_del_mission指令
						ExceptionHandler.handleDelMissionException(message);
					}
				}
			}
		} catch (Exception e) {
			ErrorLogWritter.save(e.getClass().getSimpleName() + ":" + e.getMessage());
			e.printStackTrace();
		}
	}


	/**
	 * 使用websocket发送一条ACK到AGV服务器
	 */
	public static void sendACK(String message) throws IOException {
		send(message);
	}


	/**
	 * 使用websocket发送一条消息到AGV服务器
	 */
	public static void sendMessage(String message) throws Exception {
		int waitAckTimeout = PropKit.use("properties.ini").getInt("taskPoolCycle");
		int cmdid = Json.getJson().parse(message, AGVBaseCmd.class).getCmdid();
		send(message);
		sendCmdidAckMap.put(cmdid, false);
		Thread.sleep(waitAckTimeout);
		while (!sendCmdidAckMap.get(cmdid)) {
			send(message);
			Thread.sleep(waitAckTimeout);
		}
	}


	public static Map<Integer, Boolean> getSendCmdidAckMap() {
		return sendCmdidAckMap;
	}


	public static Set<Integer> getReceiveNotAckCmdidSet() {
		return receiveNotAckCmdidSet;
	}


	private static void connect(String uri) {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		Boolean flag = true;
		while (flag) {
			try {
				Thread.sleep(3000);
				container.connectToServer(new AGVMainSocket(), new URI(uri));
				flag = false;
			} catch (Exception e) {
				ErrorLogWritter.save(e.getClass().getSimpleName() + ":" + e.getMessage());
				e.printStackTrace();
			}
		}

	}


	private static void send(String message) throws IOException {
		synchronized (AGVMainSocket.class) {
			log(true, message);
			session.getBasicRemote().sendText(message);
		}
	}


	private static void log(Boolean isSend, String message) {
		if (isSend) {
			System.out.println("[" + new Date().toString() + "]" + "send message:" + message);
		} else {
			System.out.println("[" + new Date().toString() + "]" + "receiver message:" + message);
		}
		int cmdid = -1;
		String cmdcode = "-";
		try {
			AGVBaseCmd baseCmd = Json.getJson().parse(message, AGVBaseCmd.class);
			cmdid = baseCmd.getCmdid();
			cmdcode = baseCmd.getCmdcode();
		} catch (Exception e) {
		}
		SocketLog log = new SocketLog();
		log.setCmdid(cmdid);
		log.setCmdcode(cmdcode);
		log.setIsSend(isSend);
		log.setTime(new Date());
		log.setJson(message);
		log.save();
	}

}