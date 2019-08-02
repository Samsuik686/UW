/*
 * package com.jimi.uw_server.helper;
 * 
 *//**
	* 投料辅助器
	* <br>
	* <b>2019年7月30日</b>
	* @author <a href="https://github.com/darhao">鲁智深</a>
	*/
/*
 * public abstract class InputHelper {
 * 
 * private InputHelper() { }
 * 
 *//**
	* 投料监听器
	* <br>
	* <b>2019年7月30日</b>
	* @author <a href="https://github.com/darhao">鲁智深</a>
	*/
/*
 * public static interface OnInputListener {
 * 
 *//**
	* 物料被投入回调方法
	* @param windowId 仓库ID
	* @param PositionNo 料盒内槽位编号
	*/
/*
 * public void onInput(int windowId, int PositionNo); }
 * 
 * 
 *//**
	* 获取投料辅助器实例，需覆盖实现，注意：实例是单例的
	* @return 投料辅助器实例
	*/
/*
 * public static InputHelper getInstance() { return null; }
 * 
 * 
 *//**
	* 开始监听设备连接<br>
	* 可能抛出非受检异常的情况：监听时出错，如端口被占用
	* @param port 监听端口
	*/
/*
 * public abstract void start(int port);
 * 
 * 
 *//**
	* 尝试切换灯的状态
	* @param windowId 仓库ID - 当本实例开启监听、设备登录后，该参数才有效
	* @param PositionNo 料盒内槽位编号
	* @param isTurnOn 是否亮起灯
	* @return 结果串：成功返回 “succeed” ；任何错误的参数值都会导致返回的结果串<b> 不 </b>为 “succeed”
	*/
/*
 * public abstract String switchLight(int windowId, int positionNo, boolean
 * isTurnOn);
 * 
 * 
 *//**
	* 尝试切换警报的状态
	* @param windowId 仓库ID - 当本实例开启监听、设备登录后，该参数才有效
	* @param isTurnOn 是否拉起警报
	* @return 结果串：成功返回 “succeed” ；任何错误的参数值都会导致返回的结果串<b> 不 </b>为 “succeed”
	*/
/*
 * public abstract String switchAlarm(int windowId, boolean isTurnOn);
 * 
 * 
 *//**
	* 设置投料监听器
	* @param listener 监听器
	*//*
		 * public abstract void setOnInputListener(OnInputListener listener);
		 * 
		 * }
		 */