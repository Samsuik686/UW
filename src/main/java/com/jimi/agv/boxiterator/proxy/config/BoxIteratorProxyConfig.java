package com.jimi.agv.boxiterator.proxy.config;

import com.jfinal.config.Constants;
import com.jfinal.config.Handlers;
import com.jfinal.config.Interceptors;
import com.jfinal.config.JFinalConfig;
import com.jfinal.config.Plugins;
import com.jfinal.config.Routes;
import com.jfinal.template.Engine;
import com.jimi.agv.boxiterator.proxy.constant.Constant;
import com.jimi.agv.boxiterator.proxy.constant.PackageType;
import com.jimi.agv.boxiterator.proxy.socket.AGVMainSocket;
import com.jimi.agv.boxiterator.proxy.socket.handle.SwitchHandler;
import com.jimi.agv.boxiterator.proxy.socket.proxy.RequestProxy;
import com.jimi.agv.boxiterator.proxy.util.PropUtil;

import cc.darhao.pasta.Pasta;

/**
 * 全局配置
 */
public class BoxIteratorProxyConfig extends JFinalConfig {

	@Override
	public void configConstant(Constants me) {
	}

	@Override
	public void configEngine(Engine me) {
	}

	@Override
	public void configHandler(Handlers me) {
		me.add(new WebSocketHandler("^/connect"));
	}

	@Override
	public void configInterceptor(Interceptors me) {
	}

	@Override
	public void configPlugin(Plugins me) {
	}

	@Override
	public void configRoute(Routes me) {
	}

	@Override
	public void afterJFinalStart() {
		new Thread(()->{ //当mockserver与本代理在同一个tomcat启动时，需要异步连接，否则可能会出现连接失败，因为调用连接时mockserver仍未启动
			try {
				AGVMainSocket.init(PropUtil.getString(Constant.CONFIG_NAME, Constant.MAIN_WS_STRING));
				Pasta.bindRoute(PackageType.GET, RequestProxy.class);
				Pasta.bindRoute(PackageType.RETURN, RequestProxy.class);
				Pasta.bindRoute(PackageType.LOGIN, RequestProxy.class);
				SwitchHandler.sendAllStart();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}).start();
	}
	
	@Override
	public void beforeJFinalStop() {
	}
	
}
