/**  
*  
*/  
package com.jimi.uw_server.agv.gzrobot.constant;


/**  
 * <p>Title: AGVRequestType</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年6月17日
 *
 */
public enum AgvRequestType {

	AGV_CALL_REQUEST(1, "下发订单"), AGV_CANCEL_REQUEST(2, "取消订单"), AGV_CALL_REQUEST_STATUS(101, "任务状态反馈"), ABNORMITY_FEEDBACK(102, "任务异常反馈");

	private Integer id;
	
	private String url;
	
	private String description;
	
	private AgvRequestType(Integer id, String description) {
		this.id = id;
		this.description = description;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
