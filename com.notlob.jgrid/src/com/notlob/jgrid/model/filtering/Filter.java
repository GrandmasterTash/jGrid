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

//	// TODO: Consider ditching this and moving into an abstract specialisation.
//	protected final boolean includeWholeGroup;

	//
	// Return a result with match = true if the filter matches the row's element otherwise null or match = false.
	//
	public abstract boolean matches(final Row<T> row);

	//
	// What to show the user in the UI when this filter is being applied. The parameter allows widget formatting tokens
	// to be included in the output.
	//
	public abstract String toReadableString(final boolean includeStyleTokens);

	public Filter() {
//		this(true);
	}

//	public Filter(final boolean includeWholeGroup) {
//		this.includeWholeGroup = includeWholeGroup;
//	}
//
//	public boolean isIncludeWholeGroup() {
//		return includeWholeGroup;
//	}

}
