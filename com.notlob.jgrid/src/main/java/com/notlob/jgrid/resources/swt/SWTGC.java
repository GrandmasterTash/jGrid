package com.notlob.jgrid.resources.swt;

import org.eclipse.swt.graphics.Drawable;

import com.notlob.jgrid.resources.Color;
import com.notlob.jgrid.resources.Font;
import com.notlob.jgrid.resources.FontData;
import com.notlob.jgrid.resources.GC;
import com.notlob.jgrid.resources.Image;
import com.notlob.jgrid.resources.Point;
import com.notlob.jgrid.resources.Rectangle;

public class SWTGC implements GC {

	public SWTGC(Drawable drawable) {
	}
	
	@Override
	public void dispose() {
	}

	@Override
	public void setBackground(Color colour) {
	}

	@Override
	public void setForeground(Color colour) {
	}

	@Override
	public void drawImage(Image errorImage, int i, int j) {
	}

	@Override
	public void fillRectangle(int x, int y, int width, int height) {
	}

	@Override
	public void setAlpha(int alpha) {
	}

	@Override
	public void setAntialias(boolean enabled) {
	}

	@Override
	public void setTextAntialias(boolean enabled) {
	}

	@Override
	public void fillGradientRectangle(int x, int y, int width, int halfHeight, boolean b) {
	}

	@Override
	public void setFont(Font font) {
	}

	@Override
	public void setClipping(Rectangle bounds) {
	}

	@Override
	public void fillRoundRectangle(int x, int y, int width, int height, int i, int j) {
	}

	@Override
	public void fillRectangle(Rectangle innerBounds) {
	}

	@Override
	public void drawText(String text, int x, int y) {
	}

	@Override
	public void drawText(String text, int x, int y, boolean thing) {
	}

	@Override
	public void drawText(String text, int x, int y, int flags) {
	}

	@Override
	public FontData getFontMetrics() {
		return null;
	}

	@Override
	public void drawLine(int x, int y, int x2, int y2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void drawPoint(int i, int j) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLineWidth(int width) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Point textExtent(String text) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void drawRoundRectangle(int x, int y, int width, int height, int i, int j) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getLineWidth() {
		// TODO Auto-generated method stub
		return 0;
	}
}
