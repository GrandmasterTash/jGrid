package com.notlob.jgrid.model.filtering;

/**
 * Represents a sub-part of a cell's text that has caused a filter to match the cell's row element.
 * 
 * @author Stef
 *
 */
public class FilterMatchRange {
	
	private final int startPostion;
	private final int endPosition;
	
	public FilterMatchRange(final int startPostion, final int endPosition) {
		this.startPostion = startPostion;
		this.endPosition = endPosition;
	}
	
	public int getStartPostion() {
		return startPostion;
	}
	
	public int getEndPosition() {
		return endPosition;
	}

}
