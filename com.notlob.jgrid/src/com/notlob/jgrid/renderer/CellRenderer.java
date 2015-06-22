package com.notlob.jgrid.renderer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.SortDirection;
import com.notlob.jgrid.styles.AlignmentStyle;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.styles.ContentStyle;
import com.notlob.jgrid.styles.LineStyle;

/**
 * Responsible for rendering individual cells.
 * 
 * @author Stef
 */
public class CellRenderer<T> extends Renderer<T> {
	
	// Used when painting a cell throws an exception. 
	protected Image errorImage;
		
	//
	// The references below are recycled objects - to avoid GC churn.
	//
		
	// Used to render the inner border (example: anchor) of a cell and also used to layout the content
	// of a cell.
	protected final Rectangle innerBounds;
	
	// Used to align an image and/or text within the innerBounds.
	protected final Point contentLocation;	
	
	public CellRenderer(final Grid<T> grid) {
		super(grid);
		errorImage = getImage("cell_error.gif");			
		contentLocation = new Point(0, 0);
		innerBounds = new Rectangle(0, 0, 0, 0);
	}
	
	/**
	 * Paint the background, borders and content for the specified cell (column and row).
	 * 
	 * The cell's background, content and (any) inner borders are painted within the specified cell bounds.
	 * 
	 * The cell's outer border can be renderer around the outside of these bounds.
	 */
	public void paintCell(final RenderContext rc, final Rectangle bounds, final Column column, final Row<T> row, final CellStyle cellStyle) {			
		try {
			CellStyle currentStyle = cellStyle;
			
			//
			// If the cell has the anchor, use a composite style.
			//
			if (!rc.isPaintingPinned() && grid.isFocusControl() && grid.isHighlightAnchorCellBorder() && doesColumnHaveAnchor(column) && doesRowHaveAnchor(row)) {
				styleRegistry.getStyleCollector().addFirst(styleRegistry.getAnchorStyle());
				currentStyle = styleRegistry.getStyleCollector().getCellStyle();
			}			
						
			if (rc.getRenderPass() == RenderPass.BACKGROUND) {
				//
				// Paint the cell background.
				//
				paintCellBackground(rc, bounds, currentStyle, row);
				
			} else if (rc.getRenderPass() == RenderPass.FOREGROUND) {
				//
				// Paint cell content.
				//
				paintCellContent(rc, bounds, column, row, currentStyle);
				paintCellBorders(rc, bounds, currentStyle);
			}
			
		} catch (final Throwable t) {
			if (!rc.isErrorLogged()) {
				//
				// Print the error to the std err and ensure we only do this once to avoid log fillage.
				//
				System.err.println(String.format("Failed to paint: %s", t.getMessage()));
				t.printStackTrace(System.err);
				rc.setErrorLogged(true);
			}

			//
			// Render a failure.
			//
			final RGB background = (rc.isAlternate() && (cellStyle.getBackgroundAlternate() != null)) ? cellStyle.getBackgroundAlternate() : cellStyle.getBackground();
			final GC gc = rc.getGC();
			gc.setForeground(getColour(cellStyle.getForeground()));
			gc.setBackground(getColour(background));
			gc.drawImage(errorImage, bounds.x + 2, bounds.y + 2);
			gc.drawText("ERROR", bounds.x + 2 + errorImage.getBounds().width, bounds.y + 2);
		}
	}

	/**
	 * Fill the cell background. Expand the area of the fill to include any cell spacing, otherwise strips are left
	 * in the background colour of the grid.
	 */
	protected void paintCellBackground(final RenderContext rc, final Rectangle bounds, final CellStyle cellStyle, final Row<T> row) throws Exception {
		final GC gc = rc.getGC();
		gc.setAlpha(cellStyle.getBackgroundOpacity());

		final RGB background = (rc.isAlternate() && (cellStyle.getBackgroundAlternate() != null)) ? cellStyle.getBackgroundAlternate() : cellStyle.getBackground();
		final RGB backgroundGradient1 = (rc.isAlternate() && (cellStyle.getBackgroundAlternateGradient1() != null)) ? cellStyle.getBackgroundAlternateGradient1() : cellStyle.getBackgroundGradient1();
		final RGB backgroundGradient2 = (rc.isAlternate() && (cellStyle.getBackgroundAlternateGradient2() != null)) ? cellStyle.getBackgroundAlternateGradient2() : cellStyle.getBackgroundGradient2();

		//
		// Get the background colour (or colours if there's a gradient).
		//
		if (backgroundGradient1 == null || backgroundGradient2 == null) {
			//
			// Fill with no Gradient
			//
			gc.setBackground(getColour(background));
			
			if ((row != null) && (row.getAnimation() != null)) {
				row.getAnimation().pulseBackground(rc, row);
			}
			
			gc.fillRectangle(bounds.x, bounds.y, bounds.width, bounds.height);

		} else {
			//
			// Fill with Gradient (upper, lower).
			//
			final int halfHeight = bounds.height / 2;
			gc.setForeground(getColour(backgroundGradient1));
			gc.setBackground(getColour(background));
			gc.fillGradientRectangle(bounds.x, bounds.y, bounds.width, halfHeight, true);

			gc.setForeground(getColour(background));
			gc.setBackground(getColour(backgroundGradient2));
			gc.fillGradientRectangle(bounds.x, bounds.y + halfHeight, bounds.width, 1 + halfHeight, true);
		}
	}
	
	/**
	 * Paint the outer then inner borders of the cell.
	 */
	protected void paintCellBorders(final RenderContext rc, final Rectangle bounds, final CellStyle cellStyle) throws Exception {
		final GC gc = rc.getGC();
		gc.setAlpha(cellStyle.getForegroundOpacity());
		
		//
		// Render outer border.
		//
		setCorners(bounds, topLeft, topRight, bottomRight, bottomLeft);
		paintBorderLine(gc, cellStyle.getBorderOuterTop(), topLeft, topRight);
		paintBorderLine(gc, cellStyle.getBorderOuterBottom(), bottomLeft, bottomRight);
		paintBorderLine(gc, cellStyle.getBorderOuterLeft(), topLeft, bottomLeft);
		
		//
		// Avoid rendering a grip on certain cells (corner and last header for example).
		//
		if (!(rc.isDontPaintGrip() && (cellStyle.getBorderOuterRight() != null) && (cellStyle.getBorderOuterRight().getLineStyle() == LineStyle.GRIP))) {
			paintBorderLine(gc, cellStyle.getBorderOuterRight(), topRight, bottomRight);
		}

		//
		// Calculate where the inner border should be.
		//
		shrinkRectangle(bounds, innerBounds, cellStyle.getPaddingInnerBorder());
		
		//
		// Render inner border.
		//
		setCorners(innerBounds, topLeft, topRight, bottomRight, bottomLeft);
		paintBorderLine(gc, cellStyle.getBorderInnerTop(), topLeft, topRight);		
		paintBorderLine(gc, cellStyle.getBorderInnerBottom(), bottomLeft, bottomRight);
		paintBorderLine(gc, cellStyle.getBorderInnerLeft(), topLeft, bottomLeft);
		paintBorderLine(gc, cellStyle.getBorderInnerRight(), topRight, bottomRight);
	}
	
	/**
	 * Paint cell's image and text within the bounds specified using the style specified.
	 */
	protected void paintCellContent(final RenderContext rc, final Rectangle bounds, final Column column, final Row<T> row, final CellStyle cellStyle) throws Exception {
		
		//
		// The corner cell has no content.
		//
		if (column == gridModel.getRowNumberColumn() && row == gridModel.getColumnHeaderRow()) {
			return;
		}
		
		final GC gc = rc.getGC();
		gc.setAlpha(cellStyle.getForegroundOpacity());
		gc.setFont(getFont(cellStyle.getFontData()));

		//
		// We'll use inner bounds to indicate where the next piece of content will be allowed. Initially, it's the full
		// cell bounds (adjusted for cell padding), then, after the first image or text is drawn, the inner bounds will 
		// shrink/move to ensure the next image/text content can't overwrite it.
		//
		// Note: This should only be used if content overlap is off.
		//
		final int outerBorderLeftWidth = (cellStyle.getBorderOuterLeft() == null ? 0 :cellStyle.getBorderOuterLeft().getWidth());
		final int outerBorderTopWidth = (cellStyle.getBorderOuterTop() == null ? 0 : cellStyle.getBorderOuterTop().getWidth());
		innerBounds.x = bounds.x + cellStyle.getPaddingLeft() + outerBorderLeftWidth;
		innerBounds.y = bounds.y + cellStyle.getPaddingTop() + outerBorderTopWidth;
		innerBounds.width = bounds.width - cellStyle.getPaddingLeft() - cellStyle.getPaddingRight() - outerBorderLeftWidth;
		innerBounds.height = bounds.height - cellStyle.getPaddingTop() - cellStyle.getPaddingBottom() - outerBorderTopWidth;

		//
		// Render cell image BEFORE text..
		//
		if ((row != null) && (cellStyle.getContentStyle() == ContentStyle.IMAGE || cellStyle.getContentStyle() == ContentStyle.IMAGE_THEN_TEXT)) {
			paintCellImage(rc, column, row, cellStyle);
		}

		//
		// Render cell text.
		//
		if (cellStyle.getContentStyle() != ContentStyle.IMAGE) {
			final Rectangle oldClipping = gc.getClipping();
			gc.setClipping(innerBounds);
			paintCellText(rc, column, row, cellStyle);
			gc.setClipping(oldClipping);
		}

		//
		// Render cell image AFTER text..
		//
		if ((row != null) && (cellStyle.getContentStyle() == ContentStyle.TEXT_THEN_IMAGE)) {
			paintCellImage(rc, column, row, cellStyle);
		}
		
	}
	
	/**
	 * Paints the image for the cell (if there is one) regardless of whether we're painting it before (to the left of) 
	 * or after (to the right of) the cell's text (if any).
	 */
	protected void paintCellImage(final RenderContext rc, final Column column, final Row<T> row, final CellStyle cellStyle) throws Exception {
		final Image image = getCellImage(column, row);

		if (image != null) {
			//
			// Get the image alignment.
			//
			final AlignmentStyle imageAlignment = (cellStyle.getImageAlignment() != null) ? cellStyle.getImageAlignment() : (column.getImageAlignment() != null ? column.getImageAlignment() : AlignmentStyle.LEFT_CENTER);
			
			//
			// Align/position and render the image.
			//
			align(image.getBounds().width, image.getBounds().height, innerBounds, contentLocation, imageAlignment);
			rc.getGC().drawImage(image, contentLocation.x, contentLocation.y);

			if (!cellStyle.isAllowContentOverlap()) {
				innerBounds.x += (image.getBounds().width + cellStyle.getPaddingImageText());
				innerBounds.width -= (image.getBounds().width + cellStyle.getPaddingImageText());
			}
		}
	}
	
	/**
	 * Paints the cell text (and animates if required).
	 */
	protected void paintCellText(final RenderContext rc, final Column column, final Row<T> row, final CellStyle cellStyle) throws Exception {
		final String text = getCellText(column, row);
		
		if (text != null && !text.isEmpty()) {
			final GC gc = rc.getGC();
	
			//
			// Ensure any image in the header row that follows text, doesn't have text running through it.
			//
			int widthCap = 0;
			if (row == gridModel.getColumnHeaderRow() && (cellStyle.getContentStyle() == ContentStyle.TEXT_THEN_IMAGE)) {
				final Image image = getCellImage(column, row);
				if (image != null) {
					widthCap = image.getBounds().width + cellStyle.getPaddingImageText();
					innerBounds.width -= widthCap;
					gc.setClipping(innerBounds);
				}
			}
	
			final Point textExtent = getTextExtent(text, rc, cellStyle.getFontData());
			final int width = Math.min(textExtent.x, (innerBounds.width - widthCap));
			final int height = Math.min(textExtent.y, innerBounds.height);
			final AlignmentStyle textAlignment = (cellStyle.getTextAlignment() == null) ? (column.getTextAlignment() == null ? AlignmentStyle.LEFT_CENTER : column.getTextAlignment()) : cellStyle.getTextAlignment();
			align(width, height, innerBounds, contentLocation, textAlignment);
	
			//
			// Perform an animation on the row - if required. This can cause the text to bounce into view.
			//			
			if ((row != null) && (row != gridModel.getColumnHeaderRow()) && (row.getAnimation() != null)) {
				row.getAnimation().animateText(rc, this, row);
			}
			
			final boolean highlightFilterMatch = doesCellHaveStyleableFilterMatch(row, column);
			if (highlightFilterMatch) {
				//
				// Use text highlighting if there's a FilterMatchRange in this column (and it's trackable).
				//
				gc.setBackground(getColour(styleRegistry.getFilterMatchBackground()));
				gc.setForeground(getColour(styleRegistry.getFilterMatchForeground()));
				gc.drawText(text, contentLocation.x, contentLocation.y);
	
			} else {
				//
				// Use normal colours if we're not highlighting a filter result.
				//
				gc.setForeground(getColour(cellStyle.getForeground()));
				gc.drawText(text, contentLocation.x, contentLocation.y, SWT.DRAW_TRANSPARENT);
			}
			
			if (widthCap > 0) {
				innerBounds.width += widthCap;
				gc.setClipping(innerBounds);
			}
	
			if (!cellStyle.isAllowContentOverlap()) {
				innerBounds.x += (width + cellStyle.getPaddingImageText());
				innerBounds.width -= (height + cellStyle.getPaddingImageText());
			}
		}
	}
	
	/**
	 * Gets the text for the cell from the label provider if required.
	 */
	public String getCellText(final Column column, final Row<T> row) {
		if (column == gridModel.getRowNumberColumn()) {
			return String.valueOf(row.getRowIndex() + 1);
		
		} else if (column == gridModel.getGroupSelectorColumn()) {
			return "";

		} else if (row == gridModel.getColumnHeaderRow()) {
			return column.getCaption();

		} else {
			return grid.getLabelProvider().getText(column, row.getElement());
		}
	}
	
	/**
	 * Return the image for the given cell.
	 */
	protected Image getCellImage(final Column column, final Row<T> row) {
		 if (row == gridModel.getColumnHeaderRow()) {
			//
			// Get any image from the provider
			//
			final Image image = grid.getLabelProvider().getHeaderImage(column);

			if (image != null) {
				return image;
			}

			//
			// Return a sorted image if sorted.
			//
			if (column.getSortDirection() != SortDirection.NONE) {
				if (column.getSortDirection() == SortDirection.ASC){
					return getImage("sort_ascending.png");

				} else if (column.getSortDirection() == SortDirection.DESC){
					return getImage("sort_descending.png");
				}
			}

		} else if ((column == gridModel.getRowNumberColumn()) || (column == gridModel.getGroupSelectorColumn())) {
			 return null;
			 
		} else {
			//
			// Get any image from the provider
			//
			return grid.getLabelProvider().getImage(column, row.getElement());
		}

		return null;
	}
	
	public Point getContentLocation() {
		return contentLocation;
	}
	
	public Rectangle getInnerBounds() {
		return innerBounds;
	}
}
