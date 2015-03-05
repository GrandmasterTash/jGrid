package com.notlob.jgrid.providers;

import java.util.List;

import com.notlob.jgrid.model.Column;

public interface IGridContentProvider<T> {

	T getParent(final T element);

	T[] getChildren(final T element);
	
	boolean isCollapsed(final T element);

	Object getValue(final Column column, final T element);

	String getElementId(final T element);

	void groupBy(final List<Column> columns);

}
