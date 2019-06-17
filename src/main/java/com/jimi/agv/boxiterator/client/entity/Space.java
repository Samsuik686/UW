package com.jimi.agv.tracker.entity.bo;

/**
 * 货位
 * <br>
 * <b>2019年6月14日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class Space {

	public static final int UNKNOWN = -1;
	public static final int EMPTY = 0;
	public static final int FULL = 1;
	public static final int LOCK = 2;
	
	private int state;

	private int x;
	private int y;
	private int z;
	
	
	@Override
	public boolean equals(Object object) {
		if(object == null) return false;
		if(object instanceof Space == false) return false;
		Space space = (Space) object;
		if(this.x == space.x && this.y == space.y && this.z == space.z) {
			return true;
		}else {
			return false;
		}
	}
	
	
	@Override
	public String toString() {
		return "[" + x + "," + y + "," + z +  "]";
	}
	
	
	public Space(int x, int y, int z) {
		this.state = Space.UNKNOWN;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = state;
	}

	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getZ() {
		return z;
	}
	
}
