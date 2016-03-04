package com.notlob.jgrid.styles;

import org.eclipse.swt.graphics.RGB;

public class BorderStyle {

	private LineStyle lineStyle;
	private int width;
	private RGB colour;

	public BorderStyle() {
	}

	public BorderStyle(final LineStyle lineStyle) {
		this.width = 1;
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
		
		if (borderStyle.colour != null) {
			copy.colour = new RGB(borderStyle.colour.red, borderStyle.colour.green, borderStyle.colour.blue);
		} else {
			copy.colour = null;
		}
		return copy;
	}

	public LineStyle getLineStyle() {
		return lineStyle;
	}

	public void setLineStyle(final LineStyle lineStyle) {
		this.lineStyle = lineStyle;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(final int width) {
		this.width = width;
	}

	public RGB getColour() {
		return colour;
	}

	public void setColour(final RGB colour) {
		this.colour = colour;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colour == null) ? 0 : colour.hashCode());
		result = prime * result
				+ ((lineStyle == null) ? 0 : lineStyle.hashCode());
		result = prime * result + width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BorderStyle other = (BorderStyle) obj;
		if (colour == null) {
			if (other.colour != null)
				return false;
		} else if (!colour.equals(other.colour))
			return false;
		if (lineStyle != other.lineStyle)
			return false;
		if (width != other.width)
			return false;
		return true;
	}
	
	
}
