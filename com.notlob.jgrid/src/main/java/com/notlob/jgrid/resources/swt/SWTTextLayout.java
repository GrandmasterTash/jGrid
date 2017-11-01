package com.notlob.jgrid.resources.swt;

import org.eclipse.swt.SWT;

import com.notlob.jgrid.resources.Font;
import com.notlob.jgrid.resources.GC;
import com.notlob.jgrid.resources.Rectangle;
import com.notlob.jgrid.resources.TextLayout;
import com.notlob.jgrid.styles.AlignmentStyle;

public class SWTTextLayout implements TextLayout {

	@Override
	public void dispose() {
	}

	@Override
	public void setText(String text) {
	}

	@Override
	public void setAlignment(AlignmentStyle alignmentStyle) {
		// TODO: Use the converter
	}
//	protected int convertAlignmentToSwt(final AlignmentStyle alignment) {
//		switch (alignment) {
//			case BOTTOM_LEFT:
//			case LEFT_CENTER:
//			case TOP_LEFT:
//				return SWT.LEFT;
//			
//			case BOTTOM_CENTER:
//			case CENTER:
//			case TOP_CENTER:
//				return SWT.CENTER;
//				
//			case BOTTOM_RIGHT:
//			case RIGHT_CENTER:
//			case TOP_RIGHT:
//				return SWT.RIGHT;
//		}
//		
//		return SWT.LEFT;
	}


	@Override
	public void setFont(Font font) {
	}

	@Override
	public void setWidth(int width) {
	}

	@Override
	public void setTabs(int[] tabWidths) {
	}

	@Override
	public Rectangle getBounds() {
		return null;
	}

	@Override
	public void draw(GC gc, int x, int y) {
	}

}
