package com.jimi.agv.tracker.space;

import java.util.HashSet;
import java.util.Set;

import com.jimi.agv.tracker.entity.bo.Space;
import com.jimi.agv.tracker.task.CushionAGVIOTaskItem;
import com.jimi.agv.tracker.task.TraditionAGVIOTaskItem;

/**
 * 货位管理器
 * <br>
 * <b>2019年6月14日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class SpaceManager {

	private static SpaceManager me;
	
	private Set<Space> spaces = new HashSet<>();
	
	
	public synchronized static SpaceManager getInstance() {
		if(me == null) {
			me = new SpaceManager();
		}
		return me;
	}
	
	
	/**
	 * 尝试锁定缓冲任务条目的目标和源货位，成功返回true
	 */
	public synchronized boolean tryLock(CushionAGVIOTaskItem item) {
		//构造货位实体
		Space sourceSpace = getSpaceByXYZ(item.getSourceX(), item.getSourceY(), item.getSourceZ());
		Space targetSpace = getSpaceByXYZ(item.getTargetX(), item.getTargetY(), item.getTargetZ());
		
		//还没记录的的源货位，默认给一个满状态
		if(sourceSpace == null) {
			sourceSpace = new Space(item.getSourceX(), item.getSourceY(), item.getSourceZ());
			sourceSpace.setState(Space.FULL);
			spaces.add(sourceSpace);
		}
		
		//还没记录的的目标货位，默认给一个空状态
		if(targetSpace == null) {
			targetSpace = new Space(item.getTargetX(), item.getTargetY(), item.getTargetZ());
			targetSpace.setState(Space.EMPTY);
			spaces.add(targetSpace);
		}
		
		//如果源是满状态，目标是空状态，则把它们锁定
		if(sourceSpace.getState() == Space.FULL && targetSpace.getState() == Space.EMPTY) {
			sourceSpace.setState(Space.LOCK);
			targetSpace.setState(Space.LOCK);
			return true;
		}else{
			return false;
		}
	}
	
	
	/**
	 * 尝试锁定传统任务条目的目标和源货位，成功返回true
	 */
	public synchronized boolean tryLock(TraditionAGVIOTaskItem item) {
		//构造货位实体
		Space targetSpace = getSpaceByXYZ(item.getTargetX(), item.getTargetY(), item.getTargetZ());
		
		//还没记录的的源货位，默认给一个满状态
		if(targetSpace == null) {
			targetSpace = new Space(item.getTargetX(), item.getTargetY(), item.getTargetZ());
			targetSpace.setState(Space.FULL);
			spaces.add(targetSpace);
		}
		
		//如果目标是满状态，则把它锁定
		if(targetSpace.getState() == Space.FULL) {
			targetSpace.setState(Space.LOCK);
			return true;
		}else{
			return false;
		}
	}


	/**
	 * 置满指定货位
	 */
	public void fill(int x, int y, int z) {
		Space space = getSpaceByXYZ(x, y, z);
		if(space == null) {
			throw new NullPointerException("不存在该货位：[" + x + "," + y + "," + z + "]");
		}
		if(space.getState() != Space.LOCK) {
			throw new IllegalStateException("货位必须处于锁定状态才能转换成满状态：" + space.toString());
		}
		space.setState(Space.FULL);
	}
	
	
	/**
	 * 置空指定货位
	 */
	public void empty(int x, int y, int z) {
		Space space = getSpaceByXYZ(x, y, z);
		if(space == null) {
			throw new NullPointerException("不存在该货位：[" + x + "," + y + "," + z + "]");
		}
		if(space.getState() != Space.LOCK) {
			throw new IllegalStateException("货位必须处于锁定状态才能转换成空状态：" + space.toString());
		}
		space.setState(Space.EMPTY);
	}
	
	
	private Space getSpaceByXYZ(int x, int y, int z) {
		Space temp = new Space(x, y, z);
		for (Space space : spaces) {
			if(space.equals(temp)) {
				return space;
			}
		}
		return null;
	}
	
}
