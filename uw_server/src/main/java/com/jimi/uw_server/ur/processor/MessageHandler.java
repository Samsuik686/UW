package com.jimi.uw_server.ur.processor;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import javax.websocket.Session;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Enhancer;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.handle.IOHandler;
import com.jimi.uw_server.constant.IOTaskItemState;
import com.jimi.uw_server.model.MaterialBox;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.service.TaskService;
import com.jimi.uw_server.ur.constant.UrCmdType;
import com.jimi.uw_server.ur.entity.AckPackage;
import com.jimi.uw_server.ur.entity.IOPackage;
import com.jimi.uw_server.ur.entity.ResultPackage;
import com.jimi.uw_server.ur.entity.base.UrBasePackage;
import com.jimi.uw_server.ur.entity.base.UrMaterialInfo;
import com.jimi.uw_server.ur.socket.UrSocekt;

import cc.darhao.dautils.api.DateUtil;


/**
 * UrSocket消息处理器
 * <br>
 * <b>2019年5月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class MessageHandler {

	private TaskService taskService = Enhancer.enhance(TaskService.class);
	
	private Session session;
	
	
	public MessageHandler(Session session) {
		this.session = session;
	}
	
	
	public void handle(String message) throws Exception{
		try {
			//转换成package对象
			UrBasePackage basePackage = toPackage(parseJsonObject(message));
			//判断是不是ack
			if(basePackage instanceof AckPackage) {
				//接受并处理这个ack
				UrSocekt.queueHolder.acceptAck((AckPackage) basePackage);
			}else {
				//回复ack
				replyAck(basePackage.getCmdid());
				//判断类型并分发处理
				if(basePackage instanceof IOPackage) {
					handleInPackage((IOPackage) basePackage);
				}else if(basePackage instanceof ResultPackage){
					handleResultPackage((ResultPackage) basePackage);
				}
			}
		}catch (RuntimeException e) {
			session.getBasicRemote().sendText(e.getMessage());
		}
	}


	private void replyAck(int cmdid) throws IOException {
		AckPackage ack = new AckPackage();
		ack.setCmdid(cmdid);
		session.getBasicRemote().sendText(JSON.toJSONString(ack));
	}


	private UrBasePackage toPackage(JSONObject jsonObject) {
		switch (jsonObject.get("cmdcode").toString()) {
		case UrCmdType.ACK :
			return jsonObject.toJavaObject(AckPackage.class);
		case UrCmdType.IN :
			return jsonObject.toJavaObject(IOPackage.class);
		case UrCmdType.RESULT :
			return jsonObject.toJavaObject(ResultPackage.class);
		default:
			throw new JSONException("非法cmdcode："+jsonObject.get("cmdcode").toString());
		}
	}


	private JSONObject parseJsonObject(String message) {
		JSONObject jsonObject = JSON.parseObject(message);
		if(jsonObject.get("cmdid") == null || jsonObject.get("cmdcode") == null) {
			throw new JSONException("缺乏cmdid或cmdcode字段："+message);
		}
		return jsonObject;
	}

	
	/**
	 * 处理入库结果包
	 * @throws Exception 
	 */
	private void handleInPackage(IOPackage inPackage) throws Exception {
		//判断是否存在空值
		if(inPackage.containsNullFields()) {
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
	private void handleResultPackage(ResultPackage resultPackage) throws Exception {
		//判断是否存在空值
		if(resultPackage.isContainsNullFields()) {
			throw new IllegalArgumentException("接受的包存在空属性");
		}
		
		//根据aim获取out包
		IOPackage outPackage = UrSocekt.outPackageHolder.getByAim(resultPackage.getAimid());
		if(outPackage == null) {
			throw new IllegalArgumentException("找不到aimid对应的数据");
		}
		
		updateDb(outPackage);
		updateRedis(outPackage);
		returnBox(outPackage);
	}


	private void updateDb(IOPackage ioPackage) throws ParseException {
		//库存更新和记录入库日志
		List<UrMaterialInfo> urMaterialInfos = ioPackage.getList();
		if (urMaterialInfos == null || urMaterialInfos.size() == 0) {
			throw new IllegalArgumentException("list不能为空");
		}
		for (UrMaterialInfo urMaterialInfo : urMaterialInfos) {
			if(urMaterialInfo.containsNullFields()) {
				throw new IllegalArgumentException("list的内容不能存在空属性");
			}
			if(ioPackage.getCmdcode().equals("out")) {
				taskService.out(ioPackage.getTaskId(), urMaterialInfo.getMaterialNo(), urMaterialInfo.getMaterialTypeId(), urMaterialInfo.getQuantity(), ioPackage.getSupplier());
			}else {
				taskService.in(ioPackage.getTaskId(), urMaterialInfo.getMaterialNo(), urMaterialInfo.getRow(), urMaterialInfo.getCol(), urMaterialInfo.getMaterialTypeId(), urMaterialInfo.getQuantity(), ioPackage.getSupplier(), DateUtil.yyyyMMdd(urMaterialInfo.getProductionTime()));
			}
		}
	}


	private void returnBox(IOPackage ioPackage) throws Exception {
		//令叉车把盒子还回库中
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems()) {
			if (item.getTaskId().equals(ioPackage.getTaskId())) {
				MaterialBox materialBox = MaterialBox.dao.findById(item.getBoxId());
				int windowId = Task.dao.findById(item.getTaskId()).getWindow();
				Window window = Window.dao.findById(windowId);
				IOHandler.sendReturnBoxCmd(item, materialBox, window);
				break;
			}
		}
	}


	private void updateRedis(IOPackage ioPackage) {
		//redis里记录所有条目为已分配回库
		for (AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems()) {
			if (item.getTaskId().equals(ioPackage.getTaskId())) {
				TaskItemRedisDAO.updateIOTaskItemState(item, IOTaskItemState.START_BACK);
				TaskItemRedisDAO.updateTaskIsForceFinish(item, true);
			}
		}
	}
}
