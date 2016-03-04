package com.notlob.jgrid.model.filtering;

import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.providers.IGridContentProvider;

/**
 * Applied to the filter model, checks groups to see if they are collapse/expanded.
 *
 * @author Stef
 */
public class CollapsedGroupFilter<T> extends Filter<T> {

	private final IGridContentProvider<T> contentProvider;

	public CollapsedGroupFilter(final IGridContentProvider<T> contentProvider) {
		super(false);
		this.contentProvider = contentProvider;
	}

	/**
	 * If this element is a child in a collapsed group, it matches the filter.
	 */
	@Override
	public boolean matches(final Row<T> row) {
		final T parent = contentProvider.getParent(row.getElement());
		return (parent == null) || (!contentProvider.isCollapsed(parent));
	}

	@Override
	public String toReadableString(final boolean includeStyleTokens) {
		return "Hiding collapsed groups";
	}

}
