/**  
*  
*/
package com.jimi.uw_server.comparator;

import java.util.Comparator;

import com.jimi.uw_server.agv.gaitek.entity.bo.AGVSampleTaskItem;

/**
 * <p>
 * Title: InventoryTaskItemComparator
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2019
 * </p>
 * <p>
 * Company: 惠州市几米物联技术有限公司
 * </p>
 * 
 * @author trjie
 * @date 2020年6月2日
 *
 */
public class SampleTaskItemComparator implements Comparator<AGVSampleTaskItem> {

	public static SampleTaskItemComparator me = new SampleTaskItemComparator();


	/**
	 * <p>
	 * Description:
	 * <p>
	 * 
	 * @return
	 * @exception @author trjie
	 * @Time 2020年6月2日
	 * @param o1
	 * @param o2
	 * @return
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(AGVSampleTaskItem o1, AGVSampleTaskItem o2) {
		return o1.getBoxId() - o2.getBoxId();
	}

}
