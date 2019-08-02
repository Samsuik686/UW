package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Enhancer;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.model.Destination;
import com.jimi.uw_server.model.Supplier;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.vo.DestinationVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;


/**
 * 发料目的地业务层
 * @author HardyYao
 * @createTime 2019年3月11日  上午11:36:05
 */

public class DestinationService {

	private static SelectService selectService = Enhancer.enhance(SelectService.class);

	private static final String GET_ENABLED_DESTINATION_BY_NAME_SQL = "SELECT * FROM destination WHERE name = ? AND enabled = 1";

	private static final String GET_TASK_BY_DESTINATION_ID_SQL = "SELECT * FROM task WHERE destination = ?";


	// 添加发料目的地
	public String add(String name) {
		String resultString = "添加成功！";
		if (Supplier.dao.find(GET_ENABLED_DESTINATION_BY_NAME_SQL, name).size() != 0) {
			resultString = "该发料目的地已存在，请勿重复添加！";
			return resultString;
		} else {
			Destination destination = new Destination();
			destination.setName(name);
			destination.setEnabled(true);
			destination.save();
			return resultString;
		}
	}


	// 删除发料目的地
	public String delete(Integer id, Boolean enabled) {
		String resultString = "删除成功！";
		Destination destination = Destination.dao.findById(id);
		if (!enabled) {
			Task t = Task.dao.findFirst(GET_TASK_BY_DESTINATION_ID_SQL, id);
			if (t != null) {
				resultString = "该发料目的地已绑定了某个任务，禁止删除！";
				return resultString;
			}
		}
		destination.setEnabled(enabled);
		destination.update();
		return resultString;
	}

	// public String update(Integer id, String name) {
	// String resultString = "更新成功！";
	// Destination destination = Destination.dao.findById(id);
	// if(!destination.getName().equals(name)) {
	// if (Destination.dao.find(GET_ENABLED_DESTINATION_BY_NAME_SQL, name).size() !=
	// 0) {
	// resultString = "该供应商已存在，请勿重复添加！";
	// return resultString;
	// }
	// }
	// destination.setName(name);
	// destination.update();
	// return resultString;
	// }


	// 查询发料目的地
	public Object get(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null) {
			filter = filter.concat("#&#enabled=1");
		} else {
			filter = "enabled=1";
		}
		Page<Record> result = selectService.select("destination", pageNo, pageSize, ascBy, descBy, filter);
		List<DestinationVO> destinationVOs = new ArrayList<DestinationVO>();
		for (Record res : result.getList()) {
			DestinationVO s = new DestinationVO(res.get("id"), res.get("name"), res.get("enabled"));
			destinationVOs.add(s);
		}
		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(destinationVOs);
		return pagePaginate;
	}

}
