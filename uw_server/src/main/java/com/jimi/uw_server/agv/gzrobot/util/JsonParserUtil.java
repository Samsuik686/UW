/**  
*  
*/  
package com.jimi.uw_server.agv.gzrobot.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfinal.json.Jackson;
import com.jimi.uw_server.agv.gzrobot.entity.AgvCallRequest;
import com.jimi.uw_server.agv.gzrobot.entity.base.BaseRequest;

/**  
 * <p>Title: JsonParserUtil</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年6月17日
 *
 */
public class JsonParserUtil {

	public static JsonParserUtil me = new JsonParserUtil();
	
	@SuppressWarnings("unchecked")
	public <T> T parseToBaseRequset(String jsonString, Class<T> clazz) throws IOException {
		ObjectMapper objectMapper = Jackson.getJson().getObjectMapper();
		JsonParser jsonParser = objectMapper.createParser(jsonString);
		TypeReference<BaseRequest<T>> A = new TypeReference<BaseRequest<T>>() {
			//empty
		};
		BaseRequest<AgvCallRequest> result = jsonParser.readValueAs(A);
		return (T) result.getRequestStr();
		
	}
}
