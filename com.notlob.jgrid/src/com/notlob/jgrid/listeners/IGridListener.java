package com.notlob.jgrid.listeners;

import java.util.Collection;

import org.eclipse.swt.graphics.Point;

import com.notlob.jgrid.model.Column;

public interface IGridListener<T> {

	void gridChanged();
	
	void selectionChanged(final Collection<T> selectedElements);

	void click(final Column column, final T element, final Point location, final int modifier);

	void doubleClick(final Column column, final T element, final Point location, final int modifier);

	void rightClick(final Column column, final T element, final Point location, final int modifier);
	
	void headerClick(final Column column, final Point location, final int modifier);

	void headerDoubleClick(final Column column, final Point location, final int modifier);

	void headerRightClick(final Column column, final Point location, final int modifier);
	
	void groupExpanded(final T element);
	
	void groupCollapsed(final T element);

}
