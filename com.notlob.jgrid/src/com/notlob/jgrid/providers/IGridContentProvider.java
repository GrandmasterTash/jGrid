package com.notlob.jgrid.providers;

import java.util.List;

import com.notlob.jgrid.model.Column;

public interface IGridContentProvider<T> {

	String getElementId(final T element);
	
	Object getValue(final Column column, final T element);

	T getParent(final T element);

	List<T> getChildren(final T element);

	boolean isCollapsed(final T element);

	void setCollapsed(final T element, final boolean collapsed);

}
