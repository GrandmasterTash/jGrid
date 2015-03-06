package com.notlob.jgrid.model.filtering;

import com.notlob.jgrid.model.Column;

/**
 * Represents a sub-part of a cell's text that has caused a filter to match the cell's row element.
 * 
 * @author Stef
 *
 */
public class FilterMatchRange {
	
	private final Column column;
	private final int startPostion;
	private final int endPosition;
	
	public FilterMatchRange(final Column column) {
		this(column, -1, -1);
	}
	
	public FilterMatchRange(final Column column, final int startPostion, final int endPosition) {
		this.column = column;
		this.startPostion = startPostion;
		this.endPosition = endPosition;
	}
	
	public Column getColumn() {
		return column;
	}
	
	public int getStartPostion() {
		return startPostion;
	}
	
	public int getEndPosition() {
		return endPosition;
	}

}
