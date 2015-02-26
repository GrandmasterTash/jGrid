package com.notlob.jgrid;

import java.util.Collection;

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
	public void click(final Column column, final T element, final int modifier) {
	}

	@Override
	public void doubleClick(final Column column, final T element, final int modifier) {
	}

	@Override
	public void rightClick(final Column column, final T element, final int modifier) {
	}

}
