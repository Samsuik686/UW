package com.jimi.agv.tracker.task.reporter;

/**
 * 数据报告器接口
 * <br>
 * <b>2019年6月12日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public interface Reporter {

	/**
	 * 返回字符串报告
	 */
	String getReport();
	
	/**
	 * 将报告存到数据库 
	 */
	void saveToDb();
	
}
