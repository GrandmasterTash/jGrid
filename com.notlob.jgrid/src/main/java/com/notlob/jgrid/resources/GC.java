package com.notlob.jgrid.resources;

public interface GC extends Disposeable {
	// TODO: Make this an interface. Wrapper for SWT or JFX.
	// TODO: See if we can keep the renderers using this GC so they don't need duplicating.
	// TODO: Enable 'final' on parameters.
	
	void setBackground(Color colour);

	void setForeground(Color colour);

	void drawImage(Image errorImage, int i, int j);

	void fillRectangle(int x, int y, int width, int height);

	void setAlpha(int alpha);
	
	void setAntialias(boolean enabled);
	
	void setTextAntialias(boolean enabled);

	void fillGradientRectangle(int x, int y, int width, int halfHeight, boolean b);

	void setFont(Font font);

	void setClipping(Rectangle bounds);

	void fillRoundRectangle(int x, int y, int width, int height, int i, int j);

	void fillRectangle(Rectangle innerBounds);

	void drawText(String text, int x, int y);

	void drawText(String text, int x, int y, boolean thing);
	
	void drawText(String text, int x, int y, int flags);

	FontData getFontMetrics();

	void drawLine(int x, int y, int x2, int y2);

	void drawPoint(int i, int j);

	void setLineWidth(int width);

	Point textExtent(String text);

	void drawRoundRectangle(int x, int y, int width, int height, int i, int j);

	int getLineWidth();
	
}
