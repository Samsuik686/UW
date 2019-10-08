package com.jimi.uw_server.ur.entity;

import java.util.List;

import com.jimi.uw_server.ur.entity.base.UrBasePackage;
import com.jimi.uw_server.ur.entity.base.UrMaterialInfo;


/**
 * 出入库包
 * <br>
 * <b>2019年5月9日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class IOPackage extends UrBasePackage {

	private String supplier;

	private Integer taskId;

	private List<UrMaterialInfo> list;


	public IOPackage() {
	}


	public IOPackage(boolean isIn) {
		cmdcode = isIn ? "in" : "out";
	}


	public boolean containsNullFields() {
		if (supplier == null || taskId == null || list == null) {
			return true;
		} else {
			return false;
		}
	}


	public String getSupplier() {
		return supplier;
	}


	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}


	public Integer getTaskId() {
		return taskId;
	}


	public void setTaskId(Integer taskId) {
		this.taskId = taskId;
	}


	public List<UrMaterialInfo> getList() {
		return list;
	}


	public void setList(List<UrMaterialInfo> list) {
		this.list = list;
	}

	// public static void main(String[] args) {
	// IOPackage package1 = new IOPackage();
	// System.out.println(JSON.toJSONString(package1));
	// }
}
