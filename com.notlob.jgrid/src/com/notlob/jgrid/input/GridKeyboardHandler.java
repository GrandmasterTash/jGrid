package com.notlob.jgrid.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.widgets.ScrollBar;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.model.SelectionModel;

// TODO: Both this and mouse need try...catches...
public class GridKeyboardHandler<T> implements KeyListener {

	private final GC gc;
	private final Grid<T> grid;
	private final GridModel<T> gridModel;
	private final SelectionModel<T> selectionModel;

	public GridKeyboardHandler(final Grid<T> grid, final GC gc) {
		this.gc = gc;
		this.grid = grid;
		this.gridModel = grid.getGridModel();
		this.selectionModel = grid.getGridModel().getSelectionModel();
	}

	@Override
	public void keyReleased(final KeyEvent e) {
		final boolean ctrl = (e.stateMask & SWT.CTRL) == SWT.CTRL;
		
		if ((e.keyCode == 97) && ctrl) {
			//
			// CTRL+A
			//
			gridModel.getSelectionModel().selectAll();
		}
	}

	@Override
	public void keyPressed(final KeyEvent e) {

		//
		// Ignore key presses if there's no data or no columns.
		//
		if (gridModel.getRows().isEmpty() || gridModel.getColumns().isEmpty()) {
			return;
		}

		final boolean shift = (e.stateMask & SWT.SHIFT) == SWT.SHIFT;
		final boolean ctrl = (e.stateMask & SWT.CTRL) == SWT.CTRL;

		switch (e.keyCode) {
			case SWT.ARROW_UP:
			case SWT.ARROW_DOWN:
			case SWT.ARROW_LEFT:
			case SWT.ARROW_RIGHT:
				if (shift) {
					//
					// Expand selection range down or up - preserve the anchor though.
					//
					final T anchorElement = selectionModel.getAnchorElement();
					alterSelectionRange(e.keyCode);
					selectionModel.setAnchorElement(anchorElement);
					
				} else if (ctrl) {
					//
					// Move the anchor.
					//
					moveAnchor(e.keyCode);
					
				} else {
					//
					// Move the anchor.
					//
					moveAnchor(e.keyCode);

					//
					// Set the new selection to the anchor row / column.
					//
					selectionModel.setSelectedRows(Collections.singletonList(gridModel.getRow(selectionModel.getAnchorElement())));
				}
				
				break;
				
			case SWT.PAGE_DOWN:
				pageDown();
				break;
				
			case SWT.PAGE_UP:
				pageUp();
				break;
				
			case SWT.HOME:
				if (ctrl) {
					goTopLeft();
					
				} else {
					goHome();
				}
				break;
				
			case SWT.END:
				if (ctrl) {
					goBottomLeft();
				} else {
					goEnd();
				}
				break;
			
			case SWT.SPACE:
				if (ctrl) {
					toggleSelection();
				}
				break;
		}
		
	}
	
	private void toggleSelection() {
		//
		// Ensure an anchor exists.
		//
		ensureAnchorSet();
		
		//
		// Toggle the row's selection (or group if it's a group row).
		//
		final Row<T> row = gridModel.getRow(gridModel.getSelectionModel().getAnchorElement());
		final List<Row<T>> rows = new ArrayList<>();
		rows.addAll(gridModel.isParentRow(row) ? gridModel.getWholeGroup(row) : Collections.singletonList(row));
		gridModel.getSelectionModel().toggleRowSelections(rows);
		
	}

	private void goTopLeft() {
		//
		// Get the first column and the current anchor row.
		//
		final Row<T> row = gridModel.getRows().get(0);
		final Column column = gridModel.getColumns().get(0);
		
		//
		// Update and reveal the anchor column.
		//		
		gridModel.getSelectionModel().setAnchorColumn(column);
		gridModel.getSelectionModel().setAnchorElement(row.getElement());
		grid.getViewport().reveal(gc, column, row);
		
		//
		// Ensure the grid paints.
		//
		gridModel.fireChangeEvent();
	}
	
	private void goBottomLeft() {
		//
		// Get the first column and the current anchor row.
		//
		final Row<T> row = gridModel.getRows().get(gridModel.getRows().size()-1);
		final Column column = gridModel.getColumns().get(0);
		
		//
		// Update and reveal the anchor column.
		//		
		gridModel.getSelectionModel().setAnchorColumn(column);
		gridModel.getSelectionModel().setAnchorElement(row.getElement());
		grid.getViewport().reveal(gc, column, row);
		
		//
		// Ensure the grid paints.
		//
		gridModel.fireChangeEvent();
	}
	
	private void goHome() {
		//
		// Ensure there's an anchor.
		//
		ensureAnchorSet();
		
		//
		// Get the first column and the current anchor row.
		//
		final Row<T> row = gridModel.getRow(gridModel.getSelectionModel().getAnchorElement());
		final Column column = gridModel.getColumns().get(0);
		
		//
		// Update and reveal the anchor column.
		//		
		gridModel.getSelectionModel().setAnchorColumn(column);
		grid.getViewport().reveal(gc, column, row);
		
		//
		// Ensure the grid paints.
		//
		gridModel.fireChangeEvent();
	}
	
	private void goEnd() {
		//
		// Ensure there's an anchor.
		//
		ensureAnchorSet();
		
		//
		// Get the first column and the current anchor row.
		//
		final Row<T> row = gridModel.getRow(gridModel.getSelectionModel().getAnchorElement());
		final Column column = gridModel.getColumns().get(gridModel.getColumns().size()-1);
		
		//
		// Update and reveal the anchor column.
		//		
		gridModel.getSelectionModel().setAnchorColumn(column);
		grid.getViewport().reveal(gc, column, row);
		
		//
		// Ensure the grid paints.
		//
		gridModel.fireChangeEvent();		
	}
	
	private void pageUp() {
		//
		// Ensure there's an anchor.
		//
		ensureAnchorSet();
		
		//
		// Move the scrollbar down one page.
		//
		final ScrollBar verticalBar = grid.getVerticalBar();
		verticalBar.setSelection(Math.max(verticalBar.getSelection() - verticalBar.getPageIncrement(), verticalBar.getMinimum()));
		
		//
		// Cause a repaint.
		//
		gridModel.fireChangeEvent();
		
		//
		// Move the anchor to the new page.
		//
		if (verticalBar.getSelection() != verticalBar.getMaximum()) {
			final Row<T> row = gridModel.getRows().get(grid.getViewport().getFirstRowIndex());
			gridModel.getSelectionModel().setAnchorElement(row.getElement());
		}
	}

	private void pageDown() {
		//
		// Ensure there's an anchor.
		//
		ensureAnchorSet();
		
		//
		// Move the scrollbar down one page.
		//
		final ScrollBar verticalBar = grid.getVerticalBar();
		verticalBar.setSelection(Math.min(verticalBar.getSelection() + verticalBar.getPageIncrement(), verticalBar.getMaximum()));
		
		//
		// Cause a repaint.
		//
		gridModel.fireChangeEvent();
		
		//
		// Move the anchor to the new page.
		//
		if (verticalBar.getSelection() != verticalBar.getMaximum()) {
			final Row<T> row = gridModel.getRows().get(grid.getViewport().getFirstRowIndex());
			gridModel.getSelectionModel().setAnchorElement(row.getElement());
		}
	}

	private void ensureAnchorSet() {
		//
		// If there's no current anchor element, use the first visible row.
		//
		if (selectionModel.getAnchorElement() == null) {
			final Row<T> firstRow = gridModel.getRows().get(0);
			selectionModel.setAnchorElement(firstRow.getElement());
		}

		//
		// If there's no current anchor column, use the first visible column.
		//
		if (selectionModel.getAnchorColumn() == null) {
			if (gridModel.isParentElement(selectionModel.getAnchorElement()) && !gridModel.getGroupByColumns().isEmpty()) {
				selectionModel.setAnchorColumn(gridModel.getGroupByColumns().get(0));
			} else {
				selectionModel.setAnchorColumn(gridModel.getColumns().get(0));
			}
		}
	}

	private void moveAnchor(final int direction) {
		ensureAnchorSet();
		final T oldAnchorElement = selectionModel.getAnchorElement();

		//
		// Now move the anchor.
		//
		if ((direction == SWT.ARROW_UP) || (direction == SWT.ARROW_DOWN)) {
			//
			// Move the anchor up or down (if we're not already at the top or bottom of the grid).
			//
			final Row<T> anchorRow = gridModel.getRow(selectionModel.getAnchorElement());
			final int rowIndex = gridModel.getRows().indexOf(anchorRow);

			if (((direction == SWT.ARROW_UP) && (rowIndex == 0)) || ((direction == SWT.ARROW_DOWN) && (rowIndex == (gridModel.getRows().size()-1)))) {
				//
				// We're at the top/bottom already.
				//
				return;
			}

			//
			// Update the anchor element.
			//
			final int newRowIndex = rowIndex + (direction == SWT.ARROW_UP ? -1 : 1);
			final Row<T> newRow = gridModel.getRows().get(newRowIndex);
			selectionModel.setAnchorElement(newRow.getElement());

			if (gridModel.isParentElement(oldAnchorElement) && !gridModel.isParentElement(newRow.getElement())) {
				//
				// Transitioning from a group-to-non-group row means the anchor column must change.
				//
				selectionModel.setAnchorColumn(selectionModel.getLastChildAnchorColumn());

			} else if (!gridModel.isParentElement(oldAnchorElement) && gridModel.isParentElement(newRow.getElement())) {
				//
				// Transitioning from a non-group-to-group row means the anchor column must change.
				//
				selectionModel.setAnchorColumn(selectionModel.getLastParentAnchorColumn());
			}

		} else if ((direction == SWT.ARROW_LEFT) || (direction == SWT.ARROW_RIGHT)) {
			//
			// Is this a group row or child row?
			//
			final List<Column> columns = gridModel.isParentElement(selectionModel.getAnchorElement()) ? gridModel.getGroupByColumns() : gridModel.getColumns();

			//
			// Move the anchor left or right (if we're not already at the left or right edge of the grid).
			//
			final int columnIndex = columns.indexOf(selectionModel.getAnchorColumn());

			if (((direction == SWT.ARROW_LEFT) && (columnIndex == 0)) || ((direction == SWT.ARROW_RIGHT) && (columnIndex == (columns.size()-1)))) {
				//
				// We're at the left/right edge already.
				//
				return;
			}

			//
			// Update the anchor column.
			//
			final int nextColumnIndex = columnIndex + (direction == SWT.ARROW_LEFT ? -1 : 1);
			final Column nextColumn = columns.get(nextColumnIndex);
			selectionModel.setAnchorColumn(nextColumn);

		} else {
			throw new IllegalArgumentException(String.format("Unknown direction %s", direction));
		}

		if ((selectionModel.getAnchorElement() != null) && (selectionModel.getAnchorElement() != null) && !gridModel.isParentElement(selectionModel.getAnchorElement())) {
			//
			// Ensure the anchor cell is visible.
			//
			grid.reveal(selectionModel.getAnchorColumn(), selectionModel.getAnchorElement());
		}

		//
		// Cause a repaint.
		//
		gridModel.fireChangeEvent();
	}
	
	private void alterSelectionRange(final int direction) {
		//
		// Verify the direction specified.
		//
		if (((direction == SWT.ARROW_LEFT) || (direction == SWT.ARROW_RIGHT))) {
			return;
			
		} else if (!((direction == SWT.ARROW_UP) || (direction == SWT.ARROW_DOWN))) {
			throw new IllegalArgumentException(String.format("Unknown direction %s", direction));
		}		
		
		ensureAnchorSet();
		final int anchorRowIndex = gridModel.getRows().indexOf(gridModel.getRow(selectionModel.getAnchorElement()));
		
		if (direction == SWT.ARROW_UP) {
			//
			// Shrink selection ranges below the anchor until they start growing above the anchor.
			//
			removeRowFromSelection();
			
		} else if (direction == SWT.ARROW_DOWN) {
			//
			// Shrink selection ranges above the anchor until they start growing below the anchor.
			//
			addRowToSelection(anchorRowIndex);
		}
		
		//
		// Cause a repaint.
		//
		gridModel.fireChangeEvent();
	}

	private void removeRowFromSelection() {
		//
		// Get the row index of the last selected row.
		//
		int lastIndex = -1;
		for (int rowIndex=0; rowIndex<gridModel.getRows().size(); rowIndex++) {
			if (gridModel.getRows().get(rowIndex).isSelected()) {
				lastIndex = rowIndex;
			}
		}
		
		if (lastIndex != -1) {
			selectionModel.toggleRowSelections(Collections.singletonList(gridModel.getRows().get(lastIndex)));
		}
	}

	private void addRowToSelection(final int anchorRowIndex) {
		//
		// Get the row index of the last selected row.
		//
		int lastIndex = -1;
		for (int rowIndex=0; rowIndex<gridModel.getRows().size(); rowIndex++) {
			if (gridModel.getRows().get(rowIndex).isSelected()) {
				lastIndex = rowIndex;
			}
		}
		
		//
		// If the anchor is below the last selected row, use the anchor.
		//
		if (anchorRowIndex > lastIndex) {
			lastIndex = anchorRowIndex - 1;
		}
		
		if (lastIndex == -1) {
			//
			// If there's no selected row, select the anchor row.
			//
			selectionModel.setSelectedRows(Collections.singletonList(gridModel.getRow(selectionModel.getAnchorElement())));

		} else {		
			//
			// Add the next row (if there is one) to the selection.
			//
			if (lastIndex < gridModel.getRows().size()-1) {
				selectionModel.toggleRowSelections(Collections.singletonList(gridModel.getRows().get(lastIndex + 1)));
			}
		}
	}
	
	
}
