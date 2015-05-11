package com.notlob.jgrid.model.filtering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;

public class FilterModel<T> {

	private final GridModel<T> gridModel;

	// These are the filters presently in place.
	private final Collection<Filter<T>> filters;

	// Toggles whether rows that do not pass a highlighting filter should be filtered out of view or not. Other filters still have the normal effect though.
	// For example, if true, rows that match a filter are shown and those that match none are not. If false, a row is shown regardless of whether it matches
	// a highlighting filter or not, although, if a filter exists which isn't a highlighting filter and the row doesn't match it, it will be hidden.
	private boolean hideNoneHighlightedRows = true;

	public FilterModel(final GridModel<T> gridModel) {
		this.gridModel = gridModel;
		this.filters = new ArrayList<>();
	}

	public void addFilters(final Collection<Filter<T>> filters) {
		this.filters.addAll(filters);
		applyFilters();
		gridModel.fireFiltersChangedEvent();
	}

	public void removeFilters(final Collection<Filter<T>> filters) {
		this.filters.removeAll(filters);
		applyFilters();
		gridModel.fireFiltersChangedEvent();
	}

	public Collection<Filter<T>> getFilters() {
		return filters;
	}

	public void clear() {
		filters.clear();
		
		//
		// Add a collapsed group filter to the model. It provides the ability to collapse/expand groups.
		//
		addFilters(Collections.singletonList((Filter<T>) new CollapsedGroupFilter<T>(gridModel.getContentProvider())));
		
		applyFilters();
	}

	public boolean isHideNoneHighlightedRows() {
		return hideNoneHighlightedRows;
	}

	public void setHideNoneHighlightedRows(final boolean hideNoneHighlightedRows) {
		this.hideNoneHighlightedRows = hideNoneHighlightedRows;
	}

	private boolean doesFilterHide(final Filter<T> filter) {
		if (!(filter instanceof IHighlightingFilter)) {
			return true;
		}

		return hideNoneHighlightedRows;
	}

	/**
	 * Return true if one or more filters matches the row and it should be shown. If the row matches a filter, a FilterMatch is added
	 * to the row.
	 */
	public boolean match(final Row<T> row) {
		//
		// Clear any previous match.
		//
		if (row.getFilterMatches() != null) {
			row.getFilterMatches().clear();
		}

		//
		// Check each filter building up all the matches we can.
		//
		boolean allFiltersMatch = true;
		for (final Filter<T> filter : filters) {
			boolean matches = false;
			
			if ((filter.isShowWholeGroup() && gridModel.isGroupRow(row)) || filter.mandatoryInGroup(row)) {
				//
				// If anything in the group match then this row should be shown OR if it's a parent row, always show it.
				//
				for (Row<T> relative : gridModel.getWholeGroup(row)) {
					if (filter.matches(relative)) {
						matches = true;
					}
				}
				
			} else {
				//
				// Just check the individual row.
				//
				matches = filter.matches(row);
			}

			if (!matches && doesFilterHide(filter)) {
				allFiltersMatch = false;
			}
		}

		return allFiltersMatch;
	}

	/**
	 * Run all rows through the current set of filters and hide/show the rows as appropriate.
	 */
	public void applyFilters() {
		//
		// Build a list of rows to hide that are shown.
		//
		final Collection<Row<T>> rowsToHide = new ArrayList<>();
		for (final Row<T> row : gridModel.getRows()) {
			if (!match(row)) {
				rowsToHide.add(row);
			}
		}

		//
		// Build a list of rows to show that are hidden.
		//
		final Collection<Row<T>> rowsToShow = new ArrayList<>();
		for (final Row<T> row : gridModel.getHiddenRows()) {
			if (match(row)) {
				rowsToShow.add(row);
			}
		}

		//
		// Show/hide now (if we did it in the above loops we'd get concurrent modifications).
		//
		for (final Row<T> row : rowsToShow) {
			gridModel.showRow(row);
		}

		for (final Row<T> row : rowsToHide) {
			gridModel.hideRow(row);
		}
		
		//
		// Reseed the row indexes.
		//
		gridModel.reindex();
		gridModel.fireRowCountChangedEvent();
		gridModel.fireChangeEvent();
	}
	
	/**
	 * Reevaluate with the specific row should be shown or now.
	 */
	public void applyFilters(final Row<T> row) {
		final boolean wasVisible = row.isVisible();
		final boolean nowVisible = match(row);
		
		if (wasVisible && !nowVisible) {
			gridModel.hideRow(row);
			
		} else if (!wasVisible && nowVisible) {
			gridModel.showRow(row);			
		}

		//
		// Reseed the row indexes.
		//
		gridModel.reindex();
		gridModel.fireChangeEvent();
	}

}
