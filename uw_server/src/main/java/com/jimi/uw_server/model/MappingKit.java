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
		arp.addMapping("box_type", "id", BoxType.class);
		arp.addMapping("company", "id", Company.class);
		arp.addMapping("destination", "id", Destination.class);
		arp.addMapping("efficiency", "id", Efficiency.class);
		arp.addMapping("error_log", "id", ErrorLog.class);
		arp.addMapping("external_inventory_log", "id", ExternalInventoryLog.class);
		arp.addMapping("external_wh_log", "id", ExternalWhLog.class);
		arp.addMapping("faq", "id", Faq.class);
		arp.addMapping("former_supplier", "id", FormerSupplier.class);
		arp.addMapping("goods_location", "id", GoodsLocation.class);
		arp.addMapping("inventory_log", "id", InventoryLog.class);
		arp.addMapping("inventory_task_base_info", "id", InventoryTaskBaseInfo.class);
		arp.addMapping("material", "id", Material.class);
		arp.addMapping("material_box", "id", MaterialBox.class);
		arp.addMapping("material_return_record", "id", MaterialReturnRecord.class);
		arp.addMapping("material_type", "id", MaterialType.class);
		arp.addMapping("packing_list_item", "id", PackingListItem.class);
		arp.addMapping("pda_upload_log", "id", PdaUploadLog.class);
		arp.addMapping("sample_out_record", "id", SampleOutRecord.class);
		arp.addMapping("sample_task_item", "id", SampleTaskItem.class);
		arp.addMapping("sample_task_material_record", "id", SampleTaskMaterialRecord.class);
		arp.addMapping("socket_log", "id", SocketLog.class);
		arp.addMapping("supplier", "id", Supplier.class);
		arp.addMapping("task", "id", Task.class);
		arp.addMapping("task_log", "id", TaskLog.class);
		arp.addMapping("user", "uid", User.class);
		arp.addMapping("user_type", "id", UserType.class);
		arp.addMapping("window", "id", Window.class);
	}
}

