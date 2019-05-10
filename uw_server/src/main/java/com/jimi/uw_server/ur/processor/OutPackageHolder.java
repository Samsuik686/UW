package com.jimi.uw_server.ur.processor;

import java.util.HashMap;
import java.util.Map;

import com.jimi.uw_server.ur.entity.IOPackage;

/**
 * 出库报文持有者
 * <br>
 * <b>2019年5月10日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class OutPackageHolder {

	private Map<Integer, IOPackage> map = new HashMap<>();
	
	
	public IOPackage getByAim(Integer aimid) {
		return map.get(aimid);
	}
	
	
	public void put(IOPackage outPackage) {
		map.put(outPackage.getCmdid(), outPackage);
	}
	
}
