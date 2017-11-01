package com.notlob.jgrid.resources;

public class Point {
	
	public int x;
	public int y;
	
	public Point() {
		this(0, 0);
	}
	
	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	
	@Override
	public String toString() {
		return String.format("[%s,%s]", x, y);
	}
	
	public org.eclipse.swt.graphics.Point toSWT() {
		return null;
	}
	
	// TODO: fromSWT fromJFX
}
