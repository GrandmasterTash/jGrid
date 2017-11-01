package com.notlob.jgrid.styles;

import com.notlob.jgrid.resources.RGB;

public class RegionStyle {

	private BorderStyle border;
	private RGB background;
	private RGB backgroundGradient1;
	private RGB backgroundGradient2;
	private int foregroundOpacity;
	private int backgroundOpacity;

	public RegionStyle copy() {
		final RegionStyle copy = new RegionStyle();
		copy.border = BorderStyle.copy(border);
		copy.background = background;
		copy.backgroundGradient1 = backgroundGradient1;
		copy.backgroundGradient2 = backgroundGradient2;
		copy.foregroundOpacity = foregroundOpacity;
		copy.backgroundOpacity = backgroundOpacity;
		return copy;
	}
	
	public BorderStyle getBorder() {
		return border;
	}

	public void setBorder(final BorderStyle border) {
		this.border = border;
	}

	public RGB getBackground() {
		return background;
	}

	public void setBackground(final RGB background) {
		this.background = background;
	}

	public RGB getBackgroundGradient1() {
		return backgroundGradient1;
	}

	public void setBackgroundGradient1(final RGB backgroundGradient1) {
		this.backgroundGradient1 = backgroundGradient1;
	}

	public RGB getBackgroundGradient2() {
		return backgroundGradient2;
	}

	public void setBackgroundGradient2(final RGB backgroundGradient2) {
		this.backgroundGradient2 = backgroundGradient2;
	}

	public int getForegroundOpacity() {
		return foregroundOpacity;
	}

	public void setForegroundOpacity(final int foregroundOpacity) {
		this.foregroundOpacity = foregroundOpacity;
	}

	public int getBackgroundOpacity() {
		return backgroundOpacity;
	}

	public void setBackgroundOpacity(final int backgroundOpacity) {
		this.backgroundOpacity = backgroundOpacity;
	}
}
