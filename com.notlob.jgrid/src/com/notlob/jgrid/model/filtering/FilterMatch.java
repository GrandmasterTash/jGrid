package com.notlob.jgrid.model.filtering;

import java.util.Arrays;
import java.util.List;

import com.notlob.jgrid.model.Column;

/**
 * Represents a cell matching a filter or part of a filter.
 * 
 * Note: The presence of a FilterMatch doesn't guarantee the row passes the filter. It just denotes that at least part of the row matches part
 * of the filter criteria and is used to highlight the cell.
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
	private final boolean styleable;
	
	// The filter that caused this match.
	private final Filter<T> sourceFilter;
	
	// The column causing the match - null if it's something not shown in a grid.
	private final Column column;
	
	// One or more text ranges that caused the filter to match. In future, if we want the renderer to highlight only the sections of the cell's text which match a
	// filter (rather than all the text) this will allow it.
	private List<FilterMatchRange> matchRanges;

	public FilterMatch(final Filter<T> sourceFilter, final boolean styleable, final FilterMatchRange...ranges) {
		this(sourceFilter, null, styleable, ranges);
	}
	
	public FilterMatch(final Filter<T> sourceFilter, final Column column, final boolean styleable, final FilterMatchRange...ranges) {
		this.sourceFilter = sourceFilter;
		this.column = column;
		this.styleable = styleable;
		
		if (ranges != null) {
			this.matchRanges = Arrays.asList(ranges);
		}
	}
	
	public Column getColumn() {
		return column;
	}
	
	public boolean isStyleable() {
		return styleable;
	}
	
	public Filter<T> getSourceFilter() {
		return sourceFilter;
	}
	
	public List<FilterMatchRange> getMatchRanges() {
		return matchRanges;
	}	
	
}
