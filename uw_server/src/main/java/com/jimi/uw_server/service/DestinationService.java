package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.constant.sql.DestinationSQL;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.Destination;
import com.jimi.uw_server.model.Task;
import com.jimi.uw_server.model.vo.DestinationVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.service.entity.PagePaginate;

/**
 * 发料目的地业务层
 * 
 * @author HardyYao
 * @createTime 2019年3月11日 上午11:36:05
 */

public class DestinationService {

	private static final String GET_TASK_BY_DESTINATION_ID_SQL = "SELECT * FROM task WHERE destination = ?";

	private static SelectService selectService = Aop.get(SelectService.class);

	// 添加发料目的地
	public void add(String name, Integer companyId) {
		if (Destination.dao.findFirst(DestinationSQL.GET_DESTINATION_BY_NAME_SQL, name, companyId) != null) {
			throw new OperationException("该发料目的地名称重复，请勿更改！");
		} else {
			Destination destination = new Destination();
			destination.setName(name);
			destination.setCompanyId(companyId);
			destination.setEnabled(true);
			destination.save();
		}
	}

	
	//修改目的仓库名称
	public void update(Integer id, String name) {
		Destination destination = Destination.dao.findById(id);
		if (!destination.getName().equals(name)) {
			if (Destination.dao.find(DestinationSQL.GET_DESTINATION_BY_NAME_SQL, name, destination.getCompanyId()) != null) {
				throw new OperationException("该目的地名称冲突！");
			}
		}
		destination.setName(name);
		destination.update();
	}

	
	// 删除发料目的地
	public void delete(Integer id) {
		Destination destination = Destination.dao.findById(id);
		Task t = Task.dao.findFirst(GET_TASK_BY_DESTINATION_ID_SQL, id);
		if (t != null) {
			throw new OperationException("该发料目的地已绑定了某个任务，禁止删除！");
		}
		destination.setEnabled(false);
		destination.update();
	}

	
	// 查询发料目的地
	public PagePaginate select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		// 只查询enabled字段为true的记录
		if (filter != null) {
			filter = filter.concat("#&#destination.enabled=1#&#company.enabled=1");
		} else {
			filter = "destination.enabled=1#&#company.enabled=1";
		}
		Page<Record> result = selectService.select(new String[] { "destination", "company" },
				new String[] { "destination.company_id=company.id" }, pageNo, pageSize, ascBy, descBy, filter);
		List<Destination> destinations = Destination.dao.find(DestinationSQL.GET_SHARE_DESTINATION_SQL);
		List<DestinationVO> destinationVOs = new ArrayList<DestinationVO>();
		for (Destination destination : destinations) {
			DestinationVO destinationVO = new DestinationVO(destination.getId(), destination.getName(), null, destination.getEnabled());
			destinationVOs.add(destinationVO);
		}
		for (Record res : result.getList()) {
			DestinationVO s = new DestinationVO(res.get("Destination_Id"), res.get("Destination_Name"),
					res.getStr("Company_NickName"), res.get("Destination_Enabled"));
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
