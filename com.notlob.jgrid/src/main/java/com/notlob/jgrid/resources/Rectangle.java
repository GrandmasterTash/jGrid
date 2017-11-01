package com.notlob.jgrid.resources;

public class Rectangle {
	public int x;
	public int y;
	public int width;
	public int height;
	
	public Rectangle() {
		this(0, 0, 0, 0);
	}
	
	public Rectangle(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public boolean contains(int mouseX, int mouseY) {
		return false;
	}

}
