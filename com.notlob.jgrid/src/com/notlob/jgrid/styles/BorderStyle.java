package com.notlob.jgrid.styles;

import org.eclipse.swt.graphics.RGB;

public class BorderStyle {
		
	private LineStyle lineStyle;
	private int width;
	private RGB colour;
	
	public BorderStyle() {	
	}
	
	public BorderStyle(final LineStyle lineStyle) {
		this.lineStyle = lineStyle;
	}

	
	public BorderStyle(final int width, final LineStyle lineStyle, final RGB colour) {
		this.width = width;
		this.lineStyle = lineStyle;
		this.colour = colour;		
	}
	
	public static BorderStyle copy(final BorderStyle borderStyle) {
		
		if (borderStyle == null) {
			return null;
		}
		
		final BorderStyle copy = new BorderStyle();		
		copy.lineStyle = borderStyle.lineStyle;
		copy.width = borderStyle.width;
		copy.colour = new RGB(borderStyle.colour.red, borderStyle.colour.green, borderStyle.colour.blue);
		return copy;
	}

	public LineStyle getLineStyle() {
		return lineStyle;
	}

	public void setLineStyle(LineStyle lineStyle) {
		this.lineStyle = lineStyle;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public RGB getColour() {
		return colour;
	}

	public void setColour(RGB colour) {
		this.colour = colour;
	}
}
