package com.notlob.jgrid.model.filtering;

import com.notlob.jgrid.providers.IGridContentProvider;

/**
 * Applied to the filter model, checks groups to see if they are collapse/expanded.
 * 
 * @author Stef
 */
public class CollapsedGroupFilter<T> extends Filter<T> {

	private final IGridContentProvider<T> contentProvider;
	
	// Single filter match applied to any matching rows.
	private final FilterMatch<T> filterMatch;
	
	public CollapsedGroupFilter(final IGridContentProvider<T> contentProvider) {
		super(LogicalConnective.AND);
		this.contentProvider = contentProvider;
		this.filterMatch = new FilterMatch<T>(this);
	}

	/**
	 * If this element is a child in a collapsed group, it matches the filter.
	 */
	@Override
	public FilterMatch<T> matches(T element) {		
// TODO: Logic is wrong. Should be !collapsed things OR collapse && parent 
		return ((contentProvider.getParent(element) == null) && contentProvider.isCollapsed(element)) ? filterMatch : null;
	}

	@Override
	public String toReadableString() {
		return "Hiding collapsed groups";
	}

}
