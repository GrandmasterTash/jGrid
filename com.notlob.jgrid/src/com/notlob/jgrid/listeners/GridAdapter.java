package com.notlob.jgrid.listeners;

import java.util.Collection;

import org.eclipse.swt.graphics.Point;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.GridModel;

public class GridAdapter<T> implements IGridListener<T> {

	@Override
	public void modelChanged(final GridModel<T> model) {
	}
	
	@Override
	public void selectionChanged(Collection<T> selectedElements) {
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

}
