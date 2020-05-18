/**  
*  
*/  
package com.jimi.uw_server.lock;

/**  
 * <p>Title: PreciousTaskLock</p>  
 * <p>Description: </p>  
 * <p>Copyright: Copyright (c) 2019</p>  
 * <p>Company: 惠州市几米物联技术有限公司</p>  
 * @author trjie  
 * @date 2020年5月18日
 *
 */
public class PreciousTaskLock {

	public static Object CREATE_IO_LOCK = new Object();

	public static Object PASS_IO_LOCK = new Object();
	
	public static Object START_IO_LOCK = new Object();
	
	public static Object CANCEL_IO_LOCK = new Object();

	public static Object IN_SCAN_IO_LOCK = new Object();
	
	public static Object OUT_SCAN_IO_LOCK = new Object();
	

	public static Object START_INVENTORY_LOCK = new Object();
	
	public static Object CREATE_SAMPLE_LOCK = new Object();
	
	public static Object START_SAMPLE_LOCK = new Object();
	
	public static Object CANCEL_SAMPLE_LOCK = new Object();
	
	public static Object FINISH_SAMPLE_LOCK = new Object();
	
	public static Object SAMPLE_SCAN_LOCK = new Object();
}
