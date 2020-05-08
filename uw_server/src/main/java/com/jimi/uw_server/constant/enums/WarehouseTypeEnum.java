package com.jimi.uw_server.constant.enums;

public enum WarehouseTypeEnum {

	REGULAR(0, "普通仓"), PRECIOUS(1,"贵重仓");
	
	private Integer id;
	
	private String describe;
	
	private WarehouseTypeEnum(int id, String describe){
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
		for (WarehouseTypeEnum type : values()) {
			if (type.getId().equals(id)) {
				return type.getDescribe();
			}
		}
		return "";
	}
}
