package com.jimi.agv.tracker.entity.bo;

/**
 * 平面坐标类
 * <br>
 * <b>2019年6月10日</b>
 * @author <a href="https://github.com/darhao">鲁智深</a>
 */
public class Position {

	private int x;
	private int y;
	
	public Position(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
	
}
