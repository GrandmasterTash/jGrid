package com.notlob.jgrid.providers;

import java.util.List;

import com.notlob.jgrid.model.Column;

public interface IGridContentProvider<T> {

	public T getParent(final T element);

	public T[] getChildren(final T element);
	
	public boolean isCollapsed(final T element);

	public Object getValue(final Column column, final T element);

	public String getElementId(final T element);

	public void groupBy(final List<Column> columns);

}
