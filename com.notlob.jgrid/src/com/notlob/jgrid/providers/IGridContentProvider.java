package com.notlob.jgrid.providers;

import java.util.List;

import com.notlob.jgrid.model.Column;

public interface IGridContentProvider<T> {

	T getParent(final T element);

	List<T> getChildren(final T element);

	// NOTE: It is more efficient to leave in the domain model - otherwise the expand/collapse filter would have to constantly look-up the parent element's row in a hashmap.
	// This way the domain model can use the direct parent reference.
	boolean isCollapsed(final T element);

	Object getValue(final Column column, final T element);

	String getElementId(final T element);

}
