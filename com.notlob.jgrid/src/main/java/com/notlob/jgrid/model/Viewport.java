package com.notlob.jgrid.model;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.styles.CellStyle;

/**
 * Tracks what's visible in the Grid.
 *
 * @author Stef
 *
 */
public class Viewport<T> {

	// BUG: Pretty certain there's inconsistent use of cell padding / spacing (or lack of) throughout the methods in here....

	private int firstRowIndex;
	private int lastRowIndex;
	private int firstColumnIndex;
	private int lastColumnIndex;
	
	private final Grid<T> grid;
	private final GridModel<T> gridModel;
	private final Rectangle viewportArea;
	
	// There number of horizontal pixels tolerance to trigger a column resize with the mouse. 
	private final int RESIZE_DEADZONE = 3;
	
	public Viewport(final Grid<T> grid) {
		this.grid = grid;
		this.gridModel = grid.getGridModel();
		this.viewportArea = new Rectangle(-1, -1, -1, -1);
		invalidate();
	}

	public void invalidate() {
		firstRowIndex = -1;
		lastRowIndex = -1;
		firstColumnIndex = -1;
		lastColumnIndex = -1;
		viewportArea.x = -1;
		viewportArea.y = -1;
		viewportArea.width = -1;
		viewportArea.height = -1;
	}

	/**
	 * Calculates the first and last visible column and row indexes in the viewport.
	 */
	public void calculateVisibleCellRange(final GC gc) {
		// If they haven't been invalidated, then just return and let the cached values be used.
		if (firstRowIndex != -1 && lastRowIndex != -1 && firstColumnIndex != -1 && lastColumnIndex != -1) {
			return;
		}

		final Rectangle viewportArea = getViewportArea(gc);
		
		final int originX = grid.getHorizontalBar().getSelection();
		final int originY = grid.getVerticalBar().getSelection();
		
		if (!grid.getRows().isEmpty()) {
			setFirstRowIndex(originY);

			//
			// Get the first and last visible rows.
			//
			int y = 0;
			for (int rowIndex=originY; rowIndex<gridModel.getRows().size(); rowIndex++) {
				final Row<T> row = gridModel.getRows().get(rowIndex);
	
				y += (grid.getRowHeight(row) + gridModel.getStyleRegistry().getCellSpacingVertical());
	
				if ((y > viewportArea.height) && (getLastRowIndex() == -1)) {
					setLastRowIndex(rowIndex);
					break;
				}
			}
		}
		
		//
		// If all rows fit in the screen, cap it.
		//
		if (getLastRowIndex() == -1 && !gridModel.getRows().isEmpty()) {
			setLastRowIndex(gridModel.getRows().size() - 1);
		}

		
		if (!grid.getColumns().isEmpty()) {
			setFirstColumnIndex(originX);
		
			//
			// Get the first and last visible columns.
			//
			int x = 0;
			for (int columnIndex=originX; columnIndex<gridModel.getColumns().size(); columnIndex++) {
				final Column column = gridModel.getColumns().get(columnIndex);
	
				x += (column.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal());
	
				if ((x > viewportArea.width) && (getLastColumnIndex() == -1)) {
					setLastColumnIndex(columnIndex);
					break;
				}
			}
		}

		//
		// If all columns fit in the screen, cap it.
		//
		if (getLastColumnIndex() == -1 && !gridModel.getColumns().isEmpty()) {
			setLastColumnIndex(gridModel.getColumns().size() - 1);
		}
		
		//
		// If we're showing a mega-wide column that's wider than the grid.
		//
		if ((getFirstColumnIndex() == -1) && (getLastColumnIndex() != -1)) {
			setFirstColumnIndex(getLastColumnIndex());
		}
		
		//
		// If we're showing a mega-tall row that's taller than the grid.
		//
		if ((getFirstRowIndex() == -1) && (getLastRowIndex() != -1)) {
			setFirstRowIndex(getLastRowIndex());
		}
	}

	/**
	 * Calculates the bounds of the viewport (the area without column headers and row numbers).
	 */
	public Rectangle getViewportArea(final GC gc) {
		// Use the cached size if it's not been invalidated.
		if (viewportArea.x != -1 && viewportArea.y != -1 && viewportArea.width != -1 && viewportArea.height != -1) {
			return viewportArea;
		}

		viewportArea.x = grid.getClientArea().x;
		viewportArea.y = grid.getClientArea().y;
		viewportArea.width = grid.getClientArea().width;
		viewportArea.height = grid.getClientArea().height;

		//
		// Shift the viewport down to make room for column header row(s).
		//
		for (final Row<T> row : gridModel.getColumnHeaderRows()) {
			viewportArea.y += (grid.getRowHeight(row) + gridModel.getStyleRegistry().getCellSpacingVertical());
			viewportArea.height -= viewportArea.y;
		}

		//
		// Shift the viewport right enough to show the longest row number.
		//
		if (gridModel.isShowRowNumbers()) {
			final CellStyle cellStyle = gridModel.getStyleRegistry().getRowNumberStyle();
			final Point extent = grid.getTextExtent(String.valueOf(gridModel.getRows().size() + 1), gc, cellStyle.getFontData());
			final Column rowNumberColumn = gridModel.getRowNumberColumn();
			rowNumberColumn.setWidth(cellStyle.getPaddingLeft() + extent.x + cellStyle.getPaddingRight() + (cellStyle.getBorderOuterLeft() == null ? 0 : cellStyle.getBorderOuterLeft().getWidth()));
			
			viewportArea.x += (rowNumberColumn.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal()); 
			viewportArea.width -= (rowNumberColumn.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal());
		}
		
		//
		// Shift the viewport right enough if there's a group selector column.
		//
		if (gridModel.isShowGroupSelector()) {
			final Column groupSelectorColumn = gridModel.getGroupSelectorColumn();
			viewportArea.x += (groupSelectorColumn.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal()); 
			viewportArea.width -= (groupSelectorColumn.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal());
		}
		
		//
		// Shift the viewport right for every pinned column.
		//
		for (Column pinnedColumn : gridModel.getPinnedColumns()) {
			final int width = pinnedColumn.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal();
			viewportArea.x += width;
			viewportArea.width -= width;
		}

		return viewportArea;
	}

	/**
	 * If the right-edge of a row is currently visible this return the x position (minus the viewport's x).
	 *
	 * Otherwise it returns the width of the viewport.
	 */
	public int getVisibleRowWidth(final GC gc) {
		final Rectangle viewportArea = getViewportArea(gc);
		int x = viewportArea.x;

		for (int columnIndex=getFirstColumnIndex(); columnIndex<getLastVisibleColumnIndex(); columnIndex++) {
			final Column column = gridModel.getColumns().get(columnIndex);
			x += (column.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal());
		}

		return Math.min(viewportArea.width, x - viewportArea.x); // Can't currently explain this substract. It's TOO early in the morning!!!
	}

	/**
	 * Visual trick - we actually paint the visible columns and then the next TWO columns (although they are clipped).
	 * This stops flickering on the right-most column when horizontally scrolling.
	 */
	public int getLastVisibleColumnIndex() {
		return (lastColumnIndex == -1) ? -1 : Math.min(lastColumnIndex + 2, gridModel.getColumns().size());
	}

	public int getLastVisibleRowIndex() {
		return (lastRowIndex == -1) ? -1 : Math.min(lastRowIndex + 2, gridModel.getRows().size());
	}

	public int getFirstRowIndex() {
		return firstRowIndex;
	}

	/**
	 * Last (wholly visible - un-cropped) row index.
	 */
	public int getLastRowIndex() {
		return lastRowIndex;
	}

	public int getFirstColumnIndex() {
		return firstColumnIndex;
	}

	/**
	 * Last (wholly visible - un-cropped) column index.
	 */
	public int getLastColumnIndex() {
		return lastColumnIndex;
	}

	private void setFirstRowIndex(final int firstRowIndex) {
		this.firstRowIndex = firstRowIndex;
	}

	private void setLastRowIndex(final int lastRowIndex) {
		this.lastRowIndex = lastRowIndex;
	}

	private void setFirstColumnIndex(final int firstColumnIndex) {
		this.firstColumnIndex = firstColumnIndex;
	}

	private void setLastColumnIndex(final int lastColumnIndex) {
		this.lastColumnIndex = lastColumnIndex;
	}
	
	/**
	 * Return how many columns are currently visible.
	 */
	public int getWidthInColumns() {
		if ((getFirstColumnIndex() == -1) || (getLastColumnIndex() == -1)) {
			return 0;
		}
		
		final int columns = getLastColumnIndex() - getFirstColumnIndex();
		
		if (columns == 0 && (getFirstColumnIndex() != -1 || getLastColumnIndex() != -1)) {
			//
			// Edge case where there's a single column that's wider than the grid.
			//
			return 1;
		}
		
		return columns;
	}
	
	/**
	 * Return how many rows are currently visible.
	 */
	public int getHeightInRows() {
		if ((getFirstRowIndex() == -1) || (getLastRowIndex() == -1)) {
			return 0;
		}
		
		final int rows = getLastRowIndex() - getFirstRowIndex();
		
		if (rows == 0 && (getFirstRowIndex() != -1 || getLastRowIndex() != -1)) {
			//
			// An edge case where there's a single row that's taller than the grid.
			//
			return 1;	
		}
		
		return rows;
	}
	
	/**
	 * Return how many rows fit on the last page of the viewport.
	 */
	public int getRowCountLastPage(final GC gc) {		
		if (!grid.getRows().isEmpty()) {
			return getRowsToFitAbove(gc, (gridModel.getRows().get(gridModel.getRows().size()-1)));
		}
		
		return 0;
	}
	
	/**
	 * Return the number of rows that will fit in the viewport if the specified row is to be the last shown row.
	 */
	private int getRowsToFitAbove(final GC gc, final Row<T> startingRow) {
		final Rectangle viewportArea = getViewportArea(gc);
		
		int rowCount = 0;
		if (!grid.getRows().isEmpty()) {
			//
			// Work from the last row - towards the first, trying to fit them into the viewport. 
			//
			int y = 0;
			for (int rowIndex=(gridModel.getRows().indexOf(startingRow)); rowIndex>=0; rowIndex--) {
				final Row<T> row = gridModel.getRows().get(rowIndex);
				y += (grid.getRowHeight(row) + gridModel.getStyleRegistry().getCellSpacingVertical());
	
				if ((y <= viewportArea.height)) {
					rowCount++;
				} else {
					break;
				}
			}
		}
		
		return rowCount;
	}
	
	/**
	 * Return how many column fit on the last page of the viewport.
	 */
	public int getColumnCountLastPage(final GC gc) {		
		if (!grid.getColumns().isEmpty()) {
			return getColumnsToFitToTheLeftOf(gc, gridModel.getColumns().get(gridModel.getColumns().size()-1));
		}
		
		return 0;
	}
	
	/**
	 * Return how many columns will fit to the left of the specified column.
	 */
	private int getColumnsToFitToTheLeftOf(final GC gc, final Column startingColumn) {
		final Rectangle viewportArea = getViewportArea(gc);

		int columnCount = 0;
		if (!grid.getColumns().isEmpty()) {
			//
			// Work from the last column - towards the first, trying to fit them into the viewport. 
			//
			int x = 0;
			for (int columnIndex=(gridModel.getColumns().indexOf(startingColumn)); columnIndex>=0; columnIndex--) {
				final Column column = gridModel.getColumns().get(columnIndex);
	
				x += (column.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal());
	
				if ((x <= viewportArea.width)) {
					columnCount++;
				} else {
					break;
				}
			}
		}
		
		return columnCount;
	}

	/**
	 * Find the column at the pixel coordinates specified.
	 */
	public int getColumnIndexByX(final int x, final GC gc) {
		int currentX = getViewportArea(gc).x;

		if (x >= currentX) {
			//
			// Look at viewport columns.
			//
			for (int columnIndex=getFirstColumnIndex(); columnIndex<getLastVisibleColumnIndex(); columnIndex++) {
				final Column column = gridModel.getColumns().get(columnIndex);				
				currentX += getColumnWidth(currentX, column/*, false*/) + gridModel.getStyleRegistry().getCellSpacingHorizontal();
				
				if (x <= currentX) {
					return columnIndex;
				}								
			}
			
		} else {
			//
			// Compensate for the row number and group selector columns.
			//
			currentX = gridModel.isShowRowNumbers() ? gridModel.getRowNumberColumn().getWidth() : 0;
			currentX += gridModel.isShowGroupSelector() ? gridModel.getGroupSelectorColumn().getWidth() : 0;
			
			//
			// Look at pinned columns.
			//
			for (Column column : gridModel.getPinnedColumns()) {
				if ((x > currentX) && (x <= (currentX + column.getWidth()))) {
					return gridModel.getColumns().indexOf(column);
				}
				
				currentX += column.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal();
			}
		}

		return -1;
	}

	public int getColumnX(final Column column) {
		int x = 0;

		for (final Column current : gridModel.getColumns()) {
			if (current == column) {
				return x;
			}

			x += current.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal();
		}

		return -1;
	}
	
	/**
	 * Returns the x position of the column in the viewport.
	 */
	public int getColumnViewportX(final GC gc, final Column column) {
		int currentX = getViewportArea(gc).x;

		for (int columnIndex=getFirstColumnIndex(); columnIndex<getLastVisibleColumnIndex(); columnIndex++) {
			final Column current = gridModel.getColumns().get(columnIndex);
			
			if (current == column) {
				return currentX;
			}
			
			currentX += current.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal();
		}

		return -1;
	}
	
	/**
	 * Determines if there's a column at the specified mouse location which is eligible for the operation
	 * (RESIZE | REPOSITION) specified.
	 *  
	 * For RESIZE - if the location is near the edge of a column, return that column.
	 * 
	 * For REPOSITION - if the location is in the middle of the column (but not in the RESIZE bounds, then
	 * return that column);
	 */
	public Column getColumnForMouseOperation(final GC gc, final int x, final int y, final ColumnMouseOperation operation) {		
		final int height = grid.getRowHeight(gridModel.getColumnHeaderRow());
				
		//
		// Only proceed if the mouse is in the mouse header region.
		//
		if ((y >= 0) && (y <= height)) {		
			final Rectangle viewportArea = getViewportArea(gc);
			int columnHeaderX = viewportArea.x + gridModel.getStyleRegistry().getCellSpacingHorizontal();			
	
			for (int columnIndex=firstColumnIndex; columnIndex<lastColumnIndex; columnIndex++) {						
				final Column column = gridModel.getColumns().get(columnIndex);
				final int columnWidth = getColumnWidth(columnHeaderX, column/*, false*/);
				
				//
				// Note: We bump the columnHeaderX before the check for a resize and after for a reposition.
				//
				switch (operation) {
					case RESIZE:
						columnHeaderX += (columnWidth + gridModel.getStyleRegistry().getCellSpacingHorizontal());
						
						if ((x > (columnHeaderX - RESIZE_DEADZONE)) && (x < (columnHeaderX + RESIZE_DEADZONE))) {
							return column;
						}
						break;
						
					case REPOSITION:
						if ((x >= (columnHeaderX + RESIZE_DEADZONE)) && (x <= (columnHeaderX + column.getWidth() - RESIZE_DEADZONE))) {
							return column;
						}
						
						columnHeaderX += (columnWidth + gridModel.getStyleRegistry().getCellSpacingHorizontal());
						break;
				}
			}
		}
		
		return null;
	}

	/**
	 * Find the row at the pixel coordinates specified.
	 */
	public int getRowIndexByY(final int y, final GC gc) {
		final Rectangle viewportArea = getViewportArea(gc);
		int currentY = viewportArea.y;

		if ((y >= 0) && (y < currentY)) {
			//
			// A column header row has been clicked.
			//
			for (final Row<T> row : gridModel.getColumnHeaderRows()) {
				currentY += grid.getRowHeight(row);

				if (y <= currentY) {
					return -1;
				}
			}

		} else {
			//
			// A data row (or row number) has been clicked.
			//
			for (int rowIndex=getFirstRowIndex(); rowIndex<getLastVisibleRowIndex(); rowIndex++) {
				if (rowIndex == -1) {
					return -1;
				}
				
				if (rowIndex < gridModel.getRows().size()) {
					final Row<T> row = gridModel.getRows().get(rowIndex);
					currentY += grid.getRowHeight(row);

					if (y <= currentY) {
						return rowIndex;
					}
				}
			}
		}

		return -1;
	}

	/**
	 * Locate the y pixel co-ordinate of the row in the viewport's coordinates.
	 */
	public int getRowViewportY(final GC gc, final Row<T> row) {
		final Rectangle viewportArea = getViewportArea(gc);
		int currentY = viewportArea.y;

		for (int rowIndex=getFirstRowIndex(); rowIndex<=getLastRowIndex(); rowIndex++) {
			if ((rowIndex < 0) || (rowIndex >= gridModel.getRows().size())) {
				return -1;
			}
			
			final Row<T> currentRow = gridModel.getRows().get(rowIndex);

			if (currentRow == row) {
				return currentY;
			}

			currentY += (grid.getRowHeight(currentRow) + gridModel.getStyleRegistry().getCellSpacingVertical());
		}

		return -1;
	}

	/**
	 * Locate the y pixel co-ordinate of the row regardless of whether its on screen or not. 
	 * 
	 * Note: This is not offset by the column header heights.
	 */
	public int getRowY(final GC gc, final Row<T> row) {
		int currentY = 0;

		for (final Row<T> current : gridModel.getRows()) {
			if (current == row) {
				return currentY;
			}

			currentY += grid.getRowHeight(current);
		}

		return -1;
	}
	
	private boolean isRowAboveViewport(final Row<T> row) {
		return (row.getRowIndex() > 0) && (row.getRowIndex() < getFirstRowIndex());
	}
	
	private boolean isColumnLeftOfViewport(final Column column) {
		final int columnIndex = grid.getColumns().indexOf(column); 
		return (columnIndex > 0) && (columnIndex < getFirstColumnIndex());
	}

	/**
	 * Ensures the cell specified is visible in the viewport.
	 */
	public void reveal(final GC gc, final Column column, final Row<T> row) {
		final int rowIndex = row.getRowIndex();
		final int columnIndex = gridModel.getColumns().indexOf(column);
		final int max = grid.getVerticalBar().getMaximum();
		final int capped = Math.min(rowIndex, max);
		boolean selectionChanged = false;
		
		if (grid.getVerticalBar().isVisible() && !isRowVisible(row)) {
			if (isRowAboveViewport(row)) {
				grid.getVerticalBar().setSelection(capped);	
				
			} else {
				//
				// Scrolling down to make the row visible requires us to get the row to be the last row in the viewport. To do this,
				// we have to do a little walk up from the row - calculating how many rows will fit into the page.
				//
				grid.getRowHeight(row); // Force the height to calculate or default.
				grid.updateScrollbars();
				
				final int selection = row.getRowIndex() - (getRowsToFitAbove(gc, row) - 1);
				grid.getVerticalBar().setSelection(selection);
			}
			
			selectionChanged = true;
		}
		
		if (!isColumnPartiallyVisible(column)) {
			if (isColumnLeftOfViewport(column)) {
				grid.getHorizontalBar().setSelection(columnIndex);
				
			} else {
				//
				// Scrolling right to make the column visible requires us to get the column to be the last column in the viewport. To do this,
				// we have to do a little walk left from the column - calculating how many columns will fit into the page.
				//
				grid.getHorizontalBar().setSelection(columnIndex - (getColumnsToFitToTheLeftOf(gc, column) - 1));
			}
			
			selectionChanged = true;
		}
		
		if (selectionChanged) {
			invalidate();
			grid.redraw();
			grid.update();
		}
	}
	
	public boolean isRowPartiallyVisible(final Row<T> row) {
		return (row.getRowIndex() >= getFirstRowIndex() && row.getRowIndex() <= getLastVisibleRowIndex());
	}
	
	public boolean isRowVisible(final Row<T> row) {
		return (row.getRowIndex() >= getFirstRowIndex() && row.getRowIndex() < getLastRowIndex());
	}
	
	public boolean isColumnPartiallyVisible(final Column column) {
		final int columnIndex = grid.getColumns().indexOf(column);
		return (columnIndex >= getFirstColumnIndex() && columnIndex < getLastColumnIndex());
	}
	
	public boolean isColumnVisible(final Column column) {
		final int columnIndex = grid.getColumns().indexOf(column);
		return (columnIndex >= getFirstColumnIndex() && columnIndex < getLastColumnIndex());
	}
	
	private boolean isLastColumn(final Column column) {
		return (column == gridModel.getColumns().get(gridModel.getColumns().size() - 1));
	}

	/**
	 * The last column is 'stretchy' and runs to the end of the grid.
	 */
	public int getColumnWidth(final int columnX, final Column column) {		
		return isLastColumn(column) ? Math.max((grid.getClientArea().width - columnX), column.getWidth()) : column.getWidth();
	}
	
	public boolean isLastColumnCropped() {
		final Column lastColumn = gridModel.getColumns().isEmpty() ? null : gridModel.getColumns().get(gridModel.getColumns().size() - 1);
		
		if (lastColumn != null) {
			final int columnX = getColumnX(lastColumn);
			return (columnX + getColumnWidth(columnX, lastColumn) + gridModel.getStyleRegistry().getCellSpacingHorizontal()) > grid.getClientArea().width;
		}
		
		return false;
	}
	
	public boolean isLastRowCropped() {
		final Row<T> lastRow = gridModel.getRows().isEmpty() ? null : gridModel.getRows().get(gridModel.getRows().size() - 1);
		
		if (lastRow != null) {
			final boolean cropped = (getRowViewportY(grid.getGC(), lastRow) + grid.getRowHeight(lastRow) + gridModel.getStyleRegistry().getCellSpacingVertical()) > grid.getClientArea().height;
//				System.out.println(String.format("isLastRowCropped: row-y [%s], row-height [%s] v-spacing [%s] client-area-height [%s] cropped [%s]", 
//						getRowViewportY(grid.getGC(), lastRow), 
//						grid.getRowHeight(lastRow), 
//						gridModel.getStyleRegistry().getCellSpacingVertical(), 
//						grid.getClientArea().height,
//						cropped));
			return cropped;
		}
		
		return false;
	}

	@Override
	public String toString() {
		return String.format("Row [%s -> %s] Col [%s -> %s]", firstRowIndex, lastRowIndex, firstColumnIndex, lastColumnIndex);
	}
}
