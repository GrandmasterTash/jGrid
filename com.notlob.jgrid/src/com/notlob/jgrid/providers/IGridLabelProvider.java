package com.notlob.jgrid.providers;

import org.eclipse.swt.graphics.Image;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.styles.StyleCollector;

public interface IGridLabelProvider<T> {

	String getText(final Column column, final T element);

	String getToolTip(final Column column, final T element);

	String getHeaderToolTip(final Column column);

	Image getImage(final Column column, final T element);

	Image getHeaderImage(final Column column);

	void getCellStyle(final StyleCollector styleCollector, final Column column, final T element);

	void getHeaderStyle(final StyleCollector styleCollector, final Column column);

	int getDefaultRowHeight(final T element);

}
