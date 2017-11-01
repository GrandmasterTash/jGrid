package com.notlob.jgrid.styles;

import com.notlob.jgrid.resources.FontData;
import com.notlob.jgrid.resources.RGB;

public class CellStyle {

	private ContentStyle contentStyle;
	private Boolean allowContentOverlap;
	private FontData fontData;
	private Integer mouseCursor;
	private AlignmentStyle imageAlignment;
	private AlignmentStyle textAlignment;

	private Integer paddingTop;
	private Integer paddingRight;
	private Integer paddingBottom;
	private Integer paddingLeft;
	private Integer paddingImageText; // If both alignments are the same for image and text.
	private Integer paddingInnerBorder; // The gap between the outer border and the inner border.

	private RGB foreground;
	private RGB background;
	private RGB backgroundGradient1;
	private RGB backgroundGradient2;

	private RGB backgroundAlternate;
	private RGB backgroundAlternateGradient1;
	private RGB backgroundAlternateGradient2;

	private Integer foregroundOpacity;
	private Integer backgroundOpacity;

	private BorderStyle borderInnerTop;
	private BorderStyle borderInnerRight;
	private BorderStyle borderInnerBottom;
	private BorderStyle borderInnerLeft;

	private BorderStyle borderOuterTop;
	private BorderStyle borderOuterRight;
	private BorderStyle borderOuterBottom;
	private BorderStyle borderOuterLeft;

	public CellStyle() {
	}

	public CellStyle copy() {
		final CellStyle copy = new CellStyle();
		copy.contentStyle = contentStyle;
		copy.allowContentOverlap = allowContentOverlap;

		if (fontData != null) {
			copy.fontData = new FontData();
			copy.fontData.setHeight(fontData.getHeight());
			copy.fontData.setLocale(fontData.getLocale());
			copy.fontData.setName(fontData.getName());
			copy.fontData.setStyle(fontData.getStyle());
		}

		copy.mouseCursor = mouseCursor;
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

	public ContentStyle getContentStyle() {
		return contentStyle;
	}

	public void setContentStyle(final ContentStyle contentStyle) {
		this.contentStyle = contentStyle;
	}

	public Boolean isAllowContentOverlap() {
		return allowContentOverlap;
	}

	public void setAllowContentOverlap(final Boolean allowContentOverlap) {
		this.allowContentOverlap = allowContentOverlap;
	}

	public FontData getFontData() {
		return fontData;
	}

	public void setFontData(final FontData fontData) {
		this.fontData = fontData;
	}
	
	public Integer getMouseCursor() {
		return mouseCursor;
	}
	
	public void setMouseCursor(Integer mouseCursor) {
		this.mouseCursor = mouseCursor;
	}

	public AlignmentStyle getImageAlignment() {
		return imageAlignment;
	}

	public void setImageAlignment(final AlignmentStyle imageAlignment) {
		this.imageAlignment = imageAlignment;
	}

	public AlignmentStyle getTextAlignment() {
		return textAlignment;
	}

	public void setTextAlignment(final AlignmentStyle textAlignment) {
		this.textAlignment = textAlignment;
	}

	public Integer getPaddingTop() {
		return paddingTop;
	}

	public void setPaddingTop(final Integer paddingTop) {
		this.paddingTop = paddingTop;
	}

	public Integer getPaddingRight() {
		return paddingRight;
	}

	public void setPaddingRight(final Integer paddingRight) {
		this.paddingRight = paddingRight;
	}

	public Integer getPaddingBottom() {
		return paddingBottom;
	}

	public void setPaddingBottom(final Integer paddingBottom) {
		this.paddingBottom = paddingBottom;
	}

	public Integer getPaddingLeft() {
		return paddingLeft;
	}

	public void setPaddingLeft(final Integer paddingLeft) {
		this.paddingLeft = paddingLeft;
	}

	public Integer getPaddingImageText() {
		return paddingImageText;
	}

	public void setPaddingImageText(final Integer paddingImageText) {
		this.paddingImageText = paddingImageText;
	}

	public Integer getPaddingInnerBorder() {
		return paddingInnerBorder;
	}

	public void setPaddingInnerBorder(final Integer paddingInnerBorder) {
		this.paddingInnerBorder = paddingInnerBorder;
	}

	public RGB getForeground() {
		return foreground;
	}

	public void setForeground(final RGB foreground) {
		this.foreground = foreground;
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

	public RGB getBackgroundAlternate() {
		return backgroundAlternate != null ? backgroundAlternate : background;
	}

	public void setBackgroundAlternate(final RGB backgroundAlternate) {
		this.backgroundAlternate = backgroundAlternate;
	}

	public RGB getBackgroundAlternateGradient1() {
		return backgroundAlternateGradient1;
	}

	public void setBackgroundAlternateGradient1(final RGB backgroundAlternateGradient1) {
		this.backgroundAlternateGradient1 = backgroundAlternateGradient1;
	}

	public RGB getBackgroundAlternateGradient2() {
		return backgroundAlternateGradient2;
	}

	public void setBackgroundAlternateGradient2(final RGB backgroundAlternateGradient2) {
		this.backgroundAlternateGradient2 = backgroundAlternateGradient2;
	}

	public Integer getForegroundOpacity() {
		return foregroundOpacity;
	}

	public void setForegroundOpacity(final Integer foregroundOpacity) {
		this.foregroundOpacity = foregroundOpacity;
	}

	public Integer getBackgroundOpacity() {
		return backgroundOpacity;
	}

	public void setBackgroundOpacity(final Integer backgroundOpacity) {
		this.backgroundOpacity = backgroundOpacity;
	}

	public BorderStyle getBorderInnerTop() {
		return borderInnerTop;
	}

	public void setBorderInnerTop(final BorderStyle borderInnerTop) {
		this.borderInnerTop = borderInnerTop;
	}

	public BorderStyle getBorderInnerRight() {
		return borderInnerRight;
	}

	public void setBorderInnerRight(final BorderStyle borderInnerRight) {
		this.borderInnerRight = borderInnerRight;
	}

	public BorderStyle getBorderInnerBottom() {
		return borderInnerBottom;
	}

	public void setBorderInnerBottom(final BorderStyle borderInnerBottom) {
		this.borderInnerBottom = borderInnerBottom;
	}

	public BorderStyle getBorderInnerLeft() {
		return borderInnerLeft;
	}

	public void setBorderInnerLeft(final BorderStyle borderInnerLeft) {
		this.borderInnerLeft = borderInnerLeft;
	}

	public BorderStyle getBorderOuterTop() {
		return borderOuterTop;
	}

	public void setBorderOuterTop(final BorderStyle borderOuterTop) {
		this.borderOuterTop = borderOuterTop;
	}

	public BorderStyle getBorderOuterRight() {
		return borderOuterRight;
	}

	public void setBorderOuterRight(final BorderStyle borderOuterRight) {
		this.borderOuterRight = borderOuterRight;
	}

	public BorderStyle getBorderOuterBottom() {
		return borderOuterBottom;
	}

	public void setBorderOuterBottom(final BorderStyle borderOuterBottom) {
		this.borderOuterBottom = borderOuterBottom;
	}

	public BorderStyle getBorderOuterLeft() {
		return borderOuterLeft;
	}

	public void setBorderOuterLeft(final BorderStyle borderOuterLeft) {
		this.borderOuterLeft = borderOuterLeft;
	}
}
