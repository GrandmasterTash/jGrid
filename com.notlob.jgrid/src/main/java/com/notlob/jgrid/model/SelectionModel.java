package com.notlob.jgrid.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.notlob.jgrid.Grid.SelectionStyle;

public class SelectionModel<T> {

	private final GridModel<T> gridModel;
	private final Set<T> selectedElements;
	private final List<Column> selectedColumns;
	private T anchorElement;		// The anchor is the current cell cursor.
	private Column anchorColumn;	//
	private Column lastChildAnchorColumn;  // Used when moving the anchor up/down with the keyboard.
	private Column lastParentAnchorColumn; // Gives some consistency to the position rather than snapping to the first column.
	private boolean selectGroupIfAllChildrenSelected = true;
	private boolean selectNextOnRemove = true;
	private SelectionStyle selectionStyle = SelectionStyle.ROW_BASED;
	
	private final static Logger logger = LoggerFactory.getLogger(SelectionModel.class);

	public SelectionModel(final GridModel<T> gridModel) {
		this.gridModel = gridModel;
		selectedElements = new HashSet<>();
		selectedColumns = new ArrayList<>();
	}
	
	public boolean isSelectGroupIfAllChildrenSelected() {
		return selectGroupIfAllChildrenSelected;
	}
	
	public void setSelectGroupIfAllChildrenSelected(boolean selectGroupIfAllChildrenSelected) {
		this.selectGroupIfAllChildrenSelected = selectGroupIfAllChildrenSelected;
	}
	
	public void setSelectNextOnRemove(final boolean selectNextOnRemove) {
		this.selectNextOnRemove = selectNextOnRemove;
	}
	
	public boolean isSelectNextOnRemove() {
		return this.selectNextOnRemove;
	}
	
	public SelectionStyle getSelectionStyle() {
		return selectionStyle;
	}
	
	public void setSelectionStyle(SelectionStyle selectionStyle) {
		this.selectionStyle = selectionStyle;
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
		this.anchorColumn = anchorColumn;

		if (anchorColumn != null) {
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

	public Set<T> getSelectedElements() {
		return selectedElements;
	}

	private void selectRow(final Row<T> row) {
		if (selectionStyle == SelectionStyle.SINGLE_ROW_BASED) {
			clear(false);
		}
		
		if (row.isVisible()) {
			row.setSelected(true);
			selectedElements.add(row.getElement());
		}
	}

	private void unselectRow(final Row<T> row) {
		row.setSelected(false);
		selectedElements.remove(row.getElement());
		
		if (row.getElement() == anchorElement) {
			anchorElement = null;
			anchorColumn = null;
		}
	}
	
	private void selectColumn(final Column column) {
		if (selectionStyle == SelectionStyle.SINGLE_COLUMN_BASED) {
			clear(false);
		}
		
		if (column.isVisible()) {
			column.setSelected(true);
			selectedColumns.add(column);
		}
	}
	
	private void unselectColumn(final Column column) {
		column.setSelected(false);
		selectedColumns.remove(column);
		
		if (column == anchorColumn) {
			anchorColumn = null;
		}
	}

	public void selectAll() {
		switch (selectionStyle) {
			case MULTI_COLUMN_BASED:
				setSelectedColumns(gridModel.getColumns());
				break;
			
			case ROW_BASED:
				setSelectedRows(gridModel.getRows());
				break;
				
			case SINGLE_CELL_BASED:
			case SINGLE_COLUMN_BASED:
			case SINGLE_ROW_BASED:
				// Do nothing.
				break;
		}
	}

	public void clear(final boolean notify) {
		for (final Object element : selectedElements) {
			gridModel.getRowsByElement().get(element).setSelected(false);
		}
		
		for (final Column column : selectedColumns) {
			column.setSelected(false);
		}

		selectedElements.clear();
		selectedColumns.clear();

		if (notify) {
			gridModel.fireSelectionChangedEvent();
		}
	}
	
	public void setSelectedElements(final Collection<T> elementsToSelect) {
		//
		// Find the rows for each element.
		//
		final List<Row<T>> rowsToSelect = new ArrayList<>();
		
		for (T element : elementsToSelect) {
			final Row<T> row = gridModel.getRow(element);
			
			if (row == null) {
				logger.error("Cannot find row for element to select " + element);
			} else {
				rowsToSelect.add(row);
			}
		}
		
		setSelectedRows(rowsToSelect);
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
			anchorElement = null;
			anchorColumn = null;

		} else {
			anchorElement = rowsToSelect.get(0).getElement();

			if (anchorColumn  == null && !gridModel.getColumns().isEmpty()) {
				anchorColumn = gridModel.getColumns().get(0);
			}
		}

		gridModel.fireSelectionChangedEvent();
	}

	/**
	 * Flips the selected state of the rows specified.
	 */
	public void toggleRowSelections(final List<Row<T>> rowsToToggle) {
		//
		// The first row being selected will become the new anchor.
		//
		boolean firstSelection = true;
		
		final List<Row<T>> fullListToToggle = new ArrayList<Row<T>>(rowsToToggle);		
		//
		// If any of the rows are parent rows - add any children with the same selection state as the parent.
		// This will ensure, if a group row is toggled, the children are toggled to match.
		//
		for (final Row<T> row : rowsToToggle) {
			if (gridModel.isParentRow(row)) {
				for (Row<T> child : gridModel.getWholeGroup(row)) {
					if ((child != row) && (child.isSelected() == row.isSelected())) {
						fullListToToggle.add(child);
					}
				}
			}
		}		

		//
		// Toggle selection state.
		//
		for (final Row<T> row : fullListToToggle) {
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
		checkGroupSelection(fullListToToggle);

		gridModel.fireSelectionChangedEvent();
	}
	
	/**
	 * Flips the selected state of the columns specified.
	 */
	public void toggleColumnSelections(final List<Column> columnsToToggle) {
		//
		// The first column being selected will become the new anchor.
		//
		boolean firstSelection = true;
		
		//
		// Toggle selection state.
		//
		for (final Column column: columnsToToggle) {
			if (column.isSelected()) {
				unselectColumn(column);
			} else {
				selectColumn(column);

				if (firstSelection) {
					firstSelection = false;
					anchorColumn = column;
				}
			}
		}

		gridModel.fireSelectionChangedEvent();
	}

	boolean removeRow(final Row<T> row) {
		//
		// Clear the rows selection state.
		//
		final boolean removed = selectedElements.remove(row.getElement());
		row.setSelected(false);

		if (anchorElement == row.getElement()) {
			//
			// If the row removed had the anchor, clear the anchor.
			//
			anchorElement = null;
			anchorColumn = null;
		}
		
		return removed;
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
	
	public void selectRange(final Column column, final boolean keepExisting) {
		final int anchorColumnIndex = anchorColumn == null ? 0 : gridModel.getColumns().indexOf(anchorColumn);
		final int selectionColumnIndex = gridModel.getColumns().indexOf(column);
		final int lowerIndex = anchorColumnIndex <= selectionColumnIndex ? anchorColumnIndex : selectionColumnIndex;
		final int upperIndex = anchorColumnIndex > selectionColumnIndex ? anchorColumnIndex : selectionColumnIndex;
		final List<Column> columnsToSelect = new ArrayList<>();

		for (int columnIndex=lowerIndex; columnIndex<=upperIndex; columnIndex++) {
			columnsToSelect.add(gridModel.getColumns().get(columnIndex));
		}

		if (!keepExisting) {
			clear(false);
		}

		for (final Column toSelect : columnsToSelect) {
			selectColumn(toSelect);
		}

		gridModel.fireSelectionChangedEvent();
	}
	
	/**
	 * Replace the entire selection with the new one.
	 */
	public void setSelectedColumns(final List<Column> columnsToSelect) {
		//
		// Clear any existing selection.
		//
		clear(false);

		//
		// Select the new columns.
		//
		for (Column column : columnsToSelect) {
			selectColumn(column);
		}

		//
		// Update the selection anchor.
		//
		if (columnsToSelect.isEmpty()) {
			anchorElement = null;
			anchorColumn = null;

		} else {
			anchorColumn = columnsToSelect.get(0);
		}

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
