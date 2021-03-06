package com.jimi.uw_server.model;

import com.jfinal.plugin.activerecord.ActiveRecordPlugin;

/**
 * Generated by JFinal, do not modify this file.
 * <pre>
 * Example:
 * public void configPlugin(Plugins me) {
 *     ActiveRecordPlugin arp = new ActiveRecordPlugin(...);
 *     MappingKit.mapping(arp);
 *     me.add(arp);
 * }
 * </pre>
 */
public class MappingKit {
	
	public static void mapping(ActiveRecordPlugin arp) {
		arp.addMapping("action_log", "id", ActionLog.class);
		arp.addMapping("error_log", "id", ErrorLog.class);
		arp.addMapping("material", "id", Material.class);
		arp.addMapping("material_type", "id", MaterialType.class);
		arp.addMapping("packing_list_item", "id", PackingListItem.class);
		arp.addMapping("position_log", "id", PositionLog.class);
		arp.addMapping("socket_log", "id", SocketLog.class);
		arp.addMapping("task", "id", Task.class);
		arp.addMapping("task_log", "id", TaskLog.class);
		arp.addMapping("user", "uid", User.class);
		arp.addMapping("user_type", "id", UserType.class);
		arp.addMapping("window", "id", Window.class);
	}
}

