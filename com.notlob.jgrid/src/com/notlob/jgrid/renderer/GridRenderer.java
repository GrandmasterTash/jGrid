package com.notlob.jgrid.renderer;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.TextLayout;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.Grid.GroupRenderStyle;
import com.notlob.jgrid.Grid.SelectionStyle;
import com.notlob.jgrid.input.GridMouseHandler;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.SortDirection;
import com.notlob.jgrid.model.Viewport;
import com.notlob.jgrid.model.filtering.IHighlightingFilter;
import com.notlob.jgrid.styles.AlignmentStyle;
import com.notlob.jgrid.styles.BorderStyle;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.styles.CompositeCellStyle;
import com.notlob.jgrid.styles.ContentStyle;
import com.notlob.jgrid.styles.RegionStyle;
import com.notlob.jgrid.styles.StyleRegistry;

public class GridRenderer<T> implements PaintListener {

	protected final Grid<T> grid;
	protected final GridModel<T> gridModel;
	protected final Viewport<T> viewport;
	protected final StyleRegistry<T> styleRegistry;

	// These are used during a render pass. It saves creating lots of them on the fly - instead we'll recycle these instances.
	protected final Point rowLocation;
	protected final Point content;
	protected final Rectangle rowBounds;
	protected final Rectangle cellBounds;
	protected final Rectangle innerBounds;
	protected final Rectangle selectionRegion;
	protected final Rectangle hoverRegion;
	protected final Point topLeft;
	protected final Point topRight;
	protected final Point bottomLeft;
	protected final Point bottomRight;
	protected final Point fieldLocation;
	protected final Point groupBottomLeft;
	protected final Point groupBottomRight;
	protected final Rectangle groupFieldBounds;
	protected final TextLayout textLayout;

	// The current row index being painted (used for painting row numbers).
	protected int rowIndex;

	protected final static RGB RGB__SHADOW_DARK = new RGB(80, 80, 80);
	protected final static RGB RGB__SHADOW_HIGHLIGHT = new RGB(245, 245, 245);
	
	public final static int ROW_OFFSET = 1; // TODO: Consider using borderline width?

	protected enum RenderPass {
		BACKGROUND,
		FOREGROUND
	}

	// By painting in two passes we get to avoid issues with background fills leaving gaps where a cell has no border
	// or if there is a border, we avoid issues where the next cell's background overites it (with no spacing).
	protected RenderPass renderPass;

	// Are we painting an alternate background row?
	protected boolean alternate;

	// Cache the width of any text rendered by the font data.
	protected final Map<FontData, Map<String, Point>> extentCache;

	protected boolean errorLogged;
	protected Image errorImage;
	protected Image dropImage;
	protected Image columnDragImage;

	// Double-buffering image. Used as a key for the setData method.
	private final static String DATA__DOUBLE_BUFFER_IMAGE = "double-buffer-image"; //$NON-NLS-1$

	private final static int PADDING__EXPAND_COLLAPSE_IMAGE = 2;
	private final static int SPACING__GROUP_FIELD = 4;
	private final static int PADDING__GROUP_FIELD = 8;
	
	public GridRenderer(final Grid<T> grid) {
		this.grid = grid;
		gridModel = grid.getGridModel();
		styleRegistry = gridModel.getStyleRegistry();
		viewport = grid.getViewport();
		content = new Point(0, 0);
		rowLocation = new Point(0, 0);
		rowBounds = new Rectangle(0, 0, 0, 0);
		cellBounds = new Rectangle(0, 0, 0, 0);
		topLeft = new Point(0, 0);
		topRight = new Point(0, 0);
		bottomLeft = new Point(0, 0);
		bottomRight = new Point(0, 0);
		fieldLocation = new Point(0, 0);
		innerBounds = new Rectangle(0, 0, 0, 0);
		extentCache = new HashMap<>();
		selectionRegion = new Rectangle(0, 0, 0, 0);
		hoverRegion = new Rectangle(0, 0, 0, 0);
		groupBottomLeft = new Point(0, 0);
		groupBottomRight = new Point(0, 0);
		groupFieldBounds = new Rectangle(0, 0, 0, 0);
		textLayout = new TextLayout(grid.getDisplay());
		errorImage = getImage("cell_error.gif");
		dropImage = getImage("inwards_arrows.png");
	}

	@Override
	public void paintControl(final PaintEvent e) {
		GC gc = null;
		try {
			if (grid.getLabelProvider() == null) {
				throw new IllegalArgumentException("There's no IGridLabelProvider on the grid.");
			}
			
			if (grid.getContentProvider() == null) {
				throw new IllegalArgumentException("There's no IGridContentProvider on the grid.");
			}
			
			//
			// Double-buffer the paint event.
			//
			Image image = (Image) grid.getData(DATA__DOUBLE_BUFFER_IMAGE);
		    if ((image == null) || (image.getBounds().width != grid.getSize().x) || (image.getBounds().height != grid.getSize().y)) {
		    	//
		    	// If the old image no longer fits the bounds, trash it.
		    	//
		    	if (image != null) {
		    		image.dispose();
		    	}

		    	//
		    	// Store the double-buffer image in the data of the canvas.
		    	//
		    	image = new Image(grid.getDisplay(), grid.getSize().x, grid.getSize().y);
		    	grid.setData(DATA__DOUBLE_BUFFER_IMAGE, image);
		    }

		    gc = new GC(image);
			gc.setBackground(getColour(styleRegistry.getBackgroundColour()));
			gc.fillRectangle(grid.getClientArea());
			gc.setAntialias(SWT.ON);
			gc.setTextAntialias(SWT.ON);

			if (gridModel != null && !gridModel.getColumns().isEmpty()) {
				//
				// Calculate the viewport ranges.
				//
				viewport.calculateVisibleCellRange(gc);

				//
				// Paint the grid and cell backgrounds.
				//
				renderPass = RenderPass.BACKGROUND;
				paintRows(gc);
				paintSelection(gc);

				//
				// Paint the grid and cell foregrounds.
				//
				renderPass = RenderPass.FOREGROUND;
				paintRows(gc);
				paintSelection(gc);				
				
				//
				// Paint a drag image if we're dragging a column.
				//
				paintColumnDragImage(gc);
			} 
			
			if (gridModel.getRows().isEmpty()) {
				//
				// Paint the 'no data' message. Note the filter check has to compensate for the CollapseGroupFilter. Naff.
				//
				final String text = grid.getEmptyMessage() == null ? (grid.getFilters().size() > 1 ? getDefaultFiltersHiddenDataMessage() : getDefaultNoDataMessage()) : grid.getEmptyMessage();
				final CellStyle cellStyle = styleRegistry.getNoDataStyle();
				final Point point = getTextExtent(text, gc, cellStyle.getFontData());

				gc.setAlpha(cellStyle.getForegroundOpacity());
				gc.setFont(getFont(cellStyle.getFontData()));
				gc.setForeground(getColour(cellStyle.getForeground()));
				align(point.x, point.y, grid.getClientArea(), cellStyle.getTextAlignment(), cellStyle);				
				gc.drawText(text, content.x, content.y, SWT.DRAW_TRANSPARENT);
			}

			//
			// Paint the image to the real GC now.
			//
			e.gc.drawImage(image, 0, 0);

		} catch (final Throwable t) {
			if (!errorLogged) {
				System.err.println(String.format("Failed to paint control: %s", t.getMessage()));
				t.printStackTrace(System.err);
				errorLogged = true;
			}

		} finally {
			if (gc != null) {
				gc.dispose();
			}
		}
	}

	protected String getDefaultFiltersHiddenDataMessage() {
		return "No data matches your filter criteria.";
	}

	protected String getDefaultNoDataMessage() {
		return "There is no data to display.";
	}

	/**
	 * If there's a column being repositioned with the mouse, render a 'drag image' representing the column
	 * header at the mouse location.
	 */
	@SuppressWarnings("unchecked")
	protected void paintColumnDragImage(final GC gc) {
		final Column column = grid.getMouseHandler().getRepositioningColumn();
		
		if (column == null && columnDragImage != null) {
			//
			// If we're not dragging then dispose any previous image.
			//
			columnDragImage.dispose();
			columnDragImage = null;
			
		} else if (column != null) {
			if (columnDragImage == null) {				
				//
				// Create a column drag image.
				//
				final CellStyle cellStyle = styleRegistry.getCellStyle(column, Row.COLUMN_HEADER_ROW);
				final int height = getRowHeight(Row.COLUMN_HEADER_ROW);
				columnDragImage = new Image(grid.getDisplay(), column.getWidth(), height);
				final Rectangle dragImageBounds = new Rectangle(columnDragImage.getBounds().x, columnDragImage.getBounds().y, columnDragImage.getBounds().width - 1, columnDragImage.getBounds().height - 1);
				
				final GC imageGC = new GC(columnDragImage);
				paintCellBackground(imageGC, dragImageBounds, cellStyle);
				paintCellBorders(imageGC, dragImageBounds, cellStyle);
				paintCellContent(imageGC, dragImageBounds, column, Row.COLUMN_HEADER_ROW, cellStyle);
				imageGC.dispose();
			}
			
			//
			// Render the column drag image at the mouses location.
			//
			final Point mouseLocation = grid.toControl(new Point(
					grid.getDisplay().getCursorLocation().x - (columnDragImage.getBounds().width / 2), 
					grid.getDisplay().getCursorLocation().y));

			gc.setAlpha(220);
			gc.drawImage(columnDragImage, mouseLocation.x, mouseLocation.y);
		}
	}
	
	protected void paintSelection(final GC gc) {
		if (grid.getSelectionStyle() == SelectionStyle.COLUMN_BASED) {
			paintColumnBasedSelection(gc);
		} else {
			paintRowBasedSelection(gc);
		}
	}
	
	/**
	 * Paint a selection region in the column containing the anchor.
	 */
	protected void paintColumnBasedSelection(final GC gc) {
		final Rectangle viewportArea = viewport.getViewportArea(gc);
		final Column column = grid.getAnchorColumn();
		
		if (column != null) {						
			selectionRegion.x = viewport.getColumnViewportX(gc, column);
			selectionRegion.y = viewportArea.x;
			selectionRegion.width = column.getWidth();
			selectionRegion.height= viewportArea.height;
			paintSelectionRegion(gc, selectionRegion, (viewport.getFirstRowIndex() == 0), true, true, true, styleRegistry.getSelectionRegionStyle());
			
		} else {
			selectionRegion.x = -1;
			selectionRegion.y = -1;
			selectionRegion.width = -1;
			selectionRegion.height= -1;
		}		
	}

	/**
	 * Paint the selection region's background OR the selection region's borders.
	 */
	protected void paintRowBasedSelection(final GC gc) {
		final Rectangle viewportArea = viewport.getViewportArea(gc);
		final boolean paintLeftEdge = (viewport.getFirstColumnIndex() == 0);
		final boolean paintRightEdge = viewport.getVisibleRowWidth(gc) < viewportArea.width;
		boolean paintTopEdge = false;
		boolean paintBottomEdge = false;
		boolean inSelection = false;

		rowLocation.x = viewportArea.x;
		rowLocation.y = viewportArea.y;
		selectionRegion.x = -1;
		selectionRegion.y = -1;
		selectionRegion.width = viewport.getVisibleRowWidth(gc);
		selectionRegion.height= -1;

		//
		// Paint selected row regions - a region is a contiguous block of selected rows.
		//
		for (int rowIndex=viewport.getFirstRowIndex(); rowIndex<viewport.getLastVisibleRowIndex(); rowIndex++) {
			final Row<T> row = gridModel.getRows().get(rowIndex);

			if (row.isSelected()) {
				if (inSelection) {
					//
					// Expand the selction region.
					//
					selectionRegion.height += getRowHeight(row);

				} else {
					//
					// Start a new selection region.
					//
					selectionRegion.x = rowLocation.x;
					selectionRegion.y = rowLocation.y;
					selectionRegion.height = getRowHeight(row);

					//
					// If the previous row is selected then do paint a top edge, as the selection region
					// begins above the viewport.
					//
					paintTopEdge = !((rowIndex > 0) && gridModel.getRows().get(rowIndex - 1).isSelected());
				}

			} else if (inSelection) {
				//
				// This is the next row after a selection region. We now need to paint the region.
				//
				paintSelectionRegion(gc, selectionRegion, paintTopEdge, paintRightEdge, true, paintLeftEdge, styleRegistry.getSelectionRegionStyle());

			}

			if (grid.isHighlightHoveredRow() && !row.isSelected() && (row == grid.getMouseHandler().getRow())) {
				//
				// The row has the mouse hovering over it, so paint it with the hover style. If the mouse is over a group field name though, don't highlight.
				//
				hoverRegion.x = rowLocation.x;
				hoverRegion.y = rowLocation.y;
				hoverRegion.height = getRowHeight(row);
				hoverRegion.width = selectionRegion.width;
				paintSelectionRegion(gc, hoverRegion, !inSelection, paintRightEdge, true, paintLeftEdge, styleRegistry.getHoverRegionStyle());
			}

			inSelection = row.isSelected();
			rowLocation.y += (getRowHeight(row) + styleRegistry.getCellSpacingVertical());
		}

		//
		// We'll need to paint the last selection region.
		//
		if (inSelection) {
			//
			// If the next row beyond the viewport exists and is selected, don't draw the bottom
			//
			paintBottomEdge = !(((viewport.getLastRowIndex() + 1) < gridModel.getRows().size()) && (gridModel.getRows().get(viewport.getLastRowIndex() + 1).isSelected()));
			paintSelectionRegion(gc, selectionRegion, paintTopEdge, paintRightEdge, paintBottomEdge, paintLeftEdge, styleRegistry.getSelectionRegionStyle());
		}
	}

	/**
	 * Paint foreground or background details for the region of selected rows.
	 */
	protected void paintSelectionRegion(final GC gc, final Rectangle bounds, final boolean paintTop, final boolean paintRight, final boolean paintBottom, final boolean paintLeft, final RegionStyle regionStyle) {
		if (renderPass == RenderPass.BACKGROUND) {
			gc.setAlpha(regionStyle.getBackgroundOpacity());

			if (regionStyle.getBackgroundGradient1() == null || regionStyle.getBackgroundGradient2() == null) {
				//
				// Fill with no Gradient
				//
				gc.setBackground(getColour(regionStyle.getBackground()));
				gc.fillRectangle(bounds);

			} else {
				//
				// Fill with Gradient (upper, lower).
				//
				gc.setForeground(getColour(regionStyle.getBackgroundGradient1()));
				gc.setBackground(getColour(regionStyle.getBackgroundGradient2()));
				gc.fillGradientRectangle(bounds.x, bounds.y, bounds.width, bounds.height, true);
			}
		}

		if (renderPass == RenderPass.FOREGROUND) {
			//
			// Paint a region border.
			//
			gc.setAlpha(regionStyle.getForegroundOpacity());
			gc.setForeground(getColour(regionStyle.getBorder().getColour()));
			gc.setLineWidth(regionStyle.getBorder().getWidth());

			// Get the bounds corners, but correct for thicker line widths.
			getTopLeft(bounds);
			getTopRight(bounds);
			getBottomRight(bounds);
			getBottomLeft(bounds);
			topLeft.x += (gc.getLineWidth() - 1);
			bottomLeft.x += (gc.getLineWidth() - 1);

			if (paintTop) {
				paintBorderLine(gc, regionStyle.getBorder(), topLeft, topRight);
			}

			if (paintRight) {
				paintBorderLine(gc, regionStyle.getBorder(), topRight, bottomRight);
			}

			if (paintBottom) {
				paintBorderLine(gc, regionStyle.getBorder(), bottomRight, bottomLeft);
			}

			if (paintLeft) {
				paintBorderLine(gc, regionStyle.getBorder(), bottomLeft, topLeft);
			}
		}
	}

	/**
	 * Iterate over header then body rows and render.
	 */
	protected void paintRows(final GC gc) {
		final Rectangle viewportArea = viewport.getViewportArea(gc);

		//
		// Paint the corner cell.
		//
		if (grid.isShowRowNumbers()) {
			cellBounds.x = 0;
			cellBounds.y = 0;
			cellBounds.height = viewportArea.y;
			cellBounds.width = viewportArea.x;

			if (renderPass == RenderPass.BACKGROUND) {
				paintCellBackground(gc, cellBounds, styleRegistry.getCornerStyle());

			} else if (renderPass == RenderPass.FOREGROUND) {
				paintCellBorders(gc, cellBounds, styleRegistry.getCornerStyle());
			}
		}

		//
		// Paint column header row(s).
		//
		rowLocation.x = viewportArea.x;
		rowLocation.y = 0;
		alternate = false;
		for (final Row<T> row : gridModel.getColumnHeaderRows()) {
			paintRow(gc, rowLocation, row);
			rowLocation.y += (getRowHeight(row) + styleRegistry.getCellSpacingVertical());
		}

		//
		// Paint data rows and row numbers.
		//
		alternate = false;
		rowLocation.x = viewportArea.x;
		rowLocation.y = viewportArea.y;

		for (int rowIndex=viewport.getFirstRowIndex(); rowIndex<viewport.getLastVisibleRowIndex(); rowIndex++) {
			final Row<T> row = gridModel.getRows().get(rowIndex);

			if (grid.isShowRowNumbers()) {
				paintRowNumber(gc, rowLocation, row, rowIndex);
			}

			if (gridModel.isParentRow(row)) {
				paintGroupRow(gc, rowLocation, row);
			} else {
				paintRow(gc, rowLocation, row);
			}

			rowLocation.y += (getRowHeight(row) + styleRegistry.getCellSpacingVertical());

			//
			// If there's a next row, and it's in the same group, don't flip the alternate background.
			//
			final int nextIndex = rowIndex + 1;
			if (!((nextIndex < viewport.getLastRowIndex()) && (nextIndex < gridModel.getRows().size()) && (gridModel.isSameGroup(row, gridModel.getRows().get(nextIndex))))) {
				alternate = !alternate;
			}
		}
	}

	protected void paintRowNumber(final GC gc, final Point point, final Row<T> row, final int rowIndex) {
		this.rowIndex = rowIndex;
		cellBounds.x = 0;
		cellBounds.y = point.y;
		cellBounds.height = getRowHeight(row);
		cellBounds.width = point.x;
		paintCell(gc, cellBounds, null, null, (grid.isFocusControl() && grid.isHighlightAnchorInHeaders() && doesRowHaveAnchor(row)) ? styleRegistry.getSelectionRowNumberStyle() : styleRegistry.getRowNumberStyle());
	}

	protected void paintGroupRow(final GC gc, final Point point, final Row<T> row) {
		if (grid.getGroupRenderStyle() == GroupRenderStyle.COLUMN_BASED) {
			//
			// Just paint the group row like any normal row.
			//
			paintRow(gc, point, row);
			
		} else {
			//
			// Paint the group row, by using the groupBy columns from left-to-right.
			//
			final CellStyle groupValueStyle = styleRegistry.getGroupValueStyle();
	
			rowBounds.x = point.x;
			rowBounds.y = point.y;
			rowBounds.width = viewport.getVisibleRowWidth(gc); //(viewport.getViewportArea(gc).width);
			rowBounds.height = getRowHeight(row);
	
			final Rectangle oldClipping = gc.getClipping();
			gc.setClipping(rowBounds);
	
			//
			// Paint the row background.
			//
			if ((row.getElement() != null) && (renderPass == RenderPass.BACKGROUND)) {
				gc.setBackground(getColour(alternate ? groupValueStyle.getBackgroundAlternate() : groupValueStyle.getBackground()));
				gc.fillRectangle(rowBounds);
			}
	
			//
			// Paint the expand/collapse icon.
			//
			final Image expandImage = grid.getContentProvider().isCollapsed(row.getElement()) ? getImage("plus.png") : getImage("minus.png");
			gc.drawImage(expandImage, rowBounds.x + groupValueStyle.getPaddingLeft(), rowBounds.y + groupValueStyle.getPaddingTop() + PADDING__EXPAND_COLLAPSE_IMAGE);
	
			//
			// Paint the grouped values.
			//
			if ((row.getElement() != null) && (renderPass == RenderPass.FOREGROUND)) {
				final CellStyle groupNameStyle = styleRegistry.getGroupNameStyle();
				fieldLocation.x = PADDING__EXPAND_COLLAPSE_IMAGE + groupValueStyle.getPaddingLeft() + expandImage.getBounds().width + rowBounds.x + groupValueStyle.getPaddingLeft();
				fieldLocation.y = rowBounds.y + groupValueStyle.getPaddingTop();
	
				for (final Column column : gridModel.getGroupByColumns()) {
					final CellStyle valueStyle = styleRegistry.getCellStyle(column, row);
					paintGroupCellContent(gc, column, row, groupNameStyle, valueStyle);
				}
			}
			
			gc.setClipping(oldClipping);
	
			//
			// Paint any footer border.
			//
			if ((row.getElement() != null) && (renderPass == RenderPass.FOREGROUND)) {
				if (styleRegistry.getGroupFooterBorderTop() != null) {
					//
					// Paint a border along the top of the group row.
					//
					groupBottomLeft.x = rowBounds.x;
					groupBottomLeft.y = rowBounds.y;
					groupBottomRight.x = rowBounds.x + rowBounds.width;
					groupBottomRight.y = groupBottomLeft.y;
					paintBorderLine(gc, styleRegistry.getGroupFooterBorderTop(), groupBottomLeft, groupBottomRight);
				}
				
				if (styleRegistry.getGroupFooterBorderBottom() != null) {
					//
					// Paint a border along the bottom of the group row.
					//
					groupBottomLeft.x = rowBounds.x;
					groupBottomLeft.y = rowBounds.y + rowBounds.height;
					groupBottomRight.x = rowBounds.x + rowBounds.width;
					groupBottomRight.y = groupBottomLeft.y;
					paintBorderLine(gc, styleRegistry.getGroupFooterBorderBottom(), groupBottomLeft, groupBottomRight);
				}
			}
		}
	}

	
	// TODO: These three methods need a single place where the widths/iterations are done: -
	//    paintGroupCellContent
	//    getGroupColumnX
	//    getGroupColumnForX
	
	protected void paintGroupCellContent(final GC gc, final Column column, final Row<T> row, final CellStyle groupNameStyle, final CellStyle groupValueStyle) {
		final String name = column.getCaption();
		final String providedValue = grid.getLabelProvider().getText(column, row.getElement());
		final String value = providedValue == null || providedValue.isEmpty() ? "(blank)" : providedValue;

		//
		// Highlight the field name if its field has the anchor.
		//
		// TODO: Use the selection header background?
		final boolean hasAnchor = (grid.isFocusControl() && (column == grid.getAnchorColumn()) && (row.getElement() == grid.getAnchorElement()));
		if (hasAnchor && grid.isHighlightAnchorInHeaders()) {
			gc.setForeground(getColour(styleRegistry.getHoverGroupNameForeground()));
			gc.setBackground(getColour(styleRegistry.getHoverGroupNameBackground()));
		} else {
			gc.setForeground(getColour(groupNameStyle.getForeground()));
		}

		//
		// Sort icon.
		//
		final Image sortImage = getImage((column.getSortDirection() == SortDirection.ASC ? "sort_ascending.png" : "sort_descending.png"));
		if (column.getSortDirection() != SortDirection.NONE) {
			gc.drawImage(sortImage, fieldLocation.x, fieldLocation.y);
		}
		fieldLocation.x += sortImage.getBounds().width + SPACING__GROUP_FIELD;

		//
		// Field name text.
		//
		gc.setFont(getFont(groupNameStyle.getFontData()));
		gc.drawText(name, fieldLocation.x, fieldLocation.y, (!hasAnchor || !grid.isHighlightAnchorInHeaders()));
		fieldLocation.x += getTextExtent(name, gc, groupNameStyle.getFontData()).x + SPACING__GROUP_FIELD;

		final boolean filterMatch = hasStyleableFilterMatch(row, column);
		if (filterMatch) {
			//
			// Use text highlighting if there's a FilterMatchRange in this column (and it's trackable).
			//
			gc.setBackground(getColour(styleRegistry.getFilterMatchBackground()));
			gc.setForeground(getColour(styleRegistry.getFilterMatchForeground()));

		} else {
			//
			// Use normal colours if we're not highlighting a filter result.
			//
			gc.setForeground(getColour(groupValueStyle.getForeground()));
		}

		groupFieldBounds.x = fieldLocation.x;
		groupFieldBounds.y = fieldLocation.y;
		groupFieldBounds.width = 0;
		groupFieldBounds.height = 0;

		//
		// Field value image.
		//
		if ((groupValueStyle.getContentStyle() == ContentStyle.IMAGE_THEN_TEXT) || (groupValueStyle.getContentStyle() == ContentStyle.IMAGE)) {
			final Image image = grid.getLabelProvider().getImage(column, row.getElement());
			if (image != null) {
				gc.drawImage(image, fieldLocation.x, fieldLocation.y);
				fieldLocation.x += image.getBounds().width + PADDING__GROUP_FIELD;
				groupFieldBounds.width += (image.getBounds().width + PADDING__GROUP_FIELD);
				groupFieldBounds.height += image.getBounds().height;
			}
		}

		//
		// Field value text.
		//
		switch (groupValueStyle.getContentStyle()) {
			case IMAGE_THEN_TEXT:
			case TEXT:
			case TEXT_THEN_IMAGE:
				final Point valueExtent = getTextExtent(value, gc, groupValueStyle.getFontData());
				gc.setFont(getFont(groupValueStyle.getFontData()));
				gc.drawText(value, fieldLocation.x, fieldLocation.y, true);
				fieldLocation.x += (valueExtent.x + PADDING__GROUP_FIELD);
				groupFieldBounds.width += (valueExtent.x + PADDING__GROUP_FIELD);
				groupFieldBounds.height += valueExtent.y;

			default:
				// No-op.
		}

		//
		// Field value image.
		//
		if (groupValueStyle.getContentStyle() == ContentStyle.TEXT_THEN_IMAGE) {
			final Image image = grid.getLabelProvider().getImage(column, row.getElement());
			if (image != null) {
				gc.drawImage(image, fieldLocation.x, fieldLocation.y);
				fieldLocation.x += image.getBounds().width + PADDING__GROUP_FIELD;
				groupFieldBounds.width += (image.getBounds().width + PADDING__GROUP_FIELD);
				groupFieldBounds.height += image.getBounds().height;
			}
		}
		
		//
		// Paint the anchor border.
		//
		if (hasAnchor) {
			paintBorderLine(gc, styleRegistry.getAnchorStyle().getBorderInnerTop(), getTopLeft(groupFieldBounds), getTopRight(groupFieldBounds));
			paintBorderLine(gc, styleRegistry.getAnchorStyle().getBorderInnerBottom(), getBottomLeft(groupFieldBounds), getBottomRight(groupFieldBounds));
			paintBorderLine(gc, styleRegistry.getAnchorStyle().getBorderInnerLeft(), getTopLeft(groupFieldBounds), getBottomLeft(groupFieldBounds));
			paintBorderLine(gc, styleRegistry.getAnchorStyle().getBorderInnerRight(), getTopRight(groupFieldBounds), getBottomRight(groupFieldBounds));
		}
	}
	
	public int getGroupColumnX(final GC gc, final Column findColumn, final Row<T> row) {
		final CellStyle groupNameStyle = styleRegistry.getGroupNameStyle();
		final CellStyle groupValueStyle = styleRegistry.getGroupValueStyle();

		final Image expandImage = grid.getContentProvider().isCollapsed(row.getElement()) ? getImage("plus.png") : getImage("minus.png");
		int fieldLocationX = PADDING__EXPAND_COLLAPSE_IMAGE + groupValueStyle.getPaddingLeft() + expandImage.getBounds().width + viewport.getViewportArea(gc).x + groupValueStyle.getPaddingLeft();

		for (final Column column : gridModel.getGroupByColumns()) {
			if (column == findColumn) {
				return fieldLocationX;
			}
			
			final CellStyle valueStyle = styleRegistry.getCellStyle(column, row);
			final String name = column.getCaption();
			final String providedValue = grid.getLabelProvider().getText(column, row.getElement());
			final String value = providedValue == null || providedValue.isEmpty() ? "(blank)" : providedValue;
						
			//
			// Sort icon.
			//
			final Image sortImage = getImage("sort_ascending.png");
			fieldLocationX += sortImage.getBounds().width + SPACING__GROUP_FIELD;

			//
			// Field Name.
			//
			gc.setFont(getFont(groupNameStyle.getFontData()));
			final Point nameExtent = getTextExtent(name, gc, groupNameStyle.getFontData());
			fieldLocationX += nameExtent.x + SPACING__GROUP_FIELD;

			//
			// Field value image.
			//
			if ((valueStyle.getContentStyle() == ContentStyle.IMAGE_THEN_TEXT) || (valueStyle.getContentStyle() == ContentStyle.IMAGE)) {
				final Image image = grid.getLabelProvider().getImage(column, row.getElement());
				if (image != null) {
					fieldLocationX += image.getBounds().width + PADDING__GROUP_FIELD;
				}
			}

			//
			// Field Value.
			//
			switch (valueStyle.getContentStyle()) {
				case IMAGE_THEN_TEXT:
				case TEXT:
				case TEXT_THEN_IMAGE:
					gc.setFont(getFont(valueStyle.getFontData()));
					final Point valueExtent = getTextExtent(value, gc, valueStyle.getFontData());
					fieldLocationX += (valueExtent.x + PADDING__GROUP_FIELD);
				default:
					// No-op
			}
			
			//
			// Field value image.
			//
			if (valueStyle.getContentStyle() == ContentStyle.TEXT_THEN_IMAGE) {
				final Image image = grid.getLabelProvider().getImage(column, row.getElement());
				if (image != null) {
					fieldLocationX += image.getBounds().width + PADDING__GROUP_FIELD;
				}
			}
		}
		
		return -1;
	}

	public Column getGroupColumnForX(final GC gc, final Row<T> row, final int x, final boolean header) {
		final CellStyle groupNameStyle = styleRegistry.getGroupNameStyle();
		final CellStyle groupValueStyle = styleRegistry.getGroupValueStyle();

		final Image expandImage = grid.getContentProvider().isCollapsed(row.getElement()) ? getImage("plus.png") : getImage("minus.png");
		int fieldLocationX = PADDING__EXPAND_COLLAPSE_IMAGE + groupValueStyle.getPaddingLeft() + expandImage.getBounds().width + viewport.getViewportArea(gc).x + groupValueStyle.getPaddingLeft();

		for (final Column column : gridModel.getGroupByColumns()) {
			final CellStyle valueStyle = styleRegistry.getCellStyle(column, row);
			final String name = column.getCaption();
			final String providedValue = grid.getLabelProvider().getText(column, row.getElement());
			final String value = providedValue == null || providedValue.isEmpty() ? "(blank)" : providedValue;
			final int fieldLeftX = fieldLocationX;
			
			//
			// Sort icon.
			//
			final Image sortImage = getImage("sort_ascending.png");
			fieldLocationX += sortImage.getBounds().width + SPACING__GROUP_FIELD;

			//
			// Field Name.
			//
			gc.setFont(getFont(groupNameStyle.getFontData()));
			final Point nameExtent = getTextExtent(name, gc, groupNameStyle.getFontData());

			if (header && (x >= fieldLeftX) && (x < (fieldLocationX + nameExtent.x))) {
				return column;
			}

			fieldLocationX += nameExtent.x + SPACING__GROUP_FIELD;

			//
			// Field value image.
			//
			if ((valueStyle.getContentStyle() == ContentStyle.IMAGE_THEN_TEXT) || (valueStyle.getContentStyle() == ContentStyle.IMAGE)) {
				final Image image = grid.getLabelProvider().getImage(column, row.getElement());
				if (image != null) {
					if (!header && (x >= fieldLocationX) && (x < (fieldLocationX + image.getBounds().width))) {
						return column;
					}

					fieldLocationX += image.getBounds().width + PADDING__GROUP_FIELD;
				}
			}

			//
			// Field Value.
			//
			switch (valueStyle.getContentStyle()) {
				case IMAGE_THEN_TEXT:
				case TEXT:
				case TEXT_THEN_IMAGE:
					gc.setFont(getFont(valueStyle.getFontData()));
					final Point valueExtent = getTextExtent(value, gc, valueStyle.getFontData());

					if (!header && (x >= fieldLocationX) && (x < (fieldLocationX + valueExtent.x))) {
						return column;
					}

					fieldLocationX += (valueExtent.x + PADDING__GROUP_FIELD);
				default:
					// No-op
			}
			
			//
			// Field value image.
			//
			if (valueStyle.getContentStyle() == ContentStyle.TEXT_THEN_IMAGE) {
				final Image image = grid.getLabelProvider().getImage(column, row.getElement());
				if (image != null) {
					if (!header && (x >= fieldLocationX) && (x < (fieldLocationX + image.getBounds().width))) {
						return column;
					}
					
					fieldLocationX += image.getBounds().width + PADDING__GROUP_FIELD;
				}
			}
		}

		//
		// Assume they've clicked to the right of the last field value.
		//
		if (!header && !gridModel.getGroupByColumns().isEmpty() && (x >= fieldLocationX)) {
			return gridModel.getGroupByColumns().get(gridModel.getGroupByColumns().size()-1);
		}
		
		return null;
	}
	
	public Rectangle getExpandImageBounds(final GC gc, final Row<T> row) {
		//
		// Get the y for the row from the viewport.
		//
		final int rowY = viewport.getRowViewportY(gc, row);
		final Image image = grid.getContentProvider().isCollapsed(row.getElement()) ? getImage("plus.png") : getImage("minus.png");
		final CellStyle groupValueStyle = styleRegistry.getGroupValueStyle();
		final Rectangle bounds = new Rectangle(viewport.getViewportArea(gc).x + groupValueStyle.getPaddingLeft() - PADDING__EXPAND_COLLAPSE_IMAGE, rowY + groupValueStyle.getPaddingTop(), image.getBounds().width + (PADDING__EXPAND_COLLAPSE_IMAGE * 2), image.getBounds().height + (PADDING__EXPAND_COLLAPSE_IMAGE * 2));
		return bounds;
	}

	protected void paintRow(final GC gc, final Point point, final Row<T> row) {
		try {
			rowBounds.x = point.x + ROW_OFFSET; // Shift 1 to avoid blatting the row number border line.
			rowBounds.y = point.y + ROW_OFFSET;
			rowBounds.width = viewport.getVisibleRowWidth(gc);// (viewport.getViewportArea(gc).width);
			rowBounds.height = getRowHeight(row);
	
			cellBounds.x = point.x;
			cellBounds.y = point.y;
			cellBounds.height = getRowHeight(row);
	
			//
			// Fill the row background (not the header row though).
			//
			if ((row != Row.COLUMN_HEADER_ROW) && (renderPass == RenderPass.BACKGROUND)) {
				final CellStyle rowStyle = styleRegistry.getCellStyle(null, row);
				gc.setBackground(getColour(alternate ? rowStyle.getBackgroundAlternate() : rowStyle.getBackground()));
				gc.fillRectangle(rowBounds);
			}
	
			//
			// Now paint every cell in the row.
			//
			for (int columnIndex=viewport.getFirstColumnIndex(); columnIndex<viewport.getLastVisibleColumnIndex(); columnIndex++) {
				final Column column = gridModel.getColumns().get(columnIndex);
				final CellStyle cellStyle = styleRegistry.getCellStyle(column, row);
	
				cellBounds.width = column.getWidth();
				paintCell(gc, cellBounds, column, row, cellStyle);
				cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
			}
	
			//
			// Render a column-reposition indicator if we're dragging columns around.
			//
			if ((row == Row.COLUMN_HEADER_ROW) && (renderPass == RenderPass.FOREGROUND) && (grid.getMouseHandler().getTargetColumn() != null)) {
				cellBounds.x = point.x;
				
				if (grid.getMouseHandler().getTargetColumn() == GridMouseHandler.LAST_COLUMN) {
					//
					// Edge-case, dragging to the end of the grid.
					//
					gc.drawImage(dropImage, rowBounds.x + rowBounds.width - (dropImage.getBounds().width / 2) + 1, 4);
					
				} else {
					//
					// Otherwise, move across the viewport until we get to the drag target column.
					//
					for (int columnIndex=viewport.getFirstColumnIndex(); columnIndex<viewport.getLastVisibleColumnIndex(); columnIndex++) {
						final Column column = gridModel.getColumns().get(columnIndex);
						if (column == grid.getMouseHandler().getTargetColumn()){ 
							gc.drawImage(dropImage, cellBounds.x - (dropImage.getBounds().width / 2) + 1, 4);					
						}
						cellBounds.width = column.getWidth();
						cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
					}
				}
			}
			
		} catch (Throwable t) {
			if (!errorLogged) {
				System.err.println(String.format("Failed to paint row: %s", t.getMessage()));
				t.printStackTrace(System.err);
				errorLogged = true;
			}
		}
	}

	protected void paintCell(final GC gc, final Rectangle bounds, final Column column, final Row<T> row, final CellStyle cellStyle) {
		try {
			CellStyle currentStyle = cellStyle;
			
			//
			// If the cell has the anchor, use a composite style.
			//
			if (grid.isFocusControl() && grid.isHighlightAnchorCellBorder() && doesColumnHaveAnchor(column) && doesRowHaveAnchor(row)) {
				final CompositeCellStyle compositeStyle = new CompositeCellStyle();
				compositeStyle.add(styleRegistry.getAnchorStyle());
				compositeStyle.add(cellStyle);
				currentStyle = compositeStyle;
			}			
			
			//
			// Paint the cell background.
			//
			if (renderPass == RenderPass.BACKGROUND) {
				paintCellBackground(gc, bounds, currentStyle);
			}

			//
			// Paint cell content.
			//
			if (renderPass == RenderPass.FOREGROUND) {
				paintCellContent(gc, bounds, column, row, currentStyle);
				paintCellBorders(gc, bounds, currentStyle);
			}

		} catch (final Throwable t) {
			if (!errorLogged) {
				System.err.println(String.format("Failed to paint: %s", t.getMessage()));
				t.printStackTrace(System.err);
				errorLogged = true;
			}

			//
			// Render a failure.
			//
			final RGB background = (alternate && (cellStyle.getBackgroundAlternate() != null)) ? cellStyle.getBackgroundAlternate() : cellStyle.getBackground();
			gc.setForeground(getColour(cellStyle.getForeground()));
			gc.setBackground(getColour(background));
			gc.drawImage(errorImage, bounds.x + 2, bounds.y + 2);
			gc.drawText("ERROR", bounds.x + 2 + errorImage.getBounds().width, bounds.y + 2);
		}
	}
	
	/**
	 * Paint the outer then inner borders of the cell.
	 */
	protected void paintCellBorders(final GC gc, final Rectangle bounds, final CellStyle cellStyle) {
		//
		// Render outer border.
		//
		gc.setAlpha(cellStyle.getForegroundOpacity());
		paintBorderLine(gc, cellStyle.getBorderOuterTop(), getTopLeft(bounds), getTopRight(bounds));
		paintBorderLine(gc, cellStyle.getBorderOuterRight(), getTopRight(bounds), getBottomRight(bounds));
		paintBorderLine(gc, cellStyle.getBorderOuterBottom(), getBottomLeft(bounds), getBottomRight(bounds));
		paintBorderLine(gc, cellStyle.getBorderOuterLeft(), getTopLeft(bounds), getBottomLeft(bounds));

		//
		// Render inner border.
		//
		setInnerBounds(bounds, cellStyle.getPaddingInnerBorder());
		paintBorderLine(gc, cellStyle.getBorderInnerTop(), getTopLeft(innerBounds), getTopRight(innerBounds));
		paintBorderLine(gc, cellStyle.getBorderInnerRight(), getTopRight(innerBounds), getBottomRight(innerBounds));
		paintBorderLine(gc, cellStyle.getBorderInnerBottom(), getBottomLeft(innerBounds), getBottomRight(innerBounds));
		paintBorderLine(gc, cellStyle.getBorderInnerLeft(), getTopLeft(innerBounds), getBottomLeft(innerBounds));
	}

	/**
	 * Paint cell image and text.
	 */
	protected void paintCellContent(final GC gc, final Rectangle bounds, final Column column, final Row<T> row, final CellStyle cellStyle) {
		gc.setAlpha(cellStyle.getForegroundOpacity());
		gc.setFont(getFont(cellStyle.getFontData()));

		final boolean filterMatch = hasStyleableFilterMatch(row, column);
		if (filterMatch) {
			//
			// Use text highlighting if there's a FilterMatchRange in this column (and it's trackable).
			//
			gc.setBackground(getColour(styleRegistry.getFilterMatchBackground()));
			gc.setForeground(getColour(styleRegistry.getFilterMatchForeground()));

		} else {
			//
			// Use normal colours if we're not highlighting a filter result.
			//
			gc.setForeground(getColour(cellStyle.getForeground()));
		}

		//
		// We'll use inner bounds to indicate where the next piece of content will be allowed. Initially, it's the full
		// cell bounds, then, after the first image or text is drawn, the inner bounds will shrink/move to ensure the
		// next image/text content can't overwrite it.
		//
		// Note: This is only used if content overlap is off.
		//
		innerBounds.x = bounds.x + cellStyle.getPaddingLeft();
		innerBounds.y = bounds.y + cellStyle.getPaddingTop();
		innerBounds.width = bounds.width - cellStyle.getPaddingLeft() - cellStyle.getPaddingRight();
		innerBounds.height = bounds.height - cellStyle.getPaddingTop() - cellStyle.getPaddingBottom();

		final Rectangle oldClipping = gc.getClipping();
		gc.setClipping(innerBounds);

		//
		// Render cell image BEFORE text..
		//
		if ((row != null) && (cellStyle.getContentStyle() == ContentStyle.IMAGE || cellStyle.getContentStyle() == ContentStyle.IMAGE_THEN_TEXT)) {
			final Image image = getCellImage(column, row);
			final AlignmentStyle imageAlignment = (cellStyle.getImageAlignment() != null) ? cellStyle.getImageAlignment() : (column.getImageAlignment() != null ? column.getImageAlignment() : AlignmentStyle.LEFT_CENTER);

			if (image != null) {
				align(image.getBounds().width, image.getBounds().height, innerBounds, imageAlignment, cellStyle);
				gc.drawImage(image, content.x, content.y);

				if (!cellStyle.isAllowContentOverlap()) {
					innerBounds.x += (image.getBounds().width + cellStyle.getPaddingImageText());
					innerBounds.width -= (image.getBounds().width + cellStyle.getPaddingImageText());
				}
			}
		}

		//
		// Render cell text.
		//
		if (cellStyle.getContentStyle() != ContentStyle.IMAGE) {
			final String text = getCellText(column, row);

			//
			// Ensure any image in the header row that follows text, doesn't have text running through it.
			//
			int widthCap = 0;
			if (row == Row.COLUMN_HEADER_ROW && (cellStyle.getContentStyle() == ContentStyle.TEXT_THEN_IMAGE)) {
				final Image image = getCellImage(column, row);
				if (image != null) {
					widthCap = image.getBounds().width + cellStyle.getPaddingImageText();
					innerBounds.width -= widthCap;
					gc.setClipping(innerBounds);
				}
			}

			final Point point = getTextExtent(text, gc, cellStyle.getFontData());
			final int width = Math.min(point.x, (innerBounds.width - widthCap));
			final int height = Math.min(point.y, innerBounds.height);
			final AlignmentStyle textAlignment = (cellStyle.getTextAlignment() == null) ? column.getTextAlignment() : cellStyle.getTextAlignment();

			align(width, height, innerBounds, textAlignment, cellStyle);

			if (filterMatch) {
				gc.drawText(text, content.x, content.y);
			} else {
				gc.drawText(text, content.x, content.y, SWT.DRAW_TRANSPARENT);
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

		//
		// Render cell image AFTER text..
		//
		if ((row != null) && (cellStyle.getContentStyle() == ContentStyle.TEXT_THEN_IMAGE)) {
			final Image image = getCellImage(column, row);
			final AlignmentStyle imageAlignment = (cellStyle.getImageAlignment() == null) ? column.getImageAlignment() : cellStyle.getImageAlignment();

			if (image != null) {
				align(image.getBounds().width, image.getBounds().height, innerBounds, imageAlignment, cellStyle);
				gc.drawImage(image, content.x, content.y);

				if (!cellStyle.isAllowContentOverlap()) {
					innerBounds.x += (image.getBounds().width + cellStyle.getPaddingImageText());
					innerBounds.width -= (image.getBounds().width + cellStyle.getPaddingImageText());
				}
			}
		}

		gc.setClipping(oldClipping);
	}

	/**
	 * Does this cell have a filter match we need to highlight?
	 */
	protected boolean hasStyleableFilterMatch(final Row<T> row, final Column column) {
		if (row != null && row.hasFilterMatches()) {
			for (final IHighlightingFilter filterMatch : row.getFilterMatches()) {
				if (filterMatch.isColumnHighlighted(column)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Gets the text for the cell from the label provider if required.
	 */
	protected String getCellText(final Column column, final Row<T> row) {
		if (row == null || column == Column.ROW_NUMBER_COLUMN) {
			return String.valueOf(rowIndex + 1);

		} else if (row == Row.COLUMN_HEADER_ROW) {
			return column.getCaption();

		} else {
			return grid.getLabelProvider().getText(column, row.getElement());
		}
	}

	/**
	 * Return the image for the given cell.
	 */
	protected Image getCellImage(final Column column, final Row<T> row) {
		 if (row == Row.COLUMN_HEADER_ROW) {
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

		} else {
			//
			// Get any image from the provider
			//
			return grid.getLabelProvider().getImage(column, row.getElement());
		}

		return null;
	}

	/**
	 * Fill the cell background. Expand the area of the fill to include any cell spacing, otherwise strips are left
	 * in the background colour of the grid.
	 */
	protected void paintCellBackground(final GC gc, final Rectangle bounds, final CellStyle cellStyle) {

		gc.setAlpha(cellStyle.getBackgroundOpacity());

		final RGB background = (alternate && (cellStyle.getBackgroundAlternate() != null)) ? cellStyle.getBackgroundAlternate() : cellStyle.getBackground();
		final RGB backgroundGradient1 = (alternate && (cellStyle.getBackgroundAlternateGradient1() != null)) ? cellStyle.getBackgroundAlternateGradient1() : cellStyle.getBackgroundGradient1();
		final RGB backgroundGradient2 = (alternate && (cellStyle.getBackgroundAlternateGradient2() != null)) ? cellStyle.getBackgroundAlternateGradient2() : cellStyle.getBackgroundGradient2();

		if (backgroundGradient1 == null || backgroundGradient2 == null) {
			//
			// Fill with no Gradient
			//
			gc.setBackground(getColour(background));
			gc.fillRectangle(bounds.x, bounds.y, bounds.width + styleRegistry.getCellSpacingHorizontal(), bounds.height + styleRegistry.getCellSpacingVertical());

		} else {
			//
			// Fill with Gradient (upper, lower).
			//
			final int halfHeight = bounds.height / 2;
			gc.setForeground(getColour(backgroundGradient1));
			gc.setBackground(getColour(background));
			gc.fillGradientRectangle(bounds.x, bounds.y, bounds.width + styleRegistry.getCellSpacingHorizontal(), halfHeight, true);

			gc.setForeground(getColour(background));
			gc.setBackground(getColour(backgroundGradient2));
			gc.fillGradientRectangle(bounds.x, bounds.y + halfHeight, bounds.width + styleRegistry.getCellSpacingHorizontal(), 1 + halfHeight + styleRegistry.getCellSpacingVertical(), true);
		}
	}

	/**
	 * Render a line in the specified border style.
	 */
	protected void paintBorderLine(final GC gc, final BorderStyle borderStyle, final Point point1, final Point point2) {

		if (borderStyle == null) {
			return;
		}

		switch (borderStyle.getLineStyle()) {
		case NONE:
			return;

		case BEVELED:
			// NOTE: Grips are currently only support on the right border side.
			gc.setForeground(getColour(RGB__SHADOW_HIGHLIGHT));
			gc.drawLine(point1.x, point1.y, point1.x, point2.y);

			gc.setForeground(getColour(RGB__SHADOW_DARK));
			gc.drawLine(point1.x - 1, point1.y, point1.x - 1, point2.y);
			break;

		case GRIP:
			// NOTE: Grips are currently only support on the right border side.
			final int spacer = new Double(((point2.y - point1.y) / 4.0) * 0.75).intValue();

			// Draw three dark dots in-between column headers.
			gc.setForeground(getColour(RGB__SHADOW_DARK));
			gc.drawPoint((point1.x-1), spacer + spacer);
			gc.drawPoint((point1.x-1), (spacer * 2) + spacer);
			gc.drawPoint((point1.x-1), (spacer * 3) + spacer);

			// Draw three light dots to make the dark ones look 3d.
			gc.setForeground(getColour(RGB__SHADOW_HIGHLIGHT));
			gc.drawPoint((point1.x-1), spacer + spacer + 1);
			gc.drawPoint((point1.x-1), (spacer * 2) + spacer + 1);
			gc.drawPoint((point1.x-1), (spacer * 3) + spacer + 1);
			break;

		case SOLID:
			gc.setForeground(getColour(borderStyle.getColour()));
			gc.setLineWidth(borderStyle.getWidth());
			gc.setLineStyle(SWT.LINE_SOLID);
			gc.drawLine(point1.x, point1.y, point2.x, point2.y);
			break;

		case DOTS:
			gc.setForeground(getColour(borderStyle.getColour()));
			gc.setLineWidth(borderStyle.getWidth());
			gc.setLineStyle(SWT.LINE_DOT);
			gc.drawLine(point1.x, point1.y, point2.x, point2.y);
			break;

		case DASHED:
			gc.setForeground(getColour(borderStyle.getColour()));
			gc.setLineWidth(borderStyle.getWidth());
			gc.setLineStyle(SWT.LINE_DASH);
			gc.drawLine(point1.x, point1.y, point2.x, point2.y);
			break;

		case DASH_DOT:
			gc.setForeground(getColour(borderStyle.getColour()));
			gc.setLineWidth(borderStyle.getWidth());
			gc.setLineStyle(SWT.LINE_DASHDOT);
			gc.drawLine(point1.x, point1.y, point2.x, point2.y);
			break;

		case DASH_DOT_DOT:
			gc.setForeground(getColour(borderStyle.getColour()));
			gc.setLineWidth(borderStyle.getWidth());
			gc.setLineStyle(SWT.LINE_DASHDOTDOT);
			gc.drawLine(point1.x, point1.y, point2.x, point2.y);
			break;
		}
	}

	protected Font getFont(final FontData fontData) {
		return grid.getResourceManager().getFont(fontData);
	}

	protected Color getColour(final RGB rgb) {
		return grid.getResourceManager().getColour(rgb);
	}
	
	protected Image getImage(final String imagePath) {
		return grid.getResourceManager().getImage(imagePath);
	}

	protected Point getTopLeft(final Rectangle rectangle) {
		topLeft.x = rectangle.x;
		topLeft.y = rectangle.y;
		return topLeft;
	}

	protected Point getTopRight(final Rectangle rectangle) {
		topRight.x = rectangle.x + rectangle.width;
		topRight.y = rectangle.y;
		return topRight;
	}

	protected Point getBottomLeft(final Rectangle rectangle) {
		bottomLeft.x = rectangle.x;
		bottomLeft.y = rectangle.y + rectangle.height;
		return bottomLeft;
	}

	protected Point getBottomRight(final Rectangle rectangle) {
		bottomRight.x = rectangle.x + rectangle.width;
		bottomRight.y = rectangle.y + rectangle.height;
		return bottomRight;
	}

	protected void setInnerBounds(final Rectangle original, final int delta) {
		innerBounds.x = original.x + delta;
		innerBounds.y = original.y + delta;
		innerBounds.width = original.width - (delta * 2);
		innerBounds.height = original.height - (delta * 2);
	}
	
	protected int getRowHeight(final Row<T> row) {
		return grid.getRowHeight(row);
	}
	
	protected boolean doesColumnHaveAnchor(final Column column) {
		return (column == gridModel.getSelectionModel().getAnchorColumn());
	}

	protected boolean doesRowHaveAnchor(final Row<T> row) {
		return ((row != null) && (row != Row.COLUMN_HEADER_ROW) && (row.getElement() == grid.getAnchorElement()));
	}

	/**
	 * Aligns the dimensions within the bounds specified and updates the content pointer with the top-left
	 * corner where the rectangle should be drawn.
	 */
	protected void align(final int width, final int height, final Rectangle bounds, final AlignmentStyle alignment, final CellStyle cellStyle) {

		switch (alignment) {
		case BOTTOM_CENTER:
			content.x = bounds.x + ((bounds.width - width) / 2);
			content.y = bounds.y + (bounds.height - height);
			break;

		case BOTTOM_LEFT:
			content.x = bounds.x + cellStyle.getPaddingLeft();
			content.y = bounds.y + (bounds.height - height);
			break;

		case BOTTOM_RIGHT:
			content.x = bounds.x + (bounds.width - width);
			content.y = bounds.y + (bounds.height - height);
			break;

		case CENTER:
			content.x = bounds.x + ((bounds.width - width) / 2);
			content.y = bounds.y + ((bounds.height - height) / 2);
			break;

		case LEFT_CENTER:
			content.x = bounds.x;
			content.y = bounds.y + ((bounds.height - height) / 2);
			break;

		case RIGHT_CENTER:
			content.x = bounds.x + (bounds.width - width);
			content.y = bounds.y + ((bounds.height - height) / 2);
			break;

		case TOP_CENTER:
			content.x = bounds.x + ((bounds.width - width) / 2);
			content.y = bounds.y;
			break;

		case TOP_LEFT:
			content.x = bounds.x;
			content.y = bounds.y;
			break;

		case TOP_RIGHT:
			content.x = bounds.x + (bounds.width - width);
			content.y = bounds.y;
			break;

		default:
			System.out.println("No alignment set!");
		}
	}
	
	public Point getTextExtent(final String text, final GC gc, final FontData fontData) {
		Map<String, Point> extentsByString = extentCache.get(fontData);
		
		if (extentsByString == null) {
			extentsByString = new HashMap<String, Point>();
			extentCache.put(fontData, extentsByString);			
		}
		
		Point extent = extentsByString.get(text);
		
		if (extent == null) {
			gc.setFont(getFont(fontData));
			extent = gc.textExtent(text);
			extentsByString.put(text, extent);
		}
		
		return extent;
	}
}
