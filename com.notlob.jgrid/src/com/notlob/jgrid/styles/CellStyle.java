package com.notlob.jgrid.styles;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

public class CellStyle {
	
	// TODO: Mouse cursor.
	
	private InheritanceStyle inheritanceStyle;
	private ContentStyle contentStyle;
	private boolean allowContentOverlap;
	
	private FontData fontData;
	
	private AlignmentStyle imageAlignment;
	private AlignmentStyle textAlignment;
	
	private int paddingTop;
	private int paddingRight;
	private int paddingBottom;
	private int paddingLeft;	
	private int paddingImageText; // If both alignments are the same for image and text.
	private int paddingInnerBorder; // The gap between the outer border and the inner border.
	
	private RGB foreground;
	private RGB background;
	private RGB backgroundGradient1;
	private RGB backgroundGradient2;
	
	private RGB backgroundAlternate;
	private RGB backgroundAlternateGradient1;
	private RGB backgroundAlternateGradient2;
	
	private int foregroundOpacity;
	private int backgroundOpacity;
	
	private BorderStyle borderInnerTop;
	private BorderStyle borderInnerRight;
	private BorderStyle borderInnerBottom;
	private BorderStyle borderInnerLeft;
	
	private BorderStyle borderOuterTop;
	private BorderStyle borderOuterRight;
	private BorderStyle borderOuterBottom;	
	private BorderStyle borderOuterLeft;

	// TODO: Consider Base64Encoder copying....
	public CellStyle copy() {
		final CellStyle copy = new CellStyle();
		copy.inheritanceStyle = inheritanceStyle;
		copy.contentStyle = contentStyle;
		copy.allowContentOverlap = allowContentOverlap;
		
		if (fontData != null) {
			copy.fontData = new FontData();
			copy.fontData.setHeight(fontData.getHeight());
			copy.fontData.setLocale(fontData.getLocale());
			copy.fontData.setName(fontData.getName());
			copy.fontData.setStyle(fontData.getStyle());
		}
		
		copy.imageAlignment = imageAlignment;
		copy.textAlignment = textAlignment;
		copy.paddingTop = paddingTop;
		copy.paddingRight = paddingRight;
		copy.paddingBottom = paddingBottom;
		copy.paddingLeft = paddingLeft;
		copy.paddingImageText = paddingImageText;
		copy.paddingInnerBorder = paddingInnerBorder;
		copy.foreground = foreground == null ? null : new RGB(foreground.red, foreground.green, foreground.blue);
		copy.background = background == null ? null : new RGB(background.red, background.green, background.blue);
		copy.backgroundGradient1 = backgroundGradient1 == null ? null : new RGB(backgroundGradient1.red, backgroundGradient1.green, backgroundGradient1.blue);
		copy.backgroundGradient2 = backgroundGradient2 == null ? null : new RGB(backgroundGradient2.red, backgroundGradient2.green, backgroundGradient2.blue);		
		copy.backgroundAlternate = backgroundAlternate == null ? null : new RGB(backgroundAlternate.red, backgroundAlternate.green, backgroundAlternate.blue);
		copy.backgroundAlternateGradient1 = backgroundAlternateGradient1 == null ? null : new RGB(backgroundAlternateGradient1.red, backgroundAlternateGradient1.green, backgroundAlternateGradient1.blue);
		copy.backgroundAlternateGradient2 = backgroundAlternateGradient2 == null ? null : new RGB(backgroundAlternateGradient2.red, backgroundAlternateGradient2.green, backgroundAlternateGradient2.blue);		
		copy.foregroundOpacity = foregroundOpacity;
		copy.backgroundOpacity = backgroundOpacity;
		copy.borderInnerTop = BorderStyle.copy(borderInnerTop);
		copy.borderInnerRight = BorderStyle.copy(borderInnerRight);
		copy.borderInnerBottom = BorderStyle.copy(borderInnerBottom);
		copy.borderInnerLeft = BorderStyle.copy(borderInnerLeft);
		copy.borderOuterTop = BorderStyle.copy(borderOuterTop);
		copy.borderOuterRight = BorderStyle.copy(borderOuterRight);
		copy.borderOuterBottom = BorderStyle.copy(borderOuterBottom);
		copy.borderOuterLeft = BorderStyle.copy(borderOuterLeft);
		return copy;
	}
	
	public InheritanceStyle getInheritanceStyle() {
		return inheritanceStyle;
	}

	public void setInheritanceStyle(InheritanceStyle inheritanceStyle) {
		this.inheritanceStyle = inheritanceStyle;
	}
	
	public ContentStyle getContentStyle() {
		return contentStyle;
	}

	public void setContentStyle(ContentStyle contentStyle) {
		this.contentStyle = contentStyle;
	}
	
	public boolean isAllowContentOverlap() {
		return allowContentOverlap;
	}
	
	public void setAllowContentOverlap(boolean allowContentOverlap) {
		this.allowContentOverlap = allowContentOverlap;
	}

	public FontData getFontData() {
		return fontData;
	}

	public void setFontData(FontData fontData) {
		this.fontData = fontData;
	}

	public AlignmentStyle getImageAlignment() {
		return imageAlignment;
	}

	public void setImageAlignment(AlignmentStyle imageAlignment) {
		this.imageAlignment = imageAlignment;
	}

	public AlignmentStyle getTextAlignment() {
		return textAlignment;
	}

	public void setTextAlignment(AlignmentStyle textAlignment) {
		this.textAlignment = textAlignment;
	}

	public int getPaddingTop() {
		return paddingTop;
	}

	public void setPaddingTop(int paddingTop) {
		this.paddingTop = paddingTop;
	}

	public int getPaddingRight() {
		return paddingRight;
	}

	public void setPaddingRight(int paddingRight) {
		this.paddingRight = paddingRight;
	}

	public int getPaddingBottom() {
		return paddingBottom;
	}

	public void setPaddingBottom(int paddingBottom) {
		this.paddingBottom = paddingBottom;
	}

	public int getPaddingLeft() {
		return paddingLeft;
	}

	public void setPaddingLeft(int paddingLeft) {
		this.paddingLeft = paddingLeft;
	}

	public int getPaddingImageText() {
		return paddingImageText;
	}

	public void setPaddingImageText(int paddingImageText) {
		this.paddingImageText = paddingImageText;
	}

	public int getPaddingInnerBorder() {
		return paddingInnerBorder;
	}

	public void setPaddingInnerBorder(int paddingInnerBorder) {
		this.paddingInnerBorder = paddingInnerBorder;
	}

	public RGB getForeground() {
		return foreground;
	}

	public void setForeground(RGB foreground) {
		this.foreground = foreground;
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
	
	public RGB getBackgroundAlternate() {
		return backgroundAlternate != null ? backgroundAlternate : background;
	}

	public void setBackgroundAlternate(RGB backgroundAlternate) {
		this.backgroundAlternate = backgroundAlternate;
	}

	public RGB getBackgroundAlternateGradient1() {
		return backgroundAlternateGradient1;
	}

	public void setBackgroundAlternateGradient1(RGB backgroundAlternateGradient1) {
		this.backgroundAlternateGradient1 = backgroundAlternateGradient1;
	}

	public RGB getBackgroundAlternateGradient2() {
		return backgroundAlternateGradient2;
	}

	public void setBackgroundAlternateGradient2(RGB backgroundAlternateGradient2) {
		this.backgroundAlternateGradient2 = backgroundAlternateGradient2;
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
	
	public BorderStyle getBorderInnerTop() {
		return borderInnerTop;
	}

	public void setBorderInnerTop(BorderStyle borderInnerTop) {
		this.borderInnerTop = borderInnerTop;
	}

	public BorderStyle getBorderInnerRight() {
		return borderInnerRight;
	}

	public void setBorderInnerRight(BorderStyle borderInnerRight) {
		this.borderInnerRight = borderInnerRight;
	}

	public BorderStyle getBorderInnerBottom() {
		return borderInnerBottom;
	}

	public void setBorderInnerBottom(BorderStyle borderInnerBottom) {
		this.borderInnerBottom = borderInnerBottom;
	}

	public BorderStyle getBorderInnerLeft() {
		return borderInnerLeft;
	}

	public void setBorderInnerLeft(BorderStyle borderInnerLeft) {
		this.borderInnerLeft = borderInnerLeft;
	}

	public BorderStyle getBorderOuterTop() {
		return borderOuterTop;
	}

	public void setBorderOuterTop(BorderStyle borderOuterTop) {
		this.borderOuterTop = borderOuterTop;
	}

	public BorderStyle getBorderOuterRight() {
		return borderOuterRight;
	}

	public void setBorderOuterRight(BorderStyle borderOuterRight) {
		this.borderOuterRight = borderOuterRight;
	}

	public BorderStyle getBorderOuterBottom() {
		return borderOuterBottom;
	}

	public void setBorderOuterBottom(BorderStyle borderOuterBottom) {
		this.borderOuterBottom = borderOuterBottom;
	}

	public BorderStyle getBorderOuterLeft() {
		return borderOuterLeft;
	}

	public void setBorderOuterLeft(BorderStyle borderOuterLeft) {
		this.borderOuterLeft = borderOuterLeft;
	}
}
