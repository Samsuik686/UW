package com.jimi.uw_server.util;

import java.util.HashMap;
import java.util.Map;

public class UWTokenBox extends TokenBox {

	public static Map<String, String> uwTokenMap = new HashMap<String, String>();
	
	public static void putUWToken(String tokenId, String userId, String key, Object value) {
		if (uwTokenMap.get(userId) != null) {
			remove(uwTokenMap.get(userId));
			put(tokenId, key, value);
			uwTokenMap.put(userId, tokenId);
		}else {
			put(tokenId, key, value);
			uwTokenMap.put(userId, tokenId);
		}
	}
}
