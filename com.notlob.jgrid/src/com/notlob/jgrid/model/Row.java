package com.notlob.jgrid.model;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.swt.graphics.GC;

import com.notlob.jgrid.Grid;
import com.notlob.jgrid.model.filtering.IHighlightingFilter;
import com.notlob.jgrid.renderer.animation.RowAnimation;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.util.ResourceManager;

public class Row<T> {

	private boolean visible;
	private boolean selected;
	private boolean pinned;
	private boolean alternateBackground;
	private int height;
	private int rowIndex = -1; // Allows updateElements to be implemented without an indexOf.
	private final T element;
	
	// An animation frame counter.
	private int frame;
	private RowAnimation<T> animation;	

	// If this row has matched a filter which highlights results, store the matches here.
	private Collection<IHighlightingFilter> filterMatches;

	Row(final T element) {
		this.element = element;
		height = -1;
		frame = -1; // No animation.
	}

	/**
	 * You should use grid.getRowHeight not this.
	 */
	int getHeight(final Grid<T> grid, final ResourceManager resourceManager, final GC gc, final CellStyle cellStyle) {
		if (height == -1) {
			//
			// Set the initial height based on current values.
			//
			height = getDefaultHeight(resourceManager, gc, cellStyle);
			
			//
			// Now adjust this for word wrapping.
			//
			if (!grid.getGridModel().isHeaderRow(this)) {
				height = grid.getGridRenderer().computeRowHeight(gc, this);
			}
		}

		return height;
	}
	
	/**
	 * The initial starting height for the row - NOT the current height.
	 */
	public int getDefaultHeight(final ResourceManager resourceManager, final GC gc, final CellStyle cellStyle) {
		if (gc == null) {
			return 0;
		}

		gc.setFont(resourceManager.getFont(cellStyle.getFontData()));
		
		// Include the padding and outer border.
		return cellStyle.getPaddingTop() + cellStyle.getPaddingBottom() + gc.getFontMetrics().getHeight() + (cellStyle.getBorderOuterTop() == null ? 0 : cellStyle.getBorderOuterTop().getWidth()) + (cellStyle.getBorderOuterBottom() == null ? 0 : 1);
	}

	public void setHeight(final int height) {
		this.height = height;
	}
	
	public int getRowIndex() {
		return rowIndex;
	}
	
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public T getElement() {
		return element;
	}

	public boolean isVisible() {
		return visible;
	}

	void setVisible(final boolean visible) {
		this.visible = visible;
	}

	public boolean isSelected() {
		return selected;
	}

	// Intentionally package protected - let the selection model use it.
	void setSelected(final boolean selected) {
		this.selected = selected;
	}

	public boolean isPinned() {
		return pinned;
	}

	void setPinned(final boolean pinned) {
		this.pinned = pinned;
	}
	
	public boolean isAlternateBackground() {
		return this.alternateBackground;
	}
	
	public void setAlternateBackground(final boolean alternateBackground) {
		this.alternateBackground = alternateBackground;
	}

	public void addFilterMatch(final IHighlightingFilter filter) {
		if (this.filterMatches == null) {
			this.filterMatches = new LinkedHashSet<>();
		}

		filterMatches.add(filter);
	}

	public Collection<IHighlightingFilter> getFilterMatches() {
		return filterMatches;
	}

	public boolean hasFilterMatches() {
		return (filterMatches != null) && (!filterMatches.isEmpty());
	}
	
	public int getFrame() {
		return frame;
	}
	
	public void setFrame(int frame) {
		this.frame = frame;
	}
	
	public RowAnimation<T> getAnimation() {
		return animation;
	}
	
	public void setAnimation(final RowAnimation<T> animation) {
		this.animation = animation;
	}

	@Override
	public String toString() {
		return String.format("Row : Selected [%s] Height [%s] Pinned [%s] Alternate [%s] : %s", selected, height, pinned, alternateBackground, element);
	}
}
