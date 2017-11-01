package com.notlob.jgrid.listeners;

import java.util.Collection;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.resources.Point;

public class GridAdapter<T> implements IGridListener<T> {

	@Override
	public void selectionChanged(final Collection<T> selectedElements) {
	}

	@Override
	public void click(final Column column, final T element, final Point location, final int modifier) {
	}

	@Override
	public void doubleClick(final Column column, final T element, final Point location, final int modifier) {
	}

	@Override
	public void rightClick(final Column column, final T element, final Point location, final int modifier) {
	}

	@Override
	public void headerClick(final Column column, final Point location, final int modifier) {
	}

	@Override
	public void headerDoubleClick(final Column column, final Point location, final int modifier) {
	}

	@Override
	public void headerRightClick(final Column column, final Point location, final int modifier) {
	}

	@Override
	public void elementsAdded(Collection<T> elements) {
	}

	@Override
	public void elementsUpdated(Collection<T> elements) {
	}

	@Override
	public void elementsRemoved(Collection<T> elements) {
	}

	@Override
	public void rowCountChanged() {
	}
	
	@Override
	public void filtersChanging() {
	}
	
	@Override
	public void filtersChanged() {
	}
	
	@Override
	public void columnMoved(Column column) {
	}
	
	@Override
	public void columnResized(Column column) {
	}
	
	@Override
	public void columnAboutToSort(Column column) {
	}
	
	@Override
	public void columnSorted(Column column) {
	}
	
	@Override
	public void rowNumbersVisibilityChanged(boolean visible) {
	}
	
	@Override
	public void groupSelectorVisibilityChanged(boolean visible) {
	}
	
	@Override
	public void cellRevealed(final Column column, final T element) {	
	}
}
