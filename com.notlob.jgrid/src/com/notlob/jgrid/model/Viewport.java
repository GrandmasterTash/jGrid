package com.notlob.jgrid.model;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.renderer.GridRenderer;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.util.ResourceManager;

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

		//
		// Get the first and last visible rows.
		//
		int y = 0;
		for (int rowIndex=0; rowIndex<gridModel.getRows().size(); rowIndex++) {
			final Row<T> row = gridModel.getRows().get(rowIndex);

			if (y >= originY && getFirstRowIndex() == -1) {
				setFirstRowIndex(rowIndex);
			}

			y += gridModel.getRowHeight(gc, row);

			if ((y > (originY + viewportArea.height)) && (getLastRowIndex() == -1)) {
				setLastRowIndex(rowIndex);
				break;
			}
		}

		//
		// If all rows fit in the screen, cap it.
		//
		if (getLastRowIndex() == -1 && !gridModel.getRows().isEmpty()) {
			setLastRowIndex(gridModel.getRows().size());
		}

		//
		// Get the first and last visible columns.
		//
		int x = 0;
		for (int columnIndex=0; columnIndex<gridModel.getColumns().size(); columnIndex++) {
			final Column column = gridModel.getColumns().get(columnIndex);

			if (x >= originX && getFirstColumnIndex() == -1) {
				setFirstColumnIndex(columnIndex);
			}

			x += column.getWidth();

			if ((x > (originX + viewportArea.width)) && (getLastColumnIndex() == -1)) {
				setLastColumnIndex(columnIndex);
				break;
			}
		}

		//
		// If all columns fit in the screen, cap it.
		//
		if (getLastColumnIndex() == -1 && !gridModel.getColumns().isEmpty()) {
			setLastColumnIndex(gridModel.getColumns().size());
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
			viewportArea.y += (gridModel.getRowHeight(gc, row) + gridModel.getStyleRegistry().getCellSpacingVertical());
			viewportArea.height -= viewportArea.y;
		}

		//
		// Shift the viewport right enough to show the longest row number.
		//
		if (gridModel.isShowRowNumbers()) {
			final CellStyle cellStyle = gridModel.getStyleRegistry().getRowNumberStyle();
			gc.setFont(ResourceManager.getInstance().getFont(cellStyle.getFontData()));

			final Point extent = gc.textExtent(String.valueOf(gridModel.getRows().size()));
			extent.x += cellStyle.getPaddingLeft() + cellStyle.getPaddingRight();

			viewportArea.x += extent.x;
			viewportArea.width -= extent.x;
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
			final Column column = gridModel.getColumn(columnIndex);
			x += column.getWidth();
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
	 * Last (wholey visible - uncropped) row index.
	 */
	public int getLastRowIndex() {
		return lastRowIndex;
	}

	public int getFirstColumnIndex() {
		return firstColumnIndex;
	}

	/**
	 * Last (wholey visible - uncropped) column index.
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
	 * Find the column at the pixel coordinates specified.
	 */
	public int getColumnIndexByX(final int x, final GC gc) {
		int currentX = getViewportArea(gc).x;

		if (x >= currentX) {
			for (int columnIndex=getFirstColumnIndex(); columnIndex<getLastVisibleColumnIndex(); columnIndex++) {
				final Column column = gridModel.getColumn(columnIndex);
				currentX += column.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal();

				if (x <= currentX) {
					return columnIndex;
				}
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
			final Column current = gridModel.getColumn(columnIndex);
			
			if (current == column) {
				return currentX;
			}
			
			currentX += current.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal();
		}

		return -1;
	}
	
	/**
	 * If the location is near the edge of a column, return that column.
	 */
	@SuppressWarnings("unchecked")
	public Column getColumnToResize(GC gc, int x, int y) {		
		final int height = gridModel.getRowHeight(gc, Row.COLUMN_HEADER_ROW);
		final Rectangle viewportArea = getViewportArea(gc);
		int columnHeaderX = viewportArea.x + GridRenderer.ROW_OFFSET;			

		for (int columnIndex=firstColumnIndex; columnIndex<lastColumnIndex; columnIndex++) {						
			final Column column = gridModel.getColumns().get(columnIndex);
			columnHeaderX += (column.getWidth() + gridModel.getStyleRegistry().getCellSpacingHorizontal());
			
			if ((x > (columnHeaderX - 3)) && (x < (columnHeaderX + 3)) && (y >= 0) && (y <= height)) {
				return column;
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
				currentY += gridModel.getRowHeight(gc, row);

				if (y <= currentY) {
					return -1;
				}
			}

		} else {
			//
			// A data row (or row number) has been clicked.
			//
			for (int rowIndex=getFirstRowIndex(); rowIndex<getLastVisibleRowIndex(); rowIndex++) {
				if (rowIndex < gridModel.getRows().size()) {
					final Row<T> row = gridModel.getRows().get(rowIndex);
					currentY += gridModel.getRowHeight(gc, row);

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

		for (int rowIndex=getFirstRowIndex(); rowIndex<getLastRowIndex(); rowIndex++) {
			final Row<T> currentRow = gridModel.getRows().get(rowIndex);

			if (currentRow == row) {
				return currentY;
			}

			currentY += (gridModel.getRowHeight(gc, currentRow) + gridModel.getStyleRegistry().getCellSpacingVertical());
		}

		return -1;
	}

	private int getRowY(final GC gc, final Row<T> row) {
		int currentY = 0;

		for (final Row<T> current : gridModel.getRows()) {
			if (current == row) {
				return currentY;
			}

			currentY += gridModel.getRowHeight(gc, current);
		}

		return -1;
	}

	/**
	 * Ensures the cell specified is visible in the viewport.
	 */
	public void reveal(final GC gc, final Column column, final Row<T> row) {

		final int rowIndex = gridModel.getRows().indexOf(row);
		final int columnIndex = gridModel.getColumns().indexOf(column);

		//
		// Check which direction we need to scroll vertically and horizontally and by how many rows and columns.
		//
		final int vDelta = (rowIndex < firstRowIndex) ? -1 : ((rowIndex > (lastRowIndex-1)) ? 1 : 0);
		final int hDelta = (columnIndex < firstColumnIndex) ? -1 : ((columnIndex > (lastColumnIndex-1)) ? 1 : 0);

		if (hDelta != 0) {
			//
			// Last column edge case - select max scroll.
			//
			if (columnIndex == (gridModel.getColumns().size()-1)) {
				grid.getHorizontalBar().setSelection(grid.getHorizontalBar().getMaximum());

			} else {
				final Column scrollToColumn = gridModel.getColumns().get(columnIndex + 1);

				if (hDelta < 0) {
					grid.getHorizontalBar().setSelection(getColumnX(scrollToColumn) - scrollToColumn.getWidth());
				} else {
					grid.getHorizontalBar().setSelection((getColumnX(scrollToColumn) - viewportArea.width));
				}
			}
		}

		if (vDelta != 0) {
			//
			// Last row edge case - select max scroll.
			//
			if (rowIndex == (gridModel.getRows().size()-1)) {
				grid.getVerticalBar().setSelection(grid.getVerticalBar().getMaximum());

			} else {
				final Row<T> scrollToRow = gridModel.getRows().get(rowIndex + 1);

				if (vDelta < 0) {
					grid.getVerticalBar().setSelection(getRowY(gc, scrollToRow) - gridModel.getRowHeight(gc, scrollToRow));
				} else {
					grid.getVerticalBar().setSelection((getRowY(gc, scrollToRow) - viewportArea.height));
				}
			}
		}
	}

	@Override
	public String toString() {
		return String.format("Row [%s -> %s] Col [%s -> %s]", firstRowIndex, lastRowIndex, firstColumnIndex, lastColumnIndex);
	}
}
