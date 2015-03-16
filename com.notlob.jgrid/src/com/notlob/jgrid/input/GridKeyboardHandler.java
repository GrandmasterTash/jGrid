package com.notlob.jgrid.input;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.GC;

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
		switch (e.keyCode) {
//			case SWT.ALT:
//				//
//				// Mouse hover feedback has might have changed.
//				//
//				grid.getMouseHandler().setAlt(false);
//				grid.redraw();
//				break;
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
//			case SWT.ALT:
//				//
//				// Mouse hover feedback has might have changed.
//				//
//				grid.getMouseHandler().setAlt(true);
//				grid.redraw();
//				break;

			case SWT.ARROW_UP:
			case SWT.ARROW_DOWN:
			case SWT.ARROW_LEFT:
			case SWT.ARROW_RIGHT:
				//
				// Move the anchor.
				//
				moveAnchor(e.keyCode);
				
				
				
				if (shift) {
					// Expand selection range from previous anchor.
				}
				
				if (!ctrl && !shift) {
					// Set the new selection to the anchor row / column.
					selectionModel.setSelectedRows(Collections.singletonList(gridModel.getRow(selectionModel.getAnchorElement())));
				}

				break;
		}

		//
		// PAGE-UP, PAGE-DOWN
		//

		//
		// HOME, END
		//

		//
		// ENTER, TAB?
		//

		//
		// CTRL+SPACE to select?
		//

	}

	private void moveAnchor(final int direction) {

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
}
