package com.notlob.jgrid.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectionModel<T> {

	private final GridModel<T> gridModel;
	private final Set<T> selectedElements;
	private T selectionAnchorElement;	// Used for SHIFT selects.

	public SelectionModel(final GridModel<T> gridModel) {
		this.gridModel = gridModel;
		selectedElements = new HashSet<>();
	}

	public T getSelectionAnchorElement() {
		gridModel.checkWidget();
		return selectionAnchorElement;
	}

	public Set<T> getSelectedElements() {
		gridModel.checkWidget();
		return selectedElements;
	}

	private void selectRow(final Row<T> row) {
		row.setSelected(true);
		selectedElements.add(row.getElement());
	}

	private void unselectRow(final Row<T> row) {
		row.setSelected(false);
		selectedElements.remove(row.getElement());
	}

	public void selectAll() {
		gridModel.checkWidget(); // TODO: Remove all of these - nothing should be accessing this other than through the grid
		setSelectedRows(gridModel.getRows());
	}

	public void clear() {
		gridModel.checkWidget();
		selectedElements.clear();
		selectionAnchorElement = null;
		// BUG: Clear selected property on the rows!!!!
		// TODO: Also, fuse this with the noNotify method and use a param
		gridModel.fireSelectionChangedEvent();
	}

	/**
	 * Replace the entire selection with the new one.
	 */
	public void setSelectedRows(final List<Row<T>> rowsToSelect) {
		gridModel.checkWidget();

		//
		// Clear any existing selection.
		//
		clearSelectionNoNotifty();

		//
		// Select the new rows.
		//
		for (final Row<T> row : rowsToSelect) {
			selectRow(row);
		}

		//
		// If all child rows of a group are selected, select the group.
		//
		checkGroupSelection(rowsToSelect);

		//
		// Update the selection anchor.
		//
		if (rowsToSelect.isEmpty()) {
			selectionAnchorElement = null;
		} else {
			selectionAnchorElement = rowsToSelect.get(0).getElement();
		}

		gridModel.fireSelectionChangedEvent();
	}

	/**
	 * Flips the selected state of the rows specified.
	 */
	public void toggleRowSelections(final List<Row<T>> rowsToToggle) {
		gridModel.checkWidget();
		boolean firstSelection = true;

		//
		// Toggle selection state.
		//
		for (final Row<T> row : rowsToToggle) {
			if (row.isSelected()) {
				unselectRow(row);
			} else {
				selectRow(row);

				if (firstSelection) {
					firstSelection = false;
					selectionAnchorElement = row.getElement();
				}
			}
		}

		//
		// If all child rows of a group are selected, select the group.
		//
		checkGroupSelection(rowsToToggle);

		gridModel.fireSelectionChangedEvent();
	}

	private void clearSelectionNoNotifty() {
		for (final Object element : selectedElements) {
			gridModel.getRowsByElement().get(element).setSelected(false);
		}
		selectedElements.clear();
	}

	void removeRow(final Row<T> row) {
		selectedElements.remove(row.getElement());
		
		// Bug: Update the selected property on the row. 
		
		if (selectionAnchorElement == row.getElement()) {
			selectionAnchorElement = null;
		}
	}

	public void selectRange(final Row<T> row, final boolean keepExisting) {
		gridModel.checkWidget();

		final int anchorRowIndex = selectionAnchorElement == null ? 0 : gridModel.getRows().indexOf(gridModel.getRow(selectionAnchorElement));
		final int selectionRowIndex = gridModel.getRows().indexOf(row);
		final int lowerIndex = anchorRowIndex <= selectionRowIndex ? anchorRowIndex : selectionRowIndex;
		final int upperIndex = anchorRowIndex > selectionRowIndex ? anchorRowIndex : selectionRowIndex;
		final List<Row<T>> rowsToSelect = new ArrayList<>();

		for (int rowIndex=lowerIndex; rowIndex<=upperIndex; rowIndex++) {
			rowsToSelect.add(gridModel.getRows().get(rowIndex));
		}

		if (!keepExisting) {
			clearSelectionNoNotifty();
		}

		for (final Row<T> toSelect : rowsToSelect) {
			selectRow(toSelect);
		}

		//
		// If all child rows of a group are selected, select the group.
		//
		checkGroupSelection(rowsToSelect);

		gridModel.fireSelectionChangedEvent();
	}

	/**
	 * Ensure that if all child rows in a group are selected, then the group row itself is selected.
	 *
	 * Also don't allow a parent row to be selected unless all it's children are.
	 *
	 * We only need to check the first and last rows in the list (we're assuming they are in screen order).
	 */
	private void checkGroupSelection(final List<Row<T>> rowsToSelect) {

		if (!rowsToSelect.isEmpty()) {
			final Row<T> firstRow = rowsToSelect.get(0);
			if (gridModel.isGroupRow(firstRow)) {
				checkGroup(gridModel.getWholeGroup(firstRow));
			}

			//
			// Check the last row in the selection.
			//
			if (rowsToSelect.size() > 1) {
				final Row<T> lastRow = rowsToSelect.get(rowsToSelect.size()-1);
				if (gridModel.isGroupRow(lastRow)) {
					checkGroup(gridModel.getWholeGroup(lastRow));
				}
			}
		}
	}

	private void checkGroup(final List<Row<T>> group) {
		Row<T> parentRow = null;
		boolean allChildrenSelected = true;

		for (final Row<T> row : group) {
			if (gridModel.isParentRow(row)) {
				parentRow = row;

			} else {
				allChildrenSelected &= row.isSelected();
			}
		}

		if (allChildrenSelected) {
			selectRow(parentRow);
		} else {
			unselectRow(parentRow);
		}
	}

}
