package com.notlob.jgrid.providers;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.model.Row;

/**
 * If a tool-tip provider is specified on the grid, then the label providers getToolTip methods will not be
 * invoked. Instead this provider will be, allowing the host to use display their own tool-tips.
 * 
 * @author Stef
 *
 */
public interface IGridToolTipProvider<T> {
	
	void hide();
	
	void showToolTip(final int x, final int y, final Column column, final Row<T> row);

}
