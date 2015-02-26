package com.notlob.jgrid.model.filtering;

/**
 * Filters are added to the filter model and each row in the grid is evaluated against them all.
 * 
 * If the a row matches a filter it is included in the grids visible rows - if there are filters on the grid and a row meets none,
 * then the row is hidden.
 * 
 * @author Stef
 */
public abstract class Filter<T> {
	
	// Indicates if this filter unions it's results with other filters.
	protected final LogicalConnective logicalConnective;
	
	// Return a match if the filter matches the element otherwise null.
	public abstract FilterMatch<T> matches(final T element);
	
	// What to show the user in the UI when this filter is being applied.
	public abstract String toReadableString();
	
	public Filter(final LogicalConnective logicalConnective) {
		this.logicalConnective = logicalConnective;
	}
		
	public LogicalConnective getLogicalConnective() {
		return logicalConnective;
	}
	
}
