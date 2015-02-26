package com.notlob.jgrid;

import java.util.Collection;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.GridModel;

public interface IGridListener<T> {

	void modelChanged(final GridModel<T> model);
	
	void selectionChanged(final Collection<T> selectedElements);

	void click(final Column column, final T element, final int modifier);

	void doubleClick(final Column column, final T element, final int modifier);

	void rightClick(final Column column, final T element, final int modifier);

}
