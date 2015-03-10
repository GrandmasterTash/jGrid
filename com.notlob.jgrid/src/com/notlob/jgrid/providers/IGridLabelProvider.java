package com.notlob.jgrid.providers;

import org.eclipse.swt.graphics.Image;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.styles.CellStyle;

public interface IGridLabelProvider<T> {

	String getText(final Column column, final T element);
	
	String getToolTip(final Column column, final T element);
	
	String getHeaderToolTip(final Column column);

	Image getImage(final Column column, final T element);
	
	Image getHeaderImage(final Column column);

	CellStyle getCellStyle(final Column column, final T element);
	
	CellStyle getHeaderStyle(final Column column);
	
	int getDefaultRowHeight(final T element);
	
}
