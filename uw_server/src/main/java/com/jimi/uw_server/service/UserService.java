package com.jimi.uw_server.service;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.aop.Aop;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jimi.uw_server.exception.OperationException;
import com.jimi.uw_server.model.User;
import com.jimi.uw_server.model.UserType;
import com.jimi.uw_server.model.vo.UserVO;
import com.jimi.uw_server.service.base.SelectService;
import com.jimi.uw_server.util.PagePaginate;

import cc.darhao.dautils.api.MD5Util;


/**
 * 用户业务层
 * @author HardyYao
 * @createTime 2018年6月8日
 */
public class UserService extends SelectService {

	private static SelectService selectService = Aop.get(SelectService.class);

	private static final String LOGIN_SQL = "SELECT * FROM user WHERE uid = ? AND password = ?";

	private static final String UNIQUE_USER_CHECK_SQL = "SELECT * FROM user WHERE uid = ?";

	private static final String GET_USER_TYPE_SQL = "SELECT id,name FROM user_type WHERE id > 0";


	// 登录
	public User login(String uid, String password) {
		User user = User.dao.findFirst(LOGIN_SQL, uid, MD5Util.MD5(password));
		if (user == null) {
			throw new OperationException("用户名或密码不正确！");
		}
		if (!user.getEnabled()) {
			throw new OperationException("用户" + user.getName() + "已被禁用！");
		}
		return user;
	}


	// 添加新用户
	public boolean add(String uid, String name, String password, Integer type) {
		if (uid == null || name == null || password == null || type == null) {
			return false;
		}
		if (User.dao.find(UNIQUE_USER_CHECK_SQL, uid).size() != 0) {
			throw new OperationException("用户" + uid + "已存在！");
		}
		if (uid.contains("!") || uid.contains("$")) {
			throw new OperationException("请勿往用户名中添加非法字符，如“!”或“$”！");
		}
		if (name.contains("!") || name.contains("$")) {
			throw new OperationException("请勿往用户描述中添加非法字符，如“!”或“$”！");
		}
		User user = new User();
		user.setUid(uid);
		user.setName(name);
		user.setPassword(MD5Util.MD5(password));
		user.setEnabled(true);
		user.setType(type);
		return user.save();
	}


	// 更新用户信息
	public boolean update(String uid, String name, String password, Boolean enabled, Integer type) {
		User user = User.dao.findById(uid);
		user.setUid(uid);
		if (!(name == null)) {
			if (name.contains("!") || name.contains("$")) {
				throw new OperationException("请勿往用户描述中添加非法字符，如“!”或“$”！");
			}
			user.setName(name);
		}
		if (!(password == null)) {
			user.setPassword(MD5Util.MD5(password));
		}
		if (!(enabled == null)) {
			user.setEnabled(enabled);
		}
		if (!(type == null)) {
			user.setType(type);
		}
		return user.update();
	}


	// 查询用户信息
	public Object select(Integer pageNo, Integer pageSize, String ascBy, String descBy, String filter) {
		Page<Record> result = selectService.select(new String[] {"user", "user_type"}, new String[] {"user.type = user_type.id"}, pageNo, pageSize, ascBy, descBy, filter);
		List<UserVO> userVOs = new ArrayList<UserVO>();
		for (Record res : result.getList()) {
			UserVO u = new UserVO(res.get("User_Uid"), res.get("User_Password"), res.get("User_Name"), res.get("User_Type"), res.get("User_Enabled"), res.getStr("UserType_Name"));
			userVOs.add(u);
		}

		PagePaginate pagePaginate = new PagePaginate();
		pagePaginate.setPageSize(pageSize);
		pagePaginate.setPageNumber(pageNo);
		pagePaginate.setTotalRow(result.getTotalRow());
		pagePaginate.setList(userVOs);

		return pagePaginate;
	}


	// 获取用户类型
	public List<UserType> getTypes() {
		List<UserType> userTypes = new ArrayList<UserType>();
		userTypes = UserType.dao.find(GET_USER_TYPE_SQL);
		return userTypes;
	}

}
