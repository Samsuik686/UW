/**  
*  
*/  
package com.jimi.uw_server.agv.gzrobot.entity.base;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.jfinal.json.Jackson;
import com.jimi.uw_server.agv.gzrobot.entity.AgvCallRequest;

/**  
 * <p>Title: TestApp</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年6月17日
 *
 */
public class TestApp {

	
	public static void main(String[] args) throws IOException {
		
		String a = "{\"request_str\":{\"agv_id\":1,\"custom_param1\":\"\",\"custom_param2\":\"\",\"dest_name\":\"s10\",\"has_next_task\":\"1\",\"order_id\":\"2\",\"pre_task_id\":\"\",\"priority\":1,\"task_id\":\"2\",\"task_type\":1},\"request_type\":1}";
		
		ObjectMapper objectMapper = Jackson.getJson().getObjectMapper();
		//objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		JsonParser jsonParser = objectMapper.createParser(a);
		TypeReference<BaseRequest<AgvCallRequest>> A = new TypeReference<BaseRequest<AgvCallRequest>>() {
			//empty
		};
		BaseRequest<AgvCallRequest> baseRequest = jsonParser.readValueAs(A);
			
		System.out.println(baseRequest.getRequestStr().toString());
	}
}
