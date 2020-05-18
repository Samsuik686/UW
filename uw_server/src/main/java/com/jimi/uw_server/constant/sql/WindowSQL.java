/**  
*  
*/  
package com.jimi.uw_server.constant.sql;

/**  
 * <p>Title: WindowSQL</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年1月16日
 *
 */
public class WindowSQL {

	public static final String GET_ALL_WINDOWS_SQL = "SELECT * FROM window";
	
	public static final String GET_COUNT_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 2) ORDER BY id ASC";

	public static final String GET_SAMPLE_TASK_WINDOWS_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = 7) ORDER BY id ASC";

	public static final String GET_FREE_WINDOWS_SQL = "SELECT id FROM window WHERE `bind_task_id` IS NULL AND auto = ? ORDER BY id ASC";

	public static final String GET_WINDOWS_BY_TASK_TYPE_SQL = "SELECT id FROM window WHERE bind_task_id IN (SELECT id FROM task WHERE type = ?) ORDER BY id ASC";

	public static final String GET_WINDOW_BY_TASK = "SELECT * FROM window WHERE bind_task_id = ? ORDER BY id ASC";

}
