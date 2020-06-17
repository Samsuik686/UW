/**  
*  
*/  
package com.jimi.uw_server.agv.gzrobot.handler;

import com.jimi.uw_server.agv.gzrobot.constant.AgvRequestType;
import com.jimi.uw_server.agv.gzrobot.entity.AgvCallRequest;
import com.jimi.uw_server.agv.gzrobot.entity.base.BaseRequest;
import com.jimi.uw_server.agv.gzrobot.entity.base.BaseResponse;
import com.jimi.uw_server.agv.gzrobot.util.HttpPostRequestSender;

/**  
 * <p>Title: AgvCallRequestHnadler</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年6月17日
 *
 */
public class AgvCallRequestHnadler {

	
	private void sendAgvCallRequest(AgvCallRequest agvCallRequest) {
		BaseRequest<AgvCallRequest> baseRequest = new BaseRequest<>();
		baseRequest.setRequestStr(agvCallRequest);
		baseRequest.setRequestType(AgvRequestType.AGV_CALL_REQUEST.getId());
		BaseResponse result = HttpPostRequestSender.sendBaseRequestByPost(AgvRequestType.AGV_CALL_REQUEST.getUrl(), baseRequest);
		if (!result.getRequestResult().getResultCode().equals(0)) {
			throw new RuntimeException(result.getRequestResult().getResultDesc());
		}
	}
}
