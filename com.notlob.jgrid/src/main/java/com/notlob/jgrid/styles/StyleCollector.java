package com.notlob.jgrid.styles;

/**
 * Passed to the label provider to collect cell styles and combine them into a composite cell style.
 * 
 * This allows an inheritence of styles to be applied to any given cell.
 * 
 * @author Stef
 *
 */
public class StyleCollector {
	
	private final CompositeCellStyle compositeCellStyle;
	
	public StyleCollector() {
		compositeCellStyle = new CompositeCellStyle();
	}
	
	public void add(final CellStyle innerStyle) {
		compositeCellStyle.add(innerStyle);
	}
	
	public void addFirst(final CellStyle innerStyle) {
		compositeCellStyle.addFirst(innerStyle);
	}
	
	public void clear() {
		compositeCellStyle.clear();
	}
	
	public boolean isEmpty() {
		return compositeCellStyle.isEmpty();
	}
	
	public CellStyle getCellStyle() {
		return compositeCellStyle;
	}
}
