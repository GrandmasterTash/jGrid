package com.notlob.jgrid.model.filtering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	private final static Logger logger = LoggerFactory.getLogger(FilterModel.class);

	public FilterModel(final GridModel<T> gridModel) {
		this.gridModel = gridModel;
		this.filters = new ArrayList<>();
	}

	public void addFilters(final Collection<Filter<T>> filters) {
		gridModel.fireFiltersChangingEvent();
		this.filters.addAll(filters);
		applyFilters();
		gridModel.fireFiltersChangedEvent();
	}

	public void removeFilters(final Collection<Filter<T>> filters) {
		gridModel.fireFiltersChangingEvent();
		this.filters.removeAll(filters);
		applyFilters();
		gridModel.fireFiltersChangedEvent();
	}
	
	public void setFilters(final Collection<Filter<T>> filtersToRemove, final Collection<Filter<T>> filtersToAdd) {
		gridModel.fireFiltersChangingEvent();
		this.filters.removeAll(filtersToRemove);
		this.filters.addAll(filtersToAdd);
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
			
			if (logger.isTraceEnabled()) {
				logger.trace("Evaluating Filter {} for Row {}", filter, row);	
			}
			
			if ((filter.isShowWholeGroup() && gridModel.isGroupRow(row)) || filter.mandatoryInGroup(row.getElement())) {				
				//
				// If anything in the group match then this row should be shown OR if it's a parent row, always show it.
				//
				for (T relative : gridModel.getWholeGroup(row.getElement())) {
					if (filter.matches(relative)) {
						matches = true;
						break;
					}
				}
				
			} else {
				//
				// Just check the individual row.
				//
				matches = filter.matches(row.getElement());
			}
			
			if (logger.isTraceEnabled()) {
				logger.trace("Filter {} {} for Row {}", filter, matches ? "matches" : "doesn't match", row);	
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
		boolean selectionChanged = false;
		
		//
		// Build a list of rows to hide that are shown.
		//
		final List<Row<T>> rowsToHide = new ArrayList<>();		
		for (final Row<T> row : gridModel.getRows()) {
			if (!match(row)) {
				rowsToHide.add(row);
			}
		}

		//
		// Build a list of rows to show that are hidden.
		//
		final List<Row<T>> rowsToShow = new ArrayList<>();
		for (final Row<T> row : gridModel.getHiddenRows()) {
			if (match(row)) {
				rowsToShow.add(row);
			}
		}
		
		//
		// Show/hide now (if we did it in the above loops we'd get concurrent modifications).
		//
		for (final Row<T> row : rowsToShow) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Showing %s->%s", gridModel.getContentProvider().getElementId(row.getElement()), row));
			}
			
			gridModel.showRow(row, false);
		}
		
		for (final Row<T> row : rowsToHide) {
			if (logger.isTraceEnabled()) {
				logger.trace(String.format("Hiding %s->%s", gridModel.getContentProvider().getElementId(row.getElement()), row));
			}
			
			selectionChanged |= row.isSelected();
			gridModel.hideRow(row, false);
		}
		
		//
		// Index the rows now so we can remove things by index for performance - as removing large number of rows
		// by object references results in much scanning of the list(s).
		//
		gridModel.reindex();
		
		if (!rowsToShow.isEmpty()) {
			for (int rowIndex=rowsToShow.size()-1; rowIndex>=0; rowIndex--) {
				final Row<T> row = rowsToShow.get(rowIndex);
				gridModel.getHiddenRows().remove(row.getHiddenRowIndex());
				row.setHiddenRowIndex(-1);
			}
		}
		
		if (!rowsToHide.isEmpty()) {
			for (int rowIndex=rowsToHide.size()-1; rowIndex>=0; rowIndex--) {
				final Row<T> row = rowsToHide.get(rowIndex);
				gridModel.getRows().remove(row.getRowIndex());
				row.setRowIndex(-1);
			}
		}
		
		//
		// Re-seed the row indexes again after list modifications.
		//
		gridModel.reindex();
		gridModel.fireRowCountChangedEvent();
		gridModel.fireChangeEvent();
		
		if (selectionChanged) {
			gridModel.fireSelectionChangedEvent();
		}
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
