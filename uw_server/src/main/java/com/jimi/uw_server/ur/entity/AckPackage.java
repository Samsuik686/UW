package com.jimi.uw_server.ur.entity;

import com.jimi.uw_server.ur.entity.base.UrBasePackage;


/**
 * 
 * <p>
 * Title: AckPackage
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2019
 * </p>
 * <p>
 * Company: 惠州市几米物联技术有限公司
 * </p>
 * 
 * @author trjie
 * @date 2019年12月25日
 *
 */
public class AckPackage extends UrBasePackage {

	public AckPackage(int cmdId) {
		this.cmdCode = "ack";
		this.cmdId = cmdId;
	}

}
