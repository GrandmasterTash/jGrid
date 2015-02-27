package com.notlob.jgrid.model.filtering;

import java.util.ArrayList;
import java.util.Collection;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;

public class FilterModel<T> {
	
	// TODO: Remove filters if columns are removed.
	// TODO: Filter row toggle with drop-downs.
	// TODO: Option to include entire group if any member matches.
	// TODO: Search string parsed into filter-tree.
	// TODO: Highlighting and search result navigation.
	// TODO: Advanced filters with operations.
	// TODO: Assignment filters.
	// TODO: Rule filters.	
	
	private final GridModel<T> gridModel;
	
	// These are the filters presently in place.
	private final Collection<Filter<T>> filters;
	
	public FilterModel(final GridModel<T> gridModel) {
		this.gridModel = gridModel;
		this.filters = new ArrayList<>();
	}
	
	public void addFilters(final Collection<Filter<T>> filters) {
		this.filters.addAll(filters);
		applyFilters();
	}
	
	public void removeFilters(final Collection<Filter<T>> filters) {
		this.filters.removeAll(filters);
		applyFilters();
	}
	
	public void clear() {
		filters.clear();
		applyFilters();
	}
	
	/**
	 * Return true if one or more filters matches the row and it should be shown. If the row matches a filter, a FilterMatch is added 
	 * to the row.
	 */
	public boolean match(final Row<T> row) {
		//
		// Clear any previous match.
		//
		row.setFilterMatch(null);
		
		//
		// If there are no filters, we match.
		//
		if (filters.isEmpty()) {
			return true;
		}
		
		//
		// Check each filter until we find one we match.
		//
		for (Filter<T> filter : filters) {
			final FilterMatch<T> filterMatch = filter.matches(row.getElement());
			if (filterMatch != null) {
				row.setFilterMatch(filterMatch);
				return true;
			}				
		}
		
		return false;
	}
	
	private void applyFilters() {
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
		// TODO: Some rows get tested twice - MAYBE have a hiddenRows list....
		final Collection<Row<T>> rowsToShow = new ArrayList<>();
		for (final Row<T> row : gridModel.getAllRows()) {
			if ((row.getFilterMatch() != null) && match(row)) {
				rowsToShow.add(row);
			}
		}
		
		//
		// Remove the rows to hide from the visible row list.
		//
		gridModel.getRows().removeAll(rowsToHide);
			
		//
		// Insert each visible row into the visible list.
		//
		for (final Row<T> row : rowsToShow) {
			gridModel.showRow(row);
		}
			
		gridModel.fireChangeEvent();
	}

	public QuickFilter<T> getQuickFilterForColumn(final Column column) {
		for (Filter<T> filter : filters) {
			if (filter instanceof QuickFilter) {
				final QuickFilter<T> quickFilter = (QuickFilter<T>) filter;
				if (quickFilter.getColumn() == column) {
					return quickFilter;
				}
			}
		}
		return null;
	}

}
