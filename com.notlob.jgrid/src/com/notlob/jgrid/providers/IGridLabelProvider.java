package com.notlob.jgrid.providers;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;
import com.notlob.jgrid.styles.StyleCollector;

public interface IGridLabelProvider<T> {

	String getText(final Column column, final T element);

	String getToolTip(final Column column, final T element);

	String getHeaderToolTip(final Column column);

	void getImage(final ImageCollector collector, final Column column, final T element);

	void getHeaderImage(final ImageCollector collector, final Column column);

	void getCellStyle(final StyleCollector styleCollector, final Column column, final T element);

	void getHeaderStyle(final StyleCollector styleCollector, final Column column);

	int getDefaultRowHeight(final T element);
	
	boolean shouldAlternateBackground(final Row<T> previousRow, final Row<T> currentRow);

}
