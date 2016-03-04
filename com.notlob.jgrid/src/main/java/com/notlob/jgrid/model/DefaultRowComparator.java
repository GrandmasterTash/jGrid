package com.notlob.jgrid.model;

import java.util.Comparator;
import java.util.List;

import com.notlob.jgrid.Grid.GroupRenderStyle;
import com.notlob.jgrid.providers.IGridContentProvider;

public class DefaultRowComparator<T> implements Comparator<Row<T>> {
	
	public enum GroupMixType {
		SORT__GROUPS_ABOVE_NON_GROUPS,
		SORT__NON_GROUPS_ABOVE_GROUPS,
		SORT__NON_GROUPS_WITH_GROUP_PARENTS
	}

	protected boolean parentRowsAboveChildren;
	protected GroupMixType groupMixType;
	protected final GridModel<T> gridModel;
	
	public DefaultRowComparator(final GridModel<T> gridModel) {
		this.gridModel = gridModel;
		parentRowsAboveChildren = false;
		groupMixType = GroupMixType.SORT__GROUPS_ABOVE_NON_GROUPS;
	}
	
	protected IGridContentProvider<T> getContentProvider() {
		return gridModel.getContentProvider();
	}
	
	public List<Column> getSortedColumns() {
		return gridModel.getSortModel().getSortedColumns();
	}
	
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
	protected boolean isParentOf(final Row<T> row1, final Row<T> row2) {
		return (row1.getElement() == gridModel.getContentProvider().getParent(row2.getElement()));
	}

	/**
	 * Protect against comparisons between parent rows (that don't have a field for the sorted column)
	 * and child rows.
	 */
	protected Object getValue(final Column column, final T element) {
		if ((gridModel.getGroupRenderStyle() == GroupRenderStyle.INLINE) && gridModel.isParentElement(element) && !gridModel.getGroupByColumns().contains(column)) {
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
	protected int compareElements(final T element1, final T element2) {
		int result = 0;

		//
		// Compare using column comparators.
		//
		for (final Column column : getSortedColumns()) {
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
		// Fall-back on the index of the element.
		//
		return compareAtRowLevel(element1, element2);
	}
	
	/**
	 * If there are no sorted columns to compare with - or if they yield an equal results,
	 * Fall-back on the index of the element.
	 */
	protected int compareAtRowLevel(final T element1, final T element2) {
		final Integer value1 = getContentProvider().getNaturalIndex(element1);
		final Integer value2 = getContentProvider().getNaturalIndex(element2);
		return value1.compareTo(value2);
	}
}
