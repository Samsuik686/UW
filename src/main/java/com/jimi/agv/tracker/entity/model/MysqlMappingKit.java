package com.jimi.agv.tracker.entity.model;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * Generated by JFinal, do not modify this file.
 * <pre>
 * Example:
 * public void configPlugin(Plugins me) {
 *     ActiveRecordPlugin arp = new ActiveRecordPlugin(...);
 *     MysqlMappingKit.mapping(arp);
 *     me.add(arp);
 * }
 * </pre>
 */
public class MysqlMappingKit {
	
	public static void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("cushion_body", "id", CushionBody.class);
		arp.addMapping("cushion_head", "id", CushionHead.class);
		arp.addMapping("tradition_body", "id", TraditionBody.class);
		arp.addMapping("tradition_head", "id", TraditionHead.class);
	}
}

