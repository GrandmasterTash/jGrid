package com.notlob.jgrid.renderer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.input.GridMouseHandler;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.styles.CellStyle;

/**
 * Responsible for rendering normal (non-group) rows.
 * 
 * @author Stef
 *
 * @param <T>
 */
public class RowRenderer<T> extends Renderer<T> {

	protected final CellRenderer<T> cellRenderer;
	
	protected final Image dropImage;
	
	//
	// The references below are recycled objects - to avoid GC churn.
	//	
	protected final Rectangle cellBounds;
	
	protected final Rectangle groupSelectorBounds;
	
	public RowRenderer(final Grid<T> grid, final CellRenderer<T> cellRenderer) {
		super(grid);
		this.cellRenderer = cellRenderer;
		dropImage = getImage("inwards_arrows.png");
		cellBounds = new Rectangle(0, 0, 0, 0);
		groupSelectorBounds = new Rectangle(0, 0, 0, 0);
	}
			
	public void paintRow(final RenderContext rc, final Rectangle rowBounds, final Row<T> row) {
		try {
			final GC gc = rc.getGC();
			
			//
			// Initialise the cell bounds.
			//
			cellBounds.x = rowBounds.x;
			cellBounds.y = rowBounds.y;
			cellBounds.height = rowBounds.height;
			
			if (gridModel.isShowRowNumbers() || gridModel.isShowGroupSelector()) {
				if (row == gridModel.getColumnHeaderRow()) {
					//
					// Paint the corner cell if needed.
					//
					paintCornerCell(rc);
					
				} else {				
					//
					// Paint row numbers if needed.
					//
					if (gridModel.isShowRowNumbers()) {
						paintRowNumber(rc, row);
					} 
					
					if (gridModel.isShowGroupSelector()) {
						paintGroupSelector(rc, row);
					}
				}
			}
			
			//
			// Paint any pinned cells.
			//
			rc.setPaintingPinned(true);
			for (Column pinnedColumn : gridModel.getPinnedColumns()) {
				final CellStyle cellStyle = styleRegistry.getCellStyle(pinnedColumn, row);				
				cellBounds.width = pinnedColumn.getWidth();
				cellRenderer.paintCell(rc, cellBounds, pinnedColumn, row, cellStyle);
				cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
			}
			rc.setPaintingPinned(false);
			
			//
			// Paint a vertical border - to the right of the last pinned cell.
			//
			if (!gridModel.getPinnedColumns().isEmpty()) {
				setCorners(cellBounds, topLeft, topRight, bottomRight, bottomLeft);
				paintBorderLine(gc, styleRegistry.getHeaderStyle().getBorderOuterBottom(), topLeft, bottomLeft);
			}
			
			//
			// Now paint every cell in the row.
			//
			paintBodyCells(rc, row);
	
			//
			// Render a column-reposition indicator if we're dragging columns around.
			//
			paintColumnDragImage(rc, rowBounds, row);
			
		} catch (Throwable t) {
			if (!rc.isErrorLogged()) {
				//
				// Print the error to the std err and ensure we only do this once to avoid log fillage.
				//
				System.err.println(String.format("Failed to paint row: %s", t.getMessage()));
				t.printStackTrace(System.err);
				rc.setErrorLogged(true);
			}
		}
	}

	/**
	 * Paint the main body cells for the row.
	 */
	protected void paintBodyCells(final RenderContext rc, final Row<T> row) {
		//
		// If we're computing the row size, we need to consider every cell in the row, not just the visible cells.
		// If not, the rows can shift heights as the grid scrolls from left-to-right.
		//
		final int firstIndex = (rc.getRenderPass() == RenderPass.COMPUTE_SIZE) ? 0 : viewport.getFirstColumnIndex();
		final int lastIndex = (rc.getRenderPass() == RenderPass.COMPUTE_SIZE) ? grid.getColumns().size() : viewport.getLastVisibleColumnIndex();
		
		for (int columnIndex=firstIndex; columnIndex<lastIndex; columnIndex++) {
			final Column column = gridModel.getColumns().get(columnIndex);
			final CellStyle cellStyle = styleRegistry.getCellStyle(column, row);

			//
			// Don't paint a column header grip on the last column.
			//
			rc.setDontPaintGrip(columnIndex == (gridModel.getColumns().size() - 1));
			
			//
			// Paint the cell now.
			//
			cellBounds.width = viewport.getColumnWidth(cellBounds.x, column);
			cellRenderer.paintCell(rc, cellBounds, column, row, cellStyle);
			cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
			rc.setDontPaintGrip(false);
		}
	}

	/**
	 * If the user is dragging a column, render it as an image being dragged.
	 */
	private void paintColumnDragImage(final RenderContext rc, final Rectangle rowBounds, final Row<T> row) {
		final GC gc = rc.getGC();
		
		if ((row == gridModel.getColumnHeaderRow()) && (rc.getRenderPass() == RenderPass.FOREGROUND) && (grid.getMouseHandler().getTargetColumn() != null)) {
			//
			// Otherwise, move across the viewport until we get to the drag target column.
			//
			cellBounds.x = rowBounds.x;
			
			//
			// Offset by row number column width.
			//
			if (grid.isShowRowNumbers()) {
				cellBounds.x += (gridModel.getRowNumberColumn().getWidth() + styleRegistry.getCellSpacingHorizontal());
			}
			
			//
			// Offset by group selector width.
			//
			if (grid.isShowGroupSelector()) {
				cellBounds.x += (gridModel.getGroupSelectorColumn().getWidth() + styleRegistry.getCellSpacingHorizontal());
			}
			
			//
			// Offset by pinned column widths.
			//
			for (Column pinnedColumn : gridModel.getPinnedColumns()) {
				cellBounds.x += (pinnedColumn.getWidth() + styleRegistry.getCellSpacingHorizontal());
			}
			
			for (int columnIndex=viewport.getFirstColumnIndex(); columnIndex<viewport.getLastVisibleColumnIndex(); columnIndex++) {
				final Column column = gridModel.getColumns().get(columnIndex);
				if (column == grid.getMouseHandler().getTargetColumn()){ 
					gc.drawImage(dropImage, cellBounds.x - (dropImage.getBounds().width / 2) + 1, 4);					
				}
				cellBounds.width = viewport.getColumnWidth(cellBounds.x, column);
				cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
			}
			
			if (grid.getMouseHandler().getTargetColumn() == GridMouseHandler.LAST_COLUMN) {
				gc.drawImage(dropImage, cellBounds.x - (dropImage.getBounds().width / 2) + 1, 4);
			}
		}
	}	
	
	/**
	 * Paint the top-left corner cell.
	 */
	protected void paintCornerCell(final RenderContext rc) {
		//
		// Ensure the grip style header separator isn't used for this column header.
		//
		rc.setDontPaintGrip(true);

		//
		// Paint the background or borders.
		//
		cellBounds.width = (gridModel.isShowRowNumbers() ? gridModel.getRowNumberColumn().getWidth() : 0) + (gridModel.isShowGroupSelector() ? gridModel.getGroupSelectorColumn().getWidth() : 0);
		cellRenderer.paintCell(rc, cellBounds, gridModel.getRowNumberColumn(), gridModel.getColumnHeaderRow(), styleRegistry.getCornerStyle());
		cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
		
		//
		// Allow the grip to be used.
		//
		rc.setDontPaintGrip(false);
	}
	
	/**
	 * Paint a row number cell for the specified row.
	 */
	protected void paintRowNumber(final RenderContext rc, final Row<T> row) {		
		//
		// Get the normal row-number style or the selected style.
		//
		final CellStyle cellStyle = (grid.isFocusControl() && grid.isHighlightAnchorInHeaders() && doesRowHaveAnchor(row)) ? styleRegistry.getSelectionRowNumberStyle() : styleRegistry.getRowNumberStyle();
		
		//
		// Paint the row-number cell.
		//
		cellBounds.width = gridModel.getRowNumberColumn().getWidth();
		cellRenderer.paintCell(rc, cellBounds, gridModel.getRowNumberColumn(), row, cellStyle);
		cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
	}
	
	/**
	 * Paint a group selector cell for the specified row.
	 */
	protected void paintGroupSelector(final RenderContext rc, final Row<T> row) {		
		//
		// Get the normal row-number style or the selected style.
		//
		final GC gc = rc.getGC();
		
		//
		// Ascertain if we're the first row in the group, in the middle or the last row).
		//
		final boolean isFirstInGroup = isFirstInGroup(row);
		final boolean isLastInGroup = isLastInGroup(row);
		
		cellBounds.width = gridModel.getGroupSelectorColumn().getWidth();
		shrinkRectangle(cellBounds, groupSelectorBounds, 1);
		groupSelectorBounds.x += 1;
		groupSelectorBounds.width -= 2;
		setCorners(groupSelectorBounds, topLeft, topRight, bottomRight, bottomLeft);
		
		gc.setLineWidth(1);
		gc.setLineStyle(SWT.LINE_SOLID);
		gc.setForeground(getColour(styleRegistry.getMainBorderTop().getColour()));
		gc.setBackground(getColour(styleRegistry.getHeaderStyle().getBackground()));

		if (isFirstInGroup || isLastInGroup) {
			//
			// Draw a filled rounded rectangle - we obiliterate either the top or bottom in a bit.
			//
			gc.fillRoundRectangle(groupSelectorBounds.x, groupSelectorBounds.y, groupSelectorBounds.width, groupSelectorBounds.height, 8, 8);
			gc.drawRoundRectangle(groupSelectorBounds.x, groupSelectorBounds.y, groupSelectorBounds.width, groupSelectorBounds.height, 8, 8);
			
			if (isFirstInGroup && !isLastInGroup) {
				//
				// Wipe-out the bottom curves to to make it look like we continue onto the next row.
				//
				groupSelectorBounds.x += 1;
				groupSelectorBounds.y += 8;
				groupSelectorBounds.width -= 1;
				gc.fillRectangle(groupSelectorBounds.x, groupSelectorBounds.y, groupSelectorBounds.width, groupSelectorBounds.height);
				
				groupSelectorBounds.x -= 1;
				groupSelectorBounds.width += 1;
				setCorners(groupSelectorBounds, topLeft, topRight, bottomRight, bottomLeft);
				gc.drawLine(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y);
				gc.drawLine(topRight.x, topRight.y, bottomRight.x, bottomRight.y);
			
			} else if (!isFirstInGroup && isLastInGroup) {
				//
				// Wipe-out the top curves to look like we continue from the previous row.
				//
				groupSelectorBounds.x += 1;
				groupSelectorBounds.height -= 8;
				groupSelectorBounds.width -= 1;
				gc.fillRectangle(groupSelectorBounds.x, groupSelectorBounds.y, groupSelectorBounds.width, groupSelectorBounds.height);
				
				groupSelectorBounds.x -= 1;
				groupSelectorBounds.width += 1;
				setCorners(groupSelectorBounds, topLeft, topRight, bottomRight, bottomLeft);
				gc.drawLine(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y);
				gc.drawLine(topRight.x, topRight.y, bottomRight.x, bottomRight.y);								
			}
			
			if (isLastInGroup) {
				//
				// Draw an expand / collapse image.
				//
// TODO: Use align on this rather than hardcoded offsets.				
				final Image expandImage = grid.getContentProvider().isCollapsed(row.getElement()) ? getImage("plus.png") : getImage("minus.png");
				gc.drawImage(expandImage, groupSelectorBounds.x + 2, groupSelectorBounds.y + 4);
			}
			
		} else {
			groupSelectorBounds.y -= 0;
			groupSelectorBounds.height += 6;
			setCorners(groupSelectorBounds, topLeft, topRight, bottomRight, bottomLeft);
			gc.fillRectangle(groupSelectorBounds.x, groupSelectorBounds.y, groupSelectorBounds.width, groupSelectorBounds.height);			
			gc.drawLine(topLeft.x, topLeft.y, bottomLeft.x, bottomLeft.y);
			gc.drawLine(topRight.x, topRight.y, bottomRight.x, bottomRight.y);
		}
		
		cellBounds.x += (cellBounds.width + styleRegistry.getCellSpacingHorizontal());
	}
	
	/**
	 * True if the next visible row above this one is in a different group - OR if this is the first visible row.
	 */
	public boolean isFirstInGroup(final Row<T> row) {
		if (row.getRowIndex() == 0) {
			return true;
		}
		
		if (row.getRowIndex() < 0 || row.getRowIndex() >= gridModel.getRows().size()) {
			return false;
		}
		
		return !gridModel.isSameGroup(row, gridModel.getRows().get(row.getRowIndex() - 1));
	}
	
	public boolean isLastInGroup(final Row<T> row) {
		if (row.getRowIndex() >= (gridModel.getRows().size() - 1)) {
			return true;
		}		
		
		if (row.getRowIndex() < 0 || row.getRowIndex() >= gridModel.getRows().size()) {
			return false;
		}
		
		return !gridModel.isSameGroup(row, gridModel.getRows().get(row.getRowIndex() + 1));
	}
	
	/**
	 * Return the expand/collapse group row image bounds for the specified group row.
	 */
	public Rectangle getExpandImageBounds(final RenderContext rc, final Row<T> row, final Rectangle rowBounds) {
		final int y = viewport.getRowViewportY(rc.getGC(), row);
		final Image expandImage = grid.getContentProvider().isCollapsed(row.getElement()) ? getImage("plus.png") : getImage("minus.png");
		final Rectangle bounds = new Rectangle(rowBounds.x, y, expandImage.getBounds().width, expandImage.getBounds().height);
		bounds.x += (3 + (gridModel.isShowRowNumbers() ? (gridModel.getRowNumberColumn().getWidth() + styleRegistry.getCellSpacingHorizontal()) : 0));
		bounds.y += 4;
		return bounds;
	}
}
