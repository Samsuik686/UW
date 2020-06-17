/**  
*  
*/  
package com.jimi.uw_server.agv.gzrobot.entity.base;

import com.jimi.uw_server.agv.gzrobot.entity.RequestResult;

/**  
 * <p>Title: BaseResponse</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年6月17日
 *
 */
public class BaseResponse {

	private RequestResult requestResult;

	public RequestResult getRequestResult() {
		return requestResult;
	}

	public void setRequestResult(RequestResult requestResult) {
		this.requestResult = requestResult;
	}

	@Override
	public String toString() {
		return "BaseResponse [requestResult=" + requestResult + "]";
	}
	
}
