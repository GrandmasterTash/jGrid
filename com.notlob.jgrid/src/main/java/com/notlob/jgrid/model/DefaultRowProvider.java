package com.notlob.jgrid.model;

import com.notlob.jgrid.providers.IRowProvider;

/**
 * Default Row creation. Custom versions can create their own Row types.
 * 
 * @author stef
 *
 * @param <T>
 */
public class DefaultRowProvider<T> implements IRowProvider<T> {
	
	@Override
	public Row<T> createRow(final T element) {
		return new Row<T>(element);
	}

}
