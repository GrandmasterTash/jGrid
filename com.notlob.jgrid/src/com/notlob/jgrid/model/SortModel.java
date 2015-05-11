package com.notlob.jgrid.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SortModel<T> {

	private Comparator<Row<T>> rowComparator;
	private final GridModel<T> gridModel;
	private final List<Column> sortedColumns;

	public SortModel(final GridModel<T> gridModel) {
		this.gridModel = gridModel;
		sortedColumns = new ArrayList<>();
		rowComparator = new DefaultRowComparator<T>(gridModel);
	}

	/**
	 * Called whenever a column is removed from the grid model.
	 */
	void removeColumn(final Column column) {
		sortedColumns.remove(column);
		sequenceColumns();
	}

	public void setRowComparator(final Comparator<Row<T>> rowComparator) {
		this.rowComparator = rowComparator;
	}

	/**
	 * Toggle the column's sort and apply to the current sort model (or replace the current model).
	 */
	public void sort(final Column column, final boolean toggle, final boolean append, final boolean notify) {
		//
		// Toggle the sort direction on the column.
		//
		final SortDirection sortDirection = toggle ? toggleDirection(column.getSortDirection()) : column.getSortDirection();

		if (!append) {
			clearInternal();
		}

		column.setSortDirection(sortDirection);

		//
		// Add the column to the model if it's not already there.
		//
		if (!sortedColumns.contains(column)) {
			sortedColumns.add(column);
			sequenceColumns();// TODO: Not sure we even need to do this, until we persist the columns?
		}

		//
		// Now sort the data.
		//
		Collections.sort(gridModel.getRows(), rowComparator);
		
		//
		// Re-index the rows.
		//
		gridModel.reindex();

		if (notify) {
			gridModel.fireChangeEvent();
			gridModel.fireColumnSortedEvent(column);
		}
	}

	public void refresh() {
		Collections.sort(gridModel.getRows(), rowComparator);
	}

	private SortDirection toggleDirection(final SortDirection sortDirection) {
		if (sortDirection == null) {
			return SortDirection.ASC;
		}

		switch (sortDirection) {
			case ASC:
				return SortDirection.DESC;
			case DESC:
				return SortDirection.NONE;
			case NONE:
				return SortDirection.ASC;
		}
		return null;
	}

	private void sequenceColumns() {
		int index = 0;
		for (final Column column : sortedColumns) {
			column.setSortSequence(index++);
		}
	}

	/**
	 * Clear the current sorts.
	 */
	public void clear() {
		clearInternal();
		Collections.sort(gridModel.getRows(), rowComparator);
		gridModel.reindex();
		gridModel.fireChangeEvent();
	}

	/**
	 * Clears-down the sort model.
	 */
	private void clearInternal() {
		for (final Column column : sortedColumns) {
			column.setSortSequence(0);
			column.setSortDirection(SortDirection.NONE);
		}

		sortedColumns.clear();
	}

	/**
	 * Ascertain where the specified row should live given the current sort model.
	 */
	public int getSortedRowIndex(final Row<T> row) {
		final int index = Collections.binarySearch(gridModel.getRows(), row, rowComparator);
		return (index * -1) - 1;
	}
	
	public List<Column> getSortedColumns() {
		return sortedColumns;
	}
}
