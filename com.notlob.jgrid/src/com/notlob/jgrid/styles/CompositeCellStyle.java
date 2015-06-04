package com.notlob.jgrid.styles;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

/**
 * Allows a form of cascading style inheritence by combining multiple styles into one.
 * 
 * The first non-null value for a property is used from the list of inner styles.
 * 
 * @author Stef
 *
 */
public class CompositeCellStyle extends CellStyle {
	
	private final List<CellStyle> innerStyles;
	
	CompositeCellStyle() {
		innerStyles = new ArrayList<CellStyle>();
	}
	
	public void add(final CellStyle innerStyle) {
		if (innerStyle != null) {
			innerStyles.add(innerStyle);
		}
	}
	
	public void addFirst(final CellStyle innerStyle) {
		innerStyles.add(0, innerStyle);
	}
	
	public void clear() {
		innerStyles.clear();
	}
	
	public boolean isEmpty() {
		return innerStyles.isEmpty();
	}
	
	public ContentStyle getContentStyle() {
		for (final CellStyle innerStyle : innerStyles) {
			final ContentStyle contentStyle = innerStyle.getContentStyle();
			
			if (contentStyle != null) {
				return contentStyle;
			}
		}
		
		return null;
	}

	public void setContentStyle(final ContentStyle contentStyle) {
		throw new UnsupportedOperationException();
	}

	public Boolean isAllowContentOverlap() {
		for (final CellStyle innerStyle : innerStyles) {
			final Boolean allowContentOverlap = innerStyle.isAllowContentOverlap();
			
			if (allowContentOverlap != null) {
				return allowContentOverlap;
			}
		}
		
		return null;
	}

	public void setAllowContentOverlap(final Boolean allowContentOverlap) {
		throw new UnsupportedOperationException();
	}

	public FontData getFontData() {
		for (final CellStyle innerStyle : innerStyles) {
			final FontData fontData = innerStyle.getFontData();
			
			if (fontData != null) {
				return fontData;
			}
		}
		
		return null;
	}

	public void setFontData(final FontData fontData) {
		throw new UnsupportedOperationException();
	}
	
	public Integer getMouseCursor() {
		for (final CellStyle innerStyle : innerStyles) {
			final Integer mouseCursor = innerStyle.getMouseCursor();
			
			if (mouseCursor != null) {
				return mouseCursor;
			}
		}
		
		return null;
	}
	
	public void setMouseCursor(Integer mouseCursor) {
		throw new UnsupportedOperationException();
	}

	public AlignmentStyle getImageAlignment() {
		for (final CellStyle innerStyle : innerStyles) {
			final AlignmentStyle alignmentStyle = innerStyle.getImageAlignment();
			
			if (alignmentStyle != null) {
				return alignmentStyle;
			}
		}
		
		return null;
	}

	public void setImageAlignment(final AlignmentStyle imageAlignment) {
		throw new UnsupportedOperationException();
	}

	public AlignmentStyle getTextAlignment() {
		for (final CellStyle innerStyle : innerStyles) {
			final AlignmentStyle alignmentStyle = innerStyle.getTextAlignment();
			
			if (alignmentStyle != null) {
				return alignmentStyle;
			}
		}
		
		return null;
	}

	public void setTextAlignment(final AlignmentStyle textAlignment) {
		throw new UnsupportedOperationException();
	}

	public Integer getPaddingTop() {
		for (final CellStyle innerStyle : innerStyles) {
			final Integer paddingTop = innerStyle.getPaddingTop();
			
			if (paddingTop != null) {
				return paddingTop;
			}
		}
		
		return null;
	}

	public void setPaddingTop(final Integer paddingTop) {
		throw new UnsupportedOperationException();
	}

	public Integer getPaddingRight() {
		for (final CellStyle innerStyle : innerStyles) {
			final Integer paddingRight = innerStyle.getPaddingRight();
			
			if (paddingRight != null) {
				return paddingRight;
			}
		}
		
		return null;
	}

	public void setPaddingRight(final Integer paddingRight) {
		throw new UnsupportedOperationException();
	}

	public Integer getPaddingBottom() {
		for (final CellStyle innerStyle : innerStyles) {
			final Integer paddingBottom = innerStyle.getPaddingBottom();
			
			if (paddingBottom != null) {
				return paddingBottom;
			}
		}
		
		return null;
	}

	public void setPaddingBottom(final Integer paddingBottom) {
		throw new UnsupportedOperationException();
	}

	public Integer getPaddingLeft() {
		for (final CellStyle innerStyle : innerStyles) {
			final Integer paddingLeft = innerStyle.getPaddingLeft();
			
			if (paddingLeft != null) {
				return paddingLeft;
			}
		}
		
		return null;
	}

	public void setPaddingLeft(final Integer paddingLeft) {
		throw new UnsupportedOperationException();
	}

	public Integer getPaddingImageText() {
		for (final CellStyle innerStyle : innerStyles) {
			final Integer paddingImageText = innerStyle.getPaddingImageText();
			
			if (paddingImageText != null) {
				return paddingImageText;
			}
		}
		
		return null;
	}

	public void setPaddingImageText(final Integer paddingImageText) {
		throw new UnsupportedOperationException();
	}

	public Integer getPaddingInnerBorder() {
		for (final CellStyle innerStyle : innerStyles) {
			final Integer paddingInnerBorder = innerStyle.getPaddingInnerBorder();
			
			if (paddingInnerBorder != null) {
				return paddingInnerBorder;
			}
		}
		
		return null;
	}

	public void setPaddingInnerBorder(final Integer paddingInnerBorder) {
		throw new UnsupportedOperationException();
	}

	public RGB getForeground() {
		for (final CellStyle innerStyle : innerStyles) {
			final RGB foreground = innerStyle.getForeground();
			
			if (foreground != null) {
				return foreground;
			}
		}
		
		return null;
	}

	public void setForeground(final RGB foreground) {
		throw new UnsupportedOperationException();
	}

	public RGB getBackground() {
		for (final CellStyle innerStyle : innerStyles) {
			final RGB background = innerStyle.getBackground();
			
			if (background != null) {
				return background;
			}
		}
		
		return null;
	}

	public void setBackground(final RGB background) {
		throw new UnsupportedOperationException();
	}

	public RGB getBackgroundGradient1() {
		for (final CellStyle innerStyle : innerStyles) {
			final RGB backgroundGradient1 = innerStyle.getBackgroundGradient1();
			
			if (backgroundGradient1 != null) {
				return backgroundGradient1;
			}
		}
		
		return null;
	}

	public void setBackgroundGradient1(final RGB backgroundGradient1) {
		throw new UnsupportedOperationException();
	}

	public RGB getBackgroundGradient2() {
		for (final CellStyle innerStyle : innerStyles) {
			final RGB backgroundGradient2 = innerStyle.getBackgroundGradient2();
			
			if (backgroundGradient2 != null) {
				return backgroundGradient2;
			}
		}
		
		return null;
	}

	public void setBackgroundGradient2(final RGB backgroundGradient2) {
		throw new UnsupportedOperationException();
	}

	public RGB getBackgroundAlternate() {
		for (final CellStyle innerStyle : innerStyles) {
			final RGB backgroundAlternate = innerStyle.getBackgroundAlternate();
			
			if (backgroundAlternate != null) {
				return backgroundAlternate;
			}
		}
		
		return null;
	}

	public void setBackgroundAlternate(final RGB backgroundAlternate) {
		throw new UnsupportedOperationException();
	}

	public RGB getBackgroundAlternateGradient1() {
		for (final CellStyle innerStyle : innerStyles) {
			final RGB backgroundAlternateGradient1 = innerStyle.getBackgroundAlternateGradient1();
			
			if (backgroundAlternateGradient1 != null) {
				return backgroundAlternateGradient1;
			}
		}
		
		return null;
	}

	public void setBackgroundAlternateGradient1(final RGB backgroundAlternateGradient1) {
		throw new UnsupportedOperationException();
	}

	public RGB getBackgroundAlternateGradient2() {
		for (final CellStyle innerStyle : innerStyles) {
			final RGB backgroundAlternateGradient2 = innerStyle.getBackgroundAlternateGradient2();
			
			if (backgroundAlternateGradient2 != null) {
				return backgroundAlternateGradient2;
			}
		}
		
		return null;
	}

	public void setBackgroundAlternateGradient2(final RGB backgroundAlternateGradient2) {
		throw new UnsupportedOperationException();
	}

	public Integer getForegroundOpacity() {
		for (final CellStyle innerStyle : innerStyles) {
			final Integer foregroundOpacity = innerStyle.getForegroundOpacity();
			
			if (foregroundOpacity != null) {
				return foregroundOpacity;
			}
		}
		
		return null;
	}

	public void setForegroundOpacity(final Integer foregroundOpacity) {
		throw new UnsupportedOperationException();
	}

	public Integer getBackgroundOpacity() {
		for (final CellStyle innerStyle : innerStyles) {
			final Integer backgroundOpacity = innerStyle.getBackgroundOpacity();
			
			if (backgroundOpacity != null) {
				return backgroundOpacity;
			}
		}
		
		return null;
	}

	public void setBackgroundOpacity(final Integer backgroundOpacity) {
		throw new UnsupportedOperationException();
	}

	public BorderStyle getBorderInnerTop() {
		for (final CellStyle innerStyle : innerStyles) {
			final BorderStyle borderInnerTop = innerStyle.getBorderInnerTop();
			
			if (borderInnerTop != null) {
				return borderInnerTop;
			}
		}
		
		return null;
	}

	public void setBorderInnerTop(final BorderStyle borderInnerTop) {
		throw new UnsupportedOperationException();
	}

	public BorderStyle getBorderInnerRight() {
		for (final CellStyle innerStyle : innerStyles) {
			final BorderStyle borderInnerRight = innerStyle.getBorderInnerRight();
			
			if (borderInnerRight != null) {
				return borderInnerRight;
			}
		}
		
		return null;
	}

	public void setBorderInnerRight(final BorderStyle borderInnerRight) {
		throw new UnsupportedOperationException();
	}

	public BorderStyle getBorderInnerBottom() {
		for (final CellStyle innerStyle : innerStyles) {
			final BorderStyle borderInnerBottom = innerStyle.getBorderInnerBottom();
			
			if (borderInnerBottom != null) {
				return borderInnerBottom;
			}
		}
		
		return null;
	}

	public void setBorderInnerBottom(final BorderStyle borderInnerBottom) {
		throw new UnsupportedOperationException();
	}

	public BorderStyle getBorderInnerLeft() {
		for (final CellStyle innerStyle : innerStyles) {
			final BorderStyle borderInnerLeft = innerStyle.getBorderInnerLeft();
			
			if (borderInnerLeft != null) {
				return borderInnerLeft;
			}
		}
		
		return null;
	}

	public void setBorderInnerLeft(final BorderStyle borderInnerLeft) {
		throw new UnsupportedOperationException();
	}

	public BorderStyle getBorderOuterTop() {
		for (final CellStyle innerStyle : innerStyles) {
			final BorderStyle borderOuterTop = innerStyle.getBorderOuterTop();
			
			if (borderOuterTop != null) {
				return borderOuterTop;
			}
		}
		
		return null;
	}

	public void setBorderOuterTop(final BorderStyle borderOuterTop) {
		throw new UnsupportedOperationException();
	}

	public BorderStyle getBorderOuterRight() {
		for (final CellStyle innerStyle : innerStyles) {
			final BorderStyle borderOuterRight = innerStyle.getBorderOuterRight();
			
			if (borderOuterRight != null) {
				return borderOuterRight;
			}
		}
		
		return null;
	}

	public void setBorderOuterRight(final BorderStyle borderOuterRight) {
		throw new UnsupportedOperationException();
	}

	public BorderStyle getBorderOuterBottom() {
		for (final CellStyle innerStyle : innerStyles) {
			final BorderStyle borderOuterBottom = innerStyle.getBorderOuterBottom();
			
			if (borderOuterBottom != null) {
				return borderOuterBottom;
			}
		}
		
		return null;
	}

	public void setBorderOuterBottom(final BorderStyle borderOuterBottom) {
		throw new UnsupportedOperationException();
	}

	public BorderStyle getBorderOuterLeft() {
		for (final CellStyle innerStyle : innerStyles) {
			final BorderStyle borderOuterLeft = innerStyle.getBorderOuterLeft();
			
			if (borderOuterLeft != null) {
				return borderOuterLeft;
			}
		}
		
		return null;
	}

	public void setBorderOuterLeft(final BorderStyle borderOuterLeft) {
		throw new UnsupportedOperationException();
	}
}
