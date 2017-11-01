package com.notlob.jgrid.resources;

import com.notlob.jgrid.styles.AlignmentStyle;

public interface TextLayout extends Disposeable {
	
	void setText(String text);
	
	void setAlignment(AlignmentStyle alignmentStyle);

	void setFont(Font font);

	void setWidth(int width);
	
	void setTabs(final int[] tabWidths);

	Rectangle getBounds();

	void draw(GC gc, int x, int y);

}
