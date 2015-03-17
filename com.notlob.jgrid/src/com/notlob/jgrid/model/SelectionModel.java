package com.notlob.jgrid.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectionModel<T> {

	private final GridModel<T> gridModel;
	private final Set<T> selectedElements;
	private T anchorElement;		// The anchor is the current cell cursor.
	private Column anchorColumn;	//
	private Column lastChildAnchorColumn;  // Used when moving the anchor up/down with the keyboard.
	private Column lastParentAnchorColumn; // Gives some consistency to the position rather than snapping to the first column.
	private boolean selectGroupIfAllChildrenSelected = true;

	public SelectionModel(final GridModel<T> gridModel) {
		this.gridModel = gridModel;
		selectedElements = new HashSet<>();
	}
	
	public boolean isSelectGroupIfAllChildrenSelected() {
		return selectGroupIfAllChildrenSelected;
	}
	
	public void setSelectGroupIfAllChildrenSelected(boolean selectGroupIfAllChildrenSelected) {
		this.selectGroupIfAllChildrenSelected = selectGroupIfAllChildrenSelected;
	}

	public T getAnchorElement() {
		return anchorElement;
	}

	public void setAnchorElement(final T anchorElement) {
		this.anchorElement = anchorElement;
	}

	public Column getAnchorColumn() {
		return anchorColumn;
	}

	public void setAnchorColumn(final Column anchorColumn) {
		if (this.anchorColumn != null) {
			this.anchorColumn.setAnchor(false);
		}

		this.anchorColumn = anchorColumn;

		if (anchorColumn != null) {
			anchorColumn.setAnchor(true);

			if (anchorElement != null) {
				//
				// Track the last anchor column by type of element - used in keyboard navigation.
				//
				if (gridModel.isParentElement(anchorElement)) {
					lastParentAnchorColumn = anchorColumn;

				} else {
					lastChildAnchorColumn = anchorColumn;
				}
			}
		}
	}

	public Column getLastChildAnchorColumn() {
		if (lastChildAnchorColumn == null && !gridModel.getColumns().isEmpty()) {
			lastChildAnchorColumn = gridModel.getColumns().get(0);
		}

		return lastChildAnchorColumn;
	}

	public Column getLastParentAnchorColumn() {
		if (lastParentAnchorColumn == null && !gridModel.getGroupByColumns().isEmpty()) {
			lastParentAnchorColumn = gridModel.getGroupByColumns().get(0);
		}

		return lastParentAnchorColumn;
	}

	// TODO: Make this un-modifiable to ensure the .selected flag is ref integ?
	public Set<T> getSelectedElements() {
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
		setSelectedRows(gridModel.getRows());
	}

	public void clear(final boolean notify) {
		for (final Object element : selectedElements) {
			gridModel.getRowsByElement().get(element).setSelected(false);
		}

		selectedElements.clear();

		if (notify) {
			gridModel.fireSelectionChangedEvent();
		}
	}

	/**
	 * Replace the entire selection with the new one.
	 */
	public void setSelectedRows(final List<Row<T>> rowsToSelect) {
		//
		// Clear any existing selection.
		//
		clear(false);

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
			if (anchorColumn != null) {
				anchorColumn.setAnchor(false);
			}

			anchorElement = null;
			anchorColumn = null;

		} else {
			anchorElement = rowsToSelect.get(0).getElement();

			if (anchorColumn  == null && !gridModel.getColumns().isEmpty()) {
				anchorColumn = gridModel.getColumns().get(0);
				anchorColumn.setAnchor(true);
			}
		}

		gridModel.fireSelectionChangedEvent();
	}

	/**
	 * Flips the selected state of the rows specified.
	 */
	public void toggleRowSelections(final List<Row<T>> rowsToToggle) {
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
					anchorElement = row.getElement();
				}
			}
		}

		//
		// If all child rows of a group are selected, select the group.
		//
		checkGroupSelection(rowsToToggle);

		gridModel.fireSelectionChangedEvent();
	}

	void removeRow(final Row<T> row) {
		selectedElements.remove(row.getElement());
		row.setSelected(false);

		if (anchorElement == row.getElement()) {
			anchorElement = null;
		}
	}

	public void selectRange(final Row<T> row, final boolean keepExisting) {
		final int anchorRowIndex = anchorElement == null ? 0 : gridModel.getRows().indexOf(gridModel.getRow(anchorElement));
		final int selectionRowIndex = gridModel.getRows().indexOf(row);
		final int lowerIndex = anchorRowIndex <= selectionRowIndex ? anchorRowIndex : selectionRowIndex;
		final int upperIndex = anchorRowIndex > selectionRowIndex ? anchorRowIndex : selectionRowIndex;
		final List<Row<T>> rowsToSelect = new ArrayList<>();

		for (int rowIndex=lowerIndex; rowIndex<=upperIndex; rowIndex++) {
			rowsToSelect.add(gridModel.getRows().get(rowIndex));
		}

		if (!keepExisting) {
			clear(false);
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
		if (selectGroupIfAllChildrenSelected && !rowsToSelect.isEmpty()) {
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
