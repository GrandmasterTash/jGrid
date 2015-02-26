package com.notlob.jgrid.model.filtering;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the result of a row matching a filter.
 * 
 * It allows us to decorate which parts of a cell's text actually match the filter so it can be highlighted in the grid.
 * 
 * @author Stef
 *
 */
public class FilterMatch<T> {
	
	// The filter that caused this match.
	private final Filter<T> sourceFilter;
	
	// TODO: Column???
	private List<FilterMatchRange> matchRanges;

	public FilterMatch(final Filter<T> sourceFilter, final FilterMatchRange...ranges) {
		this.sourceFilter = sourceFilter;
		if (ranges != null) {
			this.matchRanges = Arrays.asList(ranges);
		}
	}
	
	public Filter<T> getSourceFilter() {
		return sourceFilter;
	}
	
	public List<FilterMatchRange> getMatchRanges() {
		return matchRanges;
	}	
	
}
