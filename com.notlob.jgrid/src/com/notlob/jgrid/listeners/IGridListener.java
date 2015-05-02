package com.notlob.jgrid.listeners;

import java.util.Collection;

import org.eclipse.swt.graphics.Point;

import com.notlob.jgrid.model.Column;

public interface IGridListener<T> {

	void selectionChanged(final Collection<T> selectedElements);

	void click(final Column column, final T element, final Point location, final int modifier);

	void doubleClick(final Column column, final T element, final Point location, final int modifier);

	void rightClick(final Column column, final T element, final Point location, final int modifier);

	void headerClick(final Column column, final Point location, final int modifier);

	void headerDoubleClick(final Column column, final Point location, final int modifier);

	void headerRightClick(final Column column, final Point location, final int modifier);
	
	void elementsAdded(final Collection<T> elements);
	
	void elementsUpdated(final Collection<T> elements);
	
	void elementsRemoved(final Collection<T> elements);
	
	void rowCountChanged();
	
	void filtersChanged();
	
	void columnResized(final Column column);
	
	void columnMoved(final Column column);
	
	void columnSorted(final Column column);
	
	void rowNumbersVisibilityChanged(final boolean visible);
}
