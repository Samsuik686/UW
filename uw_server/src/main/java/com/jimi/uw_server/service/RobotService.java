package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jfinal.json.Json;
import com.jfinal.plugin.redis.Cache;
import com.jfinal.plugin.redis.Redis;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.handle.LSSLHandler;
import com.jimi.uw_server.agv.handle.SwitchHandler;
import com.jimi.uw_server.model.MaterialType;
import com.jimi.uw_server.model.PackingListItem;
import com.jimi.uw_server.model.Window;
import com.jimi.uw_server.model.bo.RobotBO;
import com.jimi.uw_server.model.vo.RobotVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.util.RobotComparator;

/**
 * 叉车业务层
 * 
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class RobotService extends SelectService {

	private static TaskService taskService = Enhancer.enhance(TaskService.class);

	private static final String GET_MATERIAL_TYPE_ID_SQL = "SELECT * FROM packing_list_item WHERE task_id = ? "
			+ "AND material_type_id = (SELECT id FROM material_type WHERE enabled = 1 AND no = ?)";

	private static Cache cache = Redis.use();


	public List<RobotVO> select() {
		List<RobotBO> robotBOs = RobotInfoRedisDAO.check();
		List<RobotVO> robotVOs = new ArrayList<>();
		for (RobotBO robotBO : robotBOs) {
			RobotVO robotVO = new RobotVO(robotBO);
			robotVOs.add(robotVO);
		}
		// 根据叉车ID对叉车进行升序排序
		Collections.sort(robotVOs, new RobotComparator());
		return robotVOs;
	}

	
	public void robotSwitch(String id, Integer enabled) throws Exception {
		List<Integer> idList = new ArrayList<>();
		String[] ids = id.split(",");
		for (String string : ids) {
			idList.add(Integer.parseInt(string));
		}
		if (enabled == 2) {
			SwitchHandler.sendEnable(idList);
		} else if (enabled == 1) {
			SwitchHandler.sendDisable(idList);
		}
	}

	
	public void pause(Boolean pause) throws Exception {
        if (pause) {
            SwitchHandler.sendAllStart();
            RobotInfoRedisDAO.clearLoadException();
        } else {
            SwitchHandler.sendAllPause();
        }
	}


	/**
	 * 叉车回库SL
	 */
	public String back(Integer id) throws Exception {
		String resultString = "已成功发送SL指令！";
		PackingListItem packingListItem = PackingListItem.dao.findById(id);
		if (packingListItem.getFinishTime() == null) {
			for (AGVIOTaskItem item : TaskItemRedisDAO.getTaskItems()) {
				if(id.equals(item.getId())) {
					//查询对应物料类型
					MaterialType materialType = MaterialType.dao.findById(item.getMaterialTypeId());
					LSSLHandler.sendSL(item, materialType);
					taskService.finishItem(item.getId());
				}
			}
			return resultString;
		} else {
			resultString = "该任务条目已发送过SL指令，请勿重复发送SL指令！";
			return resultString;
		}
		
	}


	/**
	 * 入库前扫料盘，发LS指令给叉车
	 */
	public String call(Integer id, String no) throws Exception {
		Window window = Window.dao.findById(id);
		Integer taskId = window.getBindTaskId();
		// 根据套料单、物料类型表生成任务条目
		List<AGVIOTaskItem> taskItems = new ArrayList<AGVIOTaskItem>();
		PackingListItem item = PackingListItem.dao.findFirst(GET_MATERIAL_TYPE_ID_SQL, taskId, no);
		String resultString = "调用成功！";

		if (item == null) {
			resultString = "该物料暂时不需要入库！";
			return resultString;
		} else {

			for (int i = 0; i < cache.llen("til"); i++) {
				byte[] redisItem = cache.lindex("til", i);
				AGVIOTaskItem agvioTaskItem = Json.getJson().parse(new String(redisItem), AGVIOTaskItem.class);
				if (item.getId().equals(agvioTaskItem.getId())) {
					resultString = "该物料已经扫描过，请勿重复扫描！";
					return resultString;
				}
			}

			if (item.getFinishTime() != null) {
				resultString = "该任务条目已完成，请勿重复扫描！";
				return resultString;
			}

			AGVIOTaskItem a = new AGVIOTaskItem(item);
			taskItems.add(a);
			TaskItemRedisDAO.addTaskItem(taskItems);
			return resultString;
		}

	}

}
