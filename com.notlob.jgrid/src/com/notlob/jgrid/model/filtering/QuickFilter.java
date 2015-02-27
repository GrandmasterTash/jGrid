package com.notlob.jgrid.model.filtering;

import java.util.Collection;

import com.notlob.jgrid.model.Column;
import com.notlob.jgrid.providers.IGridLabelProvider;

/**
 * Represents one or more selections in a filter drop-down for a given column in the filter row of the grid.
 * 
 * @author Stef
 */
public class QuickFilter<T> extends Filter<T> {

	// The column this filter is restricted to.
	private final Column column;
	
	// One or more values selected from the checked drop-down list.
	private final Collection<String> filterValues;
	
	// Used to grab a value from T.
	private final IGridLabelProvider<T> labelProvider;
	
	// Single filter match applied to any matching rows.
	private final FilterMatch<T> filterMatch;
	
	public QuickFilter(final Column column, final IGridLabelProvider<T> labelProvider, final Collection<String> filterValues) {
		super(LogicalConnective.AND);
		this.column = column;
		this.filterValues = filterValues;
		this.labelProvider = labelProvider;
		this.filterMatch = new FilterMatch<T>(this);
	}

	/**
	 * Perform an EQUALS on the values selected - if ANY of the selected values matches we're good.
	 */	
	@Override
	public FilterMatch<T> matches(T element) {
		final String elementValue = labelProvider.getText(column, element);
		
		for (String filterValue : filterValues) {
			if (filterValue.equals(elementValue)) {
				return filterMatch;
			}
		}
				
		return null;
	}
	
	public Column getColumn() {
		return column;
	}

	public String getToolTip() {
		if (filterValues.isEmpty()) {
			return "Not filtered";
		}
		
		final StringBuilder sb = new StringBuilder();
		sb.append("Filtered on: -\n");
		
		int index = 0;
		for (String filterValue : filterValues) {
			if (index != 0) {
				sb.append("\n");
			}
			
			sb.append(filterValue);
			
			if (index > 20) {
				//
				// don't show more than 20 values in the tool-tip. It could 
				//
				sb.append(String.format("\n  .\n  .\n  ."));
				break;
			}
			
			
			index++;
		}		
		
		return sb.toString();
	}
	
	@Override
	public String toReadableString() {
		if (filterValues.isEmpty()) {
			return "(not filtered)";
		}
		
		if (filterValues.size() > 1) {
			return "(multiple filters)";
		}
		
		return filterValues.iterator().next();
	}
}
