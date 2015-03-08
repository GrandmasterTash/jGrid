package com.notlob.jgrid.model;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.swt.graphics.GC;

import com.notlob.jgrid.model.filtering.FilterMatch;
import com.notlob.jgrid.styles.CellStyle;
import com.notlob.jgrid.util.ResourceManager;

public class Row<T> {

	private boolean visible;
	private boolean selected;
	private boolean pinned;
	private int height;
	private final T element;
	
	// If this row has matched a filter, store the match here.
	private Collection<FilterMatch<T>> filterMatches;
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final static Row COLUMN_HEADER_ROW = new Row(null);
	
	Row(final T element) {
		this.element = element;
		height = -1;
	}

	int getHeight(final GC gc, final CellStyle cellStyle) {
		if (height == -1) {
			if (gc == null) {
				return 0;
			}

			gc.setFont(ResourceManager.getInstance().getFont(cellStyle.getFontData()));
			height = cellStyle.getPaddingTop() + cellStyle.getPaddingBottom() + gc.getFontMetrics().getHeight();
		}

		return height;
	}

	public void setHeight(final int height) {
		this.height = height;
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
		
	public void addFilterMatches(final Collection<FilterMatch<T>> filterMatched) {
		if (filterMatches == null) {
			filterMatches = new ArrayList<>();
		}
		
		filterMatches.addAll(filterMatches);
	}
	
	public Collection<FilterMatch<T>> getFilterMatches() {
		return filterMatches;
	}
	
	public boolean hasFilterMatches() {
		return (filterMatches != null) && (!filterMatches.isEmpty());
	}

	@Override
	public String toString() {
		return String.format("Row : Selected [%s] Height [%s] Pinned [%s] : %s", selected, height, pinned, element);
	}
}
