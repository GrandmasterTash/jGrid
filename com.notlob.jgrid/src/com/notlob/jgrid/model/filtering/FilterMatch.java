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
	
	// Should this match be tracked by the filter model? For example, I might apply a filter for cells that contain the text '123'. The filter I use, if trackable,
	// will be stored in the filter model and the UI can be built to let the user navigate from the first matching cell, to the next using the tracked filter matches
	// (i.e. like the 'Find' (ctrl+f) feature in a chrome browser).
	//
	// However, collapsing a group hides the children with a filter - but these filter matches should appear in the same navigation widget so they wont be trackable.
	private final boolean trackable;
	
	// The filter that caused this match.
	private final Filter<T> sourceFilter;
	
	// One or more cells (and optionally bits of text within a cell) that caused the filter to match.
	private List<FilterMatchRange> matchRanges;

	public FilterMatch(final Filter<T> sourceFilter, final boolean trackable, final FilterMatchRange...ranges) {
		this.sourceFilter = sourceFilter;
		this.trackable = trackable;
		
		if (ranges != null) {
			this.matchRanges = Arrays.asList(ranges);
		}
	}
	
	public boolean isTrackable() {
		return trackable;
	}
	
	public Filter<T> getSourceFilter() {
		return sourceFilter;
	}
	
	public List<FilterMatchRange> getMatchRanges() {
		return matchRanges;
	}	
	
}
