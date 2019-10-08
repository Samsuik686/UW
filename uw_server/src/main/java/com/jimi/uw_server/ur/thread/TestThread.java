package com.jimi.uw_server.ur.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.jimi.uw_server.ur.entity.AckResponseManager;
import com.jimi.uw_server.ur.entity.CmdidManager;
import com.jimi.uw_server.ur.entity.IOPackage;
import com.jimi.uw_server.ur.entity.ReachInPackage;
import com.jimi.uw_server.ur.entity.ReachOutPackage;
import com.jimi.uw_server.ur.entity.SessionBox;
import com.jimi.uw_server.ur.entity.base.UrMaterialInfo;


public class TestThread {

	public static void sendReachIn() {
		ReachInPackage pack = new ReachInPackage();
		pack.setCmdid(CmdidManager.getCmdid());
		CountDownLatch l = new CountDownLatch(1);
		AckResponseManager.putAckResponse(pack.getCmdid(), l);
		SessionBox.getChannelHandlerContext("ur").writeAndFlush(pack);
		try {
			l.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void sendReachOut() {
		ReachOutPackage pack = new ReachOutPackage();
		pack.setCmdid(CmdidManager.getCmdid());
		pack.setTaskId(123);
		CountDownLatch l = new CountDownLatch(1);
		AckResponseManager.putAckResponse(pack.getCmdid(), l);
		SessionBox.getChannelHandlerContext("ur").writeAndFlush(pack);
		try {
			l.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public static void sendOutPack() {
		IOPackage pack = new IOPackage();
		pack.setSupplier("aaaa");
		pack.setCmdcode("out");
		pack.setTaskId(123);
		pack.setCmdid(CmdidManager.getCmdid());
		CountDownLatch l = new CountDownLatch(1);
		AckResponseManager.putAckResponse(pack.getCmdid(), l);
		List<UrMaterialInfo> list = new ArrayList<UrMaterialInfo>();
		UrMaterialInfo urMaterialInfo = new UrMaterialInfo();
		urMaterialInfo.setCol(1);
		urMaterialInfo.setMaterialNo("545646");
		urMaterialInfo.setMaterialTypeId(12312);
		urMaterialInfo.setQuantity(114);
		urMaterialInfo.setProductionTime("2019-4-4 00:00:12");
		urMaterialInfo.setRow(2);
		UrMaterialInfo urMaterialInfo1 = new UrMaterialInfo();
		urMaterialInfo1.setCol(1);
		urMaterialInfo1.setMaterialNo("544345646");
		urMaterialInfo1.setMaterialTypeId(12312);
		urMaterialInfo1.setQuantity(114);
		urMaterialInfo1.setProductionTime("2019-4-4 00:00:12");
		urMaterialInfo1.setRow(2);
		UrMaterialInfo urMaterialInfo2 = new UrMaterialInfo();
		urMaterialInfo2.setCol(1);
		urMaterialInfo2.setMaterialNo("4324");
		urMaterialInfo2.setMaterialTypeId(12312);
		urMaterialInfo2.setQuantity(114);
		urMaterialInfo2.setProductionTime("2019-4-4 00:00:12");
		urMaterialInfo2.setRow(2);
		list.add(urMaterialInfo);
		list.add(urMaterialInfo1);
		list.add(urMaterialInfo2);
		pack.setList(list);
		SessionBox.getChannelHandlerContext("ur").writeAndFlush(pack);
		try {
			l.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
