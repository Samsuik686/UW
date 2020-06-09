/**  
*  
*/
package com.jimi.uw_server.service;

import java.util.List;

import com.jimi.uw_server.constant.TaskType;
import com.jimi.uw_server.constant.sql.WindowSQL;
import com.jimi.uw_server.model.Window;

/**
 * <p>
 * Title: WindowService
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2019
 * </p>
 * <p>
 * Company: 惠州市几米物联技术有限公司
 * </p>
 * 
 * @author trjie
 * @date 2020年5月18日
 *
 */
public class WindowService {

	// 查询指定类型的仓口
	public List<Window> getWindows(int type, Boolean isAuto) {
		List<Window> windowIds = null;
		switch (type) {
		case 0:
			windowIds = Window.dao.find(WindowSQL.GET_FREE_WINDOWS_SQL, isAuto);
			break;
		case 1:
			windowIds = Window.dao.find(WindowSQL.GET_WINDOWS_BY_TASK_TYPE_SQL, TaskType.IN);
			break;
		case 2:
			windowIds = Window.dao.find(WindowSQL.GET_WINDOWS_BY_TASK_TYPE_SQL, TaskType.OUT);
			break;
		case 3:
			windowIds = Window.dao.find(WindowSQL.GET_WINDOWS_BY_TASK_TYPE_SQL, TaskType.SEND_BACK);
			break;
		case 4:
			windowIds = Window.dao.find(WindowSQL.GET_WINDOWS_BY_TASK_TYPE_SQL, TaskType.COUNT);
			break;
		case 5:
			windowIds = Window.dao.find(WindowSQL.GET_WINDOWS_BY_TASK_TYPE_SQL, TaskType.SAMPLE);
			break;
		default:
			break;
		}
		return windowIds;
	}


}
