package com.notlob.jgrid.renderer;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.SortDirection;
import com.notlob.jgrid.styles.AlignmentStyle;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.styles.ContentStyle;
import com.notlob.jgrid.styles.LineStyle;
import com.notlob.jgrid.styles.StyleCollector;

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
		
	// Used to render the inner border (example: anchor) of a cell and also used to layout the content of a cell.
	protected final Rectangle innerBounds;
	
	// Used to size the area needed when painting one or more images in a cell.
	protected final Point imageExtent;
	
	// Used to align an image and/or text within the innerBounds.
	protected final Point contentLocation;
	
	protected final TextLayout textLayout;
	
	protected final StyleCollector anchorCollector;
	
	public CellRenderer(final Grid<T> grid) {
		super(grid);
		errorImage = getImage("cell_error.gif");			
		contentLocation = new Point(0, 0);
		imageExtent = new Point(0, 0);
		innerBounds = new Rectangle(0, 0, 0, 0);
		textLayout = new TextLayout(grid.getDisplay());
		anchorCollector = new StyleCollector();
		grid.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				textLayout.dispose();
			}
		});
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
				anchorCollector.clear();
				grid.getLabelProvider().getAnchorStyle(anchorCollector, column, row.getElement());
				
				if (anchorCollector.isEmpty()) {
					styleRegistry.getStyleCollector().addFirst(styleRegistry.getAnchorStyle());
					currentStyle = styleRegistry.getStyleCollector().getCellStyle();
					
				} else {
					currentStyle = anchorCollector.getCellStyle();
				}
			}			
			
			switch (rc.getRenderPass()) {
				case BACKGROUND:
					//
					// Paint the cell background.
					//
					paintCellBackground(rc, bounds, currentStyle, column, row);
					break;
					
				case FOREGROUND:
					//
					// Paint cell content
					//
					paintCellContent(rc, bounds, column, row, currentStyle);
					paintCellBorders(rc, bounds, column, row, currentStyle);
					break;
					
				case COMPUTE_SIZE:
					//
					// Calculate the cell/row size.
					//
					paintCellContent(rc, bounds, column, row, currentStyle);
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
	protected void paintCellBackground(final RenderContext rc, final Rectangle bounds, final CellStyle cellStyle, final Column column, final Row<T> row) throws Exception {
		final GC gc = rc.getGC();
		gc.setAlpha(cellStyle.getBackgroundOpacity() == null ? 255 : cellStyle.getBackgroundOpacity());

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
	protected void paintCellBorders(final RenderContext rc, final Rectangle bounds, final Column column, final Row<T> row, final CellStyle cellStyle) throws Exception {
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
		gc.setAlpha(cellStyle.getForegroundOpacity() == null ? 255 : cellStyle.getForegroundOpacity());
		gc.setFont(getFont(cellStyle.getFontData() == null ? styleRegistry.getDefaultStyle().getFontData() : cellStyle.getFontData()));

		//
		// We'll use inner bounds to indicate where the next piece of content will be allowed. Initially, it's the full
		// cell bounds (adjusted for cell padding), then, after the first image or text is drawn, the inner bounds will 
		// shrink/move to ensure the next image/text content can't overwrite it.
		//
		// Note: This should only be used if content overlap is off.
		//
		final int outerBorderLeftWidth = (cellStyle.getBorderOuterLeft() == null ? 0 :cellStyle.getBorderOuterLeft().getWidth());
		final int outerBorderTopWidth = (cellStyle.getBorderOuterTop() == null ? 0 : cellStyle.getBorderOuterTop().getWidth());
		final int paddingLeft = (cellStyle.getPaddingLeft() == null ? 0 : cellStyle.getPaddingLeft());
		final int paddingRight = (cellStyle.getPaddingRight() == null ? 0 : cellStyle.getPaddingRight());
		final int paddingTop = (cellStyle.getPaddingTop() == null ? 0 : cellStyle.getPaddingTop());
		final int paddingBottom = (cellStyle.getPaddingBottom() == null ? 0 : cellStyle.getPaddingBottom());

		innerBounds.x = bounds.x + paddingLeft + outerBorderLeftWidth;
		innerBounds.y = bounds.y + paddingTop + outerBorderTopWidth;
		innerBounds.width = bounds.width - paddingLeft - paddingRight - outerBorderLeftWidth;
		innerBounds.height = bounds.height - paddingTop - paddingBottom - outerBorderTopWidth;

		//
		// Render cell image BEFORE text..
		//
		if ((row != null) && (cellStyle.getContentStyle() == ContentStyle.IMAGE || cellStyle.getContentStyle() == ContentStyle.IMAGE_THEN_TEXT)) {
			gc.setClipping(bounds);
			paintCellImages(rc, column, row, cellStyle);
		}

		//
		// Render cell text.
		//
		if (cellStyle.getContentStyle() != ContentStyle.IMAGE) {
			gc.setClipping(innerBounds);
			paintCellText(rc, column, row, cellStyle);
		}

		//
		// Render cell image AFTER text..
		//
		if ((row != null) && (cellStyle.getContentStyle() == ContentStyle.TEXT_THEN_IMAGE)) {
			gc.setClipping(bounds);
			paintCellImages(rc, column, row, cellStyle);
		}
		
		gc.setClipping((Rectangle) null);
	}
	
	/**
	 * Paints the image for the cell (if there is one) regardless of whether we're painting it before (to the left of) 
	 * or after (to the right of) the cell's text (if any).
	 */
	protected void paintCellImages(final RenderContext rc, final Column column, final Row<T> row, final CellStyle cellStyle) throws Exception {
		final List<Image> images = getCellImages(column, row);
		final boolean highlightFilterMatch = doesCellHaveStyleableFilterMatch(row, column);
		
		if (!images.isEmpty()) {
			if (highlightFilterMatch && cellStyle.getContentStyle() == ContentStyle.IMAGE) {
				rc.getGC().setBackground(getColour(styleRegistry.getFilterMatchBackground()));
				rc.getGC().fillRoundRectangle(innerBounds.x, innerBounds.y, innerBounds.width, innerBounds.height, 4, 4);
			}
			
			//
			// Get the combines bounds of all the images.
			//
			imageExtent.x = 0;
			imageExtent.y = 0;
			
			for (Image image : images) {
				imageExtent.x += image.getBounds().width;
				imageExtent.y = Math.max(image.getBounds().height, imageExtent.y);
			}
			
			//
			// Align them all within the available space (innerBounds).
			//
			final AlignmentStyle imageAlignment = (cellStyle.getImageAlignment() != null) ? cellStyle.getImageAlignment() : (column.getImageAlignment() != null ? column.getImageAlignment() : AlignmentStyle.LEFT_CENTER);
			align(imageExtent.x, imageExtent.y, innerBounds, contentLocation, imageAlignment);
			
			// TODO: Highlight if filter match.
			
			//
			// Render the images left-to-right in this space.
			//
			for (Image image : images) {
				if (rc.getRenderPass() == RenderPass.FOREGROUND) {
					rc.getGC().drawImage(image, contentLocation.x, contentLocation.y);
				}
				
				contentLocation.x += image.getBounds().width;
			}
			
			if (cellStyle.isAllowContentOverlap() == null || !cellStyle.isAllowContentOverlap()) {
				final int paddingImageText = (cellStyle.getPaddingImageText() == null ? 0 : cellStyle.getPaddingImageText());
				innerBounds.x += (imageExtent.x + paddingImageText);
				innerBounds.width -= (imageExtent.x + paddingImageText);
			}
			
		} else {
			if (highlightFilterMatch && cellStyle.getContentStyle() == ContentStyle.IMAGE) {
				rc.getGC().setBackground(getColour(styleRegistry.getFilterMatchBackground()));
				rc.getGC().fillRoundRectangle(innerBounds.x, innerBounds.y, innerBounds.width, innerBounds.height, 4, 4);
			}
		}
	}
	
	/**
	 * Paints the cell text (and animates if required).
	 */
	protected void paintCellText(final RenderContext rc, final Column column, final Row<T> row, final CellStyle cellStyle) throws Exception {
		final String text = getCellText(column, row);
		final GC gc = rc.getGC();

		//
		// Ensure any image in the header row that follows text, doesn't have text running through it.
		//
		int widthCap = 0;
		if (row == gridModel.getColumnHeaderRow() && (cellStyle.getContentStyle() == ContentStyle.TEXT_THEN_IMAGE)) {
			final List<Image> images = getCellImages(column, row);
			
			if (!images.isEmpty()) {
				for (Image image : images) {
					widthCap += image.getBounds().width;
				}
				
				widthCap += cellStyle.getPaddingImageText();
				innerBounds.width -= widthCap;
				gc.setClipping(innerBounds);
			}
		}

		final boolean highlightFilterMatch = doesCellHaveStyleableFilterMatch(row, column);
		final AlignmentStyle textAlignment = (cellStyle.getTextAlignment() == null) ? (column.getTextAlignment() == null ? AlignmentStyle.LEFT_CENTER : column.getTextAlignment()) : cellStyle.getTextAlignment();
		int width;
		int height;
		
		if (column.isWrap() && (row != grid.getColumnHeaderRow())) {
			//
			// Use a wrapping method of rendering the text.
			//
			textLayout.setText(text);				
			textLayout.setAlignment(convertAlignmentToSwt(textAlignment));
			textLayout.setFont(getFont(cellStyle.getFontData()));
			
			//
			// Edge-case, don't use available width for the last column if it's less that it's defined width.
			//
			final boolean lastColumn = (column == gridModel.getColumns().get(gridModel.getColumns().size() - 1));
			textLayout.setWidth(lastColumn ? (Math.max(column.getWidth(), innerBounds.width)) : innerBounds.width);
			
			if (rc.getRenderPass() == RenderPass.COMPUTE_SIZE) {
				computeRowSize(rc, column, textLayout.getBounds().height, innerBounds.height);
			}
			
			width = textLayout.getBounds().width;
			height = textLayout.getBounds().height;
			align(width, height, innerBounds, contentLocation, textAlignment);
			
			if (rc.getRenderPass() == RenderPass.FOREGROUND) {
				if (highlightFilterMatch) {
					//
					// Use text highlighting if there's a FilterMatchRange in this column (and it's trackable).
					//
					gc.setBackground(getColour(styleRegistry.getFilterMatchBackground()));
					gc.setForeground(getColour(styleRegistry.getFilterMatchForeground()));
					
					if (text == null || text.length() == 0) {
						//
						// There's no text to highlight (but the blank cell matches the filter), so render a
						// highlighted box.
						//
						gc.fillRoundRectangle(innerBounds.x, innerBounds.y, innerBounds.width, innerBounds.height, 4, 4);
					}
					
					textLayout.draw(gc, contentLocation.x, contentLocation.y);
		
				} else {
					//
					// Use normal colours if we're not highlighting a filter result.
					//
					gc.setForeground(getColour(cellStyle.getForeground()));
					textLayout.draw(gc, contentLocation.x, contentLocation.y);
				}
			}
			
		} else {
			//
			// Use a non-wrapping method of rendering the text.
			//
			final Point textExtent = getTextExtent(text, rc, cellStyle.getFontData());
			
			width = Math.min(textExtent.x, (innerBounds.width - 0/*widthCap seems to shunt right-aligned text if theres an image - removing didn't cause any harm...*/));
			height = Math.min(textExtent.y, innerBounds.height);	
			align(width, height, innerBounds, contentLocation, textAlignment);
			
			if (rc.getRenderPass() == RenderPass.FOREGROUND) {
				//
				// Perform an animation on the row - if required. This can cause the text to bounce into view.
				//			
				if ((row != null) && (row != gridModel.getColumnHeaderRow()) && (row.getAnimation() != null)) {
					row.getAnimation().animateText(rc, this, row);
				}
				
				if (highlightFilterMatch) {
					//
					// Use text highlighting if there's a FilterMatchRange in this column (and it's trackable).
					//
					gc.setBackground(getColour(styleRegistry.getFilterMatchBackground()));
					gc.setForeground(getColour(styleRegistry.getFilterMatchForeground()));
					
					
					
					if (text == null || text.length() == 0) {
						//
						// There's no text to highlight (but the blank cell matches the filter), so render a
						// highlighted box.
						//
						gc.fillRectangle(innerBounds);
					}
					
					gc.drawText(text, contentLocation.x, contentLocation.y);
		
				} else {
					//
					// Use normal colours if we're not highlighting a filter result.
					//
					gc.setForeground(getColour(cellStyle.getForeground()));
					gc.drawText(text, contentLocation.x, contentLocation.y, SWT.DRAW_DELIMITER | SWT.DRAW_TAB | SWT.DRAW_TRANSPARENT);
				}
			}
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
	
	/**
	 * Figure out if we need to shrink of grow the row height based on 
	 *   a) previous cell's calculations for this row
	 *   b) this cell's content.
	 */
	protected void computeRowSize(final RenderContext rc, final Column column, final int desiredHeight, final int currentHeight) {
		//
		// If we're computing size, this is our chance to alter the row height if the text wraps.
		//
		final int delta = (desiredHeight - currentHeight);
		
//		System.out.println(String.format("Column [%s] desired-height [%s] current-height [%s] delta [%s] computed [%s]", 
//				column.getCaption(), 
//				desiredHeight, 
//				currentHeight, 
//				delta, 
//				rc.getComputedHeightDelta()));
		
		if (rc.getComputedHeightDelta() == null) {
			//
			// If we want to shrink or grow the row and nothing else does (yet), then store the delta. 
			//
			rc.setComputedHeightDelta(delta);
		
		} else {
			//
			// If something else also wants to alter the height we must battle-it out.
			//
			if (rc.getComputedHeightDelta() >= 0) {
				if (delta > 0) {
					//
					// Something else also wants to grow the row, so grow it be the larger of
					// the two values - to ensure text is still visible.
					//									
					rc.setComputedHeightDelta(Math.max(rc.getComputedHeightDelta(), delta));
					
				} else {
					//
					// If something else wants to grow the row and we want to shrink it - 
					// do nothing.
					//
				}								
				
			} else {
				if (delta >= 0) {
					//
					// If something else wants to shrink the row, whilst we want to grow it, we win.
					//
					rc.setComputedHeightDelta(delta);
					
				} else {
					//
					// Something else also wants to shrink the row, so shrink it by the smaller of
					// the two values - to ensure text is still visible.
					//
					rc.setComputedHeightDelta(Math.max(rc.getComputedHeightDelta(), delta));
				}
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
	protected List<Image> getCellImages(final Column column, final Row<T> row) {
		imageCollector.clear();
		
		if (row == gridModel.getColumnHeaderRow()) {
			//
			// Get any image from the provider
			//			 
			grid.getLabelProvider().getHeaderImage(imageCollector, column);
			
			//
			// Return a sorted image if sorted.
			//
			if (column.getSortDirection() != SortDirection.NONE) {
				if (column.getSortDirection() == SortDirection.ASC){
					imageCollector.addImage(getImage("sort_ascending.png"));

				} else if (column.getSortDirection() == SortDirection.DESC){
					imageCollector.addImage(getImage("sort_descending.png"));
				}
			}

		} else if ((column == gridModel.getRowNumberColumn()) || (column == gridModel.getGroupSelectorColumn())) {
			 // Do nothing, they don't have images.
			 
		} else {
			//
			// Get any image from the provider
			//
			grid.getLabelProvider().getImage(imageCollector, column, row.getElement());
		}

		return imageCollector.getImages();
	}
	
	public Point getContentLocation() {
		return contentLocation;
	}
	
	public Rectangle getInnerBounds() {
		return innerBounds;
	}
}
