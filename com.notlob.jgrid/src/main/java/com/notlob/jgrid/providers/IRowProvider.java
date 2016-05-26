package com.notlob.jgrid.providers;

import com.notlob.jgrid.model.Row;

/**
 * The default implementation creates standard grid Rows, but specific implementations of the Grid may wish to
 * override the row with a specialised version. This can be done by implementing createRow.
 * 
 * @author stef
 *
 * @param <T>
 */
public interface IRowProvider<T> {
	
	/**
	 * Create a new Row for the element specified.
	 */
	Row<T> createRow(final T element);

}
