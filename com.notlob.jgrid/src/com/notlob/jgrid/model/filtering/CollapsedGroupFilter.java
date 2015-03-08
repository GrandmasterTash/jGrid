package com.notlob.jgrid.model.filtering;

import com.notlob.jgrid.providers.IGridContentProvider;

/**
 * Applied to the filter model, checks groups to see if they are collapse/expanded.
 * 
 * @author Stef
 */
public class CollapsedGroupFilter<T> extends Filter<T> {

	private final IGridContentProvider<T> contentProvider;
	
	public CollapsedGroupFilter(final IGridContentProvider<T> contentProvider) {
		this.contentProvider = contentProvider;
	}

	/**
	 * If this element is a child in a collapsed group, it matches the filter.
	 */
	@Override
	public FilterResult<T> matches(T element) {
		final T parent = contentProvider.getParent(element);				
		final boolean match = (parent == null) || (!contentProvider.isCollapsed(parent));
		return match ? new FilterResult<T>(true, null) : null;
	}

	@Override
	public String toReadableString(final boolean includeStyleTokens) {
		return "Hiding collapsed groups";
	}

}
