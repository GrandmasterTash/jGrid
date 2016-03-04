package com.notlob.jgrid.model.filtering;

import com.notlob.jgrid.model.Row;


/**
 * Filters are added to the filter model and each row in the grid is evaluated against them all.
 *
 * If the a row matches a filter it is included in the grids visible rows - if there are filters on the grid and a row meets none,
 * then the row is hidden.
 *
 * @author Stef
 */
public abstract class Filter<T> {
	
	private boolean showWholeGroup;
	
	public Filter() {
		this(true);
	}
			
	public Filter(final boolean showWholeGroup) {
		this.showWholeGroup = showWholeGroup;
	}
	
	public void setShowWholeGroup(final boolean showWholeGroup) {
		this.showWholeGroup = showWholeGroup;
	}
	
	public boolean isShowWholeGroup() {
		return showWholeGroup;
	}

	//
	// Return a result with match = true if the filter matches the row's element otherwise null or match = false.
	//
	public abstract boolean matches(final Row<T> row);

	//
	// What to show the user in the UI when this filter is being applied. The parameter allows widget formatting tokens
	// to be included in the output.
	//
	public abstract String toReadableString(final boolean includeStyleTokens);

	//
	// If show whole group is false, then only matched child elements in a group are shown.
	// This method can be used to ensure some records are always shown if anything else in the group is shown.
	// For example - the parent row itself.
	//
	public boolean mandatoryInGroup(final Row<T> row) {
		return false;
	}
	
	@Override
	public String toString() {
		return toReadableString(false);
	}

}
