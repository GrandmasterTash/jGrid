package com.notlob.jgrid.model.filtering;

import java.util.Collection;
import java.util.Comparator;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.providers.IGridContentProvider;

/**
 * Represents one or more selections in a filter drop-down for a given column in the filter row of the grid.
 * 
 * @author Stef
 */
public class QuickFilter<T> extends Filter<T> {

	// The column this filter is restricted to.
	private final Column column;
	
	// One or more values selected from the checked drop-down list.
	private final Collection<?> filterValues;
	
	// Used to grab a value from T.
	private final IGridContentProvider<T> contentProvider;
	
	// Single filter match applied to any matching rows.
	private final FilterMatch<T> filterMatch;
	
	public QuickFilter(final Column column, final IGridContentProvider<T> contentProvider, final Collection<?> filterValues) {
		super(LogicalConnective.AND);
		this.column = column;
		this.filterValues = filterValues;
		this.contentProvider = contentProvider;
		this.filterMatch = new FilterMatch<T>(this);
	}

	/**
	 * Perform an EQUALS on the values selected - if ANY of the selected values matches we're good.
	 */	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public FilterMatch matches(T element) {
		final Comparator comparator = column.getComparator();
		final Object elementValue = contentProvider.getValue(column, element);
		
		for (Object filterValue : filterValues) {
			if (comparator.compare(filterValue, elementValue) == 0) {
				return filterMatch;
			}
		}
				
		return null;
	}

	@Override
	public String toReadableString() {
		return String.format("%s filtered on %s values", column.getCaption(), filterValues.size());
	}
}
