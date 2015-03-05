package com.notlob.jgrid.listeners;

import java.util.Collection;

import org.eclipse.swt.graphics.Point;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.GridModel;

public interface IGridListener<T> {

	void modelChanged(final GridModel<T> model);
	
	void selectionChanged(final Collection<T> selectedElements);

	void click(final Column column, final T element, final Point location, final int modifier);

	void doubleClick(final Column column, final T element, final Point location, final int modifier);

	void rightClick(final Column column, final T element, final Point location, final int modifier);

}
