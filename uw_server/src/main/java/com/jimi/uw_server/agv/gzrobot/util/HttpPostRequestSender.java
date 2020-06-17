/**  
*  
*/  
package com.jimi.uw_server.agv.gzrobot.util;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.jfinal.json.Jackson;
import com.jimi.uw_server.agv.gzrobot.entity.base.BaseResponse;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**  
 * <p>Title: HttpPostRequestSender</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年6月9日
 *
 */
public class HttpPostRequestSender {
	
	private static OkHttpClient client = new OkHttpClient();
	
	public static BaseResponse sendBaseRequestByPost(String url, Object args) {
		
		OkHttpClient thisClient =  client.newBuilder().readTimeout(120, TimeUnit.SECONDS).build();
		RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), Jackson.getJson().toJson(args));
		Request request = new Request.Builder()
				.post(body)
				.url(url)
				.build();
		
		Response response;
		try {
			response = thisClient.newCall(request).execute();
			if (response.isSuccessful()) {
				BaseResponse result = JsonParserUtil.me.parseToBaseRequset(response.body().string(), BaseResponse.class);
				return result;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {             
			e.printStackTrace();
		}
		return null;
	}
}
