package com.notlob.jgrid.model;

import java.util.Collection;
import java.util.LinkedHashSet;

import org.eclipse.swt.graphics.GC;

import com.notlob.jgrid.model.filtering.IHighlightingFilter;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.util.ResourceManager;

public class Row<T> {

	private boolean visible;
	private boolean selected;
	private boolean pinned;
	private int height;
	private int rowIndex = -1; // Allows updateElements to be implemented without an indexOf.
	private final T element;
	
	// An animation frame counter.
	private int frame;

	// If this row has matched a filter which highlights results, store the matches here.
	private Collection<IHighlightingFilter> filterMatches;

	Row(final T element) {
		this.element = element;
		height = -1;
		frame = -1; // No animation.
	}

	int getHeight(final ResourceManager resourceManager, final GC gc, final CellStyle cellStyle) {
		if (height == -1) {
			if (gc == null) {
				return 0;
			}

			gc.setFont(resourceManager.getFont(cellStyle.getFontData()));
			height = 1 + cellStyle.getPaddingTop() + cellStyle.getPaddingBottom() + gc.getFontMetrics().getHeight();// + (cellStyle.getBorderOuterTop() == null ? 0 : cellStyle.getBorderOuterTop().getWidth());
		}

		return height;
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

	@Override
	public String toString() {
		return String.format("Row : Selected [%s] Height [%s] Pinned [%s] : %s", selected, height, pinned, element);
	}
}
