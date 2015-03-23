package com.notlob.jgrid.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.notlob.jgrid.providers.IGridContentProvider;

public class SortModel<T> {

	public enum GroupMixType {
		SORT__GROUPS_ABOVE_NON_GROUPS,
		SORT__NON_GROUPS_ABOVE_GROUPS,
		SORT__NON_GROUPS_WITH_GROUP_PARENTS
	}

	private boolean parentRowsAboveChildren;
	private GroupMixType groupMixType;
	private Comparator<Row<T>> rowComparator;
	private final GridModel<T> gridModel;
	private final List<Column> sortedColumns;

	public SortModel(final GridModel<T> gridModel) {
		this.gridModel = gridModel;
		sortedColumns = new ArrayList<>();
		rowComparator = new DefaultRowComparator();
		parentRowsAboveChildren = false;
		groupMixType = GroupMixType.SORT__GROUPS_ABOVE_NON_GROUPS;
	}

	/**
	 * Called whenever a column is removed from the grid model.
	 */
	void removeColumn(final Column column) {
		sortedColumns.remove(column);
		sequenceColumns();
	}

	public GroupMixType getGroupMixType() {
		return groupMixType;
	}

	public void setGroupMixType(final GroupMixType groupMixType) {
		this.groupMixType = groupMixType;
		refresh();
		gridModel.fireChangeEvent();
	}

	public boolean isParentRowsAboveChildren() {
		return parentRowsAboveChildren;
	}

	public void setParentRowsAboveChildren(final boolean parentRowsAboveChildren) {
		this.parentRowsAboveChildren = parentRowsAboveChildren;
		refresh();
		gridModel.fireChangeEvent();
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
		int rowIndex = 0;
		for (Row<T> row : gridModel.getRows()) {
			row.setRowIndex(rowIndex++);
		}

		if (notify) {
			gridModel.fireChangeEvent();
		}
	}

	void refresh() {
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

	private IGridContentProvider<T> getContentProvider() {
		return gridModel.getContentProvider();
	}

	/**
	 * Compare two rows, enforces that groups always stick together. Just like Take That.
	 */
	private class DefaultRowComparator implements Comparator<Row<T>> {
		@Override
		public int compare(final Row<T> row1, final Row<T> row2) {
			//
			// Compare a parent with one of it's children.
			//
			if (isParentOf(row1, row2)) {
				return parentRowsAboveChildren ? -1 : 1;

			} else if (isParentOf(row2, row1)) {
				return parentRowsAboveChildren ? 1 : -1;
			}

			//
			// Compare siblings within the same group.
			//
			if (gridModel.isSameGroup(row1, row2)) {
				return compareElements(row1.getElement(), row2.getElement());
			}

			//
			// Compare non-group row with a group-rows' parent.
			//
			if (gridModel.isGroupRow(row1) && !gridModel.isGroupRow(row2)) {
				switch (groupMixType) {
				case SORT__GROUPS_ABOVE_NON_GROUPS:
					return -1;
				case SORT__NON_GROUPS_ABOVE_GROUPS:
					return 1;
				case SORT__NON_GROUPS_WITH_GROUP_PARENTS:
					return compareElements(gridModel.getParentOrOwnElement(row1), row2.getElement());
				}

			} else if (!gridModel.isGroupRow(row1) && gridModel.isGroupRow(row2)) {
				switch (groupMixType) {
				case SORT__GROUPS_ABOVE_NON_GROUPS:
					return 1;
				case SORT__NON_GROUPS_ABOVE_GROUPS:
					return -1;
				case SORT__NON_GROUPS_WITH_GROUP_PARENTS:
					return compareElements(row1.getElement(), gridModel.getParentOrOwnElement(row2));
				}
			}

			//
			// Compare the parents of two group rows (from differing groups).
			//
			if (gridModel.isGroupRow(row1) && gridModel.isGroupRow(row2) && gridModel.getParentOrOwnElement(row1) != null && gridModel.getParentOrOwnElement(row2) != null) {
				return compareElements(gridModel.getParentOrOwnElement(row1), gridModel.getParentOrOwnElement(row2));
			}

			//
			// Compare two non-group rows.
			//
			return compareElements(row1.getElement(), row2.getElement());
		}

		/**
		 * Return true if row1 is a parent of row2.
		 */
		private boolean isParentOf(final Row<T> row1, final Row<T> row2) {
			return (row1.getElement() == gridModel.getContentProvider().getParent(row2.getElement()));
		}

		/**
		 * Protect against comparissons between parent rows (that don't have a field for the sorted column)
		 * and child rows.
		 */
		private Object getValue(final Column column, final T element) {
			if (gridModel.isParentElement(element) && !gridModel.getGroupByColumns().contains(column)) {
				return null;
			}

			return getContentProvider().getValue(column, element);
		}

		/**
		 * Compare rows with one another using the column comparators in the sort model.
		 *
		 * If there are no sorts applied, then row element's ids are used as a way of stopping rows from
		 * jumping around whenever sorts are cleared.
		 */
		@SuppressWarnings("unchecked")
		private int compareElements(final T element1, final T element2) {
			int result = 0;

			//
			// Compare using column comparators.
			//
			for (final Column column : sortedColumns) {
				if (column.getSortDirection() != SortDirection.NONE) {
					final Object value1 = getValue(column, element1);
					final Object value2 = getValue(column, element2);
					result = column.getComparator().compare(value1, value2);

					if (column.getSortDirection() == SortDirection.DESC) {
						result *= -1;
					}

					//
					// Don't use more comparators than we have to.
					//
					if (result != 0) {
						return result;
					}
				}
			}

			//
			// Fall-back on the id of the element.
			//
			final String value1 = getContentProvider().getElementId(element1);
			final String value2 = getContentProvider().getElementId(element2);
			return value1.compareTo(value2);
		}
	}
}
