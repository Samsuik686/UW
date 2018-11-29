package com.jimi.agv_mock.dao;

import java.io.File;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.activerecord.dialect.MysqlDialect;
import com.jfinal.plugin.druid.DruidPlugin;

/**
 * 任务的数据访问层
 * <br>
 * <b>2018年7月31日</b>
 * @author 沫熊工作室 <a href="http://www.darhao.cc">www.darhao.cc</a>
 */
public class TaskDAO {

	private static final String URL = "jdbc:mysql://localhost:3306/uw?characterEncoding=utf8";
	private static final String D_USER = "root";
	private static final String D_PASSWORD = "newttl!@#$1234";
	private static final String T_USER = "standard";
	private static final String T_PASSWORD = "Jimisql#1234";
	
	
	public static void init() {
		DruidPlugin dp = null;
		if (isTestEnvironment()) {
			dp = new DruidPlugin(URL, T_USER, T_PASSWORD);
		} else {
			dp = new DruidPlugin(URL, D_USER, D_PASSWORD);
		}
		dp.start();
		ActiveRecordPlugin arp = new ActiveRecordPlugin(dp);
	    arp.setDialect(new MysqlDialect());
	    arp.addMapping("task", Task.class);
	    arp.start();
	}
	
	
	public static int getWindowId(int taskId) {
		return Task.dao.findById(taskId).getWindow();
	}


	public static boolean isTestEnvironment() {
		File[] roots = File.listRoots();
        for (int i=0; i < roots.length; i++) {
            if(new File(roots[i].toString() + "TEST_ENVIRONMENT_FLAG").exists()) {
            	return true;
            }
        }
        return false;
	}

}
