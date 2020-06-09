package com.jimi.uw_server.constant;

public enum WarehouseType {

	REGULAR(0, "普通仓"), PRECIOUS(1, "贵重仓"), PCB(2, "PCB仓");


	private Integer id;

	private String describe;


	private WarehouseType(int id, String describe) {
		this.id = id;
		this.describe = describe;
	}


	public Integer getId() {
		return id;
	}


	public String getDescribe() {
		return describe;
	}


	public static String getDescribeById(int id) {
		for (WarehouseType type : values()) {
			if (type.getId().equals(id)) {
				return type.getDescribe();
			}
		}
		return "";
	}
}
