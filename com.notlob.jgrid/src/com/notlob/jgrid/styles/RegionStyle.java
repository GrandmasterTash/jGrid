package com.notlob.jgrid.styles;

import org.eclipse.swt.graphics.RGB;

public class RegionStyle {
	
	private BorderStyle border;
	private RGB background;
	private RGB backgroundGradient1;
	private RGB backgroundGradient2;
	private int foregroundOpacity;
	private int backgroundOpacity;
	
	public BorderStyle getBorder() {
		return border;
	}
	
	public void setBorder(BorderStyle border) {
		this.border = border;
	}
	
	public RGB getBackground() {
		return background;
	}
	
	public void setBackground(RGB background) {
		this.background = background;
	}
	
	public RGB getBackgroundGradient1() {
		return backgroundGradient1;
	}
	
	public void setBackgroundGradient1(RGB backgroundGradient1) {
		this.backgroundGradient1 = backgroundGradient1;
	}
	
	public RGB getBackgroundGradient2() {
		return backgroundGradient2;
	}
	
	public void setBackgroundGradient2(RGB backgroundGradient2) {
		this.backgroundGradient2 = backgroundGradient2;
	}
	
	public int getForegroundOpacity() {
		return foregroundOpacity;
	}
	
	public void setForegroundOpacity(int foregroundOpacity) {
		this.foregroundOpacity = foregroundOpacity;
	}
	
	public int getBackgroundOpacity() {
		return backgroundOpacity;
	}
	
	public void setBackgroundOpacity(int backgroundOpacity) {
		this.backgroundOpacity = backgroundOpacity;
	}
}
