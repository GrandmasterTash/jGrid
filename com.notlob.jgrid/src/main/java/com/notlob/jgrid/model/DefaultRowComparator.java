package com.notlob.jgrid.model;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private final static String REASON__NATURAL_INDEX = "[%s] using natural index for [%s] and [%s]";
	private final static String REASON__ROW_PARENT_OF_ROW = "[%s] because [%s] is parent of [%s]";
	private final static String REASON__ROW_CHILD_OF_ROW = "[%s] because [%s] is child of [%s]";
	private final static String REASON__ONE_GROUP_ONE_NONE_GROUP = "[%s] because [%s] is a group and [%s] is not";
	private final static String REASON__ONE_NONE_GROUP_ONE_GROUP = "[%s] because [%s] is not a group and [%s] is";
	private final static String REASON_PREFIX__COLUMN_COMPARATOR = "[%s] using column comparator [";
	private final static String REASON_CONJUNCTIVE__BECAUSE = "] for [%s] and [%s] because they ";
	private final static String REASON_SUFFIX__SAME_GROUP = "are the same group";
	private final static String REASON_SUFFIX__DIFFERENT_GROUPS = "are in different groups";
	private final static String REASON_SUFFIX__MIX_GROUP_WITH_NONE_GROUP = "mix group with non-group";
	private final static String REASON_SUFFIX__MIX_NONE_GROUP_WITH_GROUP = "mix non-group with group";
	private final static String REASON_SUFFIX__NON_GROUP_ROWS = "are not group rows";
	
	private final static Logger logger = LoggerFactory.getLogger(DefaultRowComparator.class);
	
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
			return logResult(parentRowsAboveChildren ? -1 : 1, row1, row2, REASON__ROW_PARENT_OF_ROW);

		} else if (isParentOf(row2, row1)) {
			return logResult(parentRowsAboveChildren ? 1 : -1, row1, row2, REASON__ROW_CHILD_OF_ROW);
		}

		//
		// Compare siblings within the same group.
		//
		if (gridModel.isSameGroup(row1, row2)) {
			return compareElements(row1.getElement(), row2.getElement(), REASON_SUFFIX__SAME_GROUP, row1, row2);
		}

		//
		// Compare non-group row with a group-rows' parent.
		//
		if (gridModel.isGroupRow(row1) && !gridModel.isGroupRow(row2)) {
			switch (groupMixType) {
			case SORT__GROUPS_ABOVE_NON_GROUPS:
				return logResult(-1, row1, row2, REASON__ONE_GROUP_ONE_NONE_GROUP);
				
			case SORT__NON_GROUPS_ABOVE_GROUPS:
				return logResult(1, row1, row2, REASON__ONE_GROUP_ONE_NONE_GROUP);
				
			case SORT__NON_GROUPS_WITH_GROUP_PARENTS:
				return compareElements(gridModel.getParentOrOwnElement(row1), row2.getElement(), REASON_SUFFIX__MIX_GROUP_WITH_NONE_GROUP, row1, row2);
			}

		} else if (!gridModel.isGroupRow(row1) && gridModel.isGroupRow(row2)) {
			switch (groupMixType) {
			case SORT__GROUPS_ABOVE_NON_GROUPS:
				return logResult(1, row1, row2, REASON__ONE_NONE_GROUP_ONE_GROUP);
				
			case SORT__NON_GROUPS_ABOVE_GROUPS:
				return logResult(-1, row1, row2, REASON__ONE_NONE_GROUP_ONE_GROUP);
				
			case SORT__NON_GROUPS_WITH_GROUP_PARENTS:
				return compareElements(row1.getElement(), gridModel.getParentOrOwnElement(row2), REASON_SUFFIX__MIX_NONE_GROUP_WITH_GROUP, row1, row2);
			}
		}

		//
		// Compare the parents of two group rows (from differing groups).
		//
		if (gridModel.isGroupRow(row1) && gridModel.isGroupRow(row2) && gridModel.getParentOrOwnElement(row1) != null && gridModel.getParentOrOwnElement(row2) != null) {
			return compareElements(gridModel.getParentOrOwnElement(row1), gridModel.getParentOrOwnElement(row2), REASON_SUFFIX__DIFFERENT_GROUPS, row1, row2);
		}

		//
		// Compare two non-group rows.
		//
		return compareElements(row1.getElement(), row2.getElement(), REASON_SUFFIX__NON_GROUP_ROWS, row1, row2);
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
	protected int compareElements(final T element1, final T element2, final String reason, final Row<T> row1, final Row<T> row2) {
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
					return logResult(result, row1, row2, REASON_PREFIX__COLUMN_COMPARATOR + column.getCaption() + REASON_CONJUNCTIVE__BECAUSE + reason);
				}
			}
		}

		//
		// Fall-back on the index of the element.
		//
		return compareAtRowLevel(element1, element2, reason, row1, row2);
	}
	
	/**
	 * If there are no sorted columns to compare with - or if they yield an equal results,
	 * Fall-back on the index of the element.
	 */
	protected int compareAtRowLevel(final T element1, final T element2, final String reason, final Row<T> row1, final Row<T> row2) {
		final int value1 = getContentProvider().getNaturalIndex(element1);
		final int value2 = getContentProvider().getNaturalIndex(element2);
		return logResult(value1 > value2 ? +1 : value1 < value2 ? -1 : 0, row1, row2, REASON__NATURAL_INDEX);
	}
	
	/**
	 * Return the specified result, but if we're tracing, log a trace entry with the specified reason.
	 */
	private int logResult(final int result, final Row<T> row1, final Row<T> row2, final String reason) {
		if (logger.isTraceEnabled()) {
			logger.trace(String.format(reason, result, getContentProvider().getElementId(row1.getElement()), getContentProvider().getElementId(row2.getElement())));
		}
		
		return result;
	}

}
