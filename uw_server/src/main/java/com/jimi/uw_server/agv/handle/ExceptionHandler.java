package com.jimi.uw_server.agv.handle;

import com.jfinal.json.Json;
import com.jimi.uw_server.agv.dao.RobotInfoRedisDAO;
import com.jimi.uw_server.agv.dao.TaskItemRedisDAO;
import com.jimi.uw_server.agv.entity.bo.AGVBuildTaskItem;
import com.jimi.uw_server.agv.entity.bo.AGVIOTaskItem;
import com.jimi.uw_server.agv.entity.cmd.AGVLoadExceptionCmd;

/**
 * 异常处理器
 * <br>
 * <b>2018年7月13日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class ExceptionHandler {
	
	public static void handleLoadException(String message) {

		AGVLoadExceptionCmd loadExceptionCmd = Json.getJson().parse(message, AGVLoadExceptionCmd.class);
		String groupid = loadExceptionCmd.getMissiongroupid();
		// missiongroupid 包含“:”表示为出入库任务
		if (groupid.contains(":")) {
			for(AGVIOTaskItem item : TaskItemRedisDAO.getIOTaskItems()) {
				if(item.getGroupId().equals(groupid)) {
					//把指定叉车的取空异常置为真
					RobotInfoRedisDAO.setloadException(item.getRobotId());

					break;
				}
			}
		} else {	// missiongroupid 不包含“:”表示为建仓任务
			for(AGVBuildTaskItem item1 : TaskItemRedisDAO.getBuildTaskItems()) {
				if(item1.getGroupId().equals(groupid)) {
					//把指定叉车的取空异常置为真
					RobotInfoRedisDAO.setloadException(item1.getRobotId());

					break;
				}
			}
		}

	}
	
}
