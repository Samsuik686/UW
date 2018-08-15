package com.jimi.uw_server.agv.socket;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.jfinal.json.Json;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVRobot;
import com.jimi.uw_server.agv.entity.cmd.AGVRobotInfoCmd;
import com.jimi.uw_server.model.bo.RobotBO;
import com.jimi.uw_server.util.ErrorLogWritter;

/**
 * 实时接收机器信息的类
 * <br>
 * <b>2018年6月15日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
@ClientEndpoint
public class RobotInfoSocket{
	
	private static String uri;
	
	
	public static void init(String uri) {
		try {
			//连接AGV服务器
			RobotInfoSocket.uri = uri;
			connect(uri);
		} catch (Exception e) {
			ErrorLogWritter.save(e.getClass().getSimpleName() + ":" + e.getMessage());
			e.printStackTrace();
		}
	}


	@OnOpen
	public void onOpen(Session userSession) {
		System.out.println("RobotInfoSocket is Running Now...");
		try {
		} catch (Exception e) {
			ErrorLogWritter.save(e.getClass().getSimpleName() + ":" + e.getMessage());
			e.printStackTrace();
		}
	}


	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		ErrorLogWritter.save("RobotInfoSocket was Stopped because :" + reason.getReasonPhrase());
		try {
			Thread.sleep(3000);
			//重新连接
			connect(uri);
		} catch (Exception e) {
			ErrorLogWritter.save(e.getClass().getSimpleName()+ ":" +e.getMessage());
			e.printStackTrace();
		}
	}

	
	@OnMessage
	public void onMessage(String message ,Session session) {
		try {
			//获取新的机器数据
			AGVRobotInfoCmd robotInfoCmd = Json.getJson().parse(message, AGVRobotInfoCmd.class);
			
			List<RobotBO> robotBOs = new ArrayList<>();
			for (AGVRobot agvRobot : robotInfoCmd.getRobotarray()) {
				robotBOs.add(AGVRobot.toBO(agvRobot));
			}
			
			//补充取空异常数据
			List<RobotBO> robotBOs2 = RobotInfoRedisDAO.check();
			for (RobotBO robotBO : robotBOs) {
				for (RobotBO robotBO2 : robotBOs2) {
					if(robotBO.getId().intValue() == robotBO2.getId().intValue()) {
						robotBO.setLoadException(robotBO2.getLoadException());
						break;
					}
				}
			}
			
			//更新机器信息
  			RobotInfoRedisDAO.update(robotBOs);
			
		}catch (Exception e) {
			ErrorLogWritter.save(e.getClass().getSimpleName() + ":" + e.getMessage());
			e.printStackTrace();
		}
	}


	private static void connect(String uri) throws Exception {
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		container.connectToServer(new RobotInfoSocket(), new URI(uri));
	}

}