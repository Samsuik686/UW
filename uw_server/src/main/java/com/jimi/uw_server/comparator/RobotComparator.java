package com.jimi.uw_server.comparator;

import java.util.Comparator;

import com.jimi.uw_server.model.vo.RobotVO;


/**
 * 叉车比较器
 * 
 * @author HardyYao
 * @createTime 2018年8月20日  下午8:19:53
 */

public class RobotComparator implements Comparator<RobotVO> {

	@Override
	public int compare(RobotVO r1, RobotVO r2) {
		if (r1.getId() > r2.getId()) { // 根据叉车ID，进行升序排序
			return 1;
		} else {
			return -1;
		}
	}

}
