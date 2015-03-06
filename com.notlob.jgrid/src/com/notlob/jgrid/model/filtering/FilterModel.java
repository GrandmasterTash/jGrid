package com.notlob.jgrid.model.filtering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.notlob.jgrid.model.GridModel;
import com.notlob.jgrid.model.Row;

public class FilterModel<T> {
	
	// TODO: Include entire group if any row is filtered.
	// TODO: Option to include entire group if any member matches.
	// TODO: Search string parsed into filter-tree.
	// TODO: Highlighting and search result navigation.
	
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
	
	public Collection<Filter<T>> getFilters() {
		return Collections.unmodifiableCollection(filters);
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
		if (row.getFilterMatches() != null) {
			row.getFilterMatches().clear();
		}
		
		//
		// Check each filter building up all the matches we can.
		//
		for (Filter<T> filter : filters) {
			final FilterMatch<T> filterMatch = filter.matches(row.getElement());
			if (filterMatch != null) {
				row.addFilterMatch(filterMatch);				
			}
		}
		
		return filters.isEmpty() || row.hasFilterMatches();
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
			
		gridModel.fireChangeEvent();
	}
	
}
