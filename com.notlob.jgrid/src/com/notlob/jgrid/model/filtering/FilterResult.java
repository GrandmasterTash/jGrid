package com.notlob.jgrid.model.filtering;

import java.util.Collection;

/**
 * Indicates if a row passes a filter or not.
 * 
 * Note: match can be false, even though there are one or more filtermatches.
 * 
 * @author Stef
 */
public class FilterResult<T> {
	
	private final boolean match;
	private final Collection<FilterMatch<T>> filterMatches;
	
	public FilterResult(final boolean match, final Collection<FilterMatch<T>> filterMatches) {
		this.match = match;
		this.filterMatches = filterMatches;
	}
	
	public Collection<FilterMatch<T>> getFilterMatches() {
		return filterMatches;
	}
	
	public boolean isMatch() {
		return match;
	}
}
