package com.notlob.jgrid.renderer;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.styles.RegionStyle;

/**
 * Responsible for rendering a selection background and border around the selected column or row(s).
 * 
 * @author Stef
 */
public class SelectionRenderer<T> extends Renderer<T> {
	
	//
	// The references below are recycled objects - to avoid GC churn.
	//
	protected final Point rowLocation;
	protected final Rectangle hoverRegion;
	protected final Rectangle selectionRegion;

	public SelectionRenderer(final Grid<T> grid) {
		super(grid);
		rowLocation = new Point(0, 0);
		hoverRegion = new Rectangle(0, 0, 0, 0);
		selectionRegion = new Rectangle(0, 0, 0, 0);		
	}
	
	public void paintSelectionRegion(final RenderContext rc) {
		switch (grid.getSelectionStyle()) {
			case COLUMN_BASED:
				paintColumnBasedSelection(rc);
				break;

			case SINGL_CELL_BASED:
				paintCellBasedSelection(rc);
				break;
				
			default:
				paintRowBasedSelection(rc);
		}
	}

	/**
	 * Paint a selection region in the column containing the anchor.
	 */
	protected void paintColumnBasedSelection(final RenderContext rc) {
		final Rectangle viewportArea = viewport.getViewportArea(rc.getGC());
		final Column column = grid.getAnchorColumn();
		
		if (column != null) {						
			selectionRegion.x = viewport.getColumnViewportX(rc.getGC(), column);
			
			if (selectionRegion.x != -1) {
				selectionRegion.y = viewportArea.y;
				selectionRegion.width = viewport.getColumnWidth(selectionRegion.x, column);
				selectionRegion.height = grid.getClientArea().height - viewportArea.y - 1;
				
				paintSelectionRegion(rc, selectionRegion, (viewport.getFirstRowIndex() == 0), true, true, true, styleRegistry.getSelectionRegionStyle());
			}
			
		} else {
			selectionRegion.x = -1;
			selectionRegion.y = -1;
			selectionRegion.width = -1;
			selectionRegion.height= -1;
		}		
	}

	/**
	 * Paint a selection region in the column and row containing the anchor.
	 */
	protected void paintCellBasedSelection(final RenderContext rc) {
		final Rectangle viewportArea = viewport.getViewportArea(rc.getGC());
		final Column anchorColumn = grid.getAnchorColumn();
		final T element = grid.getAnchorElement();
		
		rowLocation.x = viewportArea.x;
		rowLocation.y = viewportArea.y;
		selectionRegion.x = rowLocation.x;
		selectionRegion.y = -1;
		selectionRegion.width = -1;
		selectionRegion.height= -1;
		
		if (anchorColumn != null) {						
			for (int columnIndex=viewport.getFirstColumnIndex(); columnIndex<viewport.getLastColumnIndex(); columnIndex++) {
				final Column column = gridModel.getColumns().get(columnIndex);
				
				if (column == anchorColumn) {
					for (int rowIndex=viewport.getFirstRowIndex(); rowIndex<viewport.getLastVisibleRowIndex(); rowIndex++) {
						final Row<T> row = gridModel.getRows().get(rowIndex);
	
						if (row.getElement() == element) {
							selectionRegion.y = rowLocation.y;
							selectionRegion.height = grid.getRowHeight(row);
							selectionRegion.width = viewport.getColumnWidth(selectionRegion.x, anchorColumn);
					
							paintSelectionRegion(rc, selectionRegion, true, true, true, true, getSelectionRegionStyle());
							return;
						}
						
						rowLocation.y += (grid.getRowHeight(row) + styleRegistry.getCellSpacingVertical());
					}
				}
				
				selectionRegion.x += viewport.getColumnWidth(selectionRegion.x, column);
			}
		}
	}
	
	/**
	 * Paint the selection region's background OR the selection region's borders (depending on the render pass)
	 */
	protected void paintRowBasedSelection(final RenderContext rc) {
		final GC gc = rc.getGC();
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
		selectionRegion.width = grid.getClientArea().width - viewportArea.x - 1;
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
					selectionRegion.height += grid.getRowHeight(row);

				} else {
					//
					// Start a new selection region.
					//
					selectionRegion.x = rowLocation.x;
					selectionRegion.y = rowLocation.y;
					selectionRegion.height = grid.getRowHeight(row);

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
				paintSelectionRegion(rc, selectionRegion, paintTopEdge, paintRightEdge, true, paintLeftEdge, getSelectionRegionStyle());
			}

			if (grid.isHighlightHoveredRow() && !row.isSelected() && (row == grid.getMouseHandler().getRow())) {
				//
				// The row has the mouse hovering over it, so paint it with the hover style. If the mouse is over a group field name though, don't highlight.
				//
				hoverRegion.x = rowLocation.x;
				hoverRegion.y = rowLocation.y;
				hoverRegion.height = grid.getRowHeight(row);
				hoverRegion.width = selectionRegion.width;
				paintSelectionRegion(rc, hoverRegion, !inSelection, paintRightEdge, true, paintLeftEdge, styleRegistry.getHoverRegionStyle());
			}

			inSelection = row.isSelected();
			rowLocation.y += (grid.getRowHeight(row) + styleRegistry.getCellSpacingVertical());
		}

		//
		// We'll need to paint the last selection region.
		//
		if (inSelection) {
			//
			// If the next row beyond the viewport exists and is selected, don't draw the bottom
			//
			paintBottomEdge = !(((viewport.getLastRowIndex() + 1) < gridModel.getRows().size()) && (gridModel.getRows().get(viewport.getLastRowIndex() + 1).isSelected()));
			paintSelectionRegion(rc, selectionRegion, paintTopEdge, paintRightEdge, paintBottomEdge, paintLeftEdge, getSelectionRegionStyle());
		}
	}

	/**
	 * Paint foreground or background details for the region of selected rows.
	 */
	protected void paintSelectionRegion(final RenderContext rc, final Rectangle bounds, final boolean paintTop, final boolean paintRight, final boolean paintBottom, final boolean paintLeft, final RegionStyle regionStyle) {
		final GC gc = rc.getGC();
		
		if (rc.getRenderPass() == RenderPass.BACKGROUND) {
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
			
		} else if (rc.getRenderPass() == RenderPass.FOREGROUND) {
			//
			// Paint a region border.
			//
			gc.setAlpha(regionStyle.getForegroundOpacity());
			gc.setForeground(getColour(regionStyle.getBorder().getColour()));
			gc.setLineWidth(regionStyle.getBorder().getWidth());

			// Get the bounds corners, but correct for thicker line widths.
			setCorners(bounds, topLeft, topRight, bottomRight, bottomLeft);
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
	
	private RegionStyle getSelectionRegionStyle() {
		return (grid.isFocusControl() || styleRegistry.getNotActiveSelectionRegionStyle() == null) ? styleRegistry.getSelectionRegionStyle() : styleRegistry.getNotActiveSelectionRegionStyle();
	}
}
